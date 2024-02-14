package de.regasus.hotel.eventhotelinfo.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.EventHotelInfoVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.finance.currency.combo.CurrencyCombo;

public class EventHotelInfoRoomCreditComposite extends Composite  {

	// Entities

	private EventHotelInfoVO eventHotelInfoVO;
	

	// Widgets
	
	private NullableSpinner roomCreditLimitSpinner;
	private DecimalNumberText roomCreditAmountNumberText;
	private CurrencyCombo roomCreditCurrencyCombo;
	
	protected ModifySupport modifySupport = new ModifySupport(this);

	
	public EventHotelInfoRoomCreditComposite(
		Composite parent, 
		int style
	) {
		super(parent, style);
		
		setLayout(new GridLayout(4, false));
		
		try {
			
			// CREATE WIDGETS
			{			
				{
					createLabel(this, HotelLabel.EventHotelInfoRoomCredit_Limit);
    				
					roomCreditLimitSpinner = new NullableSpinner(this, SWT.FILL);
    				roomCreditLimitSpinner.setMinimum(EventHotelInfoVO.MIN_ROOM_CREDIT_LIMIT);
    				roomCreditLimitSpinner.setMaximum(EventHotelInfoVO.MAX_ROOM_CREDIT_LIMIT);
    				roomCreditLimitSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
    				roomCreditLimitSpinner.addModifyListener(modifySupport);
    				//WidgetSizer.setWidth(roomCreditLimitSpinner);
    				
    				new Label(this, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				}
				
				{
					createLabel(this, HotelLabel.EventHotelInfoRoomCredit_Amount);
					
    				roomCreditAmountNumberText = new DecimalNumberText(this, SWT.FILL);
    				roomCreditAmountNumberText.setFractionDigits(2);
    				roomCreditAmountNumberText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    				roomCreditAmountNumberText.addModifyListener(modifySupport);
				
    				roomCreditCurrencyCombo = new CurrencyCombo(this, SWT.NONE);
    				GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
    				roomCreditCurrencyCombo.setLayoutData(gridData);
    				roomCreditCurrencyCombo.addModifyListener(modifySupport);
    				
    				new Label(this, SWT.NONE).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
	
	// *
	// * Modifying
	// **************************************************************************

	
	public void setEntity(EventHotelInfoVO eventHotelInfoVO) {
		this.eventHotelInfoVO = eventHotelInfoVO;
		syncWidgetsToEntity();
	}

	
	public void syncEntityToWidgets() {
		if (eventHotelInfoVO != null) {
			eventHotelInfoVO.setRoomCreditLimit(roomCreditLimitSpinner.getValueAsInteger());
			eventHotelInfoVO.setRoomCreditAmount(roomCreditAmountNumberText.getValue());
			eventHotelInfoVO.setRoomCreditCurrency(roomCreditCurrencyCombo.getCurrencyCode());
		}
	}
	
	
	private void syncWidgetsToEntity() {
		if (eventHotelInfoVO != null) {
			roomCreditLimitSpinner.setValue(eventHotelInfoVO.getRoomCreditLimit());
			roomCreditAmountNumberText.setValue(eventHotelInfoVO.getRoomCreditAmount());
			roomCreditCurrencyCombo.setCurrencyCode(eventHotelInfoVO.getRoomCreditCurrency());
		}
	}
	
}
