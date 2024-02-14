package de.regasus.programme.waitlist.editor;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.ui.Activator;

enum WaitlistTableColumns {POSITION, NEW_TIME, OFFERING_DESCRIPTION, BENEFIT_RECIPIENT_NAME, INVOICE_RECIPIENT_NAME, AMOUNT};

public class WaitlistTable extends SimpleTable<ProgrammeBookingCVO, WaitlistTableColumns> {

	private String language = Locale.getDefault().getLanguage();
	private DateFormat dateFormat;

	private ParticipantModel participantModel;


	/**
	 * The waitlist table is not sortable, because the displayed order of the participants should always
	 * reflect the position in the waitlist.
	 *
	 * @param table
	 */
	public WaitlistTable(Table table) {
		super(table, WaitlistTableColumns.class, true/*sortable*/);
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

		participantModel = ParticipantModel.getInstance();

		super.getViewer().addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				openParticipantEditor();
			}
		});
	}


	private void openParticipantEditor() {
		ISelection selection = super.getViewer().getSelection();
		ProgrammeBookingCVO programmeBookingCVO = SelectionHelper.getUniqueSelected(selection);
		if (programmeBookingCVO != null) {
			Long participantID = programmeBookingCVO.getVO().getBenefitRecipientPK();
			ParticipantEditorInput participantEditorInput = ParticipantEditorInput.getEditInstance(participantID);

			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					participantEditorInput,
					ParticipantEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		ProgrammeBookingCVO bookingCVO,
		WaitlistTableColumns column
	) {

		switch (column) {
			case POSITION:
				return bookingCVO.getWaitPosition();

			default:
				return super.getColumnComparableValue(bookingCVO, column);
		}
	}


	@Override
	public String getColumnText(ProgrammeBookingCVO programmeBookingCVO, WaitlistTableColumns column) {
		String label = null;

		try {
			ProgrammeBookingVO pbVO = programmeBookingCVO.getProgrammeBookingVO();

			switch (column) {
    			case POSITION:
    				Integer pos = pbVO.getWaitPosition();
    				if (pos != null) {
    					label = pos.toString();
    				}
    				break;
    			case NEW_TIME:
    				Date date = pbVO.getNewTime();
    				if (date != null) {
    					label = dateFormat.format(date);
    				}
    				break;
    			case OFFERING_DESCRIPTION:
    				label = programmeBookingCVO.getDescription().getString();
    				break;
    			case BENEFIT_RECIPIENT_NAME: {
    				Long participantID = pbVO.getBenefitRecipientPK();
    				Participant p = participantModel.getParticipant(participantID);
    				label = p.getName(true);
    				break;
    			}
    			case INVOICE_RECIPIENT_NAME: {
    				Long participantID = pbVO.getInvoiceRecipientPK();
    				Participant p = participantModel.getParticipant(participantID);
    				label = p.getName(true);
    				break;
    			}
    			case AMOUNT:
    				CurrencyAmount amount = pbVO.getCurrencyAmountGross();
    				label = amount.format(language, false, true);
    				break;
    			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		if (label == null) {
			label = ""; 
		}

		return label;
	}


	@Override
	protected WaitlistTableColumns getDefaultSortColumn() {
		return WaitlistTableColumns.POSITION;
	}

}
