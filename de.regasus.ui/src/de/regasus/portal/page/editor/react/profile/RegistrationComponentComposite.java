package de.regasus.portal.page.editor.react.profile;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.react.profile.RegistrationComponent;
import de.regasus.portal.component.react.profile.RegistrationComponentButtonVisibility;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class RegistrationComponentComposite extends EntityComposite<RegistrationComponent> {

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

	private Text onlineRegistrationUrlText;

	private Button buttonVisibilityAlways;
	private Button buttonVisibilityWithoutRegistration;
	private Button buttonVisibilityWithRegistration;

	private I18NComposite<RegistrationComponent> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************


	public RegistrationComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
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

		widgetBuilder.buildTypeLabel( PortalI18N.RegistrationComponent.getString() );
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

		buildOnlineRegistrationUrlText(parent);

		SWTHelper.verticalSpace(parent);
		buildButtonVisibility(parent);
		SWTHelper.verticalSpace(parent);

		buildI18NComposite(parent);
	}


	private void buildOnlineRegistrationUrlText(Composite parent) throws Exception {
   		SWTHelper.createLabel(parent, RegistrationComponent.ONLINE_REGISTRATION_URL.getString());

   		onlineRegistrationUrlText = new Text(parent, SWT.BORDER);
   		GridDataFactory.fillDefaults().grab(true, false).applyTo(onlineRegistrationUrlText);
		onlineRegistrationUrlText.addModifyListener(modifySupport);
	}


	private void buildButtonVisibility(Composite parent) throws Exception {
		Group group = new Group(parent, SWT.NONE);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, false).applyTo(group);
		group.setLayout( new RowLayout(SWT.VERTICAL) );
		group.setText( RegistrationComponent.BUTTON_VISIBILITY.getString() );

		buttonVisibilityAlways = new Button(group, SWT.RADIO);
		buttonVisibilityAlways.setText( RegistrationComponentButtonVisibility.ALWAYS.getString() );
		buttonVisibilityAlways.setToolTipText( RegistrationComponentButtonVisibility.ALWAYS.getDesription() );
		buttonVisibilityAlways.addSelectionListener(modifySupport);

		buttonVisibilityWithoutRegistration = new Button(group, SWT.RADIO);
		buttonVisibilityWithoutRegistration.setText( RegistrationComponentButtonVisibility.WITHOUT_REGISTRATION.getString() );
		buttonVisibilityWithoutRegistration.setToolTipText( RegistrationComponentButtonVisibility.WITHOUT_REGISTRATION.getDesription() );
		buttonVisibilityWithoutRegistration.addSelectionListener(modifySupport);

		buttonVisibilityWithRegistration = new Button(group, SWT.RADIO);
		buttonVisibilityWithRegistration.setText( RegistrationComponentButtonVisibility.WITH_REGISTRATION.getString() );
		buttonVisibilityWithRegistration.setToolTipText( RegistrationComponentButtonVisibility.WITH_REGISTRATION.getDesription() );
		buttonVisibilityWithRegistration.addSelectionListener(modifySupport);
	}


	private void buildI18NComposite(Composite parent) {
		i18nComposite = new I18NComposite<>(
			parent,
			SWT.BORDER,
			languageList,
			new RegistrationComponentCompositeI18NWidgetController()
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

		onlineRegistrationUrlText.setText( avoidNull(entity.getOnlineRegistrationUrl()) );
		setButtonVisibility( entity.getButtonVisibility() );
		i18nComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
			}

			entity.setOnlineRegistrationUrl( onlineRegistrationUrlText.getText() );

			entity.setButtonVisibility( getButtonVisibility() );

			i18nComposite.syncEntityToWidgets();
		}
	}


	private RegistrationComponentButtonVisibility getButtonVisibility() {
		RegistrationComponentButtonVisibility buttonVisibility = RegistrationComponentButtonVisibility.ALWAYS;
		if ( buttonVisibilityWithoutRegistration.getSelection() ) {
			buttonVisibility = RegistrationComponentButtonVisibility.WITHOUT_REGISTRATION;
		}
		else if ( buttonVisibilityWithRegistration.getSelection() ) {
			buttonVisibility = RegistrationComponentButtonVisibility.WITH_REGISTRATION;
		}

		return buttonVisibility;
	}


	private void setButtonVisibility(RegistrationComponentButtonVisibility buttonVisibility) {
		buttonVisibilityAlways.setSelection(buttonVisibility == RegistrationComponentButtonVisibility.ALWAYS);
		buttonVisibilityWithoutRegistration.setSelection(buttonVisibility == RegistrationComponentButtonVisibility.WITHOUT_REGISTRATION);
		buttonVisibilityWithRegistration.setSelection(buttonVisibility == RegistrationComponentButtonVisibility.WITH_REGISTRATION);
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
    		htmlIdText.setEnabled(!fixedStructure);
		}
	}

}
