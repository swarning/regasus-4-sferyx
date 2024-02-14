package de.regasus.finance.invoicenumberrange.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class DaysBetweenRemindersGroup extends Group {

	// the entity
	private InvoiceNoRangeVO invoiceNoRangeVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private NullableSpinner reminder1Spinner;
	private NullableSpinner reminder2Spinner;
	private NullableSpinner reminder3Spinner;
	private NullableSpinner reminder4Spinner;
	private NullableSpinner reminder5Spinner;

	// *
	// * Widgets
	// **************************************************************************


	public DaysBetweenRemindersGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		createWidgets();
		addModifyListenerToWidgets();
	}


	private void createWidgets() throws Exception {
		setText(I18N.InvoiceNoRangeEditor_ReminderGroupLabel);
		setToolTipText(I18N.InvoiceNoRangeEditor_ReminderGroupToolTip);

		setLayout(new GridLayout(3, false));

		GridDataFactory labelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);



		// 1
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder1);

    		reminder1Spinner = new NullableSpinner(this, SWT.NONE);
    		widgetGridDataFactory.applyTo(reminder1Spinner);
    		reminder1Spinner.setMinimum(InvoiceNoRangeVO.MIN_REL_REMINDER_1);
    		reminder1Spinner.setMaximum(InvoiceNoRangeVO.MAX_REL_REMINDER_1);

    		Label daysLabel = new Label(this, SWT.NONE);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);
		}

		// 2
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder2);

    		reminder2Spinner = new NullableSpinner(this, SWT.NONE);
    		widgetGridDataFactory.applyTo(reminder2Spinner);
    		reminder2Spinner.setMinimum(InvoiceNoRangeVO.MIN_REL_REMINDER_2);
    		reminder2Spinner.setMaximum(InvoiceNoRangeVO.MAX_REL_REMINDER_2);

    		Label daysLabel = new Label(this, SWT.NONE);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);
		}

		// 3
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder3);

    		reminder3Spinner = new NullableSpinner(this, SWT.NONE);
    		widgetGridDataFactory.applyTo(reminder3Spinner);
    		reminder3Spinner.setMinimum(InvoiceNoRangeVO.MIN_REL_REMINDER_3);
    		reminder3Spinner.setMaximum(InvoiceNoRangeVO.MAX_REL_REMINDER_3);

    		Label daysLabel = new Label(this, SWT.NONE);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);
		}

		// 4
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder4);

    		reminder4Spinner = new NullableSpinner(this, SWT.NONE);
    		widgetGridDataFactory.applyTo(reminder4Spinner);
    		reminder4Spinner.setMinimum(InvoiceNoRangeVO.MIN_REL_REMINDER_4);
    		reminder4Spinner.setMaximum(InvoiceNoRangeVO.MAX_REL_REMINDER_4);

    		Label daysLabel = new Label(this, SWT.NONE);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);
		}

		// 5
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(I18N.InvoiceNoRangeEditor_Reminder5);

    		reminder5Spinner = new NullableSpinner(this, SWT.NONE);
    		widgetGridDataFactory.applyTo(reminder5Spinner);
    		reminder5Spinner.setMinimum(InvoiceNoRangeVO.MIN_REL_REMINDER_5);
    		reminder5Spinner.setMaximum(InvoiceNoRangeVO.MAX_REL_REMINDER_5);

    		Label daysLabel = new Label(this, SWT.NONE);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);
		}
	}


	private void addModifyListenerToWidgets() {
		reminder1Spinner.addModifyListener(modifySupport);
		reminder2Spinner.addModifyListener(modifySupport);
		reminder3Spinner.addModifyListener(modifySupport);
		reminder4Spinner.addModifyListener(modifySupport);
		reminder5Spinner.addModifyListener(modifySupport);
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
						reminder1Spinner.setValue( invoiceNoRangeVO.getRelReminder1() );
						reminder2Spinner.setValue( invoiceNoRangeVO.getRelReminder2() );
						reminder3Spinner.setValue( invoiceNoRangeVO.getRelReminder3() );
						reminder4Spinner.setValue( invoiceNoRangeVO.getRelReminder4() );
						reminder5Spinner.setValue( invoiceNoRangeVO.getRelReminder5() );
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
			invoiceNoRangeVO.setRelReminder1( reminder1Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setRelReminder2( reminder2Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setRelReminder3( reminder3Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setRelReminder4( reminder4Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setRelReminder5( reminder5Spinner.getValueAsInteger() );
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
