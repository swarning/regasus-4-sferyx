package de.regasus.portal.pagelayout.editor;

import static com.lambdalogic.util.StringHelper.*;

import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;


public class PageLayoutUserStyleComposite extends Composite {

	private boolean expertMode;

	// the entity
	private PageLayout pageLayout;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *
	private MultiLineText userStyleText;

	// *
	// * Widgets
	// **************************************************************************


	public PageLayoutUserStyleComposite(Composite parent, int style) {
		super(parent, style);

		try {
			expertMode = CurrentUserModel.getInstance().isPortalExpert();
		}
		catch (Exception e) {
			expertMode = false;
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		createWidgets();

		syncWidgetsToEntity();
	}


	private void createWidgets() {
		// layout without margin, because it works only as a container
		GridLayout mainLayout = new GridLayout();
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		setLayout(mainLayout);

		userStyleText = new MultiLineText(this, SWT.BORDER, false);

		// Only in expert mode the user is able to edit the style sheet.
		userStyleText.setEnabled(expertMode);

		userStyleText.addModifyListener(modifySupport);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(userStyleText);

		Composite buttonComposite = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonComposite);

		// layout without margin, because it works only as a container
		GridLayout buttonGridLayout = new GridLayout(2, true);
		buttonGridLayout.marginHeight = 0;
		buttonGridLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonGridLayout);

		GridDataFactory buttonGridDataFactory = GridDataFactory.fillDefaults();

		Button uploadButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(uploadButton);
		uploadButton.setText(UtilI18N.Upload);
		uploadButton.addSelectionListener(uploadButtonListener);

		Button downloadButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(downloadButton);
		downloadButton.setText(UtilI18N.Download);
		downloadButton.addSelectionListener(downloadButtonListener);
	}


	private SelectionListener uploadButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				String[] filterExtensions = {"*.css ; *.txt", "*.css", "*.txt", "*.*"};
				FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				fileDialog.setFilterExtensions(filterExtensions);

				String selectedFilePath = fileDialog.open();
				if (selectedFilePath != null) {
					File file = new File(selectedFilePath);
					if (file.exists()) {
						byte[] fileData = FileHelper.readFile(file);
						if (fileData != null) {
							userStyleText.setText( new String(fileData) );
						}
					}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private SelectionListener downloadButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
    			String fileData = userStyleText.getText();
    			if ( isNotEmpty(fileData) ) {
    				// Open Dialog with originalFile's name and path (if exists)
    				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
    				String filePath = fileDialog.open();

    				// If dialog was not cancelled, fetch contents from server and save in file
    				if (filePath != null) {
    					File file = new File(filePath);
    					FileHelper.writeFile(file, fileData.getBytes());
    				}
    			}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		};
	};


	private void syncWidgetsToEntity() {
		if (pageLayout != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						userStyleText.setText( avoidNull(pageLayout.getStyle()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (pageLayout != null) {
			pageLayout.setStyle( userStyleText.getText() );
		}
	}


	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
		syncWidgetsToEntity();
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
