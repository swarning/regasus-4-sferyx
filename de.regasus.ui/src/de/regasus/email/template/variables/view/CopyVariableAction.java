package de.regasus.email.template.variables.view;

import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.UtilI18N;

public class CopyVariableAction extends CopyAction implements SelectionListener {

	private Table table;


	public CopyVariableAction(Table table) {
		this.table = Objects.requireNonNull(table);

		table.addSelectionListener(this);
		setEnabled(false);

		addRunnable(table, new Runnable() {
			@Override
			public void run() {
				int selectingIndex = table.getSelectionIndex();

				// get text of column 1
				String variable = (selectingIndex >= 0) ? table.getItem(selectingIndex).getText(0) : "";

				// copy text to clipboard
				copyToClipboard(variable);
			}
		});
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


	@Override
	public void widgetSelected(SelectionEvent e) {
		boolean enabled = table.getSelectionCount() > 0;
		setEnabled(enabled);
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

}
