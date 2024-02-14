package de.regasus.participant.editor.hotelbooking;

import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.ContactHelper;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


class HotelBookingsTable extends SimpleTable<HotelBookingCVO, HotelBookingsTableColumns> {

	private Participant participant;

	protected ParticipantModel participantModel;

	private TimeFormatter timeFormatter = TimeFormatter.getDateInstance(FormatStyle.SHORT);



	public HotelBookingsTable(Table table) {
		super(table, HotelBookingsTableColumns.class);

		participantModel = ParticipantModel.getInstance();

		// activate the tooltip support
		ColumnViewerToolTipSupport.enableFor(getViewer(), ToolTip.NO_RECREATE);
	}


	public void setParticipant(Participant participant) {
		this.participant = participant;
	}


	@Override
	public Image getColumnImage(HotelBookingCVO hotelBookingCVO, HotelBookingsTableColumns column) {
		switch (column) {
			case DISPLAY_INDEX:
				LanguageString info = hotelBookingCVO.getInfo();
				boolean hasInfo =
					( info != null && !info.isEmpty() )
					||
					StringHelper.isNotEmpty( hotelBookingCVO.getHotelInfo() );

				if (hasInfo) {
					return IconRegistry.getImage("icons/info.gif");
				}
				break;

			case TOTAL_PRICE:
				boolean isPayed = Boolean.TRUE.equals(hotelBookingCVO.isBalanced());
				if (isPayed) {
					return IconRegistry.getImage("icons/tick.png");
				}
				break;

			default:
				return null;
		}
		return null;
	}


	@Override
	public String getColumnText(HotelBookingCVO hotelBookingCVO, HotelBookingsTableColumns column) {
		String label = null;

		try {
			HotelBookingVO hbVO = hotelBookingCVO.getVO();

			switch (column) {
				case DISPLAY_INDEX:
					label = String.valueOf(hotelBookingCVO.getDisplayIndex());
					break;

				case HOTEL:
					label = hotelBookingCVO.getLabelForHotelContingent();
					break;

				case HOTEL_OFFERING:
					label = hotelBookingCVO.getLabelForOffering();
					break;

				case ARRIVAL:
					label = timeFormatter.format( hotelBookingCVO.getArrival() );
					break;

				case DEPARTURE:
					label = timeFormatter.format( hotelBookingCVO.getDeparture() );
					break;

				case LODGE_PRICE:
					if (participant != null &&
						hbVO.getInvoiceRecipientPK().equals(participant.getID())
					) {
						if (!hbVO.isCanceled()) {
							label = hbVO.getLodgePriceVO().getCurrencyAmountGross().format(false, false);
						}
						else {
							label = hbVO.getCancelFeePriceVO().getCurrencyAmountGross().format(false, false);
						}
					}
					break;

				case BF_PRICE:
					if (participant != null &&
						hbVO.getInvoiceRecipientPK().equals(participant.getID())
					) {
						label = hbVO.getBfPriceVO().getCurrencyAmountGross().format(false, false);
					}
					break;

				case ADD1_PRICE:
					if (participant != null &&
						hbVO.getInvoiceRecipientPK().equals(participant.getID())
					) {
						label = hbVO.getAdd1PriceVO().getCurrencyAmountGross().format(false, false);
					}
					break;

				case ADD2_PRICE:
					if (participant != null &&
						hbVO.getInvoiceRecipientPK().equals(participant.getID())
					) {
						label = hbVO.getAdd2PriceVO().getCurrencyAmountGross().format(false, false);
					}
					break;

				case TOTAL_PRICE:
					if (participant != null &&
						hbVO.getInvoiceRecipientPK().equals(participant.getID())
					) {
						label = hbVO.getCurrencyAmountGross().format(false, false);
					}
					break;

				case BENEFIT_RECIPIENT: {
					List<Long> participantIDs = hbVO.getBenefitRecipientPKs();
					List<Participant> participants = participantModel.getParticipants(participantIDs);
					StringBuilder sb = new StringBuilder();

					int i = 0;
					for (Participant p : participants) {
						if (i++ > 0) {
							sb.append(" / ");
						}
						String name = getName(p);
						sb.append(name);
					}
					label = sb.toString();
					break;
				}

				case INVOICE_RECIPIENT: {
					Long participantID = hbVO.getInvoiceRecipientPK();
					Participant p = participantModel.getParticipant(participantID);
					label = getName(p);
					break;
				}

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
	public String getColumnToolTipText(HotelBookingCVO hotelBookingCVO, HotelBookingsTableColumns column) {
		String text = null;

		try {
			switch (column) {
				case TOTAL_PRICE:
					if ( hotelBookingCVO.isPaid() ) {
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
	public CellLabelProvider getColumnCellLabelProvider(HotelBookingsTableColumns column) {
		CellLabelProvider labelProvider = null;

		if (   column == HotelBookingsTableColumns.HOTEL) {
			labelProvider = new CancelledBookingStrikeOutLabelProvider(this, HotelBookingsTableColumns.HOTEL);
		}
		else if (   column == HotelBookingsTableColumns.HOTEL_OFFERING) {
			labelProvider = new CancelledBookingStrikeOutLabelProvider(this, HotelBookingsTableColumns.HOTEL_OFFERING);
		}
		else if (   column == HotelBookingsTableColumns.ARRIVAL) {
			labelProvider = new CancelledBookingStrikeOutLabelProvider(this, HotelBookingsTableColumns.ARRIVAL);
		}
		else if (   column == HotelBookingsTableColumns.DEPARTURE) {
			labelProvider = new CancelledBookingStrikeOutLabelProvider(this, HotelBookingsTableColumns.DEPARTURE);
		}

		return labelProvider;
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		HotelBookingCVO hotelBookingCVO,
		HotelBookingsTableColumns column
	) {
		try {
    		HotelBookingVO hbVO = hotelBookingCVO.getVO();

    		switch (column) {
    			case DISPLAY_INDEX:
    				return hotelBookingCVO.getDisplayIndex();

    			case LODGE_PRICE:
    				if (participant != null &&
    					hbVO.getInvoiceRecipientPK().equals(participant.getID())
    				) {
    					if (!hbVO.isCanceled()) {
    						return hbVO.getLodgePriceVO().getAmountGross();
    					}
    					else {
    						return hbVO.getCancelFeePriceVO().getAmountGross();
    					}
    				}

    			case BF_PRICE:
    				if (participant != null &&
    					hbVO.getInvoiceRecipientPK().equals(participant.getID())
    				) {
    					return hbVO.getBfPriceVO().getAmountGross();
    				}
    				break;

    			case ADD1_PRICE:
    				if (participant != null &&
    					hbVO.getInvoiceRecipientPK().equals(participant.getID())
    				) {
    					return hbVO.getAdd1PriceVO().getAmountGross();
    				}
    				break;

    			case ADD2_PRICE:
    				if (participant != null &&
    					hbVO.getInvoiceRecipientPK().equals(participant.getID())
    				) {
    					return hbVO.getAdd2PriceVO().getAmountGross();
    				}
    				break;

    			case TOTAL_PRICE:
    				if (participant != null &&
    					hbVO.getInvoiceRecipientPK().equals(participant.getID())
    				) {
    					return hbVO.getTotalAmountBrutto();
    				}
    				break;

    			default:
    				return super.getColumnComparableValue(hotelBookingCVO, column);
    		}
    	}
    	catch (Exception e) {
    		RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    	}

		return null;
	}


	@Override
	protected HotelBookingsTableColumns getDefaultSortColumn() {
		return HotelBookingsTableColumns.DISPLAY_INDEX;
	}


	/**
	 * When sorting for arrival or departure time, the 2nd sorting criterion is to be the guest name.
	 */
	@Override
	protected Comparator<HotelBookingCVO> getColumnComparator(HotelBookingsTableColumns column) {
		if (column == HotelBookingsTableColumns.ARRIVAL) {
			return new Comparator<HotelBookingCVO>(){
				@Override
				public int compare(HotelBookingCVO o1, HotelBookingCVO o2) {
					int resultByArrivalComparison = o1.getArrival().compareTo(o2.getArrival());
					if (resultByArrivalComparison == 0) {
						return o1.getBenefitRecipientNames(true).compareTo(o2.getBenefitRecipientNames(true));
					}
					else {
						return resultByArrivalComparison;
					}
				}
			};
		}
		else if (column == HotelBookingsTableColumns.DEPARTURE) {
			return new Comparator<HotelBookingCVO>(){
				@Override
				public int compare(HotelBookingCVO o1, HotelBookingCVO o2) {
					int resultByArrivalComparison = o1.getDeparture().compareTo(o2.getDeparture());
					if (resultByArrivalComparison == 0) {
						return o1.getBenefitRecipientNames(true).compareTo(o2.getBenefitRecipientNames(true));
					}
					else {
						return resultByArrivalComparison;
					}
				}
			};
		}
		return super.getColumnComparator(column);
	}


	@Override
	protected void afterSorting(Object[] elements) {
		// reset the display index
		for (int i = 0; i < elements.length; i++) {
			HotelBookingCVO bookingCVO = (HotelBookingCVO) elements[i];
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
