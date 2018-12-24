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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ApplicationWindowLogger class that will handle logging information by writing it to a local file.
 */
public class FileLogger extends BaseLogger
{
    private static final String filenameBase = "log ";
    private static final String filenameExtension = ".txt";

    /**
     * Create a new Base ApplicationWindowLogger that will only maintain
     * log messages that are above the set log level.
     *
     * @param loglevel
     */
    public FileLogger(LogSeverity loglevel)
    {
        super(loglevel);
    }

    @Override
    protected void log(String message, LogSeverity severity)
    {
        try {
            Files.write(getFilePath(),
                    formatMessage(message, severity),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
        }catch (IOException e) {
            // IOException will likely be due to insufficient permissions, which would
            // generally require the user to move the application someplace else (e.g. user documents)
            // where the application will have write access.
        }
    }


    /**
     * Retrieve the file-path for the log file.
     * @return
     */
    private static Path getFilePath() {
        return Paths.get(
                System.getProperty("user.dir"),
                "Logs",
                filenameBase +
                DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()) +
                filenameExtension);
    }

    /**
     * Format the message to include current time and error severity
     * @param message
     * @return
     */
    private static byte[] formatMessage(String message, LogSeverity severity)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(
                DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) +
                " " + LogSeverity.en_EN(severity).toUpperCase() + "\r\n");

        String[] lines = message.split("\\R");

        for (String line : lines)
        {
            builder.append("\t" + line + "\r\n");
        }

        return builder.toString().getBytes();
    }





}
