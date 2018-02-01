/** This file is released under the Apache License 2.0. See the LICENSE file for details. **/
package com.github.kskelm.baringo;

import java.io.IOException;

import com.squareup.okhttp.Request;
import retrofit.Call;
import retrofit.Response;

import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.model.OAuth2;
import com.github.kskelm.baringo.util.BaringoApiException;
import com.github.kskelm.baringo.util.BaringoAuthException;


/**
 * Manages the API's authentication for a specific user.
 * @author Kevin Kelm (triggur@gmail.com)
 */
public class AuthService {

	/**
	 * This makes Baringo trade an Imgur OAuth2 authorization code
	 * for an active access token and refresh token.  If successful,
	 * the refresh token is returned <i>and you must save it somewhere
	 * if you don't want the user to have to re-auth your app each time.</i>
	 * <p>
	 * Authorization codes are only good for a few minutes after
	 * retrieving them, so this should be called soon thereafter.
	 * Refer to the documentation for a handy web server toy that will
	 * walk you through the process if OAuth2 isn't your thing yet.
	 * @param authCode an authorization code returned by Imgur
	 * @return a refresh token to be saved and passed to Baringo
	 *     in the future.
	 * @throws BaringoAuthException unable to refresh tokens
	 */
	public String setAuthorizationCode( String authCode ) throws BaringoAuthException {
		this.oAuth2 = null;
		this.authenticatedAccount = null;

		tradeAuthCodeForTokens( authCode );
		return oAuth2.getRefreshToken();
	} // setAuthorizationCode


	/**
	 * Returns true if the current user's OAuth2 access token
	 * is valid.  If there is no authenticated user, 
	 * @return whether the auth token is valid
	 * @throws BaringoAuthException whomp whomp
	 */
	public boolean isAccessTokenValid()  throws BaringoAuthException {
		if( oAuth2 == null ) {
			return false;
		} // if
		Call<Object> call =
				client.getApi().validateToken();
		
		try {
			Response<Object> res = call.execute();
			return res.code() == 200;
		} catch (IOException e) {
			return false;
		} // try-catch
	} // isAccessTokenValid
	
	/**
	 * Returns the account that is currently authenticated, or null if none.
	 * @return the current Account
	 * @throws BaringoApiException the account couldn't be loaded
	 */
	public Account getAuthenticatedAccount() throws BaringoApiException {
		if( oAuth2 == null ) {
			return null;
		} // if
		if( authenticatedAccount != null ) {
			return authenticatedAccount;
		} // if
		
		authenticatedAccount = client.accountService().getAccount( oAuth2.getUserName() );
		return authenticatedAccount;
	}

	
	// =======================================================
	
	protected Request buildAuthenticatedRequest(Request request) {
		if( oAuth2 != null && oAuth2.getAccessToken() != null ) {
			request = request.newBuilder()
					.header( "Authorization", getAuthenticationHeader() )
					.build();
		} else {
			request = request.newBuilder()
					.header( "Authorization", getAuthenticationHeader() )
					.build();	
		} // if-else
		return request;
	}

	protected String getAuthenticationHeader() {
		if( oAuth2 != null && oAuth2.getAccessToken() != null ) {
			return "Bearer " + oAuth2.getAccessToken();
		} else {
			return "Client-ID " + clientId;
		} // if-else
	}

	private boolean tradeAuthCodeForTokens( String authCode ) throws BaringoAuthException {
		Call<OAuth2> call = client.getApi().tradeAuthCodeForTokens(
				clientId, clientSecret, "authorization_code", authCode );

		try {
			Response<OAuth2> res = call.execute();

			oAuth2 = res.body();
			if( oAuth2 == null ) {
				throw new BaringoAuthException( res.message(), res.code() );
			} // if

			return true;
		} catch (IOException e) {
			throw new BaringoAuthException( "Error fetching tokens from authorization code: " + e.getMessage() );
		} 

	} // tradeAuthCodeForTokens
	
	private boolean updateAccessToken() throws BaringoAuthException {
		if( oAuth2 != null && !oAuth2.isExpiringSoon() ) {
			return true; // nothing to do! all's well as far as we know.
		} // if
		
		if( refreshToken == null ) {
			throw new BaringoAuthException( "Cannot update OAuth2 access token; need refreshToken or authorizationCode to be set. See setRefreshToken() or setAuthorizationCode().  User-authenticated calls will not work until then.");
		} // if
		
		Call<OAuth2> call = client.getApi().refreshAccessToken(
				clientId, clientSecret, "refresh_token", refreshToken );

		try {
			Response<OAuth2> res = call.execute();

			oAuth2 = res.body();
			if( oAuth2 == null ) {
				throw new BaringoAuthException( res.message(), res.code() );
			} // if

			return true;
		} catch (IOException e) {
			throw new BaringoAuthException( "Error updating access tokens from refresh token: " + e.getMessage() );
		} 
	} // updateAccessToken

	/**
	 * Returns the BaringoClient (mostly internal use)
	 * @return the client
	 */
	public BaringoClient getClient() {
		return client;
	}

	/**
	 * Returns the clientId key used to set things rolling
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Returns the clientSecret key used to set things rolling
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * Returns the OAuth2 object.
	 * @return the oAuth2
	 */
	public OAuth2 getOAuth2() {
		return oAuth2;
	}

	/**
	 * Returns whether or not there is a user authenticated right now
	 * @return true if we're in authenticated user mode
	 */
	public boolean isUserAuthenticated() {
		return oAuth2 != null;
	}


	/**
	 * Returns the account name of the currently-logged-in user
	 * @return the name of the authenticated user or null if none
	 */
	public String getAuthenticatedUserName() {
		if( oAuth2 == null ) {
			return null;
		} // if
		return oAuth2.getUserName();
	}

	/**
	 * Returns the currently valid refresh token.  Call this
	 * after passing in your credentials while setting up user
	 * authorization, <i>and store it someone securely</i>.
	 * @return the current refresh token
	 */
	public String getRefreshToken() {
		if( this.oAuth2 == null ) {
			return null;
		} // if
		return this.oAuth2.getRefreshToken();
	}
	
	/**
	 * This sets the OAuth2 refresh token for user-level authentication.
	 * Baringo synchronously contacts Imgur to get an access token.
	 * Each client instance can have only one authenticated user at a time.
	 * @param refreshToken The user's refresh token.
	 * @throws BaringoAuthException couldn't fetch an access token
	 */
	public void setRefreshToken( String refreshToken ) throws BaringoAuthException {

		this.refreshToken = refreshToken;
		this.oAuth2 = null;
		this.authenticatedAccount = null;
		
		updateAccessToken();	
	} // setRefreshToken

	// ================================================
	private BaringoClient client = null;

	private String clientId = null;
	private String clientSecret = null;
	private OAuth2 oAuth2 = null;
	private Account authenticatedAccount = null;
	
	private String refreshToken = null;

	protected AuthService( BaringoClient client, String clientId, String clientSecret ) {
		this.client = client;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	} // constructor


} // class AuthService
