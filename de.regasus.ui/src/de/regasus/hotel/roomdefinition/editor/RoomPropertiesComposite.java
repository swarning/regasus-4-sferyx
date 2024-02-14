package de.regasus.hotel.roomdefinition.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.RoomProperties;
import com.lambdalogic.messeinfo.hotel.RoomProperty;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class RoomPropertiesComposite extends Composite {

	// the entity
	private RoomProperties roomProperties;


	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private Button ironingBoardInRoomButton;
	private Button additionalBedButton;
	private Button miniBarButton;
	private Button safeButton;
	private Button airConButton;
	private Button hairDryerButton;
	private Button kitchenetteButton;
	private Button showerButton;
	private Button bathtubButton;
	private Button teaCoffeeButton;
	private Button tvButton;
	private Button phoneButton;
	private Button deskButton;
	private Button fridgeButton;



	public RoomPropertiesComposite(Composite parent, int style) {
		super(parent, style);

		try {
			setLayout(new GridLayout(2, false));

			/* Buttons
			 * The order of the Buttons here does not correspond to the order in HotelProperties.
			 * The order in HotelProperties is chronological/technical. New fields are appended at the end.
			 * The order here is semantical. Properties that belong together (e.g. pool, fitness and saune) should
			 * appear near each other.
			 */

			GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();
			gridDataFactory.align(SWT.FILL, SWT.FILL);
			gridDataFactory.grab(true,  false);

			int compositeStyle = SWT.BORDER;


			// food
			{
				Composite foodComposite = new Composite(this, compositeStyle);
				foodComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(foodComposite);


				fridgeButton = new Button(foodComposite, SWT.CHECK);
				fridgeButton.setText( RoomProperty.FRIDGE.getString() );
				fridgeButton.addSelectionListener(modifySupport);

				teaCoffeeButton = new Button(foodComposite, SWT.CHECK);
				teaCoffeeButton.setText( RoomProperty.TEA_COFFEE.getString() );
				teaCoffeeButton.addSelectionListener(modifySupport);

				miniBarButton = new Button(foodComposite, SWT.CHECK);
				miniBarButton.setText( RoomProperty.MINI_BAR.getString() );
				miniBarButton.addSelectionListener(modifySupport);

				kitchenetteButton = new Button(foodComposite, SWT.CHECK);
				kitchenetteButton.setText( RoomProperty.KITCHENETTE.getString() );
				kitchenetteButton.addSelectionListener(modifySupport);
			}


			// bathroom
			{
				Composite bathroomComposite = new Composite(this, compositeStyle);
				bathroomComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(bathroomComposite);


				hairDryerButton = new Button(bathroomComposite, SWT.CHECK);
				hairDryerButton.setText( RoomProperty.HAIR_DRYER.getString() );
				hairDryerButton.addSelectionListener(modifySupport);

				showerButton = new Button(bathroomComposite, SWT.CHECK);
				showerButton.setText( RoomProperty.SHOWER.getString() );
				showerButton.addSelectionListener(modifySupport);

				bathtubButton = new Button(bathroomComposite, SWT.CHECK);
				bathtubButton.setText( RoomProperty.BATHTUB.getString() );
				bathtubButton.addSelectionListener(modifySupport);
			}


			// technics
			{
				Composite technicsComposite = new Composite(this, compositeStyle);
				technicsComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(technicsComposite);


				ironingBoardInRoomButton = new Button(technicsComposite, SWT.CHECK);
				ironingBoardInRoomButton.setText( RoomProperty.IRONING_BOARD_IN_ROOM.getString() );
				ironingBoardInRoomButton.addSelectionListener(modifySupport);

				airConButton = new Button(technicsComposite, SWT.CHECK);
				airConButton.setText( RoomProperty.AIR_CON.getString() );
				airConButton.addSelectionListener(modifySupport);

				tvButton = new Button(technicsComposite, SWT.CHECK);
				tvButton.setText( RoomProperty.TV.getString() );
				tvButton.addSelectionListener(modifySupport);

				phoneButton = new Button(technicsComposite, SWT.CHECK);
				phoneButton.setText( RoomProperty.PHONE.getString() );
				phoneButton.addSelectionListener(modifySupport);
			}


			// other
			{
				Composite otherComposite = new Composite(this, compositeStyle);
				otherComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(otherComposite);


				additionalBedButton = new Button(otherComposite, SWT.CHECK);
				additionalBedButton.setText( RoomProperty.ADDITIONAL_BED.getString() );
				additionalBedButton.addSelectionListener(modifySupport);

				safeButton = new Button(otherComposite, SWT.CHECK);
				safeButton.setText( RoomProperty.SAFE.getString() );
				safeButton.addSelectionListener(modifySupport);

				deskButton = new Button(otherComposite, SWT.CHECK);
				deskButton.setText( RoomProperty.DESK.getString() );
				deskButton.addSelectionListener(modifySupport);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}


	public void setRoomProperties(RoomProperties roomProperties) {
		this.roomProperties = roomProperties;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (roomProperties != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						final RoomProperties rp = roomProperties;

						ironingBoardInRoomButton.setSelection(	rp.isIroningBoardInRoom());
						additionalBedButton.setSelection(		rp.isAdditionalBed());
						miniBarButton.setSelection(				rp.isMiniBar());
						safeButton.setSelection(				rp.isSafe());
						airConButton.setSelection(				rp.isAirCon());
						hairDryerButton.setSelection(			rp.isHairDryer());
						kitchenetteButton.setSelection(			rp.isKitchenette());
						showerButton.setSelection(				rp.isShower());
						bathtubButton.setSelection(				rp.isBathtub());
						teaCoffeeButton.setSelection(			rp.isTeaCoffee());
						tvButton.setSelection(					rp.isTV());
						phoneButton.setSelection(				rp.isPhone());
						deskButton.setSelection(				rp.isDesk());
						fridgeButton.setSelection(				rp.isFridge());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		if (roomProperties != null) {
			final RoomProperties hp = roomProperties;

			hp.setIroningBoardInRoom(ironingBoardInRoomButton.getSelection());
			hp.setAdditionalBed(additionalBedButton.getSelection());
			hp.setMiniBar(miniBarButton.getSelection());
			hp.setSafe(safeButton.getSelection());
			hp.setAirCon(airConButton.getSelection());
			hp.setHairDryer(hairDryerButton.getSelection());
			hp.setKitchenette(kitchenetteButton.getSelection());
			hp.setShower(showerButton.getSelection());
			hp.setBathtub(bathtubButton.getSelection());
			hp.setTeaCoffee(teaCoffeeButton.getSelection());
			hp.setTV(tvButton.getSelection());
			hp.setPhone(phoneButton.getSelection());
			hp.setDesk(deskButton.getSelection());
			hp.setFridge(fridgeButton.getSelection());
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

}