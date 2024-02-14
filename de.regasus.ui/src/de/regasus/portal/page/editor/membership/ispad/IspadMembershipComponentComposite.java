package de.regasus.portal.page.editor.membership.ispad;

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
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.membership.ispad.IspadMembershipComponent;
import de.regasus.portal.page.editor.ConditionGroup;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class IspadMembershipComponentComposite extends EntityComposite<IspadMembershipComponent> {

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

	private Text clientIdText;
	private DateComposite minExpirationDateComposite;

	private IspadMembershipExistsGroup membershipExistsGroup;
	private IspadUserCredentialsGroup userCredentialsGroup;
	private IspadMembershipWantedGroup membershipWantedGroup;
	private IspadErrorMessageGroup errorMessageGroup;
	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public IspadMembershipComponentComposite(Composite parent, int style, Long portalPK)
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

		widgetBuilder.buildTypeLabel( PortalI18N.IspadMembershipComponent.getString() );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildClientId(parent);
		buildMinExpirationDate(parent);
		buildMembershipExistsGroup(parent);
		buildUserCredentialsGroup(parent);
		buildMembershipWantedGroup(parent);
		buildErrorMessageGroup(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	private void buildClientId(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText( IspadMembershipComponent.CLIENT_ID.getString() );
		SWTHelper.makeBold(label);

		clientIdText = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(clientIdText);
		clientIdText.setTextLimit( IspadMembershipComponent.CLIENT_ID.getMaxLength() );
		SWTHelper.makeBold(clientIdText);
		clientIdText.addModifyListener(modifySupport);
	}


	private void buildMinExpirationDate(Composite parent) {
		Label label = new Label(parent, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText( IspadMembershipComponent.MIN_EXPIRATION_DATE.getString() );
		SWTHelper.makeBold(label);

		minExpirationDateComposite = new DateComposite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(minExpirationDateComposite);
		SWTHelper.makeBold(minExpirationDateComposite);
		minExpirationDateComposite.addModifyListener(modifySupport);
	}


	private static GridDataFactory groupGridDataFactory = GridDataFactory.fillDefaults()
		.span(COL_COUNT, 1)
		.grab(true, false)
		.indent(SWT.DEFAULT, 10);


	private void buildMembershipExistsGroup(Composite parent) throws Exception {
		membershipExistsGroup = new IspadMembershipExistsGroup(parent, SWT.BORDER, languageList);
		groupGridDataFactory.applyTo(membershipExistsGroup);
		membershipExistsGroup.addModifyListener(modifySupport);
	}


	private void buildUserCredentialsGroup(Composite parent) throws Exception {
		userCredentialsGroup = new IspadUserCredentialsGroup(parent, SWT.BORDER, languageList);
		groupGridDataFactory.copy().hint(SWT.DEFAULT, 500).applyTo(userCredentialsGroup);
		userCredentialsGroup.addModifyListener(modifySupport);
	}


	private void buildMembershipWantedGroup(Composite parent) throws Exception {
		membershipWantedGroup = new IspadMembershipWantedGroup(parent, SWT.BORDER, languageList);
		groupGridDataFactory.copy().hint(SWT.DEFAULT, 500).applyTo(membershipWantedGroup);
		membershipWantedGroup.addModifyListener(modifySupport);
	}


	private void buildErrorMessageGroup(Composite parent) throws Exception {
		errorMessageGroup = new IspadErrorMessageGroup(parent, SWT.BORDER, languageList);
		groupGridDataFactory.copy().hint(SWT.DEFAULT, 800).applyTo(errorMessageGroup);
		errorMessageGroup.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		clientIdText.setText( avoidNull(entity.getClientId()) );
		minExpirationDateComposite.setI18NDate( entity.getMinExpirationDate() );
		membershipExistsGroup.setEntity(entity);
		userCredentialsGroup.setEntity(entity);
		membershipWantedGroup.setEntity(entity);
		errorMessageGroup.setEntity(entity);

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

			entity.setClientId( clientIdText.getText() );
			entity.setMinExpirationDate( minExpirationDateComposite.getI18NDate() );
			membershipExistsGroup.syncEntityToWidgets();
			userCredentialsGroup.syncEntityToWidgets();
			membershipWantedGroup.syncEntityToWidgets();
			errorMessageGroup.syncEntityToWidgets();

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
