package de.regasus.core.ui.rws;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import de.regasus.auth.api.BasicAuthHelper;
import de.regasus.core.ServerModel;


public class RegasusWebServiceUtil {

	public static final String UTF_8 = "UTF-8";
	public static final String AUTHORIZATION = "Authorization";
	public static final String CONTENT_LENGTH = "Content-Length";

	public static final ContentType CONTENT_TYPE_XML = ContentType.create("application/xml", UTF_8);
	public static final ContentType CONTENT_TYPE_BYTES = ContentType.create("application/octet-stream");


	private int timeoutMillis = 5000;


	public RegasusWebServiceUtil() {
	}


	public int getTimeoutMillis() {
		return timeoutMillis;
	}


	public void setTimeoutMillis(int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}


	public RegasusWebServiceUtil timeoutMillis(int timeoutMillis) {
		setTimeoutMillis(timeoutMillis);
		return this;
	}


	private HttpClientContext buildContext() {
		HttpClientContext context = HttpClientContext.create();
		return context;
	}


	public CloseableHttpClient buildClient() {
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(timeoutMillis)
			.setConnectionRequestTimeout(timeoutMillis)
			.setSocketTimeout(timeoutMillis)
			.setExpectContinueEnabled(false)
			.build();


		return HttpClients.custom()
			.useSystemProperties() // necessary to use proxy settings
			.setDefaultRequestConfig(requestConfig)
			.build();
	}


    // *****************************************************************************************************************
    // * Do not delete. This method might be used if the RCP client needs to authenticate itself with an access token.
    // *

//	private static AccessTokenProvider accessTokenProvider;
//
//
//	private static AccessTokenProvider getAccessTokenProvider() throws Exception {
//		if (accessTokenProvider == null) {
//			String authURL = ServerModel.getInstance().getAuthUrl();
//
//			String clientID = read client id from wildfly/modules/de/regasus/config/main/config.properties
//			if (isEmpty(clientID)) {
//				throw new ErrorMessageException("No property value for KEY '" + AuthPropertyKey.AUTH_CLIENT_ID + "'.");
//			}
//
//			String userName = ServerModel.getInstance().getUser();
//			String password = ServerModel.getInstance().getPassword();
//
//			accessTokenProvider = AccessTokenProvider.getUserAccountAndPasswordInstance(
//				authURL,
//				clientID,
//				userName,
//				password
//			);
//		}
//		return accessTokenProvider;
//	}
//
//
//	private static String getTokenValue() throws Exception {
//		return getAccessTokenProvider().getTokenValue();
//	}

	// *
	// * Do not delete. This method might be used if the RCP client needs to authenticate itself with an access token.
    // *****************************************************************************************************************

	private static String getBasicAuthHeader() {
		String userName = ServerModel.getInstance().getUser();
		String password = ServerModel.getInstance().getPassword();
		String basicAuthHeader = BasicAuthHelper.buildBasicAuthorizationHeader(userName, password);
		return basicAuthHeader;
	}


	/**
	 * Return header value for authorization header.
	 * Currently the authorization header for BASIC auth is returned.
	 * @return
	 */
	private static String getAuthorizationHeader() {
//		return getTokenValue();
		return getBasicAuthHeader();
	}


    public CloseableHttpResponse sendGetRequest(CloseableHttpClient client, String url) throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpGet httpGet = new HttpGet(url);

		// add OAuth token for authorization
    	httpGet.addHeader(AUTHORIZATION, getAuthorizationHeader());

    	CloseableHttpResponse httpResponse = client.execute(httpGet, buildContext());

    	return httpResponse;
    }


    public CloseableHttpResponse sendGetRequest(CloseableHttpClient client, CharSequence url, Header... headers)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpGet httpGet = new HttpGet( url.toString() );

		// add OAuth token for authorization
    	httpGet.addHeader(AUTHORIZATION, getAuthorizationHeader());

    	if (headers != null) {
    		for (Header header : headers) {
    			httpGet.addHeader(header);
			}
    	}

    	CloseableHttpResponse httpResponse = client.execute(httpGet, buildContext());

    	return httpResponse;
    }


    public CloseableHttpResponse sendGetRequest(CloseableHttpClient client, CharSequence url, Collection<Header> headers)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpGet httpGet = new HttpGet( url.toString() );

		// add OAuth token for authorization
    	httpGet.addHeader(AUTHORIZATION, getAuthorizationHeader());

    	if (headers != null) {
    		for (Header header : headers) {
    			httpGet.addHeader(header);
			}
    	}

    	CloseableHttpResponse httpResponse = client.execute(httpGet, buildContext());

    	return httpResponse;
    }


    public CloseableHttpResponse sendDeleteRequest(CloseableHttpClient client, CharSequence url) throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpDelete httpDelete = new HttpDelete( url.toString() );

		// add OAuth token for authorization
		httpDelete.addHeader(AUTHORIZATION, getAuthorizationHeader());

		return client.execute(httpDelete, buildContext());
    }


    public CloseableHttpResponse sendPostRequest(CloseableHttpClient client, CharSequence url, String xmlContent)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpPost httpPost = new HttpPost( url.toString() );

		// add OAuth token for authorization
		httpPost.addHeader(AUTHORIZATION, getAuthorizationHeader());

		// set entity as content
		if (xmlContent != null) {
			StringEntity requestEntity = new StringEntity(xmlContent, CONTENT_TYPE_XML);
			httpPost.setEntity(requestEntity);
		}

		return client.execute(httpPost, buildContext());
    }


    public CloseableHttpResponse sendPostRequest(CloseableHttpClient client, CharSequence url, InputStream inputStream)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpPost httpPost = new HttpPost( url.toString() );

		// add OAuth token for authorization
		httpPost.addHeader(AUTHORIZATION, getAuthorizationHeader());

		// set entity as content
		if (inputStream != null) {
			InputStreamEntity requestEntity = new InputStreamEntity(inputStream, CONTENT_TYPE_BYTES);
			httpPost.setEntity(requestEntity);
		}

		return client.execute(httpPost, buildContext());
    }


    public CloseableHttpResponse sendPostRequest(CloseableHttpClient client, CharSequence url) throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	return sendPostRequest(client, url, (String) null /*xmlContent*/);
    }


    public CloseableHttpResponse sendPutRequest(CloseableHttpClient client, CharSequence url, String xmlContent)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpPut httpPut = new HttpPut( url.toString() );

		// add OAuth token for authorization
		httpPut.addHeader(AUTHORIZATION, getAuthorizationHeader());

		// set entity as content
		StringEntity requestEntity = new StringEntity(xmlContent, CONTENT_TYPE_XML);
		httpPut.setEntity(requestEntity);

		return client.execute(httpPut, buildContext());
    }


    public CloseableHttpResponse sendPutRequest(CloseableHttpClient client, CharSequence url, byte[] content)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpPut httpPut = new HttpPut( url.toString() );

		// add OAuth token for authorization
		httpPut.addHeader(AUTHORIZATION, getAuthorizationHeader());

		// set entity as content
		ByteArrayEntity requestEntity = new ByteArrayEntity(content, CONTENT_TYPE_BYTES);
		httpPut.setEntity(requestEntity);

		return client.execute(httpPut, buildContext());
    }


    public CloseableHttpResponse sendPutRequest(CloseableHttpClient client, CharSequence url, InputStream inputStream)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpPut httpPut = new HttpPut( url.toString() );

		// add OAuth token for authorization
		httpPut.addHeader(AUTHORIZATION, getAuthorizationHeader());

		// set entity as content
		if (inputStream != null) {
			InputStreamEntity requestEntity = new InputStreamEntity(inputStream, CONTENT_TYPE_BYTES);
			httpPut.setEntity(requestEntity);
		}

		return client.execute(httpPut, buildContext());
    }


    public CloseableHttpResponse sendPutRequest(CloseableHttpClient client, CharSequence url, File file)
    throws Exception {
    	Objects.requireNonNull(client);
    	Objects.requireNonNull(url);

    	HttpPut httpPut = new HttpPut( url.toString() );

		// add OAuth token for authorization
		httpPut.addHeader(AUTHORIZATION, getAuthorizationHeader());

		if (file != null) {
			// add Content-Length header
			httpPut.addHeader(CONTENT_LENGTH, String.valueOf(file.length()));

			// set entity as content
			InputStream inputStream = new FileInputStream(file);
			InputStreamEntity requestEntity = new InputStreamEntity(inputStream, CONTENT_TYPE_BYTES);
			httpPut.setEntity(requestEntity);
		}

		return client.execute(httpPut, buildContext());
    }

}
