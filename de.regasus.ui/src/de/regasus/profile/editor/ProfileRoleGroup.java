package de.regasus.profile.editor;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.profile.ProfileRoleModel;
import de.regasus.profile.role.ProfileRoleLabelProvider;
import de.regasus.profile.role.ProfileRoleLabelSorter;
import de.regasus.ui.Activator;

public class ProfileRoleGroup extends Group {

	// The Model
	private ProfileRoleModel profileRoleModel = ProfileRoleModel.getInstance();

	// the entity
	private Profile profile;

	private java.util.List<ProfileRole> profileRoles;

	// Widgets
	private ListViewer profileRoleListViewer;

	private Button addToRoleButton;

	private Button removeFromRoleButton;

	// Modifying
	private ModifySupport modifySupport = new ModifySupport(this);


	public ProfileRoleGroup(Composite parent, int style) {
		super(parent, style);

		setText(I18N.ProfileRoles);

		setLayout(new GridLayout(2, false));

		List profileRoleList = new List(this, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		profileRoleList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		profileRoleListViewer = new ListViewer(profileRoleList);
		profileRoleListViewer.setContentProvider(ArrayContentProvider.getInstance());
		profileRoleListViewer.setLabelProvider(new ProfileRoleLabelProvider());
		profileRoleListViewer.setInput(profileRoles);
		profileRoleListViewer.setSorter(new ProfileRoleLabelSorter());

		profileRoleListViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = profileRoleListViewer.getSelection();
				removeFromRoleButton.setEnabled(!selection.isEmpty());
			}
		});


		addToRoleButton = new Button(this, SWT.PUSH);
		addToRoleButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		addToRoleButton.setText(UtilI18N.Add + UtilI18N.Ellipsis);
		addToRoleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addToRoles();
			}
		});

		removeFromRoleButton = new Button(this, SWT.PUSH);
		removeFromRoleButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeFromRoleButton.setText(UtilI18N.Remove);
		removeFromRoleButton.setEnabled(false);
		removeFromRoleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFromRoles();
			}
		});
	}


	protected void removeFromRoles() {
		try {
			java.util.List<ProfileRole> selectedProfileRoles = SelectionHelper.toList(profileRoleListViewer.getSelection());

			boolean somethingWasRemoved = profileRoles.removeAll(selectedProfileRoles);

			if (somethingWasRemoved) {
				syncWidgetsToEntity();
				modifySupport.fire();
				updateButtonState();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void addToRoles() {
		try {
			java.util.List<ProfileRole> allRoles = CollectionsHelper.createArrayList(profileRoleModel.getAllProfileRoles());
			// remove roles the profile is already connected with
			if (profileRoles == null) {
				profileRoles = new ArrayList<>();
			}

			allRoles.removeAll(profileRoles);

			ProfileRole[] rolesToAdd = openRoleSelectionDialog(allRoles);

			boolean somethingWasAdded = false;

			if (rolesToAdd != null) {
    			for (ProfileRole roleToAdd : rolesToAdd) {
    				if (! profileRoles.contains(roleToAdd)) {
    					profileRoles.add(roleToAdd);
    					somethingWasAdded = true;
    				}
    			}
    			if (somethingWasAdded) {
    				syncWidgetsToEntity();
					modifySupport.fire();
    			}
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


	protected void syncWidgetsToEntity() {
		if (profile != null) {
    		SWTHelper.syncExecDisplayThread(new Runnable() {
    			@Override
				public void run() {
    				try {
    					modifySupport.setEnabled(false);

    					if (profileRoleListViewer != null) {
    						profileRoleListViewer.setInput(profileRoles);
    					}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
    				finally {
    					modifySupport.setEnabled(true);
    				}
    			}
    		});
		}
	}


	@SuppressWarnings("unchecked")
	protected void syncEntityToWidgets() {
		if (profile != null) {
			try {
				profileRoles = (java.util.List<ProfileRole>) profileRoleListViewer.getInput();

				profile.setRoles(profileRoles);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public void setProfile(Profile profile) {
		this.profile = profile;
		this.profileRoles = profile.getRoles();
		syncWidgetsToEntity();
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	private void updateButtonState() {
		removeFromRoleButton.setEnabled(CollectionsHelper.notEmpty(profileRoles));
	}


	private ProfileRole[] openRoleSelectionDialog(Collection<ProfileRole> profileRoles)
	throws Exception {
		ElementListSelectionDialog listDialog = new ElementListSelectionDialog(
			getShell(),
			new ProfileRoleLabelProvider()
		);
		listDialog.setMultipleSelection(true);
		listDialog.setTitle(I18N.AssignToRole);
		listDialog.setMessage(I18N.SelectRoleForAssignment);
		listDialog.setElements(profileRoles.toArray(new ProfileRole[0]));

		ProfileRole[] resultRoles = null;
		int code = listDialog.open();
		if (code == Window.OK) {
			Object[] result = listDialog.getResult();
			resultRoles = new ProfileRole[result.length];
			for (int i = 0; i < result.length; i++) {
				if (result[i] instanceof ProfileRole) {
					ProfileRole profileRole = (ProfileRole) result[i];
					resultRoles[i] = profileRole;
				}
			}
		}

		return resultRoles;
	}

}
