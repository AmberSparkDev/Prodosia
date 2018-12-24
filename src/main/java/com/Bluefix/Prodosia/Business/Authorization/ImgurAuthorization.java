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
import com.Bluefix.Prodosia.Business.Logger.ApplicationWindowLogger;
import com.Bluefix.Prodosia.Data.Enum.AuthorizationResult;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.Bluefix.Prodosia.Data.Storage.ICookieStorage;
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
public class ImgurAuthorization implements IAuthorization
{
    private BaringoClient _client;
    private URI _callback;

    private ICookieStorage _cookieStorage;
    private ILogger _logger;
    private ILogger _appLogger;


    public ImgurAuthorization(
            BaringoClient client,
            URI callback,
            ICookieStorage cookieStorage,
            ILogger logger,
            ILogger appLogger
    )
    {
        _client = client;
        _callback = callback;

        _cookieStorage = cookieStorage;
        _logger = logger;
        _appLogger = appLogger;
    }


    /**
     * authorize the BaringoClient with OAuth2.0
     * Will ask the current user to input their name and password.
     *
     * @return true iff successfully authorized, false otherwise.
     */
    @Override
    public AuthorizationResult authorize()
    {
        if (_logger != null)
            _logger.debug("[ImgurAuthorization] Beginning authorization");

        // first, check to see if we still have an active and functioning refresh token.
        if (_cookieStorage != null)
        {
            try
            {
                String cookie = null;

                try
                {
                    cookie = _cookieStorage.getRefreshToken();
                } catch (Exception e)
                {
                    if (_logger != null)
                        _logger.warn("Exception while trying to fetch the refresh token\r\n" +
                                e.getMessage());
                }


                if (cookie != null)
                {
                    _client.authService().setRefreshToken(cookie);

                    if (_client.authService().isUserAuthenticated())
                    {
                        // update the refresh token and indicate success.

                        try
                        {
                            _cookieStorage.setRefreshToken(_client.authService().getRefreshToken());
                        } catch (Exception e)
                        {
                            if (_logger != null)
                                _logger.warn("[ImgurAuthorization] Exception while trying to set the refresh token\r\n" +
                                        e.getMessage());
                        }

                        if (_logger != null)
                            _logger.info("[ImgurAuthorization] Successful authorization with cookie.");
                        return AuthorizationResult.SUCCESS;
                    }
                }

            } catch (BaringoAuthException ex)
            {
                _logger.warn("[ImgurAuthorization] Baringo Exception while attempting to use a cookie refresh token.\r\n" +
                        ex.getMessage());
            }
        }


        // if we didn't have a proper cookie, we will need to ask for authorization.
        if (_logger != null)
            _logger.debug("[ImgurAuthorization] no valid cookie, starting authorization process.");

        String code;

        try
        {
            URI authorizationEndpoint =
                    new URI("https://api.imgur.com/oauth2/authorize?" +
                            "client_id=" + _client.authService().getClientId() + "&" +
                            "response_type=code");
            //URI tokenEndpoint = new URI("https://api.imgur.com/oauth2/token");

            // force the library to use JavaFx as its user agent provider.
            System.getProperties().setProperty("userAgentProvider", "JavaFx");

            UserAgentImpl userAgent = new UserAgentImpl();
            AuthorizationResponse authorizationResponse =
                    userAgent.requestAuthorizationCode(authorizationEndpoint, _callback);
            if (authorizationResponse.getCode() == null ||
                    authorizationResponse.getCode().trim().isEmpty())
            {
                if (_logger != null)
                    _logger.info("[ImgurAuthorization] User declined authorization.");

                return AuthorizationResult.USER_DECLINED;
            }

            code = authorizationResponse.getCode();
        } catch (URISyntaxException e)
        {
            if (_logger != null)
                _logger.error("[ImgurAuthorization] URISyntaxException thrown during authorization attempt.\r\n" + e.getMessage());

            ExceptionHelper.showWarning(e);
            return AuthorizationResult.ERROR;

        } catch (AuthorizationException e)
        {
            if (_logger != null)
                _logger.warn("[ImgurAuthorization] AuthorizationException thrown during authorization attempt.\r\n" + e.getMessage());

            return AuthorizationResult.USER_DECLINED;
        } catch (Exception e)
        {
            if (_logger != null)
                _logger.error("[ImgurAuthorization] Exception thrown during authorization attempt.\r\n" + e.getMessage());

            return AuthorizationResult.ERROR;
        }

        if (_logger != null)
            _logger.debug("[ImgurAuthorization] Authorization code received.");

        try
        {
            _client.authService().setAuthorizationCode(code);

            // store the refresh token
            if (_cookieStorage != null)
                _cookieStorage.setRefreshToken(_client.authService().getRefreshToken());

            if (_logger != null)
                _logger.debug("[ImgurAuthorization] Successful authorization.");

            return AuthorizationResult.SUCCESS;

        } catch (BaringoAuthException e)
        {
            if (_logger != null)
                _logger.error("[ImgurAuthorization] BaringoAuthException while trying to set the authorization code\r\n" +
                        e.getMessage());

            return AuthorizationResult.ERROR;
        } catch (IOException e)
        {
            if (_logger != null)
                _logger.warn("[ImgurAuthorization] IOException while trying to set the refresh token cookie.\r\n" +
                        e.getMessage());

            // a failed cookie, however annoying, doesn't inhibit the funtioning of the application.
            if (_appLogger != null)
                _appLogger.info("Cookie could not be stored.");

            return AuthorizationResult.SUCCESS;
        } catch (Exception e)
        {
            if (_logger != null)
                _logger.warn("[ImgurAuthorization] Exception while trying to set the refresh token\r\n" +
                        e.getMessage());

            return AuthorizationResult.SUCCESS;
        }
    }


}
