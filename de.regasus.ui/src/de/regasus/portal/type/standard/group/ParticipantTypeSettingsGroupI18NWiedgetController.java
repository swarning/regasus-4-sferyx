package de.regasus.portal.type.standard.group;

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

public class ParticipantTypeSettingsGroupI18NWiedgetController implements I18NWidgetController<StandardGroupPortalConfig> {

	// the entity
	private StandardGroupPortalConfig portalConfig;
	
	// widget Maps
	private Map<String, Text> messageWidgetMap = new HashMap<>();
	

	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String language) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);
		messageWidgetMap.put(language, builder.label( StandardGroupPortalConfig.MAX_GROUP_MEMBER_REACHED_MESSAGE.getString() ).build());
		
	}

	
	@Override
	public void dispose() {
	}

	
	@Override
	public StandardGroupPortalConfig getEntity() {
		return portalConfig;
	}

	
	@Override
	public void setEntity(StandardGroupPortalConfig standardGroupPortalConfig) {
		this.portalConfig = standardGroupPortalConfig;
		syncWidgetsToEntity();
	}
	

	private void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(portalConfig.getMaxGroupMemberReachedMessage(), messageWidgetMap);
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
			portalConfig.setMaxGroupMemberReachedMessage(buildLanguageString(messageWidgetMap));
		}		
	}

	
	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}
	
	
}
