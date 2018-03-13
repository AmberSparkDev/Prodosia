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

package com.Bluefix.Prodosia.Authorization;

import com.Bluefix.Prodosia.Logger.Logger;
import com.Bluefix.Prodosia.Storage.CookieStorage;
import com.github.kskelm.baringo.BaringoClient;
import com.github.kskelm.baringo.util.BaringoAuthException;
import com.microsoft.alm.oauth2.useragent.AuthorizationException;
import com.microsoft.alm.oauth2.useragent.AuthorizationResponse;
import com.microsoft.alm.oauth2.useragent.UserAgent;
import com.microsoft.alm.oauth2.useragent.UserAgentImpl;

import java.io.IOException;
import java.net.CookieStore;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Authorization class for the imgur api.
 */
public class ImgurAuthorization
{
    /**
     * authorize the BaringoClient with OAuth2.0
     * Will ask the current user to input their name and password.
     * @param client The client to authorize.
     * @return true iff successfully authorized, false otherwise.
     */
    public static Result authorize(BaringoClient client, URI callback)
    {
        // first, check to see if we still have an active and functioning refresh token.
        try
        {
            String cookie = CookieStorage.getRefreshToken();

            if (cookie != null)
            {
                client.authService().setRefreshToken(cookie);

                if (client.authService().isUserAuthenticated())
                {
                    // update the refresh token and indicate success.
                    CookieStorage.setRefreshToken(client.authService().getRefreshToken());
                    return Result.SUCCESS;
                }
            }

        }
        catch (IOException e)
        {
            // failure to find a refresh token is insignificant at this point.
        }
        catch (BaringoAuthException e)
        {
            // failure to find a refresh token is insignificant at this point.
        }


        // if we didn't have a proper cookie, we will need to ask for authorization.
        String code;

        try
        {
            // note: the `Microsoft/oauth2-useragent` library relies on the deprecated "code"
            // authorization.
            URI authorizationEndpoint = new URI("https://api.imgur.com/oauth2/authorize?" +
                    "client_id=" + client.authService().getClientId() + "&" +
                    "response_type=code");

            //URI redirectUri = new URI("https://imgur.com");

            UserAgent userAgent = new UserAgentImpl();

            AuthorizationResponse authorizationResponse =
                    userAgent.requestAuthorizationCode(authorizationEndpoint,
                            callback);

            code = authorizationResponse.getCode();

        } catch (URISyntaxException e)
        {
            e.printStackTrace();
            return Result.ERROR;
        } catch (AuthorizationException e)
        {
            // the user did not authorize.
            e.printStackTrace();
            return Result.USER_DECLINED;
        }


        try
        {
            client.authService().setAuthorizationCode(code);

            // store the refresh token
            CookieStorage.setRefreshToken(client.authService().getRefreshToken());
            return Result.SUCCESS;
        } catch (BaringoAuthException e)
        {
            return Result.ERROR;
        }
        catch (IOException e)
        {
            // a failed cookie, however annoying, doesn't inhibit the funtioning of the application.
            Logger.logMessage("Cookie could not be stored", Logger.Severity.ERROR);
            e.printStackTrace();
            return Result.SUCCESS;
        }
    }


    /**
     * Authorization result.
     */
    public enum Result
    {
        SUCCESS,
        USER_DECLINED,
        ERROR
    }




}
