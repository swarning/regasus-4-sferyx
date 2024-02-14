package de.regasus.core.ui.rws;

import static de.regasus.core.ui.rws.AuthConstants.*;

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import de.regasus.auth.AccessTokenResponse;
import de.regasus.util.XmlHelper;


/**
 * This class manages an access token.
 * When {@link #getTokenValue()} is called for the first time, it requests an access token from {@link IAuthManager}.
 * The response contains also the expiration time of the access token.
 * Later calls of {@link #getTokenValue()} return the same access token unless it is expired. Then a new access token
 * is requested.
 * This class is similar to com.lambdalogic.regasus.ws.v1.service.AccessTokenProvider but more simple. It ignores
 * the refresh token that is provided with the access token. Instead a new access token is requested every time the
 * old one expires.
 */
public class AccessTokenProvider {

	protected static HttpClientContext context = HttpClientContext.create();


	private String basePath;
	private String accessTokenURL;


	// data that is required for getting token
	private String clientID;

	// token information
	private String accessToken;
	private Date accessTokenExpiration;
	/**
	 * Complete token value containing the key word "Bearer " followed by the access token.
	 */
	private String _tokenValue;


	public static AccessTokenProvider getUserAccountAndPasswordInstance(
		String authURL,
		String clientID,
		String userName,
		String password
	) {
		AccessTokenProvider accessTokenProvider = new AccessTokenProvider(clientID);

		// build URL to get new access token
   		StringBuilder sb = new StringBuilder();
   		sb.append(authURL);
   		sb.append(accessTokenProvider.getBasePath());
		sb.append("&").append(QUERY_PARAM_REGASUS_USER).append("=").append(userName);
		sb.append("&").append(QUERY_PARAM_PASSWORD).append("=").append(password);
		accessTokenProvider.accessTokenURL = sb.toString();

		return accessTokenProvider;
	}


	private AccessTokenProvider(String clientID) {
		this.clientID = clientID;
	}


	private String getBasePath() {
		if (basePath == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("/").append(PATH_TOKEN);
			sb.append("?").append(QUERY_PARAM_GRANT_TYPE).append("=").append(GRANT_TYPE_PASSWORD);
			sb.append("&").append(QUERY_PARAM_CLIENT_ID).append("=").append(clientID);
			this.basePath = sb.toString();
		}
		return basePath;
	}


	protected boolean isAccessTokenValid() {
		return accessTokenExpiration != null && accessTokenExpiration.after(new Date());
	}


	public String getTokenValue() throws ClientProtocolException, IOException, Exception {
		if ( ! isAccessTokenValid()) {
			// get new access token and refresh token
			initTokens();

			_tokenValue = "Bearer " + accessToken;
		}
		return _tokenValue;
	}


	protected void initTokens() throws ClientProtocolException, IOException, Exception {
    	// get AccessToken and RefreshToken
   		CloseableHttpResponse httpResponse = null;
   		try (
   			CloseableHttpClient httpClient = HttpClients.createDefault()
   		) {
			// build HTTP GET request
	    	HttpGet httpGet = new HttpGet(accessTokenURL);

	    	// execute HTTP request
	    	httpResponse = httpClient.execute(httpGet, context);

	    	// check status code
	    	int statusCode = httpResponse.getStatusLine().getStatusCode();
	    	if (statusCode != HttpStatus.OK_200) {
	    		throw new Exception("Unexpected status code " + statusCode + " on GET request to URL " + accessTokenURL);
	    	}

			HttpEntity entity = httpResponse.getEntity();
			AccessTokenResponse accessTokenResponse = XmlHelper.createFromXML(entity.getContent(), AccessTokenResponse.class);

			accessToken = accessTokenResponse.getAccessToken();
			// set accessTokenExpiration 1 minute (60000 ms) before actual expiration
			accessTokenExpiration = new Date(System.currentTimeMillis() + accessTokenResponse.getExpiresIn() - 60000);
   		}
		finally {
			// close HTTP response
			HttpClientUtils.closeQuietly(httpResponse);
		}
	}

}
