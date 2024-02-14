package de.regasus.hotel.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;
import de.regasus.core.ui.i18n.LanguageProvider;

public class HotelAdditionalInformationWizardPage extends WizardPage {

	private Hotel hotel;

	// **************************************************************************
	// * Widgets
	// *

	private MultiLineText noteText;
	private I18NText guestInfoI18Ntext;


	public HotelAdditionalInformationWizardPage(Hotel hotel) {
		super(HotelAdditionalInformationWizardPage.class.getName());

		this.hotel = hotel;
	}


	@Override
	public void createControl(Composite parent) {

		setTitle(I18N.CreateHotel_AdditionalInformation);

		Group group = new Group(parent, SWT.NONE);
		group.setText(I18N.CreateHotel_AdditionalInformation);
		group.setLayout(new GridLayout(2, false));

		{
			Label label = new Label(group, SWT.RIGHT);
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			label.setLayoutData(gridData);
			label.setText( Hotel.NOTE.getLabel() );
			label.setToolTipText( Hotel.NOTE.getDescription() );
		}
		noteText = new MultiLineText(group, SWT.BORDER, false);
		noteText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		{
			Label guestInfoLabel = new Label(group, SWT.RIGHT);
			guestInfoLabel.setText(HotelLabel.Common_GuestInfo.getString());
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			guestInfoLabel.setLayoutData(gridData);
		}
		guestInfoI18Ntext = new I18NText(group, SWT.MULTI, LanguageProvider.getInstance());
		guestInfoI18Ntext.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setControl(group);

	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	public void syncEntityToWidgets() {
		if (hotel != null) {
			hotel.setGuestInfo(guestInfoI18Ntext.getLanguageString());
			hotel.setNote(noteText.getText());
		}
	}

}
