package de.regasus.participant.collectivechange.dialog;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.tree.ArrayTreeContentProvider;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileRoleModel;
import de.regasus.profile.role.ProfileRoleLabelProvider;
import de.regasus.ui.Activator;

public class CollectiveChangeOfProfileRolesDialog extends TitleAreaDialog {

	// selected profiles size
	private int profilePKSize;


	// PKs of profileRoles that are selected..
	private List<Long> selectedRoleIDs = createArrayList();


	// **************************************************************************
	// * Widgets
	// *

	private TreeViewer availableListViewer;

	private Button addButton;
	private Button removeButton;
	private Button setButton;

	// *
	// * Widgets
	// **************************************************************************


	boolean addMode = false;
	boolean removeMode = false;
	boolean setMode = false;


	public CollectiveChangeOfProfileRolesDialog(Shell parentShell,int profilePKSize) {
		super(parentShell);
		this.profilePKSize = profilePKSize;
	}


	@Override
    public void create() {
		super.create();

		// set title and message after the dialog has been opened
		String title = I18N.CollectiveChangeOfProfileRolesDialog_title;
		title = title.replaceFirst("<count>", String.valueOf(profilePKSize));
		setTitle(title);

		setMessage(I18N.CollectiveChangeOfProfileRolesDialog_message);
    }


	 /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

    	resetButtonState();
    }


    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        try {
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			container.setLayout(new GridLayout(1, false) );

			// row 1
			{
				Composite composite = new Composite(container, SWT.NONE);
				GridData gd_genderComposite = new GridData(SWT.LEFT, SWT.FILL, true, false, 2, 1);
				composite.setLayoutData(gd_genderComposite);

				RowLayout layout = new RowLayout();
				layout.wrap = true;
				composite.setLayout(layout);

				// add profileRoles
				addButton = new Button(composite, SWT.RADIO);
				addButton.setText(I18N.CollectiveChangeProfileRolesDialog_Add);
				addButton.setToolTipText(I18N.CollectiveChangeProfileRolesDialog_Add_description);


				// remove profileRoles
				removeButton = new Button(composite, SWT.RADIO);
				removeButton.setText(I18N.CollectiveChangeProfileRolesDialog_Remove);
				removeButton.setToolTipText(I18N.CollectiveChangeProfileRolesDialog_Remove_description);


				// overwrite profileRoles
				setButton = new Button(composite, SWT.RADIO);
				setButton.setText(I18N.CollectiveChangeProfileRolesDialog_Set);
				setButton.setToolTipText(I18N.CollectiveChangeProfileRolesDialog_Set_description);

				// handle selection events
				SelectionListener radioButtonSelectionListener = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						if (!ModifySupport.isDeselectedRadioButton(event)) {
							addMode = addButton.getSelection();
							removeMode = removeButton.getSelection();
							setMode = setButton.getSelection();

							resetButtonState();
						}
					}
				};
				addButton.addSelectionListener(radioButtonSelectionListener);
				removeButton.addSelectionListener(radioButtonSelectionListener);
				setButton.addSelectionListener(radioButtonSelectionListener);
			}


			// row 2
			new Label(container, SWT.NONE);
			{
				Label availableParticipantTypesLabel = new Label(container, SWT.NONE);
				availableParticipantTypesLabel.setText( Profile.ROLES.getString() );
			}

			// row 3
			{
				Tree table = new Tree(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
				table.setLinesVisible(false);
				table.setHeaderVisible(false);
				table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

				availableListViewer = new TreeViewer(table);
				availableListViewer.setContentProvider(new ArrayTreeContentProvider());
				availableListViewer.setLabelProvider(new ProfileRoleLabelProvider());
				availableListViewer.setSorter(new ViewerSorter());
				availableListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event.getSelection();
						List<ProfileRole> profileRoles = selection.toList();

						selectedRoleIDs.clear();
						for (ProfileRole profileRole : profileRoles) {
							selectedRoleIDs.add( profileRole.getID() );
						}

						resetButtonState();
					}
				});
			}

			// get all Profile Roles
			Collection<ProfileRole> profileRoles = ProfileRoleModel.getInstance().getAllProfileRoles();

			// set all Profile Roles to availableListViewer
			availableListViewer.setInput( createArrayList(profileRoles) );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

    	return area;
    }


    private void resetButtonState() {
    	boolean enabled = addMode || removeMode || setMode;

    	if (removeMode) {
    		enabled = notEmpty(selectedRoleIDs);
    	}

    	getButton(IDialogConstants.OK_ID).setEnabled(enabled);
    }


	public List<Long> getSelectedProfileRoleIDs() {
		return selectedRoleIDs;
	}


	public boolean isAddMode() {
		return addMode;
	}


	public boolean isRemoveMode() {
		return removeMode;
	}


	public boolean isSetMode() {
		return setMode;
	}

}
