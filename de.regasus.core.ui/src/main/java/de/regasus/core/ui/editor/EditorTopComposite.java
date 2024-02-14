package de.regasus.core.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IconRegistry;

public class EditorTopComposite extends Composite {

	private Label editorTypeLabel;
	private Button infoButton;


	public EditorTopComposite(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		
		{
			editorTypeLabel = new Label(this, SWT.NONE);
			
			final GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
			editorTypeLabel.setLayoutData(gridData);

			Font font = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(com.lambdalogic.util.rcp.Activator.BIG_FONT);
			editorTypeLabel.setFont(font);
		}
		{
			infoButton = new Button(this, SWT.NONE);
			infoButton.setToolTipText(CoreI18N.InfoButtonToolTip);
			infoButton.setImage(IconRegistry.getImage(
				de.regasus.core.ui.IImageKeys.INFORMATION
			));
		}

	}

	
	public void setEditorTypeLabelText(String s) {
		editorTypeLabel.setText(s);
	}
	
	
	public void setInfoButtonToolTipText(String text) {
		if (text == null) {
			text = CoreI18N.InfoButtonToolTip;
		}
		infoButton.setToolTipText(text);
	}
	
	
	public void addInfoButtonSelectionListener(SelectionListener selectionListener) {
		infoButton.addSelectionListener(selectionListener);
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	@Override
	public boolean setFocus() {
		return infoButton.setFocus();
	}

}
