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

import com.Bluefix.Prodosia.Data.Logger.ILogger;

/**
 * A worker class that will allow the Module logic to run
 * threaded.
 */
public class ModuleWorker extends Thread implements IModuleWorker
{
    private ILogger _logger;
    private ILogger _appLogger;

    //region Variables

    /**
     * The runner behind the worker object.
     */
    private IIntervalRunner _runner;

    /**
     * The previous projected amount of GET requests.
     */
    private int _previousProjection;

    /**
     * The time at which the next cycle is expected to take place.
     * Used when the module is started again.
     */
    private long _expectedCycle;

    /**
     * This boolean indicates that the worker is still functioning and not
     * scheduled for cancellation.
     */
    private boolean _isAlive;

    //endregion

    //region Constructor

    /**
     * Create a new Worker object based around the specified runner.
     * <p>
     * The worker will start executing code when its expected cycle time has
     * been reached.
     *
     * @param runner        The runner behind the worker.
     * @param expectedCycle The expected epoch-time in milliseconds when the thread should start.
     */
    public ModuleWorker(
            IIntervalRunner runner,
            long expectedCycle,
            ILogger logger,
            ILogger appLogger)
    {
        // store the dependencies.
        _logger = logger;
        _appLogger = appLogger;

        _runner = runner;
        _expectedCycle = expectedCycle;
        _previousProjection = -1;
        _isAlive = true;
    }

    //endregion


    /**
     * Keep running the logic with a timed interval unless the thread is stopped.
     */
    @Override
    public void run()
    {
        // if applicable, wait until the expected cycle has arrived.
        long diff = _expectedCycle - System.currentTimeMillis();

        if (diff > 0)
        {
            try
            {
                Thread.sleep(diff);
            } catch (InterruptedException e)
            {
                // any exception here is probably caused by the OS and not our concern.
            }
        }

        // continually execute the logic until it is stopped.
        while (_isAlive)
        {
            try
            {
                runLogic();
            } catch (MaxRequestsExceededException e)
            {
                if (_logger != null)
                    _logger.warn("[ImgurIntervalRunner::Worker] Module " + _runner.getName() + " has exceeded its amount of allotted GET requests.\r\n" + e.getMessage());

                // wait for a minute.
                try
                {
                    Thread.sleep(60000);
                } catch (InterruptedException e1)
                {
                }

            } catch (InterruptedException e)
            {
                if (_logger != null)
                    _logger.warn("[ImgurIntervalRunner::Worker] InterruptedException thrown during execution.\r\n" + e.getMessage());
            } catch (Exception e)
            {
                if (_logger != null)
                    _logger.error("[ImgurIntervalRunner::Worker] Exception thrown during execution.\r\n" + e.getMessage());
            }
        }
    }

    /**
     * Run the underlying module logic with a dynamic time difference, based on the amount of GET requests used.
     *
     * @throws MaxRequestsExceededException thrown if the amount of GET requests is exceeded.
     * @throws InterruptedException                             thrown if the thread is killed.
     */
    private void runLogic() throws MaxRequestsExceededException, InterruptedException
    {
        long startTime = System.currentTimeMillis();

        // get the current projected amount of requests.
        int requests = _runner.projectedRequests();

        // if the amount of requests exceeds the maximum, throw an exception
        if (requests > _runner.getMaximumAllowedRequests())
            throw new MaxRequestsExceededException("The cycle is expected to run " + requests + " GET requests, " +
                    "whereas only " + _runner.getMaximumAllowedRequests() + " are allowed for this module.");


        // if the projected amount of GET requests has increased,
        // we should wait an additional amount of time.
        if (requests > _previousProjection && _previousProjection != -1)
        {
            long delay = getSleep(requests - _previousProjection);
            startTime += delay;

            long sleepRequired = startTime - System.currentTimeMillis();

            if (sleepRequired > 0)
                Thread.sleep(sleepRequired);
        }

        _previousProjection = requests;


        // Execute the logic of the underlying module
        _runner.run();


        // wait the required amount of time.
        long sleepRequired = getSleep(requests);
        long diff = (startTime + sleepRequired) - System.currentTimeMillis();
        _expectedCycle = startTime + sleepRequired;

        if (diff > 0)
            Thread.sleep(diff);
    }


    /**
     * Retrieve the amount of time that should be waited before resuming
     *
     * @param requests The amount of requests that are added.
     * @return The amount of milliseconds that should be padded between cycles.
     */
    private long getSleep(int requests)
    {
        // retrieve the amount of milliseconds in an hour.
        long hourMillis = 60 * 60 * 1000;

        // use the requests as a portion of the hour to indicate how much sleep is necessary.
        return (long) Math.ceil(hourMillis * (requests / (double) _runner.getMaximumAllowedRequests()));
    }

    @Override
    public long getExpectedCycle()
    {
        return _expectedCycle;
    }
}
