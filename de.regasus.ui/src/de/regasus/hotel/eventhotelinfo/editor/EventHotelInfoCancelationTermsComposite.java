package de.regasus.hotel.eventhotelinfo.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabel;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.EventHotelInfoVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.time.I18NDateMinute;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelModel;
import de.regasus.ui.Activator;

/**
 * A Composite containing Cancellation terms with hotel and Cancellation terms with group
 */
public class EventHotelInfoCancelationTermsComposite extends Composite {

	// the entity
	private EventHotelInfoVO eventHotelInfoVO;

	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private DateComposite cancelDateWithHotel1Composite;
	private DecimalNumberText cancelPercentWithHotel1NumberText;
	private Text cancelNoteWithHotel1Text;

	private DateComposite cancelDateWithHotel2Composite;
	private DecimalNumberText cancelPercentWithHotel2NumberText;
	private Text cancelNoteWithHotel2Text;

	private DateComposite cancelDateWithHotel3Composite;
	private DecimalNumberText cancelPercentWithHotel3NumberText;
	private Text cancelNoteWithHotel3Text;

	private DateComposite cancelDateWithHotel4Composite;
	private DecimalNumberText cancelPercentWithHotel4NumberText;
	private Text cancelNoteWithHotel4Text;

	private DateComposite cancelDateWithHotel5Composite;
	private DecimalNumberText cancelPercentWithHotel5NumberText;
	private Text cancelNoteWithHotel5Text;

	private DateComposite cancelDateWithGroup1Composite;
	private DecimalNumberText cancelPercentWithGroup1NumberText;
	private Text cancelNoteWithGroup1Text;

	private DateComposite cancelDateWithGroup2Composite;
	private DecimalNumberText cancelPercentWithGroup2NumberText;
	private Text cancelNoteWithGroup2Text;

	private DateComposite cancelDateWithGroup3Composite;
	private DecimalNumberText cancelPercentWithGroup3NumberText;
	private Text cancelNoteWithGroup3Text;

	private DateComposite cancelDateWithGroup4Composite;
	private DecimalNumberText cancelPercentWithGroup4NumberText;
	private Text cancelNoteWithGroup4Text;

	private DateComposite cancelDateWithGroup5Composite;
	private DecimalNumberText cancelPercentWithGroup5NumberText;
	private Text cancelNoteWithGroup5Text;


	// reminder
	private EventHotelReminderManagementComposite eventHotelReminderManagementComposite;

	private boolean reminderVisible = true;



	public EventHotelInfoCancelationTermsComposite(
		Composite parent,
		int style,
		boolean reminderVisible,
		Long eventPK,
		Long hotelPK
	) {
		super(parent, style);

		this.reminderVisible = reminderVisible;

		setLayout(new GridLayout(2, false));

		/* create the Composites (Groups) for hotel and groups
		 * The Composite for hotel is on the left and the Composite for groups on the right side.
		 */

		Group hotelComposite = new Group(this, SWT.NONE);
		{
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    		hotelComposite.setLayoutData(layoutData);
    		hotelComposite.setText(HotelLabel.CancellationTermsWithHotel.getString());
    		hotelComposite.setLayout(new GridLayout());
		}

		Group groupComposite = new Group(this, SWT.NONE);
		{
    		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			groupComposite.setLayoutData(layoutData);
    		groupComposite.setText(HotelLabel.CancellationTermsWithGroup.getString());
    		groupComposite.setLayout(new GridLayout());
		}

		Group reminderComposite = new Group(this, SWT.NONE);
		{
    		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    		reminderComposite.setLayoutData(layoutData);
    		reminderComposite.setText(HotelLabel.EventHotelReminderReminders.getString());
    		reminderComposite.setLayout(new FillLayout());
		}

		// create widgets for hotel (left side)
		{
			// Row 1
			Group group1 = createGroup(hotelComposite, 1);

			cancelDateWithHotel1Composite = createDateWidgets(group1, KernelLabel.Date.getString());
			cancelPercentWithHotel1NumberText = createPercentWidgets(group1, KernelLabel.Percent);
			cancelNoteWithHotel1Text = createNoteWidgets(group1, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group1,
				cancelDateWithHotel1Composite,
				cancelPercentWithHotel1NumberText,
				cancelNoteWithHotel1Text
			);

			cancelDateWithHotel1Composite.addModifyListener(modifySupport);


			// Row 2
			Group group2 = createGroup(hotelComposite, 2);

			cancelDateWithHotel2Composite = createDateWidgets(group2, KernelLabel.Date.getString());
			cancelPercentWithHotel2NumberText = createPercentWidgets(group2, KernelLabel.Percent);
			cancelNoteWithHotel2Text = createNoteWidgets(group2, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group2,
				cancelDateWithHotel2Composite,
				cancelPercentWithHotel2NumberText,
				cancelNoteWithHotel2Text
			);

			cancelDateWithHotel2Composite.addModifyListener(modifySupport);


			// Row 3
			Group group3 = createGroup(hotelComposite, 3);

			cancelDateWithHotel3Composite = createDateWidgets(group3, KernelLabel.Date.getString());
			cancelPercentWithHotel3NumberText = createPercentWidgets(group3, KernelLabel.Percent);
			cancelNoteWithHotel3Text = createNoteWidgets(group3, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group3,
				cancelDateWithHotel3Composite,
				cancelPercentWithHotel3NumberText,
				cancelNoteWithHotel3Text
			);

			cancelDateWithHotel3Composite.addModifyListener(modifySupport);


			// Row 4
			Group group4 = createGroup(hotelComposite, 4);

			cancelDateWithHotel4Composite = createDateWidgets(group4, KernelLabel.Date.getString());
			cancelPercentWithHotel4NumberText = createPercentWidgets(group4, KernelLabel.Percent);
			cancelNoteWithHotel4Text = createNoteWidgets(group4, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group4,
				cancelDateWithHotel4Composite,
				cancelPercentWithHotel4NumberText,
				cancelNoteWithHotel4Text
			);

			cancelDateWithHotel4Composite.addModifyListener(modifySupport);


			// Row 5
			Group group5 = createGroup(hotelComposite, 5);

			cancelDateWithHotel5Composite = createDateWidgets(group5, KernelLabel.Date.getString());
			cancelPercentWithHotel5NumberText = createPercentWidgets(group5, KernelLabel.Percent);
			cancelNoteWithHotel5Text = createNoteWidgets(group5, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group5,
				cancelDateWithHotel5Composite,
				cancelPercentWithHotel5NumberText,
				cancelNoteWithHotel5Text
			);

			cancelDateWithHotel5Composite.addModifyListener(modifySupport);
		}



		// create widgets for group (right side)
		{
			// Row 1
			Group group1 = createGroup(groupComposite, 1);

			cancelDateWithGroup1Composite = createDateWidgets(group1, KernelLabel.Date.getString());
			cancelPercentWithGroup1NumberText = createPercentWidgets(group1, KernelLabel.Percent);
			cancelNoteWithGroup1Text = createNoteWidgets(group1, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group1,
				cancelDateWithGroup1Composite,
				cancelPercentWithGroup1NumberText,
				cancelNoteWithGroup1Text
			);

			cancelDateWithGroup1Composite.addModifyListener(modifySupport);


			// Row 2
			Group group2 = createGroup(groupComposite, 2);

			cancelDateWithGroup2Composite = createDateWidgets(group2, KernelLabel.Date.getString());
			cancelPercentWithGroup2NumberText = createPercentWidgets(group2, KernelLabel.Percent);
			cancelNoteWithGroup2Text = createNoteWidgets(group2, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group2,
				cancelDateWithGroup2Composite,
				cancelPercentWithGroup2NumberText,
				cancelNoteWithGroup2Text
			);

			cancelDateWithGroup2Composite.addModifyListener(modifySupport);


			// Row 3
			Group group3 = createGroup(groupComposite, 3);

			cancelDateWithGroup3Composite = createDateWidgets(group3, KernelLabel.Date.getString());
			cancelPercentWithGroup3NumberText = createPercentWidgets(group3, KernelLabel.Percent);
			cancelNoteWithGroup3Text = createNoteWidgets(group3, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group3,
				cancelDateWithGroup3Composite,
				cancelPercentWithGroup3NumberText,
				cancelNoteWithGroup3Text
			);

			cancelDateWithGroup3Composite.addModifyListener(modifySupport);


			// Row 4
			Group group4 = createGroup(groupComposite, 4);

			cancelDateWithGroup4Composite = createDateWidgets(group4, KernelLabel.Date.getString());
			cancelPercentWithGroup4NumberText = createPercentWidgets(group4, KernelLabel.Percent);
			cancelNoteWithGroup4Text = createNoteWidgets(group4, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group4,
				cancelDateWithGroup4Composite,
				cancelPercentWithGroup4NumberText,
				cancelNoteWithGroup4Text
			);

			cancelDateWithGroup4Composite.addModifyListener(modifySupport);


			// Row 5
			Group group5 = createGroup(groupComposite, 5);

			cancelDateWithGroup5Composite  = createDateWidgets(group5, KernelLabel.Date.getString());
			cancelPercentWithGroup5NumberText = createPercentWidgets(group5, KernelLabel.Percent);
			cancelNoteWithGroup5Text = createNoteWidgets(group5, Hotel.NOTE.getI18NLabel());
			createReminderButton(
				group5,
				cancelDateWithGroup5Composite,
				cancelPercentWithGroup5NumberText,
				cancelNoteWithGroup5Text
			);

			cancelDateWithGroup5Composite.addModifyListener(modifySupport);
		}


		// create widgets for reminders
		if (reminderVisible) {
			eventHotelReminderManagementComposite = new EventHotelReminderManagementComposite(
				reminderComposite,
				SWT.NONE,
				eventPK,
				hotelPK
			);
			eventHotelReminderManagementComposite.addModifyListener(modifySupport);
		}
	}


	private Group createGroup(Composite parent, int number) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		group.setText(HotelLabel.EventHotelInfoCancelationTerm.getString() + " " + number);
		group.setLayout(new GridLayout(7, false));
		return group;
	}


	private DateComposite createDateWidgets(Composite composite, String labelText) {
		createLabel(composite, labelText);

		DateComposite dateComposite = new DateComposite(composite, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		dateComposite.setLayoutData(layoutData);
		return dateComposite;
	}


	private DecimalNumberText createPercentWidgets(Composite parent, I18NString i18nString) {
		createLabel(parent, i18nString);

		DecimalNumberText percentNumberText = new DecimalNumberText(parent, SWT.BORDER);
		percentNumberText.setFractionDigits(1);
		percentNumberText.setNullAllowed(true);
		percentNumberText.setShowPercent(true);
		percentNumberText.setMaxValue(100);
		percentNumberText.setMinValue(0);
		percentNumberText.setValue((BigDecimal)null);

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(percentNumberText, 8);
		percentNumberText.setLayoutData(gd);

		percentNumberText.addModifyListener(modifySupport);

		return percentNumberText;
	}


	private Text createNoteWidgets(Composite parent, I18NString i18nString) {
		createLabel(parent, i18nString);

		Text cancelNoteText = new Text(parent, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		cancelNoteText.setLayoutData(gridData);
		cancelNoteText.addModifyListener(modifySupport);
		return cancelNoteText;
	}


	private Button createReminderButton(
		Composite parent,
		final DateComposite cancelDateComposite,
		final DecimalNumberText cancelPercentNumberText,
		final Text cancelNoteText
	) {
		final Button button = new Button(parent, SWT.PUSH);
		button.setText(I18N.EventHotelInfoCancelationTermsComposite_CreateReminder);


		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				boolean enabled =
					cancelDateComposite.getLocalDate() != null &&
					cancelPercentNumberText.getValue() != null &&
					StringHelper.isNotEmpty(cancelNoteText.getText());

				button.setEnabled(enabled);
			}
		};
		cancelDateComposite.addModifyListener(modifyListener);
		cancelPercentNumberText.addModifyListener(modifyListener);
		cancelNoteText.addModifyListener(modifyListener);


		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Event time: cancellation date at 00:00
				I18NDateMinute eventTime = cancelDateComposite.getI18NDate().atTime(0, 0);

				// Reminder time: the day before the cancellation date at 12:00
				I18NDateMinute reminderTime = eventTime.minusHours(12);

				// Subject: cancellation percent and hotel name
				String subject = null;
				try {
					String percent = cancelPercentNumberText.getText();

					Long hotelID = eventHotelInfoVO.getHotelPK();
					Hotel hotel = HotelModel.getInstance().getHotel(hotelID);

					subject = hotel.getName1() + " - " + percent;
				}
				catch (Exception exc) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), exc);
				}

				String text = cancelNoteText.getText();

				eventHotelReminderManagementComposite.createNewEventHotelReminderVO(
					eventTime,
					reminderTime,
					subject,
					text
				);
			}
		});

		return button;
	}


	public void setEventHotelInfoVO(EventHotelInfoVO eventHotelInfoVO) {
		this.eventHotelInfoVO = eventHotelInfoVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventHotelInfoVO != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						cancelDateWithHotel1Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithHotel1() );
						cancelDateWithHotel2Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithHotel2() );
						cancelDateWithHotel3Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithHotel3() );
						cancelDateWithHotel4Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithHotel4() );
						cancelDateWithHotel5Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithHotel5() );

						cancelPercentWithHotel1NumberText.setValue(eventHotelInfoVO.getCancelPercentWithHotel1());
						cancelPercentWithHotel2NumberText.setValue(eventHotelInfoVO.getCancelPercentWithHotel2());
						cancelPercentWithHotel3NumberText.setValue(eventHotelInfoVO.getCancelPercentWithHotel3());
						cancelPercentWithHotel4NumberText.setValue(eventHotelInfoVO.getCancelPercentWithHotel4());
						cancelPercentWithHotel5NumberText.setValue(eventHotelInfoVO.getCancelPercentWithHotel5());

						cancelNoteWithHotel1Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithHotel1()));
						cancelNoteWithHotel2Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithHotel2()));
						cancelNoteWithHotel3Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithHotel3()));
						cancelNoteWithHotel4Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithHotel4()));
						cancelNoteWithHotel5Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithHotel5()));

						cancelDateWithGroup1Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithGroup1() );
						cancelDateWithGroup2Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithGroup2() );
						cancelDateWithGroup3Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithGroup3() );
						cancelDateWithGroup4Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithGroup4() );
						cancelDateWithGroup5Composite.setI18NDate( eventHotelInfoVO.getCancelDateWithGroup5() );

						cancelPercentWithGroup1NumberText.setValue(eventHotelInfoVO.getCancelPercentWithGroup1());
						cancelPercentWithGroup2NumberText.setValue(eventHotelInfoVO.getCancelPercentWithGroup2());
						cancelPercentWithGroup3NumberText.setValue(eventHotelInfoVO.getCancelPercentWithGroup3());
						cancelPercentWithGroup4NumberText.setValue(eventHotelInfoVO.getCancelPercentWithGroup4());
						cancelPercentWithGroup5NumberText.setValue(eventHotelInfoVO.getCancelPercentWithGroup5());

						cancelNoteWithGroup1Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithGroup1()));
						cancelNoteWithGroup2Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithGroup2()));
						cancelNoteWithGroup3Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithGroup3()));
						cancelNoteWithGroup4Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithGroup4()));
						cancelNoteWithGroup5Text.setText(avoidNull(eventHotelInfoVO.getCancelNoteWithGroup5()));

						if (reminderVisible) {
							eventHotelReminderManagementComposite.setEventHotelInfoVO(eventHotelInfoVO);
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
		if (eventHotelInfoVO != null) {
			eventHotelInfoVO.setCancelDateWithHotel1( cancelDateWithHotel1Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithHotel2( cancelDateWithHotel2Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithHotel3( cancelDateWithHotel3Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithHotel4( cancelDateWithHotel4Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithHotel5( cancelDateWithHotel5Composite.getI18NDate() );

			eventHotelInfoVO.setCancelPercentWithHotel1( cancelPercentWithHotel1NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithHotel2( cancelPercentWithHotel2NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithHotel3( cancelPercentWithHotel3NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithHotel4( cancelPercentWithHotel4NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithHotel5( cancelPercentWithHotel5NumberText.getValue() );

			eventHotelInfoVO.setCancelNoteWithHotel1( cancelNoteWithHotel1Text.getText() );
			eventHotelInfoVO.setCancelNoteWithHotel2( cancelNoteWithHotel2Text.getText() );
			eventHotelInfoVO.setCancelNoteWithHotel3( cancelNoteWithHotel3Text.getText() );
			eventHotelInfoVO.setCancelNoteWithHotel4( cancelNoteWithHotel4Text.getText() );
			eventHotelInfoVO.setCancelNoteWithHotel5( cancelNoteWithHotel5Text.getText() );

			eventHotelInfoVO.setCancelDateWithGroup1( cancelDateWithGroup1Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithGroup2( cancelDateWithGroup2Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithGroup3( cancelDateWithGroup3Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithGroup4( cancelDateWithGroup4Composite.getI18NDate() );
			eventHotelInfoVO.setCancelDateWithGroup5( cancelDateWithGroup5Composite.getI18NDate() );

			eventHotelInfoVO.setCancelPercentWithGroup1( cancelPercentWithGroup1NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithGroup2( cancelPercentWithGroup2NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithGroup3( cancelPercentWithGroup3NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithGroup4( cancelPercentWithGroup4NumberText.getValue() );
			eventHotelInfoVO.setCancelPercentWithGroup5( cancelPercentWithGroup5NumberText.getValue() );

			eventHotelInfoVO.setCancelNoteWithGroup1( cancelNoteWithGroup1Text.getText() );
			eventHotelInfoVO.setCancelNoteWithGroup2( cancelNoteWithGroup2Text.getText() );
			eventHotelInfoVO.setCancelNoteWithGroup3( cancelNoteWithGroup3Text.getText() );
			eventHotelInfoVO.setCancelNoteWithGroup4( cancelNoteWithGroup4Text.getText() );
			eventHotelInfoVO.setCancelNoteWithGroup5( cancelNoteWithGroup5Text.getText() );

			if (reminderVisible) {
				eventHotelReminderManagementComposite.syncEntityToWidgets();
			}
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
