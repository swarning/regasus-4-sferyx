package de.regasus.event.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class EventDefaultsComposite extends Composite {

	protected ModifySupport modifySupport = new ModifySupport(this);
	
	private EventVO eventVO;

	// **************************************************************************
	// * Widgets
	// *

	private PriceDefaultsGroup progPriceDefaultsGroup;
	private PriceDefaultsGroup hotelLodgePriceDefaultsGroup;
	private PriceDefaultsGroup hotelBreakfastPriceDefaultsGroup;
	private PriceDefaultsGroup hotelAdd1PriceDefaultsGroup;
	private PriceDefaultsGroup hotelAdd2PriceDefaultsGroup;

	private boolean withHotel;
	private boolean withAdditionalPrices;

	// *
	// * Widgets 
	// **************************************************************************

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @throws Exception 
	 */
	public EventDefaultsComposite(
		Composite parent, 
		int style,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(parent, style);
		
		withHotel = configParameterSet.getEvent().getHotel().isVisible();
		withAdditionalPrices = configParameterSet.getEvent().getHotel().getAdditionalPrice().isVisible();

		// Setup this scrolled composite
		{
			GridLayout layout = new GridLayout(2, true);
			setLayout(layout);
		}
		
		// Setup up to 5 groups		
		progPriceDefaultsGroup = new PriceDefaultsGroup(
			this, 
			SWT.NONE,
			configParameterSet
		);
		progPriceDefaultsGroup.setText(ParticipantLabel.Programme.getString());
		progPriceDefaultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		progPriceDefaultsGroup.addModifyListener(modifySupport);
		
		String hotelSuffix = " (" + HotelLabel.Hotel.getString() + ")";
		
		// placeholder (top right)
		new Label(this, SWT.NONE);
		
		if (withHotel) {
			
			// Lodge Price
			{
    			Composite hotelLodgePriceComposite = new Composite(this, SWT.NONE);
    			
    			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
    			layoutData.verticalIndent = 20;
				hotelLodgePriceComposite.setLayoutData(layoutData);
    			
				GridLayout layout = new GridLayout(1, false);
    			layout.marginWidth = 0;
    			hotelLodgePriceComposite.setLayout(layout);
    
    			Button copyButton = createCopyButton(
					hotelLodgePriceComposite, 
					I18N.EventDefaultsComposite_CopyDefaultValuesForLodgePrice, 
					I18N.EventDefaultsComposite_CopyDefaultValuesForLodgePrice_Tooltip
				);
    						
    			hotelLodgePriceDefaultsGroup = new PriceDefaultsGroup(
    				hotelLodgePriceComposite, 
    				SWT.NONE,
    				configParameterSet
    			);
    			hotelLodgePriceDefaultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    			hotelLodgePriceDefaultsGroup.setText(HotelLabel.LodgePrice.getString() + hotelSuffix);

    			hotelLodgePriceDefaultsGroup.addModifyListener(modifySupport);
    			
    			
    			copyButton.addSelectionListener(new SelectionAdapter() {
    				@Override
    				public void widgetSelected(SelectionEvent event) {
    					try {
    						progPriceDefaultsGroup.copyTo(hotelLodgePriceDefaultsGroup);
    					}
    					catch (Exception e) {
    						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    					}
    				}
    			});

			}
			
			if (withAdditionalPrices) {
				// Breakfast Price
				{
    				Composite hotelBfPriceComposite = new Composite(this, SWT.NONE);
    				
        			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        			layoutData.verticalIndent = 20;
        			hotelBfPriceComposite.setLayoutData(layoutData);

    				GridLayout layout = new GridLayout(1, false);
        			layout.marginWidth = 0;
        			hotelBfPriceComposite.setLayout(layout);
    
    				Button copyButton = createCopyButton(
						hotelBfPriceComposite, 
						I18N.EventDefaultsComposite_CopyDefaultValuesForBreakfastPrice, 
						I18N.EventDefaultsComposite_CopyDefaultValuesForBreakfastPrice_Tooltip
					);
    
    				hotelBreakfastPriceDefaultsGroup = new PriceDefaultsGroup(
    					hotelBfPriceComposite, 
    					SWT.NONE,
    					configParameterSet
    				);
    				hotelBreakfastPriceDefaultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    				hotelBreakfastPriceDefaultsGroup.setText(HotelLabel.BreakfastPrice.getString() + hotelSuffix);
    				
    				hotelBreakfastPriceDefaultsGroup.addModifyListener(modifySupport);
        			
        			
        			copyButton.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent event) {
        					try {
        						progPriceDefaultsGroup.copyTo(hotelBreakfastPriceDefaultsGroup);
        					}
        					catch (Exception e) {
        						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
        					}
        				}
        			});

				}
				
				// Additional Price 1
				{
    				Composite hotelAdd1PriceComposite = new Composite(this, SWT.NONE);
    				
        			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        			layoutData.verticalIndent = 20;
        			hotelAdd1PriceComposite.setLayoutData(layoutData);
        			
    				GridLayout layout = new GridLayout(1, false);
        			layout.marginWidth = 0;
        			hotelAdd1PriceComposite.setLayout(layout);
    
    				Button copyButton = createCopyButton(
						hotelAdd1PriceComposite, 
						I18N.EventDefaultsComposite_CopyDefaultValuesForAdditional1Price, 
						I18N.EventDefaultsComposite_CopyDefaultValuesForAdditional1Price_Tooltip
					);
    				
    				hotelAdd1PriceDefaultsGroup = new PriceDefaultsGroup(
    					hotelAdd1PriceComposite, 
    					SWT.NONE,
    					configParameterSet
    				);
    				hotelAdd1PriceDefaultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    				hotelAdd1PriceDefaultsGroup.setText(InvoiceLabel.Add1Price.getString() + hotelSuffix);
    				
    				hotelAdd1PriceDefaultsGroup.addModifyListener(modifySupport);
        			
        			
        			copyButton.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent event) {
        					try {
        						progPriceDefaultsGroup.copyTo(hotelAdd1PriceDefaultsGroup);
        					}
        					catch (Exception e) {
        						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
        					}
        				}
        			});

				}

				// Additional Price 1
				{
    				Composite hotelAdd2PriceComposite = new Composite(this, SWT.NONE);
    				
        			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        			layoutData.verticalIndent = 20;
        			hotelAdd2PriceComposite.setLayoutData(layoutData);
        			
        			GridLayout layout = new GridLayout(1, false);
        			layout.marginWidth = 0;
        			hotelAdd2PriceComposite.setLayout(layout);
    
    				Button copyButton = createCopyButton(
						hotelAdd2PriceComposite, 
						I18N.EventDefaultsComposite_CopyDefaultValuesForAdditional2Price, 
						I18N.EventDefaultsComposite_CopyDefaultValuesForAdditional2Price_Tooltip
					);
    				
    				hotelAdd2PriceDefaultsGroup = new PriceDefaultsGroup(
    					hotelAdd2PriceComposite, 
    					SWT.NONE,
    					configParameterSet
    				);
    				hotelAdd2PriceDefaultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    				hotelAdd2PriceDefaultsGroup.setText(InvoiceLabel.Add2Price.getString() + hotelSuffix);
    				
    				hotelAdd2PriceDefaultsGroup.addModifyListener(modifySupport);
        			
        			
        			copyButton.addSelectionListener(new SelectionAdapter() {
        				@Override
        				public void widgetSelected(SelectionEvent event) {
        					try {
        						progPriceDefaultsGroup.copyTo(hotelAdd2PriceDefaultsGroup);
        					}
        					catch (Exception e) {
        						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
        					}
        				}
        			});

				}
				
			}
		}
		else {
			progPriceDefaultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		}
	}
	

	private Button createCopyButton(Composite buttonComposite, String label, String toolTipText) {
		Button copyButton = new Button(buttonComposite, SWT.PUSH);
		copyButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
		copyButton.setText(label);
		copyButton.setToolTipText(toolTipText);
		return copyButton;
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

	
	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						Long eventID = eventVO.getID();
						progPriceDefaultsGroup.setPriceDefaultsVO(eventVO.getProgPriceDefaultsVO(), eventID);
						
						if (withHotel) {
							hotelLodgePriceDefaultsGroup.setPriceDefaultsVO(eventVO.getHotelLodgePriceDefaultsVO(), eventID);
							
							if (withAdditionalPrices) {
								hotelBreakfastPriceDefaultsGroup.setPriceDefaultsVO(eventVO.getHotelBreakfastPriceDefaultsVO(), eventID);
								hotelAdd1PriceDefaultsGroup.setPriceDefaultsVO(eventVO.getHotelAdd1PriceDefaultsVO(), eventID);
								hotelAdd2PriceDefaultsGroup.setPriceDefaultsVO(eventVO.getHotelAdd2PriceDefaultsVO(), eventID);
							}
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
		progPriceDefaultsGroup.syncEntityToWidgets();
		
		if (withHotel) {
			hotelLodgePriceDefaultsGroup.syncEntityToWidgets();
			
			if (withAdditionalPrices) {
				hotelBreakfastPriceDefaultsGroup.syncEntityToWidgets();
				hotelAdd1PriceDefaultsGroup.syncEntityToWidgets();
				hotelAdd2PriceDefaultsGroup.syncEntityToWidgets();
			}
		}
	}
	
	
	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
