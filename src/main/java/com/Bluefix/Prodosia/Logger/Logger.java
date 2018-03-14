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

package com.Bluefix.Prodosia.Logger;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Logger
{
    //region final variables

    private static final int maxLogLength = 5000;
    private static final int maxGuiLogLength = 60;

    //endregion

    private ArrayList<String> log;


    //region Singleton and constructor

    private static Logger myLogger;
    private static Severity severity;

    public static Logger Logger()
    {
        if (myLogger == null)
        {
            myLogger = new Logger();
        }

        return myLogger;
    }


    private Logger()
    {
        log = new ArrayList<>();
        controls = new ArrayList<>();
    }

    //endregion

    //region Logger functionality

    /**
     * Set the current severity of the log that should be maintained.
     * @param severity
     */
    public static void setSeverity(Severity severity)
    {
        Logger.severity = severity;
    }

    public enum Severity
    {
        EMERGENCY("emerg"),
        ALERT("alert"),
        CRITICAL("crit"),
        ERROR("err"),
        WARNING("warning"),
        NOTICE("notice"),
        INFORMATIONAL("info"),
        DEBUG("debug");

        private String text;

        Severity(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }

    /**
     * Log a message to the logger. Without indication, the severity is expected to be informational.
     * @param message
     */
    public static void logMessage(String message)
    {
        logMessage(message, Severity.INFORMATIONAL);
    }

    public static void logMessage(String message, Severity severity)
    {
        // if the severity doesn't override the threshold, ignore the logmessage.
        if (!threshold(severity))
            return;

        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("H:m:s");

        Logger().log.add("(" + ldt.format(f) + ") " + severity.toString() + ": " + message);

        for (int i = maxLogLength; i < Logger().log.size(); i++)
        {
            Logger().log.remove(0);
        }

        // update the controls
        Platform.runLater(
                () -> Logger().updateControls()
        );
    }

    /**
     * Returns true iff the indicated severity is allowed to be logged.
     * @param severity
     * @return
     */
    private static boolean threshold(Severity severity)
    {
        switch (severity)
        {

            case EMERGENCY:
                return  severity == Severity.EMERGENCY;

            case ALERT:
                return  severity == Severity.EMERGENCY ||
                        severity == Severity.ALERT;

            case CRITICAL:
                return  severity == Severity.EMERGENCY ||
                        severity == Severity.ALERT ||
                        severity == Severity.CRITICAL;

            case ERROR:
                return  severity == Severity.EMERGENCY ||
                        severity == Severity.ALERT ||
                        severity == Severity.CRITICAL ||
                        severity == Severity.ERROR;

            case WARNING:
                return  severity == Severity.EMERGENCY ||
                        severity == Severity.ALERT ||
                        severity == Severity.CRITICAL ||
                        severity == Severity.ERROR ||
                        severity == Severity.WARNING;

            case NOTICE:
                return  severity == Severity.EMERGENCY ||
                        severity == Severity.ALERT ||
                        severity == Severity.CRITICAL ||
                        severity == Severity.ERROR ||
                        severity == Severity.WARNING ||
                        severity == Severity.NOTICE;

            case INFORMATIONAL:
                return  severity == Severity.EMERGENCY ||
                        severity == Severity.ALERT ||
                        severity == Severity.CRITICAL ||
                        severity == Severity.ERROR ||
                        severity == Severity.WARNING ||
                        severity == Severity.NOTICE ||
                        severity == Severity.INFORMATIONAL;

            case DEBUG:
                return true;
        }

        return false;
    }

    //endregion




    //region Gui

    private ArrayList<TextInputControl> controls;

    private void updateControls()
    {
        setLoggerText(controls);
    }

    public static void setupOutput(TextInputControl text)
    {
        // add the item to the list of controls and update its text.
        Logger().setLoggerText(text);
        Logger().controls.add(text);
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

    //endregion
}
