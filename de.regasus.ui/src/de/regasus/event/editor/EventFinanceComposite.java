package de.regasus.event.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class EventFinanceComposite extends Composite {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);


	// **************************************************************************
	// * Widgets
	// *

	private EventBankGroup bankGroup;
	private EventPaymentSystemGroup paymentSystemGroup;

	// *
	// * Widgets
	// **************************************************************************

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public EventFinanceComposite(Composite parent, int style) throws Exception {
		super(parent, style);


		setLayout(new GridLayout(1, false));

		// Bank Group
		bankGroup = new EventBankGroup(this, SWT.NONE);
		bankGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// PaymentSystem Group
		paymentSystemGroup = new EventPaymentSystemGroup(this, SWT.NONE);
		paymentSystemGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		addModifyListenerToWidgets();
	}


	private void addModifyListenerToWidgets() {
		bankGroup.addModifyListener(modifySupport);
		paymentSystemGroup.addModifyListener(modifySupport);
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


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						bankGroup.setEvent(eventVO);
						paymentSystemGroup.setEvent(eventVO);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			bankGroup.syncEntityToWidgets();
			paymentSystemGroup.syncEntityToWidgets();
		}
	}


	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}

}
