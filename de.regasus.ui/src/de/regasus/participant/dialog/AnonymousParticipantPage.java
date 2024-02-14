/**
 *
 */
package de.regasus.participant.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.I18N;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.participant.type.combo.ParticipantTypeCombo;


public class AnonymousParticipantPage extends WizardPage {

	public static final String NAME = "AnonymousParticipantPage";

	public static final String DEFAULT_LAST_NAME = "No Name";

	private Participant participantTemplate;

	private NullableSpinner countSpinner;

	private Text lastNameText;

	private ParticipantTypeCombo participantTypeCombo;

	private LanguageCombo languageCombo;


	protected AnonymousParticipantPage(Participant participantTemplate) {
		super(NAME);
		setTitle(I18N.AnonymousParticipantPage_Title);

		this.participantTemplate = participantTemplate;
	}


	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		setControl(container);

		Group anonymousParticipantGroup = new Group(container, SWT.NONE);
		anonymousParticipantGroup.setText(I18N.AnonymousParticipantPage_AnonymousParticipantGroupText);
		anonymousParticipantGroup.setLayout(new GridLayout(2, false));
		anonymousParticipantGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		try {
			Label participantQuantityLabel = new Label(anonymousParticipantGroup, SWT.NONE);
			participantQuantityLabel.setText(I18N.AnonymousParticipantPage_ParticipantQuantityLabel);

			countSpinner = new NullableSpinner(anonymousParticipantGroup, SWT.NONE);
			countSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			countSpinner.setMinimum(1);
			countSpinner.setValue(1);

			Label lastNameLabel = new Label(anonymousParticipantGroup, SWT.NONE);
			lastNameLabel.setText(I18N.AnonymousParticipantPage_LastnameLabel);

			lastNameText = new Text(anonymousParticipantGroup, SWT.BORDER);
			lastNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			lastNameText.setText(DEFAULT_LAST_NAME);
			participantTemplate.setLastName(DEFAULT_LAST_NAME);
			lastNameText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					checkPageComplete();

					// set lastName for generating address label on AddressPage
					participantTemplate.setLastName(lastNameText.getText());
				}
			});

			Label participantTypeLabel = new Label(anonymousParticipantGroup, SWT.NONE);
			participantTypeLabel.setText(I18N.AnonymousParticipantPage_ParticipantTypeLabel);

			participantTypeCombo = new ParticipantTypeCombo(anonymousParticipantGroup, SWT.READ_ONLY);
			participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			participantTypeCombo.setEventID(CreateAnonymousParticipantWizard.getEventPK());
			participantTypeCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					checkPageComplete();
				}
			});

			Label languageLabel = new Label(anonymousParticipantGroup, SWT.NONE);
			languageLabel.setText(I18N.AnonymousParticipantPage_LanguageLabel);

			languageCombo = new LanguageCombo(anonymousParticipantGroup, SWT.READ_ONLY);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

	}

	@Override
	public boolean isPageComplete() {
		boolean complete =
			lastNameText.getText().length() > 0 &&
			participantTypeCombo.getParticipantTypePK() != null;

		return complete;
	}


	private void checkPageComplete() {
		setPageComplete(isPageComplete());
	}


	public int getCount() {
		int count = countSpinner.getValueAsInteger();
		return count;
	}


	public String getLastName() {
		String lastName = lastNameText.getText();
		return lastName;
	}


	public Long getParticipantTypePK() {
		Long participantTypeID = participantTypeCombo.getParticipantTypePK();
		return participantTypeID;
	}


	public String getLanguage() {
		String language = languageCombo.getLanguageCode();
		return language;
	}

}
