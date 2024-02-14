package de.regasus.portal.page.editor.membership.ilts;

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
import de.regasus.portal.component.membership.ilts.IltsMembershipComponent;
import de.regasus.portal.page.editor.ConditionGroup;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class IltsMembershipComponentComposite extends EntityComposite<IltsMembershipComponent> {

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

	private I18NComposite<IltsMembershipComponent> i18nComposite;
	private I18NHtmlWidget nonMemberHintWidget;
	private I18NHtmlWidget unconfimedMembershipMessageWidget;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public IltsMembershipComponentComposite(Composite parent, int style, Long portalPK)
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

		widgetBuilder.buildTypeLabel( PortalI18N.IltsMembershipComponent.getString() );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildI18NWidgets(parent);
		buildNonMemberHint(parent);
		buildUnconfimedMembershipMessage(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	private void buildI18NWidgets(Composite parent) {
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new IltsMembershipI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	private void buildNonMemberHint(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(label);
		label.setText( IltsMembershipComponent.NON_MEMBER_HINT.getString() );
		SWTHelper.makeBold(label);

		nonMemberHintWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(nonMemberHintWidget);
		nonMemberHintWidget.addModifyListener(modifySupport);
	}


	private void buildUnconfimedMembershipMessage(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(COL_COUNT, 1).indent(SWT.DEFAULT, 10).applyTo(label);
		label.setText( IltsMembershipComponent.UNCONFIRMED_MEMBERSHIP_MESSAGE.getString() );
		SWTHelper.makeBold(label);

		unconfimedMembershipMessageWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(unconfimedMembershipMessageWidget);
		unconfimedMembershipMessageWidget.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		i18nComposite.setEntity(entity);
		nonMemberHintWidget.setLanguageString( entity.getNonMemberHint() );
		unconfimedMembershipMessageWidget.setLanguageString( entity.getUnconfimedMembershipMessage() );

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
			entity.setUnconfimedMembershipMessage( unconfimedMembershipMessageWidget.getLanguageString() );

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
