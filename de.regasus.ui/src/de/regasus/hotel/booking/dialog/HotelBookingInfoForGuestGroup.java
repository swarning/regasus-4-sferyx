package de.regasus.hotel.booking.dialog;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NText;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

public class HotelBookingInfoForGuestGroup extends Group {

	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);
	
	
	private boolean showEditTime;
	
	
	// languages supported by the Event
	private List<String> languages = null;
	
	
	/*** Widgets ***/
	
	private I18NText guestInfoI18NText; 
	
	private Label editTimeLabel;
	
	
	public HotelBookingInfoForGuestGroup(Composite parent, int style, boolean showEditTime, Long eventPK) {
		super(parent, style);
		
		this.showEditTime = showEditTime;
		
		try {
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			languages = eventVO.getLanguages();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		createPartControl();
	}
	
	
	/**
	 * Create widgets.
	 */
	protected void createPartControl() {
		try {
			setText(I18N.HotelBooking_InfoForGuest);
			
			setLayoutData(new GridData(GridData.FILL_BOTH));
			setLayout(new GridLayout(2, false));

			
			// guest info
			
			// no label as long there is only this single field
//			Label guestInfoLabel = new Label(this, SWT.RIGHT);
//			guestInfoLabel.setText(HotelLabel.Common_GuestInfo.getString());
//			GridData guestInfoLabelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
//			guestInfoLabelGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
//			guestInfoLabel.setLayoutData(guestInfoLabelGridData);

			guestInfoI18NText = new I18NText(this, SWT.MULTI, LanguageProvider.getInstance());
			guestInfoI18NText.setLanguageString(new LanguageString(), languages);
			guestInfoI18NText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

			guestInfoI18NText.addModifyListener(modifySupport);
			
			
			if (showEditTime) {
				Label label = new Label(this, SWT.RIGHT);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(UtilI18N.EditDateTime + ":");

				editTimeLabel = new Label(this, SWT.NONE);
				editTimeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	public LanguageString getGuestInfo() {
		return guestInfoI18NText.getLanguageString();
	}
	
	
	public void setGuestInfo(LanguageString guestInfo) {
		guestInfoI18NText.setLanguageString(guestInfo);
	}
	
	
	public void setEditTime(Date editTime) {
		if (editTimeLabel != null) {
			String s = FormatHelper.getDefaultLocaleInstance().formatDateTime(editTime);
			editTimeLabel.setText(s);
		}
	}
	
	
	// **************************************************************************
	// * Modify support
	// *
	
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}

	
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modify support
	// **************************************************************************

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
