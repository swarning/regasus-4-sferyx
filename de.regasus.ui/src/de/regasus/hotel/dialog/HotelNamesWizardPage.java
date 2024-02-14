package de.regasus.hotel.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.StringHelper;

import de.regasus.common.composite.OrganisationNameGroup;

public class HotelNamesWizardPage extends WizardPage {

	// **************************************************************************
	// * Attributes and Widgets
	// *

	private OrganisationNameGroup organisationNameGroup;
	private Hotel hotel;


	// **************************************************************************
	// * Constructors
	// *

	public HotelNamesWizardPage(Hotel hotel) {
		super(HotelNamesWizardPage.class.getName());

		this.hotel = hotel;
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void createControl(Composite parent) {
		boolean[] required = new boolean[4];
		required[0] = true;

		organisationNameGroup = new OrganisationNameGroup(parent, SWT.NONE, required);
		setTitle(organisationNameGroup.getText());
		setControl(organisationNameGroup);

		organisationNameGroup.setOrganisation(hotel);
		organisationNameGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(isPageComplete());
			}
		});
	}

	@Override
	public boolean isPageComplete() {
		String name1 = organisationNameGroup.getName1();
		return StringHelper.isNotEmpty(name1);
	}


	// **************************************************************************
	// * Other Methods
	// *

	public void syncEntityToWidgets() {
		organisationNameGroup.syncEntityToWidgets();
	}

}
