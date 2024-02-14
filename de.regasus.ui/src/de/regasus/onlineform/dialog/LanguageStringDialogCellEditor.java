package de.regasus.onlineform.dialog;

import java.util.Collection;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.LanguageString;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.LanguageStringDialog;

public class LanguageStringDialogCellEditor extends DialogCellEditor {

	private String message;
	private Label defaultLabel;


	public LanguageStringDialogCellEditor(Composite parent, String message) {
		super(parent, SWT.NONE);

		this.message = message;
	}


	@Override
	protected Object openDialogBox(Control cellEditorWindow) {

		LanguageString languageString = (LanguageString) getValue();

		LanguageStringDialog languageStringDialog = new LanguageStringDialog(cellEditorWindow.getShell());
		languageStringDialog.setTitleToShow(UtilI18N.Edit);
		languageStringDialog.setLanguageString(languageString);
		languageStringDialog.setMessageToShow(message);
		
		languageStringDialog.open();

		return languageStringDialog.getLanguageString();
	}
	
	   protected Control createContents(Composite cell) {
	        defaultLabel = new Label(cell, SWT.LEFT);
	        defaultLabel.setFont(cell.getFont());
	        defaultLabel.setBackground(cell.getBackground());
	        return defaultLabel;
	    }
	   
	   
	  protected void updateContents(Object value) {
	        if (defaultLabel == null) {
				return;
			}

	        String text = "";
	        if (value != null) {
	        	
	        	if (value instanceof LanguageString) {
	    	        text = getStrings((LanguageString) value);
	        	}
	        	else {
	        		text = value.toString();
	        	}
			}
	        defaultLabel.setText(text);
	    }

		private String getStrings(LanguageString languageString) {
			if (languageString == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			Collection<String> languageCodes = languageString.getLanguageCodes();
			
			for (String code : languageCodes) {
				sb.append(languageString.getString(code));
				sb.append(" (");
				sb.append(code);
				sb.append("), ");

			}
			
			if (sb.length() > 2) {
				sb.delete(sb.length()-2, sb.length()-1);
			}

			
			return sb.toString();
		}


}
