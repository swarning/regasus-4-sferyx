/**
 * AccountancySectionContainer.java
 * created on 18.07.2013 17:50:45
 */
package de.regasus.participant.editor.overview;

import java.math.BigDecimal;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.finance.AccountancyModel;
import de.regasus.ui.Activator;

public class AccountancySectionContainer
extends AbstractSectionContainer 
implements CacheModelListener<Long>, DisposeListener {
	
	private Long participantID;
	
	private AccountancyModel accountancyModel;
	
	private ConfigParameterSet configParameterSet;
	
	private boolean ignoreCacheModelEvents = false;
	
		
	public AccountancySectionContainer(
		FormToolkit formToolkit, 
		Composite body, 
		Long participantID,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(formToolkit, body);
		
		this.participantID = participantID;
		this.configParameterSet = configParameterSet;

		addDisposeListener(this);
		
		accountancyModel = AccountancyModel.getInstance();
		accountancyModel.addListener(this, participantID);
		
		refreshSection();
	}

	
	@Override
	protected String getTitle() {
		return I18N.ParticipantOverviewForm_AccountancyStatus;
	}
	
	
	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;
			
			AccountancyCVO accountancyCVO = accountancyModel.getAccountancyCVO(participantID);
			
			boolean visible =
				(configParameterSet == null || configParameterSet.getEvent().getInvoice().isVisible()) &&
	    		accountancyCVO != null && 
	    		! accountancyCVO.isEmpty();
			
			setVisible(visible);
			
			if (visible) {
				
				for (String currency : accountancyCVO.getCurrencyList()) {
					String symbol = CurrencyAmount.getSymbol(currency);
					// Invoice
					BigDecimal invoiceTotalAmount = accountancyCVO.getInvoiceTotalMap().get(currency);
					CurrencyAmount invoiceTotalCurrencyAmount = new CurrencyAmount(invoiceTotalAmount, currency);
					String invoiceTotalAmountAsString = invoiceTotalCurrencyAmount.format();
					addIfNotEmpty(I18N.ParticipantOverviewForm_SumOfAllInvoices + " (" + symbol + ")", invoiceTotalAmountAsString);
					
					// Incoming Payment
					BigDecimal incomingPaymentAmount = accountancyCVO.getIncomingPaymentTotalMap().get(currency);
					CurrencyAmount incomingPaymentCurrencyAmount = new CurrencyAmount(incomingPaymentAmount, currency);
					String incomingPaymentTotalAmountAsString = incomingPaymentCurrencyAmount.format();
					addIfNotEmpty(I18N.ParticipantOverviewForm_SumOfAllIncomingPayments + " (" + symbol + ")", incomingPaymentTotalAmountAsString);
					
					// Refund
					BigDecimal refundAmount = accountancyCVO.getOutgoingTotalMap().get(currency);
					CurrencyAmount refundCurrencyAmount = new CurrencyAmount(refundAmount, currency);
					String refundAmountAsString = refundCurrencyAmount.format();
					addIfNotEmpty(I18N.ParticipantOverviewForm_SumOfAllRefunds + " (" + symbol + ")", refundAmountAsString);
					
					// Total Payment
					BigDecimal paymentTotalAmount = accountancyCVO.getPaymentTotalMap().get(currency);
					CurrencyAmount paymentTotalCurrencyAmount = new CurrencyAmount(paymentTotalAmount, currency);
					String paymentTotalAmountAsString = paymentTotalCurrencyAmount.format();
					addIfNotEmpty(I18N.ParticipantOverviewForm_SumOfAllPayments + " (" + symbol + ")", paymentTotalAmountAsString);
					
					// Offener Betrag
					BigDecimal openAmount = invoiceTotalAmount.subtract(paymentTotalAmount);
					CurrencyAmount openCurrencyAmount = new CurrencyAmount(openAmount, currency);
					String openAmountAsString = openCurrencyAmount.format();
					addIfNotEmpty(I18N.ParticipantOverviewForm_OpenAmount + " (" + symbol + ")", openAmountAsString);
				}
			}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}
	

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (accountancyModel != null && participantID != null) {
			try {
				accountancyModel.removeListener(this, participantID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
