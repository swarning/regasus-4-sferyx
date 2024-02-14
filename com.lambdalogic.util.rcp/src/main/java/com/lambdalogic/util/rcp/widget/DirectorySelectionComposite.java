package com.lambdalogic.util.rcp.widget;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.error.ErrorHandler;

public class DirectorySelectionComposite extends Composite {

	private static String lastDirText;

	private Text dirText;


	public DirectorySelectionComposite(Composite parent) {
		super(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		dirText = new Text(this, SWT.BORDER);
		dirText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		if (lastDirText != null) {
			dirText.setText(lastDirText);
		}

		Button fileButton = new Button(this, SWT.PUSH);
		fileButton.setText("...");
		fileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openFileDialog();
				}
				catch (Exception e1) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
				}
			}
		});
	}


	protected void openFileDialog() {
		DirectoryDialog fileDialog = new DirectoryDialog(getShell());

		File file = getFile();

		if (file != null) {
			String filterPath = null;
			if (file.isDirectory()) {
				filterPath = file.getAbsolutePath();
			}
			else {
				filterPath = file.getParent();
			}
			fileDialog.setFilterPath(filterPath);
		}
		String selectedFilePath = fileDialog.open();
		if (selectedFilePath != null) {
			dirText.setText(selectedFilePath);
			lastDirText = selectedFilePath;
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		dirText.addModifyListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		dirText.removeModifyListener(modifyListener);
	}


	public boolean dirExists() {
		File file = getFile();
		return file != null && file.exists();
	}


	public File getFile() {
		String pathname = dirText.getText();
		if (StringHelper.isNotEmpty(pathname)) {
			return new File(pathname);
		}
		else {
			return null;
		}
	}


	public void setDirectory(File dir) {
		if (dir != null) {
			dirText.setText(dir.getPath());
		}
		else {
			dirText.setText("");
		}
	}


	public void setDirectory(String dir) {
		if (dir != null) {
			dirText.setText(dir);
		}
		else {
			dirText.setText("");
		}
	}


	public String getDirPath() {
		return dirText.getText();
	}


	public boolean exists() {
		return getFile() != null && getFile().exists();
	}

}
