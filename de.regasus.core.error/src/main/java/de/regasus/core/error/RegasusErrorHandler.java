package de.regasus.core.error;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.exception.DBExceptionAnalyzer;
import com.lambdalogic.messeinfo.exception.DirtyWriteException;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.exception.WarnMessageException;
import de.regasus.auth.AuthorizationException;



/**
 * Regasus specific ErrorHandler that sets the error level according to the type of the Exception.
 * This cannot be done in {@link com.lambdalogic.util.rcp.error.ErrorHandler} for several reasons:
 * - classes like {@link InvalidValuesException} does not exist in com.lambdalogic.util.
 * - {@link DBExceptionAnalyzer} depends on org.hibernate.JDBCException which is not available in com.lambdalogic.util.
 */
public class RegasusErrorHandler extends com.lambdalogic.util.rcp.error.ErrorHandler {

	private static final RegasusErrorHandler INSTANCE = new RegasusErrorHandler();


	protected RegasusErrorHandler() {
		super();
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.error.ErrorHandler#_handleError(java.lang.String, java.lang.String, java.lang.Throwable, com.lambdalogic.util.rcp.error.ErrorHandler.ErrorLevel, com.lambdalogic.i18n.I18NString, com.lambdalogic.i18n.I18NString)
	 */
	@Override
	protected void _handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		ErrorLevel level,
		I18NString i18nMessage,
		I18NString i18nTitle
	) {
		// determine the root Exception and check if throwable is some kind of database exception
		DBExceptionAnalyzer dbExceptionAnalyzer = new DBExceptionAnalyzer();
		dbExceptionAnalyzer.analyze(throwable);
		String details = null;
		if (dbExceptionAnalyzer.isDBException()) {
			if (i18nMessage == null) {
				i18nMessage = dbExceptionAnalyzer.getMessage();
			}

			details = dbExceptionAnalyzer.getDetails();
		}

		Throwable rootException = dbExceptionAnalyzer.getRootException();


		// determine level from Exception type
		if (level == null && rootException != null) {
			if (rootException instanceof InvalidValuesException ||
				rootException instanceof DirtyWriteException
			) {
				level = ErrorLevel.USER;
			}
			else if (rootException instanceof WarnMessageException) {
				level = ErrorLevel.WARNING;
			}
			else if (rootException instanceof AuthorizationException) {
				level = ErrorLevel.SECURITY;
			}
			else {
				level = ErrorLevel.APPLICATION;
			}
		}


		super._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			level,
			i18nMessage,
			i18nTitle,
			details
		);
	}


	// *********************************************************************************************
	// * public static methods (same methods as in com.lambdalogic.util.rcp.error.ErrorHandler)
	// *

	public static void handleErrors(
		final String sourcePlugin,
		final String sourceClass,
		final Throwable[] throwables,
		ErrorLevel level,
		I18NString i18nMessage,
		I18NString i18nTitle
	) {
		INSTANCE._handleErrors(
			sourcePlugin,
			sourceClass,
			throwables,
			level,
			i18nMessage,
			i18nTitle,
			(String) null	// details
		);
	}


	public static void handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		ErrorLevel level,
		I18NString i18nMessage,
		I18NString i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			level,
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleError(
		final String sourcePlugin,
		final String sourceClass,
		final Throwable throwable,
		ErrorLevel level,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			level,
			message,
			title
		);
	}


	public static void handleError(
		final String sourcePlugin,
		final String sourceClass,
		final Throwable throwable,
		ErrorLevel level
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			level,
			(I18NPattern) null,	// i18nMessage
			(I18NPattern) null	// i18nTitle
		);
	}


	public static void handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage,
		I18NPattern i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			null,			// level
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			null,			// level
			message,
			title
		);
	}


	public static void handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			null,			// level
			i18nMessage,
			null			// i18nTitle
		);
	}


	public static void handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			null,			// level
			message,
			null			// title
		);
	}


	public static void handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			null,				// level
			(I18NPattern) null,	// i18nMessage
			(I18NPattern) null	// i18nTitle
		);
	}


	/*
	 * handleSilentError
	 */

	public static void handleSilentError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage,
		I18NPattern i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SILENT,	// level
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleSilentError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SILENT,	// level
			message,
			title
		);
	}


	public static void handleSilentError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SILENT,	// level
			i18nMessage,
			(I18NPattern) null	// i18nTitle
		);
	}


	public static void handleSilentError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SILENT,	// level
			message,
			(String) null		// title
		);
	}


	public static void handleSilentError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SILENT,	// level
			(I18NPattern) null,
			(I18NPattern) null
		);
	}


	/*
	 * handleWarnError
	 */

	public static void handleWarnError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage,
		I18NPattern i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.WARNING,	// level
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleWarnError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.WARNING,	// level
			message,
			title
		);
	}


	public static void handleWarnError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.WARNING,	// level
			i18nMessage,
			(I18NPattern) null	// i18nTitle
		);
	}


	public static void handleWarnError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.WARNING,	// level
			message,
			(String) null		// title
		);
	}


	public static void handleWarnError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.WARNING,	// level
			(I18NPattern) null,	// i18nMessage
			(I18NPattern) null	// i18nTitle
		);
	}


	/*
	 * handleUserError
	 */

	public static void handleUserError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage,
		I18NPattern i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.USER,	// level
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleUserError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.USER,	// level
			message,
			title
		);
	}


	public static void handleUserError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.USER,	// level
			i18nMessage,
			(I18NPattern) null	// i18nTitle
		);
	}


	public static void handleUserError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.USER,	// level
			message,
			(String) null		// title
		);
	}


	public static void handleUserError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.USER,	// level
			(I18NPattern) null,	// i18nMessage
			(I18NPattern) null	// i18nTitle
		);
	}


	/*
	 * handleApplicationError
	 */

	public static void handleApplicationError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage,
		I18NPattern i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.APPLICATION,	// level
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleApplicationError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.APPLICATION,	// level
			message,
			title
		);
	}


	public static void handleApplicationError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.APPLICATION,	// level
			i18nMessage,
			(I18NPattern) null		// i18nTitle
		);
	}


	public static void handleApplicationError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.APPLICATION,	// level
			message,
			(String) null			// title
		);
	}


	public static void handleApplicationError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.APPLICATION,	// level
			(I18NPattern) null,		// i18nMessage
			(I18NPattern) null		// i18nTitle
		);
	}


	/*
	 * handleSecurityError
	 */

	public static void handleSecurityError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage,
		I18NPattern i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SECURITY,	// level
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleSecurityError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SECURITY,	// level
			message,
			title
		);
	}


	public static void handleSecurityError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SECURITY,	// level
			i18nMessage,
			(I18NPattern) null		// i18nTitle
		);
	}


	public static void handleSecurityError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SECURITY,	// level
			message,
			(String) null			// title
		);
	}


	public static void handleSecurityError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.SECURITY,	// level
			(I18NPattern) null,		// i18nMessage
			(I18NPattern) null		// i18nTitle
		);
	}


	/*
	 * handleFatalError
	 */

	public static void handleFatalError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage,
		I18NPattern i18nTitle
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.FATAL,	// level
			i18nMessage,
			i18nTitle
		);
	}


	public static void handleFatalError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message,
		String title
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.FATAL,	// level
			message,
			title
		);
	}


	public static void handleFatalError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		I18NPattern i18nMessage
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.FATAL,	// level
			i18nMessage,
			(I18NPattern) null		// i18nTitle
		);
	}


	public static void handleFatalError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		String message
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.FATAL,	// level
			message,
			(String) null		// title
		);
	}


	public static void handleFatalError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable
	) {
		INSTANCE._handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			ErrorLevel.FATAL,	// level
			(I18NPattern) null,		// i18nMessage
			(I18NPattern) null		// i18nTitle
		);
	}

	// *
	// * public static methods
	// *********************************************************************************************

}
