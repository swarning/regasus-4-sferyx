package de.regasus.finance.invoicenumberrange.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class NumberGroup extends Group {

	// the entity
	private InvoiceNoRangeVO invoiceNoRangeVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private NullableSpinner startNoSpinner;
	private NullableSpinner endNoSpinner;
	private Label nextNoText;

	// *
	// * Widgets
	// **************************************************************************


	public NumberGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		setText( InvoiceLabel.InvoiceNumbers.getString() );

		setLayout(new GridLayout(2, false));

		GridDataFactory labelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		// Start number
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		SWTHelper.makeBold(label);
    		label.setText(InvoiceLabel.InvoiceNoRange_StartNo.getString());

    		startNoSpinner = new NullableSpinner(this, SWT.NONE);
    		widgetGridDataFactory.applyTo(startNoSpinner);
    		SWTHelper.makeBold(startNoSpinner);
    		startNoSpinner.setMinimum(InvoiceNoRangeVO.MIN_START_NO);
    		startNoSpinner.setMaximum(InvoiceNoRangeVO.MAX_START_NO);
		}

		// Last number
		{
    		Label label = new Label(this, SWT.NONE);
    		label.setText(InvoiceLabel.InvoiceNoRange_EndNo.getString());
    		labelGridDataFactory.applyTo(label);
    		SWTHelper.makeBold(label);

    		endNoSpinner = new NullableSpinner(this, SWT.NONE);
    		widgetGridDataFactory.applyTo(endNoSpinner);
    		SWTHelper.makeBold(endNoSpinner);
    		endNoSpinner.setMinimum(InvoiceNoRangeVO.MIN_END_NO);
    		endNoSpinner.setMaximum(InvoiceNoRangeVO.MAX_END_NO);
		}

		// Next number
		{
    		Label label = new Label(this, SWT.NONE);
    		label.setText(InvoiceLabel.InvoiceNoRange_NextNo.getString());
    		labelGridDataFactory.applyTo(label);

    		nextNoText = new Label(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(nextNoText);
		}


		addModifyListenerToWidgets();
	}


	private void addModifyListenerToWidgets() {
		startNoSpinner.addModifyListener(modifySupport);
		endNoSpinner.addModifyListener(modifySupport);
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
						startNoSpinner.setValue( invoiceNoRangeVO.getStartNo() );
						endNoSpinner.setValue( invoiceNoRangeVO.getEndNo() );

						String nextNo = String.valueOf( invoiceNoRangeVO.getNextNo() );
						nextNoText.setText( avoidNull(nextNo) );
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
			boolean startNoCanged = !EqualsHelper.isEqual(invoiceNoRangeVO.getStartNo(), startNoSpinner.getValueAsInteger());
			if (startNoCanged) {
				invoiceNoRangeVO.setStartNo( startNoSpinner.getValueAsInteger() );

				/* It's not necessary to set nextNo, because it is managed by the server.
				 * But because we want to call validate() later, it is set to null.
				 * Otherwise validation would fail if startNo or endNo have changed.
				 */
				invoiceNoRangeVO.setNextNo( startNoSpinner.getValueAsInteger() );
			}
			invoiceNoRangeVO.setEndNo( endNoSpinner.getValueAsInteger() );
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
