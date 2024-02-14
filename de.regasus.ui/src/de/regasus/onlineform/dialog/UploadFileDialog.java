package de.regasus.onlineform.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.regasus.UploadableFileType;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.core.LanguageModel;
import de.regasus.core.ui.CoreI18N;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IconRegistry;
import de.regasus.onlineform.combo.UploadableFileTypeCombo;

public class UploadFileDialog extends TitleAreaDialog {

	private static String language;

	private static UploadableFileType type = UploadableFileType.banner;

	private UploadableFileTypeCombo typeCombo;

	private String filePath;

	private UploadableFileType uploadableFileType;

	private Button okButton;

	private FileSelectionComposite fileSelectionComposite;

	private List<String> languageCodesList;

	private LanguageModel languageModel = LanguageModel.getInstance();

	private List<DataStoreVO> existingDataStoreVOList;

	private DataStoreVO replacedDataStoreVO;


	public UploadFileDialog(Shell shell, List<String> languageCodesList) {
		super(shell);

		this.languageCodesList = languageCodesList;

		// If no language was yet used, set it already so that the radio button later
		// will already be selected
		if (language == null) {
			language = languageCodesList.get(0);
		}

	}


	@Override
	public void create() {
		super.create();

		setTitle(UtilI18N.Upload);
		setMessage(de.regasus.onlineform.OnlineFormI18N.SelectTypeLanguageAndFileToUpload);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		try {
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setFont(parent.getFont());

			// typeLabel
			Label typeLabel = new Label(composite, SWT.RIGHT);
			typeLabel.setText(UtilI18N.Type);

			// typeCombo
			typeCombo = new UploadableFileTypeCombo(composite, SWT.NONE);
			typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			typeCombo.setUploadableFileType(type);
			typeCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateFilterExtensions();
				}
			});

			// languageLabel
			Label languageLabel = new Label(composite, SWT.RIGHT);
			languageLabel.setText(UtilI18N.Language);
			languageLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

			SelectionAdapter buttonAdapter = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button button = (Button)e.widget;
					if (button.getSelection()) {
						language = (String) button.getData();
					}
				}
			};

			Composite languageComposite = new Composite(composite, SWT.NONE);
			languageComposite.setLayout(new GridLayout());

			// languageRadioButtons
			for (String languageId : languageCodesList) {
				Button button = new Button(languageComposite, SWT.RADIO);
				button.setImage(IconRegistry.getImage("icons/flags/" + languageId + ".png"));
				button.setText(languageModel.getLanguage(languageId).getName().getString());
				button.setData(languageId);
				if (languageId.equals(language)) {
					button.setSelection(true);
				}
				button.addSelectionListener(buttonAdapter);
			}

			// fileLabel
			Label fileLabel = new Label(composite, SWT.RIGHT);
			fileLabel.setText(UtilI18N.File);

			// fileSelectionComposite
			fileSelectionComposite = new FileSelectionComposite(composite, SWT.OPEN);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fileSelectionComposite.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					refreshOkButtonState();
				}
			});

			updateFilterExtensions();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return composite;
	}


	@Override
	protected void okPressed() {
		filePath = fileSelectionComposite.getFilePath();
		uploadableFileType = typeCombo.getUploadableFileType();

		if (existingDataStoreVOList != null) {
			replacedDataStoreVO = null;

			for (DataStoreVO dataStoreVO : existingDataStoreVOList) {
				if (uploadableFileType.name().equals(dataStoreVO.getDocType()) && language.equals(dataStoreVO.getLanguage()) ) {
					boolean doit = MessageDialog.openQuestion(this.getShell(), UtilI18N.Question, CoreI18N.FileExists_ConfirmOverwrite);
					if (! doit) {
						return;
					}
					else {
						replacedDataStoreVO = dataStoreVO;
					}
				}
			}
		}

		super.okPressed();
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		refreshOkButtonState();
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	private void refreshOkButtonState() {
		boolean fileOrDirExists = fileSelectionComposite.fileForOpenOrDirForSaveExists();
		okButton.setEnabled(fileOrDirExists);
	}


	public String getLanguage() {
		return language;
	}


	public String getFilePath() {
		return filePath;
	}


	public UploadableFileType getUploadableFileType() {
		return uploadableFileType;
	}


	public void setInitialDataStoreVO(DataStoreVO dataStoreVO) {
		language = dataStoreVO.getLanguage();
		type = UploadableFileType.valueOf(dataStoreVO.getDocType());
	}


	public void setExistingDataStoreVOList(List<DataStoreVO> existingDataStoreVOList) {
		this.existingDataStoreVOList = existingDataStoreVOList;
	}


	public DataStoreVO getReplacedDataStoreVO() {
		return replacedDataStoreVO;
	}


	/**
	 * Set possible file extensions to {@link FileSelectionComposite} according to currently selected
	 * {@link UploadableFileType}.
	 */
	private void updateFilterExtensions() {
		UploadableFileType selectedType = typeCombo.getUploadableFileType();
		fileSelectionComposite.setFilterExtensions(selectedType.getFileExtensions());
	}

}
