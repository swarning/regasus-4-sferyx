package de.regasus.finance.invoicenumberrange.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class FileNamePatternGroup extends Group {

	// the entity
	private InvoiceNoRangeVO invoiceNoRangeVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Text fileNamePatternInvoiceText;
	private Text fileNamePatternCreditText;
	private Text fileNamePatternReminder1Text;
	private Text fileNamePatternReminder2Text;
	private Text fileNamePatternReminder3Text;
	private Text fileNamePatternReminder4Text;
	private Text fileNamePatternReminder5Text;

	// *
	// * Widgets
	// **************************************************************************


	public FileNamePatternGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		createWidgets();
		addModifyListenerToWidgets();
	}


	private void createWidgets() throws Exception {
		setText( InvoiceLabel.InvoiceNoRange_FileNamePatterns.getString() );

		setLayout(new GridLayout(2, false));

		GridDataFactory labelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		// File name pattern for ...

		// Invoice
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(InvoiceLabel.Invoice.getString());

    		fileNamePatternInvoiceText = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(fileNamePatternInvoiceText);
    		fileNamePatternInvoiceText.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_FILE_NAME_PATTERN_INVOICE);
		}

		// Credit Note
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(InvoiceLabel.Credit.getString());

    		fileNamePatternCreditText = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(fileNamePatternCreditText);
    		fileNamePatternCreditText.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_FILE_NAME_PATTERN_CREDIT);
		}

		// Reminder 1
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder1);

    		fileNamePatternReminder1Text = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(fileNamePatternReminder1Text);
    		fileNamePatternReminder1Text.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_FILE_NAME_PATTERN_REMINDER_1);
		}

		// Reminder 2
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder2);

    		fileNamePatternReminder2Text = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(fileNamePatternReminder2Text);
    		fileNamePatternReminder2Text.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_FILE_NAME_PATTERN_REMINDER_2);
		}

		// Reminder 3
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder3);

    		fileNamePatternReminder3Text = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(fileNamePatternReminder3Text);
    		fileNamePatternReminder3Text.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_FILE_NAME_PATTERN_REMINDER_3);
		}

		// Reminder 4
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder4);

    		fileNamePatternReminder4Text = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(fileNamePatternReminder4Text);
    		fileNamePatternReminder4Text.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_FILE_NAME_PATTERN_REMINDER_4);
		}

		// Reminder 5
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder5);

    		fileNamePatternReminder5Text = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(fileNamePatternReminder5Text);
    		fileNamePatternReminder5Text.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_FILE_NAME_PATTERN_REMINDER_5);
		}
	}


	private void addModifyListenerToWidgets() {
		fileNamePatternInvoiceText.addModifyListener(modifySupport);
		fileNamePatternCreditText.addModifyListener(modifySupport);
		fileNamePatternReminder1Text.addModifyListener(modifySupport);
		fileNamePatternReminder2Text.addModifyListener(modifySupport);
		fileNamePatternReminder3Text.addModifyListener(modifySupport);
		fileNamePatternReminder4Text.addModifyListener(modifySupport);
		fileNamePatternReminder5Text.addModifyListener(modifySupport);
	}


	public void setInvoiceNoRange(InvoiceNoRangeVO invoiceNoRangeVO) {
		this.invoiceNoRangeVO = invoiceNoRangeVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (invoiceNoRangeVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						fileNamePatternInvoiceText.setText( avoidNull(invoiceNoRangeVO.getFileNamePatternInvoice()) );
						fileNamePatternCreditText.setText( avoidNull(invoiceNoRangeVO.getFileNamePatternCredit()) );
						fileNamePatternReminder1Text.setText( avoidNull(invoiceNoRangeVO.getFileNamePatternReminder1()) );
						fileNamePatternReminder2Text.setText( avoidNull(invoiceNoRangeVO.getFileNamePatternReminder2()) );
						fileNamePatternReminder3Text.setText( avoidNull(invoiceNoRangeVO.getFileNamePatternReminder3()) );
						fileNamePatternReminder4Text.setText( avoidNull(invoiceNoRangeVO.getFileNamePatternReminder4()) );
						fileNamePatternReminder5Text.setText( avoidNull(invoiceNoRangeVO.getFileNamePatternReminder5()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (invoiceNoRangeVO != null) {
			invoiceNoRangeVO.setFileNamePatternInvoice( fileNamePatternInvoiceText.getText() );
			invoiceNoRangeVO.setFileNamePatternCredit( fileNamePatternCreditText.getText() );
			invoiceNoRangeVO.setFileNamePatternReminder1( fileNamePatternReminder1Text.getText() );
			invoiceNoRangeVO.setFileNamePatternReminder2( fileNamePatternReminder2Text.getText() );
			invoiceNoRangeVO.setFileNamePatternReminder3( fileNamePatternReminder3Text.getText() );
			invoiceNoRangeVO.setFileNamePatternReminder4( fileNamePatternReminder4Text.getText() );
			invoiceNoRangeVO.setFileNamePatternReminder5( fileNamePatternReminder5Text.getText() );
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
