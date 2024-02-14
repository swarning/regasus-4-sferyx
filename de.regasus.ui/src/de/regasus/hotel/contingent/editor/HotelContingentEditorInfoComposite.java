package de.regasus.hotel.contingent.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

public class HotelContingentEditorInfoComposite extends Composite {
	
	// the entity
	private HotelContingentCVO hotelContingentCVO;
	
	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets 
	private Composite contentComposite;
	
	private Text hotelInfoText;
	private I18NText guestInfoI18nText;
	

	// additional variables
	
	public HotelContingentEditorInfoComposite(Composite parent, int style) {
		super(parent, style);
		
		try {
			setLayout(new GridLayout(1, false));						
			
			contentComposite = new Composite(this, SWT.NONE);
			contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			contentComposite.setLayout(new GridLayout(2, false));
			
			// create Widgets			
			hotelInfoText = createLabelAndMultiText(
				contentComposite, 
				HotelLabel.Common_HotelInfo.getString()
			);
			{
    			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    			layoutData.heightHint = 100;
    			hotelInfoText.setLayoutData(layoutData);
			}
			hotelInfoText.addModifyListener(modifySupport);

			guestInfoI18nText =	createLabelAndI18NMultiText(
				contentComposite, 
				HotelLabel.Common_GuestInfo.getString(), 
				LanguageProvider.getInstance()
			);
			
			{
    			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    			layoutData.heightHint = 100;
    			guestInfoI18nText.setLayoutData(layoutData);
			}
			
			guestInfoI18nText.addModifyListener(modifySupport);
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
		
		syncWidgetsToEntity();
	}	


	private void syncWidgetsToEntity() {
		if (hotelContingentCVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						EventVO eventVO = EventModel.getInstance().getEventVO(hotelContingentCVO.getEventPK());
						// set values of entity to widgets
						HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();

						hotelInfoText.setText(StringHelper.avoidNull(hotelContingentVO.getHotelInfo()));
						guestInfoI18nText.setLanguageString(hotelContingentVO.getParticipantInfo(), eventVO.getLanguages());						
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (hotelContingentCVO != null) {
			HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();

			// hotel name may not be changed here, is read only
			// first and last days are derived values, are not to be set in any attribute
			hotelContingentVO.setHotelInfo(hotelInfoText.getText());
			hotelContingentVO.setParticipantInfo(guestInfoI18nText.getLanguageString());
			
		}
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}
