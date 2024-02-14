package de.regasus.hotel.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.geo.GeoDataGroup;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;
import de.regasus.common.CommonI18N;
import de.regasus.common.CountryCity;
import de.regasus.common.Photo;
import de.regasus.common.composite.AddressGroupsComposite;
import de.regasus.common.composite.CommunicationGroup;
import de.regasus.common.composite.OrganisationNameGroup;
import de.regasus.core.PropertyModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelPhotoModel;
import de.regasus.hotel.chain.combo.HotelChainCombo;
import de.regasus.hotel.combo.HotelStarsCombo;

public class HotelEditor extends AbstractEditor<HotelEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "HotelEditor";

	// the entity
	private Hotel hotel;

	// the model
	private HotelModel hotelModel = HotelModel.getInstance();
	private HotelPhotoModel hotelPhotoModel = HotelPhotoModel.getInstance();

	// Widgets
	private TabFolder tabFolder;

	private OrganisationNameGroup organisationNameGroup;
	private HotelStarsCombo hotelStarsCombo;
	private HotelChainCombo hotelChainCombo;

	private GeoDataGroup geoDataGroup;

	private Text descriptionText;
	private Text noteText;

	private AddressGroupsComposite composite;

	private CommunicationGroup communicationGroup;

	private HotelFacilitiesComposite hotelFacilitiesComposite;

	private HotelPhotoComposite photoComposite;


	private static int lastSelectedTabIndex = 0;



	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			hotel = hotelModel.getHotel(key);

			// register at model
			hotelModel.addListener(this, key);
		}
		else {
			// create empty entity
			hotel = HotelModel.getInitialHotel();

			// if a country, city or hotel was selected in the hotel tree view,
			// give the new hotel the same country and possibly city.
			CountryCity countryCity = editorInput.getCity();
			if (countryCity != null) {
				Address address = new Address();

				address.setCity(countryCity.getCity());
				address.setCountryPK(countryCity.getCountryCode()); // May be null
				hotel.setMainAddress(address);
			}
		}
	}


	@Override
	public void dispose() {
		if (hotelModel != null && hotel.getID() != null) {
			try {
				hotelModel.removeListener(this, hotel.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
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

			tabFolder = new TabFolder(parent, SWT.NONE);

			buildGeneralTab();
			buildAddressTab();
			buildCommunicationTab();
			buildHotelFacilitiesTab();
			if ( !isNew() ) {
				buildPhotoTab();
			}


			// sync widgets and groups to the entity
			tabFolder.setSelection(lastSelectedTabIndex);

			tabFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Store the last selected index
					int selectionIndex = tabFolder.getSelectionIndex();
					if (selectionIndex != -1) {
						lastSelectedTabIndex = selectionIndex;
					}

				}
			});

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
		// sync widgets and groups to the entity
		setEntity(hotel);

		// after sync add this as ModifyListener to all widgets and groups
		organisationNameGroup.addModifyListener(this);
		hotelStarsCombo.addModifyListener(this);
		hotelChainCombo.addModifyListener(this);
		geoDataGroup.addModifyListener(this);
		descriptionText.addModifyListener(this);
		noteText.addModifyListener(this);
		composite.addModifyListener(this);
		communicationGroup.addModifyListener(this);
		hotelFacilitiesComposite.addModifyListener(this);
	}


	private void buildGeneralTab() throws Exception {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText(UtilI18N.General);

		Composite composite = new Composite(tabItem.getContentComposite(), SWT.NONE);

		final int COLUMN_COUNT = 4;
		composite.setLayout(new GridLayout(COLUMN_COUNT, false));

		/* Row 1
		 */

		// OrganisationNameGroup
		{
			organisationNameGroup = new OrganisationNameGroup(
				composite,
				SWT.NONE,
				new boolean[] {true, false, false, false} // required
			);
			organisationNameGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, COLUMN_COUNT, 1));
		}

		/* Row 2
		 */

		// Hotel Stars
		{
			Label label = new Label(composite, SWT.RIGHT);
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			label.setLayoutData(gridData);
			label.setText(HotelLabel.HotelStars.getString());
		}
		hotelStarsCombo = new HotelStarsCombo(composite, SWT.NONE);
		hotelStarsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


		// Hotel Chain
		{
			Label label = new Label(composite, SWT.RIGHT);
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			label.setLayoutData(gridData);
			label.setText(HotelLabel.HotelChain.getString());
		}
		hotelChainCombo = new HotelChainCombo(composite, SWT.NONE);
		hotelChainCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


		/* Row 3
		 */

		geoDataGroup = new GeoDataGroup(composite, SWT.NONE);
		geoDataGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_COUNT, 1));
		geoDataGroup.setText( CommonI18N.GeoData.getString() );


		/* Row 4
		 */

		// description
		{
			Label label = new Label(composite, SWT.RIGHT);
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			label.setLayoutData(gridData);
			label.setText(HotelLabel.Description.getString());
			label.setToolTipText(HotelLabel.Description_ToolTip.getString());
		}
		descriptionText = new MultiLineText(composite, SWT.BORDER, false);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, COLUMN_COUNT - 1, 1));
		descriptionText.setToolTipText(HotelLabel.Description_ToolTip.getString());

		/* Row 5
		 */

		// note
		{
			Label label = new Label(composite, SWT.RIGHT);
			GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			label.setLayoutData(gridData);
			label.setText( Hotel.NOTE.getLabel() );
			label.setToolTipText( Hotel.NOTE.getDescription() );
		}
		noteText = new MultiLineText(composite, SWT.BORDER, false);
		noteText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, COLUMN_COUNT - 1, 1));

		tabItem.refreshScrollbars();
	}


	private void buildAddressTab() throws Exception {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText(ContactLabel.Address.getString());

		// TODO: Add AddressConfigParameterSet as soon as such thing exists for Hotels
		composite = new AddressGroupsComposite(tabItem.getContentComposite(), SWT.NONE, null);
		composite.setMainCountryBold(true);
		composite.setMainCityBold(true);
		composite.setHomeCountryPK( PropertyModel.getInstance().getDefaultCountry() );

		// link with organisationNameGroup
		organisationNameGroup.addModifyListener(composite);

		tabItem.refreshScrollbars();
	}


	private void buildCommunicationTab() throws Exception {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText( Hotel.COMMUNICATION.getString() );
		communicationGroup = new CommunicationGroup(tabItem.getContentComposite(), SWT.NONE);

		tabItem.refreshScrollbars();
	}


	private void buildHotelFacilitiesTab() {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText(HotelLabel.Hotel_Facilities.getString());
		hotelFacilitiesComposite = new HotelFacilitiesComposite(tabItem.getContentComposite(), SWT.NONE);

		tabItem.refreshScrollbars();
	}


	private void buildPhotoTab() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(I18N.PortalEditor_PhotoTab);

		photoComposite = new HotelPhotoComposite(tabFolder, hotel);

		tabItem.setControl(photoComposite);

		photoComposite.addModifyListener(this);
	}



	protected void setEntity(Hotel hotel) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		hotel = hotel.clone();
		}

		this.hotel = hotel;

		organisationNameGroup.setOrganisation(hotel);
		communicationGroup.setCommunication(hotel.getCommunication());
		composite.setAbstractPerson(hotel);
		hotelFacilitiesComposite.setHotel(hotel);

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return HotelLabel.Hotel.getString();
	}

//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ProgrammePointEditor_InfoButtonToolTip;
//	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 3);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				hotel = hotelModel.create(hotel);

				// observe the HotelModel
				hotelModel.addListener(this, hotel.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(hotel.getID());

				buildPhotoTab();

				// set new entity
				setEntity(hotel);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				hotelModel.update(hotel);

				// setEntity will be called indirectly in dataChange()
			}
			monitor.worked(1);


			// save order of Photos
			if (photoComposite != null && photoComposite.isModified() ) {
    			List<Photo> photoList = photoComposite.getPhotoList();
    			if (photoList != null) {
    				List<Long> orderedPhotoIds = Photo.getPKs(photoList);
    				HotelPhotoModel.getInstance().updateOrder(orderedPhotoIds);
    			}
			}
			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (hotel != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						hotelStarsCombo.setHotelStars(hotel.getHotelStars());
						hotelChainCombo.setHotelChainPK(hotel.getHotelChainPK());
						geoDataGroup.setGeoData(hotel.getGeoData());
						descriptionText.setText(StringHelper.avoidNull(hotel.getDescription()));
						noteText.setText(StringHelper.avoidNull(hotel.getNote()));

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						setDirty(false);

						if (hotel.isDeleted()) {
							organisationNameGroup.setEnabled(false);
							hotelChainCombo.setEnabled(false);
							geoDataGroup.setEnabled(false);
							descriptionText.setEnabled(false);
							noteText.setEnabled(false);
							composite.setEnabled(false);
							communicationGroup.setEnabled(false);
							hotelFacilitiesComposite.setEnabled(false);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncEntityToWidgets() {
		if (hotel != null) {
			organisationNameGroup.syncEntityToWidgets();

			hotel.setHotelStars(hotelStarsCombo.getHotelStars());
			hotel.setHotelChainPK(hotelChainCombo.getHotelChainPK());
			hotel.setGeoData(geoDataGroup.getGeoData());
			hotel.setDescription(descriptionText.getText());
			hotel.setNote(noteText.getText());

			communicationGroup.syncEntityToWidgets();
			composite.syncEntityToWidgets();
			hotelFacilitiesComposite.syncEntityToWidgets();
		}
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
			UtilI18N.EditDateTime,
			UtilI18N.Deleted
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(hotel.getID()),
			formatHelper.formatDateTime(hotel.getEditTime()),
			hotel.isDeleted() ? UtilI18N.Yes : UtilI18N.No
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.Hotel.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == hotelModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (hotel != null) {
					hotel = hotelModel.getHotel(hotel.getID());
					if (hotel != null) {
						setEntity(hotel);
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
		if (hotel != null && hotel.getID() != null) {
			hotelModel.refresh( hotel.getID() );
			hotelPhotoModel.refreshForeignKey( hotel.getID() );

			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if ( isDirty() ) {
				hotel = hotelModel.getHotel( hotel.getID() );
				if (hotel != null) {
					setEntity(hotel);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (hotel != null) {
			name = hotel.getName1();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.HotelEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.HotelEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return hotel.getID() == null;
	}

}
