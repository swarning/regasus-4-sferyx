package de.regasus.event.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.buildMenuItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.observer.Observer;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.File;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.TemplateHelper;
import de.regasus.event.dialog.UploadEventTemplateDialog;
import de.regasus.ui.Activator;
import de.regasus.util.file.FileEvent;
import de.regasus.util.file.FileEventType;
import de.regasus.util.file.FileObserverService;


public abstract class EventTemplateGroup extends Group implements DisposeListener {

	// the entity
	private Long eventId;

	private Map<Long, de.regasus.common.File> templateMap = new HashMap<>();


	/**
	 * Association between a template (de.regasus.common.File) ID and a file (java.io.File).
	 * Necessary if a file is edited. In this case the file is stored to a temp directory and this temporary path is
	 * set to the template which is necessary for the TemplateTable to decide whether the directory is shown or not.
	 * If new data is set, because the user saves the editor, the temporary pathes have to be restored in the
	 * templates.
	 */
	private Map<Long, java.io.File> editFileMap = new HashMap<>();

	private Map<Long, String> origExtPathMap = new HashMap<>();


	// widgets
	private TemplateTable templateTable;

	private Button editButton;
	private Button uploadButton;
	private Button deleteButton;
	private Button dowloadButton;

	private MenuItem editMenuItem;
	private MenuItem editExistingTemplateMenuItem;
	private MenuItem downloadMenuItem;
	private MenuItem deleteMenuItem;


	protected abstract Collection<de.regasus.common.File> readTemplates(Long eventId)
	throws Exception;

	protected abstract de.regasus.common.File uploadTemplate(Long eventId, byte[] content, String language, String filePath)
	throws Exception;

	protected abstract void deleteTemplate(de.regasus.common.File template)
	throws Exception;

	protected abstract String getUploadDialogTitle();


	protected EventTemplateGroup(Composite parent, String text) {
		super(parent, SWT.NONE);

		setText(text);

		addDisposeListener(this);

		createPartControl();
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		FileObserverService.getInstance().removeObserver(fileObserver);

		// delete temp files
		for (java.io.File file : editFileMap.values()) {
			deleteTempFile(file);
		}
	}


	private void deleteTempFile(java.io.File file) {
		Objects.requireNonNull(file);

		java.io.File dir = file.getParentFile();

		try {
			System.out.println("Delete file " + file);
			file.delete();
		}
		catch (Exception e) {
			System.err.println("Error while deleting file " + file.getAbsolutePath() + ": " + e.getMessage());
		}

		try {
			System.out.println("Delete directory " + dir);
			dir.delete();
		}
		catch (Exception e) {
			System.err.println("Error while deleting directory " + dir.getAbsolutePath() + ": " + e.getMessage());
		}
	}


	protected void createPartControl() {
		try {
    		setLayout( new GridLayout(1, false) );

    		// table to show the templates
    		Composite tableComposite = new Composite(this, SWT.NONE);
    		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    		templateTable = buildTemplateTable(tableComposite);

    		templateTable.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				updateButtonEnabledStates();
    			}
    		});

    		templateTable.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseDoubleClick(MouseEvent e) {
    				editTemplate();
    			}
    		});

    		// buttons to edit, upload, download and delete the templates
    		buildButtonComposite(this);


    		// set data
    		templateTable.setInput( templateMap.values() );

    		updateButtonEnabledStates();
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private TemplateTable buildTemplateTable(Composite parent) {
		TableColumnLayout layout = new TableColumnLayout();
		parent.setLayout(layout);

		Table table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

//		final TableColumn inEditTableColumn = new TableColumn(table, SWT.NONE);
//		inEditTableColumn.setImage( IconRegistry.getImage(IImageKeys.EDIT) );
//		layout.setColumnData(inEditTableColumn, new ColumnPixelData(16, false, true));

		final TableColumn autoUploadTableColumn = new TableColumn(table, SWT.NONE);
		autoUploadTableColumn.setImage( IconRegistry.getImage(IImageKeys.UPLOAD) );
//		autoUploadTableColumn.setText(I18N.TemplateAutoUpload_Text);
		autoUploadTableColumn.setText("Auto");
		autoUploadTableColumn.setToolTipText(I18N.TemplateAutoUpload_ToolTip);
		layout.setColumnData(autoUploadTableColumn, new ColumnPixelData(30, false, true));
		autoUploadTableColumn.setAlignment(SWT.CENTER);

		final TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		languageTableColumn.setText(UtilI18N.Language);
		layout.setColumnData(languageTableColumn, new ColumnWeightData(30));

		final TableColumn fileTableColumn = new TableColumn(table, SWT.NONE);
		fileTableColumn.setText(UtilI18N.File);
		layout.setColumnData(fileTableColumn, new ColumnWeightData(100));

		final TableColumn dirTableColumn = new TableColumn(table, SWT.NONE);
		dirTableColumn.setText(UtilI18N.Directory);
		layout.setColumnData(dirTableColumn, new ColumnWeightData(300));

		final TableColumn editUserTableColumn = new TableColumn(table, SWT.NONE);
		editUserTableColumn.setText(UtilI18N.EditUser);
		layout.setColumnData(editUserTableColumn, new ColumnWeightData(50));

		final TableColumn editTimeTableColumn = new TableColumn(table, SWT.NONE);
		editTimeTableColumn.setText(UtilI18N.EditDateTime);
		layout.setColumnData(editTimeTableColumn, new ColumnWeightData(50));


		TemplateTable templateTable = new TemplateTable(table);

		buildContextMenu(templateTable);

		return templateTable;
	}


	private void buildContextMenu(TemplateTable templateTable) {
		Menu menu = new Menu (getShell(), SWT.POP_UP);

		editMenuItem = buildMenuItem(
			menu,
			I18N.TemplateEdit_Text,
			e -> editTemplate(),
			IconRegistry.getImage(IImageKeys.EDIT)
		);

		editExistingTemplateMenuItem = buildMenuItem(
			menu,
			I18N.EditExistingTemplateFile_Text,
			e -> editExistingTemplateFile(),
			IconRegistry.getImage(IImageKeys.EDIT)
		);

		downloadMenuItem = buildMenuItem(
			menu,
			I18N.TemplateDownload_Text,
			e -> downloadTemplate(),
			IconRegistry.getImage(IImageKeys.DOWNLOAD)
		);

		deleteMenuItem = buildMenuItem(
			menu,
			I18N.TemplateDelete_Text,
			e -> deleteTemplate(),
			IconRegistry.getImage(IImageKeys.DELETE)
		);

		templateTable.getViewer().getTable().setMenu(menu);
	}


	private Composite buildButtonComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		RowLayoutFactory.swtDefaults()
    		.pack(false)
    		.spacing(5)
    		.wrap(false)
    		.justify(true)
    		.applyTo(composite);


		// Edit Button
		if (SystemHelper.canEditOpenOfficeDocs()) {
			editButton = new Button(composite, SWT.PUSH);
			editButton.setText(I18N.TemplateEdit_Text);
			editButton.setToolTipText(I18N.TemplateEdit_ToolTip);
			editButton.setImage( IconRegistry.getImage(IImageKeys.EDIT) );
			editButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					editTemplate();
				}

			});
		}

		// Upload Button
		uploadButton = new Button(composite, SWT.PUSH);
		uploadButton.setText(I18N.TemplateUpload_Text);
		uploadButton.setToolTipText(I18N.TemplateUpload_ToolTip);
		uploadButton.setImage( IconRegistry.getImage(IImageKeys.UPLOAD) );
		uploadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uploadTemplate();
			}
		});

		// Download Button
		dowloadButton = new Button(composite, SWT.PUSH);
		dowloadButton.setText(I18N.TemplateDownload_Text);
		dowloadButton.setToolTipText(I18N.TemplateDownload_ToolTip);
		dowloadButton.setImage( IconRegistry.getImage(IImageKeys.DOWNLOAD) );
		dowloadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					downloadTemplate();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		// Delete Button
		deleteButton = new Button(composite, SWT.PUSH);
		deleteButton.setText(I18N.TemplateDelete_Text);
		deleteButton.setToolTipText(I18N.TemplateDelete_ToolTip);
		deleteButton.setImage( IconRegistry.getImage(IImageKeys.DELETE) );
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteTemplate();
			}
		});

		return composite;
	}


	protected void editTemplate() {
		TableViewer viewer = templateTable.getViewer();

		if (!SystemHelper.canEditOpenOfficeDocs()) {
			return;
		}

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.size() == 1) {
			de.regasus.common.File template = (de.regasus.common.File) selection.getFirstElement();
			Long templateId = template.getId();

			// remove previously edited File form editFileMap
			java.io.File oldFile = editFileMap.remove(templateId);
			if (oldFile == null) {
				oldFile = new java.io.File( template.getExternalPath() );
			}
			// stop observation, because the download might fire a FileObservationEvent
			FileObserverService.getInstance().removeObserver(fileObserver, oldFile);


			java.io.File newFile = TemplateHelper.editTemplate(template);
			if (newFile != null) {
				template.setExternalPath( newFile.getAbsolutePath() );
				editFileMap.put(templateId, newFile);
				FileObserverService.getInstance().addObserver(fileObserver, newFile);
				TemplateTable.setAutoUpload(template, Boolean.TRUE);

				templateTable.refresh();
			}
		}
	}


	protected void editExistingTemplateFile() {
		if (!SystemHelper.canEditOpenOfficeDocs()) {
			return;
		}

		if ( FileHelper.isValidOpenOfficePath() ) {
			TableViewer viewer = templateTable.getViewer();
    		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    		if (selection.size() == 1) {
    			de.regasus.common.File template = (de.regasus.common.File) selection.getFirstElement();
				java.io.File file = new java.io.File( template.getExternalPath() );
				if ( file.exists() ) {
    				try {
    					FileHelper.open(file);
    					TemplateTable.setAutoUpload(template, Boolean.TRUE);
    					templateTable.refresh();
    				}
    				catch (Exception e) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, TemplateHelper.class.getName(), e);
    				}
    			}
    		}
		}
		else {
			MessageDialog.openWarning(
				Display.getDefault().getActiveShell(),
				UtilI18N.Warning,
				de.regasus.core.ui.CoreI18N.OpenOfficePreference_NoProperPathConfiguration
			);
		}
	}


	protected void downloadTemplate() {
		// identify selected template
		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		de.regasus.common.File template = (de.regasus.common.File) selection.getFirstElement();

		// download template
		TemplateHelper.downloadTemplate(template);
	}


	protected void deleteTemplate() {
		boolean deleteOK = MessageDialog.openQuestion(
			Display.getDefault().getActiveShell(),
			de.regasus.core.ui.CoreI18N.TemplateDelete_ConfirmTitle,
			de.regasus.core.ui.CoreI18N.TemplateDelete_ConfirmMessage
		);

		if (!deleteOK) {
			return;
		}


		try {
			IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
			Iterator<de.regasus.common.File> iterator = selection.iterator();
			while (iterator.hasNext()) {
				de.regasus.common.File selectedTemplate = iterator.next();

				try {
					deleteTemplate(selectedTemplate);

					syncWidgetsToEntity();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, TemplateHelper.class.getName(), e);
		}
	}


	protected void uploadTemplate() {
		// determine initial values for file name and language if there is something selected in the table
		java.io.File initFile = null;
		String initLanguage = null;
		{
    		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
    		de.regasus.common.File template = (de.regasus.common.File) selection.getFirstElement();
    		if (template != null) {
    			String externalPath = template.getExternalPath();
    			if (externalPath != null) {
    				initFile = new java.io.File(externalPath);
    			}

    			initLanguage = template.getLanguage();
    		}
		}


		try {
			UploadEventTemplateDialog dialog = new UploadEventTemplateDialog(
				getShell(),
				initFile,
				initLanguage,
				getUploadDialogTitle()
			);
			int code = dialog.open();
			if (code == Window.OK) {
				// The currently selected template is only relevant to initialize the dialog!
				// If the uploaded file replaces an existing one can only be determined after the upload!

				/* An dieser Stelle muss eigentlich ein eventuell vorhandener Eintrag in editFileMap für das Template,
				 * welches sich aktualisieren wird entfernt werden, weil dieses sonst in syncWidgetsToEntity()
				 * in das neue File kopiert wird.
				 *
				 * Leider ist es aber nicht möglich, dieses File zu identifizieren!
				 * An dieser Stele kennne wir nur den Namen der neuen Datei und die ausgewählte Sprache.
				 * Da wir nicht wissen, nach welcher Logik die Datei hochgeladen wird, wissen wir nicht, ob und wenn
				 * welche vorhandene Datei ersetzt wird. Handelt es sich z.B. um ein Badge, würde eine Datei mit
				 * derselben Sprache ersetzt werden, nicht jedoch, wenn sich um eine Benachrichtigung handelt.
				 *
				 * Zwar könnte man die ID des Files in  uploadTemplate(file, language)  zurückgeben,
				 * aber das wäre zu spät, weil  uploadTemplate(file, language)  bereits  syncWidgetsToEntity()
				 * aufruft.
				 *
				 * In  syncWidgetsToEntity()  wird also ggf. das der Name der zuvor verwendeten temporären Datei
				 * in das DataStoreVo kopiert. Aus diesem Grund muss der ursprüngliche Wert manuell wieder hergestellt
				 * werden.
				 */

				java.io.File file = dialog.getFile();
				String language = dialog.getLanguage();

				File newTemplate = uploadTemplate(file, language);

				// start observing new File
				FileObserverService.getInstance().addObserver(fileObserver, file);

				// delete entry in editFileMap
				java.io.File oldFile = editFileMap.remove( newTemplate.getId() );

				// stop observing old File
				if (oldFile != null) {
    				FileObserverService.getInstance().removeObserver(fileObserver, oldFile);
				}

				// load original template from model
				String origExternalPath = origExtPathMap.get( newTemplate.getId() );

				// restore externalPath in new template
				newTemplate.setExternalPath(origExternalPath);

				templateTable.refresh();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private de.regasus.common.File uploadTemplate(java.io.File file, String language) {
		de.regasus.common.File template = null;

		try {
			// read file content
			byte[] content = FileHelper.readFile(file);

			template = uploadTemplate(eventId, content, language, file.getAbsolutePath());

			syncWidgetsToEntity();

			// syncWidgetsToEntity() cloned the template and put it into templateMap
			// get new (cloned) version
			template = templateMap.get( template.getId() );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			updateButtonEnabledStates();
		}

		return template;
	}


	private void updateButtonEnabledStates() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				boolean isNew = eventId == null;
				int count = templateTable.getSelectionCount();

				if (editButton != null) {
					editButton.setEnabled(count == 1);
					editMenuItem.setEnabled(count == 1);


					editExistingTemplateMenuItem.setEnabled(false);
					if (count == 1) {
						IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		    			de.regasus.common.File template = (de.regasus.common.File) selection.getFirstElement();

						java.io.File file = new java.io.File( template.getExternalPath() );
						if (file.exists() && FileHelper.isValidOpenOfficePath()) {
		    				editExistingTemplateMenuItem.setEnabled(true);
		    			}
					}
				}


				dowloadButton.setEnabled(count == 1);
				downloadMenuItem.setEnabled(count == 1);

				deleteButton.setEnabled(count > 0);
				deleteMenuItem.setEnabled(count > 0);

				uploadButton.setEnabled(!isNew);
			}
		});
	}


	private void syncWidgetsToEntity() {
		if (eventId != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						final Collection<de.regasus.common.File> templateCol = readTemplates(eventId);

						// handle templates that do not exist anymore
						Collection<de.regasus.common.File> removedTemplates = templateMap
							.values()
							.stream()
							.filter( template -> !templateCol.contains(template) )
							.collect( Collectors.toList() );

						for (de.regasus.common.File removedTemplate : removedTemplates) {
							templateMap.remove( removedTemplate.getId() );
							origExtPathMap.remove( removedTemplate.getId() );

							java.io.File file = editFileMap.remove( removedTemplate.getId() );
							if (file == null) {
								file = new java.io.File( removedTemplate.getExternalPath() );
							}

							// stop observation
							FileObserverService.getInstance().removeObserver(fileObserver, file);
						}


						for (de.regasus.common.File newTemplate : templateCol) {
							// clone to avoid impact to model data when setting DataStoreVO.extFileName
							newTemplate = newTemplate.clone();

							Long templateId = newTemplate.getId();

							// store original externalPath as File
							origExtPathMap.put(templateId, newTemplate.getExternalPath());

							de.regasus.common.File oldTemplate = templateMap.get(templateId);


							// set AUTO_UPLOAD info
							java.io.File file = new java.io.File( newTemplate.getExternalPath() );
							if ( file.exists() ) {
								Boolean autoUpload = Boolean.FALSE;
								if (oldTemplate != null) {
									// take previous setting if it is not null
									Boolean oldAutoUpload = TemplateTable.getAutoUpload(oldTemplate);
									if (oldAutoUpload != null) {
										autoUpload = oldAutoUpload;
									}
								}

								TemplateTable.setAutoUpload(newTemplate, autoUpload);
							}


							// set IN_EDIT info
							if ( FileHelper.isInEdit(file) ) {
								TemplateTable.setInEdit(newTemplate, true);
							}


							templateMap.put(templateId, newTemplate);

							java.io.File editFile = editFileMap.get( newTemplate.getId() );

							// if the file is currently edited, set temporary path to template
							if (editFile != null) {
								newTemplate.setExternalPath( editFile.getAbsolutePath() );
							}
							else if (oldTemplate == null) {
								// the template is new

								java.io.File newFile = new java.io.File( newTemplate.getExternalPath() );
								if (newFile.getParent() != null) {
									// start observation of new file
									FileObserverService.getInstance().addObserver(fileObserver, newFile);
								}
							}
							else if ( !newTemplate.getExternalPath().equals(oldTemplate.getExternalPath()) ) {
								// stop observation of previous file
								java.io.File oldFile = new java.io.File( oldTemplate.getExternalPath() );
								FileObserverService.getInstance().removeObserver(fileObserver, oldFile);

								java.io.File newFile = new java.io.File( newTemplate.getExternalPath() );
								if (newFile.getParent() != null) {
									// start observation of new file
									FileObserverService.getInstance().addObserver(fileObserver, newFile);
								}
							}
						}

						if (templateTable != null) {
							templateTable.setInput( templateMap.values() );
						}

						updateButtonEnabledStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void setEvent(Long eventId) {
		this.eventId = eventId;
		try {
			syncWidgetsToEntity();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private Observer<FileEvent> fileObserver = new Observer<FileEvent>() {
		@Override
		public void update(Object source, FileEvent event) {
			FileEventType type = event.getType();
			java.io.File file = event.getFile();

			de.regasus.common.File template = findTemplateByFile(file);

			if (template != null) {
				Long templateId = template.getId();

    			if (type == FileEventType.DELETED) {
					editFileMap.remove(templateId);
					setAutoUpload(templateId, null);
    			}
    			else if (type == FileEventType.EDITED) {
        			if ( file.exists() ) {
        				Boolean autoUpload = getAutoUpload(file);
        				if (autoUpload == Boolean.TRUE) {
            				System.out.println("Upload file " + type + " automatically");

                			String language = template.getLanguage();
               				uploadTemplate(file, language);
        				}
        				else {
        					System.err.println("Automatic upload turned off for file " + file);
        				}
        			}
        			else {
        				System.err.println("File " + file + " does not exist");
        			}
    			}
    			else if (type == FileEventType.EDIT_STARTED) {
    				TemplateTable.setInEdit(template, true);

    				templateTable.refresh();
    			}
    			else if (type == FileEventType.EDIT_STOPPED) {
    				TemplateTable.setInEdit(template, false);

    				// check if file is temp file that has been edited
    				String externalPath = template.getExternalPath();
    				java.io.File editFile = editFileMap.get(templateId);
    				// editFile != null --> user clicked the edit button, template has ben saved in temp dir
    				// editFile == null --> user edited existing file that matches externalPath of template
    				if (editFile != null && editFile.getAbsolutePath().equals(externalPath)) {
    					// externalPath is name of temp file that has been edited


    					// stop observation
    					FileObserverService.getInstance().removeObserver(fileObserver, file);

    					// delete temp file --> would cause FileEvent of type EDIT_STOPPED if we would still observing
    					deleteTempFile(file);


    					// restore original extFileName
    					String origExtPath = origExtPathMap.get(templateId);
    					template.setExternalPath(origExtPath);

    					// set AUTO_UPLOAD info
    					java.io.File origExtFile = new java.io.File(origExtPath);
    					if ( origExtFile.exists() ) {
    						TemplateTable.setAutoUpload(template, Boolean.FALSE);
    					}
    					else {
    						// auto-upload not possible
    						TemplateTable.setAutoUpload(template, null);
    					}
    				}

    				templateTable.refresh();
    			}
			}
		}
	};


	private de.regasus.common.File findTemplateByFile(java.io.File file) {
		Objects.requireNonNull(file);

		for (Map.Entry<Long, java.io.File> mapEntry : editFileMap.entrySet()) {
			Long templateId = mapEntry.getKey();
			java.io.File currentFile = mapEntry.getValue();

			if ( file.equals(currentFile) ) {
				return templateMap.get(templateId);
			}
		}

		for (de.regasus.common.File template : templateMap.values()) {
			java.io.File currentFile = new java.io.File( template.getExternalPath() );
			if ( file.equals(currentFile) ) {
				return template;
			}
		}

		return null;
	}


	public Boolean getAutoUpload(Long templateId) {
		Boolean autoUpload = null;

		de.regasus.common.File template = templateMap.get(templateId);
		if (template != null) {
			autoUpload = TemplateTable.getAutoUpload(template);
		}

		return autoUpload;
	}


	public Boolean getAutoUpload(java.io.File file) {
		Boolean autoUpload = null;

		de.regasus.common.File template = findTemplateByFile(file);
		if (template != null) {
			autoUpload = TemplateTable.getAutoUpload(template);
		}

		return autoUpload;
	}


	public void setAutoUpload(Long templateId, Boolean autoUpload) {
		Objects.requireNonNull(templateId);

		de.regasus.common.File template = templateMap.get(templateId);

		if (template != null) {
    		boolean changed = TemplateTable.setAutoUpload(template, autoUpload);
    		if (changed) {
    			templateTable.refresh();
    		}
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
