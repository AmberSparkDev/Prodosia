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

package com.Bluefix.Prodosia.Business.Tracker;

import com.Bluefix.Prodosia.Business.Exception.BaringoExceptionHelper;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.IImgurManager;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.util.BaringoApiException;

public class TrackerLogic implements ITrackerLogic
{
    private IImgurManager _imgurManager;
    private ILogger _logger;
    private ILogger _appLogger;

    public TrackerLogic(IImgurManager imgurManager,
                        ILogger logger,
                        ILogger appLogger)
    {
        // store the dependencies
        _imgurManager = imgurManager;
        _logger = logger;
        _appLogger = appLogger;

        if (_imgurManager == null)
            throw new IllegalArgumentException("The Imgur Manager cannot be null.");
    }


    @Override
    public TrackerMetaInfo fetchTracker(String username)
    {
        try
        {
            Account acc = _imgurManager.getClient().accountService().getAccount(username);

            if (acc != null)
                return new TrackerMetaInfo(acc.getId());

        } catch (BaringoApiException e)
        {
            if (BaringoExceptionHelper.isNotFound(e))
            {
                if (_logger != null)
                    _logger.info("[TrackerLogic] The tracker with name \"" + username + "\" could not be found.");
            }
            else
            {
                if (_logger != null)
                    _logger.warn("[TrackerLogic] BaringoApiException thrown while attempting to fetch the tracker " +
                            "with name \"" + username + "\"\r\n" + e.getMessage());
            }
        }

        return null;
    }
}
