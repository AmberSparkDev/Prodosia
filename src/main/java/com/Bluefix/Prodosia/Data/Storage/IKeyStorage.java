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

public interface IKeyStorage
{
    /**
     * Set the imgur credentials.
     * @param clientId The client id.
     * @param clientSecret The client secret.
     * @param callback The callback url.
     * @throws IOException
     */
    void setImgurKey(String clientId, String clientSecret, String callback) throws Exception;

    /**
     * Retrieve the imgur key.
     * @return the imgur key known by the system or null if it didn't exist.
     * @throws IOException
     */
    ImgurKey getImgurKey() throws Exception;


    /**
     * Set the discord token.
     * @param token The token to be used.
     * @throws IOException
     */
    void setDiscordToken(String token) throws Exception;

    /**
     * Retrieve the discord token.
     * @return The discord token known by the system or null if it didn't exist.
     * @throws IOException
     */
    String getDiscordToken() throws Exception;
}
