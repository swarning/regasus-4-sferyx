package de.regasus.portal.page.editor;

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
import de.regasus.portal.component.StreamComponent;


public class StreamComponentLabelsI18NWidgetController implements I18NWidgetController<StreamComponent>{

	// the entity
	private StreamComponent component;

	// widget Maps
	private Map<String, Text> omitLockedButtonLabelWidgetMap = new HashMap<>();
	private Map<String, Text> omitUnavailableNowButtonLabelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		omitLockedButtonLabelWidgetMap.put(			lang, builder.label( StreamComponent.FIELD_OMIT_LOCKED_BUTTON_LABEL.getLabel()          ).build());
		omitUnavailableNowButtonLabelWidgetMap.put(	lang, builder.label( StreamComponent.FIELD_OMIT_UNAVAILABLE_NOW_BUTTON_LABEL.getLabel() ).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public StreamComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(StreamComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getOmitLockedButtonLabel(), 		omitLockedButtonLabelWidgetMap);
						setLanguageStringToTextWidget(component.getOmitUnavailableNowButtonLabel(), omitUnavailableNowButtonLabelWidgetMap);
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
			component.setOmitLockedButtonLabel( 		buildLanguageString(omitLockedButtonLabelWidgetMap) );
			component.setOmitUnavailableNowButtonLabel( buildLanguageString(omitUnavailableNowButtonLabelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
