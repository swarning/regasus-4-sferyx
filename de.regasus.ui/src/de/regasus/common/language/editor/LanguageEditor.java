package de.regasus.common.language.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;

public class LanguageEditor
extends AbstractEditor<LanguageEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "LanguageEditor";

	// the entity
	private Language language;

	// the model
	private LanguageModel languageModel;

	// **************************************************************************
	// * Widgets
	// *

	private Text code;

	private I18NText names;

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		String key = editorInput.getKey();

		// get model
		languageModel = LanguageModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			language = languageModel.getLanguage(key);

			// register at model
			languageModel.addListener(this, key);
		}
		else {
			// create empty entity
			language = new Language();
		}
	}


	@Override
	public void dispose() {
		if (languageModel != null && language.getId() != null) {
			try {
				languageModel.removeListener(this, language.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(Language language) {

		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		language = language.clone();
		}

		this.language = language;


		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return AbstractPerson.LANGUAGE_CODE.getLabel();
	}


	//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ProgrammePointEditor_InfoButtonToolTip;
//	}


	/**
	 * Create contents of the editor part
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = SWTHelper.createScrolledContentComposite(parent);
			mainComposite.setLayout(new GridLayout(2, false));

			final Label languageCodeLabel = new Label(mainComposite, SWT.NONE);
			languageCodeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			languageCodeLabel.setText( AbstractPerson.LANGUAGE_CODE.getString() );
			SWTHelper.makeBold(languageCodeLabel);

			code = new Text(mainComposite, SWT.BORDER);
			// TODO: nach Umstellung auf Entity textLimit aus Annotation nehmen (siehe CommunicationGroup), Voraussetzung: Sprachen erhalten eigenes Entity
			code.setTextLimit(2);
			final GridData gd_languageCode = new GridData(SWT.FILL, SWT.CENTER, true, false);
			code.setLayoutData(gd_languageCode);
			SWTHelper.makeBold(code);

			final Label namesLabel = new Label(mainComposite, SWT.NONE);
			namesLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			namesLabel.setText(KernelLabel.Name.getString());
			SWTHelper.makeBold(namesLabel);

			names = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance(), true);
			final GridData gd_names = new GridData(SWT.FILL, SWT.CENTER, true, false);
			names.setLayoutData(gd_names);


			// sync widgets and groups to the entity
			setEntity(language);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);

			SWTHelper.refreshSuperiorScrollbar(mainComposite);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
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
				language = languageModel.create(language);

				// observe the LanguageModel
				languageModel.addListener(this, language.getId());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(language.getId());

				// set new entity
				setEntity(language);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				languageModel.update(language);

				// setEntity will be calles indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateLanguageErrorMessage;
			}
			else {
				msg = I18N.EditLanguageErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (language != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						code.setText(StringHelper.avoidNull(language.getId()));
						code.setEnabled(isNew());

						names.setLanguageString(language.getName());

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
		if (language != null) {
			language.setId( StringHelper.trim(code.getText()) );
			language.setName( names.getLanguageString() );
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		code.addModifyListener(listener);
		names.addModifyListener(listener);
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.Name,
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			getName(),
			String.valueOf(language.getId()),
			language.getNewTime().getString(),
			language.getNewDisplayUserStr(),
			language.getEditTime().getString(),
			language.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			AbstractPerson.LANGUAGE_CODE.getLabel() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == languageModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (language != null) {
					language = languageModel.getLanguage( language.getId() );
					if (language != null) {
						setEntity(language);
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

		if (language != null && language.getName() != null) {
			name = language.getName().getString();
		}

		if (StringHelper.isEmpty(name)) {
			name = I18N.LanguageEditor_NewName;
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.LanguageEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return language.getId() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (language != null && language.getId() != null) {
			languageModel.refresh(language.getId());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				language = languageModel.getLanguage( language.getId() );
				if (language != null) {
					setEntity(language);
				}
			}
		}
	}

}
