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

package com.Bluefix.Prodosia.Business.Imgur.ImgurApi;

import com.Bluefix.Prodosia.Data.Storage.IKeyStorage;
import com.github.kskelm.baringo.BaringoClient;

public interface IImgurManager
{
    /**
     * @return the Baringo Client maintained by the manager.
     */
    BaringoClient getClient();

    /**
     * @return the key storage used by the manager.
     */
    IKeyStorage getKeyStorage();

    /**
     * Set the credentials for the manager. This will invalidate the cookie and prompt user authorization.
     * @param clientId     The client-id for the Imgur API
     * @param clientSecret The client-secret for the Imgur API
     * @param callback     The optional callback for the Imgur API
     */
    void setCredentials(String clientId, String clientSecret, String callback) throws Exception;
}
