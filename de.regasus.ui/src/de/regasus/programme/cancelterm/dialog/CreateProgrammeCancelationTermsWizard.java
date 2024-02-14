package de.regasus.programme.cancelterm.dialog;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.data.CreateProgrammeCancelationTermResult;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.common.dialog.CreateCancelationTermsOptionsWizardPage;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventModel;
import de.regasus.programme.ProgrammeCancelationTermModel;
import de.regasus.I18N;
import de.regasus.common.dialog.CreateCancelationTermsDatesAndAmountPage;
import de.regasus.ui.Activator;

public class CreateProgrammeCancelationTermsWizard extends Wizard {


	private CreateCancelationTermsDatesAndAmountPage datesAndAmountPage;

	/**
	 * The Long of either a Hotel or a HotelContingent, depending on the mode. If null or that of a Hotel, the eventPK is (also) needed.
	 */
	private Long pk;

	private Long eventPK;

	private EventVO eventVO;


	private CreateProgrammeCancelationTermsWizardMode mode;

	private String objectName;

	private CreateCancelationTermsOptionsWizardPage createCancelationTermsOptionsWizardPage;

	// **************************************************************************
	// * Constructors
	// *

	public CreateProgrammeCancelationTermsWizard(Long pk, Long eventPK, CreateProgrammeCancelationTermsWizardMode mode, String objectName) {
		this.pk = pk;
		this.eventPK = eventPK;
		this.mode = mode;
		this.objectName = objectName;


		try {
			eventVO = EventModel.getInstance().getEventVO(eventPK);
		}
		catch(Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}



	// **************************************************************************
	// * Initializers
	// *

	@Override
	public void addPages() {
		datesAndAmountPage = new CreateCancelationTermsDatesAndAmountPage(eventVO, false);
		datesAndAmountPage.setTitle(I18N.CreateProgrammeCancelationTerms);
		datesAndAmountPage.setMessage(mode.getTitle().replace("<x>", objectName));
		addPage(datesAndAmountPage);


		createCancelationTermsOptionsWizardPage = new CreateCancelationTermsOptionsWizardPage();
		addPage(createCancelationTermsOptionsWizardPage);
	}



	@Override
	public boolean performFinish() {
		ProgrammeCancelationTermModel pctModel = ProgrammeCancelationTermModel.getInstance();
		Date startDate = datesAndAmountPage.getStartDate();
		Date endDate = datesAndAmountPage.getEndDate();

		BigDecimal percentValue = datesAndAmountPage.getPercent();
		PriceVO priceVO = datesAndAmountPage.getPrice();

		boolean forceInterval = createCancelationTermsOptionsWizardPage.isForceInterval();


		try {
			CreateProgrammeCancelationTermResult result = null;

			switch (mode) {
			case EVENT:
				result = pctModel.createProgrammeCancelationTermsByEvent(
					eventPK,
					startDate,
					endDate,
					percentValue,
					priceVO,
					forceInterval
				);
				break;
			case PROGRAMME_POINT:
				result = pctModel.createProgrammeCancelationTermsByProgrammePointPK(
					pk,
					startDate,
					endDate,
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
		return I18N.CreateProgrammeCancelationTerms;
	}

}
