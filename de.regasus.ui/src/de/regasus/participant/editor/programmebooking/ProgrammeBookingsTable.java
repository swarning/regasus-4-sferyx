package de.regasus.participant.editor.programmebooking;


import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.ContactHelper;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupCVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


public class ProgrammeBookingsTable extends SimpleTable<ProgrammeBookingCVO, ProgrammeBookingsTableColumns> {

	protected Participant participant;

	protected ParticipantModel participantModel;


	public void setParticipant(Participant participant) {
		this.participant = participant;
	}


	public ProgrammeBookingsTable(Table table) {
		super(table, ProgrammeBookingsTableColumns.class);
		
		participantModel = ParticipantModel.getInstance();

		// activate the tooltip support
		ColumnViewerToolTipSupport.enableFor(getViewer(), ToolTip.NO_RECREATE);
	}


	@Override
	public Image getColumnImage(ProgrammeBookingCVO programmeBookingCVO, ProgrammeBookingsTableColumns column) {
		switch (column) {
			case DISPLAY_INDEX:
				LanguageString info = programmeBookingCVO.getInfo();
				boolean hasInfo = info != null && !info.isEmpty();
				if (hasInfo) {
					if (SystemHelper.isWindows()) {
						return IconRegistry.getImage("icons/info-windows.gif");
					}
					else {
						return IconRegistry.getImage("icons/info.gif");
					}
				}
				break;

			case TOTAL_PRICE:
				if (Boolean.TRUE.equals(programmeBookingCVO.isBalanced())) {
					return IconRegistry.getImage("icons/tick.png");
				}
				break;

			default:
				return null;
		}
		return null;
	}


	@Override
	public String getColumnText(ProgrammeBookingCVO programmeBookingCVO, ProgrammeBookingsTableColumns column) {
		String label = null;

		try {
			ProgrammeBookingVO pbVO = programmeBookingCVO.getVO();

			switch (column) {
				case DISPLAY_INDEX:
					label = String.valueOf(programmeBookingCVO.getDisplayIndex());
					break;

				case DESCRIPTION: {
					String desc = programmeBookingCVO.getProgrammeOfferingCVO().getShortLabel().getString();
					desc = StringHelper.removeLineBreaks(desc);

					// mark as waitlist-booking
					if (programmeBookingCVO.isWaitList()) {
						desc =
							ParticipantLabel.WaitList.getString() +
							" [" + programmeBookingCVO.getWaitPosition() + "]" +
							": " + desc;
					}

					label = desc;
					break;
				}

				case WORKGROUP: {
					WorkGroupCVO workGroupCVO = programmeBookingCVO.getWorkGroupCVO();
					if (workGroupCVO != null) {
						String workGroupName = workGroupCVO.getName();
						if (pbVO.isWorkGroupFix()) {
							workGroupName += " FIX";
						}
						label = workGroupName;
					}
					break;
				}

				case BENEFIT_RECIPIENT: {
					Long participantID = pbVO.getBenefitRecipientPK();
					Participant p = participantModel.getParticipant(participantID);
					label = getName(p);
					break;
				}

				case INVOICE_RECIPIENT: {
					Long participantID = pbVO.getInvoiceRecipientPK();
					Participant p = participantModel.getParticipant(participantID);
					label = getName(p);
					break;
				}

				case MAIN_PRICE: {
					if (participant != null &&
						pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
						!pbVO.isWaitList()
					) {
						if (!pbVO.isCanceled()) {
							label = pbVO.getMainPriceVO().getCurrencyAmountGross().format(false, false);
						}
						else {
							label = pbVO.getCancelFeePriceVO().getCurrencyAmountGross().format(false, false);
						}
					}
					break;
				}

				case ADD1_PRICE:
					if (participant != null &&
						pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
						!pbVO.isWaitList()
					) {
						label = pbVO.getAdd1PriceVO().getCurrencyAmountGross().format(false, false);
					}
					break;

				case ADD2_PRICE:
					if (participant != null &&
						pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
						!pbVO.isWaitList()
					) {
						label = pbVO.getAdd2PriceVO().getCurrencyAmountGross().format(false, false);
					}
					break;

				case TOTAL_PRICE:
					if (participant != null &&
						pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
						!pbVO.isWaitList()
					) {
						label = pbVO.getCurrencyAmountGross().format(false, false);
					}
					break;

				default:
					// nothing
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
	public String getColumnToolTipText(ProgrammeBookingCVO programmeBookingCVO, ProgrammeBookingsTableColumns column) {
		String text = null;

		try {
			switch (column) {
				case TOTAL_PRICE:
					if ( programmeBookingCVO.isPaid() ) {
						text = "Bezahlt";
					}
					else {
						text = "Unbezahlt";
					}
					break;

				default:
					// nothing
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return text;
	}


	@Override
	public CellLabelProvider getColumnCellLabelProvider(ProgrammeBookingsTableColumns column) {
		CellLabelProvider labelProvider = null;

		if (column == ProgrammeBookingsTableColumns.DESCRIPTION) {
			labelProvider = new CancelledBookingStrikeOutLabelProvider(this, ProgrammeBookingsTableColumns.DESCRIPTION);
		}

		return labelProvider;
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		ProgrammeBookingCVO programmeBookingCVO,
		ProgrammeBookingsTableColumns column
	) {
		ProgrammeBookingVO pbVO = programmeBookingCVO.getVO();

		switch (column) {
			case DISPLAY_INDEX:
				return programmeBookingCVO.getDisplayIndex();

			case MAIN_PRICE:
				if (participant != null &&
					pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
					!pbVO.isWaitList()
				) {
					if (!pbVO.isCanceled()) {
						return pbVO.getMainPriceVO().getAmountGross();
					}
					else {
						return pbVO.getCancelFeePriceVO().getAmountGross();
					}
				}

			case ADD1_PRICE:
				if (participant != null &&
					pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
					!pbVO.isWaitList()
				) {
					return pbVO.getAdd1PriceVO().getAmountGross();
				}
				else {
					return null;
				}

			case ADD2_PRICE:
				if (participant != null &&
					pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
					!pbVO.isWaitList()
				) {
					return pbVO.getAdd2PriceVO().getAmountGross();
				}
				else {
					return null;
				}

			case TOTAL_PRICE:
				if (participant != null &&
					pbVO.getInvoiceRecipientPK().equals(participant.getID()) &&
					!pbVO.isWaitList()
				) {
					return pbVO.getTotalAmountBrutto();
				}
				else {
					return null;
				}

			default:
				return super.getColumnComparableValue(programmeBookingCVO, column);
		}
	}


	@Override
	protected ProgrammeBookingsTableColumns getDefaultSortColumn() {
		return ProgrammeBookingsTableColumns.DISPLAY_INDEX;
	}


	@Override
	protected void afterSorting(Object[] elements) {
		// reset the display index
		for (int i = 0; i < elements.length; i++) {
			ProgrammeBookingCVO bookingCVO = (ProgrammeBookingCVO) elements[i];
			bookingCVO.setDisplayIndex(i + 1);
		}
	}


	private String getName(Participant p) {
		String name = ContactHelper.formatName(
			true, // lastNameFirst
			p.getDegree(),
			p.getNobility(),
			p.getFirstName(),
			null,	// middleName,
			p.getNobilityPrefix(),
			p.getLastName(),
			null	// mandate
		);
		return name;
	}

}
