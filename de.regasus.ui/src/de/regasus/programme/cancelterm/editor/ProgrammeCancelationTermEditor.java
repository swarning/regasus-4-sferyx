package de.regasus.programme.cancelterm.editor;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.hotel.cancelterm.editor.HotelCancelationTermEditor;
import de.regasus.programme.ProgrammeCancelationTermModel;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

/**
 * Most of this editors functionality is now implemented in its superclass {@link AbstractCancelationTermEditor},
 * which it shares with the {@link HotelCancelationTermEditor}. In this class we only have the stuff
 * that needs to now that this is a Cancellation for a <i>Programme Offering</i>.
 */
public class ProgrammeCancelationTermEditor
extends AbstractEditor<ProgrammeCancelationTermEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "ProgrammeCancelationTermEditor";

	// the entity
	private ProgrammeCancelationTermVO cancelationTermVO;

	private ProgrammeOfferingVO offeringVO;

	// the model
	private ProgrammeCancelationTermModel cancelationTermModel;
	private ProgrammeOfferingModel offeringModel;

	// **************************************************************************
	// * Widgets
	// *

	protected ProgrammeCancelationTermComposite programmeCancelationTermComposite;

	// *
	// * Widgets
	// **************************************************************************


	private CacheModelListener<Long> programmeOfferingCacheModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			if (event.getOperation() == CacheModelOperation.REFRESH ||
				event.getOperation() == CacheModelOperation.UPDATE
			) {
    			List<Long> poPKs = event.getKeyList();
    			if (poPKs.contains(offeringVO.getID())) {

    				SWTHelper.syncExecDisplayThread(new Runnable() {
    					@Override
						public void run() {
    						try {
    	    					offeringVO = ProgrammeOfferingModel.getInstance().getProgrammeOfferingVO(offeringVO.getID());
    	    					programmeCancelationTermComposite.setOfferingVO(offeringVO);
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
	// * Overriden EditorPart methods

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long canelationTermPK = editorInput.getKey();
		Long offeringPK = editorInput.getOfferingPK();

		// get models
		cancelationTermModel = ProgrammeCancelationTermModel.getInstance();
		offeringModel = ProgrammeOfferingModel.getInstance();

		if (canelationTermPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			cancelationTermVO = cancelationTermModel.getProgrammeCancelationTermVO(canelationTermPK);

			offeringPK = cancelationTermVO.getOfferingPK();
			if (offeringPK != null) {
				// get the OfferingVO
				offeringVO = offeringModel.getProgrammeOfferingVO(offeringPK);
			}

			// register at model
			cancelationTermModel.addListener(this, canelationTermPK);
		}
		else {
			// get the OfferingVO
			offeringVO = offeringModel.getProgrammeOfferingVO(offeringPK);

			// create empty entity
			cancelationTermVO = new ProgrammeCancelationTermVO();
			cancelationTermVO.setOfferingPK(offeringPK);

			// copy the price of the offering and set amount to 0
			PriceVO priceVO = cancelationTermVO.getPriceVO();
			priceVO.setPriceVO(offeringVO.getMainPriceVO());
			// Programme Cancelation Terms shall have the same tax rate as their Offerings!
			priceVO.setAmount(BigDecimal.ZERO);
		}


		ProgrammeOfferingModel.getInstance().addListener(
			programmeOfferingCacheModelListener,
			offeringVO.getID()
		);

	}


	@Override
	public void dispose() {
		if (cancelationTermModel != null && cancelationTermVO.getID() != null) {
			try {
				cancelationTermModel.removeListener(this, cancelationTermVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		ProgrammeOfferingModel.getInstance().removeListener(
			programmeOfferingCacheModelListener,
			offeringVO.getID()
		);

		super.dispose();
	}


	protected void setEntity(ProgrammeCancelationTermVO pctVO) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			pctVO = pctVO.clone();
		}

		this.cancelationTermVO = pctVO;


		// set entity to other composites
		programmeCancelationTermComposite.setCancelationTermVO(this.cancelationTermVO);

		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return ParticipantLabel.ProgrammeCancelationTerm.getString();
	}

	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.ProgrammeCancelationTermEditor_InfoButtonToolTip;
	}

	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite contentComposite = SWTHelper.createScrolledContentComposite(parent);
			programmeCancelationTermComposite = new ProgrammeCancelationTermComposite(contentComposite, SWT.NONE, getEventId());
			programmeCancelationTermComposite.setOfferingVO(offeringVO);
			SWTHelper.refreshSuperiorScrollbar(contentComposite);

			// set data
			setEntity(cancelationTermVO);

			// after sync add this as ModifyListener to all widgets and groups
			programmeCancelationTermComposite.addModifyListener(this);
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
			ParticipantLabel.ProgrammeOfferingID.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			UtilI18N.CancelDateTime,
			UtilI18N.CancelUser
		};

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(cancelationTermVO.getID()),
			StringHelper.avoidNull(cancelationTermVO.getOfferingPK()),
			formatHelper.formatDateTime(cancelationTermVO.getNewTime()),
			cancelationTermVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(cancelationTermVO.getEditTime()),
			cancelationTermVO.getEditDisplayUserStr(),
			formatHelper.formatDateTime(cancelationTermVO.getCancelTime()),
			cancelationTermVO.getCancelDisplayUserStr(),
		};

		// show info dialog
		String title = ParticipantLabel.ProgrammeCancelationTerm.getString() + ": " + UtilI18N.Info;
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
			title,
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


						// set image
						Image image = null;
						if ( cancelationTermVO.isCancelled() ) {
							image = IconRegistry.getImage(IImageKeys.PROGRAMME_CANCELATION_TERM_CANCELLED);
						}
						else {
							image = IconRegistry.getImage(IImageKeys.PROGRAMME_CANCELATION_TERM);
						}
						setTitleImage(image);


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
			programmeCancelationTermComposite.syncEntityToWidgets();
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
					cancelationTermVO = cancelationTermModel.getProgrammeCancelationTermVO(cancelationTermVO.getPK());
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
				cancelationTermVO = cancelationTermModel.getProgrammeCancelationTermVO(cancelationTermVO.getID());
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
		return InvoiceLabel.CancellationTerm.getString();
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			ProgrammeOfferingVO programmeOfferingVO = offeringVO;

			Long eventPK = programmeOfferingVO.getEventPK();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			Long programmePointPK = programmeOfferingVO.getProgrammePointPK();
			ProgrammePointVO programmePointVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.ProgrammeCancelationTermEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.ProgrammeOffering.getString());
			toolTipText.append(": ");
			// May be null, but don't just fallback to NPE (see MIRCP-2086)
			if (programmeOfferingVO.getDescription() != null) {
				toolTipText.append(programmeOfferingVO.getDescription().getString());
				toolTipText.append(", ");
			}
			CurrencyAmount currencyAmount = programmeOfferingVO.getCurrencyAmountGross();
			toolTipText.append(currencyAmount.format(false, true));

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.ProgrammePoint.getString());
			toolTipText.append(": ");
			toolTipText.append(programmePointVO.getName().getString());

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
