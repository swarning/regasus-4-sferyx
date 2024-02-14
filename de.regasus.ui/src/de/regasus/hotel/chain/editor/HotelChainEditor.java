package de.regasus.hotel.chain.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.hotel.HotelChain;
import de.regasus.hotel.HotelChainModel;

public class HotelChainEditor extends AbstractEditor<HotelChainEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "HotelChainEditor";

	// the entity
	private HotelChain hotelChain;

	// the model
	private HotelChainModel hotelChainModel = HotelChainModel.getInstance();

	// Widgets
	private Text nameText;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		hotelChainModel = HotelChainModel.getInstance();

		if (key != null) {
			// get entity
			hotelChain = hotelChainModel.getHotelChain(key);

			// register at model
			hotelChainModel.addListener(this, key);
		}
		else {
			// create empty entity
			hotelChain = new HotelChain();
		}
	}


	@Override
	public void dispose() {
		if (hotelChainModel != null && hotelChain.getId() != null) {
			try {
				hotelChainModel.removeListener(this, hotelChain.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(HotelChain hotelChain) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			hotelChain = hotelChain.clone();
		}

		this.hotelChain = hotelChain;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return HotelLabel.HotelChain.getString();
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
    		this.parent = parent;

			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout(2, false));

    		// Hotel Chain
			final Label label = new Label(mainComposite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			label.setText(HotelLabel.HotelChain.getString());

			nameText = new Text(mainComposite, SWT.BORDER);
			nameText.setTextLimit( HotelChain.NAME.getMaxLength() );
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// set data
			setEntity(hotelChain);

			// after sync add this as ModifyListener to widgets
			addModifyListener(this);
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
			 * Synchronizing Entity with the widgets.
			 * The data of the widgets are copied to the Entity.
			 */

			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				hotelChain = hotelChainModel.create(hotelChain);

				// observe the HotelModel
				hotelChainModel.addListener(this, hotelChain.getId());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(hotelChain.getId());

				// set new entity
				setEntity(hotelChain);
			}
			else {
				/* Save the entity.
				 * On success setEntity will be called indirectly in dataChange(),
				 * else an Exception will be thrown.
				 * The result of update() must not be assigned to HotelChain,
				 * because this will happen in setEntity() and there it may be cloned!
				 * Assigning HotelChain here would overwrite the cloned value with
				 * the one from the model. Therefore we would have inconsistent data!
				 */
				hotelChainModel.update(hotelChain);
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
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
		if (hotelChain != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						nameText.setText(StringHelper.avoidNull(hotelChain.getName()));

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
		if (hotelChain != null) {
			hotelChain.setName(nameText.getText());
		}
	}


	private void addModifyListener(ModifyListener listener) {
		nameText.addModifyListener(listener);
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};


		// the values of the info dialog
		String[] values = {
			String.valueOf(hotelChain.getId()),
			hotelChain.getNewTime().getString(),
			hotelChain.getNewDisplayUserStr(),
			hotelChain.getEditTime().getString(),
			hotelChain.getEditDisplayUserStr()
		};

		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.HotelChain.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);

		// size up the dialog because the labels are very long
		infoDialog.setSize(new Point(300, 150));

		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == hotelChainModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (hotelChain != null) {
					hotelChain = hotelChainModel.getHotelChain( hotelChain.getId() );
					if (hotelChain != null) {
						setEntity(hotelChain);
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
	protected String getName() {
		String name = null;

		if (hotelChain != null) {
			name = hotelChain.getName();
		}

		if (StringHelper.isEmpty(name)) {
			name = I18N.HotelChainEditor_NewName;
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.HotelChainEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return hotelChain.getId() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (hotelChain != null && hotelChain.getId() != null) {
			hotelChainModel.refresh(hotelChain.getId());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				hotelChain = hotelChainModel.getHotelChain( hotelChain.getId() );
				if (hotelChain != null) {
					setEntity(hotelChain);
				}
			}
		}
	}

}
