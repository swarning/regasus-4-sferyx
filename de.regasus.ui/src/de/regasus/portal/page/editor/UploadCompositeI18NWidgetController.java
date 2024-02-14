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
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.portal.component.FieldComponent;
import de.regasus.portal.component.UploadComponent;

public class UploadCompositeI18NWidgetController implements I18NWidgetController<UploadComponent>{

	// the entity
	private UploadComponent component;

	// widget Maps
	private Map<String, Text> buttonLabelWidgetMap = new HashMap<>();
	private Map<String, Text> labelWidgetMap = new HashMap<>();
	private Map<String, Text> tooltipWidgetMap = new HashMap<>();
	private Map<String, Text> requiredMessageWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		buttonLabelWidgetMap.put(lang, builder.fieldMetadata(UploadComponent.BUTTON_LABEL).build());

		builder.bold(false);

		labelWidgetMap.put(lang, builder.fieldMetadata(UploadComponent.LABEL).build());
		tooltipWidgetMap.put(lang, builder.multiLine(true).fieldMetadata(UploadComponent.TOOLTIP).build());
		requiredMessageWidgetMap.put(lang, 	builder.fieldMetadata(FieldComponent.REQUIRED_MESSAGE).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public UploadComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(UploadComponent component) {
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
						setLanguageStringToTextWidget(component.getLabel(), labelWidgetMap);
						setLanguageStringToTextWidget(component.getTooltip(), tooltipWidgetMap);
						setLanguageStringToTextWidget(component.getRequiredMessage(), 	requiredMessageWidgetMap);
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
			component.setLabel( buildLanguageString(labelWidgetMap) );
			component.setTooltip( buildLanguageString(tooltipWidgetMap) );
			component.setRequiredMessage(	buildLanguageString(requiredMessageWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
