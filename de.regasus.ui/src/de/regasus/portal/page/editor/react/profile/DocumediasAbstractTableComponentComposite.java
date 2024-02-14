package de.regasus.portal.page.editor.react.profile;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.react.profile.DocumediasAbstractTableComponent;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class DocumediasAbstractTableComponentComposite extends EntityComposite<DocumediasAbstractTableComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean expertMode;

	private Portal portal;

	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;


	private I18NComposite<DocumediasAbstractTableComponent> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************


	public DocumediasAbstractTableComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// determine Portal languages
		Long portalPK = (Long) initValues[0];
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.DocumediasAbstractTableComponent.getString() );
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

		SWTHelper.verticalSpace(parent);

		buildI18NComposite(parent);
	}


	private void buildI18NComposite(Composite parent) {
		i18nComposite = new I18NComposite<>(
			parent,
			SWT.BORDER,
			languageList,
			new DocumediasAbstractTableComponentCompositeI18NWidgetController()
		);

		GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT, 1)
			.applyTo(i18nComposite);

		i18nComposite.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
		}

		i18nComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
			}


			i18nComposite.syncEntityToWidgets();
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
    		htmlIdText.setEnabled(!fixedStructure);
		}
	}

}
