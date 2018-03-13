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

package com.Bluefix.Prodosia.Storage;

import com.Bluefix.Prodosia.DataType.ImgurKey;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Manager for storing keys.
 *
 * The keys are stored in plaintext.
 */
public class KeyStorage
{
    private static final String filename = "keys.txt";

    /**
     * The imgur key.
     */
    private ImgurKey imgurKey;

    /**
     * The discord bot token.
     */
    private String discordToken;

    //region Singleton

    private static KeyStorage me;

    private static KeyStorage keyStorage() throws IOException
    {
        if (me == null)
        {
            me = readStorage();
        }

        return me;
    }

    //endregion

    //region Constructor

    /**
     * Empty constructor, meaning that no keys were provided yet.
     */
    private KeyStorage()
    {
        this.imgurKey = null;
        this.discordToken = null;
    }

    private KeyStorage(ImgurKey imgurKey, String discordToken)
    {
        this.imgurKey = imgurKey;
        this.discordToken = discordToken;
    }

    //endregion


    private static String getFilepath()
    {
        return System.getProperty("user.dir") + "/" + filename;
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


    //region Token handling

    /**
     * Set the imgur credentials.
     * @param clientId The client id.
     * @param clientSecret The client secret.
     * @param callback The callback url.
     * @throws IOException
     */
    public static void setImgurKey(String clientId, String clientSecret, String callback) throws IOException
    {
        keyStorage().imgurKey = new ImgurKey(clientId, clientSecret, callback);
        writeStorage();
    }

    /**
     * Retrieve the imgur key.
     * @return the imgur key known by the system or null if it didn't exist.
     * @throws IOException
     */
    public static ImgurKey getImgurKey() throws IOException
    {
        return keyStorage().imgurKey;
    }


    /**
     * Set the discord token.
     * @param token The token to be used.
     * @throws IOException
     */
    public static void setDiscordToken(String token) throws IOException
    {
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("The discord token should not be null or empty");

        keyStorage().discordToken = token;
        writeStorage();
    }

    /**
     * Retrieve the discord token.
     * @return The discord token known by the system or null if it didn't exist.
     * @throws IOException
     */
    public static String getDiscordToken() throws IOException
    {
        return keyStorage().discordToken;
    }




    //endregion







    //region IO

    private static KeyStorage readStorage() throws IOException
    {
        String imgurClientId = null;
        String imgurClientSecret = null;
        String imgurCallback = null;
        String discordToken = null;

        ArrayList<DataStorage.Item> items = DataStorage.readItems(getFilepath());

        // if the file did not exist, return an empty KeyStorage.
        if (items == null)
            return new KeyStorage();

        for (DataStorage.Item i : items)
        {
            if (Identifier.ImgurClientId.toString().equals(i.getName()))
            {
                imgurClientId = i.getData();
            }
            else if (Identifier.ImgurClientSecret.toString().equals(i.getName()))
            {
                imgurClientSecret = i.getData();
            }
            else if (Identifier.ImgurCallback.toString().equals(i.getName()))
            {
                imgurCallback = i.getData();
            }
            else if (Identifier.DiscordToken.toString().equals(i.getName()))
            {
                discordToken = i.getData();
            }
        }

        ImgurKey iKey = null;

        if (imgurClientId != null && imgurClientSecret != null && imgurCallback != null)
            iKey = new ImgurKey(imgurClientId, imgurClientSecret, imgurCallback);

        return new KeyStorage(iKey, discordToken);
    }

    private static void writeStorage() throws IOException
    {
        // retrieve the keystorage.
        KeyStorage ks = keyStorage();

        // add the items to be stored to the list.
        ArrayList<DataStorage.Item> items = new ArrayList<>();
        items.add(new DataStorage.Item(Identifier.ImgurClientId.toString(), ks.imgurKey.getClientId()));
        items.add(new DataStorage.Item(Identifier.ImgurClientSecret.toString(), ks.imgurKey.getClientSecret()));
        items.add(new DataStorage.Item(Identifier.ImgurCallback.toString(), ks.imgurKey.getCallback()));
        items.add(new DataStorage.Item(Identifier.DiscordToken.toString(), ks.discordToken));

        DataStorage.storeItems(getFilepath(), items, storageMessage());
    }

    private static String storageMessage()
    {
        return
                "This document will contain any API keys known to the system.\n" +
                "Unless you know what you are doing, please don't tamper with this file.\n\n";
    }

    //endregion









}
