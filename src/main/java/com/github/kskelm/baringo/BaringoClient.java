/** This file is released under the Apache License 2.0. See the LICENSE file for details. **/
package com.github.kskelm.baringo;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.model.ImgurResponseWrapper;
import com.github.kskelm.baringo.util.BaringoApiException;
import com.github.kskelm.baringo.util.RetrofittedImgur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.Response;


/**
 * The Baringo client that is the foundation for all API calls to Imgur
 * @author Kevin Kelm (triggur@gmail.com)
 * 
 */
public class BaringoClient {

	public static final String PROPERTY_CLIENT_ID     = "baringoclient.clientid";
	public static final String PROPERTY_CLIENT_SECRET = "baringoclient.clientsecret";

	/**
	 * Returns the AccountService object used to execute account-related operations
	 * @return the account service
	 */
	public AccountService accountService() {
		return acctSvc;
	}

	/**
	 * Returns the AlbumService object used to execute album-related operations
	 * @return the account service
	 */
	public AlbumService albumService() {
		return albSvc;
	}

	/**
	 * Returns the ImageService object used to execute image-related operations
	 * @return the image service
	 */
	public ImageService imageService() {
		return imgSvc;
	} 

	/**
	 * Returns the GalleryService object used to execute gallery-related operations
	 * @return the gallery service
	 */
	public GalleryService galleryService() {
		return galSvc;
	}

	/**
	 * Returns the CustomGalleryService object used to execute custom gallery-related operations
	 * @return the gallery service
	 */
	public CustomGalleryService customGalleryService() {
		return cusGalSvc;
	} 

	/**
	 * Returns the GalleryService object used to execute comment-related operations
	 * @return the gallery service
	 */
	public CommentService commentService() {
		return comSvc;
	}

	/**
	 * Returns the ConversationService object used to execute message-related operations
	 * @return the conversation service
	 */
	public ConversationService conversationService() {
		return cnvSvc;
	} 

	/**
	 * Returns the TopicService object used to execute topic-related operations
	 * @return the gallery service
	 */
	public NotificationService notificationService() {
		return noteSvc;
	} 

	/**
	 * Returns the MemeService object used to execute meme-related operations
	 * @return the gallery service
	 */
	public MemeService memeService() {
		return memeSvc;
	} 


	/**
	 * Returns the TopicService object used to execute topic-related operations
	 * @return the gallery service
	 */
	public TopicService topicService() {
		return topSvc;
	} 




	/**
	 * Returns the AuthService object used to execute authentication-related operations
	 * @return the gallery service
	 */
	public AuthService authService() {
		return authSvc;
	} 

	/**
	 * Returns an object that describes the remaining quotas left over for this client
	 * @return quota information
	 */
	public Quota getQuota() {
		return quota;
	} 

	/**
	 * As a convenience measure, return the username of the logged-in user
	 * @return user name or null if none
	 */
	public String getAuthenticatedUserName() {
		return authSvc.getAuthenticatedUserName();
	}

	/**
	 * Returns the Account object for the account that's currently
	 * authenticated via OAuth2, or null if none.  This is a
	 * convenience method that caches, since it seems like something
	 * that might be requested frequently.
	 * @return the current Account
	 * @throws BaringoApiException wat
	 */
	public Account getAuthenticatedAccount() throws BaringoApiException {
		return authSvc.getAuthenticatedAccount();
	}

	// =========================================================
	// static

	/**
	 * This is used to construct a new BaringoClient
	 * @author Kevin Kelm (triggur@gmail.com)
	 *
	 */
	public static class Builder {

		/**
		 * Sets the client id and secret, which are the minimum kind
		 * of Imgur authentication.  They give you access to only the
		 * publicly-accessible features of the site, not private details
		 * in a specific user's account. {Link http://api.imgur.com/}
		 * @param clientId the client_id assigned by Imgur
		 * @param clientSecret the client_secret assigned by Imgur
		 * @return This builder object
		 */
		public Builder clientAuth( String clientId, String clientSecret ) {
			this._clientId = clientId;
			this._clientSecret = clientSecret;

			return this;
		} // clientAuth


		/**
		 * Constructs the BaringoClient and returns it
		 * @return The Baringo client
		 * @throws BaringoApiException Unable to build the client
		 */
		public BaringoClient build() throws BaringoApiException {
			BaringoClient client = new BaringoClient( _clientId, _clientSecret );

			return client;
		} // build

		private String _clientId = null;
		private String _clientSecret = null;
	}


	/**
	 * Used for switching to the mashape commercial endpoint,
	 * or for mocking.  NOTE that there can only be one
	 * API endpoint in the system at a time; even if you
	 * create a new client, they all still share the same
	 * URL endpoints.
	 * @param url new endpoint
	 */
	public static void setApiEndpoint( String url ) {
		BaringoClient.apiEndpoint = url;
	}

	/**
	 * Fetch the current api endpoint base url
	 * @return the api endpoint base url
	 */
	public static String getApiEndpoint() {
		return BaringoClient.apiEndpoint;
	}
	
	// =========================================================
	// internal
	
	/**
	 * Construct a client.  This is necessary before using
	 * any of the API calls.  It is advised to store clientId
	 * and clientSecret somewhere other than in your code.
	 * Note that logging in a user is a separate step that comes
	 * later.
	 * @param clientId the clientID string for your client. If you haven't got one yet, <a href="https://api.imgur.com/oauth2/addclient">register</a>. You'll need to register as OAuth 2 without a callback URL.
	 * @param clientSecret the clientID string for your client. If you haven't got one yet, <a href="https://api.imgur.com/oauth2/addclient">register</a>. You'll need to register as OAuth 2 without a callback URL.  THIS IS A SECRET- DO NOT SHARE IT. STORE THIS IN A SECURE PLACE.
	 * @throws BaringoApiException the clientId or clientSecret were not supplied
	 */
	protected BaringoClient( String clientId, String clientSecret ) throws BaringoApiException {
		if( clientId == null || clientSecret == null ) {
			throw new BaringoApiException( "Must have clientId and clientSecret to run Baringo.  See http://api.imgur.com/");
		} // if

		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.api = create();
	} // constructor


	protected RetrofittedImgur getApi() {
		return api;
	} // getApi

	protected <T> void throwOnWrapperError( Response<ImgurResponseWrapper<T>> resp ) throws BaringoApiException {
		if( resp.code() != 200 ) {
			throw new BaringoApiException( resp.raw().request().urlString()
					+ ": " +  resp.message(), resp.code() );
		} // if
		if( resp.body() == null ) {
			throw new BaringoApiException( "No response body found" );
		} // if
		if( resp.body().getStatus() != 200 || !resp.body().isSuccess() ) {
			throw new BaringoApiException( "Unknown error", resp.body().getStatus() );
		} // if
	} // throwOnWrapperError

	private RetrofittedImgur create() {
		client = new OkHttpClient();
		client.interceptors().add(new ImgurInterceptor());

		HttpLoggingInterceptor logging = new HttpLoggingInterceptor(  );  
		logging.setLevel(Level.BODY);
		client.interceptors().add( logging );
			
		
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Date.class, new DateAdapter());

		// create the various domain-specific
		// services, giving them a chance to register any Gson
		// type adapters they're going to need.
		this.acctSvc = new AccountService( this, gsonBuilder );
		this.albSvc = new AlbumService( this, gsonBuilder );
		this.imgSvc = new ImageService( this, gsonBuilder );
		this.galSvc = new GalleryService( this, gsonBuilder );
		this.comSvc = new CommentService( this, gsonBuilder );
		this.cusGalSvc = new CustomGalleryService( this, gsonBuilder );
		this.topSvc = new TopicService( this, gsonBuilder );
		this.cnvSvc = new ConversationService( this, gsonBuilder );
		this.noteSvc = new NotificationService( this, gsonBuilder );
		this.memeSvc = new MemeService( this, gsonBuilder );

		this.authSvc = new AuthService( this, clientId, clientSecret );

		// build the gson object
		final Gson gson = gsonBuilder.create();

		// start up the API client
		GsonConverterFactory gcf = GsonConverterFactory.create( gson );
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl( apiEndpoint )
				.addConverterFactory( gcf )
				.client(client)
				.build();

		return retrofit.create(RetrofittedImgur.class);
	}

	/**
	 * This handles our authentication and logging, mostly.
	 */
	private class ImgurInterceptor implements Interceptor {

		public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
			Request  request  = chain.request();

			log.fine( "API Call: " + request.url().toString() );
			request = authService().buildAuthenticatedRequest( request );

			com.squareup.okhttp.Response response = chain.proceed(request);

			updateQuota( response );
			return response;
		}		
	}
	
	/**
	 * These define the headers that return relevant quota information
	 */
	private static final String HEADER_USER_CREDIT_RESET_DATE = "X-RateLimit-UserReset";
	private static final String HEADER_USER_CREDITS_ALLOCATED = "X-RateLimit-UserLimit";
	private static final String HEADER_USER_CREDITS_AVAILABLE = "X-RateLimit-UserRemaining";
	private static final String HEADER_APPLICATION_CREDITS_AVAILABLE = "X-RateLimit-ClientRemaining";
	private static final String HEADER_APPLICATION_CREDITS_ALLOCATED = "X-RateLimit-ClientLimit";
	private static final String HEADER_POST_CREDIT_RESET_DATE = "X-Post-Rate-Limit-Reset";
	private static final String HEADER_POST_CREDITS_ALLOCATED = "X-Post-Rate-Limit-Limit";
	private static final String HEADER_POST_CREDITS_AVAILABLE = "X-Post-Rate-Limit-Remaining";

	private void updateQuota( com.squareup.okhttp.Response response ) {
		String val = response.header( HEADER_USER_CREDIT_RESET_DATE );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setUserCreditResetDate( new Date( valInt ) );
		} // if
		val = response.header( HEADER_USER_CREDITS_ALLOCATED );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setUserCreditsAllocated(valInt);
		} // if
		val = response.header( HEADER_USER_CREDITS_AVAILABLE );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setUserCreditsAvailable(valInt);
		} // if
		val = response.header( HEADER_APPLICATION_CREDITS_AVAILABLE );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setApplicationCreditsAvailable(valInt);
		} // if
		val = response.header( HEADER_APPLICATION_CREDITS_ALLOCATED );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setApplicationCreditsAllocated(valInt);
		} // if
		val = response.header( HEADER_POST_CREDIT_RESET_DATE );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setPostCreditResetDate(new Date(valInt));
		} // if
		val = response.header( HEADER_POST_CREDITS_ALLOCATED );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setPostCreditsAllocated(valInt);
		} // if
		val = response.header( HEADER_POST_CREDITS_AVAILABLE );
		if( val != null ) {
			int valInt = Integer.parseInt( val );
			quota.setPostCreditsAvailable(valInt);
		} // if
	} 

	/**
	 * Apparently standard Gson can't tolerate a unix timestamp
	 * representing a date object.  That's pretty much all we care
	 * about, so we're subclassing it.
	 */
	class DateAdapter extends TypeAdapter<Date> {

		@Override
		public void write(JsonWriter out, Date value) throws IOException {
			if( value == null ){
				out.nullValue();
				return;
			} // if
			out.value( value.getTime() / 1000);
		}

		@Override
		public Date read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			} else if ( in.peek() == JsonToken.BOOLEAN ) {
				in.nextBoolean();  // throw it away
				return null;
			} // if-else

			return new Date( in.nextLong() * 1000 );
		}
	}

	// =============================================
	private RetrofittedImgur api = null;
	private String clientId = null;
	private String clientSecret = null;
	private static final Logger log = Logger.getLogger( BaringoClient.LOG_NAME );
	private Quota quota = new Quota();

	private AccountService acctSvc = null;
	private AlbumService   albSvc = null;
	private AuthService authSvc = null;
	private CommentService comSvc = null;
	private ConversationService cnvSvc = null;
	private CustomGalleryService cusGalSvc = null;
	private ImageService   imgSvc = null;
	private GalleryService galSvc = null;
	private MemeService memeSvc = null;
	private NotificationService noteSvc = null;
	private TopicService topSvc = null;
	
	private OkHttpClient client;
	
	public static final String DEFAULT_API_BASE_URL = "https://api.imgur.com/";
	public static final String DEFAULT_DOWNLOAD_BASE_URL = "https://i.imgur.com/"; // http would be faster but ... ?

	public static final String LOG_NAME = "ImgurApi";


	private static String apiEndpoint = DEFAULT_API_BASE_URL;
	

}
