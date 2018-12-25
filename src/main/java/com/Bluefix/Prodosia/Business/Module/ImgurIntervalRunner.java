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
 * This class allows for a subclass to be run in a periodic interval.
 * <p>
 * The class takes into account the maximum amount of GET requests that can
 * be executed and runs the underlying module according to these limitations.
 * <p>
 * Keep in mind that there is no perfect guarantee that the module will remain
 * within the maximum request limit. It is possible that some of the unused requests
 * during one hour are transferred to the next hour. This will have effect on the
 * hourly limit but not on the daily limit. The smaller the difference between the maximum
 * amount of requests and the average requests per cycle, the higher this fluctuation shall be.
 * As such, it is recommended to create more smaller cycles.
 * <p>
 * The disadvantage about more, smaller cycles means that if a user only keeps the application open
 * for a short amount of time, The Imgur API isn't optimally used.
 * <p>
 * Recommendation is to create cycles of about 1/6th of the GET requests, so that it runs every 10 minutes.
 */
public abstract class ImgurIntervalRunner implements IIntervalRunner, AutoCloseable
{
    protected ILogger _logger;
    protected ILogger _appLogger;

    private IModuleWorker _worker;

    /**
     * The maximum amount of GET requests allowed per reset.
     */
    private int _maximumRequests;

    /**
     * Indicates whether the runner is currently active.
     */
    private boolean _isRunning;

    private long _expectedCycle;

    /**
     * Instantiate a base object that can maintain functionality that runs at an interval
     *
     * @param maximumRequests The maximum amount of GET requests allowed per reset.
     */
    protected ImgurIntervalRunner(
            int maximumRequests,
            ILogger logger,
            ILogger appLogger)
    {
        // store the dependencies
        _logger = logger;
        _appLogger = appLogger;

        _maximumRequests = maximumRequests;
        _isRunning = false;
        _worker = null;
        _expectedCycle = -1;
    }

    /**
     * Start the runner. This will initiate periodic running of the module.
     */
    @Override
    public synchronized void start()
    {
        _isRunning = true;

        // if no thread already existed or it was terminated, instantiate a new one and start it.
        if (_worker == null || _worker.isAlive() == false)
        {
            _worker = new ModuleWorker(this, _expectedCycle, _logger, _appLogger);
            _worker.start();
        }
    }

    /**
     * Stop the runner. This will pause periodic running but will not interrupt a current cycle.
     */
    @Override
    public synchronized void stop()
    {
        // store the expected cycle to start at.
        _expectedCycle = _worker.getExpectedCycle();

        // shut down the current worker.
        _isRunning = false;
        _worker.stop();
        _worker = null;
    }

    @Override
    public int getMaximumAllowedRequests()
    {
        return _maximumRequests;
    }


    @Override
    public void close()
    {
        this.stop();
    }
}
