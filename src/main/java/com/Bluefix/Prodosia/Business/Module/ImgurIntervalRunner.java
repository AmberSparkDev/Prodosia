/*
 * Copyright (c) 2018 RoseLaLuna
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.Bluefix.Prodosia.Business.Module;

import com.Bluefix.Prodosia.Business.Logger.Logger;

/**
 * This class allows for a subclass to be run in a periodic interval.
 *
 * The class takes into account the maximum amount of GET requests that can
 * be executed and runs the underlying module according to these limitations.
 *
 * Keep in mind that there is no perfect guarantee that the module will remain
 * within the maximum request limit. It is possible that some of the unused requests
 * during one hour are transferred to the next hour. This will have effect on the
 * hourly limit but not on the daily limit. The smaller the difference between the maximum
 * amount of requests and the average requests per cycle, the higher this fluctuation shall be.
 * As such, it is recommended to create more smaller cycles.
 *
 * The disadvantage about more, smaller cycles means that if a user only keeps the application open
 * for a short amount of time, The Imgur API isn't optimally used.
 *
 * Recommendation is to create cycles of about 1/6th of the GET requests, so that it runs every 10 minutes.
 */
public abstract class ImgurIntervalRunner implements AutoCloseable
{
    //region Constructor

    /**
     * The maximum amount of GET requests allowed per reset.
     */
    private int maximumRequests;

    /**
     * Instantiate a new intervalrunner-like object.
     * @param maximumRequests The maximum amount of GET requests allowed per reset.
     */
    protected ImgurIntervalRunner(int maximumRequests)
    {
        this.maximumRequests = maximumRequests;
        this.isRunning = false;
        this.worker = null;
        this.expectedCycle = -1;
    }

    //endregion

    //region Runner Control

    private Worker worker;

    /**
     * Indicates whether the runner is currently active.
     */
    private boolean isRunning;

    private long expectedCycle;

    /**
     * Start the runner. This will initiate periodic running of the module.
     */
    public synchronized void start()
    {
        isRunning = true;

        // if no thread already existed or it was terminated, instantiate a new one and start it.
        if (worker == null || worker.isAlive() == false)
        {
            worker = new Worker(this, expectedCycle);
            worker.start();
        }
    }

    /**
     * Stop the runner. This will pause periodic running but will not interrupt a current cycle.
     */
    public synchronized void stop()
    {
        // store the expected cycle to start at.
        expectedCycle = worker.expectedCycle;

        // shut down the current worker.
        isRunning = false;
        worker.isAlive = false;
        worker = null;
    }

    //endregion

    //region Module methods

    /**
     * Execute a single cycle.
     */
    protected abstract void run();

    /**
     * Indicate the total amount of GET requests that were executed during the last cycle
     * @return The maximum amount of GET requests.
     */
    protected abstract int projectedRequests();

    //endregion

    //region Threaded runner logic

    /**
     * The Worker class where the actual logic takes place.
     *
     * The functionality inside this class is multi-threaded.
     */
    private static class Worker extends Thread
    {
        //region Variables

        /**
         * The runner behind the worker object.
         */
        private ImgurIntervalRunner runner;

        /**
         * The previous projected amount of GET requests.
         */
        private int previousProjection;

        /**
         * The time at which the next cycle is expected to take place.
         * Used when the module is started again.
         */
        private long expectedCycle;

        /**
         * This boolean indicates that the worker is still functioning and not
         * scheduled for cancellation.
         */
        private boolean isAlive;

        //endregion

        //region Constructor

        /**
         * Create a new Worker object based around the specified runner.
         *
         * The worker will start executing code when its expected cycle time has
         * been reached.
         * @param runner The runner behind the worker.
         * @param expectedCycle The expected epoch-time in milliseconds when the thread should start.
         */
        public Worker(ImgurIntervalRunner runner, long expectedCycle)
        {
            this.runner = runner;
            this.expectedCycle = expectedCycle;
            this.previousProjection = -1;
            this.isAlive = true;
        }

        //endregion



        /**
         * Keep running the logic with a timed interval unless the thread is stopped.
         */
        @Override
        public void run()
        {
            // if applicable, wait until the expected cycle has arrived.
            long diff = expectedCycle - System.currentTimeMillis();

            if (diff > 0)
            {
                try
                {
                    Thread.sleep(diff);
                }
                catch (InterruptedException e)
                {

                }
            }

            // continually execute the logic until it is stopped.
            while (isAlive)
            {
                try
                {
                    runLogic();
                }
                catch (IntervalRunnerException e)
                {
                    Logger.logMessage(e.getMessage(), Logger.Severity.ERROR);
                } catch (InterruptedException e)
                {
                    Logger.logMessage(e.getMessage(), Logger.Severity.ERROR);
                }
            }
        }

        private void runLogic() throws IntervalRunnerException, InterruptedException
        {
            long startTime = System.currentTimeMillis();

            // get the current projected amount of requests.
            int requests = runner.projectedRequests();

            // if the amount of requests exceeds the maximum, throw an exception
            if (requests > runner.maximumRequests)
                throw new IntervalRunnerException("The cycle is expected to run " + requests + " GET requests, " +
                        "whereas only " + runner.maximumRequests + " are allowed for this module.");


            // if the projected amount of GET requests has increased,
            // we should wait an additional amount of time.
            if (requests > previousProjection && previousProjection != -1)
            {
                long delay = getSleep(requests - previousProjection);
                startTime += delay;

                long sleepRequired = startTime - System.currentTimeMillis();

                if (sleepRequired > 0)
                    Thread.sleep(sleepRequired);
            }

            previousProjection = requests;



            // Execute the logic of the underlying module
            runner.run();


            // wait the required amount of time.
            long sleepRequired = getSleep(requests);
            long diff = (startTime + sleepRequired) - System.currentTimeMillis();
            expectedCycle = startTime + sleepRequired;

            if (diff > 0)
                Thread.sleep(diff);
        }


        /**
         * Retrieve the amount of time that should be waited before resuming
         * @param requests The amount of requests that are added.
         * @return The amount of milliseconds that should be padded between cycles.
         */
        private long getSleep(int requests)
        {
            // retrieve the amount of milliseconds in an hour.
            long hourMillis = 60 * 60 * 1000;

            // use the requests as a portion of the hour to indicate how much sleep is necessary.
            return (long)Math.ceil(hourMillis * (requests / (double)runner.maximumRequests));
        }
    }

    //endregion

    //region IntervalRunner Exception

    public static class IntervalRunnerException extends Exception
    {
        public IntervalRunnerException(String exception)
        {
            super(exception);
        }
    }

    //endregion

    //region Autocloseable implementation

    @Override
    public void close()
    {
        this.stop();
    }


    //endregion

}
