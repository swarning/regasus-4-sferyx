package de.regasus.users.user.editor;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.UserAccountCVO;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.UserGroupModel;
import de.regasus.users.UsersAdministrationHelper;
import de.regasus.users.UsersI18N;
import de.regasus.users.ui.Activator;


public class UserGroupsGroup extends Group {

	// The Model
	private UserGroupModel userGroupModel;

	// the entity
	private UserAccountCVO userAccountCVO;

	private java.util.List<UserGroupVO> userGroupVOs;

	// Widgets
	private List groupsList;

	private Button addToGroupsButton;

	private Button removeFromGroupsButton;

	// Modifying
	private ModifySupport modifySupport = new ModifySupport(this);


	public UserGroupsGroup(Composite parent, int style) {
		super(parent, style);

		// init UserGroupModel
		userGroupModel = UserGroupModel.getInstance();

		setText(AccountLabel.Groups.getString());

		setLayout(new GridLayout(2, false));

		groupsList = new List(this, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		groupsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		groupsList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFromGroupsButton.setEnabled(groupsList.getSelectionCount() > 0);
			}
		});

		addToGroupsButton = new Button(this, SWT.PUSH);
		addToGroupsButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		addToGroupsButton.setText(UtilI18N.Add + UtilI18N.Ellipsis);
		addToGroupsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addToGroups();
			}
		});

		removeFromGroupsButton = new Button(this, SWT.PUSH);
		removeFromGroupsButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeFromGroupsButton.setText(UtilI18N.Remove);
		removeFromGroupsButton.setEnabled(false);
		removeFromGroupsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFromGroups();
			}
		});
	}


	protected void removeFromGroups() {
		try {
			String[] groupIDs = groupsList.getSelection();

			boolean somethingWasRemoved = false;
			for (String groupID : groupIDs) {
				UserGroupVO selectedGroupVO = userGroupModel.getUserGroupVO(groupID);
				somethingWasRemoved |= userGroupVOs.remove(selectedGroupVO);
			}

			if (somethingWasRemoved) {
				syncWidgetsToEntity();
				modifySupport.fire();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	protected void addToGroups() {
		try {
			String title = UsersI18N.AssignToGroup;
			String message = UsersI18N.SelectGroupForAssignment;

			java.util.List<UserGroupVO> allUserGroupVOs = createArrayList(userGroupModel.getAllUserGroupVOs());
			allUserGroupVOs.removeAll(userGroupVOs);

			String[] groupsToAddIDs = UsersAdministrationHelper.selectUserGroup(getShell(), title, message, allUserGroupVOs);

			boolean somethingWasAdded = false;
			for(String groupToAddID : groupsToAddIDs) {
				UserGroupVO userGroupToAddVO = userGroupModel.getUserGroupVO(groupToAddID);

				if (! userGroupVOs.contains(userGroupToAddVO)) {
					userGroupVOs.add(userGroupToAddVO);
					somethingWasAdded = true;
				}
			}
			if (somethingWasAdded) {
				syncWidgetsToEntity();
				modifySupport.fire();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public void syncWidgetsToEntity() {
		if (userAccountCVO != null) {
			userGroupVOs = userAccountCVO.getUserGroupVOs();
			if (userGroupVOs != null) {
				java.util.List<String> groupIDs = AbstractVO.getPKs(userGroupVOs);
				Collections.sort(groupIDs, Collator.getInstance());
				groupsList.setItems(groupIDs.toArray(new String[groupIDs.size()]));
			}
		}
	}


	public void syncEntityToWidgets() {
		if (userAccountCVO != null) {

			try {
				userGroupVOs = new ArrayList<>();

				for (String groupID : groupsList.getItems()) {
					userGroupVOs.add(userGroupModel.getUserGroupVO(groupID));
				}
				userAccountCVO.setUserGroupVOs(userGroupVOs);

			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void setUserAccountCVO(UserAccountCVO userAccountCVO) {
		this.userAccountCVO = userAccountCVO;
		this.userGroupVOs = userAccountCVO.getUserGroupVOs();
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

}
