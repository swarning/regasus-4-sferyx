package com.lambdalogic.util.rcp.widget;

import static com.lambdalogic.util.FileHelper.dirExists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.ColorHelper;
import com.lambdalogic.util.rcp.UtilI18N;

public class DirectorySelectionMessageComposite extends Composite implements ModifyListener {

	private Label messageLabel;

	public DirectorySelectionMessageComposite(Composite parent) {
		super(parent, SWT.NONE);

		setLayout( new FillLayout() );

		messageLabel = new Label(this, SWT.NONE);
		messageLabel.setForeground( ColorHelper.RED_1 );

		setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
	}


	@Override
	public void modifyText(ModifyEvent e) {
		if (e.widget instanceof Text) {
			Text text = (Text) e.widget;
			String dirPath = text.getText();
			String message = dirExists(dirPath) ? "" : UtilI18N.DirectorySelectionComposite_DirectoryDoesNotExist;
			messageLabel.setText(message);
		}
	}

}
