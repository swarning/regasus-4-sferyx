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
import com.lambdalogic.util.rcp.i18n.I18NHtmlWidget;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.PaymentComponent;
import de.regasus.portal.component.react.profile.RegistrationBookingTableComponent;
import de.regasus.portal.page.editor.ConditionGroup;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class RegistrationBookingTableComponentComposite extends EntityComposite<RegistrationBookingTableComponent>{

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
	private Text renderText;

	private CurrencyCombo currencyCombo;
	private ProgrammePointTypeListComposite pptListComposite;
	private I18NHtmlWidget i18nHtmlWidget;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public RegistrationBookingTableComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Long portalPK = (Long) initValues[0];

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// load Portal to get Event and Languages
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.RegistrationBookingTableComponent.getString() );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildCurrency(parent);
		buildProgrammePointTypes(parent);
		buildI18NHtmlEditor(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	public void buildCurrency(Composite parent) throws Exception {
   		SWTHelper.createLabel(this, PaymentComponent.CURRENCY.getString(), true);

   		currencyCombo = new CurrencyCombo(parent, SWT.BORDER);
   		GridDataFactory.swtDefaults().grab(true, false).applyTo(currencyCombo);
		SWTHelper.makeBold(currencyCombo);
		currencyCombo.addModifyListener(modifySupport);
	}


	private void buildProgrammePointTypes(Composite parent) {
		pptListComposite = new ProgrammePointTypeListComposite(parent, SWT.NONE);
		GridDataFactory
			.fillDefaults()
			.grab(true, true)
			.span(COL_COUNT, 1)
			.applyTo(pptListComposite);

		pptListComposite.addModifyListener(modifySupport);
	}


	private void buildI18NHtmlEditor(Composite parent) {
		i18nHtmlWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		GridDataFactory
			.fillDefaults()
			.grab(true, true)
			.span(COL_COUNT, 1)
			.indent(SWT.DEFAULT, 20)
			.applyTo(i18nHtmlWidget);

		i18nHtmlWidget.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
			if (expertMode) {
				htmlIdText.setText( avoidNull(entity.getHtmlId()) );
				renderText.setText( avoidNull(entity.getRender()) );
			}

			currencyCombo.setCurrencyCode( entity.getCurrency() );
			pptListComposite.setProgrammePointTypeIdListProvider(entity);
			i18nHtmlWidget.setLanguageString( entity.getEmptyBookingTableText() );

			visibleConditionGroup.setCondition( entity.getVisibleCondition() );
			visibleConditionGroup.setDescription( entity.getVisibleConditionDescription() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
				entity.setRender( renderText.getText() );
			}

			entity.setCurrency( currencyCombo.getCurrencyCode() );
			entity.setProgrammePointTypeIdList( pptListComposite.getProgrammePointTypeIds() );
			entity.setEmptyBookingTableText( i18nHtmlWidget.getLanguageString() );

			entity.setVisibleCondition( visibleConditionGroup.getCondition() );
			entity.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
			htmlIdText.setEnabled(!fixedStructure);
			renderText.setEnabled(!fixedStructure);
		}
	}

}
