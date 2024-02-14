package de.regasus.users.group.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVO;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.DecorationController;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.users.UserGroupModel;
import de.regasus.users.UsersI18N;
import de.regasus.users.common.AccessControlEntryTableComposite;
import de.regasus.users.ui.Activator;

public class UserGroupEditor extends AbstractEditor<UserGroupEditorInput> implements
	IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "UserGroupEditor";

	// the entity
	private UserGroupVO userGroupVO;

	// pseudo-entity - data which belong to that entity
	private List<AccessControlEntryCVO> aceCVOs;

	// the model
	private UserGroupModel userGroupModel;

	// **************************************************************************
	// * Widgets
	// *

	private Text idText;

	private Text descriptionText;

	private DecorationController decorationController = new DecorationController();

	private AccessControlEntryTableComposite accessControlEntryTableComposite;

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		String key = editorInput.getKey();

		// get models
		userGroupModel = UserGroupModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			userGroupVO = userGroupModel.getUserGroupVO(key);

			// register at model
			userGroupModel.addListener(this, key);
		}
		else {
			// create empty entity
			userGroupVO = new UserGroupVO();
		}
	}


	@Override
	public void dispose() {
		if (userGroupModel != null && userGroupVO.getPK() != null) {
			try {
				userGroupModel.removeListener(this, userGroupVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(UserGroupVO userGroupVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		userGroupVO = userGroupVO.clone();
		}
		this.userGroupVO = userGroupVO;

		accessControlEntryTableComposite.setOwner(userGroupVO.getGroupID());
		accessControlEntryTableComposite.setEditor(this);

		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return AccountLabel.Group.getString();
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout());

			Group groupGroup = new Group(mainComposite, SWT.NONE);
			groupGroup.setLayout(new GridLayout(2, false));
			groupGroup.setText(AccountLabel.Group.getString());
			groupGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			// UserAccountVO-specific attributes
			{
				Label label = new Label(groupGroup, SWT.RIGHT);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(AccountLabel.Group.getString());

				idText = new Text(groupGroup, SWT.BORDER);
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
				layoutData.horizontalIndent = 5;
				idText.setLayoutData(layoutData);

				decorationController.add(label, idText);
			}
			{
				Label label = new Label(groupGroup, SWT.RIGHT);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(UtilI18N.Description);

				descriptionText = new Text(groupGroup, SWT.BORDER);
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
				layoutData.horizontalIndent = 5;
				descriptionText.setLayoutData(layoutData);
			}

			accessControlEntryTableComposite = new AccessControlEntryTableComposite(mainComposite, SWT.NONE);
			accessControlEntryTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			// sync widgets and groups to the entity
			setEntity(userGroupVO);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);
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

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */

				userGroupVO = userGroupModel.create(userGroupVO);
				userGroupModel.setACLCVOsForUserGroup(userGroupVO, aceCVOs);

				// observe the CountryModel
				userGroupModel.addListener(this, userGroupVO.getPK());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(userGroupVO.getPK());

				// set new entity
				setEntity(userGroupVO);
			}
			else {
				/* Save the entity.
				 * On success setEntity will be called indirectly in dataChange(),
				 * else an Exception will be thrown.
				 * The result of update() must not be assigned to userGroupVO,
				 * because this will happen in setEntity() and there it may be cloned!
				 * Assigning userGroupVO here would overwrite the cloned value with
				 * the one from the model. Therefore we would have inconsistent data!
				 */
				userGroupModel.setACLCVOsForUserGroup(userGroupVO, aceCVOs);
				userGroupModel.update(userGroupVO);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			decorationController.enableErrors();
		}

		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (userGroupVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {


						idText.setText(StringHelper.avoidNull(userGroupVO.getGroupID()));
						idText.setEnabled(isNew());

						descriptionText.setText(StringHelper.avoidNull(userGroupVO.getDescription()));

						aceCVOs = userGroupModel.getACLCVOsForUserGroup(userGroupVO);
						aceCVOs = CollectionsHelper.createArrayList(aceCVOs);
						accessControlEntryTableComposite.setAccessControlEntryCVOs(aceCVOs);

						// refresh the EditorInput
						String name = getName();

						setPartName(name);
						editorInput.setName(name);
						editorInput.setToolTipText(getToolTipText());

						firePropertyChange(PROP_TITLE);

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
		if (userGroupVO != null) {
			userGroupVO.setGroupID(StringHelper.trim(idText.getText()));
			userGroupVO.setDescription(StringHelper.trim(descriptionText.getText()));
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		idText.addModifyListener(listener);
		descriptionText.addModifyListener(listener);
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == userGroupModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else {
					userGroupVO = userGroupModel.getUserGroupVO(userGroupVO.getPK());
					if (userGroupVO != null) {
						setEntity(userGroupVO);
					}
					else {
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
	public void refresh() throws Exception {
		if (userGroupVO != null && userGroupVO.getPK() != null) {
			userGroupModel.refresh(userGroupVO.getPK());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				userGroupVO = userGroupModel.getUserGroupVO(userGroupVO.getPK());
				if (userGroupVO != null) {
					setEntity(userGroupVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (! isNew() ) {
			name = userGroupVO.getGroupID();
		}
		if (StringHelper.isEmpty(name)) {
			name = UsersI18N.UserGroup_Editor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return UsersI18N.UserGroup_Editor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return userGroupVO == null || userGroupVO.getPK() == null;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			AccountLabel.Group.getString(),
			UtilI18N.EditDateTime,
		};

		// the values of the info dialog

		final String[] values = {
			userGroupVO.getGroupID(),
			formatHelper.formatDateTime(userGroupVO.getEditTime()),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			AccountLabel.User.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


}
