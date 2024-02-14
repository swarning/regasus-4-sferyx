package de.regasus.hotel.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.kernel.KernelConstants;

import de.regasus.hotel.roomdefinition.editor.RoomTypeGroup;


public class HotelRoomTypeSelectionWizardPage extends WizardPage {

	private RoomDefinitionVO roomDefinitionVO;
	
	private int index;

	
	// Widgets
	private RoomTypeGroup roomTypeGroup;

	
	public HotelRoomTypeSelectionWizardPage(RoomDefinitionVO roomDefinitionVO, int index) {
		// Since this page type may appear several time, we append an index to the name
		super(HotelRoomTypeSelectionWizardPage.class.getName() + "_" + index);

		this.roomDefinitionVO = roomDefinitionVO;
		this.index = index;
	}


	@Override
	public void createControl(Composite parent) {
		setTitle(HotelLabel.RoomType.getString() + " " + (index + 1));

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		
    	roomTypeGroup = new RoomTypeGroup(mainComposite, SWT.NONE);
    	roomTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    	roomTypeGroup.setText("");
    	roomTypeGroup.setRoomDefinitionVO(roomDefinitionVO);
    	roomTypeGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(isPageComplete());
				syncEntityToWidgets();
			}
		});

		setControl(mainComposite);
	}

	
	@Override
	public boolean isPageComplete() {
		return roomTypeGroup.isComplete();
	}


	private void syncEntityToWidgets() {
		roomTypeGroup.syncEntityToWidgets();
		
		LanguageString roomTypeName = new LanguageString(
			roomDefinitionVO.getRoomType(),
			KernelConstants.SUPPORTED_LANGUAGES
		);
		
		roomDefinitionVO.setName(roomTypeName);
		roomDefinitionVO.setDescription(roomTypeName);
	}

}
