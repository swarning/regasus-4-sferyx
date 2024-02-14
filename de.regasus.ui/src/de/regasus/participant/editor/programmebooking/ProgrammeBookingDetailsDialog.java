package de.regasus.participant.editor.programmebooking;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantMessage;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupCVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.programme.WorkGroupModel;
import de.regasus.programme.workgroup.combo.WorkGroupCombo;
import de.regasus.ui.Activator;

class ProgrammeBookingDetailsDialog extends TitleAreaDialog implements ModifyListener {

	private boolean dirty = false;

	private ProgrammeBookingCVO programmeBookingCVO;

	private LanguageString info;

	private I18NText infoI18NText;

	private WorkGroupCombo workGroupCombo;

	private Button isWorkGroupFixButton;


	public ProgrammeBookingDetailsDialog(Shell parentShell, ProgrammeBookingCVO programmeBookingCVO) {
		super(parentShell);
		this.programmeBookingCVO = programmeBookingCVO;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(I18N.BookingDetails);

		String offeringLabel = programmeBookingCVO.getProgrammeOfferingCVO().getShortLabel().getString();
		// Removing possibly newlines, without introducing glued-together-words or double-spaces
		offeringLabel = offeringLabel.replace("(\n", "(");
		offeringLabel = offeringLabel.replace("\n", " ");
		offeringLabel = offeringLabel.replace("  ", " ");

		setMessage(offeringLabel);

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite mainComposite = new Composite(dialogArea, SWT.NONE);
		mainComposite.setLayoutData( new GridData(GridData.FILL_BOTH) );
		final int NUM_COLS = 2;
		mainComposite.setLayout(new GridLayout(NUM_COLS, false));

		try {
			FormatHelper fh = FormatHelper.getDefaultLocaleInstance();
			ProgrammeBookingVO bookingVO = programmeBookingCVO.getBookingVO();
			EventVO eventVO = EventModel.getInstance().getEventVO(bookingVO.getEventPK());

			// Info Button
			Button infoButton = buildInfoButton(mainComposite);
			GridDataFactory.swtDefaults()
				.align(SWT.RIGHT, SWT.CENTER)
				.span(NUM_COLS, 1)
				.applyTo(infoButton);

			createEntry(mainComposite, ParticipantLabel.ProgrammePoint.getString(), programmeBookingCVO.getPpName().getString());
			createEntry(mainComposite, ParticipantLabel.ProgrammeOffering.getString(), offeringLabel);
			createEntry(mainComposite, I18N.BookedAt, fh.formatDateTime(bookingVO.getBookingDate()));
			createEntry(mainComposite, I18N.CanceledAt, fh.formatDateTime(bookingVO.getCancelationDate()));
			createEntry(mainComposite, UtilI18N.CreateDateTime, fh.formatDateTime(bookingVO.getNewTime()));
			createEntry(mainComposite, UtilI18N.CreateUser, bookingVO.getNewDisplayUserStr());
			createEntry(mainComposite, UtilI18N.EditDateTime, fh.formatDateTime(bookingVO.getEditTime()));
			createEntry(mainComposite, UtilI18N.EditUser, bookingVO.getEditDisplayUserStr());

			{
				final Label infoLabel = new Label(mainComposite, SWT.RIGHT);
				infoLabel.setText(UtilI18N.Info);
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				infoLabel.setLayoutData(gridData);
			}
			{
				infoI18NText = new I18NText(mainComposite, SWT.MULTI, LanguageProvider.getInstance());
				infoI18NText.setLanguageString(bookingVO.getInfo(), eventVO.getLanguages());
				infoI18NText.setEnabled(!bookingVO.isCanceled());
				GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
				gridData.heightHint = 100;
				infoI18NText.setLayoutData(gridData);
				infoI18NText.addModifyListener(this);
			}

			createEntry(mainComposite, UtilI18N.InfoEditDateTime, fh.formatDateTime(bookingVO.getInfoEditTime()));

			BigDecimal openAmount = programmeBookingCVO.getOpenAmount();
			if (openAmount == null) {
				openAmount = BigDecimal.ZERO;
			}
			String currency = programmeBookingCVO.getBookingVO().getCurrency();
			String openAmountString = new CurrencyAmount(openAmount, currency).format(false, false);

			createEntry(mainComposite, ParticipantLabel.OpenAmount.getString(), openAmountString);

			// The following is for MIRCP-102, but only if there are work groups at all
			ProgrammeOfferingCVO programmeOfferingCVO = programmeBookingCVO.getProgrammeOfferingCVO();
			ProgrammePointCVO programmePointCVO = programmeOfferingCVO.getProgrammePointCVO();
			List<WorkGroupVO> wgVOs =
				WorkGroupModel.getInstance().getWorkGroupVOsByProgrammePointPK(programmePointCVO.getPK());

			if (!wgVOs.isEmpty()) {
				Group workGroupGroup = new Group(mainComposite, SWT.NONE);
				workGroupGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
				workGroupGroup.setText(ParticipantLabel.WorkGroup.getString());
				workGroupGroup.setLayout(new GridLayout(2, false));

				// WorkGroup Combo
				SWTHelper.createLabel(workGroupGroup, ParticipantLabel.WorkGroup.getString());

				WorkGroupCVO workGroupCVO = programmeBookingCVO.getWorkGroupCVO();

				workGroupCombo = new WorkGroupCombo(workGroupGroup, SWT.READ_ONLY);
				workGroupCombo.setProgrammePointPK(programmePointCVO.getPK());
				workGroupCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				workGroupCombo.addModifyListener(this);

				if (workGroupCVO != null) {
					workGroupCombo.setWorkGroupPK(workGroupCVO.getPK());
				}

				// Edit Time
				Date editTime = programmeBookingCVO.getProgrammeBookingVO().getWorkGroupEditTime();
				createEntry(workGroupGroup, UtilI18N.EditDateTime, fh.formatDateTime(editTime));

				// Fix
				SWTHelper.createLabel(workGroupGroup, ParticipantLabel.Fix.getString());
				isWorkGroupFixButton = new Button(workGroupGroup, SWT.CHECK);
				isWorkGroupFixButton.setSelection(programmeBookingCVO.getVO().isWorkGroupFix());
				isWorkGroupFixButton.addSelectionListener(new ModifyListenerAdapter(this));


				workGroupGroup.setEnabled(!bookingVO.isCanceled());
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return dialogArea;
	}


	private Button buildInfoButton(Composite parent) {
		Button infoButton = new Button(parent, SWT.NONE);
		infoButton.setToolTipText(CoreI18N.InfoButtonToolTip);
		infoButton.setImage(IconRegistry.getImage(
			de.regasus.core.ui.IImageKeys.INFORMATION
		));

		infoButton.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openInfoDialog();
			}
		});

		return infoButton;
	}


	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf( programmeBookingCVO.getId() )
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getShell(),
			I18N.ProgrammeBooking + ": " + UtilI18N.Info,
			labels,
			values
		);

		infoDialog.setSize(new Point(300, 120));

		infoDialog.open();
	}


	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(UtilI18N.Details);
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	@Override
	protected void okPressed() {
		if (dirty) {
			// The work group combo may possibly not shown if there is no work group at al
			Long workgoupPK = null;
			boolean isWorkGroupFix = false;
			if (workGroupCombo != null) {
				workgoupPK = workGroupCombo.getWorkGroupPK();
				isWorkGroupFix = isWorkGroupFixButton.getSelection();
			}


			LanguageString info = infoI18NText.getLanguageString();
			try {
				ProgrammeBookingModel.getInstance().updateProgrammeBooking(
					programmeBookingCVO.getVO(),
					info,
					workgoupPK,
					isWorkGroupFix
				);

				super.okPressed();
			}
			catch (ErrorMessageException e) {
				if (ParticipantMessage.OverlappingWorkGroup.name().equals(e.getErrorCode())) {
					RegasusErrorHandler.handleUserError(Activator.PLUGIN_ID, getClass().getName(), e, e.getMessage());
				}
				else {
					RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, e.getMessage());
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		else {
			super.okPressed();
		}
	}


	private void createEntry(Composite composite, String leftText, String rightText) {
		final Label label = new Label(composite, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText(StringHelper.avoidNull(leftText) + ":");

		final Label valueText = new Label(composite, SWT.NONE);
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		valueText.setText(StringHelper.avoidNull(rightText));
	}


	@Override
	public void modifyText(ModifyEvent e) {
		dirty = true;
	}


	public LanguageString getInfo() {
		return info;
	}

}
