package de.regasus.programme.programmepoint.editor;

import static com.lambdalogic.util.StringHelper.*;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.email.EmailStatus;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.common.CommonI18N;
import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.programmepointtype.combo.ProgrammePointTypeCombo;
import de.regasus.ui.Activator;

public class ProgrammePointGeneralComposite extends Composite {

	// the entity
	private ProgrammePointVO programmePointVO;

	private List<Language> languageList;

	private ModifySupport modifySupport = new ModifySupport(this);

	// Widget
	private I18NComposite<ProgrammePointVO> i18nComposite;
	private ProgrammePointTypeCombo ppTypeCombo;
	private Text externalIdText;
	private Button onsiteButton;
	private Button onlineButton;
	private DateTimeComposite startTime;
	private DateTimeComposite endTime;

	private NullableSpinner maxSpinner;
	private NullableSpinner minSpinner;
	private NullableSpinner warnSpinner;
	private Text warnEmailText;
	private Text warnEmailStatusText;
	private Text warnEmailMessageText;
	private Button resetEmailStatusButton;
	private Button requiredAllButton;
	private Button registrationButton;
	private Button waitListButton;
	private Button autoMoveUpButton;
	private Button withNoteButton;
	private Button demandWorkGroupButton;
	private Button requiredGroupButton;

	private boolean showWaitList = false;
	private boolean showExternalId = false;


	public ProgrammePointGeneralComposite(
		Composite parent,
		int style,
		List<Language> languageList,
		ConfigParameterSet configParameterSet
	) {
		super(parent, style);

		this.languageList = languageList;

		if (configParameterSet == null) {
			configParameterSet = new ConfigParameterSet();
		}
		showWaitList = configParameterSet.getEvent().getProgramme().getWaitList().isVisible();
		showExternalId = configParameterSet.getEvent().getExternalId().isVisible();

		createWidgets();
	}


	private void createWidgets() {
		setLayout(new GridLayout(4, false));

		i18nComposite = createI18NPart(this);
		GridDataFactory.fillDefaults().span(4, 1).grab(true, false).applyTo(i18nComposite);

		Composite topPart = createTypePart(this);
		topPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		Composite countPart = createCountPart(this);
		countPart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		Composite checkBoxGroup = createCheckBoxPart(this);
		checkBoxGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
	}


	private I18NComposite<ProgrammePointVO> createI18NPart(Composite parent) {
		I18NComposite<ProgrammePointVO> i18nComposite = new I18NComposite<>(
			parent,
			SWT.BORDER,
			languageList,
			new ProgrammePointI18NWidgetController()
		);
		i18nComposite.addModifyListener(modifySupport);
		return i18nComposite;
	}


	private Composite createTypePart(Composite parent) {
		try {
			final int NUM_COLUMN = 4;

			// Group
			Group group = new Group(parent, SWT.NONE);
			group.setLayout(new GridLayout(NUM_COLUMN, false));


			GridDataFactory labelGridDataFactory = GridDataFactory
				.swtDefaults()
				.align(SWT.RIGHT, SWT.CENTER);

			GridDataFactory controlGridDataFactory = GridDataFactory
				.swtDefaults()
				.align(SWT.FILL, SWT.CENTER);

			GridDataFactory dateTimeControlGridDataFactory = GridDataFactory
				.swtDefaults()
				.align(SWT.LEFT, SWT.CENTER);



			// Programme Point Type
			{
    			Label label = new Label(group, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(UtilI18N.Type);

    			ppTypeCombo = new ProgrammePointTypeCombo(group, SWT.BORDER);
    			controlGridDataFactory.copy().span(NUM_COLUMN - 1, 1).applyTo(ppTypeCombo);

    			ppTypeCombo.addModifyListener(modifySupport);
			}


			// External ID
			if (showExternalId) {
    			Label label = new Label(group, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText( CommonI18N.ExternalID.getString() );

    			externalIdText = new Text(group, SWT.BORDER);
    			controlGridDataFactory.applyTo(externalIdText);

    			externalIdText.addModifyListener(modifySupport);
			}
			else {
				new Label(group, SWT.NONE);
				new Label(group, SWT.NONE);
			}

			SWTHelper.verticalSpace(group, 5);


			// onsite
			{
				new Label(group, SWT.NONE);

				onsiteButton = new Button(group, SWT.CHECK);
				dateTimeControlGridDataFactory.applyTo(onsiteButton);
				onsiteButton.setText( ParticipantLabel.ProgrammePoint_Physical.getString() );
				onsiteButton.setToolTipText( ParticipantLabel.ProgrammePoint_Physical_description.getString() );

				onsiteButton.addSelectionListener(modifySupport);
			}


			// online
			{
				new Label(group, SWT.NONE);

				onlineButton = new Button(group, SWT.CHECK);
				dateTimeControlGridDataFactory.applyTo(onlineButton);
				onlineButton.setText( ParticipantLabel.ProgrammePoint_Virtual.getString() );
				onlineButton.setToolTipText( ParticipantLabel.ProgrammePoint_Virtual_description.getString() );

				onlineButton.addSelectionListener(modifySupport);
			}


			// startTime
			{
				Label label = new Label(group, SWT.NONE);
				labelGridDataFactory.applyTo(label);
				label.setText( ParticipantLabel.ProgrammePoint_StartTime.getString() );

				startTime = new DateTimeComposite(group, SWT.NONE);
				dateTimeControlGridDataFactory.applyTo(startTime);
				WidgetSizer.setWidth(startTime);

				startTime.addModifyListener(modifySupport);
			}


			// endTime
			{
				Label label = new Label(group, SWT.NONE);
				labelGridDataFactory.applyTo(label);
				label.setText( ParticipantLabel.ProgrammePoint_EndTime.getString() );

				endTime = new DateTimeComposite(group, SWT.NONE);
				dateTimeControlGridDataFactory.applyTo(endTime);
				WidgetSizer.setWidth(endTime);

				endTime.addModifyListener(modifySupport);
			}

			return group;
		}
		catch (Exception e) {
			throw new RuntimeException();
		}
	}


	private Composite createCountPart(Composite parent) {
		// Group
		Group group = new Group(parent, SWT.NONE);
		group.setText(ParticipantLabel.ParticipantQuantity.getString());
		group.setLayout(new GridLayout(5, false));


		// maximum number
		Label maxLabel = new Label(group, SWT.NONE);
		maxLabel.setText(UtilI18N.Maximal);
		maxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		maxSpinner = new NullableSpinner(group, SWT.NONE);
		maxSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		maxSpinner.setMinimum(ProgrammePointVO.MIN_MAX_NUMBER);
		maxSpinner.setMaximum(ProgrammePointVO.MAX_MAX_NUMBER);
		WidgetSizer.setWidth(maxSpinner);

		maxSpinner.addModifyListener(modifySupport);


		// horizontal line
		Label separatorLine = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));


		// warn number
		Label warnLabel = new Label(group, SWT.NONE);
		warnLabel.setText(UtilI18N.Warning);
		warnLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		warnSpinner = new NullableSpinner(group, SWT.NONE);
		warnSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		warnSpinner.setMinimum(ProgrammePointVO.MIN_WARN_NUMBER);
		warnSpinner.setMaximum(ProgrammePointVO.MAX_WARN_NUMBER);
		WidgetSizer.setWidth(warnSpinner);

		warnSpinner.addModifyListener(modifySupport);


		// warn email
		Label emailLabel = new Label(group, SWT.NONE);
		emailLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		emailLabel.setText(ContactLabel.email.getString());

		warnEmailText = new Text(group, SWT.BORDER);
		warnEmailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		warnEmailText.setTextLimit(ProgrammePointVO.MAX_LENGTH_WARN_EMAIL);

		warnEmailText.addModifyListener(modifySupport);


		// placeholder
		new Label(group, SWT.NONE).setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));


		// warn email status
		Label warnEmailStatusLabel = new Label(group, SWT.NONE);
		warnEmailStatusLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		warnEmailStatusLabel.setText(ParticipantLabel.ProgrammePoint_WarnEmailStatus.getString());

		warnEmailStatusText = new Text(group, SWT.BORDER);
		warnEmailStatusText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		warnEmailStatusText.setTextLimit(ProgrammePointVO.MAX_LENGTH_WARN_EMAIL);
		SWTHelper.disableTextWidget(warnEmailStatusText);

		warnEmailStatusText.addModifyListener(modifySupport);


		// Button to reset warn email status and message
		resetEmailStatusButton = new Button(group, SWT.PUSH);
		resetEmailStatusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		resetEmailStatusButton.setText(UtilI18N.Reset);
		resetEmailStatusButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				programmePointVO.setWarnEmailStatus(EmailStatus.OPEN);
				warnEmailStatusText.setText(EmailStatus.OPEN.getString());

				programmePointVO.setWarnEmailMessage(null);
				warnEmailMessageText.setText("");
			}
		});


		// placeholder
		new Label(group, SWT.NONE).setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));


		// warn email message
		new Label(group, SWT.NONE);
//		Label warnEmailMessageLabel = new Label(countGroup, SWT.NONE);
//		warnEmailMessageLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		warnEmailMessageLabel.setText("Email-Message");

		warnEmailMessageText = new Text(group, SWT.BORDER);
		warnEmailMessageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		warnEmailMessageText.setTextLimit(ProgrammePointVO.MAX_LENGTH_WARN_EMAIL);
		SWTHelper.disableTextWidget(warnEmailMessageText);

		warnEmailMessageText.addModifyListener(modifySupport);


		// placeholder
		new Label(group, SWT.NONE);


		// horizontal line
		separatorLine = new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));



		// minimum number
		Label minLabel = new Label(group, SWT.NONE);
		minLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		minLabel.setText(UtilI18N.Minimal);

		minSpinner = new NullableSpinner(group, SWT.NONE);
		minSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		minSpinner.setMinimum(ProgrammePointVO.MIN_MIN_NUMBER);
		minSpinner.setMaximum(ProgrammePointVO.MAX_MIN_NUMBER);
		WidgetSizer.setWidth(minSpinner);

		minSpinner.addModifyListener(modifySupport);

		return group;
	}


	/**
	 * Create the check buttons.
	 */
	private Composite createCheckBoxPart(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		// no text

		group.setLayout(new GridLayout(2, false));

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();
//		final GridData checkBoxGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);

		// **************************************************************************
		// * Row 1
		// *

		// Registration
		registrationButton = new Button(group, SWT.CHECK);
		registrationButton.setText(ParticipantLabel.ProgrammePoint_Registration.getString());
		registrationButton.setToolTipText(ParticipantLabel.ProgrammePoint_Registration_description.getString());
		gridDataFactory.applyTo(registrationButton);

		registrationButton.addSelectionListener(modifySupport);

		// Mit Benachrichtigung
		withNoteButton = new Button(group, SWT.CHECK);
		withNoteButton.setText(ParticipantLabel.ProgrammePoint_WithNotification.getString());
		withNoteButton.setToolTipText(ParticipantLabel.ProgrammePoint_WithNotification_description.getString());
		gridDataFactory.applyTo(withNoteButton);

		withNoteButton.addSelectionListener(modifySupport);

		// *
		// * Row 1
		// **************************************************************************

		// **************************************************************************
		// * Row 2
		// *

		// isRequiredAll
		requiredAllButton = new Button(group, SWT.CHECK);
		requiredAllButton.setText(ParticipantLabel.ProgrammePoint_RequiredAll.getString());
		requiredAllButton.setToolTipText(ParticipantLabel.ProgrammePoint_RequiredAll_description.getString());
		gridDataFactory.applyTo(requiredAllButton);

		requiredAllButton.addSelectionListener(modifySupport);

		// isRequiredGroup
		requiredGroupButton = new Button(group, SWT.CHECK);
		requiredGroupButton.setText(ParticipantLabel.ProgrammePoint_RequiredGroup.getString());
		requiredGroupButton.setToolTipText(ParticipantLabel.ProgrammePoint_RequiredGroup_description.getString());
		gridDataFactory.applyTo(requiredGroupButton);

		requiredGroupButton.addSelectionListener(modifySupport);

		// *
		// * Row 2
		// **************************************************************************

		// **************************************************************************
		// * Row 3
		// *

		// Warteliste
		if (showWaitList) {
    		waitListButton = new Button(group, SWT.CHECK);
    		waitListButton.setText(ParticipantLabel.ProgrammePoint_WaitList.getString());
    		waitListButton.setToolTipText(ParticipantLabel.ProgrammePoint_WaitList_description.getString());
    		gridDataFactory.applyTo(waitListButton);

    		waitListButton.addSelectionListener(modifySupport);

    		// Automatisches Nachrücken in Warteliste
    		autoMoveUpButton = new Button(group, SWT.CHECK);
    		autoMoveUpButton.setText(ParticipantLabel.ProgrammePoint_AutoMoveUp.getString());
    		autoMoveUpButton.setToolTipText(ParticipantLabel.ProgrammePoint_AutoMoveUp_description.getString());
    		gridDataFactory.applyTo(autoMoveUpButton);
    		// Solange die Funktion serverseitig nicht implementiert ist, wird dieser Button ausgeblendet.
    		autoMoveUpButton.setVisible(false);

    		autoMoveUpButton.addSelectionListener(modifySupport);

    		//autoMoveUpButton.setVisible(false);
		}

		// *
		// * Row 3
		// **************************************************************************

		// **************************************************************************
		// * Row 4
		// *

		// Demands WorkGroup
		demandWorkGroupButton = new Button(group, SWT.CHECK);
		demandWorkGroupButton.setText(ParticipantLabel.ProgrammePoint_DemandsWorkGroup.getString());
		demandWorkGroupButton.setToolTipText(ParticipantLabel.ProgrammePoint_DemandsWorkGroup_description.getString());
		gridDataFactory.applyTo(demandWorkGroupButton);

		demandWorkGroupButton.addSelectionListener(modifySupport);

		// *
		// * Row 4
		// **************************************************************************

		return group;
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


	private void syncWidgetsToEntity() {
		if (programmePointVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						i18nComposite.setEntity(programmePointVO);

						ppTypeCombo.setProgrammePointTypePK( programmePointVO.getTypePK() );
						if (externalIdText != null) {
							externalIdText.setText( avoidNull(programmePointVO.getExternalId()) );
						}
						onsiteButton.setSelection( programmePointVO.isPhysical() );
						onlineButton.setSelection( programmePointVO.isVirtual() );
						startTime.setDate( programmePointVO.getStartTime() );
						endTime.setDate( programmePointVO.getEndTime() );

						maxSpinner.setValue( programmePointVO.getMaxNumber() );
						minSpinner.setValue( programmePointVO.getMinNumber() );
						warnSpinner.setValue( programmePointVO.getWarnNumber() );
						warnEmailText.setText( avoidNull(programmePointVO.getWarnEmail() ));
						warnEmailStatusText.setText( programmePointVO.getWarnEmailStatus().getString() );
						warnEmailMessageText.setText( avoidNull(programmePointVO.getWarnEmailMessage()) );

						resetEmailStatusButton.setEnabled(
							programmePointVO.getWarnEmailStatus() != EmailStatus.OPEN ||
							programmePointVO.getWarnEmailMessage() != null
						);


						requiredAllButton.setSelection( programmePointVO.isRequiredAll() );
						registrationButton.setSelection( programmePointVO.isRegistration() );

						if (showWaitList) {
							waitListButton.setSelection( programmePointVO.isWaitList() );
							autoMoveUpButton.setSelection( programmePointVO.isAutoMoveUp() );
						}

						withNoteButton.setSelection( programmePointVO.isWithNote() );
						demandWorkGroupButton.setSelection( programmePointVO.isDemandsWorkGroup() );
						requiredGroupButton.setSelection( programmePointVO.isRequiredGroup() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (programmePointVO != null) {
			i18nComposite.syncEntityToWidgets();

			programmePointVO.setTypePK( ppTypeCombo.getProgrammePointTypePK() );
			if (externalIdText != null) {
				programmePointVO.setExternalId( externalIdText.getText() );
			}

			programmePointVO.setStartTime( startTime.getDate());
			programmePointVO.setEndTime( endTime.getDate() );
			programmePointVO.setPhysical( onsiteButton.getSelection() );
			programmePointVO.setVirtual( onlineButton.getSelection() );
			programmePointVO.setMaxNumber(maxSpinner.getValueAsInteger());
			programmePointVO.setMinNumber(minSpinner.getValueAsInteger());
			programmePointVO.setWarnNumber(warnSpinner.getValueAsInteger());
			programmePointVO.setWarnEmail(trim(warnEmailText.getText()));

			programmePointVO.setRequiredAll(requiredAllButton.getSelection());
			programmePointVO.setRegistration(registrationButton.getSelection());

			if (showWaitList) {
				programmePointVO.setWaitList(waitListButton.getSelection());
				programmePointVO.setAutoMoveUp(autoMoveUpButton.getSelection());
			}

			programmePointVO.setWithNote(withNoteButton.getSelection());
			programmePointVO.setDemandsWorkGroup(demandWorkGroupButton.getSelection());
			programmePointVO.setRequiredGroup(requiredGroupButton.getSelection());
		}
	}


	/**
	 * Set programm point VO entity in all GUI components that need it.
	 * @param programmePointVO Programm point VO to set.
	 */
	public void setProgrammePointVO(ProgrammePointVO programmePointVO) {
		this.programmePointVO = programmePointVO;
		syncWidgetsToEntity();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
