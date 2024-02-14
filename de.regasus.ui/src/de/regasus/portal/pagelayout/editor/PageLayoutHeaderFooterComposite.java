package de.regasus.portal.pagelayout.editor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.ui.Activator;

/**
 * Composite used in {@link PageLayoutEditor} to show the header or footer image and text of a {@link PageLayout}.
 */
public class PageLayoutHeaderFooterComposite extends LazyComposite {

	/**
	 * Define if this PageLayoutHeaderFooterComposite is handling the header (true) or footer (false) of the PageLayout.
	 */
	private boolean isHeader = true;


	// the entity
	private PageLayout pageLayout;

	// languages as defined in the associated Portal
	private List<Language> languageList;


	protected ModifySupport modifySupport = new ModifySupport(this);


	// **************************************************************************
	// * Widgets
	// *

	private I18NComposite<PageLayout> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************

	public static PageLayoutHeaderFooterComposite buildHeaderInstance(
		Composite parent,
		int style,
		List<String> languageList
	)
	throws Exception {
		return new PageLayoutHeaderFooterComposite(parent, style, languageList, true /*isHeader*/);
	}


	public static PageLayoutHeaderFooterComposite buildFooterInstance(
		Composite parent,
		int style,
		List<String> languageList
	)
	throws Exception {
		return new PageLayoutHeaderFooterComposite(parent, style, languageList, false /*isHeader*/);
	}


	private PageLayoutHeaderFooterComposite(
		Composite parent,
		int style,
		List<String> languageIds,
		boolean isHeader
	)
	throws Exception {
		super(parent, style);

		this.isHeader = isHeader;

		try {
			languageList = LanguageModel.getInstance().getLanguages(languageIds);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void createPartControl() throws Exception {
		setLayout(new FillLayout());

		PageLayoutHeaderFooterI18NWidgetController controller = new PageLayoutHeaderFooterI18NWidgetController(isHeader);
		i18nComposite = new I18NComposite<>(this, SWT.BORDER, languageList, controller);

		i18nComposite.addModifyListener(modifySupport);

		syncWidgetsToEntity();
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (i18nComposite != null) {
						i18nComposite.setEntity(pageLayout);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public void syncEntityToWidgets() {
		if (i18nComposite != null && pageLayout != null) {
			i18nComposite.syncEntityToWidgets();
		}
	}

}
