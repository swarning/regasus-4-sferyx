package de.regasus.portal.page.editor.paymentWithFeeComponent;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.i18n.I18NHtmlWidget;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.PaymentWithFeeComponent;
import de.regasus.programme.programmepoint.combo.ProgrammePointCombo;

public class PaymentWithFeeComponentOnlinePaymentComposite extends EntityComposite<PaymentWithFeeComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;

	private Long eventId;

	// **************************************************************************
	// * Widgets
	// *

	private Button showOnlinePaymentButton;

	private ProgrammePointCombo programmePointCombo;

	private DecimalNumberText onlinePaymentPercentValueNumberText;

	private I18NComposite<PaymentWithFeeComponent> i18nComposite;

	private I18NHtmlWidget onlinePaymentTextI18NHtmlWidget;

	// *
	// * Widgets
	// **************************************************************************


	public PaymentWithFeeComponentOnlinePaymentComposite(
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
		this.eventId = portal.getEventId();
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		buildShowOnlinePaymentButton(parent);
		buildProgrammePointCombo(parent);
		onlinePaymentPercentValueNumberText(parent);
		buildI18NWidgets(parent);
		buildI18NHtmlEditor(parent);
	}


	private void buildShowOnlinePaymentButton(Composite parent) {
		new Label(parent, SWT.NONE); // placeholder

		showOnlinePaymentButton = new Button(parent, SWT.CHECK);
		showOnlinePaymentButton.setText( PaymentWithFeeComponent.SHOW_ONLINE_PAYMENT.getString());
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(showOnlinePaymentButton);
		showOnlinePaymentButton.addSelectionListener(modifySupport);
	}


	private void buildProgrammePointCombo(Composite parent) throws Exception {
		SWTHelper.createLabel(parent, PaymentWithFeeComponent.ONLINE_PAYMENT_PROGRAMME_POINT_ID.getString());

		programmePointCombo = new ProgrammePointCombo(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(programmePointCombo);
		programmePointCombo.setEventPK(eventId);
		programmePointCombo.addModifyListener(modifySupport);
	}


	private void onlinePaymentPercentValueNumberText(Composite parent) {
		SWTHelper.createLabel(parent, PaymentWithFeeComponent.ONLINE_PAYMENT_PERCENT_VALUE.getString());

		onlinePaymentPercentValueNumberText = new DecimalNumberText(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(onlinePaymentPercentValueNumberText);
		onlinePaymentPercentValueNumberText.setFractionDigits(1);
		onlinePaymentPercentValueNumberText.setNullAllowed(true);
		onlinePaymentPercentValueNumberText.setShowPercent(true);
		onlinePaymentPercentValueNumberText.setMinValue(0);
		onlinePaymentPercentValueNumberText.setMaxValue(100);
		WidgetSizer.setWidth(onlinePaymentPercentValueNumberText);
		onlinePaymentPercentValueNumberText.addModifyListener(modifySupport);
	}


	private void buildI18NWidgets(Composite parent) throws Exception {
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new OnlinePaymentLabelI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(SWT.DEFAULT, 20).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	private void buildI18NHtmlEditor(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory
			.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER)
			.span(COL_COUNT, 1)
			.indent(0, 10)
			.applyTo(label);

		label.setText(PaymentWithFeeComponent.ONLINE_PAYMENT_TEXT.getString());

		onlinePaymentTextI18NHtmlWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		GridDataFactory
			.fillDefaults()
			.grab(true, true)
			.span(COL_COUNT, 1)
			.applyTo(onlinePaymentTextI18NHtmlWidget);

		onlinePaymentTextI18NHtmlWidget.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		showOnlinePaymentButton.setSelection( entity.isShowOnlinePayment() );
		programmePointCombo.setProgrammePointPK( entity.getOnlinePaymentProgrammePointId() );
		onlinePaymentPercentValueNumberText.setValue( entity.getOnlinePaymentPercentValue() );
		i18nComposite.setEntity(entity);
		onlinePaymentTextI18NHtmlWidget.setLanguageString( entity.getOnlinePaymentText() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setShowOnlinePayment( showOnlinePaymentButton.getSelection() );
			entity.setOnlinePaymentProgrammePointId( programmePointCombo.getProgrammePointPK() );
			entity.setOnlinePaymentPercentValue( onlinePaymentPercentValueNumberText.getValue() );
			i18nComposite.syncEntityToWidgets();
			entity.setOnlinePaymentText( onlinePaymentTextI18NHtmlWidget.getLanguageString() );
		}
	}

}
