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

package com.Bluefix.Prodosia.Data.Storage;

import com.Bluefix.Prodosia.Data.DataType.ImgurKey;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Manager for storing keys.
 * <p>
 * The keys are stored in plaintext.
 */
public class KeyStorage implements IKeyStorage
{
    /**
     * Filepath for storage
     */
    private String _filepath;

    /**
     * The imgur key.
     */
    private ImgurKey _imgurKey;

    /**
     * The discord bot token.
     */
    private String _discordToken;


    /**
     * Create a new KeyStorage that will save into the specified file.
     *
     * @param filepath The path to the file that will hold the keys.
     */
    public KeyStorage(String filepath)
    {
        _filepath = filepath;

        // todo: remove once no longer necessary.
        _filepath = System.getProperty("user.dir") + "/keys.txt";
    }

    private static String storageMessage()
    {
        return
                "This document will contain any API keys known to the system.\n" +
                        "Unless you know what you are doing, please don't tamper with this file.\n" +
                        "When sharing the application with others, remove this file to keep the credentials secret.\n\n";
    }

    /**
     * Set the imgur credentials.
     *
     * @param clientId     The client id.
     * @param clientSecret The client secret.
     * @param callback     The callback url.
     * @throws IOException
     */
    @Override
    public void setImgurKey(String clientId, String clientSecret, String callback) throws IOException
    {
        _imgurKey = new ImgurKey(clientId, clientSecret, callback);
        writeStorage();
    }

    /**
     * Retrieve the imgur key.
     *
     * @return the imgur key known by the system or null if it didn't exist.
     * @throws IOException
     */
    @Override

    public ImgurKey getImgurKey() throws IOException
    {
        readStorage();
        return _imgurKey;
    }

    /**
     * Retrieve the discord token.
     *
     * @return The discord token known by the system or null if it didn't exist.
     * @throws IOException
     */
    @Override
    public String getDiscordToken() throws IOException
    {
        readStorage();
        return _discordToken;
    }

    /**
     * Set the discord token.
     *
     * @param token The token to be used.
     * @throws IOException
     */
    @Override
    public void setDiscordToken(String token) throws IOException
    {
        _discordToken = token;
        writeStorage();
    }

    private void readStorage()
    {
        String imgurClientId = null;
        String imgurClientSecret = null;
        String imgurCallback = null;

        ArrayList<DataStorage.Item> items = DataStorage.readItems(_filepath);

        // if the file did not exist, return
        if (items == null)
            return;

        for (DataStorage.Item i : items)
        {
            if (Identifier.ImgurClientId.toString().equals(i.getName()))
            {
                imgurClientId = i.getData();
            } else if (Identifier.ImgurClientSecret.toString().equals(i.getName()))
            {
                imgurClientSecret = i.getData();
            } else if (Identifier.ImgurCallback.toString().equals(i.getName()))
            {
                imgurCallback = i.getData();
            } else if (Identifier.DiscordToken.toString().equals(i.getName()))
            {
                _discordToken = i.getData();
            }
        }

        _imgurKey = null;

        if (imgurClientId != null && imgurClientSecret != null && imgurCallback != null)
            _imgurKey = new ImgurKey(imgurClientId, imgurClientSecret, imgurCallback);
    }

    private void writeStorage() throws IOException
    {
        // add the items to be stored to the list.
        ArrayList<DataStorage.Item> items = new ArrayList<>();
        items.add(new DataStorage.Item(Identifier.ImgurClientId.toString(), _imgurKey.getClientId()));
        items.add(new DataStorage.Item(Identifier.ImgurClientSecret.toString(), _imgurKey.getClientSecret()));
        items.add(new DataStorage.Item(Identifier.ImgurCallback.toString(), _imgurKey.getCallback()));

        if (_discordToken != null && !_discordToken.isEmpty())
            items.add(new DataStorage.Item(Identifier.DiscordToken.toString(), _discordToken));

        DataStorage.storeItems(_filepath, items, storageMessage());
    }

    private enum Identifier
    {
        ImgurClientId("IMGUR_CLIENTID"),
        ImgurClientSecret("IMGUR_CLIENTSECRET"),
        ImgurCallback("IMGUR_CALLBACK"),
        DiscordToken("DISCORDTOKEN");

        private final String identifier;

        Identifier(final String text)
        {
            identifier = text;
        }

        @Override
        public String toString()
        {
            return identifier;
        }
    }


}
