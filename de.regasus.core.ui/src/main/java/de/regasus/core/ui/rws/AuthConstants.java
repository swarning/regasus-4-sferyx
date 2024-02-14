package de.regasus.core.ui.rws;

import de.regasus.auth.api.GrantType;

public interface AuthConstants {

	String HTTP_HEADER_AUTHORIZATION = "Authorization";

	String RESOURCE_NAME = "auth";


	/**
	 * Path component to the end point that provides access tokens.
	 */
	String PATH_TOKEN = "token";

	/**
	 * Name of the query parameter to provide the grant type.
	 */
	String QUERY_PARAM_GRANT_TYPE = "grant_type";

	/**
	 * Accepted query parameter value for the grant type "password".
	 */
	String GRANT_TYPE_PASSWORD = GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS.getValue();

	/**
	 * Accepted query parameter value for the grant type "refresh_token".
	 */
	String GRANT_TYPE_REFRESH_TOKEN = GrantType.REFRESH_TOKEN.getValue();

	/**
	 * Name of the query parameter to provide the client ID.
	 */
	String QUERY_PARAM_CLIENT_ID = "client_id";

	/**
	 * The Client Secret is used to authenticate the identity of the application to the service API when the application
	 * requests to access a user's account, and must be kept private between the application and the API.
	 */
	String QUERY_PARAM_CLIENT_SECRET = "client_secret";

	/**
	 * After an access token expires, using it to make a request from the API will result in an "Invalid Token Error".
	 * At this point, if a refresh token was included when the original access token was issued, it can be used to
	 * request a fresh access token from the authorization server.
	 */
	String QUERY_PARAM_REFRESH_TOKEN = "refresh_token";

	/**
	 * Name of the query parameter to provide the user name (user ID) of a User Account.
	 */
	String QUERY_PARAM_REGASUS_USER = "regasus_user";

	/**
	 * Name of the query parameter to provide the user name of a Profile.
	 */
	String QUERY_PARAM_PROFILE_USER = "profile_user";

	/**
	 * Name of the query parameter to provide the last name of a Profile.
	 */
	String QUERY_PARAM_PROFILE_LAST_NAME= "profile_last_name";

	/**
	 * Name of the query parameter to provide the last name of a Participant.
	 */
	String QUERY_PARAM_PARTICIPANT_LAST_NAME= "participant_last_name";

	/**
	 * Name of the query parameter to provide a password (either for a User Account or a Profile).
	 */
	String QUERY_PARAM_PASSWORD = "password";

	/**
	 * Name of the query parameter to provide a vigenere code 1 of a Profile or Participant.
	 */
	String QUERY_PARAM_VIGENERE1 = "vigenere1";

	/**
	 * Name of the query parameter to provide a vigenere code 2 of a Profile or Participant.
	 */
	String QUERY_PARAM_VIGENERE2 = "vigenere2";

	/**
	 * Name of the query parameter to provide a vigenere code 1 of a Profile.
	 */
	String QUERY_PARAM_PROFILE_VIGENERE1 = "profile_vigenere1";

	/**
	 * Name of the query parameter to provide a vigenere code 2 of a Profile.
	 */
	String QUERY_PARAM_PROFILE_VIGENERE2 = "profile_vigenere2";

	/**
	 * Name of the query parameter to provide a vigenere code 1 of a Participant.
	 */
	String QUERY_PARAM_PARTICIPANT_VIGENERE1 = "participant_vigenere1";

	/**
	 * Name of the query parameter to provide a vigenere code 2 of a Participant.
	 */
	String QUERY_PARAM_PARTICIPANT_VIGENERE2 = "participant_vigenere2";

	/**
	 * Name of the query parameter to provide the a participant number.
	 */
	String QUERY_PARAM_PARTICIPANT_NUMBER= "participant_number";

	/**
	 * Name of the query parameter to provide the event mnemonic
	 */
	String QUERY_PARAM_EVENT_MNEMONIC = "event_mnemonic";

}
