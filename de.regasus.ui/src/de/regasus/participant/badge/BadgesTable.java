package de.regasus.participant.badge;

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.data.BadgeCVO;
import com.lambdalogic.messeinfo.participant.data.BadgeVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.IconRegistry;

enum BadgesTableColumns {
	NO, ID, BARCODE, CREATED, BAD_TRIALS, DISABLED, LAST_SCANNED, TYPE
}

public class BadgesTable extends SimpleTable<BadgeCVO, BadgesTableColumns> {

	private FormatHelper formatHelper = FormatHelper.getDefaultLocaleInstance();

	private byte[] lastScannedCardId;

	public BadgesTable(Table table) {
		super(table, BadgesTableColumns.class);
	}

	public void setLastScannedCardId(byte[] lastScannedCardId) {
		this.lastScannedCardId = lastScannedCardId;
	}

	@Override
	public String getColumnText(BadgeCVO badgeCVO, BadgesTableColumns column) {
		BadgeVO badgeVO = badgeCVO.getVO();
		switch (column) {
		case NO:
			return String.valueOf(badgeVO.getBadgeNo());
		case ID:
			return StringHelper.bytesToHexString(badgeVO.getBadgeID());
		case BARCODE:
			return badgeVO.getBarcode();
		case CREATED:
			return formatHelper.formatDateTime(badgeVO.getNewTime());
		case BAD_TRIALS:
			return String.valueOf(badgeVO.getBadTrial());
		case DISABLED:
			return formatHelper.formatDateTime(badgeVO.getDisable());
		case LAST_SCANNED:
			return null;
		case TYPE:
			return String.valueOf(badgeVO.getType());
		}

		return null;
	}


	@Override
	protected java.lang.Comparable<? extends Object> getColumnComparableValue(
		BadgeCVO badgeCVO,
		BadgesTableColumns column
	) {
		BadgeVO badgeVO = badgeCVO.getVO();
		switch (column) {
			case NO:
				return badgeVO.getBadgeNo();
			case ID:
				return StringHelper.bytesToHexString(badgeVO.getBadgeID());
			case BARCODE:
				return badgeVO.getBarcode();
			case CREATED:
				return badgeVO.getNewTime();
			case BAD_TRIALS:
				return badgeVO.getBadTrial();
			case DISABLED:
				return badgeVO.getDisable();
			case LAST_SCANNED:
				return null;
			case TYPE:
				return String.valueOf(badgeVO.getType());
			default:
				return super.getColumnComparableValue(badgeCVO, column);
		}
	}


	@Override
	public Image getColumnImage(BadgeCVO badgeCVO, BadgesTableColumns column) {
		if (column == BadgesTableColumns.DISABLED) {
			if (badgeCVO.getVO().isDisabled()) {
				return IconRegistry.getImage("icons/cross.png");
			}
		}
		else if (column == BadgesTableColumns.LAST_SCANNED) {
			if (lastScannedCardId != null && Arrays.equals(lastScannedCardId, badgeCVO.getVO().getBadgeID())) {
				return IconRegistry.getImage("icons/tick.png");
			}
		}

		return null;
	}
}
