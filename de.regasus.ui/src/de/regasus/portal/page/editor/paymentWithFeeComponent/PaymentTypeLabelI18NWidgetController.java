package de.regasus.portal.page.editor.paymentWithFeeComponent;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.PaymentWithFeeComponent;


public class PaymentTypeLabelI18NWidgetController implements I18NWidgetController<PaymentWithFeeComponent>{


	// the entity
	private PaymentWithFeeComponent paymentComponent;

	// widget Maps
	private Map<String, Text> paymentTypeLabelWidgetMap = new HashMap<>();


	public PaymentTypeLabelI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		paymentTypeLabelWidgetMap.put(lang, builder.fieldMetadata(PaymentWithFeeComponent.PAYMENT_TYPE_LABEL).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public PaymentWithFeeComponent getEntity() {
		return paymentComponent;
	}


	@Override
	public void setEntity(PaymentWithFeeComponent paymentComponent) {
		this.paymentComponent = paymentComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (paymentComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(paymentComponent.getPaymentTypeLabel(), paymentTypeLabelWidgetMap);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (paymentComponent != null) {
			paymentComponent.setPaymentTypeLabel( buildLanguageString(paymentTypeLabelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
