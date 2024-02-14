package de.regasus.programme.offering.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.contact.LabelTextCombinationsVO;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.common.Language;
import de.regasus.common.composite.LabelTextCombinationsComposite;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

public class ProgrammeOfferingEditor
extends AbstractEditor<ProgrammeOfferingEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "ProgrammeOfferingEditor";

	// the entity
	private ProgrammeOfferingVO programmeOfferingVO;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;

	// the model
	private ProgrammeOfferingModel programmeOfferingModel = ProgrammeOfferingModel.getInstance();
	private ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();

	private Long eventPK = null;

	private List<Language> languageList;


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;
	private ProgrammeOfferingGeneralComposite generalComposite;
	private LabelTextCombinationsComposite labelTextCombinationsComposite;

	// *
	// * Widgets
	// **************************************************************************

	// ******************************************************************************************
	// * Overriden EditorPart methods
	// *

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long programmeOfferingPK = editorInput.getKey();
		eventPK = editorInput.getEventId();

		if (programmeOfferingPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			programmeOfferingVO = programmeOfferingModel.getProgrammeOfferingVO(programmeOfferingPK);

			// set the ProgrammeOfferingVO's eventPK if it hasn't one
			if (programmeOfferingVO.getEventPK() == null) {
				programmeOfferingVO.setEventPK(eventPK);
			}

			// register at model
			programmeOfferingModel.addListener(this, programmeOfferingPK);
		}
		else {
			// create empty entity
			programmeOfferingVO = new ProgrammeOfferingVO();
			programmeOfferingVO.setProgrammePointPK(editorInput.getProgrammePointPK());
			programmeOfferingVO.setEventPK(eventPK);

			// set default values from event
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			PriceVO priceVO = eventVO.getProgPriceDefaultsVO().createPriceVO();
			programmeOfferingVO.setMainPriceVO(priceVO);
		}


		// get ConfigParameterSet (eventPK must not be null)
		configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);

		// determine languages
		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
		List<String> languageIds = eventVO.getLanguages();
		languageList = LanguageModel.getInstance().getLanguages(languageIds);
	}


	@Override
	public void dispose() {
		if (programmeOfferingModel != null && programmeOfferingVO.getID() != null) {
			try {
				programmeOfferingModel.removeListener(this, programmeOfferingVO.getID());
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ProgrammeOfferingVO programmeOfferingVO) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			programmeOfferingVO = programmeOfferingVO.clone();
		}

		try {
			this.programmeOfferingVO = programmeOfferingVO;

			if (programmeOfferingVO.getEventPK() == null) {
				programmeOfferingVO.setEventPK(eventPK);
			}

			// set entity to other composites
			generalComposite.setProgrammeOfferingVO(programmeOfferingVO);
			labelTextCombinationsComposite.setEntity( programmeOfferingVO.getLabelTextCombinationsVO() );

			syncWidgetsToEntity();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.ProgrammeOffering.getString();
	}


	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.ProgrammeOfferingEditor_InfoButtonToolTip;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			// tabFolder
			tabFolder = new TabFolder(parent, SWT.NONE);

			// General Tab
			LazyScrolledTabItem generalTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			generalTabItem.setText(I18N.ProgrammeOfferingEditor_GeneralTabText);
			generalComposite = new ProgrammeOfferingGeneralComposite(
				generalTabItem.getContentComposite(),
				SWT.NONE,
				getEventId(),
				languageList,
				configParameterSet.getEvent().getProgramme()
			);

			generalTabItem.refreshScrollbars();


			// LabelTextCombinations Tab
			LazyScrolledTabItem labelTextCombinationsTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			labelTextCombinationsTabItem.setText(UtilI18N.Texts);

			LabelTextCombinationsVO labelTextCombinationsVO = programmeOfferingVO.getLabelTextCombinationsVO();
			labelTextCombinationsComposite = new LabelTextCombinationsComposite(
				labelTextCombinationsTabItem.getContentComposite(),
				SWT.NONE,
				labelTextCombinationsVO,
				languageList
			);

			labelTextCombinationsTabItem.refreshScrollbars();


			// set data
			setEntity(programmeOfferingVO);

			// after sync add this as ModifyListener to all widgets and groups
			generalComposite.addModifyListener(this);
			labelTextCombinationsComposite.addModifyListener(this);
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

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			// Show warning if the invoice number range of additional price components differ from the main price
			if (! programmeOfferingVO.hasConsistentInvoiceNumberRangeInNonEmptyPrices()) {
				MessageDialog.openWarning(getSite().getShell(), UtilI18N.Warning, I18N.InconsistentInvoiceNumberRanges_Message);
			}

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				programmeOfferingVO = programmeOfferingModel.create(programmeOfferingVO);

				// observe the Model
				programmeOfferingModel.addListener(this, programmeOfferingVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(programmeOfferingVO.getID());

				// set new entity
				setEntity(programmeOfferingVO);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				programmeOfferingModel.update(programmeOfferingVO);

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
					if (programmeOfferingVO.isCancelled()) {
						image = IconRegistry.getImage(IImageKeys.PROGRAMME_OFFERING_CANCELLED);
					}
					else if (programmeOfferingVO.isDisabled()) {
						image = IconRegistry.getImage(IImageKeys.PROGRAMME_OFFERING_DISABLED);
					}
					else {
						image = IconRegistry.getImage(IImageKeys.PROGRAMME_OFFERING);
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


	private void syncEntityToWidgets() {
		generalComposite.syncEntityToWidgets();
		labelTextCombinationsComposite.syncEntityToWidgets();
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == programmeOfferingModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (programmeOfferingVO != null) {
					programmeOfferingVO = programmeOfferingModel.getProgrammeOfferingVO(programmeOfferingVO.getPK());
					if (programmeOfferingVO != null) {
						setEntity(programmeOfferingVO);
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
		if (programmeOfferingVO != null && programmeOfferingVO.getID() != null) {
			programmeOfferingModel.refresh(programmeOfferingVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				programmeOfferingVO = programmeOfferingModel.getProgrammeOfferingVO(programmeOfferingVO.getID());
				if (programmeOfferingVO != null) {
					setEntity(programmeOfferingVO);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return programmeOfferingVO.getPK() == null;
	}


	@Override
	protected String getName() {
		String name = null;

		if (programmeOfferingVO != null) {
			I18NPattern label = new I18NPattern();

			// Name of Programme Point
			try {
				Long programmePointPK = programmeOfferingVO.getProgrammePointPK();
				ProgrammePointVO programmePointVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);
				label.add(programmePointVO.getName());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			label.add(": ");

			// description
			LanguageString description = programmeOfferingVO.getDescription();
			if (description != null && ! description.isEmpty()) {
				String strDescription = description.getString();
				strDescription = StringHelper.trim(strDescription);
				label.add(strDescription);
			}
			else {
				// Participant Type
				Long participantTypePK = programmeOfferingVO.getParticipantTypePK();
				if (participantTypePK != null) {
					try {
						ParticipantType participantType = ParticipantTypeModel.getInstance().getParticipantType(
							participantTypePK
						);
						if (participantType != null) {
							label.add(participantType.getName());
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
				else {
					// Price
					CurrencyAmount currencyAmountBrutto = programmeOfferingVO.getCurrencyAmountGross();
					label.add(currencyAmountBrutto);
				}
			}

			name = label.getString();
		}
		else {
			name = I18N.ProgrammeOfferingEditor_NewName;
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long eventPK = programmeOfferingVO.getEventPK();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			Long programmePointPK = programmeOfferingVO.getProgrammePointPK();
			ProgrammePointVO programmePointVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.ProgrammeOfferingEditor_DefaultToolTip);

			if (programmeOfferingVO.getDescription() != null) {
				toolTipText.append('\n');
				toolTipText.append(KernelLabel.Description.getString());
				toolTipText.append(": ");
				toolTipText.append(programmeOfferingVO.getDescription().getString());
			}

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
		return eventPK;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			ParticipantLabel.Event.getString(),
			ParticipantLabel.EventID.getString(),
			ParticipantLabel.ProgrammePointID.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			UtilI18N.CancelDateTime,
			UtilI18N.CancelUser
		};

		// get name of event
		Long eventPK = programmeOfferingVO.getEventPK();
		String eventMnemonic = null;
		try {
			EventVO eventVO =  EventModel.getInstance().getEventVO(eventPK);
			if (eventVO != null) {
				eventMnemonic = eventVO.getMnemonic();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(programmeOfferingVO.getID()),
			eventMnemonic,
			StringHelper.avoidNull(eventPK),
			StringHelper.avoidNull(programmeOfferingVO.getProgrammePointPK()),
			formatHelper.formatDateTime(programmeOfferingVO.getNewTime()),
			programmeOfferingVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(programmeOfferingVO.getEditTime()),
			programmeOfferingVO.getEditDisplayUserStr(),
			formatHelper.formatDateTime(programmeOfferingVO.getCancelTime()),
			programmeOfferingVO.getCancelDisplayUserStr(),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
			ParticipantLabel.ProgrammeOffering.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
