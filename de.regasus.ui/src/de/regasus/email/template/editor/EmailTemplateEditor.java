package de.regasus.email.template.editor;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.EventConfigParameterSet;
import com.lambdalogic.messeinfo.contact.data.SimplePersonSearchData;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailScriptContextBuilder;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateEvaluationType;
import com.lambdalogic.messeinfo.email.interfaces.IEmailTemplateManager;
import com.lambdalogic.messeinfo.exception.WarnMessageException;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.report.script.ScriptContext;
import com.lambdalogic.report.script.ScriptHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.ImagePathHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.dnd.TextDropListener;
import com.lambdalogic.util.rcp.html.BrowserFactory;
import com.lambdalogic.util.rcp.html.LazyHtmlEditor;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.common.Language;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.email.EmailI18N;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.SampleRecipientListener;
import de.regasus.email.template.SampleRecipientModel;
import de.regasus.email.template.variables.view.EmailVariablesView;
import de.regasus.onlineform.RegistrationFormConfigModel;


public class EmailTemplateEditor
extends AbstractEditor<EmailTemplateEditorInput>
implements IRefreshableEditorPart, SampleRecipientListener, CacheModelListener<Long> {

	public static final String ID = "EmailTemplateEditor";


	/**
	 * The entity that this Editor shows for editing.
	 */
	private EmailTemplate emailTemplate;

	/**
	 * The context that translates the variables according to the current sample participant or person and according to
	 * the {@link EmailTemplate}'s language.
	 */
	private ScriptContext scriptContext;

	private List<ContextChangedListener> contextChangedListenerList = new ArrayList<>();


	// Models
	private EmailTemplateModel emailTemplateModel;
	private ConfigParameterSetModel configParameterSetModel;
	private RegistrationFormConfigModel regFormConfigModel;



	// Tabs
	private TabFolder tabFolder;
	private TabItem htmlTabItem;
	private TabItem textPreviewTabItem;
	private TabItem htmlPreviewTabItem;


	// **************************************************************************
	// * Widgets within Tabs
	// *

	/**
	 * A group that allows editing of the email headers From, To, Cc and so on.
	 */

	private DefinitionGroup definitionGroup;
	private AddressesGroup addressesGroup;
	private EmailTemplateSystemRoleGroup emailTemplateSystemRoleGroup;
	private RegistrationFormConfigGroup registrationFormConfigGroup;

	private AttachmentComposite attachmentsComposite;

	private Text scriptText;

	// widget to show HTML htmlMessage if evaluationType == EmailTemplateEvaluationType.Groovy
	private LazyHtmlEditor htmlEditor;
	// widget to show HTML htmlMessage if evaluationType == EmailTemplateEvaluationType.Template
	private Text htmlSourceText;

	private Browser htmlPreviewBrowser;

	private Text textMessageText;

	private Composite textPreviewComposite;
	private AddressesGroup previewAddressesGroup;
	private Text textPreviewMessageText;

	private Long eventId;



	@Override
	protected String getName() {
		if (isNew()) {
			return EmailI18N.NewEmailTemplate;
		}
		else {
			return emailTemplate.getName();
		}
	}


	@Override
	protected String getToolTipText() {
		return EmailLabel.EmailTemplate.getString();
	}


	@Override
	public void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();
		eventId = editorInput.getEventPK();


		// get models
		emailTemplateModel = EmailTemplateModel.getInstance();
		configParameterSetModel = ConfigParameterSetModel.getInstance();
		regFormConfigModel = RegistrationFormConfigModel.getInstance();

		// get entity
		if (key != null) {
			// register at model for this particular entity
			emailTemplateModel.addListener(this, key);

			emailTemplate = emailTemplateModel.getEmailTemplate(key);
			// clone data to avoid impact to cache if save operation fails
			emailTemplate = emailTemplate.clone();
		}
		else {
			emailTemplate = new EmailTemplate();
			emailTemplate.setEventPK(eventId);
		}


		SampleRecipientModel.INSTANCE.addSampleRecipientListener(this);

		// create ScriptContext with ClassLoader from plug-in de.regasus.ejb.intf
		scriptContext = new EmailScriptContextBuilder()
			.contextClass( getClass() )
			.classLoader( IEmailTemplateManager.class.getClassLoader() )
			.emailTemplate(emailTemplate)
//			.variables(variables)
			.build();


		// Makes that the script context has the proper formatters
		setLanguageCode(emailTemplate.getLanguage());
	}


	@Override
	public void dispose() {
		// When the editor is closed, it doesn't need to get notified by the model anymore.
		if (emailTemplate != null && emailTemplate.getID() != null) {
			emailTemplateModel.removeListener(this, emailTemplate.getID());
		}

		SampleRecipientModel.INSTANCE.removeSampleRecipientListener(this);

		super.dispose();
	}


	@Override
	protected String getTypeName() {
		return EmailLabel.EmailTemplate.getString();
	}


	/**
	 * Create contents of the editor part, which consists of a folder for Definition and Preview
	 *
	 * @param parent
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			tabFolder = new TabFolder(parent, SWT.NONE);

			createGeneralTab();
			createAttachementsTab();
			createScriptTab();

//			if ( ServerModel.getInstance().isAdmin() ) {
//				createHtmlSourceTab();
//			}

			createHtmlEditorTab();
			createHtmlPreviewTab();
			createTextEditorTab();
			createTextPreviewTab();


			// set data
			setEntity(emailTemplate);


			// after sync add this as ModifyListener to all widgets and groups
			definitionGroup.addModifyListener(this);
			addressesGroup.addModifyListener(this);
			emailTemplateSystemRoleGroup.addModifyListener(this);
			if (registrationFormConfigGroup != null) {
				registrationFormConfigGroup.addModifyListener(this);
			}

			attachmentsComposite.addModifyListener(this);

			textMessageText.addModifyListener(this);

			initDragAndDrop();

			// When the user clicks on one of the preview tabs, the preview gets evaluated
			tabFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						tabItemChanged();
					}
					catch (Exception ex) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
					}
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void initDragAndDrop() {
		// init DragNDrop for plain text
		DropTarget target = new DropTarget(
			textMessageText,
			DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK
		);
		target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		target.addDropListener(new TextDropListener(textMessageText));

		// the HTML editor implements DragNDrop itself
	}


	/**
	 * Create the tab of the editor where the user finds
	 * <ul>
	 * <li> {@link DefinitionGroup} for name and language</li>
	 * <li> {@link AddressesGroup} for addresses (to, from etc) and subject</li>
	 * <li> {@link MessageTabFolder} for writing the text and HTML message text</li>
	 * </ul>
	 */
	private void createGeneralTab() throws Exception {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText(UtilI18N.General);

		Composite mainComposite = tabItem.getContentComposite();
		mainComposite.setLayout(new GridLayout(2, true));


		Composite leftComposite = new Composite(mainComposite, SWT.NONE);
		leftComposite.setLayout( new GridLayout() );

		Composite rightComposite = new Composite(mainComposite, SWT.NONE);
		rightComposite.setLayout( new GridLayout() );

		// layout
		{
    		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, true);
    		gridDataFactory.applyTo(leftComposite);
    		gridDataFactory.applyTo(rightComposite);
		}


		ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventId);
		EventConfigParameterSet eventConfigParameterSet = configParameterSet.getEvent();

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		// The group for defining name and language of the EmailTemplate
		{
    		definitionGroup = new DefinitionGroup(leftComposite, SWT.NONE);
    		gridDataFactory.applyTo(definitionGroup);

    		// update the language in ScriptContext if it changes in the Combo
    		definitionGroup.addLanguageModifyListener(new ModifyListener() {
    			@Override
    			public void modifyText(ModifyEvent e) {
    				String language = getLanguage();
    				scriptContext.setLanguage(language);
    			}
    		});
		}

		// The group for defining the addresses and the subject
		{
    		addressesGroup = new AddressesGroup(leftComposite, SWT.NONE);
    		gridDataFactory.applyTo(addressesGroup);
		}



		// EmailTemplateSystemRoleGroup
		{
    		emailTemplateSystemRoleGroup = new EmailTemplateSystemRoleGroup(rightComposite, SWT.NONE, eventId);
    		gridDataFactory.applyTo(emailTemplateSystemRoleGroup);
		}

		// RegistrationFormConfigGroup
		if (eventId != null && eventConfigParameterSet.getFormEditor().isVisible() ) {
			List<RegistrationFormConfig> regFormConfigs = regFormConfigModel.getRegistrationFormConfigsByEventPK(eventId);

			if ( notEmpty(regFormConfigs) ) {
				registrationFormConfigGroup = new RegistrationFormConfigGroup(rightComposite, SWT.NONE, eventId);
				gridDataFactory.applyTo(registrationFormConfigGroup);
			}
		}


		tabItem.refreshScrollbars();
	}


	private void createAttachementsTab() throws Exception {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText( EmailLabel.Attachments.getString() );

		Composite composite = tabItem.getContentComposite();
		composite.setLayout(new FillLayout());

		attachmentsComposite = new AttachmentComposite(composite, SWT.NONE, this);

		tabItem.refreshScrollbars();
	}


	private void createScriptTab() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(EmailLabel.Script.getString());
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabItem.setControl(composite);

		composite.setLayout( new GridLayout(1, false) );

		Label scriptLabel = new Label(composite, SWT.WRAP);
		scriptLabel.setText(EmailLabel.ScriptDescription.getString());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(scriptLabel);

		scriptText = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scriptText);
		scriptText.addModifyListener(this);
	}


	private ModifyListener evaluationTypeModifyListener = new ModifyListener() {
		private EmailTemplateEvaluationType previousEvaluationType;

		@Override
		public void modifyText(ModifyEvent e) {
			EmailTemplateEvaluationType selectedEvaluationType = definitionGroup.getEmailTemplateEvaluationType();
			if (selectedEvaluationType != previousEvaluationType) {
				previousEvaluationType = selectedEvaluationType;

				syncExecInParentDisplay(new Runnable() {
					@Override
					public void run() {
						createHtmlEditorTab();
					}
				});

			}
		}
	};


	private void createHtmlEditorTab() {
		if (htmlTabItem == null) {
    		htmlTabItem = new TabItem(tabFolder, SWT.NONE);
    		htmlTabItem.setText(EmailLabel.HTML.getString());

    		definitionGroup.addModifyListener(evaluationTypeModifyListener);
		}


		EmailTemplateEvaluationType selectedEvaluationType = definitionGroup.getEmailTemplateEvaluationType();
		if (selectedEvaluationType == EmailTemplateEvaluationType.Groovy) {
			// destroy htmlSourceText and save its content
			String content = destroyHtmlSourceEditor();

			// build LazyHtmlEditor and restore content
			buildHtmlWysiwygEditor(content);
		}
		else if (selectedEvaluationType == EmailTemplateEvaluationType.Template) {
			// destroy htmlEditor and save its content
			String content = destroyHtmlWysiwygEditor();

			// build Text for Groovy template and restore content
			buildHtmlSourceEditor(content);
		}
	}


	private void buildHtmlWysiwygEditor(String content) {
		if (htmlEditor == null) {
			// build LazyHtmlEditor
			htmlEditor = new LazyHtmlEditor(tabFolder, SWT.NONE);
			htmlTabItem.setControl(htmlEditor);

    		htmlEditor.addModifyListener(this);

    		if (content != null) {
    			htmlEditor.setHtml(content);
    		}
		}
	}


	/**
	 * Destroy {@link #htmlEditor} and return its content.
	 */
	private String destroyHtmlWysiwygEditor() {
		String content = null;
		if (htmlEditor != null) {
			content = htmlEditor.getHtml();

			htmlEditor.removeModifyListener(this);
			htmlEditor.dispose();
			htmlEditor = null;
		}
		return content;
	}


	private void buildHtmlSourceEditor(String content) {
		if (htmlSourceText == null) {
    		// build text editor
    		htmlSourceText = new Text(tabFolder, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
    		htmlTabItem.setControl(htmlSourceText);
    		htmlSourceText.addModifyListener(this);

    		// restore content
    		if (content != null) {
    			htmlSourceText.setText(content);
    		}
		}
	}


	/**
	 * Destroy {@link #htmlSourceText} and return its content.
	 * @return
	 */
	private String destroyHtmlSourceEditor() {
		String content = null;
		if (htmlSourceText != null) {
			content = htmlSourceText.getText();

    		htmlSourceText.removeModifyListener(this);
    		htmlSourceText.dispose();
    		htmlSourceText = null;
		}
		return content;
	}


	private String getHtml() {
		String html = "";
		if (htmlEditor != null) {
			html = htmlEditor.getHtml();
		}
		else if (htmlSourceText != null) {
			html = htmlSourceText.getText();
		}

		return html;
	}


	private void setHtml(String html) {
		if (htmlEditor != null) {
			htmlEditor.setHtml(html);
		}
		else if (htmlSourceText != null) {
			htmlSourceText.setText( avoidNull(html) );
		}
	}


	private void createHtmlPreviewTab() {
		htmlPreviewTabItem = new TabItem(tabFolder, SWT.NONE);
		htmlPreviewTabItem.setText(EmailLabel.PreviewHtml.getString());

		htmlPreviewBrowser = BrowserFactory.createBrowser(tabFolder, SWT.BORDER);
		htmlPreviewTabItem.setControl(htmlPreviewBrowser);
		// no modify listener, since only for preview
	}


	private void createTextEditorTab() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(EmailLabel.Text.getString());

		textMessageText = new Text(tabFolder, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		tabItem.setControl(textMessageText);
	}


	private void createTextPreviewTab() {
		textPreviewTabItem = new TabItem(tabFolder, SWT.NONE);
		textPreviewTabItem.setText(EmailLabel.PreviewText.getString());

		textPreviewComposite = new Composite(tabFolder, SWT.NONE);
		textPreviewTabItem.setControl(textPreviewComposite);

		textPreviewComposite.setLayout( new GridLayout(1, false) );

		previewAddressesGroup = new AddressesGroup(textPreviewComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(previewAddressesGroup);
		previewAddressesGroup.setEditable(false);

		textPreviewMessageText = new Text(textPreviewComposite, SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(textPreviewMessageText);
		textPreviewMessageText.setEditable(false);
	}


	/**
	 * Set the HTML editor's value of sourceCodeModeOnly according to the currently selected EmailTemplateEvaluationType.
	 */
//	private void syncHtmlEditorsSourceCodeOnlyMode() {
//		syncExecInParentDisplay(new Runnable() {
//			@Override
//			public void run() {
//				EmailTemplateEvaluationType type = definitionGroup.getEmailTemplateEvaluationType();
//				boolean sourceCodeModeOnly = htmlEditor.isSourceCodeModeOnly();
//
//				if (type != null && type.isAllowsHtmlWysiwyg() == sourceCodeModeOnly) {
//					htmlEditor.setSourceCodeModeOnly(!sourceCodeModeOnly);
//				}
//			}
//		});
//	}


	@Override
	public void doSave(IProgressMonitor monitor) {

		boolean create = isNew();
		try {
			int stepsToWork = 2; // Syncing, Saving
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, stepsToWork);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);


			if (create) {
				// Save new entity. On success we get the updated entity, else an Exception will be thrown.
				emailTemplate = emailTemplateModel.create(emailTemplate);

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(emailTemplate.getID());

				emailTemplateModel.addListener(this, emailTemplate.getID());

				// set new entity
				setEntity(emailTemplate);
			}
			else {
				/* Save the entity.
				 * On success setEntity() will be called indirectly in dataChange(), else an
				 * Exception will be thrown.
				 * The result of update() must not be assigned (to the entity), because this will
				 * happen in setEntity() and there it may be cloned!
				 * Assigning the entity here would overwrite the cloned value with the one from
				 * the model. Therefore we would have inconsistent data!
				 */
				emailTemplateModel.update(emailTemplate);
			}

			monitor.worked(1);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {

		syncExecInParentDisplay(new Runnable() {
			@Override
			public void run() {
				try {
					definitionGroup.syncWidgetsToEntity(emailTemplate);
					addressesGroup.setEmailTemplate(emailTemplate);
					emailTemplateSystemRoleGroup.setEmailTemplate(emailTemplate);
					if (registrationFormConfigGroup != null) {
						registrationFormConfigGroup.setEmailTemplate(emailTemplate);
					}
					textMessageText.setText( avoidNull(emailTemplate.getTextMessage()) );
					scriptText.setText( avoidNull(emailTemplate.getScript()) );

					List<File> imageFileList = attachmentsComposite.setEmailTemplate(emailTemplate);


					// set HTML content
					String htmlMessage = emailTemplate.getHtmlMessage();
					htmlMessage = avoidNull(htmlMessage);


					if ( ! isEmpty(htmlMessage) ) {
						if (imageFileList.size() > 0) {
							File sampleFile = imageFileList.get(0);
							File commonDirectory = sampleFile.getParentFile();
							String baseURI = FileHelper.convertAbsolutePathToFileSchemeURI(commonDirectory.getAbsolutePath()) + "/";
							htmlMessage = ImagePathHelper.replaceLocalPathesWithFileURIs(baseURI, htmlMessage);
						}
					}


					createHtmlEditorTab();
					setHtml(htmlMessage);


					scriptContext.setLanguage( emailTemplate.getLanguage() );

					TabItem selectedTabItem = tabFolder.getSelection()[0];

					if (selectedTabItem == textPreviewTabItem) {
						setupPreview();
					}
					else if (selectedTabItem == htmlPreviewTabItem) {
						setupHtmlPreview();
					}

					// refresh the EditorInput
					editorInput.setName(getName());
					editorInput.setToolTipText(getToolTipText());

					// refresh name of editor
					setPartName(getName());
					firePropertyChange(PROP_TITLE);

					// Signal that Editor has no more unsaved data
					setDirty(false);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}


	public void syncEntityToWidgets() throws Exception {
		definitionGroup.syncEntityToWidgets(emailTemplate);
		addressesGroup.syncEntityToWidgets(emailTemplate);
		emailTemplateSystemRoleGroup.syncEntityToWidgets();
		if (registrationFormConfigGroup != null) {
			registrationFormConfigGroup.syncEntityToWidgets(emailTemplate);
		}
		emailTemplate.setTextMessage( textMessageText.getText() );
		emailTemplate.setScript( trim(scriptText.getText()) );


		// copy HTML content
		String html = getHtml();

		// get image files with absolute paths
		List<File> imageFileList = ImagePathHelper.findImageFiles(html);

		// replace absolute paths with relative paths
		html = ImagePathHelper.replaceAbsoluteFileURIsWithRelative(html);

		html = trim(html);
		emailTemplate.setHtmlMessage(html);

		// sync attachments
		attachmentsComposite.syncEntityToWidgets(imageFileList);
	}


	@Override
	public boolean isNew() {
		return emailTemplate.getID() == null;
	}


	@Override
	public void changed(Long eventPK, SimplePersonSearchData psd) throws Exception {
		if ( EqualsHelper.isEqual(eventPK, editorInput.getEventPK()) ) {
			if (htmlPreviewTabItem != null && htmlPreviewBrowser.isVisible()) {
				setupHtmlPreview();
			}
			else if (textPreviewTabItem != null && textPreviewMessageText.isVisible()) {
				setupPreview();
			}
		}
	}


	/**
	 * When the user switches to the Preview tab, the current (even possibly unsaved) expresions and text in the
	 * address fields are evaluated and the result is shown here
	 */
	protected void setupPreview() {
		try {
			EmailTemplate previewTemplate = buildPreviewEmailTemplateWithCurrentWidgetContents();

			previewAddressesGroup.setEmailTemplate(previewTemplate);
			textPreviewMessageText.setText(previewTemplate.getTextMessage());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * When the user switches to the Preview tab, the expresions and text in the address fields are evaluated and the
	 * result is shown here
	 */
	protected void setupHtmlPreview() {
		try {
			EmailTemplate previewTemplate = buildPreviewEmailTemplateWithCurrentWidgetContents();

			String htmlMessageWithSurroundingFontDiv = previewTemplate.getHtmlMessage();
			htmlPreviewBrowser.setText(htmlMessageWithSurroundingFontDiv);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private EmailTemplate buildPreviewEmailTemplateWithCurrentWidgetContents() throws Exception {
		loadSampleParticipantOrProfile();

		EmailTemplate previewTemplate = new EmailTemplate();
		definitionGroup.syncEntityToWidgets(previewTemplate);
		addressesGroup.syncEntityToWidgets(previewTemplate);
		previewTemplate.setTextMessage( textMessageText.getText() );

		previewTemplate.setToAddr(      evaluateString(previewTemplate.getToAddr())      );
		previewTemplate.setFromAddr(    evaluateString(previewTemplate.getFromAddr())    );
		previewTemplate.setReplyToAddr( evaluateString(previewTemplate.getReplyToAddr()) );
		previewTemplate.setCcAddr(      evaluateString(previewTemplate.getCcAddr())      );
		previewTemplate.setBccAddr(     evaluateString(previewTemplate.getBccAddr())     );
		previewTemplate.setSubject(     evaluateString(previewTemplate.getSubject())     );

		String textMessage = previewTemplate.getTextMessage();
		String htmlMessage = getHtml();

		switch ( previewTemplate.getEvaluationType() ) {
		case Groovy:
			previewTemplate.setTextMessage( evaluateString(textMessage) );
			if (htmlMessage != null) {
				previewTemplate.setHtmlMessage( evaluateString(htmlMessage) );
			}
			break;
		case Template:
			previewTemplate.setTextMessage( avoidNull(scriptContext.runCachedGroovyTemplate(textMessage)) );
			if (htmlMessage != null) {
				previewTemplate.setHtmlMessage( avoidNull(scriptContext.runCachedGroovyTemplate(htmlMessage)) );
			}
			break;
		}
		return previewTemplate;
	}


	public void loadSampleParticipantOrProfile() throws Exception {
		Long sampleRecipientPK = SampleRecipientModel.INSTANCE.getSampleRecipientPK(editorInput.getEventPK());
		if (sampleRecipientPK != null) {
			EmailScriptContextBuilder.enrichScriptContext(
				scriptContext,
				sampleRecipientPK, // abstractPersonPK
				emailTemplate,
				null               // ImagesAndAttachmentsContainer
			);

			fireContextChanged();
		}
		else {
			throw new WarnMessageException(EmailI18N.MissingSampleParticipant);
		}
	}


	private String evaluateString(String s) {
		// see also: EmailDispatchService.evaluateString(ScriptContext context, EmailTemplate template, String s)

		if (s != null) {
			s = s.replace("${", "&{");
			s = scriptContext.evaluateString(s);
			s = ScriptHelper.removeScripts(s);
		}

		return s;
	}


	/**
	 * Not only sets the given attribute, but also makes that the context uses the proper language specific formatters
	 *
	 * @param newLanguageCode
	 */
	protected void setLanguageCode(String newLanguageCode) {
		String language = scriptContext.getLanguage();
		if (!EqualsHelper.isEqual(language, newLanguageCode)) {
			scriptContext.setLanguage(newLanguageCode);
			fireContextChanged();
		}
	}


	// *************************************************************************
	// * Listener Infrastructure
	// *

	public void addContextChangedListener(ContextChangedListener contextChangedListener) {
		contextChangedListenerList.add(contextChangedListener);

	}


	public void removeContextChangedListener(ContextChangedListener contextChangedListener) {
		contextChangedListenerList.remove(contextChangedListener);

	}


	protected void fireContextChanged() {
		Iterator<ContextChangedListener> iterator = contextChangedListenerList.iterator();
		while (iterator.hasNext()) {
			ContextChangedListener contextChangedListener = iterator.next();
			try {
				contextChangedListener.contextChanged(this);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				iterator.remove();
			}
		}
	}


	// *************************************************************************
	// * Generated Getters
	// *

	public EmailTemplate getEmailTemplate() {
		return emailTemplate;
	}


	/**
	 * The editor's {@link ScriptContext} is also used by the {@link EmailVariablesView} to show the sample values in case a
	 * sample person is chosen.
	 */
	public ScriptContext getContext() {
		return scriptContext;
	}


	/**
	 * The variables view can show sample values when either a sample profile or participant is selected by the
	 * @return
	 */
	public boolean isCanEvaluateContext() {
		return scriptContext.containsVariable("p");
	}


	/**
	 * This method is always called when the user selects a different tab item.
	 */
	private void tabItemChanged() throws Exception {
		if ( isDirty() ) {
			syncEntityToWidgets();
		}

		// If maybe needed, prepare one of the two preview tabs

		TabItem selectedTabItem = tabFolder.getSelection()[0];

		if (selectedTabItem == textPreviewTabItem) {
			setupPreview();
		}
		else if (selectedTabItem == htmlPreviewTabItem) {
			setupHtmlPreview();
		}
	}


	protected boolean shouldShowWysiwygHtmlEditor() {
		if (definitionGroup != null && definitionGroup.getEmailTemplateEvaluationType() != null) {
			return definitionGroup.getEmailTemplateEvaluationType().isAllowsHtmlWysiwyg();
		}
		return false;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.Name,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(emailTemplate.getID()),
			emailTemplate.getName(),
			formatHelper.formatDateTime(emailTemplate.getNewTime()),
			emailTemplate.getNewDisplayUserStr(),
			formatHelper.formatDateTime(emailTemplate.getEditTime()),
			emailTemplate.getEditDisplayUserStr(),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			EmailLabel.EmailTemplate.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event)  {
		try {
			if (event.getSource() == emailTemplateModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (emailTemplate != null ) {
					emailTemplate = emailTemplateModel.getEmailTemplate(emailTemplate.getID());
					setEntity(emailTemplate);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * The complete model might have changed, update to the new model data or even close yourself.
	 * @throws Exception
	 */
	@Override
	public void refresh() throws Exception {
		if (emailTemplate != null && emailTemplate.getID() != null) {
			emailTemplateModel.refresh(emailTemplate.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				emailTemplate = emailTemplateModel.getEmailTemplate(emailTemplate.getID());
				if (emailTemplate != null) {
					setEntity(emailTemplate);
				}
			}
		}
	}


	private String getLanguage() {
		String languageId = null;

		if (definitionGroup != null) {
    		Language language = definitionGroup.getLanguage();
    		if (language != null) {
    			languageId = language.getId();
    		}
		}

		return languageId;
	}


	protected void setEntity(EmailTemplate emailTemplate) {
		if (! isNew()) {
			emailTemplate = emailTemplate.clone();
		}

		this.emailTemplate = emailTemplate;

		syncWidgetsToEntity();
		fireContextChanged();
	}

}
