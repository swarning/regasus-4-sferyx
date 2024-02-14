package de.regasus.portal.pagelayout.editor;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.portal.PageLayoutLink;


public class PageLayoutLinkI18NWidgetController implements I18NWidgetController<PageLayoutLink>{

	// the entity
	private PageLayoutLink pageLayoutLink;

	private Long portalId;

	// widget Maps
	private Map<String, Text> linkWidgetMap = new HashMap<>();
	private Map<String, Text> descriptionWidgetMap = new HashMap<>();
	private Map<String, Text> labelWidgetMap = new HashMap<>();
	private Map<String, Text> tooltipWidgetMap = new HashMap<>();


	public PageLayoutLinkI18NWidgetController(Long portalId) {
		this.portalId = portalId;
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(3, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		builder.bold(true);
		linkWidgetMap.put(lang, builder.fieldMetadata(PageLayoutLink.GLOBAL_LINK).build());


		Button chooseLinkButton = new Button(parent, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(chooseLinkButton);
		chooseLinkButton.setText("...");
		chooseLinkButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chooseLink(lang);
			}
		});


		builder.bold(false);
		descriptionWidgetMap.put(lang, builder.fieldMetadata(PageLayoutLink.DESCRIPTION).build());
		new Label(parent, SWT.NONE); // placeholder

		builder.bold(true);
		labelWidgetMap.put(lang, builder.fieldMetadata(PageLayoutLink.LABEL).build());
		new Label(parent, SWT.NONE); // placeholder

		builder.bold(false).multiLine(true);
		tooltipWidgetMap.put(lang, builder.fieldMetadata(PageLayoutLink.TOOLTIP).build());
		new Label(parent, SWT.NONE); // placeholder
	}


	private void chooseLink(String lang) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		PageLayoutLinkDialog dialog = new PageLayoutLinkDialog(shell, portalId, lang);

		int code = dialog.open();
		if (code == Window.OK) {
			// put selected value back to the text widget
			String link = dialog.getLink();
			Text linkText = linkWidgetMap.get(lang);
			linkText.setText(link);

			String description = dialog.getDescription();
			Text descriptionText = descriptionWidgetMap.get(lang);
			descriptionText.setText(description);
		}
	}


	@Override
	public void dispose() {
	}


	@Override
	public PageLayoutLink getEntity() {
		return pageLayoutLink;
	}


	@Override
	public void setEntity(PageLayoutLink pageLayoutLink) {
		this.pageLayoutLink = pageLayoutLink;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLayoutLink != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(pageLayoutLink.getGlobalLink(),   		linkWidgetMap);
						setLanguageStringToTextWidget(pageLayoutLink.getDescription(),	descriptionWidgetMap);
						setLanguageStringToTextWidget(pageLayoutLink.getLabel(),		labelWidgetMap);
						setLanguageStringToTextWidget(pageLayoutLink.getTooltip(),		tooltipWidgetMap);
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
		if (pageLayoutLink != null) {
			pageLayoutLink.setGlobalLink(       buildLanguageString(linkWidgetMap) );
			pageLayoutLink.setDescription(buildLanguageString(descriptionWidgetMap) );
			pageLayoutLink.setLabel(      buildLanguageString(labelWidgetMap) );
			pageLayoutLink.setTooltip(    buildLanguageString(tooltipWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		addFocusListenerToWidgets(listener, linkWidgetMap, descriptionWidgetMap, labelWidgetMap, tooltipWidgetMap);
	}

}
