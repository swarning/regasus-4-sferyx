/**
 * OpenCertificateCommandHandler.java
 * created on 16.09.2013 11:52:38
 */
package de.regasus.participant.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.interfaces.CertificateResult;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.rcp.dialog.MessageDetailsDialog;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;


public class OpenCertificateCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			IParticipant participant = ParticipantSelectionHelper.getParticipant(event);
			if (participant != null) {
				Long participantID = participant.getPK();
				if (participantID != null) {
					// proceed only if there is no ParticipantEditor for the participant with unsaved data
					boolean editorSaveCheckOK = ParticipantEditor.saveEditor(participantID);
					if (editorSaveCheckOK) {
        				ParticipantModel participantModel = ParticipantModel.getInstance();
        				
        				// generate certificate documents
        				CertificateResult certificateResult = participantModel.generateCertificateDocuments(
    						participantID,
    						null // certificatePolicySource
    					);
        				
        				// log messages to System.out
        				System.out.println("--------------------------------------------------------------------------------");
        				System.out.println("Messages of certificate generation:");
        				System.out.println("--------------------------------------------------------------------------------");
        				System.out.println(certificateResult.getMessages());
        				System.out.println("--------------------------------------------------------------------------------");
        				
        				List<DocumentContainer> certificateDocumentList = certificateResult.getDocumentContainerList();
        				
        				// open certificate documents
        				if (certificateDocumentList != null) {
        					for (DocumentContainer documentContainer : certificateDocumentList) {
        						/* save and open generated certificate file
        						 * This code is referenced by 
        						 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
        						 * Adapt the wiki document if this code is moved to another class or method.
        						 */
        						documentContainer.open();
        					}
        				}
        				
        				// show dialog that informs the user about the result
        				String msg = I18N.OpenCertificateCommandHandler_FinalMessage;
        				msg = msg.replaceFirst("<count>", String.valueOf(certificateResult.getCertificateCount()));

        				MessageDetailsDialog.openInformation(
    						UtilI18N.Info,				// title
    						msg,							// message
    						certificateResult.getMessages()	// details
    					);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return null;
	}

}
