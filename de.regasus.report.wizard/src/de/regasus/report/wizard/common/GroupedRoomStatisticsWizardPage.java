package de.regasus.report.wizard.common;

import static com.lambdalogic.messeinfo.hotel.report.groupedRoomStat.BookingCountGroup.*;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.hotel.report.groupedRoomStat.BookingCountGroup;
import com.lambdalogic.messeinfo.hotel.report.groupedRoomStat.IGroupedRoomStatisticsReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class GroupedRoomStatisticsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "GroupedRoomStatisticsWizardPage";

	private IGroupedRoomStatisticsReportParameter parameter;


	// 4 Radio buttons to select one of the groups
	private Button hotelButton;
	private Button hotelChainButton;
	private Button hotelStarsButton;
	private Button hotelContingentTypeButton;

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					syncReportParameter();
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	public GroupedRoomStatisticsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.Grouping);
	}


	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NULL);
		controlComposite.setLayout(new GridLayout());
		setControl(controlComposite);

		Composite widgetComposite = new Composite(controlComposite, SWT.NONE);
		widgetComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		widgetComposite.setLayout(new GridLayout(1, false));

		// Booking group
		Group bookingCountGroupGroup = new Group(widgetComposite, SWT.NONE);
		bookingCountGroupGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		bookingCountGroupGroup.setLayout(new GridLayout(1, false));

		hotelButton = new Button(bookingCountGroupGroup, SWT.RADIO);
		hotelButton.setText(HOTEL.getString());
		hotelButton.addSelectionListener(selectionListener);

		hotelChainButton = new Button(bookingCountGroupGroup, SWT.RADIO);
		hotelChainButton.setText(HOTEL_CHAIN.getString());
		hotelChainButton.addSelectionListener(selectionListener);

		hotelStarsButton = new Button(bookingCountGroupGroup, SWT.RADIO);
		hotelStarsButton.setText(HOTEL_STARS.getString());
		hotelStarsButton.addSelectionListener(selectionListener);

		hotelContingentTypeButton = new Button(bookingCountGroupGroup, SWT.RADIO);
		hotelContingentTypeButton.setText(HOTEL_CONTINGENT_TYPE.getString());
		hotelContingentTypeButton.addSelectionListener(selectionListener);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IGroupedRoomStatisticsReportParameter) {
			parameter = (IGroupedRoomStatisticsReportParameter) reportParameter;
			BookingCountGroup group = parameter.getBookingCountGroup();

			hotelButton.setSelection(HOTEL == group);
			hotelChainButton.setSelection(HOTEL_CHAIN == group);
			hotelStarsButton.setSelection(HOTEL_STARS == group);
			hotelContingentTypeButton.setSelection(HOTEL_CONTINGENT_TYPE == group);

			setPageComplete(isPageComplete());
		}
	}


	private void syncReportParameter() {
		if (hotelButton.getSelection()) {
			parameter.setBookingCountGroup(HOTEL);
		}
		else if (hotelChainButton.getSelection()) {
			parameter.setBookingCountGroup(HOTEL_CHAIN);
		}
		else if (hotelStarsButton.getSelection()) {
			parameter.setBookingCountGroup(HOTEL_STARS);
		}
		else if (hotelContingentTypeButton.getSelection()) {
			parameter.setBookingCountGroup(HOTEL_CONTINGENT_TYPE);
		}
		else {
			// Shouldn't happen
			parameter.setBookingCountGroup(null);
		}

		// set description
		BookingCountGroup bookingCountGroup = parameter.getBookingCountGroup();
		if (bookingCountGroup != null) {
			String group = bookingCountGroup.getString();
			parameter.setDescription("grouping", ReportWizardI18N.Grouping + ": " + group);
		}
		else {
			parameter.setDescription("grouping", null);
		}

		setPageComplete(isPageComplete());
	}


	@Override
	public boolean isPageComplete() {
		return
			parameter != null &&
			parameter.getBookingCountGroup() != null;
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
