package de.regasus.finance.currency.editor;

import java.math.BigDecimal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.CurrencyVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.finance.CurrencyModel;
import de.regasus.finance.FinanceI18N;
import de.regasus.ui.Activator;

public class CurrencyEditor
extends AbstractEditor<CurrencyEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "CurrencyEditor";

	// the entity
	private CurrencyVO currencyVO;

	// the model
	private CurrencyModel currencyModel;

	// Widgets
	private Text nameText;
	private Text descriptionText;
	private DecimalNumberText ratioToEuroNumberText;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		String key = editorInput.getKey();

		// get model
		currencyModel = CurrencyModel.getInstance();

		if (key != null) {
			/* Get the entity from the Model before registration as listener at it.
			 * So, if an Exception occurs when getting the entity, we don't register as listener.
			 * look at MIRCP-1129
			 */

			// get entity
			currencyVO = currencyModel.getCurrencyVO(key);

			// register at model
			currencyModel.addListener(this, key);
		}
		else {
			// create empty entity
			currencyVO = new CurrencyVO();
		}
	}


	@Override
	public void dispose() {
		if (currencyModel != null && currencyVO.getID() != null) {
			try {
				currencyModel.removeListener(this, currencyVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(CurrencyVO currencyVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		currencyVO = currencyVO.clone();
		}
		this.currencyVO = currencyVO;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return InvoiceLabel.Currency.getString();
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = SWTHelper.createScrolledContentComposite(parent);
			mainComposite.setLayout(new GridLayout(2, false));


			// Currency Name
			final Label nameLabel = new Label(mainComposite, SWT.NONE);
			SWTHelper.makeBold(nameLabel);
			nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			nameLabel.setText(UtilI18N.Name);

			nameText = new Text(mainComposite, SWT.BORDER);
			nameText.setTextLimit(CurrencyVO.MAX_LENGTH_ID);

			// First make the font bold, then compute the width, cause bold fonts have different metrics
			SWTHelper.makeBold(nameText);
			GridData nameLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			nameLayoutData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(nameText, 3);
			nameText.setLayoutData(nameLayoutData);

			// Description
			Label descriptionLabel = new Label(mainComposite, SWT.NONE);
			SWTHelper.makeBold(descriptionLabel);
			descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			descriptionLabel.setText(UtilI18N.Description);

			descriptionText = new Text(mainComposite, SWT.BORDER);
			SWTHelper.makeBold(descriptionText);
			descriptionText.setTextLimit(CurrencyVO.MAX_LENGTH_DESCRIPTION);
			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// Ratio To Euro
			Label ratioToEuroLabel = new Label(mainComposite, SWT.NONE);
			SWTHelper.makeBold(ratioToEuroLabel);
			ratioToEuroLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			ratioToEuroLabel.setText(InvoiceLabel.Ratio_To_EURO.getString());

			ratioToEuroNumberText = new DecimalNumberText(mainComposite, SWT.BORDER);
			ratioToEuroNumberText.setFractionDigits(4);
			ratioToEuroNumberText.setNullAllowed(false);
			ratioToEuroNumberText.setMaxValue(CurrencyVO.MAX_RATIO_TO_EURO);
			ratioToEuroNumberText.setMinValue(CurrencyVO.MIN_RATIO_TO_EURO);
			SWTHelper.makeBold(ratioToEuroNumberText);
			ratioToEuroNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// set data
			setEntity(currencyVO);

			// after sync add this as ModifyListener to widgets
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
				currencyVO = currencyModel.create(currencyVO);

				create = false;

				// observe the CountryModel
				currencyModel.addListener(this, currencyVO.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(currencyVO.getID());

				// set new entity
				setEntity(currencyVO);
			}
			else {
				/* Save the entity.
				 * On success setEntity will be called indirectly in dataChange(),
				 * else an Exception will be thrown.
				 * The result of update() must not be assigned to CurrencyVO,
				 * because this will happen in setEntity() and there it may be cloned!
				 * Assigning CurrencyVO here would overwrite the cloned value with
				 * the one from the model. Therefore we would have inconsistent data!
				 */
				currencyModel.update(currencyVO);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);

			// Delete PK in entity, to recover the new-state for further save operations.
			if (create) {
				currencyVO.setID(null);
			}
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (currencyVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						nameText.setText(StringHelper.avoidNull(currencyVO.getID()));
						// disable nameText if currency is not new
						nameText.setEnabled(isNew());

						descriptionText.setText(StringHelper.avoidNull(currencyVO.getDescription()));
						ratioToEuroNumberText.setValue(currencyVO.getRatioToEuro());

						String name = getName();

						// set editor title
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
		if (currencyVO != null) {
			currencyVO.setID(nameText.getText());
			currencyVO.setDescription(StringHelper.trim(descriptionText.getText()));

			// ratioToEuro
			BigDecimal ratioToEuroValue = ratioToEuroNumberText.getValue();
			if (ratioToEuroValue == null) {
				ratioToEuroValue = BigDecimal.ZERO;
			}
			currencyVO.setRatioToEuro(ratioToEuroValue);
		}
	}


	private void addModifyListener(ModifyListener listener) {
		nameText.addModifyListener(listener);
		descriptionText.addModifyListener(listener);
		ratioToEuroNumberText.addModifyListener(listener);
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == currencyModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (currencyVO != null) {
					currencyVO = currencyModel.getCurrencyVO(currencyVO.getID());
					if (currencyVO != null) {
						setEntity(currencyVO);
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
		if (currencyVO != null && currencyVO.getID() != null) {
			currencyModel.refresh(currencyVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				currencyVO = currencyModel.getCurrencyVO(currencyVO.getID());
				if (currencyVO != null) {
					setEntity(currencyVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (currencyVO != null && currencyVO.getID() != null) {
			name = currencyVO.getID();
		}
		if (StringHelper.isEmpty(name)) {
			name = FinanceI18N.Currency_Editor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return FinanceI18N.Currency_Editor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return currencyVO.getID() == null;
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
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(currencyVO.getID()),
			formatHelper.formatDateTime(currencyVO.getNewTime()),
			currencyVO.getNewUser(),
			formatHelper.formatDateTime(currencyVO.getEditTime()),
			currencyVO.getEditUser()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			InvoiceLabel.Currency.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
