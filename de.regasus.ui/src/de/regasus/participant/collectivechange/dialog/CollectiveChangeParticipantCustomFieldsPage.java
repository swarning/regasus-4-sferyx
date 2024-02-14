package de.regasus.participant.collectivechange.dialog;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.contact.CustomFieldUpdateParameter;
import com.lambdalogic.messeinfo.contact.CustomFieldValue;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.customfield.CustomFieldWidgetComposite;
import de.regasus.event.customfield.combo.ParticipantCustomFieldCombo;
import de.regasus.ui.Activator;


public class CollectiveChangeParticipantCustomFieldsPage extends WizardPage {

	private static final int FIELD_COUNT = 10;

	public static String NAME = "CollectiveChangeParticipantCustomFieldsPage";

	private Composite mainComposite;
	private List<ParticipantCustomFieldCombo> customFieldComboList = createArrayList();
	private List<CustomFieldWidgetComposite> customFieldWidgetCompositeList = createArrayList();
	private List<Button> customFieldOverwriteCheckBoxList = createArrayList();

	private Long eventID;


	/**
	 * Wizard page to enter which Participant Custom Fields should get which values.
	 *
	 * @param eventID PK of the Event whose Participant Custom Fields should be presented
	 * @param participantCount number of Participants whose Participant Custom Field Values should change
	 */
	protected CollectiveChangeParticipantCustomFieldsPage(Long eventID, int participantCount) {
		super(NAME);

		this.eventID = eventID;

		setTitle(I18N.CollectiveChange);

		String desc = I18N.CollectiveChangeParticipantCustomFields;
		desc = desc.replaceFirst("<count>", String.valueOf(participantCount));
		setDescription(desc);
	}


	@Override
	public void createControl(Composite parent) {
		try {
			mainComposite = SWTHelper.createScrolledContentComposite(parent);

			mainComposite.setLayout(new GridLayout(4, false));

			for (int i = 0; i< FIELD_COUNT; i++) {
				createCustomFieldEditRow(mainComposite);
			}


			setControl(mainComposite);

			refreshGUI();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void updatePageComplete() {
		boolean anyRowComplete = false;

		for (int i = 0; i < FIELD_COUNT; i++) {
			ParticipantCustomFieldCombo combo = customFieldComboList.get(i);
			ParticipantCustomField cf = combo.getEntity();
			if (cf != null) {
				anyRowComplete = true;
				break;
			}
		}
		setPageComplete(anyRowComplete);
	}


	private void createCustomFieldEditRow(final Composite mainComposite) throws Exception {
		// column 1
		Label label = new Label(mainComposite, SWT.NONE);
		label.setText(ContactLabel.CustomField.getString());
		final GridData labelLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		labelLayoutData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		label.setLayoutData(labelLayoutData);

		// column 2
		final ParticipantCustomFieldCombo customFieldCombo = new ParticipantCustomFieldCombo(mainComposite, SWT.READ_ONLY);
		customFieldCombo.setEventID(eventID);
		final GridData comboLayoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		comboLayoutData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		comboLayoutData.widthHint = 300;
		customFieldCombo.setLayoutData(comboLayoutData);
		customFieldComboList.add(customFieldCombo);


		// column 3
		final CustomFieldWidgetComposite customFieldWidgetComposite = new CustomFieldWidgetComposite(mainComposite, SWT.NONE);
		/* Do NOT set LayoutData of customFieldWidgetComposite, because it sets its own GridData
		 * which depends on the values of isGrabHorizontalSpace() and isGrabVerticalSpace() of its
		 * internal customFieldWidget.
		 */
		customFieldWidgetCompositeList.add(customFieldWidgetComposite);


		// column 4
		final Button overwriteCheckBox = new Button(mainComposite, SWT.CHECK);
		overwriteCheckBox.setText(UtilI18N.Overwrite);
		final GridData overwriteLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		overwriteLayoutData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		overwriteCheckBox.setLayoutData(overwriteLayoutData);
		customFieldOverwriteCheckBoxList.add(overwriteCheckBox);


		// Changing a different custom field requires that previously set values are deleted
		customFieldCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				CustomField customField = customFieldCombo.getEntity();
				customFieldWidgetComposite.setCustomField(customField);

				overwriteCheckBox.setEnabled(customField != null);

				refreshGUI();
			}

		});
	}


	private void refreshGUI() {
		// refresh GUI
		mainComposite.layout(true, true);
		SWTHelper.refreshSuperiorScrollbar(mainComposite);

		updatePageComplete();
	}


	public List<CustomFieldUpdateParameter> getParameters() throws Exception {
		List<CustomFieldUpdateParameter> parameters = createArrayList();

		for (int i = 0; i < FIELD_COUNT; i++) {
			CustomFieldWidgetComposite customFieldWidgetComposite = customFieldWidgetCompositeList.get(i);
			if (customFieldWidgetComposite.isEnabled()) {
				ParticipantCustomField customField = customFieldComboList.get(i).getEntity();
				CustomFieldValue customFieldValue = customFieldWidgetComposite.getValue();
				boolean selection = customFieldOverwriteCheckBoxList.get(i).getSelection();

				CustomFieldUpdateParameter parameter = new CustomFieldUpdateParameter(customField);
				parameter.setCustomFieldValue(customFieldValue);
				parameter.setOverwrite(selection);

				parameters.add(parameter);
			}
		}
		return parameters;
	}

}
