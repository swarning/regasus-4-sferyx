package de.regasus.finance.costunit.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.CostCenterVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.NumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.finance.CostCenter2Model;
import de.regasus.finance.FinanceI18N;
import de.regasus.ui.Activator;

public class CostUnitEditor extends AbstractEditor<CostUnitEditorInput> implements
	IRefreshableEditorPart, CacheModelListener<Integer> {

	public static final String ID = "CostUnitEditor"; 

	// the entity
	private CostCenterVO costCenterVO;

	// the model
	private CostCenter2Model costCenter2Model;

	// **************************************************************************
	// * Widgets
	// *

	private NumberText numberText;

	private Text nameText;

	private MultiLineText descriptionText;


	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Integer key = editorInput.getKey();

		// get models
		costCenter2Model = CostCenter2Model.getInstance();

		if (key != null) {
			/* Get the entity from the Model before registration as listener at it.
			 * So, if an Exception occurs when getting the entity, we don't register as listener. 
			 * look at MIRCP-1129
			 */
			
			// get entity
			costCenterVO = costCenter2Model.getCostCenterVO(key);
			
			// register at model
			costCenter2Model.addListener(this, key);
		}
		else {
			// create empty entity
			costCenterVO = new CostCenterVO();
		}
	}


	@Override
	public void dispose() {
		if (costCenter2Model != null && costCenterVO.getPK() != null) {
			try {
				costCenter2Model.removeListener(this, costCenterVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(CostCenterVO costCenterVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		costCenterVO = costCenterVO.clone();
		}
		this.costCenterVO = costCenterVO;

		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return InvoiceLabel.CostUnit.getString();
	}
	
//	@Override
//	protected String getInfoButtonToolTipText() {
//		return FinanceI18N.ProgrammePointEditor_InfoButtonToolTip;
//	}

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

			// Number
			final Label numberLabel = new Label(mainComposite, SWT.NONE);
			numberLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			numberLabel.setText(UtilI18N.Number);
			SWTHelper.makeBold(numberLabel);

			numberText = new NumberText(mainComposite, SWT.BORDER);
			numberText.setMinValue(0);
			numberText.setMaxValue(99999999);
			numberText.setLeadingZeroDigits(5);
			numberText.setNullAllowed(true);
			numberText.setMessage("00000");
			// First make the font bold, then compute the width, cause bold fonts have different metrics
			SWTHelper.makeBold(numberText);
			GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			layoutData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(numberText, 8);
			numberText.setLayoutData(layoutData);

			// Name
			final Label nameLabel = new Label(mainComposite, SWT.NONE);
			nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			nameLabel.setText(UtilI18N.Name);

			nameText = new Text(mainComposite, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// Description

			Label descriptionLabel = new Label(mainComposite, SWT.NONE);
			descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			descriptionLabel.setText(UtilI18N.Description);

			descriptionText = new MultiLineText(mainComposite, SWT.BORDER);
			descriptionText.setMinLineCount(5);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
			descriptionText.setLayoutData(gridData);

			// sync widgets and groups to the entity
			setEntity(costCenterVO);

			// after sync add this as ModifyListener to all widgets and groups
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
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				costCenterVO = costCenter2Model.create(costCenterVO);

				create = false;
				
				// observe the CountryModel
				costCenter2Model.addListener(this, costCenterVO.getPK());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(costCenterVO.getPK());

				// set new entity
				setEntity(costCenterVO);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				costCenter2Model.update(costCenterVO);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			
			// Delete PK in entity, to recover the new-state for further save operations.
			if (create) {
				costCenterVO.setNo(null);
			}
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (costCenterVO != null) {
			syncExecInParentDisplay(new Runnable() {
				public void run() {
					try {
						numberText.setValue(costCenterVO.getPK());
						// disable numberText if cost center is not new
						numberText.setEnabled(isNew());

						nameText.setText(StringHelper.avoidNull(costCenterVO.getName()));

						descriptionText.setText(StringHelper.avoidNull(costCenterVO.getDescription()));

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
		if (costCenterVO != null) {
			costCenterVO.setNo(numberText.getValue());
			costCenterVO.setName(nameText.getText());
			costCenterVO.setDescription(StringHelper.trim(descriptionText.getText()));
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		numberText.addModifyListener(listener);
		nameText.addModifyListener(listener);
		descriptionText.addModifyListener(listener);
	}


	public void dataChange(CacheModelEvent<Integer> event) {
		try {
			if (event.getSource() == costCenter2Model) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (costCenterVO != null) {
					costCenterVO = costCenter2Model.getCostCenterVO(costCenterVO.getPK());
					if (costCenterVO != null) {
						setEntity(costCenterVO);
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


	public void refresh() throws Exception {
		if (costCenterVO != null && costCenterVO.getPK() != null) {
			costCenter2Model.refresh(costCenterVO.getPK());
			
			
			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				costCenterVO = costCenter2Model.getCostCenterVO(costCenterVO.getPK());
				if (costCenterVO != null) {
					setEntity(costCenterVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		
		if (costCenterVO.getNo() != null) {
			name = StringHelper.padLeft(String.valueOf(costCenterVO.getNo()), '0', 5);
			
			if (StringHelper.isNotEmpty(costCenterVO.getName())) {
				name += " (" + costCenterVO.getName() + ")";
			}
		}
		else {
			name = FinanceI18N.CostUnit_Editor_NewName;
		}
		
		return name;
	}


	@Override
	protected String getToolTipText() {
		return FinanceI18N.CostUnit_Editor_DefaultToolTip;
	}


	public boolean isNew() {
		return costCenterVO.getPK() == null;
	}

	
	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.Name,
			UtilI18N.EditDateTime,
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(costCenterVO.getPK()),
			costCenterVO.getName(),
			formatHelper.formatDateTime(costCenterVO.getEditTime()),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			InvoiceLabel.CostUnit.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}
	
}
