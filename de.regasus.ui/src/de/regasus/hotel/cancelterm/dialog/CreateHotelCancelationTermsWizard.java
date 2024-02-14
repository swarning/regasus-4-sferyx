package de.regasus.hotel.cancelterm.dialog;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.hotel.data.CreateHotelCancelationTermResult;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.common.dialog.CreateCancelationTermsOptionsWizardPage;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventModel;
import de.regasus.hotel.HotelCancelationTermModel;
import de.regasus.I18N;
import de.regasus.common.dialog.CreateCancelationTermsDatesAndAmountPage;
import de.regasus.ui.Activator;

public class CreateHotelCancelationTermsWizard extends Wizard {

	
	private CreateCancelationTermsDatesAndAmountPage datesAndAmountPage;
	
	/**
	 * The Long of either a Hotel or a HotelContingent, depending on the mode. If null or that of a Hotel, the eventPK is (also) needed. 
	 */
	private Long pk; 
	
	private Long eventPK;
	
	private EventVO eventVO;
	

	private CreateHotelCancelationTermsWizardMode mode;

	private String objectName;

	private CreateCancelationTermsOptionsWizardPage createCancelationTermsOptionsWizardPage;

	// **************************************************************************
	// * Constructors
	// *

	public CreateHotelCancelationTermsWizard(
		Long pk, 
		Long eventPK, 
		CreateHotelCancelationTermsWizardMode mode, 
		String objectName
	) {
		this.pk = pk;
		this.eventPK = eventPK;
		this.mode = mode;
		this.objectName = objectName;
		
		if (this.objectName == null) {
			this.objectName = "";
		}
		
		try {
			eventVO = EventModel.getInstance().getEventVO(eventPK);
		}
		catch(Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}



	// **************************************************************************
	// * Initializers
	// *

	@Override
	public void addPages() {
		datesAndAmountPage = new CreateCancelationTermsDatesAndAmountPage(eventVO, true);
		datesAndAmountPage.setTitle(I18N.CreateHotelCancelationTerms);
		
		String msg = mode.getTitle();
		if (msg != null) {
			msg = msg.replace("<x>", objectName);
		}
		datesAndAmountPage.setMessage(msg);
		
		addPage(datesAndAmountPage);
		
		createCancelationTermsOptionsWizardPage = new CreateCancelationTermsOptionsWizardPage();
		addPage(createCancelationTermsOptionsWizardPage);
	}



	@Override
	public boolean performFinish() {
		try {
			HotelCancelationTermModel cancelationTermModel = HotelCancelationTermModel.getInstance();
			Date startDate = datesAndAmountPage.getStartDate();
			Date endDate = datesAndAmountPage.getEndDate();
			boolean pricePerNight = datesAndAmountPage.isPricePerNight();
			
			
			BigDecimal percentValue = datesAndAmountPage.getPercent();
			PriceVO priceVO  = datesAndAmountPage.getPrice();
			
			boolean forceInterval = createCancelationTermsOptionsWizardPage.isForceInterval();
			
			
			CreateHotelCancelationTermResult result = null;
			
			switch (mode) {
				case CONTINGENT:
					result = cancelationTermModel.createHotelCancelationTermsByContingent(
						pk, 
						startDate, 
						endDate, 
						pricePerNight,
						percentValue, 
						priceVO, 
						forceInterval
					);
					break;
				case HOTEL:
					result = cancelationTermModel.createHotelCancelationTermsByHotelAndEvent(
						pk, 
						eventPK, 
						startDate, 
						endDate, 
						pricePerNight,
						percentValue, 
						priceVO, 
						forceInterval
					);
					break;
				case EVENT:
					result = cancelationTermModel.createHotelCancelationTermsByEvent(
						eventPK, 
						startDate, 
						endDate, 
						pricePerNight,
						percentValue, 
						priceVO, 
						forceInterval
					);
					break;
			}
			
			String message = result.getMessage().getString();
			MessageDialog.openInformation(getShell(), UtilI18N.Info, message);
			
			return true; 
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}
	}


	@Override
	public String getWindowTitle() {
		return I18N.CreateHotelCancelationTerms;
	}

}
