package com.lambdalogic.util.rcp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * An {@link Action} that copies the contents of the control which has the focus (be it Text, Table, Tree, List
 * or Combo) into the clipboard.
 *
 * Special behavior for certain {@link Control}s can be added by calling {@link #addRunnable(Control, Runnable)}.
 */
public class CopyAction extends Action {

	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final char TAB = '\t';


	protected Map<Control, Runnable> runnableMap = new HashMap<>();


	public CopyAction() {
	}


	@Override
	public String getId() {
		return getClass().getName();
	}


	@Override
	public String getText() {
		return UtilI18N.CopyVerb;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY);
	}


	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED);
	}


	/**
	 * Assign a special {@link Runnable} for a certain {@link Control}.
	 * If the control is the focus owner when {@link CopyAction#run()} is called, its assigned {@link Runnable#run()}
	 * is called instead of the default behaviour.
	 *
	 * @param control
	 * @param runnable
	 */
	public void addRunnable(Control control, Runnable runnable) {
		runnableMap.put(control, runnable);
	}


	/**
	 * Iterates through all rows, concatenates the text of the cells, and puts the resulting
	 * string into the provided clipboard.
	 *
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		Control control = Display.getCurrent().getFocusControl();
		if (control != null) {
			Runnable runnable = runnableMap.get(control);
			if (runnable != null) {
				runnable.run();
			}
			else if (control instanceof Text) {
				Text text = (Text) control;
				if ((text.getStyle() & SWT.PASSWORD) == 0) {
					//textForClipboard = text.getSelectionText();
					text.copy();
				}
			}
			else if (control instanceof Combo) {
				Combo combo = (Combo) control;
				String s = combo.getText();
				copyToClipboard(s);
			}
			else if (control instanceof List) {
				String s = getContentsFromList((List) control);
				copyToClipboard(s);
			}
			else if (control instanceof Table) {
				String s = getContentsFromTable((Table) control);
				copyToClipboard(s);
			}
			else if (control instanceof Tree) {
				String s = getContentsFromTree((Tree) control);
				copyToClipboard(s);
			}
		}
	}


	public void copyToClipboard(String text) {
		ClipboardHelper.copyToClipboard(text);
	}


	private String getContentsFromTable(Table table) {
		StringBuilder copyContent = new StringBuilder(1024);
		int columnCount = table.getColumnCount();

		// make a row for the headers
		for (int colIndex = 0; colIndex < columnCount; colIndex++) {
			TableColumn tableColumn = table.getColumn(colIndex);
			copyContent.append(tableColumn.getText());
			if (colIndex < columnCount - 1) {
				copyContent.append(TAB);
			}
		}
		copyContent.append(LINE_SEPARATOR);

		TableItem[] selectedItems = table.getSelection();
		if (selectedItems != null) {
			// Iterate through all rows
			for (TableItem tableItem : selectedItems) {

				// Concatenate the text of the cells
				for (int colIndex=0; colIndex<columnCount; colIndex++) {
					copyContent.append(tableItem.getText(colIndex));

					// If not last column, a Tab character as separator
					if (colIndex < columnCount -1) {
						copyContent.append(TAB);
					}
				}
				copyContent.append(LINE_SEPARATOR);
			}
		}

		return copyContent.toString();
	}


	private String getContentsFromTree(Tree tree) {
		StringBuilder copyContent = new StringBuilder(1024);
		int columnCount = tree.getColumnCount();

		// make a row for the headers
		for (int colIndex=0; colIndex<columnCount; colIndex++) {
			TreeColumn treeColumn = tree.getColumn(colIndex);
			copyContent.append(treeColumn.getText());
			if (colIndex < columnCount - 1) {
				copyContent.append(TAB);
			}
		}
		copyContent.append(LINE_SEPARATOR);


		TreeItem[] selectedItems = tree.getSelection();
		if (selectedItems != null) {
			// Iterate through all rows
			for (TreeItem treeItem : selectedItems) {

				if (columnCount == 0) {
					// Only tree, not with several columns
					copyContent.append(treeItem.getText());
				}
				else {
					// Concatenate the text of the cells in the several columns
					for(int colIndex=0;colIndex<columnCount; colIndex++) {
						copyContent.append(treeItem.getText(colIndex));

						// If not last column, a Tab character as separator
						if (colIndex < columnCount -1) {
							copyContent.append(TAB);
						}
					}
				}
				copyContent.append(LINE_SEPARATOR);
			}
		}

		return copyContent.toString();
	}


	private String getContentsFromList(List list) {
		StringBuilder copyContent = new StringBuilder(1024);

		String[] strings = list.getSelection();

		if (strings != null) {
			// Iterate through all rows
			for (String string : strings) {
				copyContent.append(string);
				copyContent.append(LINE_SEPARATOR);
			}
		}
		return copyContent.toString();
	}

}
