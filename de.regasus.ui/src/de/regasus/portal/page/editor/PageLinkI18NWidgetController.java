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

import de.regasus.portal.PageLink;


public class PageLinkI18NWidgetController implements I18NWidgetController<PageLink> {

	// the entity
	private PageLink pageLink;

	// widget Maps
	private Map<String, Text> labelWidgetMap = new HashMap<>();
	private Map<String, Text> tooltipWidgetMap = new HashMap<>();



	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		builder.bold(true);
		labelWidgetMap.put(lang, builder.fieldMetadata(PageLink.LABEL).build());

		builder.bold(false).multiLine(true);
		tooltipWidgetMap.put(lang, builder.fieldMetadata(PageLink.TOOLTIP).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public PageLink getEntity() {
		return pageLink;
	}


	@Override
	public void setEntity(PageLink pageLink) {
		this.pageLink = pageLink;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLink != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(pageLink.getLabel(), labelWidgetMap);
						setLanguageStringToTextWidget(pageLink.getTooltip(), tooltipWidgetMap);
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
		if (pageLink != null) {
			pageLink.setLabel(   buildLanguageString(labelWidgetMap) );
			pageLink.setTooltip( buildLanguageString(tooltipWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		addFocusListenerToWidgets(listener, labelWidgetMap, tooltipWidgetMap);
	}

}
