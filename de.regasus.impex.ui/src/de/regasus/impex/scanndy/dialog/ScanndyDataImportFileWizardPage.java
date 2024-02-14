package de.regasus.impex.scanndy.dialog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.impex.ImpexI18N;



public class ScanndyDataImportFileWizardPage extends WizardPage {

	private static final String NAME = "FileWizardPage";

	private static final String[] filterExtensions = {"*.txt", "*.csv"};

	private static final String DIRECTION_PASS = "p";
    private static final String DIRECTION_IN = "I";
    private static final String DIRECTION_OUT = "O";

	/**
	 * The File which was chosen by the user when this dialog was used the last time (during the current application session)
	 */
	private static Map<Object, File> previousFileMap = new HashMap<Object, File>();

	private Object source;
	private String direction;

	private FileSelectionComposite fileSelectionComposite;
	private Button passButton;
	private Button inButton;
	private Button outButton;

	private ModifySupport modifySupport = new ModifySupport();


	/**
	 * Creates a new FileWizardPage
	 * @param source is used to manage the previous opened files for each source
	 * @param save determines if the WizardPage is used to save (true) or open (false) a file
	 * @param title the title of the WizardPage, if null a default title is used
	 * @param description the description of the WizardPage, if null a default description is used
	 * @param filterExtensions used for the native Open/Close-File-Dialog
	 */
	public ScanndyDataImportFileWizardPage(Object source) {
		super(NAME);
		this.source = source;

		// set title
		setTitle(ImpexI18N.ScanndyDataImportFileWizardPage_OpenTitle);

		// set description
		setDescription(ImpexI18N.ScanndyDataImportFileWizardPage_OpenDesc);
	}


	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout());

		fileSelectionComposite = new FileSelectionComposite(controlComposite, SWT.OPEN);
		fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fileSelectionComposite.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(fileSelectionComposite.fileForOpenOrDirForSaveExists());
				modifySupport.fire(fileSelectionComposite);
			}
		});

		Composite directionComposite = new Composite(controlComposite, SWT.NONE);
		directionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout directionCompositeLayout = new GridLayout(4, false);
		directionComposite.setLayout(directionCompositeLayout);

		Label directionLabel = new Label(directionComposite, SWT.NONE);
		directionLabel.setText(ParticipantLabel.LeadDirection.getString());
		
		// create radio button for PASS
		passButton = new Button(directionComposite, SWT.RADIO);
		passButton.setText(ParticipantLabel.LeadDirectionPass.getString());
		passButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					setDirection(DIRECTION_PASS);
				}
			}
		});

		// create radio button for IN
		inButton = new Button(directionComposite, SWT.RADIO);
		inButton.setText(ParticipantLabel.LeadDirectionIn.getString());
		inButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					setDirection(DIRECTION_IN);
				}
			}
		});
		
		// create radio button for OUT
		outButton = new Button(directionComposite, SWT.RADIO);
		outButton.setText(ParticipantLabel.LeadDirectionOut.getString());
		outButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					setDirection(DIRECTION_OUT);
				}
			}
		});

		// set PASS as default 
		setDirection(DIRECTION_PASS);

		if (filterExtensions != null) {
			fileSelectionComposite.setFilterExtensions(filterExtensions);
		}

		setControl(controlComposite);
		setPageComplete(false);

		File prevFile = getPreviousFile();
		if (prevFile != null) {
			fileSelectionComposite.setFile(prevFile);
			if (prevFile.exists()) {
				setPageComplete(true);
			}
		}
	}


	public String getDirection() {
		return direction;
	}


	public void setDirection(String direction) {
		this.direction = direction;
		if (DIRECTION_PASS.equals(direction)) {
			passButton.setSelection(true);
			inButton.setSelection(false);
			outButton.setSelection(false);
		}
		else if (DIRECTION_IN.equals(direction)) {
			passButton.setSelection(false);
			inButton.setSelection(true);
			outButton.setSelection(false);
		}
		else if (DIRECTION_OUT.equals(direction)) {
			passButton.setSelection(false);
			inButton.setSelection(false);
			outButton.setSelection(true);
		}
	}


	public File getFile() {
		File file = fileSelectionComposite.getFile();
		setPreviousFile(file);
		return file;
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public boolean fileExists() {
		return fileSelectionComposite.fileForOpenOrDirForSaveExists();
	}


	private File getPreviousFile() {
		File file = null;
		if (source != null) {
			file = previousFileMap.get(source);
		}
		return file;
	}


	private void setPreviousFile(File file) {
		if (source != null && file != null) {
			previousFileMap.put(source, file);
		}
	}

}
