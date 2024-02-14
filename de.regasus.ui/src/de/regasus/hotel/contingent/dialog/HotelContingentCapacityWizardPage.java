package de.regasus.hotel.contingent.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.hotel.contingent.editor.CapacityComposite;

public class HotelContingentCapacityWizardPage extends WizardPage {

	private HotelContingentCVO hotelContingentCVO;

	private CapacityComposite capacityComposite;


	protected HotelContingentCapacityWizardPage(HotelContingentCVO hotelContingentCVO) {
		super(HotelContingentCapacityWizardPage.class.getName());

		this.hotelContingentCVO = hotelContingentCVO;

		setTitle(I18N.HotelContingentEditor_RoomCapacities);
	}


	/**
	 * Enthält die Felder Name, Zimmerbezeichnung und Provision.
	 */
	@Override
	public void createControl(Composite parent) {
		// create Widgets
		try {
			ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();
			Long eventPK = hotelContingentCVO.getVO().getEventPK();
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
			HotelConfigParameterSet hotelConfigParameterSet = configParameterSet.getEvent().getHotel();

			capacityComposite = new CapacityComposite(parent, SWT.NONE, hotelConfigParameterSet);
			capacityComposite.setEntity(hotelContingentCVO);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}

		setControl(capacityComposite);
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		capacityComposite.setVisible(visible);
	}


	public void syncEntityToWidgets() {
		capacityComposite.syncEntityToWidgets();
	}

}
