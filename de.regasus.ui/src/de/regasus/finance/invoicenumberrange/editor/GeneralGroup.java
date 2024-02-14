package de.regasus.finance.invoicenumberrange.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class GeneralGroup extends Group {

	// the entity
	private InvoiceNoRangeVO invoiceNoRangeVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	private boolean auditProofAccountancy;

	// **************************************************************************
	// * Widgets
	// *

	private Text nameText;
	private Text descriptionText;
	private Text prefixText;
	private Button exportableCheckButton;
	private Button auditProofCheckButton;

	// *
	// * Widgets
	// **************************************************************************


	public GeneralGroup(Composite parent, int style, ConfigParameterSet configParameterSet)
	throws Exception {
		super(parent, style);

		auditProofAccountancy = configParameterSet.getEvent().getAuditProof().isVisible();

		createWidgets();
		addModifyListenerToWidgets();
	}


	private void createWidgets() throws Exception {
		setText( KernelLabel.General.getString() );

		setLayout(new GridLayout(2, false));

		GridDataFactory labelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory textGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		// Name
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		SWTHelper.makeBold(label);
    		label.setText(UtilI18N.Name);

    		nameText = new Text(this, SWT.BORDER);
    		textGridDataFactory.applyTo(nameText);
    		SWTHelper.makeBold(nameText);
    		nameText.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_NAME);
		}

		// Description
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText(UtilI18N.Description);

    		descriptionText = new Text(this, SWT.BORDER);
    		textGridDataFactory.applyTo(descriptionText);
    		descriptionText.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_DESCRIPTION);
		}

		// Prefix
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( InvoiceLabel.InvoiceNoRange_Prefix.getString() );

    		prefixText = new Text(this, SWT.BORDER);
    		textGridDataFactory.applyTo(prefixText);
    		prefixText.setTextLimit(InvoiceNoRangeVO.MAX_LENGTH_NUMBER_PREFIX);
		}

		// Exportable
		{
    		new Label(this, SWT.NONE); // dummy-label for layout

    		exportableCheckButton = new Button(this, SWT.CHECK);
    		exportableCheckButton.setText( InvoiceLabel.InvoiceNoRange_Exportable.getString() );
    		exportableCheckButton.setToolTipText(I18N.InvoiceNoRangeEditor_ExportableToolTip);
		}

		// Audit-Proof
		if (auditProofAccountancy) {
			new Label(this, SWT.NONE); // dummy-label for layout

			auditProofCheckButton = new Button(this, SWT.CHECK);
			auditProofCheckButton.setText( InvoiceLabel.InvoiceNoRange_AuditProof.getString() );
			auditProofCheckButton.setToolTipText(I18N.InvoiceNoRangeEditor_AuditProofToolTip);
		}
	}


	private void addModifyListenerToWidgets() {
		nameText.addModifyListener(modifySupport);
		descriptionText.addModifyListener(modifySupport);
		prefixText.addModifyListener(modifySupport);
		exportableCheckButton.addSelectionListener(modifySupport);
		if (auditProofAccountancy) {
			auditProofCheckButton.addSelectionListener(modifySupport);
		}

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
						nameText.setText( avoidNull(invoiceNoRangeVO.getName()) );
						descriptionText.setText( avoidNull(invoiceNoRangeVO.getDescription()) );
						prefixText.setText( avoidNull(invoiceNoRangeVO.getNumberPrefix()) );

						exportableCheckButton.setSelection( invoiceNoRangeVO.isExportable() );

						if (auditProofAccountancy) {
							auditProofCheckButton.setSelection( invoiceNoRangeVO.isAuditProof() );
						}
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
			invoiceNoRangeVO.setDescription( descriptionText.getText() );
			invoiceNoRangeVO.setName( nameText.getText() );
			invoiceNoRangeVO.setNumberPrefix( prefixText.getText() );

			invoiceNoRangeVO.setExportable( exportableCheckButton.getSelection() );

			if (auditProofAccountancy) {
				invoiceNoRangeVO.setAuditProof( auditProofCheckButton.getSelection() );
			}
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
