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

package com.Bluefix.Prodosia.Imgur.ImgurApi;


import com.Bluefix.Prodosia.Authorization.ImgurAuthorization;
import com.Bluefix.Prodosia.DataType.ImgurKey;
import com.Bluefix.Prodosia.Module.ModuleManager;
import com.Bluefix.Prodosia.Storage.KeyStorage;
import com.github.kskelm.baringo.BaringoClient;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Manager for the imgur api.
 */
public class ImgurManager
{
    //region Singleton

    private static BaringoClient client;

    /**
     * Retrieve the Imgur Api client that allows access to the underlying API.
     * @return The Imgur API client.
     * @throws IOException Exception reading/writing cookies. Non-essential, but good to know anyways.
     * @throws BaringoApiException Baringo framework exception.
     * @throws URISyntaxException Might pertain to an incompatibility with the imgur API.
     */
    public static BaringoClient client() throws IOException, BaringoApiException, URISyntaxException
    {
        if (client == null)
        {
            ImgurKey key = KeyStorage.getImgurKey();

            client = createClient(key);

            // immediately authorize the client.
            // note: for anonymous usage it is not necessary to authorize. However,
            // heavy use of authorized api access it expected and immediate authorization
            // determines when the user is prompted, so it can be done on startup.
            if (client != null)
                ImgurAuthorization.authorize(client, new URI(KeyStorage.getImgurKey().getCallback()));

            // start the imgur dependencies
            ModuleManager.startImgurDependencies();
        }

        return client;
    }

    private static BaringoClient createClient(ImgurKey key) throws IOException, BaringoApiException, URISyntaxException
    {
        // get the api key credentials.
        if (key == null)
            return null;

        BaringoClient client = new BaringoClient.Builder()
                .clientAuth(key.getClientId(), key.getClientSecret())
                .build();

        return client;
    }

    //endregion

    //region Sanitation check

    /**
     * Check if the supplied Imgur key is valid. This does *not* update the imgur key of the system.
     * @param key
     * @return
     */
    public static boolean checkValidity(ImgurKey key) throws Exception
    {
        // checks on whether the strings are empty are already done pre-emptively before this.
        // current functionality is commented out because it serves no real purpose.
        return true;
        /*

        BaringoClient client = createClient(key);

        // TODO: it is possible to expand this a little bit by forcing the user to
        // authorize on these credentials to check its validity.

        if (client != null)
            return true;

        throw new Exception("Could not initialize the Imgur client");
        */
    }


    /**
     * Prompts the Client to re-initialize
     */
    public static void update() throws IOException, URISyntaxException, BaringoApiException
    {
        boolean clientWasInitialized = client != null;

        ImgurKey key = KeyStorage.getImgurKey();

        client = createClient(key);

        // immediately authorize the client.
        // note: for anonymous usage it is not necessary to authorize. However,
        // heavy use of authorized api access it expected and immediate authorization
        // determines when the user is prompted, so it can be done on startup.
        if (client != null)
            ImgurAuthorization.authorize(client, new URI(KeyStorage.getImgurKey().getCallback()));

        // if the client itself wasn't initialized yet, start the imgur dependencies.
        if (!clientWasInitialized)
            ModuleManager.startImgurDependencies();
    }

    //endregion


}
