package com.lambdalogic.util.rcp.i18n;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.DefaultLanguageProvider;
import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.i18n.ILanguageProvider;
import com.lambdalogic.i18n.Language;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.ArrayHelper;
import com.lambdalogic.util.ListSet;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.Images;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

/**
 * A widget to display and edit internationalized Strings.
 * The widget contains a tab folder with one tab for each language.
 * The tabs contain a Text widget for the String value (of the tab's language).
 * A button on the top right corner opens a dialog to define the available language tabs.
 *
 * Architecture of this widget:
 *
 * This Composite contains a CTabFolder that contains CTabItems and Text / MultiLineText.
 * The CTabItems and Text / MultiLineText are connected with each other.
 *
 * Only this Composite is assigned a layout: FillLayout.
 *
 * this.Composite: FillLayout
 * 		CTabFolder
 * 			CTabItem <--------------|
 * 			                        |
 * 			Text / MultiLineText <--|
 *
 * Important characteristics:
 * - Tabs of default language appear always at first, even if there is no corresponding value.
 * - After setting a value the first Tab with a non-empty value is selected.
 * - Setting a value causes the widget to adapt (add and remove) Tabs as needed.
 */
public class I18NText extends Composite implements ModifyListener {

	private CTabFolder folder;

	/**
	 * Button to open a dialog to define the languages.
	 */
	private Button languageButton;


	/* The fields languageList, cTabItemMap and textWidgetMap correspond to each other.
	 * The Maps always have the elements of languageList as keys.
	 */

	/**
	 * List of Languages that are currently visible.
	 */
	private List<Language> languageList = createArrayList();

	/**
	 * Map from Language to CTabItem widget.
	 */
	private Map<Language, CTabItem> cTabItemMap = MapHelper.createHashMap(10);

	/**
	 * Map from Language to Text widget.
	 */
	private Map<Language, Text> textWidgetMap = MapHelper.createHashMap(10);


	/**
	 * Defines if the Text widget shows multiple lines (true) or only one (false).
	 */
	private boolean multiLine;

	/**
	 * Stores if this widget is enabled.
	 */
	private boolean enabled = true;

	/**
	 * Stores if the value of this widget is required.
	 */
	private boolean required = false;


	/**
	 * Default Languages whose Tabs are visible, even if there are no values for them.
	 */
	private List<Language> defaultLanguageList = createArrayList();

	private ILanguageProvider languageProvider;

	private ModifySupport modifySupport;


	/**
	 * Constructor creates a widget that shows tabs for all languages in the language list, the default Locale, and
	 * perhaps for additional languages contained in an added LanguageString
	 */
	public I18NText(
		Composite parent,
		int style,
		ILanguageProvider languageProvider,
		Collection<String> initialLanguagePKs,
		boolean required
	) {
		super(parent, style);

		multiLine = (style & SWT.MULTI) == SWT.MULTI;

		// init LanguageProvider
		if (languageProvider == null) {
			languageProvider = DefaultLanguageProvider.getInstance();
		}
		this.languageProvider = languageProvider;


		initDefaultLanguageList(initialLanguagePKs);

		this.required = required;


		// create widgets

		setLayout(new FillLayout());
		folder = new CTabFolder(this, SWT.BORDER);
		folder.setSimple(false);
		folder.setUnselectedImageVisible(true);
		folder.setUnselectedCloseVisible(false);
		folder.marginWidth = 0;
		folder.marginHeight = 0;

		languageButton = new Button(folder, SWT.PUSH);
		languageButton.setImage(Images.get(Images.LANGUAGES));
		languageButton.setToolTipText(UtilI18N.SelectLanguages_ToolTip);
		languageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openDialog();
				}
				catch (Exception e1) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e1);
				}
			}
		});
		folder.setTopRight(languageButton);


		// create 1 Tab for each default language
		for (Language language : defaultLanguageList) {
			createTab(language);
		}


		folder.setMinimizeVisible(false);
		folder.setMaximizeVisible(false);
		folder.setSelection(0);


		modifySupport = new ModifySupport(this);
	}


	public I18NText(
		Composite parent,
		int style,
		ILanguageProvider languageProvider,
		Collection<String> initialLanguageList
	) {
		this(
			parent,
			style,
			languageProvider,
			initialLanguageList,
			false // required
		);
	}


	public I18NText(
		Composite parent,
		int style,
		ILanguageProvider languageProvider,
		boolean required
	) {
		this(
			parent,
			style,
			languageProvider,
			null,		// initialLanguageList
			required
		);
	}


	/**
	 * Constructor with standard parameters for SWT widgets, plus a languageProvider to determine what the languages are to choose from.
	 */
	public I18NText(
		Composite parent,
		int style,
		ILanguageProvider languageProvider
	) {
		this(
			parent,
			style,
			languageProvider,
			null,		// initialLanguageList
			false		// required
		);
	}


	public I18NText(Composite parent, int style, boolean required) {
		this(
			parent,
			style,
			null,		// languageProvider
			null,		// initialLanguageList
			required
		);
	}


	/**
	 * Constructor with standard parameters for SWT widgets (MIRCP-231)
	 */
	public I18NText(Composite parent, int style) {
		this(
			parent,
			style,
			null,		// languageProvider
			null,		// initialLanguageList
			false		// required
		);
	}


	/**
	 * Initialize defaultLanguageList:
	 * 1. Copy all values from defaultLanguagePKs as Language.
	 * 2. If defaultLanguageList is empty, copy default Languages from LanguageProvider instead.
	 * 3. Otherwise assure that the default Languages from LanguageProvider appear at first in defaultLanguageList.
	 *
	 * @param defaultLanguagePKs
	 */
	private void initDefaultLanguageList(Collection<String> defaultLanguagePKs) {
		// initialize defaultLanguageList
		defaultLanguageList.clear();

		// 1. Copy all values from defaultLanguagePKs as Language.
		if (defaultLanguagePKs != null) {
			for (String lang : defaultLanguagePKs) {
				Language language = languageProvider.getLanguageByCode(lang);
				// language is null if languageProvider does not know lang (a language PK)
				if (language != null) {
					defaultLanguageList.add(language);
				}
			}
		}

		List<Language> globalDefaultLanguages = languageProvider.getDefaultLanguageList();
		if (globalDefaultLanguages != null) {
			// 2. If defaultLanguageList is empty, copy default Languages from LanguageProvider instead.
			if (defaultLanguageList.isEmpty()) {
				defaultLanguageList.addAll(globalDefaultLanguages);
			}
			else {
				// 3. Otherwise assure that the default Languages from LanguageProvider appear at first in defaultLanguageList.

				// iterate over defaultLanguages from end to begin
				for (int i = globalDefaultLanguages.size() - 1; i >= 0; i--) {
					Language defaultLanguage = globalDefaultLanguages.get(i);
					// try to remove defaultLangauge from defaultLanguageList
					boolean removed = defaultLanguageList.remove(defaultLanguage);
					if (removed) {
						// if default Language was in defaultLanguageList: re-add it at the beginning
						defaultLanguageList.add(0, defaultLanguage);
					}
				}
			}
		}
	}


	/**
	 * Set the default languages that shall appear even if there is no corresponding value.
	 * @param defaultLanguagePKs
	 */
	public void setDefaultLanguages(Collection<String> defaultLanguagePKs) {
		// initialize default languages
		if (defaultLanguagePKs != null) {
			initDefaultLanguageList(defaultLanguagePKs);
		}
	}


	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		// the CTabFolder remains always enabled, so that the user can switch between languages

		languageButton.setEnabled(enabled);

		for (Text text : textWidgetMap.values()) {
			text.setEnabled(enabled);
		}
	}


	/**
	 * Return a LanguageString built from all non-empty text widgets in all tabs.
	 * The result is always a new LanguageString.
	 */
	public LanguageString getLanguageString() {
		LanguageString languageString = new LanguageString();
		for (Language language : languageList) {
			String value = textWidgetMap.get(language).getText();
			if (isNotEmpty(value)) {
				languageString.put(language.getLanguageCode(), value);
			}
		}
		return languageString;
	}


	/**
	 * Set a new value for this I18NText widget.
	 * Create additional tabs for languages in the given LanguageString that are not yet shown.
	 * Afterwards each language-specific String is put in the Text widget of the according Tab.
	 *
	 * @param languageString - the new value
	 * @param defaultLanguagePKs - languages that shall appear even if there is no corresponding value
	 */
	public void setLanguageString(LanguageString languageString, Collection<String> defaultLanguagePKs) {
		try {
			// initialize default languages
			setDefaultLanguages(defaultLanguagePKs);

			/* Determine the languages that shall appear, that are:
			 * - the default languages
			 * - the languages in the parameter languageString
			 * The order is relevant, because the Tabs of the default languages shall appear first.
			 */

			// create usedLanguages and initialize with default languages
			ListSet<Language> usedLanguages = new ListSet<>(defaultLanguageList, 10);

			// add languages of languageString (new value)
			if (languageString != null) {
				// for all Languages
				for (String lang : languageString.getUsedLanguageCodes()) {
					Language language = languageProvider.getLanguageByCode(lang);
					if (language != null) {
						usedLanguages.add(language);
					}
				}
			}


			// adapt Tabs

			// create additional Tabs
			for (Language language : usedLanguages) {
				createTabIfNeeded(language);
			}

			// remove Tabs that are not needed anymore
			//removeUnusedLanguageTabs(usedLanguages);


			// Put each language-specific String in the text widget of the according tab
			for (int i = 0; i < languageList.size(); i++) {
				Language language = languageList.get(i);

				String value = null;
				if (languageString != null) {
					value = languageString.getString(language.getLanguageCode(), false);
				}

				if (value == null) {
					value = "";
				}

				/* Set String value even if it is empty to remove values that have been deleted.
				 * ( avoid stale values (MIRCP-1661) )
				 */
				Text text = textWidgetMap.get(language);
				text.setText(value);
			}

			// select first folder with non-empty value
			selectFirstNonEmptyFolder();
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Set a new value for this I18NText widget.
	 * Create additional tabs for languages in the given LanguageString that are not yet shown.
	 * Afterwards each language-specific String is put in the Text widget of the according Tab.
	 *
	 * @param languageString - the new value
	 */
	public void setLanguageString(LanguageString languageString) {
		setLanguageString(languageString, null);
	}


	private void selectFirstNonEmptyFolder() {
		int indexOfFirstNonEmptyValue = -1;
		for (int i = 0; i < languageList.size(); i++) {
			Language language = languageList.get(i);
			Text text = textWidgetMap.get(language);
			if (isNotEmpty(text.getText())) {
				indexOfFirstNonEmptyValue = i;
				break;
			}
		}

		// select first folder with non-empty value
		if (folder.getItemCount() > 0) {
			folder.setSelection(indexOfFirstNonEmptyValue);
		}
	}


	/**
	 * Remove all tabs whose language is not in the parameter usedLanguageList.
	 * The result is true if any of the removed tabs was not empty.
	 * @param usedLanguageList - Collection of Languages that shall remain.
	 * @return
	 */
	public boolean removeUnusedLanguageTabs(Collection<Language> usedLanguageList) {
		boolean modified = false;

		for (Iterator<Language> it = languageList.iterator(); it.hasNext();) {
			Language language = it.next();

			if (!usedLanguageList.contains(language)) {
				// remove and dispose Text widget, set modified true if Text is not empty
				Text text = textWidgetMap.remove(language);
				if (text.getText().length() > 0) {
					modified = true;
				}
				text.dispose();

				// remove and dispose TabItem
				CTabItem tabItem = cTabItemMap.remove(language);
				tabItem.dispose();

				// remove language
				it.remove();
			}
		}

		return modified;
	}


	/**
	 * Open a dialog to change the languages to be shown in this widget.
	 * When the dialog is closed, CTabFolders are added and/or removed according to the desired set of languages.
	 */
	protected void openDialog() {
		// open LanguageSelectionDialog (modal) and get code constant (defined in Window) of selected button
		LanguageSelectionDialog dialog = new LanguageSelectionDialog(getShell(), languageProvider, languageList);
		int button = dialog.open();

		// go on only if OK was pressed, otherwise there is nothing to do
		if (button == Window.OK) {
			Language[] selectedLanguages = dialog.getTabFolderLanguageItems();


			// check if any default language has been removed
			boolean defaultLanguageRemoved = false;
			for (Language language : defaultLanguageList) {
				if ( ! ArrayHelper.contains(language, selectedLanguages)) {
					defaultLanguageRemoved = true;
					break;
				}
			}
			// show warn message if any default language has been removed
			if (defaultLanguageRemoved) {
				MessageDialog.openWarning(getShell(), UtilI18N.Warning, UtilI18N.DefaultLanguagesCannotBeRemoved);
			}


			ListSet<Language> newLanguageList = new ListSet<>();

			// add default languages
			newLanguageList.addAll(defaultLanguageList);

			// add selected Languages
			newLanguageList.addAll(selectedLanguages);


			/* remove tabs for unchecked languages (all other tabs)
			 * Removing a tab also deletes the value of the corresponding language. Therefore a ModifyEvent has to be
			 * fired. However, adding a tab leads to no direct data change, because at this time new tabs don't contain
			 * data yet.
			 */
			boolean modified = removeUnusedLanguageTabs(newLanguageList);

			// add tabs for new languages
			for (Language language : newLanguageList) {
				createTabIfNeeded(language);
			}

			if (modified) {
				try {
					modifySupport.fire();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		}
	}


	/**
	 * Find Language of a Text widget.
	 * @param text
	 * @return
	 */
	private Language findLanguage(Text text) {
		for (Map.Entry<Language, Text> entry : textWidgetMap.entrySet()) {
			if (entry.getValue() == text) {
				return entry.getKey();
			}
		}
		return null;
	}


	/**
	 * Whenever the content of a widget changes, adapt the icon and the font of its containing tab either to
	 * bold and a tick (if content is present) or normal and ? (if no content is present).
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		Text text = (Text) e.widget;

		// find Language of Text widget
		Language language = findLanguage(text);
		if (language == null) {
			throw new RuntimeException("Invalid state in I18NText: language == null");
		}

		// determine if Text widget of this Language is not empty
		boolean textNotEmpty = text.getText().trim().length() > 0;


		CTabItem cTabItem = cTabItemMap.get(language);

		if (textNotEmpty) {
			cTabItem.setImage(Images.get(Images.KNOWN));
			cTabItem.setFont(Activator.getDefault().getFontFromRegistry(Activator.DEFAULT_FONT_BOLD));
		}
		else {
			if (languageProvider.getDefaultLanguageList().contains(language)) {
				cTabItem.setImage(Images.get(Images.UNKNOWN_DEFAULT));
			}
			else {
				cTabItem.setImage(Images.get(Images.UNKNOWN));
			}

			cTabItem.setFont(JFaceResources.getDefaultFont());
		}

		// re-layout, because the number of lines might have changed
		getParent().layout();

		modifySupport.fire();
	}


	/**
	 * Create tab for the given language if it does not already exist.
	 * @param language
	 * @return true if a tab has been created
	 */
	private boolean createTabIfNeeded(Language language) {
		boolean modify =  ! languageList.contains(language);
		if (modify) {
			createTab(language);
		}
		return modify;
	}


	/**
	 * Create tab for the given language.
	 * @param language
	 */
	private void createTab(Language language) {
		// add language to visible List of languages
		languageList.add(language);

		CTabItem cTabItem = new CTabItem(folder, SWT.NONE);
		cTabItem.setText(language.getLanguageCode());
		cTabItem.setToolTipText(language.getLanguageName().getString());

		Text text = null;
		if (multiLine) {
			text = new MultiLineText(
				folder,		// parent
				SWT.BORDER,	// style
				false		// dynamic: value has no effect, dynamic is realized in modifyText() by calling getParent().layout();
			);
		}
		else {
			text = new Text(folder, SWT.BORDER);
		}

		if (required) {
			SWTHelper.makeBold(text);
		}

		text.setEnabled(enabled);

		text.addModifyListener(this);

		cTabItem.setControl(text);


		// set image of cTabItem
		String image = languageProvider.getDefaultLanguage().equals(language) ? Images.UNKNOWN_DEFAULT : Images.UNKNOWN;
		cTabItem.setImage( Images.get(image) );

		// set font of cTabItem
		cTabItem.setFont(JFaceResources.getDefaultFont());

		// add cTabItem to its Map
		cTabItemMap.put(language, cTabItem);
		// add text widget to its Map
		textWidgetMap.put(language, text);
	}


	@Override
	public boolean setFocus() {
		return folder.getSelection().getControl().setFocus();
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}


	public static String getString(I18NString i18nString) {
		String s = null;
		if (i18nString != null) {
			s = i18nString.getString();
		}

		if (s == null) {
			s = "";
		}

		return s;
	}

}
