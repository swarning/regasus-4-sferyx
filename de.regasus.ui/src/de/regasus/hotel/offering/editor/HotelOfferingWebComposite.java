package de.regasus.hotel.offering.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabelAndI18NMultiText;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

public class HotelOfferingWebComposite extends Composite  {

	// the entity
	private HotelOfferingVO hotelOfferingVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private Text tagsText;
	private Text referenceCodeText;
	private Button onlineAvailableButton;
	private I18NText onlineInfo;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public HotelOfferingWebComposite(Composite parent, int style) {
		super(parent, style);

		try {
			setLayout(new GridLayout(2, false));

			// Tags
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
				label.setText( HotelLabel.Tags.getString() );
				label.setToolTipText( HotelLabel.Tags_Desc.getString() );

				tagsText = new Text(this, SWT.BORDER);
				tagsText.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );

				tagsText.addModifyListener(modifySupport);
			}

			// Reference Code
			{
				Label label = new Label(this, SWT.NONE);
				label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
				label.setText( HotelLabel.ReferenceCode.getString() );
				label.setToolTipText( HotelLabel.ReferenceCode_Desc.getString() );

				referenceCodeText = new Text(this, SWT.BORDER);
				referenceCodeText.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );

				referenceCodeText.addModifyListener(modifySupport);
			}

			new Label(this, SWT.NONE);
			{
				onlineAvailableButton = new Button(this, SWT.CHECK);
				onlineAvailableButton.setSelection(true);
				onlineAvailableButton.setText( HotelLabel.HotelOffering_OnlineAvailable.getString() );
				onlineAvailableButton.setToolTipText( HotelLabel.HotelOffering_OnlineAvailable_desc.getString() );

				onlineAvailableButton.addSelectionListener(modifySupport);
			}

			onlineInfo = createLabelAndI18NMultiText(this, HotelLabel.OnlineInfo.getString(), LanguageProvider.getInstance());

			onlineInfo.addModifyListener(modifySupport);
//			{
//				Label onlineInfoLabel = new Label(this, SWT.NONE);
//				onlineInfoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
//				onlineInfoLabel.setText(HotelLabel.OnlineInfo.getString());
//			}
//			{
//				onlineInfo = new I18NText(this, SWT.BORDER | SWT.MULTI);
//				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
//				gridData.heightHint = 100;
//				onlineInfo.setLayoutData(gridData);
//			}
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


	public void setHotelOfferingVO(HotelOfferingVO hotelOfferingVO) {
		this.hotelOfferingVO = hotelOfferingVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (hotelOfferingVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						EventVO eventVO = EventModel.getInstance().getEventVO(hotelOfferingVO.getEventPK());

						tagsText.setText(StringHelper.avoidNull(hotelOfferingVO.getTags()));
						referenceCodeText.setText(StringHelper.avoidNull(hotelOfferingVO.getReferenceCode()));
						onlineAvailableButton.setSelection(hotelOfferingVO.isOnlineAvailable());
						onlineInfo.setLanguageString(hotelOfferingVO.getOnlineInfo(), eventVO.getLanguages());
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
			hotelOfferingVO.setTags(tagsText.getText());
			hotelOfferingVO.setReferenceCode(referenceCodeText.getText());
			hotelOfferingVO.setOnlineAvailable(onlineAvailableButton.getSelection());
			hotelOfferingVO.setOnlineInfo(onlineInfo.getLanguageString());
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
