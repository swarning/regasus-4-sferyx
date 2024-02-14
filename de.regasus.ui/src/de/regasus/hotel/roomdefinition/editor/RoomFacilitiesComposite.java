package de.regasus.hotel.roomdefinition.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.util.Unit;

public class RoomFacilitiesComposite extends EntityComposite<RoomDefinitionVO> {

	// widgets
	private NullableSpinner roomSizeSpinner;

	private RoomPropertiesComposite roomPropertiesComposite;


	public RoomFacilitiesComposite(Composite parent, int style) throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		// set layout for this Composite: 2 columns
		setLayout(new GridLayout());

		// room size
		{
			Composite roomSizeComposite = new Composite(this, SWT.NONE);

			GridLayout gridLayout = new GridLayout(3, false);
			// remove 5 pixel gap in roomSizeComposite
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			roomSizeComposite.setLayout(gridLayout);

			roomSizeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,  false));


			Label roomSizeLabel = new Label(roomSizeComposite, SWT.RIGHT);
    		roomSizeLabel.setText(HotelLabel.RoomDefinition_RoomSize.getString());

   			roomSizeSpinner = new NullableSpinner(roomSizeComposite, SWT.BORDER);
   			roomSizeSpinner.setMinimum(RoomDefinitionVO.MIN_ROOM_SIZE);
   			roomSizeSpinner.setMaximum(RoomDefinitionVO.MAX_ROOM_SIZE);
   			WidgetSizer.setWidth(roomSizeSpinner);
   			roomSizeSpinner.addModifyListener(modifySupport);

			Label unitLabel = new Label(roomSizeComposite, SWT.NONE);
    		unitLabel.setText( Unit.sqm.getString() );
		}

		// Hotel Properties
		{
			roomPropertiesComposite = new RoomPropertiesComposite(this, SWT.NONE);

			// remove 5 pixel gap around HotelPropertiesComposite
			GridLayout gridLayout = (GridLayout) roomPropertiesComposite.getLayout();
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;

			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true,  false, 2, 1);
			roomPropertiesComposite.setLayoutData(gridData);

			roomPropertiesComposite.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		roomSizeSpinner.setValue( entity.getRoomSize() );
		roomPropertiesComposite.setRoomProperties( entity.getRoomProperties() );
	}


	@Override
	public void syncEntityToWidgets() {
		entity.setRoomSize( roomSizeSpinner.getValueAsInteger() );
		roomPropertiesComposite.syncEntityToWidgets();
	}

}
