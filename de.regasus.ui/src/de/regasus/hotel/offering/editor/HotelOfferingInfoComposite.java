package de.regasus.hotel.offering.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

public class HotelOfferingInfoComposite extends Composite {

	// the entity
	private HotelOfferingVO hotelOfferingVO;
	
	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private Text categoryText;
	private I18NText infoText;
	
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public HotelOfferingInfoComposite(
		Composite parent, 
		int style, 
		Long eventPK
	) {
		super(parent, style);

		try {
			setLayout(new GridLayout(4, false));
			
			// row 2, col 1
			{
				Label label = new Label(this, SWT.RIGHT);
				GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				label.setLayoutData(layoutData);
				
				label.setText(HotelLabel.Hotel_Category.getString());
			}
			
			// row 2, col 2-4
			{
    			categoryText = new Text(this, SWT.BORDER);
    			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
    			categoryText.setLayoutData(layoutData);
    
    			categoryText.setTextLimit(HotelOfferingVO.MAX_LENGTH_CATEGORY);
    			
    			categoryText.addModifyListener(modifySupport);
			}
			
			// row 4, col 1
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
				label.setText(HotelLabel.Common_GuestInfo.getString());
			}
			
			// row 4, col 2-4
			{
				infoText = new I18NText(this, SWT.MULTI, LanguageProvider.getInstance());
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
				gridData.heightHint = 100;
				infoText.setLayoutData(gridData);
				
				infoText.addModifyListener(modifySupport);
			}
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

	public void setHotelOfferingVO(HotelOfferingVO hotelOfferingVO) {
		this.hotelOfferingVO = hotelOfferingVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (hotelOfferingVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						EventVO eventVO = EventModel.getInstance().getEventVO(hotelOfferingVO.getEventPK());
						
						infoText.setLanguageString(hotelOfferingVO.getInfo(), eventVO.getLanguages());
						categoryText.setText(StringHelper.avoidNull(hotelOfferingVO.getCategory()));
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (hotelOfferingVO != null) {
			hotelOfferingVO.setInfo(infoText.getLanguageString());
			hotelOfferingVO.setCategory(categoryText.getText());
		}
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
