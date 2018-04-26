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

package com.Bluefix.Prodosia.Application;


import com.Bluefix.Prodosia.GUI.Navigation.GuiApplication;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Module.ModuleManager;
import com.github.kskelm.baringo.BaringoClient;
import com.github.kskelm.baringo.util.BaringoApiException;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URISyntaxException;


public class Main
{
    private static String duplicateMsg = "duplicate";
    private static String uniqueIdentifier = "85386f61-c164-4671-9dfc-1e50ed2b9541";

    public static void main(String[] args)
    {
        // ensure that there wasn't a former instance of the application already running.
        try
        {
            JUnique.acquireLock(uniqueIdentifier, s ->
            {
                if (duplicateMsg.equals(s))
                    VistaNavigator.show();

                return null;
            });
        }
        catch (AlreadyLockedException e)
        {
            JUnique.sendMessage(uniqueIdentifier, duplicateMsg);
            System.exit(0);
        }


        System.out.println("com.Bluefix.Prodosia.Application started");

        // launch the VistaNavigator
        VistaNavigator.initialize();
    }
}
