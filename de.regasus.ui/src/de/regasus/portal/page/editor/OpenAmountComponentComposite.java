package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.OpenAmountComponent;
import de.regasus.users.CurrentUserModel;

public class OpenAmountComponentComposite extends EntityComposite<OpenAmountComponent> {

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
	private I18NComposite<OpenAmountComponent> i18nComposite;

	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public OpenAmountComponentComposite(Composite parent, int style, Long portalPK)
	throws Exception {
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

		widgetBuilder.buildTypeLabel( PortalI18N.OpenAmountComponent.getString() );
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildCurrency(parent);
		buildLabel(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	public void buildCurrency(Composite parent) throws Exception {
   		SWTHelper.createLabel(parent, OpenAmountComponent.CURRENCY.getString(), true);

   		currencyCombo = new CurrencyCombo(parent, SWT.BORDER);
   		GridDataFactory.swtDefaults().grab(true, false).applyTo(currencyCombo);
		SWTHelper.makeBold(currencyCombo);
		currencyCombo.addModifyListener(modifySupport);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildLabel(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(label);

		i18nComposite = new I18NComposite(parent, SWT.BORDER, languageList, new OpenAmountCompositeLabelI18NWidgetController());
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		currencyCombo.setCurrencyCode( entity.getCurrency() );
		i18nComposite.setEntity(entity);

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
			i18nComposite.syncEntityToWidgets();

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
