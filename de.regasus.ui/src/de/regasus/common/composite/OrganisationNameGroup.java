package de.regasus.common.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Organisation;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class OrganisationNameGroup extends Group {

	private Organisation organisation;
	private ModifySupport modifySupport = new ModifySupport(this);

	
	private Label name1Label;
	private Text name1;
	private Label name2Label;
	private Text name2;
	private Label name3Label;
	private Text name3;
	private Label name4Label;
	private Text name4;

	private boolean[] required;
	
	
	public OrganisationNameGroup(
    	Composite parent,
    	int style
    ) {
		this(parent, style, null);
	}
	
    public OrganisationNameGroup(
    	Composite parent,
    	int style,
    	boolean [] required
    ) {
    	super(parent, style);

    	if (required == null) {
			required = new boolean[4];
		}
    	this.required = required;
    	
    	createWidgets();
    }
    

	protected void createWidgets() {
		setLayout(new GridLayout(2, false));
		setText(ContactLabel.Names.getString());
		

		// name1
		{
			name1Label = new Label(this, SWT.NONE);
			name1Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			name1Label.setText(ContactLabel.Organisation_name1.getString());
	
			name1 = new Text(this, SWT.BORDER);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			name1.setLayoutData(gridData);
			
			if (required[0]) {
				SWTHelper.makeBold(name1Label);
				SWTHelper.makeBold(name1);
			}
			
			name1.addModifyListener(modifySupport);
		}

		// name2
		{
			name2Label = new Label(this, SWT.NONE);
			name2Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			name2Label.setText(ContactLabel.Organisation_name2.getString());
	
			name2 = new Text(this, SWT.BORDER);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			name2.setLayoutData(gridData);
			
			if (required[1]) {
				SWTHelper.makeBold(name2Label);
				SWTHelper.makeBold(name2);
			}
			
			name2.addModifyListener(modifySupport);
		}

		// name3
		{
			name3Label = new Label(this, SWT.NONE);
			name3Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			name3Label.setText(ContactLabel.Organisation_name3.getString());
	
			name3 = new Text(this, SWT.BORDER);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			name3.setLayoutData(gridData);
			
			if (required[2]) {
				SWTHelper.makeBold(name3Label);
				SWTHelper.makeBold(name3);
			}
			
			name3.addModifyListener(modifySupport);
		}

		// name4
		{
			name4Label = new Label(this, SWT.NONE);
			name4Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			name4Label.setText(ContactLabel.Organisation_name4.getString());
	
			name4 = new Text(this, SWT.BORDER);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			name4.setLayoutData(gridData);
			
			if (required[3]) {
				SWTHelper.makeBold(name4Label);
				SWTHelper.makeBold(name4);
			}
			
			name4.addModifyListener(modifySupport);
		}
		
		
		
		
		modifySupport.addBeforeModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				/* Copy values from widgets to entity.
				 * Necessary because some ModifyListeners expect the modified data in the entity.
				 */
				syncEntityToWidgets();
			}
		});

		
		layout();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}

	
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************
	
	private void syncWidgetsToEntity() {
		if (organisation != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						modifySupport.setEnabled(false);
						
						name1.setText(StringHelper.avoidNull(organisation.getName1()));
						name2.setText(StringHelper.avoidNull(organisation.getName2()));
						name3.setText(StringHelper.avoidNull(organisation.getName3()));
						name4.setText(StringHelper.avoidNull(organisation.getName4()));
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

	
	public void syncEntityToWidgets() {
		if (organisation != null) {
			organisation.setName1(getName1());
			organisation.setName2(getName2());
			organisation.setName3(getName3());
			organisation.setName4(getName4());
		}
	}

	
	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
		syncWidgetsToEntity();
	}


	// **************************************************************************
	// * Getter
	// *

	public String getName1() {
		return StringHelper.trim(name1.getText());
	}

	
	public String getName2() {
		return StringHelper.trim(name2.getText());
	}

	
	public String getName3() {
		return StringHelper.trim(name3.getText());
	}

	
	public String getName4() {
		return StringHelper.trim(name4.getText());
	}

	// *
	// * Getter
	// **************************************************************************

}
