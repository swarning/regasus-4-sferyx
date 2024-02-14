package de.regasus.report.editor;

import static de.regasus.LookupService.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jdom2.JDOMException;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.exception.DirtyWriteException;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.report.ReportLabel;
import com.lambdalogic.messeinfo.report.data.BaseReportCVO;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.report.od.DocumentFormat;
import com.lambdalogic.report.oo.OpenOfficeHelper;
import com.lambdalogic.report.parameter.DefaultReportParameter;
import com.lambdalogic.report.parameter.FormatReportParameter;
import com.lambdalogic.report.parameter.IFormatReportParameter;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.PropertyModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.core.ui.openoffice.OpenOfficeEditor;
import de.regasus.core.ui.openoffice.OpenOfficeEditorInput;
import de.regasus.report.IImageKeys;
import de.regasus.report.IconRegistry;
import de.regasus.report.ReportI18N;
import de.regasus.report.ReportWizardFactory;
import de.regasus.report.combo.BaseReportCombo;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.dialog.ReportWizardDialog;
import de.regasus.report.model.UserReportListModel;
import de.regasus.report.ui.Activator;
import de.regasus.report.view.GenerateReportAction;

/**
 * The editor for "Report Definitions" / "Berichtsdefinitionen".
 */
public class UserReportEditor
extends AbstractEditor<UserReportEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "UserReportEditor";

	// the entity
	private UserReportVO userReportVO;

	// the model
	private UserReportListModel userReportListModel;

	/**
	 * Template of this Report Definition.
	 *
	 * template == null means that we don't know if there is a special template because it isn't loaded.
	 *
	 * template != null && template.getContent() != null means that there is a special template and it is loaded.
	 *
	 * template != null && template.getContent() == null means that there is no special template.
	 *
	 * To delete a special template, the field template must be set to an empty DocumentContainer (content == null).
	 */
	private DocumentContainer template;

	private FormatHelper formatHelper = new FormatHelper();

	/**
	 * Zeigt an, ob die Berichtsdefinition alle zur Generierung notwendigen Parameter enthält.
	 * Fungiert als unsichtbares Widget für UserReportVO.isComplete()
	 */
	private boolean complete = false;


	// **************************************************************************
	// * Widgets
	// *

	private BaseReportCombo baseReportCombo;
	private I18NMultiText i18nMultiText;
	private Text templatePathText;
	private StyledText parameterText;
	private StyledText parameterDescriptionText;
	private Button generateReportButton;
	private Button editTemplateButton;
	private Button openTemplateButton;
	private Button refreshTemplateButton;
	private Button saveTemplateButton;
	private Button editParameterButton;
	private Button deleteTemplateButton;

	private boolean refreshDontAsk;

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long userReportPK = editorInput.getKey();

		// get models
		userReportListModel = UserReportListModel.getInstance();

		if (userReportPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			userReportVO = userReportListModel.getUserReportVO(userReportPK);

			// register at model
			userReportListModel.addListener(this, userReportPK);
		}
		else {
			// create empty entity
			userReportVO = new UserReportVO();
			userReportVO.setUserReportDirID(editorInput.getUserReportDirPK());
		}
	}

	@Override
	public void dispose() {
		if (userReportListModel != null && userReportVO.getID() != null) {
			try {
				userReportListModel.removeListener(this, userReportVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}

	@Override
	protected String getTypeName() {
		return ReportLabel.userReport.getString();
	}

	@Override
	protected String getInfoButtonToolTipText() {
		return ReportI18N.UserReportEditor_InfoButtonToolTip;
	}

	/**
	 * Create contents of the editor part
	 *
	 * @param parent
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite container = SWTHelper.createScrolledContentComposite(parent);
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 2;
			container.setLayout(gridLayout);

			Label baseReportLabel = new Label(container, SWT.NONE);
			baseReportLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			baseReportLabel.setText(ReportLabel.userReport_baseReport.getString());
			SWTHelper.makeBold(baseReportLabel);

			baseReportCombo = new BaseReportCombo(container, SWT.READ_ONLY);
			baseReportCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			baseReportCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (userReportVO != null) {
						UserReportVO newUserReportVO = userReportVO.clone();
						newUserReportVO.initXmlRequest();
						setXmlRequestToWidgets(newUserReportVO);
						syncAvailableFormats();
						setComplete(false);
					}
				}
			});

			String[] labels = {
				ReportLabel.userReport_name.getString(),
				ReportLabel.userReport_description.getString()
			};
			i18nMultiText = new I18NMultiText(
				container,
				SWT.NONE,
				labels,
				new boolean[] {false, true},
				new boolean[] {true, false},
				LanguageProvider.getInstance()
			);
			{
				GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1);
				gridData.heightHint = 120;
				i18nMultiText.setLayoutData(gridData);
			}

			Label templateLabel = new Label(container, SWT.NONE);
			templateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			templateLabel.setText(ReportLabel.baseReport_template.getString());

			templatePathText = new Text(container, SWT.BORDER);
			templatePathText.setEditable(false);

			GridData gd_templatePathText = new GridData(SWT.FILL, SWT.CENTER, true, false);
			templatePathText.setLayoutData(gd_templatePathText);

			new Label(container, SWT.NONE);

			Composite templateButtonsComposite = new Composite(container, SWT.NONE);
			GridData gd_templateButtonsComposite = new GridData();
			templateButtonsComposite.setLayoutData(gd_templateButtonsComposite);
			GridLayout gridLayout_3 = new GridLayout();
			gridLayout_3.marginHeight = 0;
			gridLayout_3.marginWidth = 0;
			gridLayout_3.makeColumnsEqualWidth = true;
			gridLayout_3.numColumns = 5;
			templateButtonsComposite.setLayout(gridLayout_3);


			// "Vorlage bearbeiten"
			// Hide button until editing its working again (https://lambdalogic.atlassian.net/browse/MIRCP-4283)
			boolean deactivated = true;
			if (!deactivated && SystemHelper.canEditOpenOfficeDocs()) {
    			editTemplateButton = new Button(templateButtonsComposite, SWT.PUSH);
    			editTemplateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    			editTemplateButton.setText(ReportI18N.UserReportEditor_EditTemplateButtonText);
    			editTemplateButton.setToolTipText(ReportI18N.UserReportEditor_EditTemplateButtonToolTip);
    			editTemplateButton.addSelectionListener(new SelectionAdapter() {
    				@Override
    				public void widgetSelected(SelectionEvent e) {
    					editTemplate();
    				}
    			});
			}

			// "Vorlage laden"
			openTemplateButton = new Button(templateButtonsComposite, SWT.NONE);
			openTemplateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			openTemplateButton.setText(ReportI18N.UserReportEditor_LoadTemplateButtonText);
			openTemplateButton.setToolTipText(ReportI18N.UserReportEditor_LoadTemplateButtonToolTip);
			openTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					loadTemplate();
				}
			});


			// Aktualisieren
			// Refresh Button
			refreshTemplateButton = new Button(templateButtonsComposite, SWT.PUSH);
			refreshTemplateButton.setText(ReportI18N.UserReportEditor_RefreshTemplateButtonText);
			refreshTemplateButton.setToolTipText(ReportI18N.UserReportEditor_RefreshTemplateButtonToolTip);
			refreshTemplateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			refreshTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						refreshTemplate();
					}
					catch (Exception e1) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
					}
				}

			});
			// enable/disable refreshTemplateButton if there is an individual template or not
			ModifyListener modifyListener_refreshTemplateButton = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					final boolean enable =
						userReportVO.getTemplateID() != null ||
						(template != null && template.getContent() != null);

					refreshTemplateButton.setEnabled(enable);
				}
			};
			templatePathText.addModifyListener(modifyListener_refreshTemplateButton);


			// "Vorlage speichern"
			saveTemplateButton = new Button(templateButtonsComposite, SWT.NONE);
			saveTemplateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			saveTemplateButton.setText(ReportI18N.TemplateDownloadAction_Text);
			saveTemplateButton.setToolTipText(ReportI18N.TemplateDownloadAction_ToolTip);
			saveTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					saveTemplate();
				}
			});

			ModifyListener modifyListener_saveTemplateButton = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					final BaseReportCVO baseReportCVO = baseReportCombo.getEntity();

					final boolean enable =
						(baseReportCVO != null && baseReportCVO.getPK() != null)
						||
						(template != null && template.getContent() != null);

					saveTemplateButton.setEnabled(enable);
				}
			};
			baseReportCombo.addModifyListener(modifyListener_saveTemplateButton);
			templatePathText.addModifyListener(modifyListener_saveTemplateButton);


			// "Vorlage löschen"
			deleteTemplateButton = new Button(templateButtonsComposite, SWT.NONE);
			deleteTemplateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			deleteTemplateButton.setText(ReportI18N.UserReportEditor_DeleteTemplateButtonText);
			deleteTemplateButton.setToolTipText(ReportI18N.UserReportEditor_DeleteTemplateButtonToolTip);
			deleteTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					deleteTemplate();
				}
			});
			ModifyListener modifyListener_deleteTemplateButton = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					final boolean enable =
						userReportVO.getTemplateID() != null ||
						(template != null && template.getContent() != null);

					deleteTemplateButton.setEnabled(enable);
				}
			};
			templatePathText.addModifyListener(modifyListener_deleteTemplateButton);


			Label parameterLabel = new Label(container, SWT.NONE);
			{
			    GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			    gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			    parameterLabel.setLayoutData(gridData);
			}
			parameterLabel.setText(ReportI18N.UserReportEditor_Parameter);

			TabFolder tabFolder = new TabFolder(container, SWT.NONE);
			GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd_tabFolder.heightHint = 100;
			gd_tabFolder.widthHint = 400;
			tabFolder.setLayoutData(gd_tabFolder);

			TabItem descriptionTabItem = new TabItem(tabFolder, SWT.NONE);
			descriptionTabItem.setText(ReportI18N.UserReportEditor_ParameterDesc);

			parameterDescriptionText = new StyledText(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
			parameterDescriptionText.setWordWrap(true);
			descriptionTabItem.setControl(parameterDescriptionText);

			TabItem xmlTabItem = new TabItem(tabFolder, SWT.NONE);
			xmlTabItem.setText(ReportI18N.UserReportEditor_ParameterXML);

			parameterText = new StyledText(tabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			xmlTabItem.setControl(parameterText);


			new Label(container, SWT.NONE);

			Composite bottomButtonComposite = new Composite(container, SWT.NONE);
			bottomButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			bottomButtonComposite.setLayout(new GridLayout(2, false));

			{
				editParameterButton = new Button(bottomButtonComposite, SWT.NONE);
				GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
				editParameterButton.setLayoutData(gridData);
				editParameterButton.setText(ReportI18N.UserReportEditor_EditReportParameterButtonText);
				editParameterButton.setToolTipText(ReportI18N.UserReportEditor_EditReportParameterButtonToolTip);
				editParameterButton.setImage(IconRegistry.getImage(IImageKeys.EDIT_REPORT_PARAMETER));
				editParameterButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						openWizard();
					}
				});
				ModifyListener modifyListener_editParameterButton = new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						final BaseReportCVO baseReportCVO = baseReportCombo.getEntity();
						final boolean enable = baseReportCVO != null && baseReportCVO.getPK() != null;
						editParameterButton.setEnabled(enable);
					}
				};
				baseReportCombo.addModifyListener(modifyListener_editParameterButton);
			}
			{
				generateReportButton = new Button(bottomButtonComposite, SWT.NONE);
				generateReportButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
				generateReportButton.setText(ReportI18N.UserReportEditor_GenerateReportButtonText);
				generateReportButton.setToolTipText(ReportI18N.UserReportEditor_GenerateReportButtonToolTip);
				generateReportButton.setImage(IconRegistry.getImage(IImageKeys.GENERATE_REPORT));
				generateReportButton.setEnabled(isComplete());
				generateReportButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (template != null && template.getContent() != null) {
							GenerateReportAction.generateReport(getParameter(), template);
						}
						else {
							GenerateReportAction.generateReport(getParameter(), null);
						}
					}
				});
			}

			// sync widgets and groups to the entity
			setEntity(userReportVO);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);

			SWTHelper.refreshSuperiorScrollbar(container);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}




	protected void setEntity(UserReportVO userReportVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		userReportVO = userReportVO.clone();
		}
		this.userReportVO = userReportVO;

		syncWidgetsToEntity();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/* Entity mit den Widgets synchronisieren.
			 * Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			/*
			 * Gespeichert wird über eine Model-Methode. Wenn keine ID vorhanden ist, handelt es sich um ein neues
			 * Entity und es wird über create() ein neues Entity erzeugt. Andernfalls wird das bestehende über update()
			 * aktualisiert.
			 */
			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				userReportVO = userReportListModel.create(userReportVO, template);

				// observe the Model
				userReportListModel.addListener(this, userReportVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(userReportVO.getID());

				// set new entity
				setEntity(userReportVO);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				userReportListModel.updateEditorData(userReportVO, template);

				// setEntity will be called indirectly in dataChange()
			}

			// spezielle Vorlage "entladen", da diese jetzt hochgeladen ist
			template = null;

			monitor.worked(1);
		}
		catch (DirtyWriteException e) {
			// DirtyWriteException werden gesondert behandelt um eine aussagekräftige Fehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(
				Activator.PLUGIN_ID,
				getClass().getName(),
				e,
				ReportI18N.UpdateUserReportDirtyWriteMessage
			);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = ReportI18N.CreateUserReportErrorMessage;
			}
			else {
				msg = ReportI18N.UpdateUserReportErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private boolean initTemplate() throws Exception {
		if (isDirty()) {
			// Ask user to save the editor
			getSite().getWorkbenchWindow().getActivePage().saveEditor(this, true);
			if (isDirty()) {
				return false;
			}
		}

		userReportListModel.initTemplate(userReportVO.getID());
		return true;
	}


	/**
	 * Opens this userReport's template in separate OpenOfficeEditor. Checks first whether the preferences contain an
	 * entry for the installation path.
	 * <p>
	 * Is called when the user presses the "Vorlage bearbeiten" button.
	 */
	protected void editTemplate() {
		// Check if openoffice-path is configured or can be found out
		if ( !FileHelper.isValidOpenOfficePath() ) {
			MessageDialog.openWarning(
				getSite().getShell(),
				UtilI18N.Warning,
				de.regasus.core.ui.CoreI18N.OpenOfficePreference_NoProperPathConfiguration);
			return;
		}

		try {
			/* If the UserReport has no individual template so far,
			 * initialize the individual template with the default template.
			 */
			if (userReportVO.getTemplateID() == null) {
				boolean ok = initTemplate();
				if (!ok) {
					return;
				}
			}

			DataStoreVO templateDataStoreVO = getDataStoreMgr().getDataStoreVO(
				userReportVO.getTemplateID(),
				true // withContent
			);

			// The unknown content lies on the server, so no local template available
			template = null;

			OpenOfficeEditorInput editorInput = new OpenOfficeEditorInput(templateDataStoreVO);
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
				editorInput,
				OpenOfficeEditor.ID
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(
				Activator.PLUGIN_ID,
				getClass().getName(),
				e,
				ReportI18N.TemplateDownloadAction_TemplateCouldNotBeDownloaded
			);
		}
	}


	protected void saveTemplate() {
		try {
			DocumentContainer documentContainer = null;
			if (template != null && template.getContent() != null) {
				// take template
				documentContainer = template;
			}
			else {
				// get template from server
				documentContainer = getReportMgr().getTemplate(userReportVO.getID());
			}


    		// Dialog to save a file
			final FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
			dialog.setText(ReportI18N.TemplateDownloadAction_SaveTemplateFileDialog_Title);
			dialog.setFilterExtensions(new String[] {"*.ods"});
			// init Dialog with path and file from extFilePath
			if (userReportVO != null) {
				if (userReportVO.getExtFilePath() != null) {
					File file = new File(userReportVO.getExtFilePath());
					dialog.setFilterPath(file.getParent());
					dialog.setFileName(file.getName());
				}
				else if (userReportVO.getName() != null) {
					String fName = userReportVO.getName().getString();
					fName += ".";
					fName += documentContainer.getFilePostfix();

					dialog.setFileName(fName);
				}
			}

			final String[] fileName = new String[1];
    		SWTHelper.syncExecDisplayThread(new Runnable() {
    			@Override
				public void run() {
    				// open Dialog
    				fileName[0] = dialog.open();
    			}
    		});


    		// if user selected a file in the Dialog
    		if (fileName[0] != null && documentContainer != null) {
    			final byte[] content = documentContainer.getContent();

				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						try {
			        		// save File
		        			File file = new File(fileName[0]);
							FileHelper.writeFile(file, content);
			        	}
		        		catch (IOException e) {
		    				RegasusErrorHandler.handleApplicationError(
		    					Activator.PLUGIN_ID,
		    					getClass().getName(),
		    					e,
		    					ReportI18N.TemplateDownloadAction_FileCouldNotBeSaved
		    				);
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}

				});
    		}
		}
		catch (Exception e) {
    		RegasusErrorHandler.handleApplicationError(
    			Activator.PLUGIN_ID,
    			getClass().getName(),
    			e,
    			ReportI18N.TemplateDownloadAction_TemplateCouldNotBeDownloaded
    		);
		}
	}


	private void syncWidgetsToEntity() {
		if (userReportVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try{
						baseReportCombo.setPK(userReportVO.getBaseReportID());
						i18nMultiText.setLanguageString(ReportLabel.userReport_name.getString(), userReportVO.getName());
						i18nMultiText.setLanguageString(ReportLabel.userReport_description.getString(), userReportVO.getDescription());

						String extFilePath = userReportVO.getExtFilePath();
						if (extFilePath == null) {
							if (userReportVO.getTemplateID() == null) {
								extFilePath = KernelLabel.DefaultTemplate.getString();
							}
							else {
								extFilePath = "";
							}
						}
						templatePathText.setText(extFilePath);

						setXmlRequestToWidgets(userReportVO);

						if (!isNew()) {
							// sync available formats to rewrite old available formats defined in xml parameters
							syncAvailableFormats();
						}

						setComplete(userReportVO.isComplete());

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						setDirty(false);

						editButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void setXmlRequestToWidgets(UserReportVO userReportVO) {
		// set XML source
		parameterText.setText(StringHelper.avoidNull(userReportVO.getXMLRequestSource()));

		// extract and set human readable parameters
		try {
			XMLContainer xmlContainer = userReportVO.getXMLRequest();
			DefaultReportParameter reportParameter = new DefaultReportParameter(xmlContainer);
			parameterDescriptionText.setText(reportParameter.getDescription());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
			parameterDescriptionText.setText( e.getMessage() );
		}
	}


	private void setXmlRequestToWidgets(XMLContainer xmlContainer) {
		// set XML source
		String xmlSource = null;
		if (xmlContainer != null) {
			try {
				xmlSource = xmlContainer.getPrettySource();
			}
			catch (JDOMException e) {
				xmlSource = xmlContainer.getRawSource();
			}
		}
		parameterText.setText(StringHelper.avoidNull(xmlSource));

		// extract and set human readable parameters
		DefaultReportParameter reportParameter = new DefaultReportParameter(xmlContainer);
		parameterDescriptionText.setText(reportParameter.getDescription());
	}


	private void syncEntityToWidgets() {
		if (userReportVO != null) {
			userReportVO.setBaseReportID(baseReportCombo.getPK());
			userReportVO.setName(i18nMultiText.getLanguageString(ReportLabel.userReport_name.getString()));
			userReportVO.setDescription(i18nMultiText.getLanguageString(ReportLabel.userReport_description.getString()));

			String extFilePath = templatePathText.getText();
			if (extFilePath.equals(KernelLabel.DefaultTemplate.getString())) {
				extFilePath = null;
			}
			userReportVO.setExtFilePath(extFilePath);

			userReportVO.setXMLRequestSource(parameterText.getText());

			try {
				DefaultReportParameter reportParameter = new DefaultReportParameter(userReportVO.getXMLRequest());
				userReportVO.setComplete(reportParameter.isComplete());
			}
			catch (Exception e) {
				I18NPattern msg = new I18NPattern();
				msg.add(ReportI18N.UserReportEditor_InvalidXMLRequest);
				RegasusErrorHandler.handleUserError(Activator.PLUGIN_ID, getClass().getName(), e, msg);
			}

			userReportVO.setComplete(isComplete());
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		baseReportCombo.addModifyListener(listener);
		i18nMultiText.addModifyListener(listener);
		templatePathText.addModifyListener(listener);
		parameterText.addModifyListener(listener);
	}


	@Override
	public void modifyText(ModifyEvent event) {
		super.modifyText(event);
		editButtonStates();
	}


	private void editButtonStates() {
		if (editTemplateButton != null) {
			editTemplateButton.setEnabled(!isDirty());
		}
	}


	protected void refreshTemplate() throws Exception {
		String extFilePath = userReportVO.getExtFilePath();

		if (StringHelper.isNotEmpty(extFilePath)) {
			File file = new File(extFilePath);
			if (file.exists()) {

				boolean shouldRefresh = false;

				if (refreshDontAsk) {
					shouldRefresh = true;
				}
				else {
					MessageDialogWithToggle dialogWithToggle = MessageDialogWithToggle.openOkCancelConfirm(
						getSite().getShell(),
						UtilI18N.Hint,
						ReportI18N.UserReportEditor_RefreshTemplateButtonToolTip,
						UtilI18N.DontShowThisConfirmDialogAgain,
						false,
						null,
						null
					);

					shouldRefresh = (Window.OK == dialogWithToggle.getReturnCode());
					refreshDontAsk = dialogWithToggle.getToggleState();
				}

				if (shouldRefresh) {
					// read file
					byte[] content = FileHelper.readFile(file);
					template = new DocumentContainer(content, file.getName());
					setDirty(true);
					userReportListModel.updateEditorData(userReportVO, template);
				}
			}
			else {
				loadTemplate();
			}
		}
	}


	/**
	 * Opens a dialog, so the user can select an OpenOffice-file, which then is used as template for the current record
	 * definition.
	 * <p>
	 * Is called when the user presses the "Vorlage laden" button.
	 */
	private void loadTemplate() {
		// Dialog zum Öffnen einer Datei

		// Titel aus ResourceBundle dieses Plug-ins laden
		String title = ReportI18N.UserReportEditor_OpenTemplateFileDialog_Title;

		FileDialog dialog = new FileDialog(getEditorSite().getShell(), SWT.OPEN);
		dialog.setText(title);

		// avoid space characters! (MIRCP-2897 - Use portable file extension filter for multiple extensions)
		String[] extensions = {"*.ods;*.odt;*.odg", "*.ods", "*.odt", "*.odg"};
		dialog.setFilterExtensions(extensions);

		// Dialog mit Pfad und Datei aus ExtFilePath initialisieren
		if (userReportVO.getExtFilePath() != null) {
			File file = new File(userReportVO.getExtFilePath());
			dialog.setFilterPath(file.getParent());
			dialog.setFileName(file.getName());
		}

		// Dialog öffnen
		String fileName = dialog.open();

		// Wenn Nutzer im Dialog eine Datei ausgewählt hat
		if (fileName != null) {
			// Datei öffnen
			File file = new File(fileName);
			template = null;
			try {
				// Datei einlesen
				byte[] content = FileHelper.readFile(file);
				template = new DocumentContainer(content, file.getName());

				// Pfad zur Vorlagendatei im Widget speichern
				templatePathText.setText(fileName);

				// verfügbare Formate in ReportParametern aktualisieren
				syncAvailableFormats();
			}
			catch (Exception e) {
				String message = ReportI18N.UserReportEditor_FileCouldNotBeLoaded;
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, message);
			}
		}
	}


	/**
	 * Refresh available formats in report parameters.
	 */
	private void syncAvailableFormats() {
		try {
			// determine DocumentFormat of template
			DocumentFormat documentFormat = null;
			if (template != null && template.getContent() != null && template.getFilePostfix() != null) {
				/*
				 * If an individual template exists, determine DocumentFormat from file extension
				 * of this template.
				 */
				String fileExtension = template.getFilePostfix();
				documentFormat = OpenOfficeHelper.getDocumentFormatByFileExtension(fileExtension);
			}
			else if (userReportVO.getTemplateID() != null) {
				/*
				 * If an individual template exists but is lot loaded, extract DocumentFormat from
				 * file extension of userReportVO.getExtFilePath().
				 */
				String fileName = userReportVO.getExtFilePath();
				String fileExtension = FileHelper.getExtension(fileName);

				documentFormat = OpenOfficeHelper.getDocumentFormatByFileExtension(fileExtension);
			}
			else {
				/*
				 * If no individual template exists, get DocumentFormat from BaseReport.
				 */
				BaseReportCVO baseReportCVO = baseReportCombo.getEntity();
				if (baseReportCVO != null) {
 					String formatKey = baseReportCVO.getBaseReportVO().getTemplateFormat();
 					documentFormat = OpenOfficeHelper.getDocumentFormatByFormatKey(formatKey);
				}
			}

			// determine possible target formats for template
			List<String> availableFormatKeys = new ArrayList<>();
			if (documentFormat != null) {
				// get available extensions for the extension of the template document
				String propKey = "convertFrom." + documentFormat.getExtension();
				String availableExtensions = PropertyModel.getInstance().getPropertyValue(propKey);
				if ( StringHelper.isEmpty(availableExtensions) ) {
					// at least the original extension of the template is available
					availableExtensions = documentFormat.getExtension();
				}

				List<String> availableExtensionKeys = Arrays.asList(availableExtensions.split(","));
				if ( CollectionsHelper.notEmpty(availableExtensionKeys)) {
					for (String extension : availableExtensionKeys) {
						DocumentFormat format = OpenOfficeHelper.getDocumentFormatByFileExtension(extension);
						if (format != null) {
							availableFormatKeys.add(format.getFormatKey());
						}
					}
				}
			}

			// store possible target formats in report parameters (XMLRequest) in widget
			final XMLContainer xmlRequest = new XMLContainer(parameterText.getText());
			FormatReportParameter formatReportParameter = new FormatReportParameter(xmlRequest);
			formatReportParameter.setAvailableFormats(availableFormatKeys);

			// delete current format if it is not one of the possible target formats
			final String currentFormatKey = formatReportParameter.getFormat();
			if (!availableFormatKeys.contains(currentFormatKey)) {
				formatReportParameter.setFormat(null);
				formatReportParameter.setDescription(IFormatReportParameter.DESCRIPTION_ID, null);
				setComplete(false);
			}

			setXmlRequestToWidgets(xmlRequest);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void deleteTemplate() {
		try {
			// Datei löschen
			template = new DocumentContainer(null, null);

			// Pfad zur Vorlagendatei im Widget speichern
			templatePathText.setText(KernelLabel.DefaultTemplate.getString());

			// verfügbare Formate in ReportParametern aktualisieren
			syncAvailableFormats();
		}
		catch (Exception e) {
			String message = ReportI18N.UserReportEditor_FileCouldNotBeLoaded;
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, message);
		}
	}


	private XMLContainer getParameter() {
		XMLContainer xmlRequest = new XMLContainer(parameterText.getText());
		return xmlRequest;
	}


	protected void openWizard() {
		// use the currently selected Base Report, not the one of the Report Parameters
		BaseReportCVO baseReportCVO = baseReportCombo.getEntity();
		if (baseReportCVO != null) {
			String wizardClassName = baseReportCVO.getBaseReportVO().getJFaceWizard();
			IReportWizard wizard = null;

			try {
				wizard = ReportWizardFactory.getReportWizardInstance(wizardClassName);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			if (wizard == null) {
				System.err.println("Instantiation of " + wizardClassName + " faild. Creating DefaultReportWizard instead.");
				wizard = new DefaultReportWizard();
			}

			try {
				/*
				 * Wizard initialisieren. Der reportName und XML-Request müssen den Widgets entnommen werden, weil sie
				 * durch den Nutzer im Editor verändert worden sein können. Diese Änderungen werden erst mit dem
				 * Speichern nach userReportVO kopiert. Der Assistent soll aber die Werte aus dem Editor erhalten.
				 *
				 * userReportVO.getXMLRequest() liefert hingegen immer den Original-XMLRequest.
				 */
				final BaseReportVO baseReportVO = baseReportCVO.getBaseReportVO();
				LanguageString name = i18nMultiText.getLanguageString(ReportLabel.userReport_name.getString());
				String reportName;
				if (name != null) {
					reportName = StringHelper.trim(name.getString());
				}
				else {
					reportName = baseReportVO.getName().getString();
				}

				XMLContainer xmlRequest = getParameter();
				wizard.initialize(baseReportCVO.getBaseReportVO(), reportName, xmlRequest);

				ReportWizardDialog wizardDialog = new ReportWizardDialog(getSite().getShell(), wizard, template);

				wizardDialog.create();
				int returnCode = wizardDialog.open();

				if (returnCode == Window.OK) {
					/*
					 * Der Wizard schreibt seine Änderunge direkt in den bei initialize() übergebenen XMLContainer.
					 * Daher brauchen die Reportparameter nicht angefordert werden. Folgende Code-Zeile ist daher
					 * überflüssig: xmlRequest = wizard.getReportParameter().getXMLRequest();
					 */
					parameterText.setText(xmlRequest.getPrettySource());

					// XML-Request-Description auslesen und anzeigen
					try {
						DefaultReportParameter reportParameter = new DefaultReportParameter(xmlRequest);
						parameterDescriptionText.setText(reportParameter.getDescription());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleWarnError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

					setComplete(true);
				}
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}

		}
	}


	private boolean isComplete() {
		return complete;
	}


	private void setComplete(boolean complete) {
		this.complete = complete;
		generateReportButton.setEnabled(userReportVO != null && userReportVO.getID() != null && isComplete());
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		ArrayList<String> labels = new ArrayList<>();
		labels.add(UtilI18N.ID);
		labels.add(ReportI18N.UserReportEditor_InfoLabelTemplateID);
		labels.add(ReportI18N.UserReportEditor_InfoLabelDirectoryID);
		labels.add(ReportLabel.userReport_name.getString());
		labels.add(ReportLabel.userReport_baseReport.getString());
		labels.add(UtilI18N.CreateDateTime);
		labels.add(UtilI18N.CreateUser);
		labels.add(UtilI18N.EditDateTime);
		labels.add(UtilI18N.EditUser);


		// get name of report type
		String baseReportName;
		try {
			baseReportName = baseReportCombo.getEntity().getBaseReportVO().getName().getString();
		}
		catch (Exception e) {
			baseReportName = "";
		}

		// the values of the info dialog
		ArrayList<String> values = new ArrayList<>();
		values.add(StringHelper.avoidNull(userReportVO.getID()));
		values.add(StringHelper.avoidNull(userReportVO.getTemplateID()));
		values.add(StringHelper.avoidNull(userReportVO.getUserReportDirID()));

		LanguageString name = i18nMultiText.getLanguageString(ReportLabel.userReport_name.getString());
		if (name != null){
			values.add( name.getString() );
		}

		values.add(baseReportName);
		values.add(formatHelper.formatDateTime(userReportVO.getNewTime()));
		values.add(userReportVO.getNewDisplayUserStr());
		values.add(formatHelper.formatDateTime(userReportVO.getEditTime()));
		values.add(userReportVO.getEditDisplayUserStr());


		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ReportLabel.userReport.getString() + ": " + UtilI18N.Info,
			labels.toArray(new String[]{}),
			values.toArray(new String[]{})
		);
		infoDialog.open();
	}


	@Override
	protected String getName() {
		String name = null;
		if (userReportVO != null && userReportVO.getName() != null) {
			name = userReportVO.getName().getString();
		}
		if (StringHelper.isEmpty(name)) {
			name = ReportI18N.UserReportEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTipText = null;
		if (userReportVO != null && userReportVO.getDescription() != null) {
			toolTipText = userReportVO.getName().getString();
		}
		if (StringHelper.isEmpty(toolTipText)) {
			toolTipText = ReportI18N.UserReportEditor_DefaultToolTip;
		}
		return toolTipText;
	}


	@Override
	public boolean isNew() {
		return userReportVO.getID() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (userReportVO != null && userReportVO.getID() != null) {
			userReportListModel.refresh(userReportVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				userReportVO = userReportListModel.getUserReportVO(userReportVO.getPK());
				if (userReportVO != null) {
					setEntity(userReportVO);
				}
			}
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == userReportListModel) {
				if (event.getOperation() == CacheModelOperation.REFRESH ||
					event.getOperation() == CacheModelOperation.UPDATE
				) {
					if (userReportVO != null) {
						userReportVO = userReportListModel.getUserReportVO(userReportVO.getPK());
						if (userReportVO != null) {
							setEntity(userReportVO);
						}
						else if (ServerModel.getInstance().isLoggedIn()) {
							closeBecauseDeletion();
						}
					}
				}
				else if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
