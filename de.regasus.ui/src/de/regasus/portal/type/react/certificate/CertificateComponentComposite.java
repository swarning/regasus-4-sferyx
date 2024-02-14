package de.regasus.portal.type.react.certificate;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.react.certificate.CertificateComponent;
import de.regasus.portal.page.editor.ConditionGroup;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.portal.page.editor.ProgrammePointListComposite;
import de.regasus.users.CurrentUserModel;

public class CertificateComponentComposite extends EntityComposite<CertificateComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private Portal portal;

	private boolean expertMode;

	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private NullableSpinner reportDefinitionIdSpinner;

	private I18NComposite<CertificateComponent> i18nComposite;

	private ProgrammePointListComposite ppListComposite;

	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public CertificateComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(parent, style, Objects.requireNonNull(portalPK));
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
		widgetBuilder.buildTypeLabel( PortalI18N.CertificateComponent.getString() );

		setLayout( new GridLayout(COL_COUNT, false) );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

		buildReportDefinitionId(parent);

		buildI18NComposite(parent);

		SWTHelper.verticalSpace(parent);

		buildProgrammePoints(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
		}

		reportDefinitionIdSpinner.setValue(entity.getReportDefinitionId());

		ppListComposite.setProgrammePointIdListProvider(entity);

		i18nComposite.setEntity(entity);

		visibleConditionGroup.setCondition( entity.getVisibleCondition() );
		visibleConditionGroup.setDescription( entity.getVisibleConditionDescription() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
			}

			i18nComposite.syncEntityToWidgets();

			entity.setReportDefinitionId(reportDefinitionIdSpinner.getValue());

			entity.setProgrammePointIdList( ppListComposite.getProgrammePointIds() );

			entity.setVisibleCondition( visibleConditionGroup.getCondition() );
			entity.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	private void buildReportDefinitionId(Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText(CertificateComponent.FIELD_REPORT_DEFINITION_ID.getString());

		reportDefinitionIdSpinner = new NullableSpinner(parent, SWT.NONE);
		GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT - 1, 1)
			.applyTo(reportDefinitionIdSpinner);
		reportDefinitionIdSpinner.addModifyListener(modifySupport);
	}


	private void buildI18NComposite(Composite parent) {
		i18nComposite = new I18NComposite<>(
			parent,
			SWT.BORDER,
			languageList,
			new CertificateComponentCompositeI18NWidgetController()
		);

		GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT, 1)
			.applyTo(i18nComposite);

		i18nComposite.addModifyListener(modifySupport);
	}


	private void buildProgrammePoints(Composite parent) {
		ppListComposite = new ProgrammePointListComposite(parent, SWT.NONE, portal.getId());

		GridDataFactory
	    	.fillDefaults()
	    	.grab(true, true)
	    	.span(COL_COUNT, 1)
			.applyTo(ppListComposite);

		ppListComposite.addModifyListener(modifySupport);
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
    		htmlIdText.setEnabled(!fixedStructure);
		}
	}
}
