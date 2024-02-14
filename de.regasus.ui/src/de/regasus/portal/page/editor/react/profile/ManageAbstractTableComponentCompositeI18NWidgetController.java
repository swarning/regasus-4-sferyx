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
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.react.profile.ManageAbstractTableComponent;

public class ManageAbstractTableComponentCompositeI18NWidgetController
	implements I18NWidgetController<ManageAbstractTableComponent> {

	// the entity
	private ManageAbstractTableComponent component;

	// widget Maps
	private Map<String, Text> abstractNumberColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> abstractTitleColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> abstractStatusColumnNameWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		abstractNumberColumnNameWidgetMap.put(lang, builder.fieldMetadata(ManageAbstractTableComponent.ABSTRACT_NUMBER_COLUMN_NAME).build());
		abstractTitleColumnNameWidgetMap.put(lang, builder.fieldMetadata(ManageAbstractTableComponent.ABSTRACT_TITLE_COLUMN_NAME).build());
		abstractStatusColumnNameWidgetMap.put(lang, builder.fieldMetadata(ManageAbstractTableComponent.ABSTRACT_STATUS_COLUMN_NAME).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public ManageAbstractTableComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(ManageAbstractTableComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getAbstractNumberColumnName(), abstractNumberColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getAbstractTitleColumnName(), abstractTitleColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getAbstractStatusColumnName(), abstractStatusColumnNameWidgetMap);
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
			component.setAbstractNumberColumnName( buildLanguageString(abstractNumberColumnNameWidgetMap) );
			component.setAbstractTitleColumnName( buildLanguageString(abstractTitleColumnNameWidgetMap) );
			component.setAbstractStatusColumnName( buildLanguageString(abstractStatusColumnNameWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
