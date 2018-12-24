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

package com.Bluefix.Prodosia.Business.Logger;

import com.Bluefix.Prodosia.Data.Enum.LogSeverity;
import com.Bluefix.Prodosia.Data.Logger.BaseLogger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ApplicationWindowLogger extends BaseLogger
{
    //region final variables

    private static final int maxLogLength = 5000;
    private static final int maxGuiLogLength = 60;

    //endregion

    private ArrayList<String> log;


    //region Singleton and constructor

    public ApplicationWindowLogger()
    {
        super(LogSeverity.INFO);

        log = new ArrayList<>();
        controls = new ArrayList<>();
    }

    //endregion

    //region ApplicationWindowLogger functionality


    @Override
    protected void log(String message, LogSeverity severity)
    {

        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("H:m:s");

        log.add("(" + ldt.format(f) + "): " + message);

        for (int i = maxLogLength; i < log.size(); i++)
        {
            log.remove(0);
        }

        // update the controls
        Platform.runLater(
                () -> updateControls()
        );
    }

    //endregion




    //region Gui

    private ArrayList<TextInputControl> controls;

    private void updateControls()
    {
        setLoggerText(controls);
    }

    public void setupOutput(TextInputControl text)
    {
        // add the item to the list of controls and update its text.
        setLoggerText(text);
        controls.add(text);
    }


    private void setLoggerText(TextInputControl control)
    {
        control.setText(getControlText());
    }

    private void setLoggerText(ArrayList<TextInputControl> control)
    {
        String controlText = getControlText();

        for (TextInputControl tic : control)
        {
            tic.setText(controlText);

            // if the window is a textarea, scroll to the bottom.
            if (tic instanceof TextArea)
            {
                TextArea ta = (TextArea) tic;
                ta.layout();

                ta.setScrollTop(Double.MAX_VALUE);
            }
        }
    }

    private String getControlText()
    {
        // determine the text to be set.
        StringBuilder strB = new StringBuilder();

        for (int i = log.size() - 1; i >= 0 && i >= log.size() - maxGuiLogLength; i--)
        {
            if (i == log.size() - 1)
                strB.insert(0, log.get(i));
            else
                strB.insert(0, log.get(i) + "\n");
        }

        return strB.toString();
    }


    {

    }

    //endregion
}
