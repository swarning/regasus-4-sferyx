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

public class PaymentWithFeeComponentBankTransferComposite extends EntityComposite<PaymentWithFeeComponent> {

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

	private Button showBankTransferButton;

	private ProgrammePointCombo programmePointCombo;

	private DecimalNumberText bankTransferPercentValueNumberText;

	private I18NComposite<PaymentWithFeeComponent> bankTransferLabelI18NComposite;

	private I18NHtmlWidget bankTransferTextI18NHtmlWidget;

	// *
	// * Widgets
	// **************************************************************************


	public PaymentWithFeeComponentBankTransferComposite(
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

		buildShowBankTransferButton(parent);
		buildProgrammePointCombo(parent);
		bankTransferPercentValueNumberText(parent);
		buildI18NWidgets(parent);
		buildI18NHtmlEditor(parent);
	}


	private void buildShowBankTransferButton(Composite parent) {
		new Label(parent, SWT.NONE); // placeholder

		showBankTransferButton = new Button(parent, SWT.CHECK);
		showBankTransferButton.setText( PaymentWithFeeComponent.SHOW_BANK_TRANSFER.getString());
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(showBankTransferButton);
		showBankTransferButton.addSelectionListener(modifySupport);
	}


	private void buildProgrammePointCombo(Composite parent) throws Exception {
		SWTHelper.createLabel(parent, PaymentWithFeeComponent.BANK_TRANSFER_PROGRAMME_POINT_ID.getString());

		programmePointCombo = new ProgrammePointCombo(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(programmePointCombo);
		programmePointCombo.setEventPK(eventId);
		programmePointCombo.addModifyListener(modifySupport);
	}


	private void bankTransferPercentValueNumberText(Composite parent) {
		SWTHelper.createLabel(parent, PaymentWithFeeComponent.BANK_TRANSFER_PERCENT_VALUE.getString());

		bankTransferPercentValueNumberText = new DecimalNumberText(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(bankTransferPercentValueNumberText);
		bankTransferPercentValueNumberText.setFractionDigits(1);
		bankTransferPercentValueNumberText.setNullAllowed(true);
		bankTransferPercentValueNumberText.setShowPercent(true);
		bankTransferPercentValueNumberText.setMinValue(0);
		bankTransferPercentValueNumberText.setMaxValue(100);
		WidgetSizer.setWidth(bankTransferPercentValueNumberText);
		bankTransferPercentValueNumberText.addModifyListener(modifySupport);
	}


	private void buildI18NWidgets(Composite parent) throws Exception {
		bankTransferLabelI18NComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new BankTransferLabelI18NWidgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT, 1).indent(SWT.DEFAULT, 20).applyTo(bankTransferLabelI18NComposite);
		bankTransferLabelI18NComposite.addModifyListener(modifySupport);
	}


	private void buildI18NHtmlEditor(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory
			.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER)
			.span(COL_COUNT, 1)
			.indent(0, 10)
			.applyTo(label);

		label.setText(PaymentWithFeeComponent.BANK_TRANSFER_TEXT.getString());

		bankTransferTextI18NHtmlWidget = new I18NHtmlWidget(parent, SWT.BORDER, languageList);
		GridDataFactory
			.fillDefaults()
			.grab(true, true)
			.span(COL_COUNT, 1)
			.applyTo(bankTransferTextI18NHtmlWidget);

		bankTransferTextI18NHtmlWidget.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		showBankTransferButton.setSelection( entity.isShowBankTransfer() );
		programmePointCombo.setProgrammePointPK( entity.getBankTransferProgrammePointId() );
		bankTransferPercentValueNumberText.setValue( entity.getBankTransferPercentValue() );
		bankTransferLabelI18NComposite.setEntity(entity);
		bankTransferTextI18NHtmlWidget.setLanguageString( entity.getBankTransferText() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setShowBankTransfer( showBankTransferButton.getSelection() );
			entity.setBankTransferProgrammePointId( programmePointCombo.getProgrammePointPK() );
			entity.setBankTransferPercentValue( bankTransferPercentValueNumberText.getValue() );
			bankTransferLabelI18NComposite.syncEntityToWidgets();
			entity.setBankTransferText( bankTransferTextI18NHtmlWidget.getLanguageString() );
		}
	}

}
