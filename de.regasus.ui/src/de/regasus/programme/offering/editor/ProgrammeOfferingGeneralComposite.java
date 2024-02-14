package de.regasus.programme.offering.editor;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.*;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.ProgrammeConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.PriceGroup;
import de.regasus.participant.type.combo.ParticipantTypeCombo;
import de.regasus.programme.ProgrammeCancelationTermModel;
import de.regasus.ui.Activator;

public class ProgrammeOfferingGeneralComposite extends Composite {

	// the entity
	private ProgrammeOfferingVO programmeOfferingVO;

	// ConfigParameterSet
	private ProgrammeConfigParameterSet programmeConfigParameterSet;

	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private I18NComposite<ProgrammeOfferingVO> i18nComposite;
	private Button onlineAvailableButton;
	private Button disabledButton;
	private Text tagsText;
	private Text referenceCodeText;
	private DateTimeComposite validFromDTC;
	private DateTimeComposite validToDTC;
	private ParticipantTypeCombo participantTypeCombo;
	private NullableSpinner maxSpinner;
	private Button priceEditableButton;

	private PriceGroup mainPriceGroup;
	private Button withAdd1Price;
	private Button withAdd2Price;
	private AdditionalProgrammePriceGroup add1PriceGroup;
	private AdditionalProgrammePriceGroup add2PriceGroup;

	private GridData add1PriceData;
	private GridData add2PriceData;


	private ModifyListener mainPriceGroupModifyListener = new ModifyListener() {
		/* previous values of currency and brutto
		 * Necessary to check which of them has changed
		 */
		String lastCurrency = "";
		Boolean lastBrutto = null;

		@Override
		public void modifyText(ModifyEvent event) {
			// get values to update
			String newCurrency = mainPriceGroup.getCurrency();
			boolean newBrutto = mainPriceGroup.isGross();

			// show info dialog if user has changed brutto to a value different than the original one
			if (newBrutto != programmeOfferingVO.isGross() &&
				(lastBrutto == null || newBrutto != lastBrutto)
			) {
				try {
					Long offeringPK = programmeOfferingVO.getID();
					ProgrammeCancelationTermModel pctModel = ProgrammeCancelationTermModel.getInstance();
					List<ProgrammeCancelationTermVO> pctVOs = pctModel.getProgrammeCancelationTermVOsByProgrammeOfferingPK(offeringPK);
					if ( notEmpty(pctVOs) ) {
						MessageDialog.openInformation(
							getShell(),
							UtilI18N.Info,
							I18N.OfferingEditor_ChangeBruttoMessage
						);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}

			lastBrutto = newBrutto;


			// show info dialog if user has changed currency to a value different than the original one
			if (   programmeOfferingVO.getCurrency() != null
				&& !newCurrency.equals(programmeOfferingVO.getCurrency())
				&& !newCurrency.equals(lastCurrency)
			) {
				try {
					Long offeringPK = programmeOfferingVO.getID();
					ProgrammeCancelationTermModel pctModel = ProgrammeCancelationTermModel.getInstance();
					List<ProgrammeCancelationTermVO> pctVOs = pctModel.getProgrammeCancelationTermVOsByProgrammeOfferingPK(offeringPK);
					if ( notEmpty(pctVOs) ) {
						MessageDialog.openInformation(
							getShell(),
							UtilI18N.Info,
							I18N.OfferingEditor_ChangeCurrencyMessage
						);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}

			lastCurrency = newCurrency;


			if (programmeConfigParameterSet.getAdditionalPrice().isVisible()) {
				{
        			// update currency and brutto in add1 price
        			add1PriceGroup.getPriceVO().setCurrency(newCurrency);
        			add1PriceGroup.getPriceVO().setGross(newBrutto);

        			// update widgets in add1PriceGroup
        			add1PriceGroup.setCurrency(newCurrency);
        			add1PriceGroup.setGross(newBrutto);
        			add1PriceGroup.refreshAmounts();
				}

				{
        			// update currency and brutto in add2 price
        			add2PriceGroup.getPriceVO().setCurrency(newCurrency);
        			add2PriceGroup.getPriceVO().setGross(newBrutto);

        			// update widgets in add2PriceGroup
        			add2PriceGroup.setCurrency(newCurrency);
        			add2PriceGroup.setGross(newBrutto);
        			add2PriceGroup.refreshAmounts();
				}
			}
		}

	};


	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ProgrammeOfferingGeneralComposite(
		Composite parent,
		int style,
		Long eventPK,
		List<Language> languageList,
		ProgrammeConfigParameterSet programmeConfigParameterSet
	) {
		super(parent, style);

		// init ConfigParameterSet
		if (programmeConfigParameterSet == null) {
			programmeConfigParameterSet = new ProgrammeConfigParameterSet();
		}
		this.programmeConfigParameterSet = programmeConfigParameterSet;


		try {
			final int NUM_COLS = 4;
			setLayout(new GridLayout(NUM_COLS, false));


			/* Row 1
			 */

			i18nComposite = new I18NComposite<>(this, SWT.BORDER, languageList, new ProgrammeOfferingI18NWidgetController());
			GridDataFactory.fillDefaults().span(NUM_COLS, 1).grab(true, false).applyTo(i18nComposite);
			i18nComposite.addModifyListener(modifySupport);


			/* Row 2
			 */

			// useInOnlineForm
			new Label(this, SWT.NONE);
			{
				onlineAvailableButton = new Button(this, SWT.CHECK);
				onlineAvailableButton.setText( ParticipantLabel.ProgrammeOffering_OnlineAvailable.getString() );
				onlineAvailableButton.setToolTipText( ParticipantLabel.ProgrammeOffering_OnlineAvailable_desc.getString() );
				onlineAvailableButton.addSelectionListener(modifySupport);
			}

			// disabled
			new Label(this, SWT.NONE);
			{
				disabledButton = new Button(this, SWT.CHECK);
				disabledButton.setText( ParticipantLabel.ProgrammeOffering_Disabled.getString() );
				disabledButton.addSelectionListener(modifySupport);
			}


			/* Row 3
			 */

			// Tags
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
				label.setText( ParticipantLabel.ProgrammeOffering_Tags.getString() );
				label.setToolTipText( ParticipantLabel.ProgrammeOffering_Tags_Desc.getString() );

				tagsText = new Text(this, SWT.BORDER);
				tagsText.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
				tagsText.addModifyListener(modifySupport);
			}

			// Reference Code
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
				label.setText( ParticipantLabel.ProgrammeOffering_ReferenceCode.getString() );
				label.setToolTipText( ParticipantLabel.ProgrammeOffering_ReferenceCode_Desc.getString() );

				referenceCodeText = new Text(this, SWT.BORDER);
				referenceCodeText.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
				referenceCodeText.addModifyListener(modifySupport);
			}


			/*
			 * Row 4
			 */

			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText( ParticipantLabel.ProgrammeOffering_StartTime.getString() );

				validFromDTC = new DateTimeComposite(this, SWT.BORDER);
				validFromDTC.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false) );
			 	WidgetSizer.setWidth(validFromDTC);

			 	validFromDTC.addModifyListener(modifySupport);
			}

			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText( ParticipantLabel.ProgrammeOffering_EndTime.getString() );

				validToDTC = new DateTimeComposite(this, SWT.BORDER);
				validToDTC.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false) );
				WidgetSizer.setWidth(validToDTC);

				validToDTC.addModifyListener(modifySupport);
			}


			/*
			 * Row 5
			 */

			// Participant Type
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				label.setText(ParticipantLabel.ProgrammeOffering_ParticipantType.getString());

				participantTypeCombo = new ParticipantTypeCombo(this, SWT.READ_ONLY);
				participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

				participantTypeCombo.addModifyListener(modifySupport);
			}

			// Max
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(ParticipantLabel.ProgrammeOffering_MaxNumber.getString());

				maxSpinner = new NullableSpinner(this, SWT.NONE);
				maxSpinner.setMinimum(ProgrammeOfferingVO.MIN_MAX_NUMBER);
				maxSpinner.setMaximum(ProgrammeOfferingVO.MAX_MAX_NUMBER);
				maxSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

				maxSpinner.addModifyListener(modifySupport);
			}


			/*
			 * Row 6
			 */
			new Label(this, SWT.NONE);
			{
				priceEditableButton = new Button(this, SWT.CHECK);
				priceEditableButton.setSelection(true);
				priceEditableButton.setText(ParticipantLabel.ProgrammeOffering_PriceEditable.getString());

				priceEditableButton.addSelectionListener(modifySupport);
			}

			new Label(this, SWT.NONE);
			new Label(this, SWT.NONE);


			/*
			 * Row 7
			 */
			{
				mainPriceGroup = new PriceGroup(this, SWT.NONE, eventPK);
				{
					GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
					mainPriceGroup.setLayoutData(gridData);

					mainPriceGroup.addModifyListener(modifySupport);
				}
			}


			/*
			 * Row 8
			 */
			if (programmeConfigParameterSet.getAdditionalPrice().isVisible()) {
				{
					Composite withAdditionalComposite = new Composite(this, SWT.NONE);
					withAdditionalComposite.setLayout(new GridLayout(2, false));
					withAdditionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

        			withAdd1Price = new Button(withAdditionalComposite, SWT.CHECK);
        			withAdd1Price.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        			withAdd1Price.setText(InvoiceLabel.Add1Price.getString());

        			withAdd1Price.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent e) {
        					programmeOfferingVO.setWithAdd1Price(withAdd1Price.getSelection());
        					setWithAdd1Price(programmeOfferingVO.isWithAdd1Price());
        				}
        			});
        			withAdd1Price.addSelectionListener(modifySupport);



        			withAdd2Price = new Button(withAdditionalComposite, SWT.CHECK);
        			withAdd2Price.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        			withAdd2Price.setText(InvoiceLabel.Add2Price.getString());

        			withAdd2Price.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent e) {
        					programmeOfferingVO.setWithAdd2Price(withAdd2Price.getSelection());
        					setWithAdd2Price(programmeOfferingVO.isWithAdd2Price());
        				}
        			});
        			withAdd2Price.addSelectionListener(modifySupport);
				}

    			{
        			// row 6, col 1 - 4
        			add1PriceGroup = new AdditionalProgrammePriceGroup(
        				this,
        				SWT.NONE,
        				eventPK,
        				InvoiceLabel.Add1Price.getString()
        			) {
        				@Override
        				protected void syncAdditionalWidgetToEntity() {
        					names.setLanguageString(programmeOfferingVO.getAdd1PriceName());
        				}


        				@Override
        				protected void syncEntityToAdditionalWidget() {
							programmeOfferingVO.setAdd1PriceName(names.getLanguageString());
        				}
        			};
        			add1PriceData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
        			add1PriceGroup.setLayoutData(add1PriceData);
        			add1PriceGroup.setEnabledCurrencyCombo(false);
        			add1PriceGroup.setEnabledGrossButton(false);
        			add1PriceGroup.setEnabledNetButton(false);

        			add1PriceGroup.addModifyListener(modifySupport);
    			}

    			{
        			add2PriceGroup = new AdditionalProgrammePriceGroup(
        				this,
        				SWT.NONE,
        				eventPK,
        				InvoiceLabel.Add2Price.getString()
        			) {
        				@Override
        				protected void syncAdditionalWidgetToEntity() {
        					names.setLanguageString(programmeOfferingVO.getAdd2PriceName());
        				}


        				@Override
        				protected void syncEntityToAdditionalWidget() {
        					programmeOfferingVO.setAdd2PriceName(names.getLanguageString());
        				}
        			};
        			add2PriceData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
        			add2PriceGroup.setLayoutData(add2PriceData);
        			add2PriceGroup.setEnabledCurrencyCombo(false);
        			add2PriceGroup.setEnabledGrossButton(false);
        			add2PriceGroup.setEnabledNetButton(false);

        			add2PriceGroup.addModifyListener(modifySupport);
    			}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	protected void setWithAdd1Price(boolean withAdd1Price) {
		if (programmeConfigParameterSet.getAdditionalPrice().isVisible()) {
    		if (withAdd1Price) {
    			add1PriceData.exclude = false;
    			add1PriceGroup.setVisible(true);
    		}
    		else {
    			add1PriceData.exclude = true;
    			add1PriceGroup.setVisible(false);
    		}

    		add1PriceGroup.setMinimize(withAdd1Price);

    		this.layout();
    		SWTHelper.refreshSuperiorScrollbar(this);
		}
	}


	protected void setWithAdd2Price(boolean withAdd2Price) {
		if (programmeConfigParameterSet.getAdditionalPrice().isVisible()) {
    		if (withAdd2Price) {
    			add2PriceData.exclude = false;
    			add2PriceGroup.setVisible(true);
    		}
    		else {
    			add2PriceData.exclude = true;
    			add2PriceGroup.setVisible(false);
    		}

    		add2PriceGroup.setMinimize(withAdd2Price);


    		this.layout();

    		SWTHelper.refreshSuperiorScrollbar(this);
		}
	}

	// *
	// * Modifying
	// **************************************************************************

	public void setProgrammeOfferingVO(ProgrammeOfferingVO programmeOfferingVO) {
		this.programmeOfferingVO = programmeOfferingVO;
		mainPriceGroup.setPriceVO(programmeOfferingVO.getMainPriceVO());
		syncWidgetsToEntity();

		// observe mainPriceGroup not before first initialization
		mainPriceGroup.addModifyListener(mainPriceGroupModifyListener);
	}


	private void syncWidgetsToEntity() {
		if (programmeOfferingVO != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						i18nComposite.setEntity(programmeOfferingVO);

						onlineAvailableButton.setSelection( programmeOfferingVO.isOnlineAvailable() );
						disabledButton.setSelection(programmeOfferingVO.isDisabled());
						tagsText.setText( avoidNull(programmeOfferingVO.getTags()) );
						referenceCodeText.setText( avoidNull(programmeOfferingVO.getReferenceCode()) );
						validFromDTC.setDate( programmeOfferingVO.getStartTime() );
						validToDTC.setDate( programmeOfferingVO.getEndTime() );

						// To initialize the ParticipantTypeCombo we have to set the eventPK, too
						participantTypeCombo.setEventID(programmeOfferingVO.getEventPK());
						participantTypeCombo.setParticipantTypePK( programmeOfferingVO.getParticipantTypePK() );


						maxSpinner.setValue(programmeOfferingVO.getMaxNumber());
						priceEditableButton.setSelection(programmeOfferingVO.isPriceEditable());

						mainPriceGroup.setPriceVO(programmeOfferingVO.getMainPriceVO());

						if (programmeConfigParameterSet.getAdditionalPrice().isVisible()) {
    						withAdd1Price.setSelection(programmeOfferingVO.isWithAdd1Price());
    						add1PriceGroup.setPriceVO(programmeOfferingVO.getAdd1PriceVO());
    						setWithAdd1Price(programmeOfferingVO.isWithAdd1Price());

    						withAdd2Price.setSelection(programmeOfferingVO.isWithAdd2Price());
    						add2PriceGroup.setPriceVO(programmeOfferingVO.getAdd2PriceVO());
    						setWithAdd2Price(programmeOfferingVO.isWithAdd2Price());
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		if (programmeOfferingVO != null) {
			i18nComposite.syncEntityToWidgets();
			programmeOfferingVO.setOnlineAvailable( onlineAvailableButton.getSelection() );
			programmeOfferingVO.setDisabled(disabledButton.getSelection());
			programmeOfferingVO.setTags( trim(tagsText.getText()) );
			programmeOfferingVO.setReferenceCode( trim(referenceCodeText.getText()) );
			programmeOfferingVO.setStartTime(validFromDTC.getDate());
			programmeOfferingVO.setEndTime(validToDTC.getDate());
			programmeOfferingVO.setParticipantTypePK(participantTypeCombo.getParticipantTypePK());
			programmeOfferingVO.setMaxNumber(maxSpinner.getValueAsInteger());
			programmeOfferingVO.setPriceEditable(priceEditableButton.getSelection());

			mainPriceGroup.syncEntityToWidgets();


			// set currency and brutto/gross for all prices even if additional prices are not visible
			programmeOfferingVO.setCurrency( mainPriceGroup.getCurrency() );
			programmeOfferingVO.setGross( mainPriceGroup.isGross() );


			// set Currency for all prices even if additional prices are not visible
			programmeOfferingVO.setCurrency( programmeOfferingVO.getMainPriceVO().getCurrency() );

			if (programmeConfigParameterSet.getAdditionalPrice().isVisible()) {
    			if (!withAdd1Price.getSelection() && add1PriceGroup.getCurrency() == null) {
					add1PriceGroup.setCurrency(mainPriceGroup.getCurrency());
				}
    			add1PriceGroup.syncEntityToWidgets();
    			programmeOfferingVO.setWithAdd1Price(withAdd1Price.getSelection());

    			if (!withAdd2Price.getSelection() && add2PriceGroup.getCurrency() == null) {
					add2PriceGroup.setCurrency(mainPriceGroup.getCurrency());
				}
    			add2PriceGroup.syncEntityToWidgets();
    			programmeOfferingVO.setWithAdd2Price(withAdd2Price.getSelection());
			}
		}
	}

}
