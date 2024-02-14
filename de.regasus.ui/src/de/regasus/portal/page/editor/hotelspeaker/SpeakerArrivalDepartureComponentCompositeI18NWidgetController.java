package de.regasus.portal.page.editor.hotelspeaker;

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
import de.regasus.portal.component.hotelspeaker.SpeakerArrivalDepartureComponent;

public class SpeakerArrivalDepartureComponentCompositeI18NWidgetController
	implements I18NWidgetController<SpeakerArrivalDepartureComponent> {

	// the entity
	private SpeakerArrivalDepartureComponent component;

	// widget Maps
	private Map<String, Text> arrivalLabelWidgetMap = new HashMap<>();
	private Map<String, Text> departureLabelWidgetMap = new HashMap<>();
	private Map<String, Text> invalidMessageWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		arrivalLabelWidgetMap.put(lang, builder.fieldMetadata(SpeakerArrivalDepartureComponent.ARRIVAL_LABEL).bold(true).build());
		departureLabelWidgetMap.put(lang, builder.fieldMetadata(SpeakerArrivalDepartureComponent.DEPARTURE_LABEL).bold(true).build());
		invalidMessageWidgetMap.put(lang, builder.fieldMetadata(SpeakerArrivalDepartureComponent.INVALID_MESSAGE).bold(true).build());
		
	}


	@Override
	public void dispose() {
	}


	@Override
	public SpeakerArrivalDepartureComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(SpeakerArrivalDepartureComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getArrivalLabel(), arrivalLabelWidgetMap);
						setLanguageStringToTextWidget(component.getDepartureLabel(), departureLabelWidgetMap);
						setLanguageStringToTextWidget(component.getInvalidMessage(), invalidMessageWidgetMap);
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
			component.setArrivalLabel( buildLanguageString(arrivalLabelWidgetMap) );
			component.setDepartureLabel( buildLanguageString(departureLabelWidgetMap) );
			component.setInvalidMessage( buildLanguageString(invalidMessageWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
