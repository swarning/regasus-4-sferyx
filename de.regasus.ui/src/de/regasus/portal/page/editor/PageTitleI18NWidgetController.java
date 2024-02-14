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
import de.regasus.portal.Page;

public class PageTitleI18NWidgetController implements I18NWidgetController<Page>{

	// the entity
	private Page page;

	// widget Maps
	private Map<String, Text> windowTitleWidgetMap = new HashMap<>();
	private Map<String, Text> stepTitleWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		windowTitleWidgetMap.put(lang, builder.fieldMetadata(Page.WINDOW_TITLE).build());
		stepTitleWidgetMap.put(lang, builder.fieldMetadata(Page.STEP_TITLE).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public Page getEntity() {
		return page;
	}


	@Override
	public void setEntity(Page page) {
		this.page = page;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (page != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(page.getWindowTitle(), windowTitleWidgetMap);
						setLanguageStringToTextWidget(page.getStepTitle(), stepTitleWidgetMap);
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
		if (page != null) {
			page.setWindowTitle( buildLanguageString(windowTitleWidgetMap) );
			page.setStepTitle( buildLanguageString(stepTitleWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
