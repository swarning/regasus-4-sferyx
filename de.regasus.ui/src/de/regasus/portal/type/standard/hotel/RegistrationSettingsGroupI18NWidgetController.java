package de.regasus.portal.type.standard.hotel;

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

public class RegistrationSettingsGroupI18NWidgetController implements I18NWidgetController<StandardHotelPortalConfig> {

	// the entity
	private StandardHotelPortalConfig portalConfig;

	// widget Maps
	private Map<String, Text> toProfileButtonLabelWidgetMap = new HashMap<>();

	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String language) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);
		toProfileButtonLabelWidgetMap.put(language, builder.label(StandardHotelPortalConfig.TO_PROFILE_PORTAL_BUTTON_LABEL.getString()).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public StandardHotelPortalConfig getEntity() {
		return portalConfig;
	}


	@Override
	public void setEntity(StandardHotelPortalConfig standardRegistrationPortalConfig) {
		this.portalConfig = standardRegistrationPortalConfig;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(portalConfig.getToProfilePortalButtonLabel(), toProfileButtonLabelWidgetMap);
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
		if (portalConfig != null) {
			portalConfig.setToProfilePortalButtonLabel(buildLanguageString(toProfileButtonLabelWidgetMap));
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
