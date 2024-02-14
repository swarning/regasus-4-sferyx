package de.regasus.hotel.contingent.editor;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IActionBars;

import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class HotelContingentEditorCapacityComposite extends Composite {

	// the entity
	private HotelContingentCVO hotelContingentCVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private CapacityComposite capacityComposite;
	private Group optionalHotelBookingGroup;


	private OptionalHotelBookingManagementComposite optionalHotelBookingManagementComposite;


	public HotelContingentEditorCapacityComposite(
		Composite parent,
		int style,
		HotelConfigParameterSet hotelConfigParameterSet
	) {
		super(parent, style);

		if (hotelConfigParameterSet == null) {
			hotelConfigParameterSet = new HotelConfigParameterSet();
		}

		try {
			setLayout(new GridLayout(1, false));

			// SashForm
			SashForm sashForm = new SashForm(this, SWT.VERTICAL);
			sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


			capacityComposite = new CapacityComposite(sashForm, SWT.NONE, hotelConfigParameterSet);

			capacityComposite.addModifyListener(modifySupport);


			// Optional Hotel Booking
			optionalHotelBookingGroup = new Group(sashForm, SWT.SHADOW_ETCHED_IN);
			{
				optionalHotelBookingGroup.setText(HotelLabel.OptionalBookings.getString());
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    			optionalHotelBookingGroup.setLayoutData(layoutData);
				optionalHotelBookingGroup.setLayout(new FillLayout());
			}

			optionalHotelBookingManagementComposite = new OptionalHotelBookingManagementComposite(
				optionalHotelBookingGroup,
				SWT.NONE,
				this
			);

			optionalHotelBookingManagementComposite.addModifyListener(modifySupport);

			sashForm.setWeights(new int[] { 1, 1 });
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
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


	public void setEntity(HotelContingentCVO hotelContingentCVO) {
		this.hotelContingentCVO = hotelContingentCVO;

		capacityComposite.setEntity(hotelContingentCVO);

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (hotelContingentCVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// set values of entity to widgets
						optionalHotelBookingManagementComposite.setHotelContingentCVO(hotelContingentCVO);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		capacityComposite.syncEntityToWidgets();
		optionalHotelBookingManagementComposite.syncEntityToWidgets();
	}


	public boolean isNew() {
		return hotelContingentCVO.getPK() == null;
	}


	/**
	 * Make that Ctl+C copies table contents to clipboard.
	 */
	public void registerCopyAction(IActionBars actionBars) {
		capacityComposite.registerCopyAction(actionBars);
	}


	public Date getFirstDayDate() {
		return capacityComposite.getFirstDayDate();
	}


	public Date getLastDayDate() {
		return capacityComposite.getLastDayDate();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
