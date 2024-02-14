package de.regasus.core.ui.editor.config;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.Config;
import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.ConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.EventConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.ParticipantConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.ProfileConfigParameter;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.core.ConfigIdentifier;
import de.regasus.core.ConfigModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;

public class ConfigEditor extends AbstractEditor<ConfigEditorInput> implements IRefreshableEditorPart {

	public static final String ID = "ConfigEditor";

	// the entity
	private Config configEntity;
	private ConfigParameter configParameter;
	private Long adminConfigId;

	private ConfigScope scope;

	// the model
	private ConfigModel configModel;


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;
	private Text xmlText;
	private GeneralConfigComposite generalConfigComposite;
	private FinanceConfigComposite financeConfigComposite;
	private EventConfigComposite eventConfigComposite;
	private ParticipantConfigComposite participantConfigComposite;
	private ProfileConfigComposite profileConfigComposite;

	// *
	// * Widgets
	// **************************************************************************


	private CacheModelListener<Long> configListener = new CacheModelListener<Long>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long	> event) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {

		    		try {
						if (event.getOperation() == CacheModelOperation.DELETE) {
							closeBecauseDeletion();
						}
						else if (configEntity != null) {
							configEntity = configModel.getConfig( configEntity.getId() );
							if (configEntity != null) {
								setEntity(configEntity);
							}
							else if (ServerModel.getInstance().isLoggedIn()) {
								closeBecauseDeletion();
							}
						}
		    		}
		    		catch (Exception e) {
		    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		    		}

				}
			});
    	}
	};


	private CacheModelListener<Long> adminConfigListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long	> event) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {

					try {
						refreshAdminConfig();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

				}
			});
		}
	};


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		ConfigIdentifier configIdentifier = editorInput.getKey();

		scope = configIdentifier.getScope();
		String key = configIdentifier.getKey();

		// get models
		configModel = ConfigModel.getInstance();

		// get entity
		configEntity = configModel.getConfig(scope, key);

		if (configEntity != null) {
			// register at model
			configModel.addListener(configListener, configEntity.getId());


			// determine corresponding admin Config
			Config adminConfig = null;
			if (configEntity.getScope() == ConfigScope.GLOBAL_CUSTOMER) {
				adminConfig = configModel.getConfig(ConfigScope.GLOBAL_ADMIN, null);
			}
			else if (configEntity.getScope() == ConfigScope.EVENT_CUSTOMER) {
				adminConfig = configModel.getConfig(ConfigScope.EVENT_ADMIN, configEntity.getKey());
			}

			if (adminConfig != null) {
				adminConfigId = adminConfig.getId();
				configModel.addListener(adminConfigListener, adminConfigId);
			}
		}
		else {
			throw new ErrorMessageException("Config with scope " + scope + " and key " + key + " not found.");
		}
	}


	@Override
	public void dispose() {
		if (configModel != null && configEntity.getKey() != null) {
			try {
				configModel.removeListener(configListener, configEntity.getId());

				if (adminConfigId != null) {
					configModel.removeListener(adminConfigListener, adminConfigId);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	@Override
	protected String getTypeName() {
		return CoreI18N.Config_Configuration;
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

			// tabFolder
			tabFolder = new TabFolder(parent, SWT.NONE);


			// if Scope is global
			if (   scope == ConfigScope.GLOBAL_ADMIN
				|| scope == ConfigScope.GLOBAL_CUSTOMER
			) {
    			// General Tab
				{
    				LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
        			tabItem.setText(CoreI18N.Config_General);

        			generalConfigComposite = new GeneralConfigComposite(
        				tabItem.getContentComposite(),
        				SWT.NONE,
        				scope
        			);

        			tabItem.refreshScrollbars();
				}

    			// Global Invoice Tab
				{
        			LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
        			tabItem.setText(CoreI18N.Config_Finances);

        			financeConfigComposite = new FinanceConfigComposite(
        				tabItem.getContentComposite(),
        				SWT.NONE,
        				scope
        			);

        			tabItem.refreshScrollbars();
				}

    			// Profile Tab
				{
    				LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
    				tabItem.setText(CoreI18N.Config_Profile);

    				profileConfigComposite = new ProfileConfigComposite(tabItem.getContentComposite(), SWT.NONE);

    				tabItem.refreshScrollbars();
				}
			}


			// Event Tab
			{
    			LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
    			tabItem.setText( ParticipantLabel.Event.getString() );

    			eventConfigComposite = new EventConfigComposite(
    				tabItem.getContentComposite(),
    				SWT.NONE,
    				scope
    			);

    			tabItem.refreshScrollbars();
			}

			// Participant Tab
			{
    			LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
    			tabItem.setText(CoreI18N.Config_Participant);

    			participantConfigComposite = new ParticipantConfigComposite(tabItem.getContentComposite(), SWT.NONE);

    			tabItem.refreshScrollbars();
			}

			// XML Tab
			{
    			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    			tabItem.setText("XML");

    			// xmlText
    			xmlText = new Text(tabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    			xmlText.setEditable(false);

    			tabItem.setControl(xmlText);
			}

			// sync widgets and groups to the entity
			refreshAdminConfig();
			setEntity(configEntity);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void refreshAdminConfig() throws Exception {
		ConfigParameter adminConfigParameter = null;
		if (adminConfigId != null) {
			Config adminConfig = configModel.getConfig(adminConfigId);
			adminConfigParameter = ConfigParameter.createFromXML( adminConfig.getConfigData() );
		}
		else {
			/* If there is no admin Config, use generic ConfigParameter with all values set to true.
			 * This reduces complexity of code, because otherwise a lot of if-statements would be necessary.
			 */
			adminConfigParameter = ConfigParameter.createDefault();
			adminConfigParameter.complete(true);
		}

		if (generalConfigComposite != null) {
			generalConfigComposite.setAdminConfigParameter(adminConfigParameter);
		}

		if (financeConfigComposite != null) {
			financeConfigComposite.setAdminConfigParameter( adminConfigParameter.getInvoiceConfigParameter() );
		}

		if (eventConfigComposite != null) {
			eventConfigComposite.setAdminConfigParameter(adminConfigParameter);
		}

		if (participantConfigComposite != null) {
			participantConfigComposite.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter().getParticipantConfigParameter() );
		}

		if (profileConfigComposite != null) {
			profileConfigComposite.setAdminConfigParameter( adminConfigParameter.getProfileConfigParameter() );
		}
	}


	protected void setEntity(Config config) throws ErrorMessageException {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		config = config.clone();
		}
		this.configEntity = config;
		this.configParameter = ConfigParameter.createFromXML( configEntity.getConfigData() );

		// set entity to other composites

		if (generalConfigComposite != null) {
			generalConfigComposite.setConfigParameter(configParameter);
		}

		if (financeConfigComposite != null) {
			financeConfigComposite.setConfigParameter( configParameter.getInvoiceConfigParameter() );
		}

		if (eventConfigComposite != null) {
			eventConfigComposite.setConfigParameter(configParameter);
		}

		if (participantConfigComposite != null) {
			EventConfigParameter eventConfigParameter = configParameter.getEventConfigParameter();
			ParticipantConfigParameter participantConfigParameter = eventConfigParameter.getParticipantConfigParameter();
			participantConfigComposite.setParticipantConfigParameter(participantConfigParameter);
		}

		if (profileConfigComposite != null) {
			ProfileConfigParameter profileConfigParameter = configParameter.getProfileConfigParameter();
			profileConfigComposite.setProfileConfigParameter(profileConfigParameter);
		}

		syncWidgetsToEntity();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				configEntity = configModel.create(configEntity);

				create = false;

				// observe the Model
				configModel.addListener(configListener, configEntity.getId());

				// Set the PK of the new entity to the EditorInput
				// not necessary, cause the key of ConfigEditorInput is not the Config's PK

				// set new entity
				setEntity(configEntity);
			}
			else {
				if (configEntity.getScope() == ConfigScope.GLOBAL_ADMIN) {
					ConfigEditorInput editorInput = ConfigEditorInput.getInstance(ConfigScope.GLOBAL_CUSTOMER, null);
					if ( ! ConfigEditor.saveEditor(editorInput)) {
						return;
					}
				}
				else if (configEntity.getScope() == ConfigScope.EVENT_ADMIN) {
					ConfigEditorInput editorInput = ConfigEditorInput.getInstance(ConfigScope.EVENT_CUSTOMER, configEntity.getKey());
					if ( ! ConfigEditor.saveEditor(editorInput)) {
						return;
					}
				}

				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				configModel.update(configEntity);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (configEntity != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						if (generalConfigComposite != null) {
							generalConfigComposite.syncWidgetsToEntity();
						}

						if (financeConfigComposite != null) {
							financeConfigComposite.syncWidgetsToEntity();
						}

						if (eventConfigComposite != null) {
							eventConfigComposite.syncWidgetsToEntity();
						}

						if (participantConfigComposite != null) {
							participantConfigComposite.syncWidgetsToEntity();
						}

						if (profileConfigComposite != null) {
							profileConfigComposite.syncWidgetsToEntity();
						}

						xmlText.setText(configEntity.getConfigData());


						String name = getName();
						setPartName(name);
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(name);
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						setDirty(false);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncEntityToWidgets() {
		if (configEntity != null) {
			if (generalConfigComposite != null) {
				generalConfigComposite.syncEntityToWidgets();
			}

			if (financeConfigComposite != null) {
				financeConfigComposite.syncEntityToWidgets();
			}

			if (eventConfigComposite != null) {
				eventConfigComposite.syncEntityToWidgets();
			}

			if (participantConfigComposite != null) {
				participantConfigComposite.syncEntityToWidgets();
			}

			if (profileConfigComposite != null) {
				profileConfigComposite.syncEntityToWidgets();
			}

			String xml = configParameter.toXML();
			configEntity.setConfigData(xml);
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		if (generalConfigComposite != null) {
			generalConfigComposite.addModifyListener(listener);
		}

		if (financeConfigComposite != null) {
			financeConfigComposite.addModifyListener(listener);
		}

		if (eventConfigComposite != null) {
			eventConfigComposite.addModifyListener(listener);
		}

		if (participantConfigComposite != null) {
			participantConfigComposite.addModifyListener(listener);
		}

		if (profileConfigComposite != null) {
			profileConfigComposite.addModifyListener(listener);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (configEntity != null && configEntity.getId() != null) {
			configModel.refresh(configEntity.getId());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				configEntity = configModel.getConfig(configEntity.getId());
				if (configEntity != null) {
					setEntity(configEntity);
				}
			}
		}
	}


	@Override
	protected String getName() {
		switch (scope) {
			case GLOBAL_ADMIN:
				return CoreI18N.Config_GlobalAdministratorConfiguration;
			case EVENT_ADMIN:
				return CoreI18N.Config_EventAdministratorConfiguration;
			case GLOBAL_CUSTOMER:
				return CoreI18N.Config_GlobalCustomerConfiguration;
			case EVENT_CUSTOMER:
				return CoreI18N.Config_EventCustomerConfiguration;
			case GROUP:
				return CoreI18N.Config_GroupConfiguration;
			case USER:
				return CoreI18N.Config_UserConfiguration;

			default:
				return CoreI18N.Config_Configuration;
		}
	}


	@Override
	protected String getToolTipText() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			return product.getName() + " " + CoreI18N.Config_Configuration;
		}
		else {
			return CoreI18N.Config_Configuration ;
		}
	}


	@Override
	public boolean isNew() {
		return configEntity.getNewTime() == null;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			"Scope",
			"Key",
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};


		// the values of the info dialog
		final String[] values = {
			String.valueOf( configEntity.getId() ),
			configEntity.getScope().toString(),
			configEntity.getKey(),
			configEntity.getNewTime().getString(),
			configEntity.getNewDisplayUser().getString(),
			configEntity.getEditTime().getString(),
			configEntity.getEditDisplayUser().getString()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			CoreI18N.Config_GlobalConfiguration,
			labels,
			values
		);
		infoDialog.open();
	}

}
