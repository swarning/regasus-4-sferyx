package de.regasus.finance.customeraccount.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.CustomerAccountVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.finance.CustomerAccountModel;
import de.regasus.finance.FinanceI18N;
import de.regasus.ui.Activator;

public class CustomerAccountEditor extends AbstractEditor<CustomerAccountEditorInput> implements
	IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "CustomerAccountEditor"; 

	// the entity
	private CustomerAccountVO customerAccountVO;

	// the model
	private CustomerAccountModel customerAccountModel;

	// **************************************************************************
	// * Widgets
	// *

	private Text numberText;

	private Text nameText;

	private MultiLineText descriptionText;


	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		String key = editorInput.getKey();

		// get models
		customerAccountModel = CustomerAccountModel.getInstance();

		if (key != null) {
			/* Get the entity from the Model before registration as listener at it.
			 * So, if an Exception occurs when getting the entity, we don't register as listener. 
			 * look at MIRCP-1129
			 */
			
			// get entity
			customerAccountVO = customerAccountModel.getCustomerAccountVO(key);
			
			// register at model
			customerAccountModel.addListener(this, key);
		}
		else {
			// create empty entity
			customerAccountVO = new CustomerAccountVO();
		}
	}


	@Override
	public void dispose() {
		if (customerAccountModel != null && customerAccountVO.getPK() != null) {
			try {
				customerAccountModel.removeListener(this, customerAccountVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(CustomerAccountVO customerAccountVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		customerAccountVO = customerAccountVO.clone();
		}
		this.customerAccountVO = customerAccountVO;

		syncWidgetsToEntity();
	}

	
	@Override
	protected String getTypeName() {
		return InvoiceLabel.CustomerAccount.getString();
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
			
			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout(2, false));

			
			// Number
			final Label numberLabel = new Label(mainComposite, SWT.NONE);
			numberLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			numberLabel.setText(UtilI18N.Number);
			SWTHelper.makeBold(numberLabel);

			numberText = new Text(mainComposite, SWT.BORDER);
			numberText.setTextLimit(CustomerAccountVO.MAX_LENGTH_NO);
			// First make the font bold, then compute the width, cause bold fonts have different metrics
			SWTHelper.makeBold(numberText);
			GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			layoutData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(
				numberText, 
				numberText.getTextLimit()
			);
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
			setEntity(customerAccountVO);

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
				customerAccountVO = customerAccountModel.create(customerAccountVO);

				create = false;
				
				// observe the CountryModel
				customerAccountModel.addListener(this, customerAccountVO.getNo());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(customerAccountVO.getPK());

				// set new entity
				setEntity(customerAccountVO);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				customerAccountModel.update(customerAccountVO);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			
			// Delete PK in entity, to recover the new-state for further save operations.
			if (create) {
				customerAccountVO.setNo(null);
			}
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (customerAccountVO != null) {
			syncExecInParentDisplay(new Runnable() {
				public void run() {
					try {
						numberText.setText( StringHelper.avoidNull(customerAccountVO.getPK()) );
						// disable numberText if cost center is not new
						numberText.setEnabled(isNew());

						nameText.setText( StringHelper.avoidNull(customerAccountVO.getName()) );
						descriptionText.setText( StringHelper.avoidNull(customerAccountVO.getDescription()) );

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
		if (customerAccountVO != null) {
			customerAccountVO.setNo(numberText.getText());
			customerAccountVO.setName(nameText.getText());
			customerAccountVO.setDescription(descriptionText.getText());
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		numberText.addModifyListener(listener);
		nameText.addModifyListener(listener);
		descriptionText.addModifyListener(listener);
	}


	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == customerAccountModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (customerAccountVO != null) {
					customerAccountVO = customerAccountModel.getCustomerAccountVO(customerAccountVO.getPK());
					if (customerAccountVO != null) {
						setEntity(customerAccountVO);
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
		if (customerAccountVO != null && customerAccountVO.getPK() != null) {
			customerAccountModel.refresh(customerAccountVO.getPK());
			
			
			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				customerAccountVO = customerAccountModel.getCustomerAccountVO(customerAccountVO.getPK());
				if (customerAccountVO != null) {
					setEntity(customerAccountVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		StringBuilder name = new StringBuilder(50);
		
		if (customerAccountVO.getNo() != null) {
			name.append(customerAccountVO.getNo());
			
			if (StringHelper.isNotEmpty(customerAccountVO.getName())) {
				name.append(" (").append(customerAccountVO.getName()).append(")");
			}
		}
		else {
			name.append(FinanceI18N.CustomerAccount_Editor_NewName);
		}
		
		return name.toString();
	}


	@Override
	protected String getToolTipText() {
		return FinanceI18N.CustomerAccount_Editor_DefaultToolTip;
	}


	public boolean isNew() {
		return customerAccountVO.getPK() == null;
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
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(customerAccountVO.getPK()),
			customerAccountVO.getName(),
			formatHelper.formatDateTime(customerAccountVO.getNewTime()),
			customerAccountVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(customerAccountVO.getEditTime()),
			customerAccountVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			InvoiceLabel.CustomerAccountNo.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
