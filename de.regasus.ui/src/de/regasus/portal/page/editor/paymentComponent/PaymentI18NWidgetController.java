package de.regasus.portal.page.editor.paymentComponent;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.portal.component.PaymentComponent;

public class PaymentI18NWidgetController implements I18NWidgetController<PaymentComponent>{

	// the entity
	private PaymentComponent component;

	// widget Maps
	private Map<String, Text> buttonLabelWidgetMap = new HashMap<>();
	private Map<String, Text> successMessageWidgetMap = new HashMap<>();
	private Map<String, Text> errorMessageIntroWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		buttonLabelWidgetMap.put(lang, builder.fieldMetadata(PaymentComponent.BUTTON_LABEL).build());
		successMessageWidgetMap.put(lang, builder.fieldMetadata(PaymentComponent.SUCCESS_MESSAGE).build());
		errorMessageIntroWidgetMap.put(lang, builder.fieldMetadata(PaymentComponent.ERROR_MESSAGE_INTRO).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public PaymentComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(PaymentComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getButtonLabel(), buttonLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSuccessMessage(), successMessageWidgetMap);
						setLanguageStringToTextWidget(component.getErrorMessageIntro(), errorMessageIntroWidgetMap);
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
		if (component != null) {
			component.setButtonLabel( buildLanguageString(buttonLabelWidgetMap) );
			component.setSuccessMessage( buildLanguageString(successMessageWidgetMap) );
			component.setErrorMessageIntro( buildLanguageString(errorMessageIntroWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
