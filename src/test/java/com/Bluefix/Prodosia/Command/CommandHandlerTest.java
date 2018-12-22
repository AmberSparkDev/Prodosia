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

package com.Bluefix.Prodosia.Command;

import com.Bluefix.Prodosia.Business.Command.CommandHandler;
import com.Bluefix.Prodosia.Data.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.Data.DataType.Tracker.Tracker;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.verify;

public class CommandHandlerTest
{
    @Mock
    CommandInformation commandInformation;

    @Mock
    Tracker tracker;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();



    @Before
    public void setUp() throws Exception
    {

    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void testList() throws Exception
    {
        CommandHandler.execute(commandInformation, "list");
        verify(commandInformation).reply(Matchers.anyString());
    }


    @Test
    public void testGenericHelp() throws Exception
    {
        CommandHandler.execute(commandInformation, "help");
        verify(commandInformation).reply(
                "Use the `list` command to retrieve a list of possible commands.\n" +
                "Use `help` with the specific command(s) as argument for information.");
    }

    @Test
    public void testNonExistentHelp() throws Exception
    {
        CommandHandler.execute(commandInformation, "help nonexistentcommand");
        verify(commandInformation).reply(
                "(nonexistentcommand): The command does not exist.");
    }

    @Test
    public void testExistentHelp() throws Exception
    {
        CommandHandler.execute(commandInformation, "help sub");
        verify(commandInformation).reply("(sub): Please visit https://github.com/RoseLaLuna/Prodosia/wiki/Subscription-Command for information");
    }

    @Test
    public void testExecuteNonexistentCommand() throws Exception
    {
        CommandHandler.execute(commandInformation, "NonexistentCommand");
        verify(commandInformation).reply("Command \"nonexistentcommand\" was not recognized!");
    }

    @Test
    public void testListHelp() throws Exception
    {
        CommandHandler.execute(commandInformation, "help list");
        verify(commandInformation).reply("(list): Will list all of the known commands.");
    }
}