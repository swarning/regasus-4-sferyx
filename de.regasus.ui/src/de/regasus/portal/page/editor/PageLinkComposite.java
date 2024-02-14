package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.*;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLink;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;


public class PageLinkComposite extends Composite {

	// the entity
	private PageLink pageLink;

	private boolean advancedAccess = false;

	private List<Language> languageList;

	private Portal portal;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private ConditionGroup visibleConditionGroup;

	private I18NComposite<PageLink> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************


	public PageLinkComposite(
		Composite parent,
		int style,
		List<Language> languageList,
		Long portalId
	) {
		super(parent, style);

		this.languageList = languageList;

		try {
			portal = PortalModel.getInstance().getPortal(portalId);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		createWidgets();
	}


	private void createWidgets() {
		/* layout with 2 columns
		 */
		final int COL_COUNT = 2;
		setLayout( new GridLayout(COL_COUNT, false) );


		/****** Row 1 ******/
		i18nComposite = new I18NComposite<>(this, SWT.BORDER, languageList, new PageLinkI18NWidgetController());
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, false).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);


		/****** Row 2 ******/

		/*** htmlId ***/
		SWTHelper.createLabel(this, PageLink.HTML_ID.getString(), true);

		htmlIdText = new Text(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(htmlIdText);
		SWTHelper.makeBold(htmlIdText);
		htmlIdText.setTextLimit( PageLink.HTML_ID.getMaxLength() );
		htmlIdText.addModifyListener(modifySupport);
		htmlIdText.setData(PageLink.HTML_ID);
		htmlIdText.setEnabled(false);


		/****** Row 3 ******/

		/*** condition ***/
		Label conditionLabel = new Label(this, SWT.NONE);
		conditionLabel.setText(I18N.PageEditor_Visibility);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.TOP).indent(0, SWTConstants.VERTICAL_INDENT).applyTo(conditionLabel);

		visibleConditionGroup = new ConditionGroup(this, SWT.NONE, false /*showYesIfNotNewButton*/, portal);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(visibleConditionGroup);
		visibleConditionGroup.addModifyListener(modifySupport);
	}


	public PageLink getPageLink() {
		return pageLink;
	}


	public void setPageLink(PageLink pageLink) {
		this.pageLink = pageLink;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLink != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						i18nComposite.setEntity(pageLink);

						htmlIdText.setText( avoidNull(pageLink.getHtmlId()) );
						visibleConditionGroup.setCondition( pageLink.getVisibleCondition() );
						visibleConditionGroup.setDescription( pageLink.getVisibleConditionDescription() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (pageLink != null) {
			i18nComposite.syncEntityToWidgets();

			pageLink.setHtmlId( trim(htmlIdText.getText()) );
			pageLink.setVisibleCondition( visibleConditionGroup.getCondition() );
			pageLink.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
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

}
