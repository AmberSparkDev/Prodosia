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

import com.Bluefix.Prodosia.Business.Imgur.CommentDeletion.CommentDeletionExecution;
import com.Bluefix.Prodosia.Business.Imgur.CommentScanner.CommentScannerExecution;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.IImgurManager;
import com.Bluefix.Prodosia.Business.Imgur.Tagging.CommentExecution;
import com.Bluefix.Prodosia.Business.Imgur.UserSanitation.UserSanitationModule;
import com.Bluefix.Prodosia.Data.Logger.ILogger;

/**
 * Module manager for all Imgur modules that are run in the background.
 */
public class ModuleManager
{
    private CommentDeletionExecution _commentDeletionExecution;
    private CommentScannerExecution _commentScannerExecution;
    private CommentExecution _commentExecution;
    private UserSanitationModule _sanitationModule;

    public ModuleManager(
            IImgurManager imgurManager,
            ILogger logger,
            ILogger appLogger)
    {
        _commentScannerExecution = new CommentScannerExecution(imgurManager, logger, appLogger);
        _commentDeletionExecution = new CommentDeletionExecution(imgurManager, logger, appLogger);
        _commentExecution = new CommentExecution(imgurManager, logger, appLogger);
        _sanitationModule = new UserSanitationModule(imgurManager, logger, appLogger);

        _commentScannerExecution.start();
        _commentDeletionExecution.start();
        _commentExecution.start();
        _sanitationModule.start();
    }
}
