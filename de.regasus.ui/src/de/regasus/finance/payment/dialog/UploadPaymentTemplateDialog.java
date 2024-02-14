package de.regasus.finance.payment.dialog;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentReceiptType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.ObjectComparator;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.finance.PaymentType;
import de.regasus.finance.payment.combo.PaymentTypeCombo;


public class UploadPaymentTemplateDialog extends TitleAreaDialog {

	private String language;

	private File initFile;

	private String filePath;

	private PaymentReceiptType paymentReceiptType;
	private PaymentType paymentType;


	// List of existing templates to be able to open a confirmation dialog to ask the user if he want to overwrite
	private Collection<DataStoreVO> existingPaymentReceiptTemplateCol;

	// widgets
	private Button paymentButton;
	private Button refundButton;
	private PaymentTypeCombo paymentTypeCombo;
	private FileSelectionComposite fileSelectionComposite;
	private Button okButton;
	private LanguageCombo languageCombo;



	public UploadPaymentTemplateDialog(
		Shell shell,
		File initFile,
		PaymentReceiptType initPaymentReceiptType,
		PaymentType initPaymentType
	) {
		super(shell);
		this.initFile = initFile;
		this.paymentReceiptType = initPaymentReceiptType;
		this.paymentType = initPaymentType;
	}


	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		// Set the title
		setTitle(CoreI18N.UploadTemplateDialog_Title);

		return contents;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		try {
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setFont(parent.getFont());


			// paymentReceiptTypeLabel
			Label paymentReceiptLabel = new Label(composite, SWT.RIGHT);
			paymentReceiptLabel.setText(UtilI18N.Type);

			Composite paymentReceiptTypeComposite = new Composite(composite, SWT.NONE);
			paymentReceiptTypeComposite.setLayout(new GridLayout(2, true));

			paymentButton = new Button(paymentReceiptTypeComposite, SWT.RADIO);
			paymentButton.setText(PaymentReceiptType.PAYMENT.getString());

			refundButton = new Button(paymentReceiptTypeComposite, SWT.RADIO);
			refundButton.setText(PaymentReceiptType.REFUND.getString());

			// set initial selection
			if (paymentReceiptType != null) {
				paymentButton.setSelection(paymentReceiptType == PaymentReceiptType.PAYMENT);
				refundButton.setSelection(paymentReceiptType == PaymentReceiptType.REFUND);
			}
			else {
				paymentButton.setSelection(true);
				refundButton.setSelection(false);
			}


			// paymentTypeLabel
			Label paymentLabel = new Label(composite, SWT.RIGHT);
			paymentLabel.setText(InvoiceLabel.PaymentType.getString());

			// typeCombo
			// Do not use PaymentTypeCombo, because it contains only

			// List of all Payment Types order by name in current language
			ArrayList<PaymentType> paymentTypeList = createArrayList(PaymentType.values());

			// order PaymentTypes (as I18NString, not as enum)
			Collections.sort(paymentTypeList, ObjectComparator.getInstance());

			paymentTypeCombo = new PaymentTypeCombo(
				composite,
				SWT.DROP_DOWN | SWT.READ_ONLY,
				paymentTypeList
			);
			paymentTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			// set initial selection
			if (paymentType != null) {
				paymentTypeCombo.setEntity(paymentType);
			}
			else {
				paymentTypeCombo.setEntity(null);
			}


			// languageLabel
			Label languageLabel = new Label(composite, SWT.RIGHT);
			languageLabel.setText(UtilI18N.Language);

			// languageCombo
			languageCombo = new LanguageCombo(composite, SWT.NONE);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


			// fileLabel
			Label fileLabel = new Label(composite, SWT.RIGHT);
			fileLabel.setText(UtilI18N.File);

			// fileSelectionComposite
			fileSelectionComposite = new FileSelectionComposite(composite, SWT.OPEN);
			fileSelectionComposite.setFilterExtensions(new String[] { "*.odt" });
			fileSelectionComposite.setFile(initFile);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fileSelectionComposite.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					refreshOkButtonState();
				}
			});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return composite;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		refreshOkButtonState();
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	private void refreshOkButtonState() {
		boolean fileOrDirExists = fileSelectionComposite.fileForOpenOrDirForSaveExists();
		okButton.setEnabled(fileOrDirExists);
	}


	@Override
	protected void okPressed() {
		/* Check if a template with same type and language already exists.
		 * If so, open confirmation dialog.
		 */
		paymentReceiptType = paymentButton.getSelection() ? PaymentReceiptType.PAYMENT : PaymentReceiptType.REFUND;
		paymentType = paymentTypeCombo.getEntity();
		language = languageCombo.getLanguageCode();
		filePath = fileSelectionComposite.getFilePath();

		for (DataStoreVO dataStoreVO : existingPaymentReceiptTemplateCol) {
			if (
				// DataStoreVOs without ID are taken from JAR, so only DataSoreVOs with an ID are really overwritten.
				dataStoreVO.getID() != null
				&&
				// DataStore.docType is a combination of PaymentReceiptType and PaymentType, e.g. "PAYMENT.CASH".
				dataStoreVO.getDocType().endsWith(paymentType.name())
				&&
				EqualsHelper.isEqual(language, dataStoreVO.getLanguage())
			) {
				boolean ok = MessageDialog.openQuestion(
					this.getShell(),
					UtilI18N.Question,
					CoreI18N.TemplateExists_ConfirmOverwrite
				);
				if (! ok) {
					return;
				}
			}
		}

		super.okPressed();
	}


	public String getLanguage() {
		return language;
	}


	public String getFilePath() {
		return filePath;
	}


	public PaymentReceiptType getPaymentReceiptType() {
		return paymentReceiptType;
	}


	public PaymentType getPaymentType() {
		return paymentType;
	}


	public void setExistingPaymentReceiptTemplateList(Collection<DataStoreVO> paymentReceiptTemplateCol) {
		this.existingPaymentReceiptTemplateCol = paymentReceiptTemplateCol;
	}

}
