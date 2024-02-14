package de.regasus.email.template.editor;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateEvaluationType;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.report.script.ScriptContext;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.email.EmailI18N;
import de.regasus.email.template.combo.EmailTemplateEvaluationTypeCombo;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

/**
 * An SWT group that shows and allows editing of
 * <ul>
 * <li>name</li>
 * <li>language</li>
 * <li>event</li>
 * </ul>
 * of an {@link EmailTemplate}. I made the event not editable, since I expect that mails are very event specific and
 * shouldn't by accident be shoved to another one . The group is used in the {@link EmailTemplateEditor} and could be
 * expected to appear in a wizard, too.
 */
public class DefinitionGroup extends Group {

	// *************************************************************************
	// * Widgets
	// *

	private Text templateName;

	private LanguageCombo languageCombo;

	private EmailTemplateEvaluationTypeCombo typeCombo;

	private Label event;

	private Button sendOnlyWithDynamicAttachmentButton;


	// *************************************************************************
	// * Other Attributes
	// *

	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	private Label eventLabel;


	// *************************************************************************
	// * Constructor
	// *

	public DefinitionGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setText(EmailLabel.EmailTemplate.getString());

		setLayout(new GridLayout(2, false));

		// Event - ReadOnly

		eventLabel = new Label(this, SWT.NONE);
		eventLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		eventLabel.setText(EmailI18N.GenericTemplate);

		event = new Label(this, SWT.NONE);
		event.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Template Name
		{
			Label templateNameLabel = new Label(this, SWT.NONE);
			templateNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			templateNameLabel.setText(UtilI18N.Name);
			SWTHelper.makeBold(templateNameLabel);

			templateName = new Text(this, SWT.BORDER);
			templateName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			SWTHelper.makeBold(templateName);

			templateName.addModifyListener(modifySupport);
		}

		// Language
		{
			Label languageLabel = new Label(this, SWT.NONE);
			languageLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			languageLabel.setText( Person.LANGUAGE_CODE.getLabel() );

			languageCombo = new LanguageCombo(this, SWT.NONE);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			languageCombo.addModifyListener(modifySupport);
		}

		// Type
		{
			Label typeLabel = new Label(this, SWT.NONE);
			typeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			typeLabel.setText(EmailLabel.EvaluationType.getString());

			typeCombo = new EmailTemplateEvaluationTypeCombo(this, SWT.NONE);
			typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			typeCombo.addModifyListener(modifySupport);
		}

		new Label(this, SWT.NONE); // Dummy because of layout

		{
			sendOnlyWithDynamicAttachmentButton = new Button(this, SWT.CHECK);
			sendOnlyWithDynamicAttachmentButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

			sendOnlyWithDynamicAttachmentButton.addSelectionListener(modifySupport);
			sendOnlyWithDynamicAttachmentButton.setText(EmailLabel.SendOnlyWithDynamicAttachment.getString());
			sendOnlyWithDynamicAttachmentButton.setToolTipText(EmailLabel.SendOnlyWithDynamicAttachment_Desc.getString());
		}

	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	// **************************************************************************
	// * Synching and Modifying
	// *

	/**
	 * Stores the widgets' contents to the entity.
	 */
	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		emailTemplate.setName(templateName.getText());
		emailTemplate.setLanguage(languageCombo.getLanguageCode());
		// Don't set the event, since it is immutable anyhow.
		emailTemplate.setEvaluationType(getEmailTemplateEvaluationType());
		emailTemplate.setSendOnlyWithAttachment(sendOnlyWithDynamicAttachmentButton.getSelection());
	}


	/**
	 * Show the entity's properties to the widgets
	 */
	public void syncWidgetsToEntity(EmailTemplate emailTemplate) {
		sendOnlyWithDynamicAttachmentButton.setSelection(emailTemplate.isSendOnlyWithAttachment());
		templateName.setText(StringHelper.avoidNull(emailTemplate.getName()));

		// Show language (null value is allowed and leads to empty combo)
		languageCombo.setLanguageCode(emailTemplate.getLanguage());

		// Show event (null value is allowed, uneditable text widget stays always empty in this case)
		if (emailTemplate.getEventPK() != null) {
			try {
				EventVO eventVO = EventModel.getInstance().getEventVO(emailTemplate.getEventPK());
				eventLabel.setText(ParticipantLabel.Event.getString());
				event.setText(eventVO.getName(Locale.getDefault()));
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		else {
			eventLabel.setText(EmailI18N.GenericTemplate);
		}

		if (emailTemplate.getEvaluationType() != null) {
			typeCombo.setEntity( emailTemplate.getEvaluationType() );
		}
		else {
			typeCombo.setEntity(EmailTemplateEvaluationType.Groovy);
		}
	}


	public void addLanguageModifyListener(ModifyListener modifyListener) {
		languageCombo.addModifyListener(modifyListener);
	}


	public void removeLanguageModifyListener(ModifyListener modifyListener) {
		languageCombo.removeModifyListener(modifyListener);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	// *
	// * Synching and Modifying
	// **************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	/**
	 * When the selected language changes, the {@link EmailTemplateEditor} needs to ask for the new language and push it
	 * in the {@link ScriptContext}, since the evaluated strings that are shown in the {@link VariablesTableComposite}
	 * may have to be updated (eg formatted dates).
	 */
	public Language getLanguage() {
		return languageCombo.getEntity();
	}


	public EmailTemplateEvaluationType getEmailTemplateEvaluationType() {
		return typeCombo.getEntity();
	}


	public void setNotForEvent() {
		sendOnlyWithDynamicAttachmentButton.setEnabled(false);
	}

}
