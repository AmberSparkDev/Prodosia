/*
 * Copyright (c) 2018 Bas Boellaard
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

package com.Bluefix.Prodosia.ImgurApi;


import com.Bluefix.Prodosia.Authorization.ImgurAuthorization;
import com.Bluefix.Prodosia.DataType.ImgurKey;
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
     * @throws IOException
     * @throws BaringoApiException
     */
    public static BaringoClient client() throws IOException, BaringoApiException, URISyntaxException
    {
        if (client == null)
        {
            // get the api key credentials.
            ImgurKey key = KeyStorage.getImgurKey();

            if (key == null)
                throw new NullPointerException("The Imgur key was not known in the system. Ensure they are set up through `KeyStorage`.");

            client = new BaringoClient.Builder()
                    .clientAuth(key.getClientId(), key.getClientSecret())
                    .build();

            // immediately authorize the client.
            // note: for anonymous usage it is not necessary to authorize. However,
            // heavy use of authorized api access it expected and immediate authorization
            // determines when the user is prompted, so it can be done on startup.
            ImgurAuthorization.authorize(client, new URI(key.getCallback()));
        }

        return client;
    }

    //endregion



}
