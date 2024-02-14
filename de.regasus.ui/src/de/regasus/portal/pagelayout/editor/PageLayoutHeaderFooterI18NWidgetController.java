package de.regasus.portal.pagelayout.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.LazyableGroup;
import com.lambdalogic.util.rcp.LazyableSashForm;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.html.LazyHtmlEditor;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.image.ImageFileGroup;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.portal.PageLayoutFile;

public class PageLayoutHeaderFooterI18NWidgetController implements I18NWidgetController<PageLayout>{

	private boolean isHeader;

	// the entity
	private PageLayout pageLayout;

	/**
	 * Map from Language to Text widget.
	 */
	private List<PageLayoutImageFileGroupController> controllerList = new ArrayList<>();

	// widget Maps
	private Map<String, LazyHtmlEditor> htmlEditorMap = MapHelper.createHashMap(10);


	public PageLayoutHeaderFooterI18NWidgetController(boolean isHeader) {
		this.isHeader = isHeader;
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String language) {
		parent.setLayout( new FillLayout() );

		// add Composite for contained widgets
		LazyableSashForm sashForm = new LazyableSashForm(parent, SWT.VERTICAL);

		// ImageFileComposite
		ImageFileGroup imageFileGroup = new ImageFileGroup(sashForm, SWT.NONE);
		imageFileGroup.setText(isHeader ? I18N.PageLayoutHeaderComposite_HeaderImage : I18N.PageLayoutFooterComposite_FooterImage);

		// create ImageFileGroupController
		PageLayoutImageFileGroupController controller = new PageLayoutImageFileGroupController(language, isHeader ? PageLayoutFile.HEADER_IMAGE : PageLayoutFile.FOOTER_IMAGE);
		imageFileGroup.setController(controller);
		controllerList.add(controller);


		LazyableGroup htmlGroup = new LazyableGroup(sashForm, SWT.NONE);
		htmlGroup.setText(isHeader ? I18N.PageLayoutHeaderComposite_HeaderText : I18N.PageLayoutFooterComposite_FooterText);
		htmlGroup.setLayout(new FillLayout());

		LazyHtmlEditor htmlEditor = new LazyHtmlEditor(htmlGroup, SWT.NONE);

		// add text widget to its Map
		htmlEditorMap.put(language, htmlEditor);


		/* Define the ratio between the upper (image) part and the lower (text) part of the SashControl.
		 * This cannot be done before both part have been added.
		 */
		sashForm.setWeights(new int[] {1, 1});


		htmlEditor.addModifyListener(modifySupport);
	}


	@Override
	public void dispose() {
	}


	@Override
	public PageLayout getEntity() {
		return pageLayout;
	}


	@Override
	public void setEntity(PageLayout pageLayout) {
		this.pageLayout = pageLayout;

		// init ImageFileComposites and controllers, but not before the PageLayout exists in the DB
		if (pageLayout.getId() != null) {
    		for (PageLayoutImageFileGroupController controller : controllerList) {
    			controller.setPageLayoutPK( pageLayout.getId() );
    		}
		}

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLayout != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						syncHeaderFooterToEntity();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncHeaderFooterToEntity() {
		// set headerFooter texts
		LanguageString headerFooter = isHeader ? pageLayout.getHeaderText() : pageLayout.getFooterText();
		if (headerFooter == null) {
			headerFooter = new LanguageString();
		}
		for (Map.Entry<String, LazyHtmlEditor> entry : htmlEditorMap.entrySet()) {
			entry.getValue().setHtml( avoidNull(headerFooter.getString(entry.getKey(), false)) );
		}
	}


	@Override
	public void syncEntityToWidgets() {
		syncEntityToHeaderFooter();
	}


	private void syncEntityToHeaderFooter() {
		// build LanguageString from values in widget
		LanguageString headerFooter = new LanguageString();
		for (Map.Entry<String, LazyHtmlEditor> entry : htmlEditorMap.entrySet()) {
			String lang = entry.getKey();
			LazyHtmlEditor widget = entry.getValue();
			headerFooter.put(lang, widget.getHtml());
		}

		if (isHeader) {
			pageLayout.setHeaderText(headerFooter);
		}
		else {
			pageLayout.setFooterText(headerFooter);
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
	}

}
