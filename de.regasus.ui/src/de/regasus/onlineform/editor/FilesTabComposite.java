package de.regasus.onlineform.editor;


import static de.regasus.LookupService.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.messeinfo.regasus.UploadableFileType;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.Vigenere;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.TemplateHelper;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.onlineform.dialog.UploadFileDialog;
import de.regasus.onlineform.provider.DataStoreTableLabelProvider;
import de.regasus.onlineform.provider.DataStoreTableSorter;

/**
 * This class realizes an editor tab to manage files that belong to the RegistrationFormConfig entity.
 * <p>
 * Those files are however not attributes of the entity, because they might be large, and the decision had been
 * to store them separately in the "datastore", which is a table for BLOBs and some metadata.
 * <p>
 * The scenarios are similar to those with eg invoice number range templates: the user can upload, download
 * and delete them. The differences here are
 * <ul>
 * <li>the user cannot choose freely which file to upload, but only
 * use certain files predefined in @see {@link UploadableFileType}.</li>
 * </ul>
 */
public class FilesTabComposite extends Composite {

	private List<DataStoreVO> dataStoreVOs = new ArrayList<>();

	private Table table;

	private TableViewer tableViewer;

	private Long eventPK;

	private Button uploadFileButton;

	private Button dowloadFileButton;

	private Button deleteTemplateButton;

	private Button copyURLButton;

	private MenuItem downloadMenuItem;

	private MenuItem deleteMenuItem;

	private MenuItem uploadMenuItem;

	private MenuItem copyURLMenuItem;

	private MenuItem infoMenuItem;

	private RegistrationFormConfig registrationFormConfig;


	// **************************************************************************
	// * Widgets
	// *

	public FilesTabComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(2, false));

		// ==================================================
		// The table to show already uploaded files

		Composite tableComposite = new Composite(this, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		layoutData.heightHint = 150;
		tableComposite.setLayoutData(layoutData);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		table = new Table(tableComposite, SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setToolTipText(OnlineFormI18N.ToolTipFiles);

		TableColumn fileTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(fileTableColumn, new ColumnWeightData(300));
		fileTableColumn.setText(OnlineFormI18N.File);
		fileTableColumn.setWidth(600);

		TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(languageTableColumn, new ColumnWeightData(200));
		languageTableColumn.setText(OnlineFormI18N.Language);
		languageTableColumn.setWidth(100);

		TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(nameTableColumn, new ColumnWeightData(200));
		nameTableColumn.setText(OnlineFormI18N.FileName);
		nameTableColumn.setWidth(400);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new DataStoreTableLabelProvider());
		tableViewer.setSorter(new DataStoreTableSorter());
		tableViewer.setInput(dataStoreVOs);
		tableViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonStates();
			}
		});

		initializeContextMenu();

		// ==================================================
		// The 4 buttons for upload, download, copy URL delete actions

		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		RowLayout buttonCompositeRowLayout = new RowLayout();
		buttonCompositeRowLayout.pack = false;
		buttonCompositeRowLayout.spacing = 5;
		buttonCompositeRowLayout.wrap = false;
		buttonCompositeRowLayout.justify = true;
		buttonComposite.setLayout(buttonCompositeRowLayout);

		// Upload Button
		uploadFileButton = new Button(buttonComposite, SWT.PUSH);
		uploadFileButton.setText(UtilI18N.Upload);
		uploadFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					uploadFile();
				}
				catch (Exception e1) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, FilesTabComposite.class.getName(), e1);
				}
			}
		});

		// Download Button
		dowloadFileButton = new Button(buttonComposite, SWT.PUSH);
		dowloadFileButton.setText(UtilI18N.Download);
		dowloadFileButton.setEnabled(false);
		dowloadFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				downloadFile();
			}
		});

		// Delete Button
		deleteTemplateButton = new Button(buttonComposite, SWT.PUSH);
		deleteTemplateButton.setText(UtilI18N.Delete);
		deleteTemplateButton.setEnabled(false);
		deleteTemplateButton.setImage(de.regasus.core.ui.IconRegistry.getImage(
			de.regasus.core.ui.IImageKeys.DELETE
			));

		deleteTemplateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteFile();
			}
		});

		// Copy URL Button
		copyURLButton = new Button(buttonComposite, SWT.PUSH);
		copyURLButton.setText(OnlineFormI18N.CopyURL);
		copyURLButton.setToolTipText(OnlineFormI18N.CopyImageURLToClipboard);
		copyURLButton.setEnabled(false);
		copyURLButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyURL();
			}
		});
	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig) throws Exception {
		this.registrationFormConfig = registrationFormConfig;
		this.eventPK = registrationFormConfig.getEventPK();

		updateButtonStates();

		syncWidgetsToEntity();
	}


	// **************************************************************************
	// * The internal methods belonging to the buttons
	// *


	protected void uploadFile() {
		try {

			UploadFileDialog dialog = new UploadFileDialog(getShell(), registrationFormConfig.getLanguageCodesList());

			// If a DataStore is selected, use its properties for initial values
			DataStoreVO dataStoreVO = SelectionHelper.getUniqueSelected(tableViewer);
			if (dataStoreVO != null) {
				dialog.setInitialDataStoreVO(dataStoreVO);
			}
			dialog.setExistingDataStoreVOList(dataStoreVOs);

			int code = dialog.open();
			if (code == Window.OK) {
				String filePath = dialog.getFilePath();
				String language = dialog.getLanguage();
				UploadableFileType fileType = dialog.getUploadableFileType();
				DataStoreVO replacedDataStoreVO = dialog.getReplacedDataStoreVO();

				if (filePath != null) {
					File file = new File(filePath);
					if (file.exists() && file.canRead() && file.isFile()) {

						byte[] content = FileHelper.readFile(file);

						DataStoreVO uploadedDataStoreVO = getRegistrationFormConfigMgr().uploadFile(eventPK.longValue(), registrationFormConfig.getId(), fileType, language, file, content);
						if (replacedDataStoreVO != null) {
							dataStoreVOs.remove(replacedDataStoreVO);
						}

						dataStoreVOs.add(uploadedDataStoreVO);

						tableViewer.refresh();
						tableViewer.setSelection(new StructuredSelection());
						updateButtonStates();
					}
					else {
						MessageDialog.openError(getShell(), UtilI18N.Error, OnlineFormI18N.CannotAccessFile);
					}
				}
			}
		}
		catch (Exception e1) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, FilesTabComposite.class.getName(), e1);
		}
	}


	protected void deleteFile() {
		try {
			DataStoreVO dataStoreVO = SelectionHelper.getUniqueSelected(tableViewer);


			// Ask the user to confirm
			String message = NLS.bind(UtilI18N.ReallyDeleteOne, UtilI18N.File, dataStoreVO.getExtFileName());
			boolean deleteOK = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), UtilI18N.Confirm, message);

			// If deletion was confirmed
			if (deleteOK) {
				getDataStoreMgr().delete(dataStoreVO.getPK());

				dataStoreVOs.remove(dataStoreVO);
				tableViewer.refresh();
				updateButtonStates();
			}
		}
		catch (Exception ex) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, FilesTabComposite.class.getName(), ex);
		}
	}


	protected void downloadFile() {
		try {
			DataStoreVO dataStoreVO = SelectionHelper.getUniqueSelected(tableViewer);

			// Delegate the work to an existing class the already does the required function
			TemplateHelper.downloadTemplate(dataStoreVO);
		}
		catch (Exception ex) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, FilesTabComposite.class.getName(), ex);
		}
	}


	protected void copyURL() {
		try {
			DataStoreVO dataStoreVO = SelectionHelper.getUniqueSelected(tableViewer);
			String baseUrl = ServerModel.getInstance().getBaseUrl();
			String vigenere = Vigenere.toVigenereString(registrationFormConfig.getId());
			String docType = dataStoreVO.getDocType();
			String language = dataStoreVO.getLanguage();

			StringBuilder url = new StringBuilder(512);
			url.append(baseUrl);
			if ( !baseUrl.endsWith("/") ) {
				url.append("/");
			}
			url.append("online");
			url.append("/");
			url.append("datastore");
			url.append("?rpk=").append(vigenere);
			url.append("&file=").append(docType);
			url.append("&lang=").append(language);

			ClipboardHelper.copyToClipboard( url.toString() );
		}
		catch (Exception ex) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, FilesTabComposite.class.getName(), ex);
		}
	}


	// **************************************************************************
	// * Completely internal methods
	// *


	/**
	 * This method may be called frequently, but we only want to reload the data
	 * from the server upon getting visible the first time, and upon refresh
	 */
	private void syncWidgetsToEntity() {
		if (dataStoreVOs.isEmpty()) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// We want to show all uploaded files in a table, with making only one server call

						dataStoreVOs.clear();
						Long id = registrationFormConfig.getId();
						if (id != null) {
							dataStoreVOs.addAll(getRegistrationFormConfigMgr().getUploadedFiles(id));
						}

						tableViewer.refresh();
						table.setFocus();

						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	/**
	 * Download and deletion are to be enabled when a row is selected that has an existing data store entry.
	 */
	private void updateButtonStates() {

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				// Since MIRCP-2150, all files belong to a config, so to have one with id is precondition
				boolean configCreated = (registrationFormConfig != null && registrationFormConfig.getId() != null);
				uploadFileButton.setEnabled(configCreated);
				uploadMenuItem.setEnabled(configCreated);
				DataStoreVO dataStoreVO = SelectionHelper.getUniqueSelected(tableViewer);
				boolean somethingSelected = (dataStoreVO != null);
				deleteTemplateButton.setEnabled(somethingSelected);
				deleteMenuItem.setEnabled(somethingSelected);
				dowloadFileButton.setEnabled(somethingSelected);
				downloadMenuItem.setEnabled(somethingSelected);
				copyURLButton.setEnabled(somethingSelected);
				copyURLMenuItem.setEnabled(somethingSelected);
				infoMenuItem.setEnabled(somethingSelected);
			}
		});
	}



	private void initializeContextMenu() {
		final Menu menu = new Menu (getShell(), SWT.POP_UP);

		uploadMenuItem = new MenuItem (menu, SWT.PUSH);
		uploadMenuItem.setText(UtilI18N.Upload);
		uploadMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uploadFile();
			}
		});

		downloadMenuItem = new MenuItem (menu, SWT.PUSH);
		downloadMenuItem.setText(UtilI18N.Download);
		downloadMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				downloadFile();
			}
		});

		deleteMenuItem = new MenuItem (menu, SWT.PUSH);
		deleteMenuItem.setText(UtilI18N.Delete);
		deleteMenuItem.setImage(IconRegistry.getImage(IImageKeys.DELETE));
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteFile();
			}
		});

		copyURLMenuItem = new MenuItem (menu, SWT.PUSH);
		copyURLMenuItem.setText(OnlineFormI18N.CopyURL);
		copyURLMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyURL();
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		infoMenuItem = new MenuItem (menu, SWT.PUSH);
		infoMenuItem.setText(UtilI18N.Info);
		infoMenuItem.setImage(IconRegistry.getImage(IImageKeys.INFORMATION));
		infoMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openDataStoreVOInfoDialog();
			}
		});

		table.setMenu(menu);
	}


	protected void openDataStoreVOInfoDialog() {
		DataStoreVO dataStoreVO = SelectionHelper.getUniqueSelected(tableViewer);
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.EditDateTime,
			UtilI18N.Path,
			UtilI18N.Name,
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(dataStoreVO.getID()),
			formatHelper.formatDateTime(dataStoreVO.getNewTime()),
			formatHelper.formatDateTime(dataStoreVO.getEditTime()),
			dataStoreVO.getPath(),
			dataStoreVO.getExtFileName(),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getShell(),
			UtilI18N.Info,
			labels,
			values
			);

		infoDialog.open();
	}
}
