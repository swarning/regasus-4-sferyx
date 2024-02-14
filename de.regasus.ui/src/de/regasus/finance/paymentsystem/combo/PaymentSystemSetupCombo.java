package de.regasus.finance.paymentsystem.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.finance.easycheckout.EasyCheckoutSetup;


@SuppressWarnings("rawtypes")
public class PaymentSystemSetupCombo
extends AbstractComboComposite<PaymentSystemSetup>
implements CacheModelListener {
	private static final PaymentSystemSetup EMPTY_PAY_ENGINE_SETUP = new PaymentSystemSetup();

	// Model
	protected PaymentSystemSetupModel model;


	public PaymentSystemSetupCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected PaymentSystemSetup getEmptyEntity() {
		return EMPTY_PAY_ENGINE_SETUP;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				PaymentSystemSetup paymentSystemSetup = (PaymentSystemSetup) element;
				StringBuilder label = new StringBuilder();
				label.append( paymentSystemSetup.getName() );
				label.append(" (");
				label.append( paymentSystemSetup.getPaymentSystem() );
				if ( paymentSystemSetup.isTest() ) {
					label.append(" - ");
					label.append( EasyCheckoutSetup.TEST_ENVIRONMENT.getString() );
				}
				label.append(")");
				return label.toString();
			}
		};
	}


	@Override
	protected Collection<PaymentSystemSetup> getModelData() throws Exception {
		Collection<PaymentSystemSetup> modelData = model.getAllPaymentSystemSetups();
		return modelData;
	}


	@Override
	protected void initModel() {
		model = PaymentSystemSetupModel.getInstance();
		model.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getPaymentSystemSetupId() {
		Long paymentSystemSetupId = null;
		if (entity != null) {
			paymentSystemSetupId = entity.getId();
		}
		return paymentSystemSetupId;
	}


	public void setPaymentSystemSetupId(Long paymentSystemSetupId) {
		PaymentSystemSetup paymentSystemSetup = null;
		if (paymentSystemSetupId != null) {
			try {
				paymentSystemSetup = model.getPaymentSystemSetup(paymentSystemSetupId);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(paymentSystemSetup);
	}

}
