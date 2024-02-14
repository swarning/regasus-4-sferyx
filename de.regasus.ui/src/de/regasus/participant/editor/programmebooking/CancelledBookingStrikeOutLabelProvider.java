package de.regasus.participant.editor.programmebooking;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import com.lambdalogic.messeinfo.invoice.data.BookingCVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;


/**
 * {@link CellLabelProvider} that is used to strike-out the text of a table cell.
 *
 * However, when doing so, the alignment setting of the cell get lost. The reason is that this class extends
 * {@link StyledCellLabelProvider} which paints the cell content on its own. So finally the text is always
 * aligned to the left.
 */
@SuppressWarnings("rawtypes")
public class CancelledBookingStrikeOutLabelProvider extends StyledCellLabelProvider {

	private SimpleTable simpleTable;
	private Enum columnEnum;



	private Styler strikeOutStyler = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.strikeout = true;
		}
	};


	public CancelledBookingStrikeOutLabelProvider(SimpleTable simpleTable, Enum columnEnum) {
		super();
		this.simpleTable = simpleTable;
		this.columnEnum = columnEnum;
	}


	@Override
	public void update(ViewerCell cell) {
		BookingCVO bookingCVO = (BookingCVO) cell.getElement();

		String text = simpleTable.getColumnText(bookingCVO, columnEnum);
		Image image = simpleTable.getColumnImage(bookingCVO, columnEnum);

		StyledString styledString = new StyledString(text);
		if ( bookingCVO.isCanceled() ) {
			styledString.setStyle(0, text.length(), strikeOutStyler);
		}

		cell.setImage(image);
		cell.setText(text);
		cell.setStyleRanges( styledString.getStyleRanges() );

		super.update(cell);
	}

}
