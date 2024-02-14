package de.regasus.participant.editor.hotelbooking;

import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.SmokerType;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;


/**
 * Tooltip for the Table used in the {@link HotelBookingsTableComposite}. Shows much of the information otherwise
 * to be seen in the details dialog.
 *
 * @author manfred
 *
 */
class HotelBookingTableToolTip extends DefaultToolTip {
	private Table outerTable;

	private Table innerTable;


	HotelBookingTableToolTip(Table table) {
		super(table);
		this.outerTable = table;
	}


	@Override
	protected boolean shouldCreateToolTip(Event event) {
		return (getHoveredElement(event) != null && super.shouldCreateToolTip(event));
	}


	private HotelBookingCVO getHoveredElement(Event event) {
		TableItem item = outerTable.getItem(new Point(event.x, event.y));
		if (item != null) {
			Object data = item.getData();

			if (data instanceof HotelBookingCVO) {
				HotelBookingCVO bookingCVO = (HotelBookingCVO) data;
				return bookingCVO;
			}
		}
		return null;
	}


	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		composite.setLayout(layout);
		Color fgColor = getForegroundColor(event);
		Color bgColor = getBackgroundColor(event);
		Font font = getFont(event);

		innerTable = new Table(composite, SWT.NO_SCROLL);
		TableColumn column1 = new TableColumn(innerTable, SWT.NONE);
		TableColumn column2 = new TableColumn(innerTable, SWT.NONE);

		createItemsForBooking(event);

		column1.pack();
		column1.setWidth(column1.getWidth() + 20);
		column2.pack();

		if (fgColor != null) {
			innerTable.setForeground(fgColor);
		}

		if (bgColor != null) {
			innerTable.setBackground(bgColor);
			composite.setBackground(bgColor);
		}
		if (font != null) {
			innerTable.setFont(font);
		}

		return composite;
	}


	private void createItemsForBooking(Event event) {
		HotelBookingCVO hotelBookingCVO = getHoveredElement(event);
		HotelBookingVO hotelBookingVO = hotelBookingCVO.getHotelBookingVO();
		FormatHelper fh = FormatHelper.getDefaultLocaleInstance();

		createEntry(UtilI18N.Date, fh.formatDateTime(hotelBookingVO.getBookingDate()));
		Date cancelationDate = hotelBookingVO.getCancelationDate();
		if (cancelationDate != null) {
			createEntry(ParticipantLabel.CancellationDate, fh.formatDateTime(cancelationDate));
		}
		createEntry(UtilI18N.CreateDateTime, fh.formatDateTime(hotelBookingVO.getNewTime()));
		createEntry(UtilI18N.CreateUser, hotelBookingVO.getNewDisplayUserStr());
		createEntry(UtilI18N.EditDateTime, fh.formatDateTime(hotelBookingVO.getEditTime()));
		createEntry(UtilI18N.EditUser, hotelBookingVO.getEditDisplayUserStr());
		createMultiLineEntry(UtilI18N.Info, hotelBookingVO.getInfo());
		Date infoEditTime = hotelBookingVO.getInfoEditTime();
		if (infoEditTime != null) {
			createEntry(UtilI18N.InfoEditDateTime, fh.formatDateTime(infoEditTime));
		}

		HotelOfferingVO hotelOfferingVO = hotelBookingCVO.getHotelOfferingCVO().getHotelOfferingVO();
		StringBuilder sb = new StringBuilder();

		LanguageString description = hotelOfferingVO.getDescription();
		if (description != null) {
			sb.append(description.getString());
		}
		if (hotelOfferingVO.getCurrencyAmountGross() != null) {
			if (sb.length() > 0) {
				sb.append(" - ");
			}
			sb.append(hotelOfferingVO.getCurrencyAmountGross().format(false, true));
		}

		createEntry(HotelLabel.HotelOffering, sb.toString());

		TimeFormatter timeFormatter = TimeFormatter.getDateInstance(FormatStyle.SHORT);
		createEntry(HotelLabel.HotelBooking_Arrival, timeFormatter.format(hotelBookingCVO.getArrival()));
		createEntry(HotelLabel.HotelBooking_Departure, timeFormatter.format( hotelBookingCVO.getDeparture()));

		I18NString arrivalInfo = HotelLabel.HotelBooking_ArrivalInfo_Normal;
		if (hotelBookingVO.isEarlyArrival()) {
			arrivalInfo = HotelLabel.HotelBooking_ArrivalInfo_Early;
		}
		else if (hotelBookingVO.isLateArrival()) {
			arrivalInfo = HotelLabel.HotelBooking_ArrivalInfo_Late;
		}

		createEntry(HotelLabel.HotelBooking_ArrivalInfo, arrivalInfo);
		createEntry(HotelLabel.HotelBooking_TwinRoom, hotelBookingVO.isTwinRoom() ? UtilI18N.Yes : UtilI18N.No);

		SmokerType smokerType = hotelBookingVO.getSmokerType();
		if (smokerType != null) {
			createEntry(HotelLabel.SmokerInfo, smokerType.getString());
		}

		String hotelInfo = hotelBookingVO.getHotelInfo();
		if (hotelInfo != null) {
			createMultiLineEntry(HotelLabel.HotelBooking_HotelInfo.getString(), hotelInfo);
		}

		createEntry(HotelLabel.HotelBookingPaymentCondition, HotelBookingVO.getLabelForHotelBookingPaymentCondition(
			hotelBookingVO.getPaymentCondition())
		);

	}


	private void createMultiLineEntry(String prefix, LanguageString info) {
		if (info != null) {
			String infoString = info.getString();
			createMultiLineEntry(prefix, infoString);
		}
	}


	private void createMultiLineEntry(String prefix, String infoString) {
		List<String> lines = StringHelper.getLines(infoString);
		for(int i=0; i<lines.size(); i++) {
			if (i > 0) {
				prefix = "";
			}
			createEntry(prefix, lines.get(i));
		}
	}


	private void createEntry(I18NString i18nString, I18NString i18nDetail) {
		createEntry(i18nString.getString(), i18nDetail.getString());
	}


	private void createEntry(I18NString i18nString, String detail) {
		createEntry(i18nString.getString(), detail);
	}


	private void createEntry(String label, String detail) {
		TableItem tableItem = new TableItem(innerTable, SWT.NONE);
		tableItem.setText(new String[] { label, detail });

	}

}
