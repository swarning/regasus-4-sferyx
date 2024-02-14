package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.ProgrammeBookingComponent;


public class ProgrammeBookingComponentBookingRulesComposite extends EntityComposite<ProgrammeBookingComponent> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private I18NComposite<ProgrammeBookingComponent> i18nComposite;
	private MultiLineText bookingRulesDescriptionText;
	private MultiLineText bookingRulesText;

	private GridDataFactory multiLineLabelGridDataFactory;
	private GridDataFactory multiLineTextGridDataFactory;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammeBookingComponentBookingRulesComposite(
		Composite parent,
		int style,
		Long portalPK
	)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);

	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		// determine Portal languages
		Long portalPK = (Long) initValues[0];
		Portal portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		multiLineLabelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.TOP)
			.indent(SWT.DEFAULT, SWTConstants.VERTICAL_INDENT);

		multiLineTextGridDataFactory = GridDataFactory
			.fillDefaults()
			.grab(true, false);

		setLayout( new GridLayout(COL_COUNT, false) );

		buildBookingRulesDescription(parent);
		buildBookingRules(parent);
		buildI18NWidgets(parent);
	}


	private void buildBookingRulesDescription(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ProgrammeBookingComponent.FIELD_BOOKING_RULES_DESCRIPTION.getString());
		multiLineLabelGridDataFactory.applyTo(label);

		bookingRulesDescriptionText = new MultiLineText(parent, SWT.BORDER);
		multiLineTextGridDataFactory.applyTo(bookingRulesDescriptionText);
		bookingRulesDescriptionText.setTextLimit( ProgrammeBookingComponent.FIELD_BOOKING_RULES_DESCRIPTION.getMaxLength() );
		bookingRulesDescriptionText.addModifyListener(modifySupport);
	}


	private void buildBookingRules(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText( ProgrammeBookingComponent.FIELD_BOOKING_RULES.getString() );
		multiLineLabelGridDataFactory.applyTo(label);

		bookingRulesText = new MultiLineText(parent, SWT.BORDER);
		multiLineTextGridDataFactory.applyTo(bookingRulesText);
		bookingRulesText.setTextLimit( ProgrammeBookingComponent.FIELD_BOOKING_RULES.getMaxLength() );
		bookingRulesText.addModifyListener(modifySupport);
	}


	private void buildI18NWidgets(Composite parent) throws Exception {
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new BookingCountI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(SWT.DEFAULT, 20).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		i18nComposite.setEntity(entity);
		bookingRulesText.setText( avoidNull(entity.getBookingRules()) );
		bookingRulesDescriptionText.setText( avoidNull(entity.getBookingRulesDescription()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			i18nComposite.syncEntityToWidgets();
			entity.setBookingRules( bookingRulesText.getText() );
			entity.setBookingRulesDescription( bookingRulesDescriptionText.getText() );
		}
	}

}
