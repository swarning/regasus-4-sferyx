package de.regasus.portal.page.editor.paymentWithFeeComponent;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.PaymentWithFeeComponent;
import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.portal.page.editor.ConditionGroup;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.portal.page.editor.ProgrammePointListComposite;
import de.regasus.users.CurrentUserModel;

public class PaymentWithFeeComponentGeneralComposite extends EntityComposite<PaymentWithFeeComponent> {

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

	private Button refersToProgrammeBookingsButton;
	private Button refersToHotelBookingsButton;

	private ProgrammePointListComposite ppListComposite;

	private I18NComposite<PaymentWithFeeComponent> i18nComposite;

	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public PaymentWithFeeComponentGeneralComposite(
		Composite parent,
		int style,
		Long portalPK
	)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK));
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

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildCurrency(parent);

		buildRefersToProgrammeBookingsButton(parent);
		buildRefersToHotelBookingsButton(parent);

		buildProgrammePointTypes(parent);

		buildI18NWidgets(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	private void buildCurrency(Composite parent) throws Exception {
   		SWTHelper.createLabel(parent, ProgrammeBookingComponent.FIELD_CURRENCY.getString(), true);

   		currencyCombo = new CurrencyCombo(this, SWT.BORDER);
   		GridDataFactory.swtDefaults().grab(true, false).applyTo(currencyCombo);
		SWTHelper.makeBold(currencyCombo);
		currencyCombo.addModifyListener(modifySupport);
	}


	private void buildRefersToProgrammeBookingsButton(Composite parent) {
		new Label(parent, SWT.NONE); // placeholder

		refersToProgrammeBookingsButton = new Button(parent, SWT.CHECK);
		refersToProgrammeBookingsButton.setText( PaymentWithFeeComponent.REFERS_TO_PROGRAMME_BOOKINGS.getString());
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(refersToProgrammeBookingsButton);
		refersToProgrammeBookingsButton.addSelectionListener(modifySupport);
	}


	private void buildRefersToHotelBookingsButton(Composite parent) {
		new Label(parent, SWT.NONE); // placeholder

		refersToHotelBookingsButton = new Button(parent, SWT.CHECK);
		refersToHotelBookingsButton.setText( PaymentWithFeeComponent.REFERS_TO_HOTEL_BOOKINGS.getString());
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(refersToHotelBookingsButton);
		refersToHotelBookingsButton.addSelectionListener(modifySupport);
	}


	private void buildProgrammePointTypes(Composite parent) {
		ppListComposite = new ProgrammePointListComposite(parent, SWT.NONE, portal.getId());
		GridDataFactory
			.fillDefaults()
			.grab(true, true)
			.span(COL_COUNT, 1)
			.applyTo(ppListComposite);

		ppListComposite.addModifyListener(modifySupport);
	}


	private void buildI18NWidgets(Composite parent) throws Exception {
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new PaymentTypeLabelI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(SWT.DEFAULT, 20).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}



	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		currencyCombo.setCurrencyCode( entity.getCurrency() );

		refersToProgrammeBookingsButton.setSelection( entity.isRefersToProgrammeBookings() );
		refersToHotelBookingsButton.setSelection( entity.isRefersToHotelBookings() );

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
				entity.setRender( renderText.getText() );
			}

			entity.setCurrency( currencyCombo.getCurrencyCode() );

			entity.setRefersToProgrammeBookings( refersToProgrammeBookingsButton.getSelection() );
			entity.setRefersToHotelBookings( refersToHotelBookingsButton.getSelection() );

			entity.setProgrammePointIdList( ppListComposite.getProgrammePointIds() );

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
