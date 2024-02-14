package de.regasus.common.country.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.Country;
import de.regasus.core.CountryModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;

public class CountryEditor
extends AbstractEditor<CountryEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "CountryEditor";

	// the entity
	private Country country;

	// the model
	private CountryModel countryModel;

	// **************************************************************************
	// * Widgets
	// *

	private Text code;
	private Text alpha3;
	private I18NText names;
	private I18NText region1;
	private I18NText region2;
	private I18NText region3;
	private I18NText region4;
	private I18NText region5;
	private Text altNameList;

	// *
	// * Widgets
	// **************************************************************************


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		String key = editorInput.getKey();

		// get models
		countryModel = CountryModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			//So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			country = countryModel.getCountry(key);

			// register at model
			countryModel.addListener(this, key);
		}
		else {
			// create empty entity
			country = new Country();
		}
	}

	@Override
	public void dispose() {
		if (countryModel != null && country.getId() != null) {
			try {
				countryModel.removeListener(this, country.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}

	protected void setEntity(Country country) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		country = country.clone();
		}

		this.country = country;

		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return ContactLabel.Country.getString();
	}

//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ProgrammePointEditor_InfoButtonToolTip;
//	}

	/**
	 * Create contents of the editor part
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = SWTHelper.createScrolledContentComposite(parent);
			mainComposite.setLayout(new GridLayout(2, false));


			// Code
			Label countryCodeLabel = new Label(mainComposite, SWT.NONE);
			countryCodeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			countryCodeLabel.setText(ContactLabel.countryMnemonic.getString());
			SWTHelper.makeBold(countryCodeLabel);

			code = new Text(mainComposite, SWT.BORDER);
			code.setTextLimit( Country.ID.getMaxLength() );
			GridData gd_countryCode = new GridData(SWT.FILL, SWT.CENTER, true, false);
			code.setLayoutData(gd_countryCode);
			SWTHelper.makeBold(code);

			// ALPHA-3
			Label alpha3Label = new Label(mainComposite, SWT.NONE);
			alpha3Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			SWTHelper.makeBold(alpha3Label);
			alpha3Label.setText( Country.ALPHA3.getString() );
			alpha3Label.setToolTipText( Country.ALPHA3.getDescription() );

			alpha3 = new Text(mainComposite, SWT.BORDER);
			alpha3.setTextLimit( Country.ALPHA3.getMaxLength() );
			GridData gd_alpha3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
			SWTHelper.makeBold(alpha3);
			alpha3.setLayoutData(gd_alpha3);

			// Names
			Label namesLabel = new Label(mainComposite, SWT.NONE);
			namesLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			namesLabel.setText(KernelLabel.Name.getString());
			SWTHelper.makeBold(namesLabel);

			names = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance(), true);
			GridData gd_names = new GridData(SWT.FILL, SWT.CENTER, true, false);
			names.setLayoutData(gd_names);

			// Region 1
			Label region1Label = new Label(mainComposite, SWT.NONE);
			region1Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			region1Label.setText(ContactLabel.region1.getString());

			region1 = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance());
			GridData gd_region1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
			region1.setLayoutData(gd_region1);

			// Region 2
			Label region2Label = new Label(mainComposite, SWT.NONE);
			region2Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			region2Label.setText(ContactLabel.region2.getString());

			region2 = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance());
			GridData gd_region2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
			region2.setLayoutData(gd_region2);

			// Region 3
			Label region3Label = new Label(mainComposite, SWT.NONE);
			region3Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			region3Label.setText(ContactLabel.region3.getString());

			region3 = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance());
			GridData gd_region3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
			region3.setLayoutData(gd_region3);

			// Region 4
			Label region4Label = new Label(mainComposite, SWT.NONE);
			region4Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			region4Label.setText(ContactLabel.region4.getString());

			region4 = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance());
			GridData gd_region4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
			region4.setLayoutData(gd_region4);

			// Region 5
			Label region5Label = new Label(mainComposite, SWT.NONE);
			region5Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			region5Label.setText(ContactLabel.region5.getString());

			region5 = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance());
			GridData gd_region5 = new GridData(SWT.FILL, SWT.CENTER, true, false);
			region5.setLayoutData(gd_region5);

			// AltNameList
			Label altNameListLabel = new Label(mainComposite, SWT.NONE);
			altNameListLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			altNameListLabel.setText(ContactLabel.alt_name_list.getString());
			altNameListLabel.setToolTipText(ContactLabel.alt_name_list_description.getString());

			altNameList = new Text(mainComposite, SWT.BORDER);
			GridData gd_altNameList = new GridData(SWT.FILL, SWT.CENTER, true, false);
			altNameList.setLayoutData(gd_altNameList);


			// sync widgets and groups to the entity
			setEntity(country);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);

			SWTHelper.refreshSuperiorScrollbar(mainComposite);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			// sync entity with widgets (copy values from the widgets to the entity)
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				country = countryModel.create(country);

				// observe the CountryModel
				countryModel.addListener(this, country.getId());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey( country.getId() );

				// set new entity
				setEntity(country);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				countryModel.update(country);

				// setEntity will be calles indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateCountryErrorMessage;
			}
			else {
				msg = I18N.EditCountryErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (country != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						code.setText(StringHelper.avoidNull(country.getId()));
						code.setEnabled(isNew());

						names.setLanguageString(country.getName());
						alpha3.setText( avoidNull(country.getAlpha3()) );
						region1.setLanguageString(country.getRegion1());
						region2.setLanguageString(country.getRegion2());
						region3.setLanguageString(country.getRegion3());
						region4.setLanguageString(country.getRegion4());
						region5.setLanguageString(country.getRegion5());
						altNameList.setText( avoidNull(country.getAltNameList()) );

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
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
		if (country != null) {
			country.setId( StringHelper.trim(code.getText()) );
			country.setAlpha3( alpha3.getText() );
			country.setName( names.getLanguageString() );
			country.setRegion1( region1.getLanguageString() );
			country.setRegion2( region2.getLanguageString() );
			country.setRegion3( region3.getLanguageString() );
			country.setRegion4( region4.getLanguageString() );
			country.setRegion5( region5.getLanguageString() );
			country.setAltNameList( altNameList.getText() );
		}
	}


	private void addModifyListener(ModifyListener listener) {
		code.addModifyListener(listener);
		alpha3.addModifyListener(listener);
		names.addModifyListener(listener);
		region1.addModifyListener(listener);
		region2.addModifyListener(listener);
		region3.addModifyListener(listener);
		region4.addModifyListener(listener);
		region5.addModifyListener(listener);
		altNameList.addModifyListener(listener);
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		String[] labels = {
			UtilI18N.Name,
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// the values of the info dialog
		String[] values = {
			getName(),
			String.valueOf(country.getId()),
			country.getNewTime().getString(),
			country.getNewDisplayUserStr(),
			country.getEditTime().getString(),
			country.getEditDisplayUserStr()
		};

		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ContactLabel.Country.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == countryModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (country != null) {
					country = countryModel.getCountry(country.getId());
					if (country != null) {
						setEntity(country);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (country != null && country.getId() != null) {
			countryModel.refresh(country.getId());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				country = countryModel.getCountry(country.getId());
				if (country != null) {
					setEntity(country);
				}
			}
		}
	}

	@Override
	protected String getName() {
		String name = null;
		if (country != null && country.getName() != null) {
			name = country.getName().getString();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.CountryEditor_NewName;
		}
		return name;
	}

	@Override
	protected String getToolTipText() {
		return I18N.CountryEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return country.getNewTime() == null;
	}

}
