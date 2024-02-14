package de.regasus.onlineform.admin.dialog;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.ListSet;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.ParticipantType;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.admin.ui.Activator;
import de.regasus.participant.ParticipantTypeModel;

public class CopyRegistrationFormWizardOptionsPage extends WizardPage {

	public static final String NAME = "CopyRegistrationFormWizardOptionsPage";

	private Text webIdText;

	private Button copyUploadedFilesButton;
	private Button copyEmailTemplatesButton;
	private Button copyBookingRulesButton;

	private Collection<Long> referencedParticipantTypePKs;

	private Label missingParticipantTypesLabel;

	private Set<Long> requiredTargetParticipantTypePKs;

	private boolean additionOfParticipantTypesNeeded;


	public CopyRegistrationFormWizardOptionsPage() {
		super(NAME);

		setTitle(UtilI18N.Options);
		setDescription(UtilI18N.Options);
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		SWTHelper.createLabel(composite, OnlineFormI18N.WebId);
		webIdText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, false);
		webIdText.setLayoutData(gridData);
		webIdText.setToolTipText(OnlineFormI18N.WebIdToolTip);
		gridData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(webIdText, 50);
		webIdText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(! webIdText.getText().isEmpty());
			}
		});

		SWTHelper.createLabel(composite, OnlineFormI18N.CopyUploadedFiles);
		copyUploadedFilesButton = new Button(composite, SWT.CHECK);
		copyUploadedFilesButton.setSelection(true);

		SWTHelper.createLabel(composite, OnlineFormI18N.CopyEmailTemplates);
		copyEmailTemplatesButton = new Button(composite, SWT.CHECK);
		copyEmailTemplatesButton.setSelection(false);

		SWTHelper.createLabel(composite, OnlineFormI18N.CopyBookingRules);
		copyBookingRulesButton = new Button(composite, SWT.CHECK);
		copyBookingRulesButton.setSelection(false);

		// Separator
		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		missingParticipantTypesLabel = new Label(composite, SWT.WRAP);
		missingParticipantTypesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		setControl(composite);
	}


	public boolean isCopyUploadedFiles() {
		return copyUploadedFilesButton.getSelection();
	}

	public boolean isCopyEmailTemplates() {
		return copyEmailTemplatesButton.getSelection();
	}

	public boolean isCopyBookingRules() {
		return copyBookingRulesButton.getSelection();
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			computeIfAdditionalParticipantTypesAreNeeded();
		}
		super.setVisible(visible);
	}


	/**
	 * Handle missing Participant Types when copying a Registration Form Config (MIRCP-2371)
	 */
	private void computeIfAdditionalParticipantTypesAreNeeded() {

		EventWizardPage eventWizardPage = (EventWizardPage) getWizard().getPage(de.regasus.event.dialog.EventWizardPage.ID);
		Long targetEventPK = eventWizardPage.getEventId();
		try {
			ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();
			List<ParticipantType> targetParticipantTypes = participantTypeModel.getParticipantTypesByEvent(targetEventPK);
			List<Long> targetParticipantTypePKs = ParticipantType.getPKs(targetParticipantTypes);

			requiredTargetParticipantTypePKs = new ListSet<>(targetParticipantTypePKs);
			requiredTargetParticipantTypePKs.addAll(referencedParticipantTypePKs);

			if (requiredTargetParticipantTypePKs.size() > targetParticipantTypePKs.size()) {
				additionOfParticipantTypesNeeded = true;

				List<Long> missingParticipantTypePKs = new ArrayList<>(requiredTargetParticipantTypePKs);
				missingParticipantTypePKs.removeAll(targetParticipantTypePKs);


				// build String with names of missing Participant Types
				String participantTypeNamesString;
				{
    				List<ParticipantType> missingParticipantTypes = participantTypeModel.getParticipantTypes(missingParticipantTypePKs);
    				List<String> missingParticipantTypeNames = new ArrayList<>();
    				for (ParticipantType participantType : missingParticipantTypes) {
    					missingParticipantTypeNames.add(participantType.getName().getString());
    				}
    				participantTypeNamesString = StringHelper.concatIfNotEmpty(", ", missingParticipantTypeNames);
				}

				String message = OnlineFormI18N.TheseMissingParticipantTypesWillBeCreated.replace("<types>", participantTypeNamesString);
				missingParticipantTypesLabel.setText(message);
			}
			else {
				additionOfParticipantTypesNeeded = false;
				missingParticipantTypesLabel.setText("");
			}
		}
		catch (Exception ex) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), ex);
		}
	}


	public void setWebId(String webId) {
		// don't want to override previous user inputs
		if (webIdText.getText().isEmpty()) {
			webIdText.setText( avoidNull(webId) );
		}
	}


	public String getWebId() {
		return webIdText.getText();
	}


	public void setReferredParticipantTypes(Collection<Long> pkList) {
		this.referencedParticipantTypePKs = pkList;
	}


	/**
	 * The complete list (previous plus additional ones) of participant types
	 * that the target event needs to have so that the config may be copied safely.
	 */
	public List<Long> getRequiredTargetParticipantTypePKlist() {
		return new ArrayList<>(requiredTargetParticipantTypePKs);
	}


	/**
	 * A flag saying whether the above requiredTargetParticipantTypePKset contains
	 * more elements than the currentTargetParticipantTypeVOs, so that the
	 * wizard knows wether they need to be set anew.
	 */
	public boolean isAdditionOfParticipantTypesNeeded() {
		return additionOfParticipantTypesNeeded;
	}

}
