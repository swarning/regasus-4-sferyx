package com.lambdalogic.util.rcp.error;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.i18n.I18NMessageException;
import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.time.I18NDateSecond;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.ThrowableHelper;
import com.lambdalogic.util.exception.MultiException;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.dialog.MessageDetailsDialog;

public class ErrorHandler {
	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public enum ErrorLevel {SILENT, USER, WARNING, APPLICATION, SECURITY, FATAL}

	protected static LinkedHashMap<String, String> globalInfoMap = null;


	private static final ErrorHandler INSTANCE = new ErrorHandler();


	protected ErrorHandler() {
	}


	public static void putGlobalInfo(String title, String text) {
		if (globalInfoMap == null) {
			globalInfoMap = new LinkedHashMap<>();
		}

		globalInfoMap.put(title, text);
	}


	public static String getGlobalInfoText() {
		String globalInfoText = null;
		if (globalInfoMap != null) {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : globalInfoMap.entrySet()) {
				String title = entry.getKey();
				String text = entry.getValue();
				if (sb.length() > 0) {
					sb.append('\n');
				}
				sb.append(title);
				sb.append(": ");
				sb.append(text);
			}
			globalInfoText = sb.toString();
		}
		return globalInfoText;
	}




	// *********************************************************************************************
	// * the actual error handling code in non-public and non-static methods
	// *

	protected void _handleErrors(
		final String sourcePlugin,
		final String sourceClass,
		final Throwable[] throwables,
		ErrorLevel level,
		I18NString i18nMessage,
		I18NString i18nTitle,
		String details
	) {
		// Don't show any ErrorMessage if the application is closing.
//		if (PlatformUI.getWorkbench().isClosing()) {
//			return;
//		}

		String language = Locale.getDefault().getLanguage();


		// determine ErrorLevel
		if (level == null) {
			level = ErrorLevel.APPLICATION;
		}


		// Logging
		if (level == ErrorLevel.SILENT ||
			level == ErrorLevel.WARNING ||
			level == ErrorLevel.APPLICATION ||
			level == ErrorLevel.FATAL
		) {
			I18NPattern errorMessage = _getErrorMessage(
				sourcePlugin,
				sourceClass,
				throwables,
				level,
				i18nMessage
			);


			if (level == ErrorLevel.SILENT) {
				log.warn( errorMessage.getString() );
			}
			else if (level == ErrorLevel.WARNING) {
				log.warn( errorMessage.getString() );
			}
			else if (level == ErrorLevel.APPLICATION) {
				log.error( errorMessage.getString() );
			}
			else if (level == ErrorLevel.FATAL) {
				log.error( errorMessage.getString() );
			}
		}


//		Display.getDefault().syncExec(new Runnable() {
//			public void run() {
//
//			}
//		});



		// Error-Dialog
		if (level == ErrorLevel.SILENT) {
			// No Dialog (silent)
		}
		else if (level == ErrorLevel.USER) {
			final String title;
			if (i18nTitle != null) {
				title = i18nTitle.getString(language);
			}
			else {
				title = UtilI18N.UserError;
			}

			final String message;
			if (i18nMessage != null) {
				message = i18nMessage.getString();
			}
			else {
				message = UtilI18N.UndefinedUserError;
			}

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openInformation(null, title, message);
				}
			});
		}
		else if (level == ErrorLevel.WARNING) {
			final String title;
			if (i18nTitle != null) {
				title = i18nTitle.getString(language);
			}
			else {
				title = UtilI18N.Warning;
			}

			final String message;
			if (i18nMessage != null) {
				message = i18nMessage.getString();
			}
			else {
				message = UtilI18N.UndefinedUserError;
			}

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(null, title, message);
				}
			});
		}
		else if (level == ErrorLevel.APPLICATION) {
			final String title;
			if (i18nTitle != null) {
				title = i18nTitle.getString(language);
			}
			else {
				title = UtilI18N.ApplicationError;
			}

			final String message;
			if (i18nMessage != null) {
				message = i18nMessage.getString();
			}
			else {
				message = UtilI18N.UndefinedApplicationError;
			}

			if (details == null) {
				details = getDetails(sourcePlugin, sourceClass, throwables, level);
			}
			else {
				details += "\n\n\n" + getDetails(sourcePlugin, sourceClass, throwables, level);
			}
			final String finalDetails = details;

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDetailsDialog.openError(title, message, finalDetails);
				}
			});
		}
		else if (level == ErrorLevel.SECURITY) {
			final String title;
			if (i18nTitle != null) {
				title = i18nTitle.getString(language);
			}
			else {
				title = UtilI18N.AccessDenied;
			}

			final String message;
			if (i18nMessage != null) {
				message = i18nMessage.getString();
			}
			else {
				message = UtilI18N.YouDontHaveTheRequiredAccessRights;
			}

			final ErrorLevel level_final = level;
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					String details = getDetails(sourcePlugin, sourceClass, throwables, level_final);
					MessageDetailsDialog.openError(title, message, details);
				}
			});
		}
		else if (level == ErrorLevel.FATAL) {
			final String title;
			if (i18nTitle != null) {
				title = i18nTitle.getString(language);
			}
			else {
				title = UtilI18N.FatalError;
			}

			final StringBuilder message = new StringBuilder();
			if (i18nMessage != null) {
				message.append(i18nMessage.getString());
			}
			else {
				message.append(UtilI18N.UndefinedFatalError);
			}
			message.append("\n\n");
			message.append(UtilI18N.FatalErrorMessage);

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(null, title, message.toString());
				}
			});
		}
	}


	protected void _handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		ErrorLevel level,
		I18NString i18nMessage,
		I18NString i18nTitle
	) {
		_handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			level,
			i18nMessage,
			i18nTitle,
			(String) null	// details
		);
	}


	protected void _handleError(
		String sourcePlugin,
		String sourceClass,
		Throwable throwable,
		ErrorLevel level,
		I18NString i18nMessage,
		I18NString i18nTitle,
		String details
	) {
		// determine the root Exception
		throwable = ThrowableHelper.findRootThrowable(throwable);


		// create Throwable[]
		Throwable[] throwables = null;
		if (throwable instanceof MultiException) {
			MultiException me = (MultiException) throwable;
			throwables = me.getThrowables().toArray(new Throwable[me.size()]);
		}
		else {
			throwables = new Throwable[] {throwable};
		}


		// build i18nMessage out of throwable
		if (i18nMessage == null) {
			if (throwable instanceof I18NMessageException) {
				i18nMessage = ((I18NMessageException) throwable).getI18NMessage();
			}

			// set message from Exception
			if (i18nMessage == null) {
				String message = throwable.getMessage();
				if (StringHelper.isNotEmpty(message)) {
					i18nMessage = new I18NPattern(message);
				}
			}
		}


		_handleErrors(
			sourcePlugin,
			sourceClass,
			throwables,
			level,
			i18nMessage,
			i18nTitle,
			details
		);
	}


	protected void _handleError(
		final String sourcePlugin,
		final String sourceClass,
		final Throwable throwable,
		ErrorLevel level,
		String message,
		String title
	) {
		I18NString i18nMessage = null;
		if (message != null) {
			i18nMessage = new I18NPattern(message);
		}

		I18NString i18nTitle = null;
		if (title != null) {
			i18nTitle = new I18NPattern(title);
		}


		_handleError(
			sourcePlugin,
			sourceClass,
			throwable,
			level,
			i18nMessage,
			i18nTitle
		);
	}


	protected static I18NPattern _getErrorMessage(
		String sourcePlugin,
		String sourceClass,
		Throwable[] throwables,
		ErrorLevel level,
		I18NString message
	) {
		I18NPattern errorMessage = new I18NPattern();

		String levelText = null;
		switch (level) {
    		case SILENT:
    		case WARNING:
    			levelText = UtilI18N.Warning;
    			break;
    		case USER:
    			levelText = UtilI18N.UserError;
    			break;
    		case APPLICATION:
    			levelText = UtilI18N.ApplicationError;
    			break;
    		case FATAL:
    			levelText = UtilI18N.FatalError;
    			break;
    		case SECURITY:
    			levelText = UtilI18N.SecurityError;
    			break;
    		default:
    			levelText = UtilI18N.Error;
		}
		errorMessage.add(levelText);
		errorMessage.add("\n");

		errorMessage.add(UtilI18N.Time);
		errorMessage.add(": ");
		errorMessage.add(I18NDateSecond.now());
		errorMessage.add(".");

		if (sourcePlugin != null || sourceClass != null) {
			errorMessage.add("\n");
			errorMessage.add(UtilI18N.Source);
			errorMessage.add(": ");
			if (sourcePlugin != null) {
				errorMessage.add(UtilI18N.Plugin);
				errorMessage.add("=");
				errorMessage.add(sourcePlugin);
			}
			if (sourceClass != null) {
				if (sourcePlugin != null) {
					errorMessage.add("\n");
				}
				errorMessage.add(UtilI18N.Class);
				errorMessage.add("=");
				errorMessage.add(sourceClass);
			}
			errorMessage.add(".");
		}

		if (message != null) {
			errorMessage.add("\n");
			errorMessage.add(UtilI18N.Message);
			errorMessage.add(": ");
			errorMessage.add(message);
			errorMessage.add(".");
		}

		if (throwables != null) {
			if (throwables.length == 1 && throwables[0] != null) {
				String throwableMessage = throwables[0].getMessage();
				if (throwableMessage != null) {
					errorMessage.add("\n");
					errorMessage.add(UtilI18N.Error);
					errorMessage.add(": ");
					errorMessage.add(throwableMessage);
				}

				errorMessage.add("\n");
				errorMessage.add("Stacktrace");
				errorMessage.add(": ");
				errorMessage.add(getStackTrace(throwables[0]));
			}
			else if (throwables.length > 1) {
				for (int i = 0; i < throwables.length; i++) {
					if (throwables[i] != null) {
						String throwableMessage = throwables[i].getMessage();
						if (throwableMessage != null) {
							errorMessage.add("\n");
							errorMessage.add(UtilI18N.Error);
							errorMessage.add(" (");
							errorMessage.add(i);
							errorMessage.add("/");
							errorMessage.add(throwables.length);
							errorMessage.add("): ");
							errorMessage.add(throwableMessage);
						}

						errorMessage.add("\n");
						errorMessage.add("Stacktrace");
						errorMessage.add(": ");
						errorMessage.add( getStackTrace(throwables[i]) );
					}
				}
			}
		}

		if (level == ErrorLevel.FATAL) {
			errorMessage.add("\n");
			errorMessage.add(UtilI18N.ApplicationWillBeClosed);
		}

		return errorMessage;
	}


	protected static String getStackTrace(Throwable throwable) {
		String result = null;
		if (throwable != null) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			PrintStream printStream = new PrintStream(byteArrayOutputStream);
			throwable.printStackTrace(printStream);
			result = byteArrayOutputStream.toString();
		}
		return result;
	}

	// *
	// * the actual error handling code in non-public and non-static methods
	// *********************************************************************************************


	private static String getDetails(
		String sourcePlugin,
		String sourceClass,
		Throwable[] throwables,
		ErrorLevel level
	) {
		StringBuilder details = new StringBuilder();

		// show timestamp
		if (details.length() > 0) {
			details.append("\n\n");
		}
		details.append("Time: ").append(new Date());

		// if this plugin is running within a product, show its name
		IProduct product = Platform.getProduct();
		if (product != null) {
			String productName = product.getName();
			if (details.length() > 0) {
				details.append("\n\n");
			}
			details.append("Product: ").append(productName);
		}

		String javaVersion = System.getProperty("java.version");
		if (details.length() > 0) {
			details.append("\n\n");
		}
		details.append("Java-Version: ").append(javaVersion);

		String trustStore = System.getProperty("javax.net.ssl.trustStore");
		if (details.length() > 0) {
			details.append("\n\n");
		}
		details.append("Trust-Store: ").append(trustStore);


		// show global infos
		String globalInfoText = ErrorHandler.getGlobalInfoText();
		if (globalInfoText != null) {
			if (details.length() > 0) {
				details.append("\n\n");
			}
//			details.append("Global Infos:\n");
			details.append(globalInfoText);
		}



		if (sourcePlugin != null) {
			if (details.length() > 0) {
				details.append("\n\n");
			}
			details.append("Source-Plug-in: ").append(sourcePlugin);
		}

		if (sourceClass != null) {
			if (details.length() > 0) {
				details.append("\n\n");
			}
			details.append("Source-Class: ").append(sourceClass);
		}

		if (sourceClass != null) {
			if (details.length() > 0) {
				details.append("\n\n");
			}
			details.append("Error-Level: ").append(level);
		}

		if (throwables != null) {
			if (throwables.length == 1 && throwables[0] != null) {
				if (details.length() > 0) {
					details.append("\n\n");
				}
				details.append("Throwable: ").append(throwables[0].getMessage());
				details.append("\n\nStacktrace:\n");

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(byteArrayOutputStream);
				throwables[0].printStackTrace(printStream);
				String stackTrace = byteArrayOutputStream.toString();

				details.append(stackTrace);
			}
			else if (throwables.length > 1) {
				for (int i = 0; i < throwables.length; i++) {
					if (throwables[i] != null) {
						if (details.length() > 0) {
							details.append("\n\n");
						}

						details.append("Throwable (").append(i).append("/").append(throwables.length).append("): ");
						details.append(throwables[i].getMessage());
						details.append("\n\nStacktrace:\n");

						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						PrintStream printStream = new PrintStream(byteArrayOutputStream);
						throwables[i].printStackTrace(printStream);
						String stackTrace = byteArrayOutputStream.toString();

						details.append(stackTrace);
					}
				}
			}
		}



		return details.toString();
	}



	// *********************************************************************************************
	// * public static methods (the same methods exist in de.regasus.core.error.ErrorHandler)
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


	public static void logError(Throwable throwable) {
		String message = throwable.getMessage();
		if (message != null) {
			log.error(message);
		}

		String stackTrace = getStackTrace(throwable);
		log.error(stackTrace);
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
			(I18NPattern) null,	// i18nMessage
			(I18NPattern) null	// i18nTitle
		);
	}

	// *
	// * public static methods
	// *********************************************************************************************

}
