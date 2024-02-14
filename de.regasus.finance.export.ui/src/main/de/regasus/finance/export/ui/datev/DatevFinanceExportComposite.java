package de.regasus.finance.export.ui.datev;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.DirectorySelectionComposite;
import com.lambdalogic.util.rcp.widget.DirectorySelectionMessageComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.export.ui.Activator;


public class DatevFinanceExportComposite extends Composite implements DisposeListener {

	// keys used to store values in IDialogSettings
	private static final String EXPORT_INVOICES_DLG_SETTINGS_KEY = "exportInvoices";
	private static final String EXPORT_PAYMENTS_DLG_SETTINGS_KEY = "exportPayments";
	private static final String BEGIN_DATE_DLG_SETTINGS_KEY = "beginDate";
	private static final String END_DATE_DLG_SETTINGS_KEY = "endDate";
	private static final String PATH_DLG_SETTINGS_KEY = "path";


	/*** Widgets ***/
	private Button exportInvoicesButton;
	private Button exportPaymentsButton;

	private DateComposite beginDateComposite;
	private DateComposite endDateComposite;

	private DirectorySelectionComposite directorySelectionComposite;


	private ModifySupport modifySupport;



	public DatevFinanceExportComposite(Composite parent, int style) {
		super(parent, style);

		modifySupport = new ModifySupport(this);

		this.addDisposeListener(this);

		// create main Composite
		setLayout(new GridLayout(2, false));


		/***** Export Selection Group *****/

		{
    		Group group = new Group(this, style);
    		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    		group.setText("Exportdaten");
    		group.setLayout( new GridLayout(2, false) );

    		exportInvoicesButton = new Button(group, SWT.CHECK);
    		exportInvoicesButton.setText(InvoiceLabel.Invoices.getString());
    		exportInvoicesButton.addSelectionListener(modifySupport);

    		exportPaymentsButton = new Button(group, SWT.CHECK);
    		exportPaymentsButton.setText("Zahlungen");
    		exportPaymentsButton.addSelectionListener(modifySupport);
		}


		/***** begin and end date *****/

		{
    		Group group = new Group(this, style);
    		group.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1) );
    		group.setText("Zeitraum");
    		group.setLayout( new GridLayout(4, false) );

    		Label beginLabel = new Label(group, SWT.NONE);
    		beginLabel.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
    		beginLabel.setText("Beginn");

    		beginDateComposite = new DateComposite(group, SWT.BORDER);
    		beginDateComposite.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
    		beginDateComposite.addModifyListener(modifySupport);


    		Label endLabel = new Label(group, SWT.NONE);
    		endLabel.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
    		endLabel.setText("Ende");

    		endDateComposite = new DateComposite(group, SWT.BORDER);
    		endDateComposite.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
    		endDateComposite.addModifyListener(modifySupport);
		}


		/***** File Handling *****/

		Label saveToLabel = SWTHelper.createLabel(this, UtilI18N.SaveTo);
		SWTHelper.makeBold(saveToLabel);

		directorySelectionComposite = new DirectorySelectionComposite(this);
		directorySelectionComposite.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );

		// message text
		new Label(this, SWT.NONE);
		DirectorySelectionMessageComposite messageComposite = new DirectorySelectionMessageComposite(this);
		directorySelectionComposite.addModifyListener(messageComposite);


		// initialize widgets according to dialog settings
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		exportInvoicesButton.setSelection( dialogSettings.getBoolean(EXPORT_INVOICES_DLG_SETTINGS_KEY) );
		exportPaymentsButton.setSelection( dialogSettings.getBoolean(EXPORT_PAYMENTS_DLG_SETTINGS_KEY) );

		try {
    		String beginDateStr = dialogSettings.get(BEGIN_DATE_DLG_SETTINGS_KEY);
    		beginDateComposite.setI18NDate( TypeHelper.toI18NDate(beginDateStr) );
		}
		catch (Exception e) {
			RegasusErrorHandler.logError(e);
		}

		try {
			String endDateStr = dialogSettings.get(END_DATE_DLG_SETTINGS_KEY);
			endDateComposite.setI18NDate( TypeHelper.toI18NDate(endDateStr) );
		}
		catch (Exception e) {
			RegasusErrorHandler.logError(e);
		}

		String path = dialogSettings.get(PATH_DLG_SETTINGS_KEY);
		directorySelectionComposite.setDirectory(path);
		directorySelectionComposite.addModifyListener(modifySupport);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		// initialize widgets according to dialog settings
		// store dialog settings
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();

		dialogSettings.put(EXPORT_INVOICES_DLG_SETTINGS_KEY, exportInvoicesButton.getSelection());
		dialogSettings.put(EXPORT_PAYMENTS_DLG_SETTINGS_KEY, exportPaymentsButton.getSelection());

		dialogSettings.put(BEGIN_DATE_DLG_SETTINGS_KEY, TypeHelper.toString(beginDateComposite.getI18NDate() ) );
		dialogSettings.put(END_DATE_DLG_SETTINGS_KEY,   TypeHelper.toString(endDateComposite.getI18NDate()   ) );

		dialogSettings.put(PATH_DLG_SETTINGS_KEY, directorySelectionComposite.getDirPath());

		super.dispose();
	}


	public boolean isExportInvoices() {
		return exportInvoicesButton.getSelection();
	}


	public boolean isExportPayments() {
		return exportPaymentsButton.getSelection();
	}


	public I18NDate getBeginDate() {
		return beginDateComposite.getI18NDate();
	}


	public I18NDate getEndDate() {
		return endDateComposite.getI18NDate();
	}


	public String getDirectoryPathPath() {
		return directorySelectionComposite.getDirPath();
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
