package de.regasus.hotel.contingent.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NText;

import de.regasus.core.ui.i18n.LanguageProvider;

public class HotelContingentInfoWizardPage extends WizardPage {

	// **************************************************************************
	// * Attributes and Widgets
	// *

	private HotelContingentCVO hotelContingentCVO;
	private Text hotelInfoText;
	private I18NText guestInfoI18nText;
	private Text noteText;


	// **************************************************************************
	// * Constructors
	// *

	protected HotelContingentInfoWizardPage(HotelContingentCVO hotelContingentCVO) {
		super(HotelContingentInfoWizardPage.class.getName());

		this.hotelContingentCVO = hotelContingentCVO;

		setTitle(UtilI18N.Info);
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void createControl(Composite parent) {
		Composite contentComposite = new Composite(parent, SWT.NONE);
		contentComposite.setLayout(new GridLayout(2, false));

		// create Widgets
		hotelInfoText = createLabelAndMultiText(
			contentComposite,
			HotelLabel.Common_HotelInfo.getString()
		);

		guestInfoI18nText =	createLabelAndI18NMultiText(
			contentComposite,
			HotelLabel.Common_GuestInfo.getString(),
			LanguageProvider.getInstance()
		);


		noteText = createLabelAndMultiText(contentComposite, Hotel.NOTE.getLabel());
		// TODO: set tooltip to Label

		setControl(contentComposite);
	}


	// **************************************************************************
	// * Other Methods
	// *

	public void syncEntityToWidgets() {
		HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();

		hotelContingentVO.setHotelInfo(hotelInfoText.getText());
		hotelContingentVO.setParticipantInfo(guestInfoI18nText.getLanguageString());
		hotelContingentVO.setNote(noteText.getText());
	}

}
