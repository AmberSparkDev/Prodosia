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


import com.Bluefix.Prodosia.Business.Authorization.IAuthorization;
import com.Bluefix.Prodosia.Business.Authorization.ImgurAuthorization;
import com.Bluefix.Prodosia.Business.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.Data.DataType.ImgurKey;
import com.Bluefix.Prodosia.Data.Enum.AuthorizationResult;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.Bluefix.Prodosia.Data.Storage.ICookieStorage;
import com.Bluefix.Prodosia.Data.Storage.IKeyStorage;
import com.github.kskelm.baringo.BaringoClient;
import com.microsoft.alm.oauth2.useragent.AuthorizationException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Manager for the imgur api.
 */
public class ImgurManager implements IImgurManager
{
    public static final String EnvVarImgurClientId = "ENV_IMGUR_CLIENT_ID";
    public static final String EnvVarImgurClientSecret = "ENV_IMGUR_CLIENT_SECRET";
    public static final String EnvVarImgurCallback = "ENV_IMGUR_CALLBACK";

    public static final String DefaultImgurCallback = "https://imgur.com";

    private IKeyStorage _keyStorage;
    private ICookieStorage _cookieStorage;
    private ILogger _logger;
    private ILogger _appLogger;


    private String _clientId;
    private String _clientSecret;
    private String _callback;

    private BaringoClient _client;


    /**
     * Initialize the Imgur Manager based on stored credentials.
     * <p>
     * At first this will check for the following environment variables:
     * - ENV_IMGUR_CLIENT_ID
     * - ENV_IMGUR_CLIENT_SECRET
     * - ENV_IMGUR_CALLBACK
     * <p>
     * Afterwards this will check for a stored Imgur Key.
     * If neither can be found, the Manager does not initialize the Baringo Client and setCredentials()
     * will have to be called manually
     */
    public ImgurManager(
            IKeyStorage keystorage,
            ICookieStorage cookieStorage,
            ILogger logger,
            ILogger appLogger) throws URISyntaxException, AuthorizationException
    {
        // store dependencies
        _keyStorage = keystorage;
        _cookieStorage = cookieStorage;
        _logger = logger;
        _appLogger = appLogger;

        // first check if there are environment variables for this.
        String envClientId = System.getenv(EnvVarImgurClientId);
        String envClientSecret = System.getenv(EnvVarImgurClientSecret);
        String envCallback = System.getenv(EnvVarImgurCallback);

        if (envClientId != null && envClientSecret != null)
        {
            _clientId = envClientId;
            _clientSecret = envClientSecret;

            _callback = envCallback == null
                    ? DefaultImgurCallback
                    : envCallback;
        }

        // if the credentials could not be found in the environment variables,
        // check the keystorage.
        if (_keyStorage != null && (_clientId == null || _clientSecret == null || _callback == null))
        {
            try
            {
                ImgurKey ik = _keyStorage.getImgurKey();

                _clientId = ik.getClientId();
                _clientSecret = ik.getClientSecret();
                _callback = ik.getCallback();

                if (_callback == null || _callback.trim().isEmpty())
                    _callback = DefaultImgurCallback;

            } catch (Exception e)
            {
                if (_logger != null)
                    _logger.warn("Exception while attempting to fetch the imgur key.\r\n" + e.getMessage());
            }
        }

        // initialize the manager if credentials could be found.
        if (_clientId != null && _clientSecret != null)
            initialize();
    }

    /**
     * Initialize the Imgur Manager with the supplied credentials.
     *
     * @param clientId     The client-id for the Imgur API
     * @param clientSecret The client-secret for the Imgur API
     * @param callback     The optional callback for the Imgur API
     */
    public ImgurManager(String clientId,
                        String clientSecret,
                        String callback,
                        IKeyStorage keystorage,
                        ICookieStorage cookieStorage,
                        ILogger logger,
                        ILogger appLogger) throws URISyntaxException, AuthorizationException
    {
        // store dependencies
        _keyStorage = keystorage;
        _cookieStorage = cookieStorage;
        _logger = logger;
        _appLogger = appLogger;

        // set credentials
        _clientId = clientId;
        _clientSecret = clientSecret;
        _callback = callback;

        // initialize the manager.
        initialize();
    }


    /**
     * Set the credentials for the manager. This will invalidate the cookie and prompt user authorization.
     *
     * @param clientId     The client-id for the Imgur API
     * @param clientSecret The client-secret for the Imgur API
     * @param callback     The optional callback for the Imgur API
     */
    @Override
    public void setCredentials(String clientId, String clientSecret, String callback) throws URISyntaxException, AuthorizationException
    {
        _clientId = clientId;
        _clientSecret = clientSecret;
        _callback = callback;

        _client = null;

        // attempt to remove the stored cookie.
        try
        {
            _cookieStorage.setRefreshToken(null);
        } catch (Exception e)
        {
            if (_logger != null)
                _logger.warn("[ImgurManager] Exception while resetting the cookie.\r\n" + e.getMessage());
        }

        // initialize the new client
        initialize();

        // if the client was initialized without issue, store the keys in the key storage.
        try
        {
            _keyStorage.setImgurKey(_clientId, _clientSecret, _callback);
        } catch (Exception e)
        {
            if (_logger != null)
                _logger.error("[ImgurManager] Exception while trying to store the Imgur Key.\r\n" + e.getMessage());

            if (_appLogger != null)
                _appLogger.info("There was a problem storing the Imgur keys.");
        }
    }


    private void initialize() throws URISyntaxException, AuthorizationException
    {
        createClient();
        initializeClient();
    }


    private void createClient()
    {
        if (_clientId == null || _clientSecret == null)
        {
            if (_logger != null)
                _logger.warn("Attempt to create Baringo client without proper credentials.\r\n" +
                        "client id: " + _clientId + "\r\n" +
                        "client secret: " + _clientSecret);

            throw new IllegalArgumentException("Cannot instantiate the client without credentials.");
        }

        try
        {
            _client = new BaringoClient.Builder()
                    .clientAuth(_clientId, _clientSecret)
                    .build();
        } catch (Exception e)
        {
            if (_logger != null)
                _logger.error("[ImgurManager] Exception thrown while attempting to create the Baringo Client\r\n" + e.getMessage());

            ExceptionHelper.showWarning(e);
        }
    }

    private void initializeClient() throws URISyntaxException, AuthorizationException
    {
        // create a new Authorization object
        IAuthorization authorization = new ImgurAuthorization(
                _client,
                new URI(_callback),
                _cookieStorage,
                _logger,
                _appLogger);

        // authorize the user.
        AuthorizationResult res = authorization.authorize();

        // throw an AuthorizationException if the user could not be authorized properly.
        if (res != AuthorizationResult.SUCCESS)
        {
            _client = null;

            if (_logger != null)
                _logger.warn("[ImgurManager] Failed to authorize user.");

            throw new AuthorizationException("User authorization failed.");
        }
    }


    /**
     * @return the Baringo Client maintained by the manager.
     */
    @Override
    public BaringoClient getClient()
    {
        return _client;
    }

    /**
     * @return the key storage used by the manager.
     */
    @Override
    public IKeyStorage getKeyStorage()
    {
        return _keyStorage;
    }
}