/*
 * Copyright (c) 2018 J.S. Boellaard
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

package com.Bluefix.Prodosia.Module;

import com.Bluefix.Prodosia.Imgur.CommentDeletion.CommentDeletionExecution;
import com.Bluefix.Prodosia.Imgur.CommentScanner.CommentScannerExecution;
import com.Bluefix.Prodosia.Imgur.Tagging.CommentExecution;

/**
 * Global module manager
 */
public class ModuleManager
{
    //region Variables, Constructor and Singleton

    private CommentDeletionExecution commentDeletionExecution;
    private CommentScannerExecution commentScannerExecution;
    private CommentExecution commentExecution;

    private static ModuleManager me;

    private static ModuleManager handler()
    {
        if (me == null)
            me = new ModuleManager();

        return me;
    }

    private ModuleManager()
    {

    }

    //endregion


    /**
     * Start execution of the application modules.
     */
    public static void start()
    {
        // initialize all the module values
        handler().commentScannerExecution = CommentScannerExecution.handler();
        handler().commentDeletionExecution = CommentDeletionExecution.handler();
        handler().commentExecution = CommentExecution.handler();

        // start the modules
        handler().commentScannerExecution.start();
        //handler().commentDeletionExecution.start();
        handler().commentExecution.start();
    }

}
