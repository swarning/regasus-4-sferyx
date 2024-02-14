package de.regasus.hotel.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.TimeComposite;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.ui.Activator;

public class HotelFacilitiesComposite extends Composite {

	private ModifySupport modifySupport = new ModifySupport(this);

	private Hotel hotel;


	// widgets
	private TimeComposite checkInTimeComposite;
	private TimeComposite checkOutTimeComposite;

	private HotelPropertiesComposite hotelPropertiesComposite;

	private I18NText guestInfoI18Ntext;


	public HotelFacilitiesComposite(Composite parent, int style) {
		super(parent, style);

		try {
			// set layout for this Composite: 2 columns
			setLayout(new GridLayout());

			// checkIn / checkOut
			{
				Composite checkComposite = new Composite(this, SWT.BORDER);
				checkComposite.setLayout(new GridLayout(4, true));
				checkComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,  false));


				Label checkInTimeLabel = new Label(checkComposite, SWT.RIGHT);
	    		checkInTimeLabel.setText(HotelLabel.Hotel_checkInTime.getString());

				checkInTimeComposite = new TimeComposite(checkComposite, SWT.RIGHT);
				checkInTimeComposite.addModifyListener(modifySupport);


				Label checkOutTimeLabel = new Label(checkComposite, SWT.RIGHT);
	    		checkOutTimeLabel.setText(HotelLabel.Hotel_checkOutTime.getString());

				checkOutTimeComposite = new TimeComposite(checkComposite, SWT.RIGHT);
				checkOutTimeComposite.addModifyListener(modifySupport);
			}

			// Hotel Properties
			{
				hotelPropertiesComposite = new HotelPropertiesComposite(this, style);

				// remove 5 pixel gap around HotelPropertiesComposite
				GridLayout gridLayout = (GridLayout) hotelPropertiesComposite.getLayout();
				gridLayout.marginHeight = 0;
				gridLayout.marginWidth = 0;

				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true,  false, 2, 1);
				hotelPropertiesComposite.setLayoutData(gridData);

				hotelPropertiesComposite.addModifyListener(modifySupport);
			}


			// Guest Info
			{
				Label guestInfoLabel = new Label(this, SWT.NONE);
				guestInfoLabel.setText(HotelLabel.Common_GuestInfo.getString());
				GridData gridData = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
				gridData.verticalIndent = 10;
				guestInfoLabel.setLayoutData(gridData);

				guestInfoI18Ntext = new I18NText(this, SWT.MULTI, LanguageProvider.getInstance());
				guestInfoI18Ntext.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				guestInfoI18Ntext.addModifyListener(modifySupport);
			}

		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}


	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (hotel != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						checkInTimeComposite.setLocalTime( TypeHelper.toLocalTime(hotel.getCheckInTime()) );
						checkOutTimeComposite.setLocalTime( TypeHelper.toLocalTime(hotel.getCheckOutTime()) );
						hotelPropertiesComposite.setHotelProperties(hotel.getHotelProperties());
						guestInfoI18Ntext.setLanguageString(hotel.getGuestInfo());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		if (hotel != null) {
			hotel.setCheckInTime( TypeHelper.toDate(checkInTimeComposite.getLocalTime()) );
			hotel.setCheckOutTime( TypeHelper.toDate(checkOutTimeComposite.getLocalTime()) );

			hotelPropertiesComposite.syncEntityToWidgets();

			hotel.setGuestInfo(guestInfoI18Ntext.getLanguageString());
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

}
