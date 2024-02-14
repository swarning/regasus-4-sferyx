/**
 * Copyright (c) 2007, Ralf Ebert All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the project team nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lambdalogic.util.rcp.simpleviewer;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;

import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.widget.SWTHelper;

/**
 * SimpleTable is a simplification of JFace Viewer handling which supports the standard case of displaying a list of
 * model objects in a sortable table.
 *
 * This class is generic, you need to specify a object class (=row in your table) and an enum having values defined for
 * the columns of your table in order.
 *
 * Everything else is handled by overwriting the methods of this class, which provide a default behavior for most
 * things: - Table contents are defined by implementing getColumnText / optionally getColumnImage - Sorting can be
 * customized by implementing getColumnComparableValue, getColumnComparator and getDefaultSortColumn. - For editable
 * tables, override getColumnCellEditor, getColumnEditValue and setColumnEditValue, optionally isColumnEditable.
 *
 * A SimpleTable creates and holds a JFace Table Viewer internally which is accessible via getViewer().
 *
 * Please note: JFace TableViewers provides a very nice, highly customizable abstraction for providing data for tables
 * and this class is intended only as simplification of a very common usage pattern. If you go too far away from the
 * simplification provided by this class, you should consider using a original JFace TableViewer.
 *
 * @author Ralf Ebert
 *
 * @param <Entity>
 *            model object class
 * @param <Column>
 *            column enum class
 */
public abstract class SimpleTable<Entity extends Object, Column extends Enum<?>> {

	// ========================= Attributes ==============================================

	private static Collator collator;

	private final Class<Column> enumClass;

	/**
     * The SWT table style that we used when the table was created.
     */
    private final int tableStyles;

	private final TableViewer viewer;

	private Column currentSortColumn = null;

	private final Map<Column, Comparator<?>> comparators = new HashMap<>();

	private boolean sortAsc = true;

	private final static Image IMAGE_ASC =
		ImageDescriptor.createFromFile(SimpleTable.class, "sort_asc.gif").createImage();

	private final static Image IMAGE_DESC =
		ImageDescriptor.createFromFile(SimpleTable.class, "sort_desc.gif").createImage();

	private final ListenerList<ITableEditListener> changeListeners = new ListenerList<>();


	// ========================= Constructor Chain ==============================================

	/**
	 * Constructor for SimpleTables which are sortable, but not editable
	 *
	 * @param table
	 *            swt table widget
	 * @param enumClass
	 *            class of your column enum
	 */

	public SimpleTable(final Table table, final Class<Column> enumClass) {
		this(table, enumClass, true /* sortable */);
	}


	/**
	 * Constructor for SimpleTables which are editable and which may be sortable.
	 *
	 * @param table
	 *            swt table widget
	 * @param enumClass
	 *            class of your column enum
	 * @param sortable
	 *            whether the table is sortable
	 */

	public SimpleTable(final Table table, final Class<Column> enumClass, boolean sortable) {
		this(table, enumClass, sortable, false /* editable */);
	}


	/**
	 * Constructor for SimpleTables which may be editable and/or sortable.
	 *
	 * @see TableViewer
	 * @param table
	 *            swt table widget
	 * @param enumClass
	 *            class of your column enum
	 * @param sortable
	 *            whether the table is sortable
	 * @param editable
	 *            whether the table is editable
	 */
	public SimpleTable(final Table table, final Class<Column> enumClass, boolean sortable, boolean editable) {
		this.enumClass = enumClass;

		this.tableStyles = table.getStyle();

		if (isCheckboxTable()) {
			viewer = new CheckboxTableViewer(table);
        }
		else {
            viewer = new TableViewer(table);
        }


		// check for matching column count model / enum check
		int columnCount = table.getColumnCount();
		int enumLength = enumClass.getEnumConstants().length;
		if (columnCount != enumLength) {
			throw new UnsupportedOperationException(
				  "SimpleTableViewer requires the table to have the same column count (was " + columnCount
				+ ") as the enum class (was " + enumLength + " for " + enumClass.getSimpleName() + ")");
		}

		// provide default content provider
		viewer.setContentProvider(new ArrayContentProvider());

		// set label provider for each column
		for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++) {
			TableColumn tableColumn = table.getColumns()[columnIdx];
			Column columnEnum = enumClass.getEnumConstants()[columnIdx];

			// get specific CellLabelProvider
			CellLabelProvider labelProvider = getColumnCellLabelProvider(columnEnum);
			if (labelProvider == null) {
				// if there is no specific CellLabelProvider defined, use default
				labelProvider = new SimpleTableLabelProvider(this, columnEnum);
			}

			TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
			tableViewerColumn.setLabelProvider(labelProvider);
		}


		if (sortable) {
			setupViewerForSortableTable(table, enumClass, enumLength);
		}

		// provide column properties by enum name
		String[] columnProperties = new String[enumLength];
		for (int i = 0; i < enumLength; i++) {
			final Column columnEnum = enumClass.getEnumConstants()[i];
			columnProperties[i] = columnEnum.name();
		}
		viewer.setColumnProperties(columnProperties);

		setupCellModifiers(table, enumClass, enumLength);
	}


    /**
     * Tests whether this {@link TableControl}'s table was created with the
     * {@link SWT#CHECK} style bit. If this method returns <code>true</code>, it
     * is safe to cast the result of the {@link #getViewer()} method to a
     * {@link CheckboxTableViewer}.
     *
     * @return <code>true</code> if this {@link TableControl} was created with
     *         the {@link SWT#CHECK} style bit
     */
    protected final boolean isCheckboxTable() {
        return (tableStyles & SWT.CHECK) != 0;
    }


	/**
	 * Makes that Ctrl+C starts the Action to copy the table contents to the clipboard
	 */
	public void registerCopyAction(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
	}


	private void setupCellModifiers(final Table table, final Class<Column> enumClass, int enumLength) {
		// edit handling
		CellEditor[] cellEditors = new CellEditor[enumLength];

		boolean atLeastOneColumnEditable = false;
		for (int i = 0; i < enumLength; i++) {
			final Column columnEnum = enumClass.getEnumConstants()[i];
			CellEditor columnCellEditor = getColumnCellEditor(table, columnEnum);
			cellEditors[i] = columnCellEditor;
			if (columnCellEditor != null) {
				atLeastOneColumnEditable = true;
			}
		}


		viewer.setCellEditors(cellEditors);

		if (atLeastOneColumnEditable) {
			viewer.setCellModifier(new ICellModifier() {

				@Override
				@SuppressWarnings("unchecked")
				public boolean canModify(Object element, String property) {
					return SimpleTable.this.isColumnEditable((Entity) element, getEnumByColumnName(enumClass, property));
				}


				@Override
				@SuppressWarnings("unchecked")
				public Object getValue(Object element, String property) {
					return SimpleTable.this.getColumnEditValue((Entity) element, getEnumByColumnName(enumClass, property));
				}


				@Override
				@SuppressWarnings("unchecked")
				public void modify(Object element, String property, Object value) {
					if (!(element instanceof Item)) {
						throw new SimpleTableWrongUsageException("Modify was called with a non Item-value!");
					}
					Object data = ((Item) element).getData();
					if (SimpleTable.this.setColumnEditValue((Entity) data, getEnumByColumnName(enumClass, property), value)) {
						viewer.refresh();
						for (Object listener : changeListeners.getListeners()) {
							((ITableEditListener) listener).tableCellChanged();
						}
					}
				}

			});
		}
	}


	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	private void setupViewerForSortableTable(final Table table, final Class<Column> enumClass, int enumLength) {
		// provide default sorter which uses getColumnComparator()
		// comparators are cached and created only once per instance
		viewer.setSorter(new ViewerSorter() {

			@Override
			public int compare(Viewer viewer, Object o1, Object o2) {
				if (currentSortColumn == null) {
					if (shouldSortInitialTable()) {
						return super.compare(viewer, o1, o2);
					}
					else {
						return 0;
					}
				}
				Comparator comparator = comparators.get(currentSortColumn);
				if (comparator == null) {
					comparator = SimpleTable.this.getColumnComparator(currentSortColumn);
					comparators.put(currentSortColumn, comparator);
				}
				return sortAsc ? comparator.compare(o1, o2) : comparator.compare(o2, o1);
			}

			/**
			 *  Added so that you actually see an exception when eg an overwritten getColumnComparableValue() method
			 *  has flaws and returns things of different types
			 */
			@Override
			public void sort(Viewer viewer, Object[] elements) {
				try {
					super.sort(viewer, elements);
					afterSorting(elements);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}

		});


		boolean restored = restoreSortSettings();
		if ( ! restored) {
    		// default sort column handling
    		Column defaultSortColumn = getDefaultSortColumn();
    		if (defaultSortColumn != null) {
    			setSortColumn(defaultSortColumn);
    		}
		}

		// add column header click listeners for handling sorting
		for (int i = 0; i < enumLength; i++) {
			final Column columnEnum = enumClass.getEnumConstants()[i];
			final TableColumn column = table.getColumn(i);

			column.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					setSortColumn(columnEnum);
				}
			});
		}
	}


	protected void afterSorting(Object[] elements) {
	}


	/**
	 * Whether a newly created sortable table also should appear sorted initially. Overrride and return false if you
	 * want to show the table rows in their original order first.
	 *
	 * @return
	 */
	protected boolean shouldSortInitialTable() {
		return true;
	}


	protected Column getEnumByColumnName(final Class<Column> enumClass, String columnName) {
		for (Column enumValue : enumClass.getEnumConstants()) {
			if (enumValue.name().equals(columnName)) {
				return enumValue;
			}
		}
		throw new SimpleTableWrongUsageException("Unknown column property name: " + columnName);
	}


	/**
	 * Sets the column by which the table is sorted
	 *
	 * @param columnEnum
	 */
	public final void setSortColumn(Column columnEnum) {
		setSortColumn(columnEnum, null);
	}


	public void setEnabled(boolean enabled) {
		viewer.getTable().setEnabled(enabled);
	}


	/**
	 * Sets the column by which the table is sorted
	 *
	 * @param columnEnum
	 */
	public final void setSortColumn(Column columnEnum, Boolean ascending) {

		// remove image from last sort column (if it has not been set explicitly)
		if (currentSortColumn != null) {
			Image currentImage = viewer.getTable().getColumn(currentSortColumn.ordinal()).getImage();
			if (currentImage == IMAGE_ASC || currentImage == IMAGE_DESC) {
				viewer.getTable().getColumn(currentSortColumn.ordinal()).setImage(null);
			}
		}

		// determine sort order
		if (currentSortColumn == columnEnum) {
			if (ascending != null) {
				sortAsc = ascending.booleanValue();
			}
			else {
				sortAsc = !sortAsc;
			}
		}
		else {
			currentSortColumn = columnEnum;

			if (ascending != null) {
				sortAsc = ascending.booleanValue();
			}
			else {
				sortAsc = true;
			}
		}

		// set image to sort column (if it has not been set explicitly)
		Image currentImage = viewer.getTable().getColumn(currentSortColumn.ordinal()).getImage();
		if (currentImage == null || currentImage == IMAGE_ASC || currentImage == IMAGE_DESC) {
			viewer.getTable().getColumn(currentSortColumn.ordinal()).setImage(sortAsc ? IMAGE_ASC : IMAGE_DESC);
		}

		viewer.refresh();

		saveSortSettings();
	}



	private String getSortColumnKey() {
		String sortColumnKey = enumClass.getName() + "/SORT_COLUMN";
		return sortColumnKey;
	}


	private String getSortAscKey() {
		String sortAscKey = enumClass.getName() + "/IS_SORT_ASC";
		return sortAscKey;
	}


	/**
	 * Restore the settings for sorting.
	 * This includes which column is sorted and the sort order (ascending / descending).
	 *
	 * @return true is setting could be restored
	 */
	protected boolean restoreSortSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();

		String sortColumn = settings.get( getSortColumnKey() );
		boolean sortAsc = TypeHelper.toBoolean(settings.get( getSortAscKey() ), true);

		// find sortColumnEnum by name
		Column sortColumnEnum = null;
		for (Column columnEnum : enumClass.getEnumConstants()) {
			if (columnEnum.name().equals(sortColumn)) {
				sortColumnEnum = columnEnum;
				break;
			}
		}

		if (sortColumnEnum != null) {
			/* Set column and ascending in one method call!
			 * Do not call  setSortColumn(E columnEnum), because this would switch ascending!
			 */
			setSortColumn(sortColumnEnum, sortAsc);
			return true;
		}

		return false;
	}


	protected void saveSortSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		Column sortColumnEnum = getCurrentSortColumn();
		boolean sortAsc = isSortAsc();

		if (sortColumnEnum != null) {
			settings.put(getSortColumnKey(), sortColumnEnum.name());
			settings.put(getSortAscKey(), sortAsc);
		}
	}


	/**
	 * Overwrite to provide a different sort column, by default the first column is used
	 *
	 * @return
	 */
	protected Column getDefaultSortColumn() {
		return null;
	}


	/**
	 * Overwrite to provide your own comparator for sorting of the given column, by default getColumnComparableValue is
	 * used
	 *
	 * @param column
	 * @return Comparator for sorting this column
	 */
	protected Comparator<Entity> getColumnComparator(final Column column) {
		return new Comparator<Entity>() {

			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public int compare(Entity o1, Entity o2) {
				Comparable c1 = getColumnComparableValue(o1, column);
				Comparable c2 = getColumnComparableValue(o2, column);
				if (c1 == c2) {
					return 0;
				}
				else if (c1 == null) {
					return -1;
				}
				else if (c2 == null) {
					return 1;
				}
				else if (c1 instanceof String && c2 instanceof String) {
					return getCollator().compare((String) c1, (String) c2);
				}
				else {
					return c1.compareTo(c2);
				}
			}

		};
	}


	/**
	 * Overwrite to provide a value by which the given column is sorted. By default the value of getColumnText() is used
	 *
	 * @param element
	 * @param column
	 * @return
	 */
	protected Comparable<? extends Object> getColumnComparableValue(Entity element, Column column) {
		String columnText = getColumnText(element, column);
		return (columnText == null) ? null : columnText.toLowerCase();
	}


	/**
	 * Overwrite to provide the text for the given column and object.
	 *
	 * @param object
	 * @param column
	 * @return
	 */
	public abstract String getColumnText(Entity object, Column column);


	/**
	 * Overwrite to provide the tooltip text for the given column and object.
	 *
	 * Make sure to activate the tooltip support by calling
	 * <pre>
	 * ColumnViewerToolTipSupport.enableFor(getViewer(), ToolTip.NO_RECREATE);
	 * </pre>
	 * in the constructor of the extending class when overwriting this method!
	 *
	 * @param object
	 * @param column
	 * @return
	 */
	public String getColumnToolTipText(Entity object, Column column) {
		return null;
	}


	/**
	 * Overwrite to provide alternative  {@link CellLabelProvider} for the given column.
	 * If null is returned, {@link SimpleTableLabelProvider} is used as default.
	 *
	 * @param object
	 * @param column
	 * @return
	 */
	public CellLabelProvider getColumnCellLabelProvider(Column column) {
		return null;
	}


	/**
	 * Overwrite to provide an image for the given column and object
	 *
	 * @param element
	 * @param column
	 * @return
	 */
	public Image getColumnImage(Entity element, Column column) {
		return null;
	}


	/**
	 * Overwrite this method to make columns and return a {@link CellEditor} (like {@link TextCellEditor}) to make
	 * columns editable.
	 *
	 * @param parent
	 *            parent composite to pass to the cell editor
	 * @param column
	 * @return
	 */
	public CellEditor getColumnCellEditor(Composite parent, Column column) {
		return null;
	}


	/**
	 * Overwrite this method to customize when a cell will be editable. By default all cells are editable, provided that
	 * a cell editor is assigned.
	 *
	 * @param element
	 * @param column
	 * @return
	 */
	public boolean isColumnEditable(Entity element, Column column) {
		return true;
	}


	/**
	 * Overwrite to provide the editing object (a string for a TextCellEditor for example) for the given column and
	 * object - by default the columnText is returned.
	 */
	public Object getColumnEditValue(Entity element, Column column) {
		return getColumnText(element, column);
	}


	/**
	 * Overwrite this method to apply changes to the underlying object. You need to implement this method for all
	 * editable columns.
	 *
	 * @return true, if you changed something and want the viewer to be refreshed / edit listeners to be notified
	 */
	public boolean setColumnEditValue(Entity element, Column column, Object value) {
		throw new SimpleTableWrongUsageException("You need to implement setColumnEditValue for editable columns.");
	}


	/**
	 * Adds a listener which will be notified whenever a cell in the table has been edited.
	 *
	 * @param listener
	 */
	public void addEditListener(ITableEditListener listener) {
		changeListeners.add(listener);
	}


	public void removeEditListener(ITableEditListener listener) {
		changeListeners.remove(listener);
	}


	/**
	 * Returns the underlying TableViewer. Use this to set the input object / customize the default behavior supplied by
	 * this class.
	 *
	 * @return
	 */
	public TableViewer getViewer() {
		return viewer;
	}


	/**
	 * Sets the input object to the underlying viewer.
	 *
	 * @param inputList
	 */
	public void setInput(Collection<Entity> inputList) {
		viewer.setInput(inputList);
	}


	/**
	 * Sets the input object to the underlying viewer.
	 *
	 * @param inputList
	 */
	public void setInput(Entity[] inputList) {
		viewer.setInput(inputList);
	}


	protected static Collator getCollator() {
		if (collator == null) {
			collator = Collator.getInstance();
		}
		return collator;
	}


	public void addSelectionListener(SelectionListener listener) {
		viewer.getTable().addSelectionListener(listener);
	}


	public void removeSelectionListener(SelectionListener listener) {
		viewer.getTable().removeSelectionListener(listener);
	}


	public void addMouseListener(MouseListener listener) {
		viewer.getTable().addMouseListener(listener);
	}


	public void removeMouseListener(MouseListener listener) {
		viewer.getTable().removeMouseListener(listener);
	}


	public Color getBackground(Object element, int columnIndex) {
		return null;
	}


	public Color getForeground(Object element, int columnIndex) {
		return null;
	}


	public Column getCurrentSortColumn() {
		return currentSortColumn;
	}


	public boolean isSortAsc() {
		return sortAsc;
	}


	public void setSortAsc(boolean sortAsc) {
		this.sortAsc = sortAsc;
	}


	public void refresh() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}


	public int getSelectionCount() {
		return viewer.getTable().getSelectionIndices().length;
	}

}
