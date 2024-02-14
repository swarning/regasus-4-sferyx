package de.regasus.finance.paymentsystem.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.finance.PaymentSystem;

public class PaymentSystemSetupEditorInput extends AbstractEditorInput<Long> {

	private PaymentSystem paymentSystem;


	private PaymentSystemSetupEditorInput() {
	}


	public static PaymentSystemSetupEditorInput buildCreatePayEngineInstance() {
		PaymentSystemSetupEditorInput editorInput = new PaymentSystemSetupEditorInput();
		editorInput.paymentSystem = PaymentSystem.PAYENGINE;
		return editorInput;
	}


	public static PaymentSystemSetupEditorInput buildCreateEasyCheckoutInstance() {
		PaymentSystemSetupEditorInput editorInput = new PaymentSystemSetupEditorInput();
		editorInput.paymentSystem = PaymentSystem.EASY_CHECKOUT;
		return editorInput;
	}


	public static PaymentSystemSetupEditorInput buildEditInstance(Long paymentSystemSetupId) {
		PaymentSystemSetupEditorInput editorInput = new PaymentSystemSetupEditorInput();
		editorInput.key = paymentSystemSetupId;
		return editorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}


	public PaymentSystem getPaymentSystem() {
		return paymentSystem;
	}

}
