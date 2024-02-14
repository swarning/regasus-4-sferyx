package de.regasus.portal.dialog;

import static com.lambdalogic.util.StringHelper.trim;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.portal.CopyPortalMissingCustomFieldBehaviour;
import de.regasus.portal.CopyPortalMissingParticipantTypeBehaviour;
import de.regasus.portal.CopyPortalMissingProgrammePointBehaviour;
import de.regasus.portal.Portal;


/**
 * {@link WizardPage} to query the following parameters:
 * - String newPortalMnemonic
 * - boolean copyPhotos
 * - CopyPortalMissingParticipantTypeBehaviour missingParticipantTypeBehaviour
 * - CopyPortalMissingCustomFieldBehaviour missingCustomFieldBehaviour
 * - CopyPortalMissingProgrammePointBehaviour missingProgrammePointBehaviour
 */
public class CopyPortalSettingsWizardPage extends WizardPage {

	public static final String NAME = "CopyPortalSettingsWizardPage";

	// widgets
	private Text mnemonicText;
	private Button copyPhotosButton;

	private Group missingParticipantTypeBehaviourGroup;
	private Button missingParticipantTypeBehaviourErrorButton;
	private Button missingParticipantTypeBehaviourIgnoreButton;
	private Button missingParticipantTypeBehaviourCopyButton;

	private Group missingCustomFieldBehaviourGroup;
	private Button missingCustomFieldBehaviourErrorButton;
	private Button missingCustomFieldBehaviourIgnoreButton;
	private Button missingCustomFieldBehaviourCopyButton;

	private Group missingProgrammePointBehaviourGroup;
	private Button missingProgrammePointBehaviourErrorButton;
	private Button missingProgrammePointBehaviourIgnoreButton;


	public CopyPortalSettingsWizardPage() {
		super(NAME);

		setTitle(UtilI18N.Settings);
		setMessage(I18N.CopyPortalSettingsWizardPage_Message);
	}


	@Override
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));

		setControl(mainComposite);

		// mnemonic
		{
    		Label label = new Label(mainComposite, SWT.NONE);
    		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
    		SWTHelper.makeBold(label);
    		label.setText( Portal.MNEMONIC.getString() );
    		label.setToolTipText(I18N.CopyPortalSettingsWizardPage_MnemonicToolTip);

    		mnemonicText = new Text(mainComposite, SWT.BORDER);
    		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(mnemonicText);
    		SWTHelper.makeBold(mnemonicText);
    		mnemonicText.setTextLimit( Portal.MNEMONIC.getMaxLength() );

    		mnemonicText.addModifyListener( new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					setPageComplete(isPageComplete());
				}
			} );
		}

		// copyPhotos
		{
			new Label(mainComposite, SWT.NONE); // placeholder

			copyPhotosButton = new Button(mainComposite, SWT.CHECK);
			copyPhotosButton.setText(I18N.CopyPortalSettingsWizardPage_CopyPhotosButtonText);
		}

		// CopyPortalMissingParticipantTypeBehaviour
		{
			missingParticipantTypeBehaviourGroup = new Group(mainComposite, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(missingParticipantTypeBehaviourGroup);
			missingParticipantTypeBehaviourGroup.setLayout( new RowLayout(SWT.HORIZONTAL) );
			missingParticipantTypeBehaviourGroup.setText(I18N.CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourGroupText);

			missingParticipantTypeBehaviourErrorButton = new Button(missingParticipantTypeBehaviourGroup, SWT.RADIO);
			missingParticipantTypeBehaviourErrorButton.setText(UtilI18N.Error);
			missingParticipantTypeBehaviourErrorButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourErrorToolTip);

			// set default value
			missingParticipantTypeBehaviourErrorButton.setSelection(true);

			missingParticipantTypeBehaviourIgnoreButton = new Button(missingParticipantTypeBehaviourGroup, SWT.RADIO);
			missingParticipantTypeBehaviourIgnoreButton.setText(UtilI18N.Ignore);
			missingParticipantTypeBehaviourIgnoreButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourIgnoreToolTip);

			missingParticipantTypeBehaviourCopyButton = new Button(missingParticipantTypeBehaviourGroup, SWT.RADIO);
			missingParticipantTypeBehaviourCopyButton.setText(UtilI18N.CopyVerb);
			missingParticipantTypeBehaviourCopyButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourCopyToolTip);
		}

		// CopyPortalMissingCustomFieldBehaviour
		{
			missingCustomFieldBehaviourGroup = new Group(mainComposite, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(missingCustomFieldBehaviourGroup);
			missingCustomFieldBehaviourGroup.setLayout( new RowLayout(SWT.HORIZONTAL) );
			missingCustomFieldBehaviourGroup.setText(I18N.CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourGroupText);

			missingCustomFieldBehaviourErrorButton = new Button(missingCustomFieldBehaviourGroup, SWT.RADIO);
			missingCustomFieldBehaviourErrorButton.setText(UtilI18N.Error);
			missingCustomFieldBehaviourErrorButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourErrorToolTip);

			// set default value
			missingCustomFieldBehaviourErrorButton.setSelection(true);

			missingCustomFieldBehaviourIgnoreButton = new Button(missingCustomFieldBehaviourGroup, SWT.RADIO);
			missingCustomFieldBehaviourIgnoreButton.setText(UtilI18N.Ignore);
			missingCustomFieldBehaviourIgnoreButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourIgnoreToolTip);

			missingCustomFieldBehaviourCopyButton = new Button(missingCustomFieldBehaviourGroup, SWT.RADIO);
			missingCustomFieldBehaviourCopyButton.setText(UtilI18N.CopyVerb);
			missingCustomFieldBehaviourCopyButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourCopyToolTip);
		}

		// CopyPortalMissingProgrammePointBehaviour
		{
			missingProgrammePointBehaviourGroup = new Group(mainComposite, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(missingProgrammePointBehaviourGroup);
			missingProgrammePointBehaviourGroup.setLayout( new RowLayout(SWT.HORIZONTAL) );
			missingProgrammePointBehaviourGroup.setText(I18N.CopyPortalSettingsWizardPage_MissingProgrammePointBehaviourGroupText);

			missingProgrammePointBehaviourErrorButton = new Button(missingProgrammePointBehaviourGroup, SWT.RADIO);
			missingProgrammePointBehaviourErrorButton.setText(UtilI18N.Error);
			missingProgrammePointBehaviourErrorButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingProgrammePointBehaviourErrorToolTip);

			// set default value
			missingProgrammePointBehaviourErrorButton.setSelection(true);

			missingProgrammePointBehaviourIgnoreButton = new Button(missingProgrammePointBehaviourGroup, SWT.RADIO);
			missingProgrammePointBehaviourIgnoreButton.setText(UtilI18N.Ignore);
			missingProgrammePointBehaviourIgnoreButton.setToolTipText(I18N.CopyPortalSettingsWizardPage_MissingProgrammePointBehaviourIgnoreToolTip);
		}

		setPageComplete( isPageComplete() );
	}


	@Override
	public boolean isPageComplete() {
		return getMnemonic() != null;
	}


	public void showWidgetsForDifferentEvents(boolean visible) {
		missingParticipantTypeBehaviourGroup.setVisible(visible);
		missingCustomFieldBehaviourGroup.setVisible(visible);
		missingProgrammePointBehaviourGroup.setVisible(visible);
	}


	public String getMnemonic() {
		return trim( mnemonicText.getText() );
	}


	public boolean isCopyPhotos() {
		return copyPhotosButton.getSelection();
	}


	public CopyPortalMissingParticipantTypeBehaviour getMissingParticipantTypeBehaviour() {
		CopyPortalMissingParticipantTypeBehaviour result = CopyPortalMissingParticipantTypeBehaviour.ERROR;
		if ( missingParticipantTypeBehaviourIgnoreButton.getSelection() ) {
			result = CopyPortalMissingParticipantTypeBehaviour.IGNORE;
		}
		else if ( missingParticipantTypeBehaviourCopyButton.getSelection() ) {
			result = CopyPortalMissingParticipantTypeBehaviour.COPY;
		}
		return result;
	}


	public CopyPortalMissingCustomFieldBehaviour getMissingCustomFieldBehaviour() {
		CopyPortalMissingCustomFieldBehaviour result = CopyPortalMissingCustomFieldBehaviour.ERROR;
		if ( missingCustomFieldBehaviourIgnoreButton.getSelection() ) {
			result = CopyPortalMissingCustomFieldBehaviour.IGNORE;
		}
		else if ( missingCustomFieldBehaviourCopyButton.getSelection() ) {
			result = CopyPortalMissingCustomFieldBehaviour.COPY;
		}
		return result;
	}


	public CopyPortalMissingProgrammePointBehaviour getMissingProgrammePointBehaviour() {
		CopyPortalMissingProgrammePointBehaviour result = CopyPortalMissingProgrammePointBehaviour.ERROR;
		if ( missingProgrammePointBehaviourIgnoreButton.getSelection() ) {
			result = CopyPortalMissingProgrammePointBehaviour.IGNORE;
		}
		return result;
	}

}
