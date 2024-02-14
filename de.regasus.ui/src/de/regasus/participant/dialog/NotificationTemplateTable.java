package de.regasus.participant.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.File;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.ui.Activator;

enum NotificationTemplateTableColumns {
	SELECT, LANGUAGE, NAME
}

public class NotificationTemplateTable extends SimpleTable<File, NotificationTemplateTableColumns> {

	private LanguageModel languageModel = LanguageModel.getInstance();

	private Set<File> selectionSet = new HashSet<>();


	public NotificationTemplateTable(Table table) {
		super(table, NotificationTemplateTableColumns.class, true, true);
	}


	// *************************************************************************
	// * Overridden table specific methods
	// *

	@Override
	public String getColumnText(File file, NotificationTemplateTableColumns column) {
		switch (column) {
			case LANGUAGE: {
				String languageCode = file.getLanguage();
				String languageName = "";
				if (languageCode != null) {
					try {
						Language language = languageModel.getLanguage(languageCode);
						languageName = language.getName().getString();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
				return languageName;
			}
			case NAME: {
				String extFileName = file.getExternalFileName();
				return extFileName;
			}
			default: return "";
		}
	}


	@Override
	protected NotificationTemplateTableColumns getDefaultSortColumn() {
		return NotificationTemplateTableColumns.NAME;
	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, NotificationTemplateTableColumns column) {
		switch (column) {
		case SELECT:
			return new CheckboxCellEditor(parent, SWT.CENTER);
		default:
			return null;
		}
	}


	@Override
	public Object getColumnEditValue(File file, NotificationTemplateTableColumns column) {
		switch (column) {
			case SELECT:
				return isSelected(file);
			default:
				return null;
		}
	}


	@Override
	public Image getColumnImage(File element, NotificationTemplateTableColumns column) {
		switch (column) {
			case SELECT:
				if (isSelected(element)) {
					return IconRegistry.getImage(IImageKeys.CHECKED);
				}
				else {
					return IconRegistry.getImage(IImageKeys.UNCHECKED);
				}
			default:
				return null;
		}
	}


	@Override
	public boolean setColumnEditValue(File element, NotificationTemplateTableColumns column, Object value) {
		switch (column) {
			case SELECT:
				boolean selected = false;
				if (value instanceof Boolean) {
					selected = (Boolean) value;
				}
				setSelected(element, selected);
				return true;

			default:
				return false;
		}
	}


	private boolean isSelected(File file) {
		return selectionSet.contains(file);
	}


	public Collection<File> getSelection() {
		return new ArrayList<>(selectionSet);
	}


	private void setSelected(File file, boolean selected) {
		if (selected) {
			selectionSet.add(file);
		}
		else {
			selectionSet.remove(file);
		}
	}


	public void selectAll() {
		Object input = getViewer().getInput();
		if (input instanceof Collection) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<File> allFiles = (Collection) input;
			selectionSet.clear();
			selectionSet.addAll(allFiles);
			getViewer().refresh();
		}
	}


	public void selectNothing() {
		selectionSet.clear();
		getViewer().refresh();
	}

}
