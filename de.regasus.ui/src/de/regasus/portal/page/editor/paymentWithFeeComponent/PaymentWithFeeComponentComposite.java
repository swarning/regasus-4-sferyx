package de.regasus.portal.page.editor.paymentWithFeeComponent;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.participant.data.PreferredPaymentType;
import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.PaymentWithFeeComponent;
import de.regasus.portal.page.editor.PageWidgetBuilder;

public class PaymentWithFeeComponentComposite extends EntityComposite<PaymentWithFeeComponent> {

	private static final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private Portal portal;

	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private PaymentWithFeeComponentGeneralComposite generalComposite;
	private PaymentWithFeeComponentBankTransferComposite bankTransferComposite;
	private PaymentWithFeeComponentOnlinePaymentComposite onlinePaymentComposite;

	// *
	// * Widgets
	// **************************************************************************


	public PaymentWithFeeComponentComposite(Composite parent, int style, Long portalPK)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Long portalPK = (Long) initValues[0];

		// load Portal
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.PaymentWithFeeComponent.getString() );

		// tabFolder
		tabFolder = new TabFolder(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);

		// General Tab
		{
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(UtilI18N.General);
    		generalComposite = new PaymentWithFeeComponentGeneralComposite(tabFolder, SWT.NONE, portal.getId());
    		tabItem.setControl(generalComposite);
    		generalComposite.addModifyListener(modifySupport);
		}

		// Bank Transfer Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(PreferredPaymentType.BANK_TRANSFER.getString());
    		bankTransferComposite = new PaymentWithFeeComponentBankTransferComposite(tabFolder, SWT.NONE, portal.getId());
    		tabItem.setControl(bankTransferComposite);
    		bankTransferComposite.addModifyListener(modifySupport);
		}

		// Online Payment Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(PreferredPaymentType.ONLINE_PAYMENT.getString());
    		onlinePaymentComposite = new PaymentWithFeeComponentOnlinePaymentComposite(tabFolder, SWT.NONE, portal.getId());
    		tabItem.setControl(onlinePaymentComposite);
    		onlinePaymentComposite.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		generalComposite.setEntity(entity);
		bankTransferComposite.setEntity(entity);
		onlinePaymentComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		generalComposite.syncEntityToWidgets();
		bankTransferComposite.syncEntityToWidgets();
		onlinePaymentComposite.syncEntityToWidgets();
	}


	public void setFixedStructure(boolean fixedStructure) {
		generalComposite.setFixedStructure(fixedStructure);
	}

}
