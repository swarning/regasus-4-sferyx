package de.regasus.core.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.rcp.i18n.I18NText;

import de.regasus.core.ui.i18n.LanguageProvider;

public class LanguageStringDialog extends TitleAreaDialog {
	
	private List<String> initialLanguageCodeList;
	private LanguageString languageString;
	private I18NText i18nText;
	private String messageToShow;
	private String titleToShow; 

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public LanguageStringDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.RESIZE );
	}


	public LanguageStringDialog(Shell parentShell, List<String> initialLanguageCodeList) {
		this(parentShell);
		
		this.initialLanguageCodeList = initialLanguageCodeList;
		
	}

	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage(messageToShow);
		setTitle(titleToShow);
		
		Composite area = (Composite) super.createDialogArea(parent);
		
		i18nText = new I18NText(area, SWT.NONE, LanguageProvider.getInstance(), initialLanguageCodeList);
		i18nText.setLayoutData(new GridData(GridData.FILL_BOTH));
		i18nText.setLanguageString(languageString);
		i18nText.setFocus();
		
		return area;
	}


	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			languageString = i18nText.getLanguageString();
		}
		super.buttonPressed(buttonId);
	}
	
	
	
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}


	public LanguageString getLanguageString() {
		return languageString;
	}


	public void setLanguageString(LanguageString languageString) {
		this.languageString = languageString;
	}


	public void setMessageToShow(String message) {
		this.messageToShow = message;
	}

	
	public void setTitleToShow(String titleToShow) {
		this.titleToShow = titleToShow;
	}


}
