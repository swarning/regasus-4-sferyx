package de.regasus.hotel.cancelterm.editor;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.hotel.HotelCancelationTermModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.ui.Activator;

/**
 * Most of this editors functionality is now implemented in its superclass {@link AbstractCancelationTermEditor},
 * which it shares with the {@link HotelCancelationTermEditor}. In this class we only have the stuff
 * that needs to now that this is a Cancellation for a <i>Hotel Offering</i>.
 */
public class HotelCancelationTermEditor
extends AbstractEditor<HotelCancelationTermEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "HotelCancelationTermEditor";

	// the entity
	private HotelCancelationTermVO cancelationTermVO;

	private HotelOfferingVO offeringVO;

	// the model
	private HotelCancelationTermModel cancelationTermModel;
	private HotelOfferingModel offeringModel;

	// **************************************************************************
	// * Widgets
	// *

	protected HotelCancelationTermComposite hotelCancelationTermComposite;

	// *
	// * Widgets
	// **************************************************************************


	private CacheModelListener<Long> hotelOfferingCacheModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			if (event.getOperation() == CacheModelOperation.REFRESH ||
				event.getOperation() == CacheModelOperation.UPDATE
			) {
    			List<Long> hoPKs = event.getKeyList();
    			if (hoPKs.contains(offeringVO.getID())) {

    				SWTHelper.syncExecDisplayThread(new Runnable() {
    					@Override
						public void run() {
            				try {
            					offeringVO = HotelOfferingModel.getInstance().getHotelOfferingVO(offeringVO.getID());
            					hotelCancelationTermComposite.setOfferingVO(offeringVO);
            				}
            				catch (Exception e) {
            					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
            				}
    					}
        			});

    			}
			}
		}
	};


	// ******************************************************************************************
	// * Overwritten EditorPart methods

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long canelationTermPK = editorInput.getKey();
		Long offeringPK = editorInput.getOfferingPK();

		// get models
		cancelationTermModel = HotelCancelationTermModel.getInstance();
		offeringModel = HotelOfferingModel.getInstance();

		if (canelationTermPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			cancelationTermVO = cancelationTermModel.getHotelCancelationTermVO(canelationTermPK);

			offeringPK = cancelationTermVO.getOfferingPK();
			if (offeringPK != null) {
				// get the OfferingVO
				offeringVO = offeringModel.getHotelOfferingVO(offeringPK);
			}

			// register at model
			cancelationTermModel.addListener(this, canelationTermPK);
		}
		else {
			// get the OfferingVO
			offeringVO = offeringModel.getHotelOfferingVO(offeringPK);

			// create empty entity
			cancelationTermVO = new HotelCancelationTermVO();
			cancelationTermVO.setOfferingPK(offeringPK);

			// copy the price of the offering and set amount to 0
			PriceVO priceVO = cancelationTermVO.getPriceVO();
			priceVO.setPriceVO(offeringVO.getLodgePriceVO());
			priceVO.setTaxRate(BigDecimal.ZERO); // explicitly asked for in https://mi2.lambdalogic.de/jira/browse/MIRCP-99
			priceVO.setAmount(BigDecimal.ZERO);
		}


		offeringModel.addListener(
			hotelOfferingCacheModelListener,
			offeringVO.getID()
		);

	}


	@Override
	public void dispose() {
		try {
			if (cancelationTermModel != null && cancelationTermVO.getID() != null) {
				cancelationTermModel.removeListener(this, cancelationTermVO.getID());
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			if (offeringModel != null && offeringVO.getID() != null) {
				offeringModel.removeListener(hotelOfferingCacheModelListener, offeringVO.getID());
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		super.dispose();
	}


	protected void setEntity(HotelCancelationTermVO hctVO) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			hctVO = hctVO.clone();
		}

		this.cancelationTermVO = hctVO;


		// set entity to other composites
		hotelCancelationTermComposite.setCancelationTermVO(this.cancelationTermVO);

		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return HotelLabel.HotelCancelationTerm.getString();
	}

	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.HotelCancelationTermEditor_InfoButtonToolTip;
	}

	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite contentComposite = SWTHelper.createScrolledContentComposite(parent);

			// General Tab
			hotelCancelationTermComposite = new HotelCancelationTermComposite(contentComposite, SWT.NONE, getEventId());
			hotelCancelationTermComposite.setOfferingVO(offeringVO);

			// set data
			setEntity(cancelationTermVO);

			// after sync add this as ModifyListener to all widgets and groups
			hotelCancelationTermComposite.addModifyListener(this);

			SWTHelper.refreshSuperiorScrollbar(contentComposite);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			HotelLabel.HotelOfferingID.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(cancelationTermVO.getID()),
			StringHelper.avoidNull(cancelationTermVO.getOfferingPK()),
			formatHelper.formatDateTime(cancelationTermVO.getNewTime()),
			cancelationTermVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(cancelationTermVO.getEditTime()),
			cancelationTermVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.HotelCancelationTerm.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
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
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				cancelationTermVO = cancelationTermModel.create(cancelationTermVO);

				// observe the Model
				cancelationTermModel.addListener(this, cancelationTermVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(cancelationTermVO.getID());

				// set new entity
				setEntity(cancelationTermVO);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				cancelationTermModel.update(cancelationTermVO);

				// setEntity will be called indirectly in dataChange()
			}
			monitor.worked(1);
		}
		catch (Exception e) {
			monitor.setCanceled(true);
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (cancelationTermVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
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
		if (cancelationTermVO != null) {
			hotelCancelationTermComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == cancelationTermModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (cancelationTermVO != null) {
					cancelationTermVO = cancelationTermModel.getHotelCancelationTermVO(cancelationTermVO.getPK());
					if (cancelationTermVO != null) {
						setEntity(cancelationTermVO);
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
		if (cancelationTermVO != null && cancelationTermVO.getID() != null) {
			cancelationTermModel.refresh(cancelationTermVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				cancelationTermVO = cancelationTermModel.getHotelCancelationTermVO(cancelationTermVO.getID());
				if (cancelationTermVO != null) {
					setEntity(cancelationTermVO);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return cancelationTermVO.getPK() == null;
	}


	@Override
	protected String getName() {
		return InvoiceLabel.CancellationTerm.getString()  + " (" + HotelLabel.HotelOffering.getString()+ ")";
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			HotelOfferingVO hotelOfferingVO = offeringVO;

			Long eventPK = hotelOfferingVO.getEventPK();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			Long hotelContingentPK = hotelOfferingVO.getHotelContingentPK();
			HotelContingentVO hotelContingentVO = HotelContingentModel.getInstance().getHotelContingentVO(hotelContingentPK);

			Long hotelID = hotelContingentVO.getHotelPK();
			Hotel hotel = HotelModel.getInstance().getHotel(hotelID);

			Long roomDefinitionPK = hotelOfferingVO.getRoomDefinitionPK();
			RoomDefinitionVO roomDefinitionVO = RoomDefinitionModel.getInstance().getRoomDefinitionVO(roomDefinitionPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.HotelCancelationTermEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.RoomDefinition.getString());
			toolTipText.append(": ");
			toolTipText.append(roomDefinitionVO.getName().getString());

			toolTipText.append('\n');
			toolTipText.append(KernelLabel.Description.getString());
			toolTipText.append(": ");
			toolTipText.append(hotelOfferingVO.getDescription().getString());

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.HotelContingent.getString());
			toolTipText.append(": ");
			toolTipText.append(hotelContingentVO.getName());

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.Hotel.getString());
			toolTipText.append(": ");
			toolTipText.append(hotel.getName1());

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.Event.getString());
			toolTipText.append(": ");
			toolTipText.append(eventVO.getMnemonic());

			toolTip = toolTipText.toString();
		}
		catch (Exception e) {
			// This shouldn't happen
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			toolTip = "";
		}
		return toolTip;
	}


	@Override
	public Long getEventId() {
		if (offeringVO != null) {
			Long eventPK = offeringVO.getEventPK();
			if (eventPK != null) {
				return eventPK;
			}
		}
		return null;
	}

}
