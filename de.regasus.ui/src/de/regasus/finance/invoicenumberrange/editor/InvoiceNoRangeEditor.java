package de.regasus.finance.invoicenumberrange.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.actions.ActionFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.ui.Activator;

/**
 *
 * Realizes following requirements from {@link https://lambdalogic.atlassian.net/browse/MIRCP-126}:
 *
 * <ul>
 * <li>shows name of (optional) event</li>
 * <li>for new invoice number ranges, which are created from view "invoices", the (optional) event can be chosen via a
 * combo box</li>
 * <li>for saved invoice number ranges, the event cannot be changed anymore</li>
 * <li>the field "next number" is not editable</li>
 * <li>the field "first number" is not editable, when numbers already have been assigned (START_NO != NEXT_NO)</li>
 * <li>the editor contains 2 pages</li>
 * </ul>
 */
public class InvoiceNoRangeEditor
extends AbstractEditor<InvoiceNoRangeEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> , EventIdProvider {

	public static final String ID = "InvoiceNoRangeEditor";

	// the entity
	private InvoiceNoRangeCVO invoiceNoRangeCVO;

	// the model
	private InvoiceNoRangeModel invoiceNoRangeModel;


	// **************************************************************************
	// * Widgets
	// *

	private GeneralTabComposite generalTabComposite;
	private TemplateTabComposite templateTabComposite;

	private TabFolder tabFolder;

	private LazyScrolledTabItem generalTabItem;
	private TabItem templatesTabItem;


	private EventVO eventVO;

	private ConfigParameterSet configParameterSet;

	// *
	// * Widgets
	// **************************************************************************

	// ******************************************************************************************
	// * Overriden EditorPart methods

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long invoiceNoRangePK = editorInput.getKey();
		Long eventPK = editorInput.getEventId();
		eventVO = EventModel.getInstance().getEventVO(eventPK);
		configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventPK);

		// get models
		invoiceNoRangeModel = InvoiceNoRangeModel.getInstance();

		// get entity
		if (invoiceNoRangePK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			invoiceNoRangeCVO = invoiceNoRangeModel.getExtendedInvoiceNoRangeCVO(invoiceNoRangePK);

			// register at model
			invoiceNoRangeModel.addListener(this, invoiceNoRangePK);
		}
		else {
			// create empty entity
			InvoiceNoRangeVO invoiceNoRangeVO = new InvoiceNoRangeVO();
			invoiceNoRangeVO.setEventPK(eventPK);
			invoiceNoRangeCVO = new InvoiceNoRangeCVO();
			invoiceNoRangeCVO.setVO(invoiceNoRangeVO);
		}
	}


	@Override
	public void dispose() {
		if (invoiceNoRangeModel != null && invoiceNoRangeCVO.getPK() != null) {
			try {
				invoiceNoRangeModel.removeListener(this, invoiceNoRangeCVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(InvoiceNoRangeCVO invoiceNoRangeCVO) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		invoiceNoRangeCVO = invoiceNoRangeCVO.clone();
		}

		this.invoiceNoRangeCVO = invoiceNoRangeCVO;

		// set entity to other composites
		// ...

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return InvoiceLabel.InvoiceNoRange.getString();
	}


	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.InvoiceNoRangeEditor_InfoButtonToolTip;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			tabFolder = new TabFolder(parent, SWT.NONE);

			// General Tab
			{
    			generalTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
    			generalTabItem.setText(UtilI18N.General);

    			generalTabItem.getContentComposite();

    			generalTabComposite = new GeneralTabComposite(generalTabItem.getContentComposite(), configParameterSet);
    			generalTabItem.refreshScrollbars();
			}

			// Template Tab
			{
    			templatesTabItem = new TabItem(tabFolder, SWT.NONE);
    			templatesTabItem.setText(UtilI18N.Templates);

    			templateTabComposite = new TemplateTabComposite(tabFolder);
    			templatesTabItem.setControl(templateTabComposite);
			}

			// MIRCP-284 - Copy und Paste
			getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());

			/* Sync widgets and groups to the entity.
			 *
			 * Because this editor doesn't use any sub-composites or -groups, we
			 * could call syncWidgetsToEntity() also. But for compatibility with
			 * other editors, we call setEntity().
			 */
			setEntity(invoiceNoRangeCVO);

			// after sync add this as ModifyListener to all widgets and groups
			generalTabComposite.addModifyListener(this);
			templateTabComposite.addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = invoiceNoRangeCVO.getPK() == null;

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
				invoiceNoRangeCVO = invoiceNoRangeModel.create(invoiceNoRangeCVO);

				// observe the Model
				invoiceNoRangeModel.addListener(this, invoiceNoRangeCVO.getPK());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(invoiceNoRangeCVO.getPK());

				// set new entity
				setEntity(invoiceNoRangeCVO);
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
				invoiceNoRangeModel.update(invoiceNoRangeCVO);

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
		if (invoiceNoRangeCVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						generalTabComposite.setInvoiceNoRange( invoiceNoRangeCVO.getVO() );
						templateTabComposite.setInvoiceNoRange( invoiceNoRangeCVO.getVO() );


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
		if (invoiceNoRangeCVO != null) {
			generalTabComposite.syncEntityToWidgets();
			templateTabComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == invoiceNoRangeModel) {
				// If the model was completely l
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (invoiceNoRangeCVO != null) {
					invoiceNoRangeCVO = invoiceNoRangeModel.getExtendedInvoiceNoRangeCVO(invoiceNoRangeCVO.getPK());
					if (invoiceNoRangeCVO != null) {
						setEntity(invoiceNoRangeCVO);
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
		if (invoiceNoRangeCVO != null && invoiceNoRangeCVO.getPK() != null) {
			invoiceNoRangeModel.refresh(invoiceNoRangeCVO.getPK());

			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				invoiceNoRangeCVO = invoiceNoRangeModel.getExtendedInvoiceNoRangeCVO(invoiceNoRangeCVO.getPK());
				if (invoiceNoRangeCVO != null) {
					setEntity(invoiceNoRangeCVO);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return invoiceNoRangeCVO.getPK() == null;
	}


	@Override
	protected String getName() {
		String name = null;
		if (invoiceNoRangeCVO != null) {
			name = invoiceNoRangeCVO.getVO().getName();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.InvoiceNoRangeEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.InvoiceNoRangeEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(KernelLabel.Name.getString());
			toolTipText.append(": ");
			toolTipText.append(invoiceNoRangeCVO.getVO().getName());

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
		if (invoiceNoRangeCVO != null) {
			return invoiceNoRangeCVO.getInvoiceNoRangeVO().getEventPK();
		}
		return null;
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
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		InvoiceNoRangeVO invoiceNoRangeVO = invoiceNoRangeCVO.getVO();

		// get name of event
		String eventMnemonic = eventVO.getMnemonic();

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			avoidNull( invoiceNoRangeVO.getID() ),
			avoidNull(eventMnemonic),
			avoidNull( invoiceNoRangeVO.getEventPK() ),
			formatHelper.formatDateTime( invoiceNoRangeVO.getNewTime() ),
			invoiceNoRangeVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime( invoiceNoRangeVO.getEditTime() ),
			invoiceNoRangeVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			InvoiceLabel.InvoiceNoRange.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
