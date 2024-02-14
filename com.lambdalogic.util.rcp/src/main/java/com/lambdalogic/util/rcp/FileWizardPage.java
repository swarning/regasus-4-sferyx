package com.lambdalogic.util.rcp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.widget.FileSelectionComposite;



public class FileWizardPage extends WizardPage {

	private static final String NAME = "FileWizardPage";
	
	/**
	 * The File which was chosen by the user when this dialog was used the last time (during the current application session) 
	 */
	private static Map<Object, File> previousFileMap = new HashMap<Object, File>();

	private Object source;
	private boolean save;
	private String[] filterExtensions;
	

	private FileSelectionComposite fileSelectionComposite;
	
	private ModifySupport modifySupport = new ModifySupport();
	
	
	/**
	 * Creates a new FileWizardPage
	 * @param source is used to manage the previous opened files for each source
	 * @param save determines if the WizardPage is used to save (true) or open (false) a file
	 * @param title the title of the WizardPage, if null a default title is used
	 * @param description the description of the WizardPage, if null a default description is used
	 * @param filterExtensions used for the native Open/Close-File-Dialog
	 */
	public FileWizardPage(
		Object source,
		boolean save, 
		String title, 
		String description, 
		String[] filterExtensions
	) {
		super(NAME);
		this.source = source;
		this.save = save;
		this.filterExtensions = filterExtensions;
		
		// set title
		if (title != null) {
			setTitle(title);
		}
		else if (save) {
			setTitle(UtilI18N.FileWizardPage_DefaultSaveTitle);
		}
		else {
			setTitle(UtilI18N.FileWizardPage_DefaultOpenTitle);
		}
		
		// set description
		if (description != null) {
			setDescription(description);
		}
		else if (save) {
			setDescription(UtilI18N.FileWizardPage_DefaultSaveDescription);
		}
		else {
			setDescription(UtilI18N.FileWizardPage_DefaultOpenDescription);
		}
	}
	
	
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout());

		int openSaveStyle = 0;
		if (save) {
			openSaveStyle = SWT.SAVE;
		 }
		else {
			 openSaveStyle = SWT.OPEN;
		 }
		
		
		fileSelectionComposite = new FileSelectionComposite(controlComposite, openSaveStyle);
		fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fileSelectionComposite.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				setPageComplete(fileSelectionComposite.fileForOpenOrDirForSaveExists()); 
				modifySupport.fire(fileSelectionComposite);
			}
		});
		
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
