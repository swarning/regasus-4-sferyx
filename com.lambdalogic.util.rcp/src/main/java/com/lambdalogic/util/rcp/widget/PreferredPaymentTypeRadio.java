package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.PreferredPaymentType;
import com.lambdalogic.util.rcp.ModifySupport;

public class PreferredPaymentTypeRadio extends Composite {

	private ModifySupport modifySupport = new ModifySupport(this);

	private Button bankTransferButton;
	private Button onlinePaymentButton;


	public PreferredPaymentTypeRadio(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));

		bankTransferButton = new Button(this, SWT.RADIO);
		bankTransferButton.setText(PreferredPaymentType.BANK_TRANSFER.getString());
		bankTransferButton.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false) );
		bankTransferButton.addSelectionListener(modifySupport);

		onlinePaymentButton = new Button(this, SWT.RADIO);
		onlinePaymentButton.setText(PreferredPaymentType.ONLINE_PAYMENT.getString());
		onlinePaymentButton.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false) );
		onlinePaymentButton.addSelectionListener(modifySupport);
	}


	@Override
	public void setEnabled(boolean enabled) {
		bankTransferButton.setEnabled(enabled);
		onlinePaymentButton.setEnabled(enabled);
	}


	public PreferredPaymentType getValue() {
		PreferredPaymentType value = null;
		if (bankTransferButton.getSelection()) {
			value = PreferredPaymentType.BANK_TRANSFER;
		}
		else if (onlinePaymentButton.getSelection()) {
			value = PreferredPaymentType.ONLINE_PAYMENT;
		}
		return value;
	}


	public void setValue(PreferredPaymentType value) {
		bankTransferButton.setSelection(value == PreferredPaymentType.BANK_TRANSFER);
		onlinePaymentButton.setSelection(value == PreferredPaymentType.ONLINE_PAYMENT);
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
