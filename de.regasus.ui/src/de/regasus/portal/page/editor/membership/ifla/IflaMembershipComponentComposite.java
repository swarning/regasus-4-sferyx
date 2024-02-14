package de.regasus.portal.page.editor.membership.ifla;

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
import com.lambdalogic.util.rcp.i18n.I18NHtmlWidget;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.membership.ifla.IflaMembershipComponent;
import de.regasus.portal.page.editor.ConditionGroup;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class IflaMembershipComponentComposite extends EntityComposite<IflaMembershipComponent> {

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

	private I18NComposite<IflaMembershipComponent> i18nComposite;
	private I18NHtmlWidget nonMemberHintWidget;
	private ConditionGroup checkMembershipConditionGroup;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public IflaMembershipComponentComposite(Composite parent, int style, Long portalPK)
	throws Exception {
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

		widgetBuilder.buildTypeLabel( PortalI18N.IflaMembershipComponent.getString() );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildI18NWidgets(parent);
		buildNonMemberHint(parent);

		checkMembershipConditionGroup = widgetBuilder.buildConditionGroup( IflaMembershipComponent.CHECK_MEMBERSHIP_CONDITION.getString() );
		checkMembershipConditionGroup.setDefaultCondition(true);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	private void buildI18NWidgets(Composite parent) {
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new IflaMembershipI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	private void buildNonMemberHint(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(label);
		label.setText( IflaMembershipComponent.NON_MEMBER_HINT.getString() );
		SWTHelper.makeBold(label);

		nonMemberHintWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(nonMemberHintWidget);
		nonMemberHintWidget.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		i18nComposite.setEntity(entity);
		nonMemberHintWidget.setLanguageString( entity.getNonMemberHint() );

		checkMembershipConditionGroup.setCondition( entity.getCheckMembershipCondition() );
		checkMembershipConditionGroup.setDescription( entity.getCheckMembershipConditionDescription() );

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

			i18nComposite.syncEntityToWidgets();
			entity.setNonMemberHint( nonMemberHintWidget.getLanguageString() );

			entity.setCheckMembershipCondition( checkMembershipConditionGroup.getCondition() );
			entity.setCheckMembershipConditionDescription( checkMembershipConditionGroup.getDescription() );

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
