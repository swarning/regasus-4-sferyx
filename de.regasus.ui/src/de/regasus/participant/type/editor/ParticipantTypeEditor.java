package de.regasus.participant.type.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.apache.commons.codec.EncoderException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.Vigenere;
import com.lambdalogic.util.Vigenere2;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;

public class ParticipantTypeEditor
extends AbstractEditor<ParticipantTypeEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "ParticipantTypeEditor";

	// the entity
	private ParticipantType participantType;

	// the model
	private ParticipantTypeModel participantTypeModel;

	// widgets
	private I18NText names;
	private Text externalIdText;
	private Text categoryText;
	private Button proofRequiredButton;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		participantTypeModel = ParticipantTypeModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			participantType = participantTypeModel.getParticipantType(key);

			// register at model
			participantTypeModel.addListener(this, key);
		}
		else {
			// create empty entity
			participantType = new ParticipantType();
		}
	}


	@Override
	public void dispose() {
		if (participantTypeModel != null && participantType.getId() != null) {
			try {
				participantTypeModel.removeListener(this, participantType.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ParticipantType participantType) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		participantType = participantType.clone();
		}

		this.participantType = participantType;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return Participant.PARTICIPANT_TYPE.getString();
	}


//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ParticipantTypeEditor_InfoButtonToolTip;
//	}


	/**
	 * Create contents of the editor part
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout(2, false));


			buildName(mainComposite);
			buildExternalId(mainComposite);
			buildCategory(mainComposite);
			buildProofRequired(mainComposite);


			// sync widgets and groups to the entity
			setEntity(participantType);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void buildName(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText( ParticipantType.NAME.getString() );
		SWTHelper.makeBold(label);

		names = new I18NText(parent, SWT.NONE, LanguageProvider.getInstance(), true);
		names.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
	}


	private void buildExternalId(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText( ParticipantType.EXTERNAL_ID.getString() );

		externalIdText = new Text(parent, SWT.BORDER);
		externalIdText.setTextLimit(ParticipantType.EXTERNAL_ID.getMaxLength());
		externalIdText.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
	}


	private void buildCategory(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText( ParticipantType.CATEGORY.getString() );

		categoryText = new Text(parent, SWT.BORDER);
		categoryText.setTextLimit(ParticipantType.CATEGORY.getMaxLength());
		categoryText.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
	}


	private void buildProofRequired(Composite parent) {
		new Label(parent, SWT.NONE);

		proofRequiredButton = new Button(parent, SWT.CHECK);
		proofRequiredButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		proofRequiredButton.setText( ParticipantType.PROOF_REQUIRED.getString() );
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/* Entity mit den Widgets synchronisieren.
			 * Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				participantType = participantTypeModel.create(participantType);

				// observe the ParticipantTypeModel
				participantTypeModel.addListener(this, participantType.getId());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(participantType.getId());

				// set new entity
				setEntity(participantType);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				participantTypeModel.update(participantType);

				// setEntity will be calles indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException are handled separately in order to be able to output the original error message.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateParticipantTypeErrorMessage;
			}
			else {
				msg = I18N.EditParticipantTypeErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (participantType != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						names.setLanguageString( participantType.getName() );
						externalIdText.setText( avoidNull(participantType.getExternalId()) );
						categoryText.setText( avoidNull(participantType.getCategory()) );
						proofRequiredButton.setSelection( participantType.isProofRequired() );

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						setDirty(false);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncEntityToWidgets() {
		if (participantType != null) {
			participantType.setName( names.getLanguageString() );
			participantType.setExternalId( externalIdText.getText() );
			participantType.setCategory( categoryText.getText() );
			participantType.setProofRequired( proofRequiredButton.getSelection() );
		}
	}


	private void addModifyListener(ModifyListener listener) {
		names.addModifyListener(listener);
		externalIdText.addModifyListener(listener);
		categoryText.addModifyListener(listener);
		proofRequiredButton.addSelectionListener( new ModifyListenerAdapter(listener) );
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			ContactLabel.VigenereCode.getString(),
			ContactLabel.VigenereCodeAsHex.getString(),
			ContactLabel.Vigenere2Code.getString(),
			ContactLabel.Vigenere2CodeAsHex.getString(),
			ContactLabel.Vigenere2CodeUrlSafe.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			UtilI18N.Name
		};

		Long participantTypeId = participantType.getId();
		String vigenereCode = participantTypeId == null ? "" : Vigenere.toVigenereString(participantTypeId);
		String vigenereCodeHex = participantTypeId == null ? "" : Vigenere.toVigenereCodeHex(participantTypeId);
		String vigenere2Code = participantTypeId == null ? "" : Vigenere2.toVigenereString(participantTypeId);
		String vigenere2CodeHex = participantTypeId == null ? "" : Vigenere2.toVigenereCodeHex(participantTypeId);
		String vigenere2CodeUrlSafe = "";
		try {
			 if (participantTypeId != null) {
				 vigenere2CodeUrlSafe = Vigenere2.toVigenereCodeUrlSafe(participantTypeId);
			 }
		}
		catch (EncoderException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		// the values of the info dialog
		final String[] values = {
			String.valueOf(participantTypeId),
			vigenereCode,
			vigenereCodeHex,
			vigenere2Code,
			vigenere2CodeHex,
			vigenere2CodeUrlSafe,
			participantType.getNewTime().getString(),
			participantType.getNewDisplayUserStr(),
			participantType.getEditTime().getString(),
			participantType.getEditDisplayUserStr(),
			getName()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			Participant.PARTICIPANT_TYPE.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == participantTypeModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (participantType != null) {
					participantType = participantTypeModel.getParticipantType(participantType.getId());
					if (participantType != null) {
						setEntity(participantType);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (participantType != null && participantType.getName() != null) {
			name = participantType.getName().getString();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.ParticipantTypeEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ParticipantTypeEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return participantType.getId() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (participantType != null && participantType.getId() != null) {
			participantTypeModel.refresh(participantType.getId());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				participantType = participantTypeModel.getParticipantType(participantType.getId());
				if (participantType != null) {
					setEntity(participantType);
				}
			}
		}
	}

}
