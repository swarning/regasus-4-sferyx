package de.regasus.programme.booking.dialog;

import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.i18n.I18NText;

import de.regasus.I18N;
import de.regasus.core.error.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;

/**
 * A simple page that shows an {@link I18NText} widget that is used to enter booking information relevant to the
 * participant.
 * <p>
 * {@link https://mi2.lambdalogic.de/jira/browse/MIRCP-104 }
 * 
 * @author manfred
 * 
 */
public class InfoAndOptionsPage extends WizardPage {

	public static final String NAME = "InfoAndOptionsPage";

	private I18NText text;

	private Button onlyOnceCheckBox;

	private DateTimeComposite referenceDateWidget;

	private Long eventPK;

	public InfoAndOptionsPage(Long eventPK) {
		super(NAME);

		this.eventPK = eventPK;
		setTitle(I18N.CreateProgrammeBookings_Text);
		setMessage(I18N.CreateProgrammeBookings_InfosForParticipant);
	}


	public void createControl(Composite parent) {

		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(2, false));

		try {
			
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			{
				Label label = new Label(controlComposite, SWT.NONE);
				label.setText(UtilI18N.Info);
				GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				label.setLayoutData(gridData);
			}
	
			text = new I18NText(controlComposite, SWT.MULTI, LanguageProvider.getInstance());
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			text.setLanguageString(text.getLanguageString(), eventVO.getLanguages());
			setControl(controlComposite);
	
			new Label(controlComposite, SWT.NONE); // dummy
	
			onlyOnceCheckBox = new Button(controlComposite, SWT.CHECK);
			onlyOnceCheckBox.setText(
				I18N.CreateProgrammeBookings_OnlyOnce
			);
	
			if (getWizard() instanceof CreateProgrammeBookingsWizardSeveralParticipantTypes) {
	
				DecideBookingModePage decideBookingModePage =
					(DecideBookingModePage) getWizard().getPage(DecideBookingModePage.NAME);
	
				if (!decideBookingModePage.isBookingViaProgrammeOfferings()) {
					// Date composite for entry of a reference date
					Label referenceDateLabel = new Label(controlComposite, SWT.NONE);
					referenceDateLabel.setText(I18N.ReferenceTime);
					referenceDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
	
					referenceDateWidget = new DateTimeComposite(controlComposite, SWT.BORDER);
					// Registration date should default to today
					referenceDateWidget.setDate(new Date());
					referenceDateWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	public Date getReferenceDate() {
		if (referenceDateWidget != null) {
			return referenceDateWidget.getDate();
		}
		else {
			return null;
		}
	}

	
	public boolean isOnlyOnce() {
		return onlyOnceCheckBox.getSelection();
	}

	public LanguageString getInfo() {
		return text.getLanguageString();
	}

}
