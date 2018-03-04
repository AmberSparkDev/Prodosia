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

package com.Bluefix.Prodosia.DataType;

/**
 * Immutable data class for an imgur key.
 */
public class ImgurKey
{
    private String clientId;
    private String clientSecret;
    private String callback;

    /**
     * Create a new immutable Imgur Key Object
     * @param clientId The client id.
     * @param clientSecret The client secret.
     * @param callback The callback url.
     */
    public ImgurKey(String clientId, String clientSecret, String callback)
    {
        if (clientId == null || clientId.isEmpty())
            throw new IllegalArgumentException("The client id should not be null or empty.");

        if (clientSecret == null || clientSecret.isEmpty())
            throw new IllegalArgumentException("The client secret should not be null or empty.");

        if (callback == null || callback.isEmpty())
            throw new IllegalArgumentException("The callback should not be empty. " +
                    "Keep in mind that the \"no callback\" option defaults to using " +
                    "`https://imgur.com` as callback.");

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.callback = callback;
    }

    public String getClientId()
    {
        return clientId;
    }

    public String getClientSecret()
    {
        return clientSecret;
    }

    public String getCallback()
    {
        return callback;
    }
}
