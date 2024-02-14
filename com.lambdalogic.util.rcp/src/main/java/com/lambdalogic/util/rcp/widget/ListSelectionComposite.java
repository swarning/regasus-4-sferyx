package com.lambdalogic.util.rcp.widget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Extracted from {@link ListSelectionDialog} to show a list with checkboxes, together
 * with buttons to select/deselect all of them
 */
@SuppressWarnings("restriction")
public class ListSelectionComposite<T> extends Composite {

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;

	private CheckboxTableViewer listViewer;
	private List<T> elements;


	public ListSelectionComposite(Composite parent, List<T> elements,  ILabelProvider labelProvider) {

		super(parent, SWT.NONE);

		setLayout(new GridLayout());
		listViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(ArrayContentProvider.getInstance());
		listViewer.setInput(elements);

		this.elements = elements;

		addSelectionButtons(this);
	}


	private void addSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.horizontalSpacing = 4;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));

		Button selectButton = createButton(buttonComposite,
			IDialogConstants.SELECT_ALL_ID, WorkbenchMessages.SelectionDialog_selectLabel);

		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(true);
			}
		});

		Button deselectButton = createButton(buttonComposite,
			IDialogConstants.DESELECT_ALL_ID, WorkbenchMessages.SelectionDialog_deselectLabel);

		deselectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(false);
			}
		});
	}


	protected Button createButton(Composite parent, int id, String label) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(id);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = minSize.x;
		button.setLayoutData(data);
		return button;
	}


	public void setCheckedElements(List<T> elementsToBeChecked) {
		for (T t : elements) {
			listViewer.setChecked(t, elementsToBeChecked.contains(t));
		}
	}

	public List<T> getCheckedElements() {
		List<T> result = new ArrayList();
		Object[] checkedElements = listViewer.getCheckedElements();
		for (Object object : checkedElements) {
			result.add((T) object);
		}
		return result;
	}

}
