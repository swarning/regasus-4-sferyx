package de.regasus.participant.editor.finance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.invoice.data.AccountancyCVO;
import com.lambdalogic.messeinfo.invoice.data.BookingCVO;
import com.lambdalogic.messeinfo.invoice.data.ClearingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.kernel.BookingType;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.ObjectComparator;

/**
 * Mediator between the UI and an {@link AccountancyCVO}.
 */
public class AccountancyHelper {

	private AccountancyCVO accountancyCVO;
	private Map<Long, ProgrammeBookingCVO> programmeBookingMap;
	private Map<Long, HotelBookingCVO> hotelBookingMap;

	private Map<Long, List<ClearingVO>> invoicePositionPK2ClearingsMap = new TreeMap<>();

	private Map<Long, InvoiceVO> invoicePK2InvoiceVOMap = new TreeMap<>();

	private List<InvoiceVO> invoiceVOs = new ArrayList<>();

	private List<PaymentVO> paymentVOs = new ArrayList<>();

	private List<Long> invoicePKs;



	private static final Comparator<InvoiceVO> INVOICE_COMPARATOR = Comparator.nullsFirst(
		Comparator
    		.comparing(    InvoiceVO::getInvoiceDate, ObjectComparator.getInstance().nullsLast())
    		.thenComparing(InvoiceVO::getId,          ObjectComparator.getInstance())
	);


	private static final Function<PaymentVO, Date> PAYMENT_DATE_FUNCTION = new Function<>() {
		@Override
		public Date apply(PaymentVO payment) {
			Date date = payment.getBookingDate();
			if (date == null) {
				date = payment.getNewTime();
			}
			return date;
		}
	};

	private static final Comparator<PaymentVO> PAYMENT_COMPARATOR = Comparator.nullsFirst(
		Comparator
    		.comparing(    PaymentVO::isClearing, ObjectComparator.getInstance().reversed()) // Clearing Payments shall appear first
    		.thenComparing(PAYMENT_DATE_FUNCTION, ObjectComparator.getInstance())
	);


	public AccountancyHelper() {
		programmeBookingMap = MapHelper.createHashMap(100);
		hotelBookingMap = MapHelper.createHashMap(100);
	}


	public void setProgrammeBookingCVOs(Collection<ProgrammeBookingCVO> programmeBookingCVOs) {
		programmeBookingMap.clear();
		for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
			programmeBookingMap.put(programmeBookingCVO.getPK(), programmeBookingCVO);
		}
	}


	public void setHotelBookingCVOs(Collection<HotelBookingCVO> hotelBookingCVOs) {
		hotelBookingMap.clear();
		for (HotelBookingCVO hotelBookingCVO : hotelBookingCVOs) {
			hotelBookingMap.put(hotelBookingCVO.getPK(), hotelBookingCVO);
		}
	}


	public void setAccountancyCVO(AccountancyCVO accountancyCVO) {
		this.accountancyCVO = accountancyCVO;

		// Clear old data
		invoicePositionPK2ClearingsMap.clear();
		invoicePK2InvoiceVOMap.clear();
		paymentVOs.clear();
		invoiceVOs.clear();
		invoicePKs = null;

		// If no accountancy, stop here
		if (accountancyCVO == null) {
			return;
		}

		// Fill new data

		// Invoices ordered by date
		invoiceVOs.addAll( accountancyCVO.getInvoiceVOs() );

		Collections.sort(invoiceVOs, INVOICE_COMPARATOR);

		invoicePKs = AbstractVO.getPKs(invoiceVOs);

		// Invoices in a map with Long as Key
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePK2InvoiceVOMap.put(invoiceVO.getPK(), invoiceVO);
		}


		// Clearings in a map with invoice position Long as Key
		for (ClearingVO clearingVO : accountancyCVO.getClearingVOs()) {

			List<ClearingVO> clearingList = invoicePositionPK2ClearingsMap.get(clearingVO.getInvoicePositionPK());
			if (clearingList == null) {
				clearingList = new ArrayList<>();
				invoicePositionPK2ClearingsMap.put(clearingVO.getInvoicePositionPK(), clearingList);
			}
			clearingList.add(clearingVO);

		}


		// Payments ordered by booking dates (if present, see MIRCP 2206)
		paymentVOs.addAll(accountancyCVO.getPaymentVOs());
		Collections.sort(paymentVOs, PAYMENT_COMPARATOR);
	}


	public InvoiceVO getInvoiceByPK(Long invoicePK) {
		return invoicePK2InvoiceVOMap.get(invoicePK);
	}


	public List<ClearingVO> getClearing(InvoicePositionVO invoicePositionVO, PaymentVO paymentVO) {
		List<ClearingVO> ipClearingVOs = invoicePositionPK2ClearingsMap.get(invoicePositionVO.getPK());
		List<ClearingVO> clearingVOs = null;

		if (ipClearingVOs != null) {
			for (ClearingVO clearingVO : ipClearingVOs) {
				if (clearingVO.getPaymentPK().equals(paymentVO.getPK())) {
					if (clearingVOs == null) {
						clearingVOs = new ArrayList<>(ipClearingVOs.size());
					}
					clearingVOs.add(clearingVO);
				}
			}
		}
		return clearingVOs;
	}


	public List<InvoiceVO> getInvoiceVOs() {
		return invoiceVOs;
	}


	public List<PaymentVO> getPaymentVOs() {
		return paymentVOs;
	}


	public int getInvoiceIndex(Long invoicePK) {
		int index = 0;
		if (invoicePKs != null) {
			for (Long pk : invoicePKs) {
				if (pk.equals(invoicePK)) {
					return index;
				}
				index++;
			}
		}
		return -1;
	}


	public boolean hasUnbalancedPayments() {
		for (PaymentVO paymentVO : paymentVOs) {
			if (! paymentVO.isBalanced()) {
				return true;
			}
		}
		return false;
	}


	public boolean hasEvenInvoiceIndex(Long invoicePK) {
		return invoicePKs.indexOf(invoicePK) % 2 == 0;
	}


	public BigDecimal getCumulatedClearingAmount(InvoiceVO invoiceVO, PaymentVO paymentVO) {
		BigDecimal result = null;
		List<InvoicePositionVO> invoicePositionVOs = invoiceVO.getInvoicePositionVOs();
		if (invoicePositionVOs != null) {
			for (InvoicePositionVO invoicePositionVO : invoicePositionVOs) {
				List<ClearingVO> clearingVOs = getClearing(invoicePositionVO, paymentVO);
				if (clearingVOs != null) {
					if (result == null) {
						result = BigDecimal.ZERO;
					}
					for (ClearingVO clearingVO : clearingVOs) {
						result = result.add(clearingVO.getAmount());
					}
				}
			}
		}
		return result;
	}


	public List<String> getCurrencyList() {
		List<String> currencyList = null;
		if (accountancyCVO != null) {
			currencyList = accountancyCVO.getCurrencyList();
		}
		return currencyList;
	}


	public CurrencyAmount getTotalIncomingPaymentAmountByCurrency(String currency) {
		CurrencyAmount currencyAmount = null;
		if (accountancyCVO != null) {
			BigDecimal amount = accountancyCVO.getIncomingPaymentTotalMap().get(currency);
			currencyAmount = new CurrencyAmount(amount, currency);
		}
		return currencyAmount;
	}


	public CurrencyAmount getTotalRefundAmountByCurrency(String currency) {
		CurrencyAmount currencyAmount = null;
		if (accountancyCVO != null) {
			BigDecimal amount = accountancyCVO.getOutgoingTotalMap().get(currency);
			currencyAmount = new CurrencyAmount(amount, currency);
		}
		return currencyAmount;
	}


	public CurrencyAmount getTotalInvoiceAmountByCurrency(String currency) {
		CurrencyAmount currencyAmount = null;
		if (accountancyCVO != null) {
			BigDecimal amount = accountancyCVO.getInvoiceTotalMap().get(currency);
			currencyAmount = new CurrencyAmount(amount, currency);
		}
		return currencyAmount;
	}


	public CurrencyAmount getOpenInvoiceAmountByCurrency(String currency) {
		CurrencyAmount currencyAmount = null;
		if (accountancyCVO != null) {
			BigDecimal amount = accountancyCVO.getOpenAmountMap().get(currency);
			currencyAmount = new CurrencyAmount(amount, currency);
		}
		return currencyAmount;
	}


	public CurrencyAmount getTotalPaymentAmountByCurrency(String currency) {
		CurrencyAmount currencyAmount = null;
		if (accountancyCVO != null) {
			BigDecimal amount = accountancyCVO.getPaymentTotalMap().get(currency);
			currencyAmount = new CurrencyAmount(amount, currency);
		}
		return currencyAmount;
	}


    public BookingCVO<?> getBookingCVO(Long bookingPK) {
    	BookingCVO<?> bookingCVO = null;

    	BookingType bookingType = BookingType.getBookingTypeByPK(bookingPK);
    	if (bookingType == BookingType.PROGRAMME_BOOKING) {
    		bookingCVO = programmeBookingMap.get(bookingPK);
    	}
    	else if (bookingType == BookingType.HOTEL_BOOKING) {
    		bookingCVO = hotelBookingMap.get(bookingPK);
    	}

    	return bookingCVO;
    }

}
