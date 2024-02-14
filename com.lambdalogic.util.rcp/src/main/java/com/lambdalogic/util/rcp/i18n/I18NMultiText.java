package com.lambdalogic.util.rcp.i18n;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static com.lambdalogic.util.MapHelper.createHashMap;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.DefaultLanguageProvider;
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
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

/**
 * A widget to display and edit internationalized Strings.
 * The widget contains a tab folder with one tab for each language.
 * The tabs contain several Text and Label widgets for the String values and their labels (of the tab's language).
 * A button on the top right corner opens a dialog to define the available language tabs.
 * 
 * <p>
 * The constructors are to be given the array of labels, the LanguageStrings are entered and retrieved on a per
 * label basis. 
 * <p>
 * Furthermore it is possible to determine via a boolean array which text are to be multiLine-line.
 * 
 * 
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
 * 			CTabItem <---------------|
 *                                   |
 * 			Composite: GridLayout <--|
 * 				Label
 * 				Text / MultiLineText
 * 
 * Important characteristics:
 * - Tabs of default language appear always at first, even if there is no corresponding value.
 * - After setting a value the first Tab with a non-empty value is selected.
 * - Setting a value causes the widget to adapt (add and remove) Tabs as needed.
 */
public class I18NMultiText extends Composite implements ModifyListener {

	/**
	 * CTabFolder that contains all CTabItem in cTabItemList.
	 */
	private CTabFolder folder;
	
	/**
	 * Button to open a dialog to define the languages.
	 */
	private Button languageButton;
	
	/**
	 * Names of the labels on each tab.
	 * The Label widgets on each tab have the same text values.
	 */
	private String[] labelNames;
	
	/**
	 * Defines which Text widgets show multiple lines (true) or only one (false).
	 */
	private boolean[] multiLine;
	
	/**
	 * Defines which Text widgets are mandatory.
	 */
	private boolean[] required;
	
	
	/* The fields languageList, cTabItemMap, textWidgetMap, languageToNameToLabelMap and languageToNameToTextMap 
	 * correspond to each other.
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
	 * Map from Language to a Map from label name to Label widget.
	 * 
	 * Language --> labelName -- Label
	 */
	private Map<Language, Map<String, Label>> languageToNameToLabelMap = createHashMap(10);
	
	/**
	 * Map from Language to a Map from label name to Text widget.
	 * 
	 * Language --> labelName -- Text
	 */
	private Map<Language, Map<String, Text>> languageToNameToTextMap = createHashMap(10);

	
	/**
	 * Stores if this widget is enabled.
	 */
	private boolean enabled = true;

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
	public I18NMultiText(
		Composite parent, 
		int style, 
		String[] labelNames, 
		boolean[] multiLine, 
		boolean[] required,
		ILanguageProvider languageProvider, 
		List<String> initialLanguagePKs
	) {
		super(parent, style);
		
		if (labelNames == null) {
			throw new IllegalArgumentException("Parameter 'labelNames' must not be null.");
		}
		
		this.labelNames = labelNames;
		
		// init multiLine
		if (multiLine == null) {
			multiLine = new boolean[labelNames.length];
		}
		this.multiLine = multiLine;
		
		// init required
		if (required == null) {
			required = new boolean[labelNames.length];
		}
		this.required = required;

		// init LanguageProvider
		if (languageProvider == null) {
			languageProvider = DefaultLanguageProvider.getInstance();
		}
		this.languageProvider = languageProvider;
		
		
		initDefaultLanguageList(initialLanguagePKs);
		

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

	
	public I18NMultiText(
		Composite parent, 
		int style, 
		String[] labels, 
		boolean[] multiLine, 
		boolean[] required,
		ILanguageProvider languageProvider
	) {
		this(
			parent, 
			style, 
			labels, 
			multiLine, 
			required, 
			languageProvider, 
			null	// List<String> initialLanguagePKs
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
		
		// call setEnabled() of all Text widgets
		for (Map<String, Text> nameToTextMap : languageToNameToTextMap.values()) {
			for (Text text : nameToTextMap.values()) {
				text.setEnabled(enabled);
			}
		}
	}
	

	/**
	 * Return a LanguageString built from all non-empty text widgets in all tabs that belong to the given label.
	 * The result is always a new LanguageString.
	 */
	public LanguageString getLanguageString(String label) {
		LanguageString languageString = new LanguageString();
		for (Language language : languageList) {
			String value = languageToNameToTextMap.get(language).get(label).getText();
			if (isNotEmpty(value)) {
				languageString.put(language.getLanguageCode(), value);
			}
		}
		return languageString;

	}
	

	/**
	 * Wrapper for setLanguageString method to create tabs for default languages even if they are empty
	 * 
	 * @param labelsToLanguageStringMap
	 * @param defaultLanguages
	 */
	public void setLanguageString(
		Map<String, LanguageString> labelsToLanguageStringMap, 
		Collection<String> defaultLanguagePKs
	) {
		// initialize default languages
		setDefaultLanguages(defaultLanguagePKs);

		/* Determine the languages that shall appear, that are:
		 * - the default languages
		 * - the languages of the LanguageStrings in the values of the parameter labelsToLanguagesMap
		 * The order is relevant, because the Tabs of the default languages shall appear first.   
		 */
		
		// create usedLanguages and initialize with default languages
		ListSet<Language> usedLanguages = new ListSet<>(defaultLanguageList, 10);
		
		// add languages of languageString (new value)
		if (labelsToLanguageStringMap != null) {
			// for all LanguageStrings
			for (LanguageString languageString : labelsToLanguageStringMap.values()) {
				if (languageString != null) {
					// for all Languages
					for (String lang : languageString.getUsedLanguageCodes()) {
						Language language = languageProvider.getLanguageByCode(lang);
						if (language != null) {
							usedLanguages.add(language);
						}
					}
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

		// for all LanguageStrings
		for (Map.Entry<String, LanguageString> entry : labelsToLanguageStringMap.entrySet()) {
			String label = entry.getKey();
			LanguageString languageString = entry.getValue();
			
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
				// Set a (possibly empty) string for all widgets, to avoid stale values (MIRCP-1661)
				languageToNameToTextMap.get(language).get(label).setText(value);
			}
		}
		
		// select first folder with non-empty value
		selectFirstNonEmptyFolder();
	}


	/**
	 * Creates tabs if necessary for languages in the LanguageString that are not yet shown, and puts afterwards each
	 * language-specific String in the text widget of the according tab
	 * 
	 * @param languageString
	 */
	public void setLanguageString(String label, LanguageString languageString) {
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

		// do not remove any tab, because it might contain data, even if this LanguageString does not contain its language

		
		
		
		// Put each language-specific String in the text widget of the according tab
		int indexOfFirstNonEmptyValue = -1;
		for (int i = 0; i < languageList.size(); i++) {
			Language language = languageList.get(i);

			String value = null;
			if (languageString != null) {
				value = languageString.getString(language.getLanguageCode(), false);
			}
			
			if (value == null) {
				value = "";
			}
			else if (indexOfFirstNonEmptyValue < 0) {
				// remember first Tab with non-empty value
				indexOfFirstNonEmptyValue = i;
			}
			
			/* Set String value even if it is empty to remove values that have been deleted.
			 * ( avoid stale values (MIRCP-1661) )
			 */
			languageToNameToTextMap.get(language).get(label).setText(value);
		}
		
		
		selectFirstNonEmptyFolder();
	}

	
	private void selectFirstNonEmptyFolder() {
		int indexOfFirstNonEmptyValue = -1;
		for (int i = 0; i < languageList.size(); i++) {
			Language language = languageList.get(i);
			Map<String, Text> labelToText = languageToNameToTextMap.get(language);
			for (Text text : labelToText.values()) {
				if (isNotEmpty(text.getText())) {
					indexOfFirstNonEmptyValue = i;
					break;
				}
			}
			if (indexOfFirstNonEmptyValue >= 0) {
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
				// remove and dispose all Label widgets of Language
				Map<String, Label> nameToLabelMap = languageToNameToLabelMap.remove(language);
				for (Label label : nameToLabelMap.values()) {
					label.dispose();
				}
				
				// remove and dispose Text widgets of one language & set modified true if Text is not empty
				Map<String, Text> nameToTextMap = languageToNameToTextMap.remove(language);
				for (Text text : nameToTextMap.values()) {
					if (text.getText().length() > 0) {
						modified = true;
					}
					text.dispose();
				}
				
				
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
	 * Opens a dialog to change the languages to be shown in this widget. When the dialog is closed, CTabFolders are
	 * added and/or removed according to the desired set of languages.
	 */
	public void openDialog() {
		// open LanguageSelectionDialog (modal) and get code constant (defined in Window) of selected button
		LanguageSelectionDialog dialog = new LanguageSelectionDialog(this.getShell(), languageProvider, languageList);
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
		for (Map.Entry<Language, Map<String, Text>> entry0 : languageToNameToTextMap.entrySet()) {
			Map<String, Text> nameToTextMap = entry0.getValue();
			for (Map.Entry<String, Text> entry1 : nameToTextMap.entrySet()) {
				if (entry1.getValue() == text) {
					return entry0.getKey();
				}
			}
		}
		return null;
	}

	/**
	 * Whenever the content of a widget changes, adapt the icon and the font of its containing tab either to
	 * bold and a tick (if content is present) or normal and ? (if no content is present).
	 */
	public void modifyText(ModifyEvent e) {
		Text text = (Text) e.widget;

		Language language = findLanguage(text);
		if (language == null) {
			throw new RuntimeException("Invalid state in I18NText: language == null");
		}

		
		// determine if all Text widgets of this Language are not empty
		Collection<Text> textCol = languageToNameToTextMap.get(language).values();
		boolean allTextNotEmpty = true;
		for (Text currentText: textCol) {
			if (currentText.getText().trim().length() == 0) {
				allTextNotEmpty = false;
				break;
			} 
		}
		
		
		CTabItem cTabItem = cTabItemMap.get(language);

		if (allTextNotEmpty) {
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
		boolean modify = ! languageList.contains(language);
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


		Composite composite = new Composite(folder, SWT.NONE);
			
		cTabItem.setControl(composite);

		
		// set image
		String image = languageProvider.getDefaultLanguage().equals(language) ? Images.UNKNOWN_DEFAULT : Images.UNKNOWN;
		cTabItem.setImage( Images.get(image) );

		
		cTabItem.setFont(JFaceResources.getDefaultFont());
		cTabItemMap.put(language, cTabItem);

		Map<String, Label> nameToLabelMap = createHashMap(10);
		Map<String, Text> nameToTextMap = createHashMap(10);

		composite.setLayout(new GridLayout(2, false));
		
		for (int i = 0; i < labelNames.length; i++) {
			String name = labelNames[i];
			Label label = new Label(composite, SWT.NONE);
			label.setText(name);
			
			Text text = null;
			
			if (multiLine[i]) {
				// create MultiLineText
				MultiLineText mlText = new MultiLineText(
					composite,	// parent
					SWT.BORDER,	// type
					true		// dynamic
				);
				
				/* Set minLineCount after LayoutData!
				 */
				mlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				mlText.setMinLineCount(2);
				
				text = mlText;
				
				// set LayoutData to label
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				label.setLayoutData(gridData);
			} 
			else {
				// create Text
				text = new Text(composite, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true , false));
				
				// set LayoutData to label
				GridData gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				label.setLayoutData(gridData);
			}
			
			text.setEnabled(enabled);
			
			text.addModifyListener(this);
			
			if (required[i]) {
				SWTHelper.makeBold(label);
				SWTHelper.makeBold(text);
			}
			
			// add Label to its Map
			nameToLabelMap.put(name, label);
			// add Text to its Map
			nameToTextMap.put(name, text);
		}
		
		// add inner Maps to global Maps
		languageToNameToLabelMap.put(language, nameToLabelMap);
		languageToNameToTextMap.put(language, nameToTextMap);
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


	/**
	 * Set tool tips to Labels of all tabs.
	 * @param toolTips - tool tip text values in same order as Label widgets 
	 */
	public void setToolTips(String[] toolTips) {
		// for all nameToLabelMap (inner Maps)
		for (Map<String, Label> nameToLabelMap : languageToNameToLabelMap.values()) {
			// iterate over label names
			for (int i = 0; i < labelNames.length; i++) {
				if (toolTips.length > i) {
					// get Label widget of current label name
					Label label = nameToLabelMap.get( labelNames[i] );
					if (label != null) {
						// set toolTip of same index
						label.setToolTipText( toolTips[i] );
					}
				}
			}
		}
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}
