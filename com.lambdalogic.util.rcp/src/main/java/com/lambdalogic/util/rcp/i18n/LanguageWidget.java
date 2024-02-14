package com.lambdalogic.util.rcp.i18n;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.DefaultLanguageProvider;
import com.lambdalogic.i18n.ILanguageProvider;
import com.lambdalogic.i18n.Language;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.IconRegistry;
import com.lambdalogic.util.rcp.Images;
import com.lambdalogic.util.rcp.ModifySupport;


public class LanguageWidget extends Composite {

	private ILanguageProvider languageProvider;

	/**
	 * List of Languages that represent the current value.
	 */
	private List<Language> languageList = createArrayList();


	private ModifySupport modifySupport = new ModifySupport(this);

	private Composite languageComposite;

	/**
	 * Button to open a dialog to define the languages.
	 */
	private Button languageButton;


	public LanguageWidget(Composite parent, int style, ILanguageProvider languageProvider) {
		super(parent, style);

		// init LanguageProvider
		if (languageProvider == null) {
			languageProvider = DefaultLanguageProvider.getInstance();
		}
		this.languageProvider = languageProvider;

		createWidgets();
	}


	private void createWidgets() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = false;
		setLayout(gridLayout);

		languageComposite = new Composite(this, SWT.NONE);
		languageComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		RowLayout languageCompositeLayout = new RowLayout();
		languageCompositeLayout.marginTop = 0;
		languageCompositeLayout.marginBottom = 0;
		languageComposite.setLayout(languageCompositeLayout);



		languageButton = new Button(this, SWT.PUSH);

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


			List<Language> newLanguageList = createArrayList(selectedLanguages);


			boolean equal = languageList.size() == newLanguageList.size()
				&& languageList.containsAll(newLanguageList)
				&& newLanguageList.containsAll(languageList);

			if ( ! equal) {
				languageList = newLanguageList;
				syncCompositeToList();

				try {
					modifySupport.fire();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		}
	}


	private void syncCompositeToList() {
			for (Control control : languageComposite.getChildren()) {
				control.dispose();
			}

			for (Language language : languageList) {
				Image image = IconRegistry.getLanguageIcon(language.getLanguageCode());
				Label imageLabel = new Label(languageComposite, SWT.NONE);
				imageLabel.setImage(image);
				imageLabel.setAlignment(SWT.CENTER);

				Label textLabel = new Label(languageComposite, SWT.NONE);
				textLabel.setText(language.getLanguageName().getString());
			}

			/* Indeed both calls of layout() are necessary!
			 * layout() is necessary to make the first selected language visible.
			 * languageComposite.layout() is necessary to make all further changes visible.
			 */
			layout();
			languageComposite.layout();
	}


	public List<Language> getLanguageList() {
		return Collections.unmodifiableList(languageList);
	}


	public List<String> getLanguageCodeList() {
		List<String> languageCodeList = new ArrayList<>();
		if (notEmpty(languageList)) {
			for (Language language : languageList) {
				languageCodeList.add(language.getLanguageCode());
			}
		}
		return languageCodeList;
	}


	public void setLanguageList(List<Language> languageList) {
		this.languageList = languageList;
		syncCompositeToList();
	}


	public void setLanguageCodeList(List<String> languageCodeList) {
		this.languageList = new ArrayList<>();

		if (notEmpty(languageCodeList)) {
			for (String code : languageCodeList) {
				Language language = languageProvider.getLanguageByCode(code);
				this.languageList.add(language);
			}
		}

		syncCompositeToList();
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
