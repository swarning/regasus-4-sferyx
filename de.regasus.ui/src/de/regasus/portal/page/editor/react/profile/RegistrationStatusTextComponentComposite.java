package de.regasus.portal.page.editor.react.profile;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NHtmlWidget;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.validation.FieldMetadata;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.react.profile.RegistrationStatusTextComponent;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class RegistrationStatusTextComponentComposite extends EntityComposite<RegistrationStatusTextComponent> {

	private final int COL_COUNT = 2;

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

	private TabFolder tabFolder;
	private I18NHtmlWidget statusOpenWidget;
	private I18NHtmlWidget statusClosedWidget;
	private I18NHtmlWidget statusRegistrationWidget;
	private I18NHtmlWidget statusOnlineWidget;
	private I18NHtmlWidget statusProspectWidget;
	private I18NHtmlWidget statusWaitlistWidget;
	private I18NHtmlWidget statusUnpaidWidget;
	private I18NHtmlWidget statusNotFullyPaidWidget;
	private I18NHtmlWidget statusProofRequiredWidget;
	private I18NHtmlWidget statusCancelationWidget;
	private I18NHtmlWidget statusDuplicateWidget;

	// *
	// * Widgets
	// **************************************************************************


	public RegistrationStatusTextComponentComposite(Composite parent, int style, Long portalPK)
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

		widgetBuilder.buildTypeLabel( PortalI18N.RegistrationStatusTextComponent.getString() );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

		// tabFolder
		tabFolder = new TabFolder(this, SWT.NONE);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, true).applyTo(tabFolder);

		statusOpenWidget =			buildEditor(RegistrationStatusTextComponent.STATUS_OPEN_TEXT);
		statusClosedWidget =		buildEditor(RegistrationStatusTextComponent.STATUS_CLOSED_TEXT);
		statusRegistrationWidget =	buildEditor(RegistrationStatusTextComponent.STATUS_REGISTRATION_TEXT);
		statusOnlineWidget =		buildEditor(RegistrationStatusTextComponent.STATUS_ONLINE_TEXT);
		statusProspectWidget =		buildEditor(RegistrationStatusTextComponent.STATUS_PROSPECT_TEXT);
		statusWaitlistWidget =		buildEditor(RegistrationStatusTextComponent.STATUS_WAITLIST_TEXT);
		statusUnpaidWidget =		buildEditor(RegistrationStatusTextComponent.STATUS_UNPAID_TEXT);
		statusNotFullyPaidWidget =	buildEditor(RegistrationStatusTextComponent.STATUS_NOT_FULLY_PAID_TEXT);
		statusProofRequiredWidget =	buildEditor(RegistrationStatusTextComponent.STATUS_PROOF_REQUIRED_TEXT);
		statusCancelationWidget =	buildEditor(RegistrationStatusTextComponent.STATUS_CANCELATION_TEXT);
		statusDuplicateWidget =		buildEditor(RegistrationStatusTextComponent.STATUS_DUPLICATE_TEXT);
	}


	private I18NHtmlWidget buildEditor(FieldMetadata fieldMetadata) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText( fieldMetadata.getLabel() );

		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout( new GridLayout() );
		tabItem.setControl(composite);

		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel.setText( fieldMetadata.getDescription() );

		SWTHelper.verticalSpace(composite);

		I18NHtmlWidget i18nHtmlWidget = new I18NHtmlWidget(composite, SWT.NONE, languageList);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(i18nHtmlWidget);
		i18nHtmlWidget.addModifyListener(modifySupport);

		return i18nHtmlWidget;
	}


	@Override
	protected void syncWidgetsToEntity() {
			if (expertMode) {
				htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			}

			statusOpenWidget.setLanguageString( entity.getStatusOpenText() );
			statusClosedWidget.setLanguageString( entity.getStatusClosedText() );
			statusRegistrationWidget.setLanguageString( entity.getStatusRegistrationText() );
			statusOnlineWidget.setLanguageString( entity.getStatusOnlineText() );
			statusProspectWidget.setLanguageString( entity.getStatusProspectText() );
			statusWaitlistWidget.setLanguageString( entity.getStatusWaitlistText() );
			statusUnpaidWidget.setLanguageString( entity.getStatusUnpaidText() );
			statusNotFullyPaidWidget.setLanguageString( entity.getStatusNotFullyPaidText() );
			statusProofRequiredWidget.setLanguageString( entity.getStatusProofRequiredText() );
			statusCancelationWidget.setLanguageString( entity.getStatusCancelationText() );
			statusDuplicateWidget.setLanguageString( entity.getStatusDuplicateText() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
			}

			entity.setStatusOpenText( statusOpenWidget.getLanguageString() );
			entity.setStatusClosedText( statusClosedWidget.getLanguageString() );
			entity.setStatusRegistrationText( statusRegistrationWidget.getLanguageString() );
			entity.setStatusOnlineText( statusOnlineWidget.getLanguageString() );
			entity.setStatusProspectText( statusProspectWidget.getLanguageString() );
			entity.setStatusWaitlistText( statusWaitlistWidget.getLanguageString() );
			entity.setStatusUnpaidText( statusUnpaidWidget.getLanguageString() );
			entity.setStatusNotFullyPaidText( statusNotFullyPaidWidget.getLanguageString() );
			entity.setStatusProofRequiredText( statusProofRequiredWidget.getLanguageString() );
			entity.setStatusCancelationText( statusCancelationWidget.getLanguageString() );
			entity.setStatusDuplicateText( statusDuplicateWidget.getLanguageString() );
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
    		htmlIdText.setEnabled(!fixedStructure);
		}
	}

}
