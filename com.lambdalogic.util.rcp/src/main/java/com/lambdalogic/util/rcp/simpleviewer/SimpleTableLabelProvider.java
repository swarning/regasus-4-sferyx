package com.lambdalogic.util.rcp.simpleviewer;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;


@SuppressWarnings("rawtypes")
public class SimpleTableLabelProvider extends CellLabelProvider {

	private SimpleTable simpleTable;

	private Enum columnEnum;


	public SimpleTableLabelProvider(SimpleTable simpleTable, Enum columnEnum) {
		this.simpleTable = simpleTable;
		this.columnEnum = columnEnum;
	}


	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		int columnIndex = cell.getColumnIndex();

		String text = getColumnText(element);
		if (text == null) {
			text = "";
		}
		cell.setText(text);


		Image image = getColumnImage(element);
		cell.setImage(image);

		cell.setBackground(simpleTable.getBackground(element, columnIndex));
		cell.setForeground(simpleTable.getForeground(element, columnIndex));
//		cell.setFont(getFont(element));
	}


	private Image getColumnImage(Object element) {
		return simpleTable.getColumnImage(element, columnEnum);
	}


	private String getColumnText(Object element) {
		return simpleTable.getColumnText(element, columnEnum);
	}


	@Override
	public String getToolTipText(Object element) {
		return simpleTable.getColumnToolTipText(element, columnEnum);
	}


	@Override
	public void addListener(ILabelProviderListener listener) {
	}


	@Override
	public void dispose() {
	}


	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}


	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
}
