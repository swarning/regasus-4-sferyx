package de.regasus.finance.payment.editor;

import de.regasus.core.ui.editor.SimpleEditorInput;
import de.regasus.finance.FinanceI18N;

public class PaymentReceiptTemplateEditorInput extends SimpleEditorInput {

	public PaymentReceiptTemplateEditorInput() {
		super(
			null,	// imageDescriptor,
			FinanceI18N.OpenPaymentReceiptTemplateEditor_Text,
			FinanceI18N.OpenPaymentReceiptTemplateEditor_ToolTip
		);
	}
}
