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


public class OnlinePaymentLabelI18NWidgetController implements I18NWidgetController<PaymentWithFeeComponent>{


	// the entity
	private PaymentWithFeeComponent paymentComponent;

	// widget Maps
	private Map<String, Text> onlinePaymentLabelWidgetMap = new HashMap<>();
	private Map<String, Text> onlinePaymentButtonLabelWidgetMap = new HashMap<>();
	private Map<String, Text> successMessageWidgetMap = new HashMap<>();
	private Map<String, Text> errorMessageIntroWidgetMap = new HashMap<>();


	public OnlinePaymentLabelI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		onlinePaymentLabelWidgetMap.put(lang, builder.fieldMetadata(PaymentWithFeeComponent.ONLINE_PAYMENT_LABEL).build());
		onlinePaymentButtonLabelWidgetMap.put(lang, builder.fieldMetadata(PaymentWithFeeComponent.ONLINE_PAYMENT_BUTTON_LABEL).build());
		successMessageWidgetMap.put(lang, builder.fieldMetadata(PaymentWithFeeComponent.SUCCESS_MESSAGE).build());
		errorMessageIntroWidgetMap.put(lang, builder.fieldMetadata(PaymentWithFeeComponent.ERROR_MESSAGE_INTRO).build());
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
						setLanguageStringToTextWidget(paymentComponent.getOnlinePaymentLabel(), onlinePaymentLabelWidgetMap);
						setLanguageStringToTextWidget(paymentComponent.getOnlinePaymentButtonLabel(), onlinePaymentButtonLabelWidgetMap);
						setLanguageStringToTextWidget(paymentComponent.getSuccessMessage(), successMessageWidgetMap);
						setLanguageStringToTextWidget(paymentComponent.getErrorMessageIntro(), errorMessageIntroWidgetMap);
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
			paymentComponent.setOnlinePaymentLabel( buildLanguageString(onlinePaymentLabelWidgetMap) );
			paymentComponent.setOnlinePaymentButtonLabel( buildLanguageString(onlinePaymentButtonLabelWidgetMap) );
			paymentComponent.setSuccessMessage( buildLanguageString(successMessageWidgetMap) );
			paymentComponent.setErrorMessageIntro( buildLanguageString(errorMessageIntroWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
