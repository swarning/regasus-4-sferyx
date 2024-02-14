package de.regasus.users.common;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVO;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;
import com.lambdalogic.util.rcp.simpleviewer.TriStateCellEditor;

import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.users.UsersAdministrationHelper;

enum AccessControlEntryTableColumns {
	TYPE, OWNER, PRIORITY, CONSTRAINT_TYPE, CONSTRAINT, READ, WRITE, CREATE, DELETE, ACTIVE
}

public class AccessControlEntryTable extends SimpleTable<AccessControlEntryCVO, AccessControlEntryTableColumns> {

	private String owner;

	private Color gray;

	private AbstractEditor<?> editor;


	public AccessControlEntryTable(Table table) {
		super(table, AccessControlEntryTableColumns.class, true, true);

		gray = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
	}


	@Override
	public String getColumnText(AccessControlEntryCVO aceCVO, AccessControlEntryTableColumns column) {
		AccessControlEntryVO aceVO = aceCVO.getVO();
		switch (column) {
    		case TYPE:
    			String object = aceVO.getACLObject().object;
    			return AccountLabel.valueOf(object).getString();
    		case OWNER:
    			return aceVO.getSubject();
    		case PRIORITY:
    			return String.valueOf(aceVO.getPriority());
    		case CONSTRAINT_TYPE:
    			return UsersAdministrationHelper.getLabelForConstraintType(aceVO.getConstraintType());
    		case CONSTRAINT:
    			return aceCVO.getConstraintName();
    		default:
    			// No text for READ, WRITE, CREATE, DELETE, ACTIVE
    			return null;
		}
	}


	/**
	 * When a column returns a CellEditor, all cells in it are editable.
	 */
	@Override
	public CellEditor getColumnCellEditor(Composite parent, AccessControlEntryTableColumns column) {
		switch (column) {
    		case PRIORITY:
    			return new TextCellEditor(parent, SWT.RIGHT);
    		case READ:
    			return new TriStateCellEditor(parent);
    		case WRITE:
    			return new TriStateCellEditor(parent);
    		case CREATE:
    			return new TriStateCellEditor(parent);
    		case DELETE:
    			return new TriStateCellEditor(parent);
    		case ACTIVE:
    			return new CheckboxCellEditor(parent, SWT.NONE);
    		default:
    			// Other columns are not editable
    			return null;
		}
	}


	@Override
	public Image getColumnImage(AccessControlEntryCVO aceCVO, AccessControlEntryTableColumns column) {
		AccessControlEntryVO aceVO = aceCVO.getVO();
		switch (column) {
    		case READ:
    			return UsersAdministrationHelper.getImageForRight( aceVO.getRead() );
    		case WRITE:
    			return UsersAdministrationHelper.getImageForRight( aceVO.getWrite() );
    		case CREATE:
    			return UsersAdministrationHelper.getImageForRight( aceVO.getCreate() );
    		case DELETE:
    			return UsersAdministrationHelper.getImageForRight( aceVO.getDelete() );
    		case ACTIVE:
    			if (aceVO.isDisabled()) {
    				return de.regasus.users.IconRegistry.getImage("icons/unchecked.gif");
    			}
    			else {
    				return de.regasus.users.IconRegistry.getImage("icons/checked.gif");
    			}
    		default:
    			// Other columns don't show an image
    			return null;
		}
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		AccessControlEntryCVO aceCVO,
		AccessControlEntryTableColumns column) {
		if (column == AccessControlEntryTableColumns.PRIORITY) {
			return aceCVO.getVO().getPriority();
		}
		return super.getColumnComparableValue(aceCVO, column);
	}


	@Override
	protected AccessControlEntryTableColumns getDefaultSortColumn() {
		return AccessControlEntryTableColumns.PRIORITY;
	}


	@Override
	public Color getBackground(Object element, int columnIndex) {
		AccessControlEntryCVO aceCVO = (AccessControlEntryCVO) element;

		if ( !isEditable(aceCVO.getVO()) ) {
			return gray;
		}
		else {
			return null;
		}
	}


	private boolean isEditable(AccessControlEntryVO aceVO) {
		return owner == null || owner.equals(aceVO.getSubject());
	}


	@Override
	public Object getColumnEditValue(AccessControlEntryCVO aceCVO, AccessControlEntryTableColumns column) {
		AccessControlEntryVO aceVO = aceCVO.getVO();
		switch (column) {
    		case PRIORITY:
    			return String.valueOf(aceVO.getPriority());
    		case READ:
    			return aceVO.getRead();
    		case WRITE:
    			return aceVO.getWrite();
    		case CREATE:
    			return aceVO.getCreate();
    		case DELETE:
    			return aceVO.getDelete();
    		case ACTIVE:
    			return Boolean.valueOf(!aceVO.isDisabled());
    		default:
    			// Other columns are not editable
    			return null;
		}
	}


	@Override
	public boolean setColumnEditValue(AccessControlEntryCVO aceCVO, AccessControlEntryTableColumns column, Object value) {
		boolean changed = false;

		AccessControlEntryVO aceVO = aceCVO.getVO();
		if ( !isEditable(aceVO) ) {
			Display.getDefault().beep();
			return false;
		}

		if (column == AccessControlEntryTableColumns.PRIORITY) {
			try {
				int count = Integer.parseInt(String.valueOf(value));
				aceVO.setPriority(count);
				return true;
			}
			catch (Exception e) {
				System.err.println(e);
			}
			// Beep if count couldn't be parsed
			Display.getDefault().beep();
		}
		else if (column == AccessControlEntryTableColumns.READ) {
   			aceVO.setRead((Boolean) value);
       		changed = true;
   		}
		else if (column == AccessControlEntryTableColumns.WRITE) {
   			aceVO.setWrite((Boolean) value);
       		changed = true;
   		}
		else if (column == AccessControlEntryTableColumns.CREATE) {
			aceVO.setCreate((Boolean) value);
			changed = true;
		}
		else if (column == AccessControlEntryTableColumns.DELETE) {
			aceVO.setDelete((Boolean) value);
			changed = true;
		}
		else if (column == AccessControlEntryTableColumns.ACTIVE) {
			aceVO.setDisabled( Boolean.FALSE.equals(value) );
			changed = true;
		}



		if (changed && editor != null) {
			editor.setDirty(true);
		}

		return changed;
	}


	public void setOwner(String owner) {
		this.owner = owner;
	}


	public void setEditor(AbstractEditor<?> editor) {
		this.editor = editor;
	}

}
