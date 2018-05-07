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

package com.Bluefix.Prodosia.DataHandler;

import com.Bluefix.Prodosia.Prefix.CommandPrefix;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class CommandPrefixStorageTest extends DataHandlerTest<CommandPrefix>
{
    /**
     * Keep track of the old prefix item in the database, and restore it at the end.
     */
    private CommandPrefix prefix;




    //region abstract method implementation

    public CommandPrefixStorageTest(boolean useLocalStorage)
    {
        super(useLocalStorage);
    }

    @Override
    protected LocalStorageHandler<CommandPrefix> getHandler()
    {
        return CommandPrefixStorage.handler();
    }

    @Override
    protected CommandPrefix getItem()
    {
        return this.prefix;
    }

    //endregion


    @Before
    public void setUp()
    {
        prefix = new CommandPrefix(CommandPrefix.Type.TEST, "testPrefix");

    }

    @After
    public void tearDown() throws Exception
    {
        CommandPrefixStorage.handler().remove(prefix);
    }


    @Test
    public void testGetCommandPrefixForType() throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        CommandPrefix myPrefix = CommandPrefixStorage.getPrefixForType(CommandPrefix.Type.TEST);
        Assert.assertNull(myPrefix);

        CommandPrefixStorage.handler().set(prefix);

        myPrefix = CommandPrefixStorage.getPrefixForType(CommandPrefix.Type.TEST);
        Assert.assertNotNull(myPrefix);
        Assert.assertEquals("testPrefix", myPrefix.getRegex());
    }


}