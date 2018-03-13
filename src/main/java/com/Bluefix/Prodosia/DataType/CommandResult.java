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

package com.Bluefix.Prodosia.DataType;

/**
 * Helper class that contains all necessary information after command execution.
 */
public  class CommandResult
{
    private String message;
    private String command;

    /**
     * Create a new ExecutionResult object.
     * @param message The message pertaining to the execution of the command.
     * @param command The command that was executed. Null if no command was recognized.
     */
    public CommandResult(String message, String command)
    {
        this.message = message;
        this.command = command;
    }

    /**
     * Retrieve the message for the executed command.
     * @return The message to be displayed to the user that issued the command.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @return The command issued by the user, or null if the command was not recognized.
     */
    public String getCommand()
    {
        return command;
    }
}
