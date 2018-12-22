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

package com.Bluefix.Prodosia.Business.Authorization;

import com.Bluefix.Prodosia.Business.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.Business.Logger.Logger;
import com.Bluefix.Prodosia.Data.Storage.CookieStorage;
import com.github.kskelm.baringo.BaringoClient;
import com.github.kskelm.baringo.util.BaringoAuthException;
import com.microsoft.alm.oauth2.useragent.AuthorizationException;
import com.microsoft.alm.oauth2.useragent.AuthorizationResponse;
import com.microsoft.alm.oauth2.useragent.UserAgentImpl;

import java.io.IOException;
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
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        catch (BaringoAuthException ex)
        {
            ex.printStackTrace();
        }


        // if we didn't have a proper cookie, we will need to ask for authorization.
        String code;

        try
        {
            URI authorizationEndpoint =
                    new URI("https://api.imgur.com/oauth2/authorize?" +
                            "client_id=" + client.authService().getClientId() + "&" +
                            "response_type=code");
            //URI tokenEndpoint = new URI("https://api.imgur.com/oauth2/token");

            // force the library to use JavaFx as its user agent provider.
            System.getProperties().setProperty("userAgentProvider", "JavaFx");

            UserAgentImpl userAgent = new UserAgentImpl();
            AuthorizationResponse authorizationResponse =
                    userAgent.requestAuthorizationCode(authorizationEndpoint, callback);
            if (authorizationResponse.getCode() == null ||
                    authorizationResponse.getCode().trim().isEmpty())
                return Result.USER_DECLINED;

            code = authorizationResponse.getCode();
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
            ExceptionHelper.showWarning(e);
            return Result.ERROR;
        } catch (AuthorizationException e)
        {
            e.printStackTrace();

            return Result.USER_DECLINED;
        } catch (Exception e)
        {
            e.printStackTrace();
            return Result.ERROR;
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
