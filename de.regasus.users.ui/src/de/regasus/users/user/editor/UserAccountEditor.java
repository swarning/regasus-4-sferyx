package de.regasus.users.user.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVO;
import com.lambdalogic.messeinfo.account.data.UserAccountCVO;
import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.users.UserAccountModel;
import de.regasus.users.UserGroupModel;
import de.regasus.users.UsersI18N;
import de.regasus.users.common.AccessControlEntryTableComposite;
import de.regasus.users.ui.Activator;

public class UserAccountEditor extends AbstractEditor<UserAccountEditorInput> implements
	IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "UserAccountEditor";

	// the entity
	private UserAccountCVO userAccountCVO;

	// pseudo-entity - data which belong to that entity
	private List<AccessControlEntryCVO> aceCVOs;

	// the model
	private UserAccountModel userAccountModel;

	private boolean ignoreCacheModelEvents = false;


	// **************************************************************************
	// * Groups / Widgets
	// *

	private UserAccountGroup userAccountGroup;

	private UserGroupsGroup userGroupsGroup;

	private AccessControlEntryTableComposite accessControlEntryTableComposite;


	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {

		/* Load all User Groups in advance to avoid multiple single load operations later.
		 * Do it before observing UserAccountModel, because loading the User Groups causes CacheModel Events
		 * from UserAccountModel which cannot be handled at this time.
		 */
		UserGroupModel.getInstance().getAllUserGroupVOs();

		// handle EditorInput
		Long key = editorInput.getKey();

		// get models
		userAccountModel = UserAccountModel.getInstance();

		if (key != null) {
			// get entity
			userAccountCVO = userAccountModel.getUserAccountCVO(key);

			// register at model
			userAccountModel.addListener(this, key);
		}
		else {
			// create empty entity
			userAccountCVO = new UserAccountCVO();
			userAccountCVO.setVO(new UserAccountVO());
		}
	}


	@Override
	public void dispose() {
		if (userAccountModel != null && userAccountCVO != null && userAccountCVO.getPK() != null) {
			try {
				userAccountModel.removeListener(this, userAccountCVO.getPK());
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		super.dispose();
	}


	@Override
	public void setFocus() {
		userAccountGroup.setFocus();
	}


	protected void setEntity(UserAccountCVO userAccountCVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		userAccountCVO = userAccountCVO.clone();
		}
		this.userAccountCVO = userAccountCVO;

		// Clone also the list of userGroups since we edit it here

		List<UserGroupVO> originalUserGroupVOs = this.userAccountCVO.getUserGroupVOs();
		List<UserGroupVO> clonedUserGroupVOs = null;
		if (originalUserGroupVOs == null || originalUserGroupVOs.isEmpty()) {
			clonedUserGroupVOs = new ArrayList<>();
		}
		else {
			clonedUserGroupVOs = new ArrayList<>(originalUserGroupVOs);
		}

		this.userAccountCVO.setUserGroupVOs(clonedUserGroupVOs);

		userAccountGroup.setUserAccountCVO(this.userAccountCVO);
		userGroupsGroup.setUserAccountCVO(this.userAccountCVO);

		accessControlEntryTableComposite.setOwner(this.userAccountCVO.getVO().getUserID());
		accessControlEntryTableComposite.setEditor(this);

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return AccountLabel.User.getString();
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

			mainComposite.setLayout(new GridLayout(2, false));

			// UserAccountVO-specific attributes

			userAccountGroup = new UserAccountGroup(mainComposite, SWT.NONE);
			userAccountGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			userGroupsGroup = new UserGroupsGroup(mainComposite, SWT.NONE);
			userGroupsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			accessControlEntryTableComposite = new AccessControlEntryTableComposite(mainComposite, SWT.NONE);
			accessControlEntryTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			// sync widgets and groups to the entity
			setEntity(userAccountCVO);

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

				userAccountCVO = userAccountModel.create(userAccountCVO);
				userAccountModel.setACLCVOsForUserAccount(userAccountCVO, aceCVOs);

				userAccountModel.addListener(this, userAccountCVO.getPK());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(userAccountCVO.getPK());

				// set new entity
				setEntity(userAccountCVO);
			}
			else {
				/* Save the entity.
				 * On success setEntity will be called indirectly in dataChange(),
				 * else an Exception will be thrown.
				 * The result of update() must not be assigned to userAccountCVO,
				 * because this will happen in setEntity() and there it may be cloned!
				 * Assigning userAccountCVO here would overwrite the cloned value with
				 * the one from the model. Therefore we would have inconsistent data!
				 */
				userAccountModel.setACLCVOsForUserAccount(userAccountCVO, aceCVOs);
				userAccountModel.update(userAccountCVO);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			userAccountGroup.enableErrors();
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (userAccountCVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						userAccountGroup.syncWidgetsToEntity();
						userGroupsGroup.syncWidgetsToEntity();

						aceCVOs = userAccountModel.getACLCVOsForUserAccount(userAccountCVO);
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


	private void syncEntityToWidgets() throws ErrorMessageException {
		try {
			// ignore CacheModel Events, because they would cause the loss of edited data
			// CacheModel Events might appear when UserGroups are loaded indirectly
			ignoreCacheModelEvents = true;
			userAccountGroup.syncEntityToWidgets();
			userGroupsGroup.syncEntityToWidgets();
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		userAccountGroup.addModifyListener(listener);
		userGroupsGroup.addModifyListener(listener);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (!ignoreCacheModelEvents) {
    			if (event.getSource() == userAccountModel) {
    				if (event.getOperation() == CacheModelOperation.DELETE) {
    					closeBecauseDeletion();
    				}
    				else {
    					userAccountCVO = userAccountModel.getUserAccountCVO(userAccountCVO.getPK());
    					if (userAccountCVO != null) {
    						setEntity(userAccountCVO);
    					}
    					else {
    						closeBecauseDeletion();
    					}
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
		if (userAccountCVO != null && userAccountCVO.getPK() != null) {
			userAccountModel.refresh(userAccountCVO.getPK());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				userAccountCVO = userAccountModel.getUserAccountCVO(userAccountCVO.getPK());
				if (userAccountCVO != null) {
					setEntity(userAccountCVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (! isNew() ) {
			name = userAccountCVO.getVO().getUserID();
		}
		if (StringHelper.isEmpty(name)) {
			name = UsersI18N.UserAccount_Editor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return UsersI18N.UserAccount_Editor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return userAccountCVO == null || userAccountCVO.getPK() == null;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.EditDateTime,
			AccountLabel.UserID.getString(),
			AccountLabel.State.getString(),
			AccountLabel.FailCount.getString()
		};

		// the values of the info dialog
		UserAccountVO userAccountVO = userAccountCVO.getVO();
		final String[] values = {
			String.valueOf(userAccountVO.getPK()),
			formatHelper.formatDateTime(userAccountVO.getEditTime()),
			userAccountVO.getUserID(),
			userAccountVO.getState().getString(),
			String.valueOf(userAccountVO.getFailCount())
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
