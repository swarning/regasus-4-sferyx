package de.regasus.portal.page.editor;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.buildLanguageString;
import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.setLanguageStringToTextWidget;

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

import de.regasus.portal.component.OpenAmountComponent;

public class OpenAmountCompositeLabelI18NWidgetController implements I18NWidgetController<OpenAmountComponent>{

	// the entity
	private OpenAmountComponent component;

	// widget Maps
	private Map<String, Text> labelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		labelWidgetMap.put(lang, builder.fieldMetadata(OpenAmountComponent.LABEL).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public OpenAmountComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(OpenAmountComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getLabel(), labelWidgetMap);
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
			component.setLabel( buildLanguageString(labelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
