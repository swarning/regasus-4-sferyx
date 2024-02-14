package de.regasus.core.ui.dialog;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.CustomFieldListValue;
import com.lambdalogic.util.rcp.i18n.I18NText;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.i18n.LanguageProvider;

public class CustomFieldListValueDialog extends TitleAreaDialog {

	private Collection<String> defaultLanguageCodes;

	private CustomFieldListValue listValue;

	private String dialogTitle = "";
	private String dialogMessage = "";

	// widgets
	private I18NText i18nText;
	private Text valueWidget;

	private Button okButton;


	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			handleButtonState();
		}

	};


	public static boolean openAddInstance(
		Shell parentShell,
		CustomFieldListValue listValue,
		Collection<String> defaultLanguageCodes
	) {
		CustomFieldListValueDialog dialog = new CustomFieldListValueDialog(parentShell, listValue, defaultLanguageCodes);
		dialog.dialogTitle = CoreI18N.CustomFieldListValueDialog_AddTitle;
		dialog.dialogMessage = CoreI18N.CustomFieldListValueDialog_AddMessage;
		return (dialog.open() == Window.OK);
	}


	public static boolean openEditInstance(
		Shell parentShell,
		CustomFieldListValue listValue,
		Collection<String> defaultLanguageCodes
	) {
		CustomFieldListValueDialog dialog = new CustomFieldListValueDialog(parentShell, listValue, defaultLanguageCodes);
		dialog.dialogTitle = CoreI18N.CustomFieldListValueDialog_EditTitle;
		dialog.dialogMessage = CoreI18N.CustomFieldListValueDialog_EditMessage;
		return (dialog.open() == Window.OK);
	}


	private CustomFieldListValueDialog(
		Shell parentShell,
		CustomFieldListValue listValue,
		Collection<String> defaultLanguageCodes
	) {
		super(parentShell);

		this.listValue = listValue;
		this.defaultLanguageCodes = defaultLanguageCodes;

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(dialogTitle);
		setMessage(dialogMessage);

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		Composite mainComposite = new Composite(dialogArea, SWT.NONE);
		mainComposite.setLayoutData( GridDataFactory.fillDefaults().grab(true, true).create() );
		mainComposite.setLayout(new GridLayout(2, false));


		// CoreI18N Labels
		i18nText = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance(), defaultLanguageCodes);
		i18nText.setLayoutData( GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create() );
		i18nText.addModifyListener(modifyListener);
		i18nText.setFocus();


		Label valueLabel = new Label(mainComposite, SWT.NONE);
		valueLabel.setLayoutData( GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).create() );
		valueLabel.setText(UtilI18N.Value);


		valueWidget = new Text(mainComposite, SWT.BORDER);
		valueWidget.setLayoutData( GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create() );
		valueWidget.addModifyListener(modifyListener);


		syncWidgetsToEntity();

		return dialogArea;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		handleButtonState();

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			syncEntityToWidgets();
		}
		super.buttonPressed(buttonId);
	}


	private void handleButtonState() {
		/* The okButton might not be created yet, because it is not created in createDialogArea(...)
		 * but in createButtonsForButtonBar(...)
		 */
		if (okButton != null && i18nText != null && valueWidget != null) {
			LanguageString label = i18nText.getLanguageString();
			String value = valueWidget.getText();

			okButton.setEnabled( !label.isEmpty() || !value.isEmpty() );
		}
	}


	private void syncWidgetsToEntity() {
		i18nText.setLanguageString( listValue.getLabel() );
		valueWidget.setText( avoidNull(listValue.getValue()) );
	}


	private void syncEntityToWidgets() {
		LanguageString label = i18nText.getLanguageString();
		String value = valueWidget.getText();

		if (value.isEmpty() && !label.isEmpty()) {
			// if the user did not enter a value but a label: set first label as value
			value = buildValueFromLabel(label);
		}
		else if (label.isEmpty() && !value.isEmpty()) {
			// if the user did not enter a label but a value: set value for all languages
			if (defaultLanguageCodes != null) {
				for (String lang : defaultLanguageCodes) {
					label.put(lang, value);
				}
			}

			if (label.isEmpty()) {
				label.put(Locale.getDefault().getLanguage(), value);
			}
		}

		listValue.setLabel(label);
		listValue.setValue(value);
	}


	/**
	 * Generate the value based on the label.
	 * The first label is taken as the value, but all commas are replaced by space.
	 * @param label
	 * @return
	 */
	private String buildValueFromLabel(LanguageString label) {
		String firstLabel = label.getNames().get(0);

		StringBuilder value = new StringBuilder( firstLabel.length() );

		for (int i = 0; i < firstLabel.length(); i++) {
			char currentChar = firstLabel.charAt(i);

			// replace comma by space if the next char is not a space
			if (currentChar == ',') {
				if (i + 1 < firstLabel.length() && firstLabel.charAt(i + 1) != ' ') {
					value.append(' ');
				}
			}
			else {
				value.append(currentChar);
			}
		}

		return value.toString();
	}


	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

}
