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

package com.Bluefix.Prodosia.Data.Logger;

import com.Bluefix.Prodosia.Data.Enum.LogSeverity;

/**
 * Helper class for filtered logging.
 */
public abstract class BaseLogger implements ILogger
{
    private LogSeverity _loglevel;

    /**
     * Create a new Base ApplicationWindowLogger that will only maintain
     * log messages that are above the set log level.
     * @param loglevel
     */
    public BaseLogger(LogSeverity loglevel)
    {
        _loglevel = loglevel;
    }


    /**
     * The internal logger will have to handle the eventual processing of the log message.
     * @param message The message to be processed.
     * @param severity The severity of the log message.
     */
    protected abstract void log(String message, LogSeverity severity);



    public void debug(String message)
    {
        if (    _loglevel == LogSeverity.DEBUG)
            log(message, LogSeverity.DEBUG);
    }

    public void info(String message)
    {
        if (    _loglevel == LogSeverity.DEBUG ||
                _loglevel == LogSeverity.INFO)
            log(message, LogSeverity.INFO);
    }

    public void warn(String message)
    {
        if (    _loglevel == LogSeverity.DEBUG ||
                _loglevel == LogSeverity.INFO ||
                _loglevel == LogSeverity.WARN)
            log(message, LogSeverity.WARN);
    }

    public void error(String message)
    {
        if (    _loglevel != LogSeverity.OFF &&
                _loglevel != LogSeverity.FATAL)
            log(message, LogSeverity.ERROR);
    }

    public void fatal(String message)
    {
        if (    _loglevel != LogSeverity.OFF)
            log(message, LogSeverity.FATAL);
    }

}
