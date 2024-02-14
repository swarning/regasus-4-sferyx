package de.regasus.portal.page.editor.profile;

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
import de.regasus.portal.component.profile.PortalTableComponent;


public class PortalTableComponentLabelsI18NWidgetController implements I18NWidgetController<PortalTableComponent>{

	// the entity
	private PortalTableComponent component;

	// widget Maps
	private Map<String, Text> portalNameColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> portalOnlineBeginColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> portalOnlineEndColumnNameWidgetMap = new HashMap<>();

	private Map<String, Text> eventMnemonicColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> eventNameColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> eventBeginColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> eventEndColumnNameWidgetMap = new HashMap<>();

	private Map<String, Text> registrationStatusColumnNameWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		portalNameColumnNameWidgetMap.put(			lang, builder.label( PortalTableComponent.PORTAL_NAME_COLUMN_NAME.getLabel() ).build());
		portalOnlineBeginColumnNameWidgetMap.put(	lang, builder.label( PortalTableComponent.PORTAL_ONLINE_BEGIN_COLUMN_NAME.getLabel() ).build());
		portalOnlineEndColumnNameWidgetMap.put(		lang, builder.label( PortalTableComponent.PORTAL_ONLINE_END_COLUMN_NAME.getLabel() ).build());

		eventMnemonicColumnNameWidgetMap.put(		lang, builder.label( PortalTableComponent.EVENT_MNEMONIC_COLUMN_NAME.getLabel() ).build());
		eventNameColumnNameWidgetMap.put(			lang, builder.label( PortalTableComponent.EVENT_NAME_COLUMN_NAME.getLabel() ).build());
		eventBeginColumnNameWidgetMap.put(			lang, builder.label( PortalTableComponent.EVENT_BEGIN_COLUMN_NAME.getLabel() ).build());
		eventEndColumnNameWidgetMap.put(			lang, builder.label( PortalTableComponent.EVENT_END_COLUMN_NAME.getLabel() ).build());

		registrationStatusColumnNameWidgetMap.put(	lang, builder.label( PortalTableComponent.REGISTRATION_STATUS_COLUMN_NAME.getLabel() ).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public PortalTableComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(PortalTableComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getPortalNameColumnName(), 			portalNameColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getPortalOnlineBeginColumnName(),	portalOnlineBeginColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getPortalOnlineEndColumnName(),		portalOnlineEndColumnNameWidgetMap);

						setLanguageStringToTextWidget(component.getEventMnemonicColumnName(),		eventMnemonicColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getEventNameColumnName(),			eventNameColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getEventBeginColumnName(),			eventBeginColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getEventEndColumnName(),			eventEndColumnNameWidgetMap);

						setLanguageStringToTextWidget(component.getRegistrationStatusColumnName(),	registrationStatusColumnNameWidgetMap);
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
			component.setPortalNameColumnName( 			buildLanguageString(portalNameColumnNameWidgetMap) );
			component.setPortalOnlineBeginColumnName( 	buildLanguageString(portalOnlineBeginColumnNameWidgetMap) );
			component.setPortalOnlineEndColumnName( 	buildLanguageString(portalOnlineEndColumnNameWidgetMap) );

			component.setEventMnemonicColumnName(		buildLanguageString(eventMnemonicColumnNameWidgetMap) );
			component.setEventNameColumnName(			buildLanguageString(eventNameColumnNameWidgetMap) );
			component.setEventBeginColumnName(			buildLanguageString(eventBeginColumnNameWidgetMap) );
			component.setEventEndColumnName(			buildLanguageString(eventEndColumnNameWidgetMap) );

			component.setRegistrationStatusColumnName(	buildLanguageString(registrationStatusColumnNameWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
