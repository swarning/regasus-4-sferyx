package de.regasus.hotel.contingent.editor;

import java.util.Collection;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;


enum RoomDefinitionTableColumns {
	USED, NAME, GUEST_COUNT
}

public class RoomDefinitionTable extends SimpleTable<RoomDefinitionVO, RoomDefinitionTableColumns> {

	private Collection<Long> usedRoomDefinitionPKs;


	public RoomDefinitionTable(Table table) {
		super(table, RoomDefinitionTableColumns.class);
	}


	@Override
	protected RoomDefinitionTableColumns getDefaultSortColumn() {
		return RoomDefinitionTableColumns.GUEST_COUNT;
	}

	/**
	 * When a column returns a CellEditor, all cells in it are editable.
	 */
	@Override
	public CellEditor getColumnCellEditor(Composite parent, RoomDefinitionTableColumns column) {
		switch (column) {
		case USED:
			return new CheckboxCellEditor(parent, SWT.NONE);
		default:
			return null;
		}
	}

	@Override
	public Image getColumnImage(RoomDefinitionVO roomDefinitionVO, RoomDefinitionTableColumns column) {
		switch (column) {
		case USED:
			if (isUsed(roomDefinitionVO)) {
				return IconRegistry.getImage(IImageKeys.CHECKED);
			}
			else {
				return IconRegistry.getImage(IImageKeys.UNCHECKED);
			}
		default:
			return null;
		}
	}


	private boolean isUsed(RoomDefinitionVO roomDefinitionVO) {
		return usedRoomDefinitionPKs != null && usedRoomDefinitionPKs.contains(roomDefinitionVO.getID());
	}


	@Override
	public String getColumnText(RoomDefinitionVO roomDefinitionVO, RoomDefinitionTableColumns column) {
		switch (column) {
    		case NAME:
    			return roomDefinitionVO.getName().getString();
    		case GUEST_COUNT:
    			return String.valueOf(roomDefinitionVO.getGuestQuantity());
    		default:
    			return "";
		}
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		RoomDefinitionVO roomDefinitionVO,
		RoomDefinitionTableColumns column) {

		switch (column) {
		case USED:
			return Boolean.valueOf(isUsed(roomDefinitionVO));
		case GUEST_COUNT:
			return roomDefinitionVO.getGuestQuantity();
		default:
			return super.getColumnComparableValue(roomDefinitionVO, column);
		}
	}


	@Override
	public Object getColumnEditValue(RoomDefinitionVO roomDefinitionVO, RoomDefinitionTableColumns column) {
		switch (column) {
		case USED:
			return Boolean.valueOf(isUsed(roomDefinitionVO));
		default:
			return null;
		}
	}


	@Override
	public boolean setColumnEditValue(RoomDefinitionVO element, RoomDefinitionTableColumns column, Object value) {
		if (column == RoomDefinitionTableColumns.USED) {
			if (Boolean.TRUE.equals(value) && ! usedRoomDefinitionPKs.contains(element.getPK())) {
				usedRoomDefinitionPKs.add(element.getPK());
				return true;
			} else if (Boolean.FALSE.equals(value) &&  usedRoomDefinitionPKs.contains(element.getPK())) {
				usedRoomDefinitionPKs.remove(element.getPK());
				return true;
			}
		}
		return false;
	}


	public void setUsedRoomDefinitionPKs(Collection<Long> usedRoomDefinitionPKs) {
		this.usedRoomDefinitionPKs = usedRoomDefinitionPKs;
	}


	public Collection<Long> getUsedRoomDefinitionPKs() {
		return usedRoomDefinitionPKs;
	}
}
