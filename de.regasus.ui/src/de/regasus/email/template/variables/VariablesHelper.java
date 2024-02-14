package de.regasus.email.template.variables;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.lambdalogic.util.FileHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PaymentLinkConstants;
import de.regasus.ui.Activator;

public class VariablesHelper {


	private final String MESSE_INFO = "MesseInfo";

	/**
	 * Depending on platform and user, this could be C:\Dokumente und Einstellungen\manfred\Anwendungsdaten\MesseInfo
	 */
	private File directory;

	private File participantVarsFile;

	private File profileVarsFile;

	private List<VariableValuePair> participantVariableValuePairs;

	private List<VariableValuePair> profileVariableValuePairs;


	public VariablesHelper() {
		 directory = FileHelper.getWorkingDirectory(MESSE_INFO);
		 profileVarsFile = new File(directory, "ProfileVars.txt");
		 participantVarsFile = new File(directory, "ParticipantVars.txt");

		 try {
				List<String> participantVars;
				if (participantVarsFile.exists()) {
					participantVars = FileHelper.readFileLinesIntoArrayList(participantVarsFile);

					// Add NEW variables, they are also in the initial list but wouldn't be used
					// in this case when the file alredy exists
					String[] newVars = new String[] {
							"${event.name}",
							"${event.label}",
							"${event.mnemonic}",
							"${event.location}",
							"${mediumDate.format(event.startTime)}",
							"${mediumDate.format(event.endTime)}"
					};
					for (String newVar : newVars) {
						if (! participantVars.contains(newVar)) {
							participantVars.add(newVar);
						}
					}
				} else {
					participantVars = Arrays.asList(initialParticipantVars);
					FileHelper.writeStringCollectionAsFileLines(participantVars, participantVarsFile);
				}
				participantVariableValuePairs = VariableValuePair.createFromVariableList(participantVars);
			}
			catch (IOException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, VariablesHelper.class.getName(), e);
			}

			try {
				List<String> profileVars;
				if (profileVarsFile.exists()) {
					profileVars = FileHelper.readFileLinesIntoArrayList(profileVarsFile);
				} else {
					profileVars = Arrays.asList(initialProfileVars);
					FileHelper.writeStringCollectionAsFileLines(profileVars, profileVarsFile);
				}
				profileVariableValuePairs = VariableValuePair.createFromVariableList(profileVars);
			}
			catch (IOException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, VariablesHelper.class.getName(), e);
			}

	}


	public  List<VariableValuePair> getVariableValuePairList(boolean forEvent) {
		if (forEvent)
			return participantVariableValuePairs;
		else
			return profileVariableValuePairs;
	}


	public void saveVariableValuePairList(boolean forEvent) {
		try {
			if (forEvent) {
				List<String> participantVars = VariableValuePair.getVariableList(participantVariableValuePairs);
				FileHelper.writeStringCollectionAsFileLines(participantVars, participantVarsFile);
			} else {
				List<String> profileVars = VariableValuePair.getVariableList(profileVariableValuePairs);
				FileHelper.writeStringCollectionAsFileLines(profileVars, profileVarsFile);
			}
		}
		catch (IOException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, VariablesHelper.class.getName(), e);
		}

	}


	public static String getDefaultToAddrVariable() {
		return "${p.email1}";
	}


	private static String[] initialParticipantVars = {
		"${p.communication.email1}",
		"${p.name}",
		"${p.salutation}",
		"${p.firstName}",
		"${p.middleName}",
		"${p.lastName}",
		"${p.registerDate}",
		"${mediumDate.format(p.registerDate)}",
		"${p.participantType}",
		"${p.participantState}",
		"${p.vigenereCode}",
		"${p.vigenere2Code}",
		"${p.vigenere2CodeUrlSafe}",
		"${p.vigenere2CodeHex}",
		"${p.customerNo}",
		"${p.gender}",
		"${mediumDate.format(p.dateOfBirth)}",
		"${p.adminTitle}",
		"${p.degree}",
		"${p.mandate}",
		"${p.function}",
		"${p.language}",
		"${p.mainCountry}",
		"${p.invCountry}",
		"${p.nationality}",
		"${p.getCustomField(1)}",
		"${p.getCustomField(2)}",
		"${p.getCustomField(3)}",
		"${p.getCustomField(4)}",
		"${p.getCustomField(5)}",
		"${p.getCustomField(6)}",
		"${p.getCustomField(7)}",
		"${p.getCustomField(8)}",
		"${p.getCustomField(9)}",
		"${p.getCustomField(10)}",
		"${event.name}",
		"${event.label}",
		"${event.location}",
		"${mediumDate.format(event.startTime)}",
		"${mediumDate.format(event.endTime)}",
		"${subscribeNewsletter}",
		PaymentLinkConstants.PAYMENT_LINK_VARIABLE,
		PaymentLinkConstants.PROGRAMME_PAYMENT_LINK_VARIABLE,
		PaymentLinkConstants.HOTEL_PAYMENT_LINK_VARIABLE
	};


	private static String[] initialProfileVars = {
		"${p.communication.email1}",
		"${p.name}",
		"${p.salutation}",
		"${p.firstName}",
		"${p.middleName}",
		"${p.lastName}",
		"${p.gender}",
		"${mediumDate.format(p.dateOfBirth)}",
		"${p.placeOfBirth}",
		"${p.adminTitle}",
		"${p.degree}",
		"${p.mandate}",
		"${p.function}",
		"${p.language}",
		"${p.mainCountry}",
		"${p.invCountry}",
		"${p.nationality}",
		"${p.customerNo}"
	};

}
