package de.regasus.hotel.offering.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
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

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.finance.PriceGroup;
import de.regasus.hotel.HotelCancelationTermModel;
import de.regasus.hotel.roomdefinition.combo.RoomDefinitionCombo;
import de.regasus.ui.Activator;

public class HotelOfferingGeneralComposite extends Composite {

	// the entity
	private HotelOfferingVO hotelOfferingVO;

	// ConfigParameterSet
	private HotelConfigParameterSet hotelConfigParameterSet;


	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private RoomDefinitionCombo roomDefinitionCombo;

	private NullableSpinner guestCountSpinner;

	private I18NText descriptionI18NText;

	private PriceGroup lodgePriceGroup;

	private Button withBfPrice;
	private Button withAdd1Price;
	private Button withAdd2Price;

	private PriceGroup bfPriceGroup;
	private AdditionalHotelPriceGroup add1PriceGroup;
	private AdditionalHotelPriceGroup add2PriceGroup;

	private DecimalNumberText commissionableAmountNumberText;

	private Label commissionableCurrencyLabel;

	private DecimalNumberText depositNumberText;

	private Label depositCurrencyLabel;

	private PaymentTypesComposite allowedPaymentTypesComposite;

	private PaymentTypesComposite defaultPaymentTypeComposite;

	private GridData bfPriceData;
	private GridData add1PriceData;
	private GridData add2PriceData;


	/**
	 * Wird auf true gesetzt, wenn der Nutzer im Feld description eine
	 * Eingabe macht oder die gesetzten Daten eine nicht leere description
	 * beinhalten.
	 * Die Variable wird dafür benötigt, dass die description automat. gesetzt wird,
	 * wenn der Nutzer eine RoomDefinition auswählt. Der Automatismus soll aber nur
	 * greifen, solange die description leer ist und der Nutzer sie noch nicht manuell
	 * verändert hat.
	 */
	private boolean descriptionManuallyChanged = false;


	private ModifyListener lodgePriceGroupModifyListener = new ModifyListener() {
		/* previous values of currency and brutto
		 * Necessary to check which of them has changed
		 */
		String lastCurrency = "";
		Boolean lastBrutto = null;

		@Override
		public void modifyText(ModifyEvent event) {
			// get values to update
			String newCurrency = lodgePriceGroup.getCurrency();
			boolean newBrutto = lodgePriceGroup.isGross();

			// show info dialog if user has changed brutto to a value different than the original one
			if (newBrutto != hotelOfferingVO.isGross() &&
				(lastBrutto == null || newBrutto != lastBrutto)
			) {
				try {
					Long offeringPK = hotelOfferingVO.getID();
					HotelCancelationTermModel hctModel = HotelCancelationTermModel.getInstance();
					List<HotelCancelationTermVO> hctVOs = hctModel.getHotelCancelationTermVOsByHotelOfferingPK(offeringPK);
					if (CollectionsHelper.notEmpty(hctVOs)) {
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
			if (   ! EqualsHelper.isEqual(newCurrency, hotelOfferingVO.getCurrency())
				&& ! EqualsHelper.isEqual(newCurrency, lastCurrency)
			) {
				try {
					Long offeringPK = hotelOfferingVO.getID();
					HotelCancelationTermModel hctModel = HotelCancelationTermModel.getInstance();
					List<HotelCancelationTermVO> hctVOs = hctModel.getHotelCancelationTermVOsByHotelOfferingPK(offeringPK);
					if (CollectionsHelper.notEmpty(hctVOs)) {
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

			if (hotelConfigParameterSet.getAdditionalPrice().isVisible()) {
				{
        			// update currency and brutto in breakfast price
        			bfPriceGroup.getPriceVO().setCurrency(newCurrency);
        			bfPriceGroup.getPriceVO().setGross(newBrutto);

        			// update widgets in bfPriceGroup
        			bfPriceGroup.setCurrency(newCurrency);
        			bfPriceGroup.setGross(newBrutto);
        			bfPriceGroup.refreshAmounts();
				}

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

			// update currency widgets
			commissionableCurrencyLabel.setText( avoidNull(newCurrency) );
			depositCurrencyLabel.setText( avoidNull(newCurrency) );

			/* We need to call layout by the parent of commissionableCurrencyLabel (it is the parent
			 * of depositCurrencyLabel too), because the labels won't be visible if there is no
			 * currency the first time they are rendered, e.g. when a new hotel offering is created.
			 */
			commissionableCurrencyLabel.getParent().layout();
		}
	};


	/**
	 * ModifyListener to observe the PaymentTypeComposite that includes check buttons.
	 */
	private ModifyListener allowedPaymentTypesModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			initEnabledDefaultPaymentCondition();
		}
	};


	/**
	 * ModifyListener to observe the PaymentTypeComposite that includes radio buttons.
	 * There is no need to ignore deselection events of radio buttons, because we are not observing the buttons
	 * directly, but the PaymentTypeComposite.
	 */
	private ModifyListener defaultPaymentTypesModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			initEnabledAllowedPaymentCondition();
		}
	};


	private void initEnabledDefaultPaymentCondition() {
		Map<HotelBookingPaymentCondition, Button> allowedPaymentTypeButtonMap = allowedPaymentTypesComposite.getCondition2ButtonMap();
		Map<HotelBookingPaymentCondition, Button> defaultPaymentTypeButtonMap = defaultPaymentTypeComposite.getCondition2ButtonMap();
		for (HotelBookingPaymentCondition paymentCondition : allowedPaymentTypeButtonMap.keySet()) {
			boolean enabled = allowedPaymentTypeButtonMap.get(paymentCondition).getSelection();
			defaultPaymentTypeButtonMap.get(paymentCondition).setEnabled(enabled);
		}
	}


	private void initEnabledAllowedPaymentCondition() {
		Map<HotelBookingPaymentCondition, Button> allowedPaymentTypeButtonMap = allowedPaymentTypesComposite.getCondition2ButtonMap();
		Map<HotelBookingPaymentCondition, Button> defaultPaymentTypeButtonMap = defaultPaymentTypeComposite.getCondition2ButtonMap();
		for (HotelBookingPaymentCondition paymentCondition : defaultPaymentTypeButtonMap.keySet()) {
			boolean enabled = defaultPaymentTypeButtonMap.get(paymentCondition).getSelection();
			allowedPaymentTypeButtonMap.get(paymentCondition).setEnabled(!enabled);
		}
	}


	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public HotelOfferingGeneralComposite(
		Composite parent,
		int style,
		Long eventPK,
		Long hotelPK,
		Long hotelContingentPK,
		HotelConfigParameterSet hotelConfigParameterSet
	) {
		super(parent, style);


		// init ConfigParameterSet
		if (hotelConfigParameterSet == null) {
			hotelConfigParameterSet = new HotelConfigParameterSet();
		}
		this.hotelConfigParameterSet = hotelConfigParameterSet;


		try {

			setLayout(new GridLayout(4, false));

			// row 1, col 1
			createLabel(this, HotelLabel.RoomDefinition.getString(), true);

			// row 1, col 2
			{
    			roomDefinitionCombo = new RoomDefinitionCombo(
    				this,
    				SWT.NONE,
    				hotelPK,
    				hotelContingentPK
    			);

				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
				roomDefinitionCombo.setLayoutData(gridData);

    			roomDefinitionCombo.addModifyListener(new ModifyListener() {
    				@Override
					public void modifyText(ModifyEvent e) {
    					adjustGuestCount();
    					copyName();
    					guestCountSpinner.setValue(guestCountSpinner.getMaximum());
    				}
    			});

    			roomDefinitionCombo.addModifyListener(modifySupport);

				// Avoid incomprehensible error messages like "Allowed values are 10024611313"
				// when a selected room definition is excluded from hotel contingent in another
				// editor, whereupon this hotel offering cannot be saved anymore (MIRCP-1626)
				roomDefinitionCombo.setKeepEntityInList(false);
			}


			// row 1, col 3
			createLabel(this, HotelLabel.HotelBooking_GuestCount.getString());

			// row 1, col 4
			guestCountSpinner = new NullableSpinner(this, SWT.BORDER);
			guestCountSpinner.setMinimum(HotelOfferingVO.MIN_BEDCOUNT);
			guestCountSpinner.setMaximum(HotelOfferingVO.MAX_BEDCOUNT);
			WidgetSizer.setWidth(guestCountSpinner);

			guestCountSpinner.addModifyListener(modifySupport);

			// row 2, col 1
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
				label.setText(UtilI18N.Description);
			}

			// row 2, col 2-4
			{
				descriptionI18NText = new I18NText(this, SWT.MULTI, LanguageProvider.getInstance());
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
				gridData.heightHint = 100;
				descriptionI18NText.setLayoutData(gridData);

				descriptionI18NText.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						descriptionManuallyChanged = true;
					}
				});

				descriptionI18NText.addModifyListener(modifySupport);
			}

			// row 3, col 1 -4
			lodgePriceGroup = new PriceGroup(this, SWT.NONE, eventPK, HotelLabel.LodgePrice.getString());
			lodgePriceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

			lodgePriceGroup.addModifyListener(modifySupport);

			if (hotelConfigParameterSet.getAdditionalPrice().isVisible()) {
				{	// row 4, col 1 -4
					Composite withAdditionalComposite = new Composite(this, SWT.NONE);
					withAdditionalComposite.setLayout(new GridLayout(3, false));
					withAdditionalComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

        			withBfPrice = new Button(withAdditionalComposite, SWT.CHECK);
        			withBfPrice.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        			withBfPrice.setText(HotelLabel.BreakfastPrice.getString());

        			withBfPrice.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent e) {
        					hotelOfferingVO.setWithBfPrice(withBfPrice.getSelection());
        					setWithBfPrice(hotelOfferingVO.isWithBfPrice());
        				}
        			});
        			withBfPrice.addSelectionListener(modifySupport);

        			withAdd1Price = new Button(withAdditionalComposite, SWT.CHECK);
        			withAdd1Price.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        			withAdd1Price.setText(InvoiceLabel.Add1Price.getString());

        			withAdd1Price.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent e) {
        					hotelOfferingVO.setWithAdd1Price(withAdd1Price.getSelection());
        					setWithAdd1Price(hotelOfferingVO.isWithAdd1Price());
        				}
        			});
        			withAdd1Price.addSelectionListener(modifySupport);

        			withAdd2Price = new Button(withAdditionalComposite, SWT.CHECK);
        			withAdd2Price.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        			withAdd2Price.setText(InvoiceLabel.Add2Price.getString());

        			withAdd2Price.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent e) {
        					hotelOfferingVO.setWithAdd2Price(withAdd2Price.getSelection());
        					setWithAdd2Price(hotelOfferingVO.isWithAdd2Price());
        				}
        			});
        			withAdd2Price.addSelectionListener(modifySupport);
				}

				{	// row 5, col 1 - 4
    				bfPriceGroup = new PriceGroup(this, SWT.NONE, eventPK, HotelLabel.BreakfastPrice.getString());
    				bfPriceData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
    				bfPriceGroup.setLayoutData(bfPriceData);
    				bfPriceGroup.setEnabledCurrencyCombo(false);
    				bfPriceGroup.setEnabledGrossButton(false);
    				bfPriceGroup.setEnabledNetButton(false);

    				bfPriceGroup.addModifyListener(modifySupport);
				}

    			{
        			// row 6, col 1 - 4
        			add1PriceGroup = new AdditionalHotelPriceGroup(this, SWT.NONE, eventPK, InvoiceLabel.Add1Price.getString()) {
        				@Override
        				protected void syncAdditionalWidgetToEntity() {
        					names.setLanguageString(hotelOfferingVO.getAdd1PriceName());
        					calcAmountWithGuestCount.setSelection(hotelOfferingVO.isCalcAdd1PriceWithGuestCount());
        					calcAmountWithNightCount.setSelection(hotelOfferingVO.isCalcAdd1PriceWithNightCount());
        				}


        				@Override
        				protected void syncEntityToAdditionalWidget() {
							hotelOfferingVO.setAdd1PriceName(names.getLanguageString());
							hotelOfferingVO.setCalcAdd1PriceWithGuestCount(calcAmountWithGuestCount.getSelection());
							hotelOfferingVO.setCalcAdd1PriceWithNightCount(calcAmountWithNightCount.getSelection());
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
        			// row 7, col 1 - 4
        			add2PriceGroup = new AdditionalHotelPriceGroup(this, SWT.NONE, eventPK, InvoiceLabel.Add2Price.getString()) {
        				@Override
        				protected void syncAdditionalWidgetToEntity() {
        					names.setLanguageString(hotelOfferingVO.getAdd2PriceName());
        					calcAmountWithGuestCount.setSelection(hotelOfferingVO.isCalcAdd2PriceWithGuestCount());
        					calcAmountWithNightCount.setSelection(hotelOfferingVO.isCalcAdd2PriceWithNightCount());
        				}


        				@Override
        				protected void syncEntityToAdditionalWidget() {
							hotelOfferingVO.setAdd2PriceName(names.getLanguageString());
							hotelOfferingVO.setCalcAdd2PriceWithGuestCount(calcAmountWithGuestCount.getSelection());
							hotelOfferingVO.setCalcAdd2PriceWithNightCount(calcAmountWithNightCount.getSelection());
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

			// row 8, col 1
			createLabel(this, HotelLabel.CommissionableAmount.getString());
			{
				// row 8, col 2 - 3
				commissionableAmountNumberText = new DecimalNumberText(this, SWT.BORDER);
				commissionableAmountNumberText.setFractionDigits(2);
				commissionableAmountNumberText.setNullAllowed(HotelOfferingVO.NULL_ALLOWED_PROVISION);
				commissionableAmountNumberText.setShowPercent(false);
				commissionableAmountNumberText.setMinValue(0);
//				commissionableAmountNumberText.setValue(0.0);
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
				gridData.widthHint = 100;
				commissionableAmountNumberText.setLayoutData(gridData);

				commissionableAmountNumberText.addModifyListener(modifySupport);

				// row 8, col 4
				commissionableCurrencyLabel = new Label(this, SWT.NONE);
			}


			// row 9, col 1
			createLabel(this, HotelLabel.HotelBooking_Deposit.getString());
			{
				// row 9, col 2 - 3
				depositNumberText = new DecimalNumberText(this, SWT.BORDER);
				depositNumberText.setFractionDigits(2);
				depositNumberText.setNullAllowed(HotelOfferingVO.NULL_ALLOWED_DEPOSIT);
				depositNumberText.setMinValue(0);
//				depositNumberText.setValue(0.0);
				GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
				layoutData.widthHint = 100;
				depositNumberText.setLayoutData(layoutData);

				depositNumberText.addModifyListener(modifySupport);

				// row 9, col 4
				depositCurrencyLabel = new Label(this, SWT.NONE);
			}

			// row 10, col 1
			createTopLabel(this, InvoiceLabel.AllowedPaymentTypes.getString());

			// row 10, col 2 - 4
			allowedPaymentTypesComposite = new PaymentTypesComposite(this, SWT.CHECK);
			allowedPaymentTypesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

			allowedPaymentTypesComposite.addModifyListener(modifySupport);
			allowedPaymentTypesComposite.addModifyListener(allowedPaymentTypesModifyListener);

			// row 11, col 1
			createTopLabel(this, InvoiceLabel.DefaultPaymentType.getString());

			// row 11, col 2 - 4
			defaultPaymentTypeComposite = new PaymentTypesComposite(this, SWT.RADIO);
			defaultPaymentTypeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

			defaultPaymentTypeComposite.addModifyListener(modifySupport);
			defaultPaymentTypeComposite.addModifyListener(defaultPaymentTypesModifyListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * When a room definition is selected, make sure that the guest count spinner doesn't show more beds
	 * then available in this room.
	 */
	protected void adjustGuestCount() {
		RoomDefinitionVO roomDefinitionVO = roomDefinitionCombo.getEntity();
		int maximumGuestCount = RoomDefinitionVO.MAX_GUEST_QUANTITY;
		if (roomDefinitionVO != null) {
			maximumGuestCount = roomDefinitionVO.getGuestQuantity();
		}
		guestCountSpinner.setMaximum(maximumGuestCount);
	}


	protected void copyName() {
		RoomDefinitionVO roomDefinitionVO = roomDefinitionCombo.getEntity();
		if (roomDefinitionVO != null) {
			if (descriptionI18NText.getLanguageString().isEmpty() || !descriptionManuallyChanged) {
				descriptionI18NText.setLanguageString(roomDefinitionVO.getName());
				descriptionManuallyChanged = false;
			}
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

	// *
	// * Modifying
	// **************************************************************************


	protected void setWithBfPrice(boolean withBfPrice) {
		if (hotelConfigParameterSet.getAdditionalPrice().isVisible()) {
    		if (withBfPrice) {
    			bfPriceData.exclude = false;
    			bfPriceGroup.setVisible(true);
    		}
    		else {
    			bfPriceData.exclude = true;
    			bfPriceGroup.setVisible(false);
    		}

    		bfPriceGroup.setMinimize(withBfPrice);

    		this.layout();
    		SWTHelper.refreshSuperiorScrollbar(this);
		}
	}


	protected void setWithAdd1Price(boolean withAdd1Price) {
		if (hotelConfigParameterSet.getAdditionalPrice().isVisible()) {
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
		if (hotelConfigParameterSet.getAdditionalPrice().isVisible()) {
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


	public void setEntity(HotelOfferingVO hotelOfferingVO) {
		this.hotelOfferingVO = hotelOfferingVO;

		syncWidgetsToEntity();

		// observe mainPriceGroup not before first initialization
		lodgePriceGroup.addModifyListener(lodgePriceGroupModifyListener);
	}


	private void syncWidgetsToEntity() {
		if (hotelOfferingVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						EventVO eventVO = EventModel.getInstance().getEventVO(hotelOfferingVO.getEventPK());

						roomDefinitionCombo.setRoomDefinitionByPK(hotelOfferingVO.getRoomDefinitionPK());

						// set the maximum, because it may have been changed
						guestCountSpinner.setMaximum(HotelOfferingVO.MAX_BEDCOUNT);
						guestCountSpinner.setValue(hotelOfferingVO.getBedCount().intValue());
						adjustGuestCount();

						LanguageString description = hotelOfferingVO.getDescription();
						descriptionI18NText.setLanguageString(description, eventVO.getLanguages());
						descriptionManuallyChanged = description != null && !description.isEmpty();

						lodgePriceGroup.setPriceVO(hotelOfferingVO.getLodgePriceVO());

						if (hotelConfigParameterSet.getAdditionalPrice().isVisible()) {
    						withBfPrice.setSelection(hotelOfferingVO.isWithBfPrice());
    						bfPriceGroup.setPriceVO(hotelOfferingVO.getBfPriceVO());
    						setWithBfPrice(hotelOfferingVO.isWithBfPrice());

    						withAdd1Price.setSelection(hotelOfferingVO.isWithAdd1Price());
    						add1PriceGroup.setPriceVO(hotelOfferingVO.getAdd1PriceVO());
    						setWithAdd1Price(hotelOfferingVO.isWithAdd1Price());

    						withAdd2Price.setSelection(hotelOfferingVO.isWithAdd2Price());
    						add2PriceGroup.setPriceVO(hotelOfferingVO.getAdd2PriceVO());
    						setWithAdd2Price(hotelOfferingVO.isWithAdd2Price());
						}

						String currency = hotelOfferingVO.getLodgePriceVO().getCurrency();
						currency = StringHelper.avoidNull(currency);
						commissionableAmountNumberText.setValue(hotelOfferingVO.getProvision());
						commissionableCurrencyLabel.setText(currency);
						depositNumberText.setValue(hotelOfferingVO.getDeposit());
						depositCurrencyLabel.setText(currency);

						// payment type groups
						defaultPaymentTypeComposite.setUniqueConditionToTrue(hotelOfferingVO.getDefaultPaymentCondition());

						allowedPaymentTypesComposite.setCondition(
							HotelBookingPaymentCondition.ABSORPTION_OF_COSTS,
							hotelOfferingVO.isPaymentConditionAbsorptionOfCosts()
						);
						allowedPaymentTypesComposite.setCondition(
							HotelBookingPaymentCondition.DEPOSIT,
							hotelOfferingVO.isPaymentConditionDeposit()
						);
						allowedPaymentTypesComposite.setCondition(
							HotelBookingPaymentCondition.BOOKING_AMOUNT,
							hotelOfferingVO.isPaymentConditionBookingAmount()
						);
						allowedPaymentTypesComposite.setCondition(
							HotelBookingPaymentCondition.SELF_PAY_PATIENT,
							hotelOfferingVO.isPaymentConditionSelfPayPatient()
						);

						initEnabledDefaultPaymentCondition();
						initEnabledAllowedPaymentCondition();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (hotelOfferingVO != null) {
			hotelOfferingVO.setRoomDefinitionPK(roomDefinitionCombo.getRoomDefinitionPK());
			hotelOfferingVO.setBedCount(guestCountSpinner.getValueAsInteger());
			hotelOfferingVO.setDescription(descriptionI18NText.getLanguageString());

			lodgePriceGroup.syncEntityToWidgets();


			// set currency and brutto/gross for all prices even if additional prices are not visible
			hotelOfferingVO.setCurrency( lodgePriceGroup.getCurrency() );
			hotelOfferingVO.setGross( lodgePriceGroup.isGross() );


			if (hotelConfigParameterSet.getAdditionalPrice().isVisible()) {
				if (!withBfPrice.getSelection() && bfPriceGroup.getCurrency() == null) {
					bfPriceGroup.setCurrency(lodgePriceGroup.getCurrency());
				}
    			bfPriceGroup.syncEntityToWidgets();
    			hotelOfferingVO.setWithBfPrice(withBfPrice.getSelection());

    			if (!withAdd1Price.getSelection() && add1PriceGroup.getCurrency() == null) {
					add1PriceGroup.setCurrency(lodgePriceGroup.getCurrency());
				}
    			add1PriceGroup.syncEntityToWidgets();
    			hotelOfferingVO.setWithAdd1Price(withAdd1Price.getSelection());

    			if (!withAdd2Price.getSelection() && add2PriceGroup.getCurrency() == null) {
					add2PriceGroup.setCurrency(lodgePriceGroup.getCurrency());
				}
    			add2PriceGroup.syncEntityToWidgets();
    			hotelOfferingVO.setWithAdd2Price(withAdd2Price.getSelection());
			}

			BigDecimal provisionValue = commissionableAmountNumberText.getValue();
			hotelOfferingVO.setProvision(provisionValue == null ? null : provisionValue);


			BigDecimal depositValue = depositNumberText.getValue();
			hotelOfferingVO.setDeposit(depositValue == null ? null : depositValue);

			// payment type groups
			hotelOfferingVO.setDefaultPaymentCondition(defaultPaymentTypeComposite.getCondition());

			hotelOfferingVO.setPaymentConditionAbsorptionOfCosts(
				allowedPaymentTypesComposite.isCondition(HotelBookingPaymentCondition.ABSORPTION_OF_COSTS)
			);
			hotelOfferingVO.setPaymentConditionDeposit(
				allowedPaymentTypesComposite.isCondition(HotelBookingPaymentCondition.DEPOSIT)
			);
			hotelOfferingVO.setPaymentConditionBookingAmount(
				allowedPaymentTypesComposite.isCondition(HotelBookingPaymentCondition.BOOKING_AMOUNT)
			);
			hotelOfferingVO.setPaymentConditionSelfPayPatient(
				allowedPaymentTypesComposite.isCondition(HotelBookingPaymentCondition.SELF_PAY_PATIENT)
			);
		}
	}

}
