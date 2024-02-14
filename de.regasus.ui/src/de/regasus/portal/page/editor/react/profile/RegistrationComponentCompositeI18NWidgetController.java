package de.regasus.portal.page.editor.react.profile;

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

import de.regasus.portal.component.react.profile.RegistrationComponent;

public class RegistrationComponentCompositeI18NWidgetController
	implements I18NWidgetController<RegistrationComponent> {

	// the entity
	private RegistrationComponent component;

	// widget Maps
	private Map<String, Text> buttonLabelWithoutRegistrationWidgetMap = new HashMap<>();
	private Map<String, Text> buttonLabelWithRegistrationWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		buttonLabelWithoutRegistrationWidgetMap.put(lang, builder.fieldMetadata(RegistrationComponent.BUTTON_LABEL_WITHOUT_REGISTRATION).build());
		buttonLabelWithRegistrationWidgetMap.put(lang, builder.fieldMetadata(RegistrationComponent.BUTTON_LABEL_WITH_REGISTRATION).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public RegistrationComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(RegistrationComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getButtonLabelWithoutRegistration(), buttonLabelWithoutRegistrationWidgetMap);
						setLanguageStringToTextWidget(component.getButtonLabelWithRegistration(), buttonLabelWithRegistrationWidgetMap);
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
			component.setButtonLabelWithoutRegistration( buildLanguageString(buttonLabelWithoutRegistrationWidgetMap) );
			component.setButtonLabelWithRegistration( buildLanguageString(buttonLabelWithRegistrationWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
