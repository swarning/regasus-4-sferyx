package de.regasus.onlineform.editor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.i18n.ILanguageProvider;

import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.onlineform.OnlineFormI18N;

public abstract class AbstractLanguageButtonGroup extends Group {

	private Button[] buttons;
	private String language;
	private Button selectedButton;
	
	public AbstractLanguageButtonGroup(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(5, false));
		setText(OnlineFormI18N.Language);
		
		SelectionAdapter adapter = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;

				if (button.getSelection()) {
					onButtonSelected(button);
				}
			}

		};
		
		ILanguageProvider languageProvider = LanguageProvider.getInstance();

		// add Button to choose German language
		Button germanButton = new Button(this, SWT.RADIO);
		germanButton.setImage(IconRegistry.getImage("icons/flags/de.png"));
		germanButton.setText(languageProvider.getLanguageByCode("de").getLanguageName().getString());
		germanButton.setData("de");
		germanButton.addSelectionListener(adapter);
		
		// add Button to choose English language
		Button englishButton = new Button(this, SWT.RADIO);
		englishButton.setImage(IconRegistry.getImage("icons/flags/gb.png"));
		englishButton.setText(languageProvider.getLanguageByCode("en").getLanguageName().getString());
		englishButton.setData("en");
		englishButton.addSelectionListener(adapter);
		
		// add Button to choose Spanish language
		Button spanishButton = new Button(this, SWT.RADIO);
		spanishButton.setImage(IconRegistry.getImage("icons/flags/es.png"));
		spanishButton.setText(languageProvider.getLanguageByCode("es").getLanguageName().getString());
		spanishButton.setData("es");
		spanishButton.addSelectionListener(adapter);
		
		// add Button to choose French language
		Button frenchButton = new Button(this, SWT.RADIO);
		frenchButton.setImage(IconRegistry.getImage("icons/flags/fr.png"));
		frenchButton.setText(languageProvider.getLanguageByCode("fr").getLanguageName().getString());
		frenchButton.setData("fr");
		frenchButton.addSelectionListener(adapter);
		
		// add Button to choose Russian language
		Button russianButton = new Button(this, SWT.RADIO);
		russianButton.setImage(IconRegistry.getImage("icons/flags/ru.png"));
		russianButton.setText(languageProvider.getLanguageByCode("ru").getLanguageName().getString());
		russianButton.setData("ru");
		russianButton.addSelectionListener(adapter);
		
		buttons = new Button[]{germanButton, englishButton, spanishButton, frenchButton, russianButton};
	}

	
	abstract void onSelectLanguage(String language);
	

	public void setVisibleLanguages(List<String> languages) {
		
		// If selected language is not visible anymore, null it and deselect its button
		if (language != null && ! languages.contains(language)) {
			selectedButton.setSelection(false);
			language = null;
			selectedButton = null;
		}
		
		// Set only those buttons visible that are contained in the list of selected languages
		for (Button button : buttons) {
			String buttonLanguage = (String) button.getData();
			boolean buttonLanguageVisible = languages.contains(buttonLanguage);
			button.setVisible(buttonLanguageVisible);
		}
		
		// In case there is no selected button/language select the first of the visible ones
		if (selectedButton == null) {
			for (Button button : buttons) {
				String buttonLanguage = (String) button.getData();
				if (languages.contains(buttonLanguage)) {
					button.setSelection(true);
					onButtonSelected(button);
					break;
				}
			}
		}
	}
	
	protected void onButtonSelected(Button button) {
		selectedButton = button;
		language = (String) selectedButton.getData();
		
		onSelectLanguage(language);
	}

	
	@Override
	protected void checkSubclass() {
	}

}
