package de.regasus.event.dialog;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

public class CopyEventWizard extends Wizard {

	private CopyEventWizardEventPage eventPage;
//	private CopyEventWizardInvoiceNoRangePage invoiceNoRangePage;
	
	private EventVO eventVO;
	
	private Exception exception;
	
	
	public CopyEventWizard(Long eventPK) throws Exception {
		if (eventPK == null) {
			throw new IllegalArgumentException("Parameter eventPK must not be null.");
		}

		eventVO = EventModel.getInstance().getEventVO(eventPK);
	}
	
	
	@Override
	public void addPages() {
		eventPage = new CopyEventWizardEventPage(this);
		addPage(eventPage);
	}

	
	@Override
	public boolean performFinish() {
		exception = null;

		final Long eventPK = eventVO.getID();
		final String mnemonic = eventPage.getMnemonic();
		final int dayShift = eventPage.getDayShift();

		
		BusyCursorHelper.busyCursorWhile(new Runnable() {
			public void run() {
				
				try {
					EventModel.getInstance().copyEvent(
						eventPK,
						mnemonic,
						dayShift
					);
				}
				catch (Exception e) {
					exception = e;
				}
				
			}
		});

		if (exception != null) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), exception);
			return false;
		}
		else {
			return true;
		}
	}

	
	@Override
	public String getWindowTitle() {
		return I18N.CopyEventWizard_Title;
	}

	
	public Point getPreferredSize() {
		return new Point(500, 400);
	}

	
	public EventVO getEventVO() {
		return eventVO;
	}
	
}
