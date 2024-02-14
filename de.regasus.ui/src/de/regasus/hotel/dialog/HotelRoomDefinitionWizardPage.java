package de.regasus.hotel.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.hotel.data.RoomType;
import com.lambdalogic.messeinfo.kernel.KernelConstants;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.hotel.roomdefinition.editor.RoomPropertiesComposite;

public class HotelRoomDefinitionWizardPage extends WizardPage {

	// **************************************************************************
	// * Attributes
	// *

	private RoomDefinitionVO roomDefinitionVO;
	private int index;
	
	// **************************************************************************
	// * Widgets
	// *
	
	private I18NText nameText;
	private I18NText descriptionText;
	private RoomPropertiesComposite roomPropertiesComposite;


	// **************************************************************************
	// * Constructors
	// *

	public HotelRoomDefinitionWizardPage(RoomDefinitionVO roomDefinitionVO, int index) {
		// Since this page type may appear several time, we append an index to the name
		super(HotelRoomDefinitionWizardPage.class.getName() + "_" + index);
		
		this.index = index;
		this.roomDefinitionVO = roomDefinitionVO;
		
		setPageComplete(false);
	}

	
	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void createControl(Composite parent) {
		setTitle(HotelLabel.RoomDefinition.getString() + " " +  (index + 1));
		
		Group roomDefinitionGroup = new Group(parent, SWT.NONE);
		roomDefinitionGroup.setText(HotelLabel.RoomDefinition.getString());
		roomDefinitionGroup.setLayout(new GridLayout(2, false));
		

		{
			Label label = new Label(roomDefinitionGroup, SWT.RIGHT);
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			label.setLayoutData(gridData);
			label.setText(UtilI18N.Name);
			SWTHelper.makeBold(label);
		}
		{
			nameText = new I18NText(roomDefinitionGroup, SWT.NONE, LanguageProvider.getInstance(), true /*required*/);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			nameText.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					LanguageString languageString = nameText.getLanguageString();
					setPageComplete(! languageString.isEmpty());
				}
			});
		}


		{
			Label label = new Label(roomDefinitionGroup, SWT.RIGHT);
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			label.setLayoutData(gridData);
			label.setText(UtilI18N.Description);
		}
		{
			descriptionText = new I18NText(roomDefinitionGroup, SWT.NONE, LanguageProvider.getInstance());
			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}


		// Room Properties
		Group roomPropertiesGroup = new Group(roomDefinitionGroup, SWT.NONE);
		roomPropertiesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		roomPropertiesGroup.setLayout(new FillLayout());
		roomPropertiesGroup.setText(HotelLabel.RoomProperties.getString());

		
		roomPropertiesComposite = new RoomPropertiesComposite(roomPropertiesGroup, SWT.NONE);
		roomPropertiesComposite.setRoomProperties(roomDefinitionVO.getRoomProperties());

		setControl(roomDefinitionGroup);
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	/**
	 * Depending on the selection in the previous page, the fields are prefilled. 
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			RoomType roomType = roomDefinitionVO.getRoomType();

			if (roomType != null && nameText.getLanguageString().isEmpty()) {
				LanguageString roomTypeName = new LanguageString(roomType, KernelConstants.SUPPORTED_LANGUAGES);
				nameText.setLanguageString(roomTypeName);
				descriptionText.setLanguageString(roomTypeName);
			}
		}
	}

	
	// **************************************************************************
	// * Other Methods
	// *
	
	public RoomDefinitionVO getRoomDefinitionVO() {
		if (descriptionText != null) {
			roomDefinitionVO.setDescription(descriptionText.getLanguageString());
			roomDefinitionVO.setName(nameText.getLanguageString());
			roomPropertiesComposite.syncEntityToWidgets();
		}
		return roomDefinitionVO;
	}
	
	
	public void setRoomDefinitionVO(RoomDefinitionVO roomDefinitionVO) {
		this.roomDefinitionVO = roomDefinitionVO;
	}
	
}
