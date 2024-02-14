package de.regasus.hotel.offering.editor;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.util.rcp.ModifySupport;

public class PaymentTypesComposite extends Composite {

	private Map<HotelBookingPaymentCondition, Button> condition2ButtonMap = new TreeMap<HotelBookingPaymentCondition, Button>();

	private ModifySupport modifySupport = new ModifySupport(this);

	
	public PaymentTypesComposite(Composite parent, int style) {
		super(parent, style);

		int buttonStyle = SWT.RADIO;
		if ( (style & SWT.CHECK) != 0) {
			buttonStyle = SWT.CHECK;
		}

		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = true;
		setLayout(rowLayout);

		createButton(HotelBookingPaymentCondition.BOOKING_AMOUNT, buttonStyle);
		createButton(HotelBookingPaymentCondition.DEPOSIT, buttonStyle);
		createButton(HotelBookingPaymentCondition.SELF_PAY_PATIENT, buttonStyle);
		createButton(HotelBookingPaymentCondition.ABSORPTION_OF_COSTS, buttonStyle);
	}


	private void createButton(HotelBookingPaymentCondition paymentCondition, int buttonStyle) {
		Button button = new Button(this, buttonStyle);
		button.setData(paymentCondition);
		button.setText(HotelBookingVO.getLabelForHotelBookingPaymentCondition(paymentCondition).getString());

		// observe Button
		button.addSelectionListener(modifySupport);

		// put Button to condition2ButtonMap
		condition2ButtonMap.put(paymentCondition, button);
	}


	public boolean isCondition(HotelBookingPaymentCondition condition) {
		Button button = condition2ButtonMap.get(condition);
		return button.getSelection();
	}

	public void setCondition(HotelBookingPaymentCondition condition, boolean value) {
		Button button = condition2ButtonMap.get(condition);
		button.setSelection(value);
	}

	public void setUniqueConditionToTrue(HotelBookingPaymentCondition condition) {
		for (Button button : condition2ButtonMap.values()) {
			boolean value = condition == button.getData();
			button.setSelection(value);
		}
	}


	public HotelBookingPaymentCondition getCondition() {
		for (Button button : condition2ButtonMap.values()) {
			if (button.getSelection()) {
				return (HotelBookingPaymentCondition) button.getData();
			}
		}
		return null;
	}


	public Map<HotelBookingPaymentCondition, Button> getCondition2ButtonMap() {
		return condition2ButtonMap;
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

}
