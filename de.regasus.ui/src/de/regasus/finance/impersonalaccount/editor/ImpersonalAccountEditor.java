package de.regasus.finance.impersonalaccount.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.ImpersonalAccountVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.NumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.ImpersonalAccountModel;
import de.regasus.ui.Activator;

public class ImpersonalAccountEditor 
extends AbstractEditor<ImpersonalAccountEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Integer> {

	public static final String ID = "ImpersonalAccountEditor"; 

	// the entity
	private ImpersonalAccountVO impersonalAccountVO;

	// the model
	private ImpersonalAccountModel impersonalAccountModel;

	// **************************************************************************
	// * Widgets
	// *

	private NumberText numberText;

	private Text nameText;

	private MultiLineText descriptionText;

	private Text taxCodeText;
	
	private Button financeAccountCheckBox;


	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Integer key = editorInput.getKey();

		// get models
		impersonalAccountModel = ImpersonalAccountModel.getInstance();

		if (key != null) {
			/* Get the entity from the Model before registration as listener at it.
			 * So, if an Exception occurs when getting the entity, we don't register as listener. 
			 * look at MIRCP-1129
			 */
			
			// get entity
			impersonalAccountVO = impersonalAccountModel.getImpersonalAccountVO(key);
						
			// register at model
			impersonalAccountModel.addListener(this, key);
		}
		else {
			// create empty entity
			impersonalAccountVO = new ImpersonalAccountVO();
		}
	}


	@Override
	public void dispose() {
		if (impersonalAccountModel != null && impersonalAccountVO.getPK() != null) {
			try {
				impersonalAccountModel.removeListener(this, impersonalAccountVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ImpersonalAccountVO impersonalAccountVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		impersonalAccountVO = impersonalAccountVO.clone();
		}
		this.impersonalAccountVO = impersonalAccountVO;
		
		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return InvoiceLabel.ImpersonalAccount.getString();
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
			numberText.setMinValue( ImpersonalAccountVO.MIN_NO );
			numberText.setMaxValue( ImpersonalAccountVO.MAX_NO );
			numberText.setLeadingZeroDigits(ImpersonalAccountVO.MAX_DIGITS_NO);
			numberText.setNullAllowed(true);
			numberText.setMessage( StringHelper.padRight("0", '0', ImpersonalAccountVO.MAX_DIGITS_NO) );
			// First make the font bold, then compute the width, cause bold fonts have different metrics
			SWTHelper.makeBold(numberText);
			GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			layoutData.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(numberText, ImpersonalAccountVO.MAX_DIGITS_NO);
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
			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			// Taxcode

			Label taxCodeLabel = new Label(mainComposite, SWT.NONE);
			taxCodeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			taxCodeLabel.setText(FinanceI18N.TaxCode);

			taxCodeText = new Text(mainComposite, SWT.BORDER);
			taxCodeText.setTextLimit(ImpersonalAccountVO.MAX_LENGTH_TAX_CODE);
			GridData layoutData2 = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			layoutData2.widthHint = SWTHelper.computeTextWidgetWidthForCharCount(taxCodeText, ImpersonalAccountVO.MAX_LENGTH_TAX_CODE);
			taxCodeText.setLayoutData(layoutData2);
			
			// FinanceAccount

			Label financeAccountLabel = new Label(mainComposite, SWT.NONE);
			financeAccountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			financeAccountLabel.setText(InvoiceLabel.FinanceAccount.getString());
			financeAccountLabel.setToolTipText(InvoiceLabel.FinanceAccount_Description.getString());
			
			financeAccountCheckBox = new Button(mainComposite, SWT.CHECK);

			// sync widgets and groups to the entity
			setEntity(impersonalAccountVO);

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
				impersonalAccountVO = impersonalAccountModel.create(impersonalAccountVO);
				
				create = false;

				// observe the CountryModel
				impersonalAccountModel.addListener(this, impersonalAccountVO.getPK());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(impersonalAccountVO.getPK());

				// set new entity
				setEntity(impersonalAccountVO);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				impersonalAccountModel.update(impersonalAccountVO);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			
			// Delete PK in entity, to recover the new-state for further save operations.
			if (create) {
				impersonalAccountVO.setNo(null);
			}
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (impersonalAccountVO != null) {
			syncExecInParentDisplay(new Runnable() {
				public void run() {
					try {
						numberText.setValue(impersonalAccountVO.getPK());
						// disable numberText if cost center is not new
						numberText.setEnabled(isNew());

						nameText.setText(StringHelper.avoidNull(impersonalAccountVO.getName()));

						descriptionText.setText(StringHelper.avoidNull(impersonalAccountVO.getDescription()));

						taxCodeText.setText(StringHelper.avoidNull(impersonalAccountVO.getTaxCode()));
						
						financeAccountCheckBox.setSelection(impersonalAccountVO.isFinanceAccount());
						
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
		if (impersonalAccountVO != null) {
			impersonalAccountVO.setNo(numberText.getValue());
			impersonalAccountVO.setName(nameText.getText());
			impersonalAccountVO.setDescription(StringHelper.trim(descriptionText.getText()));
			impersonalAccountVO.setTaxCode(StringHelper.trim(taxCodeText.getText()));
			impersonalAccountVO.setFinanceAccount(financeAccountCheckBox.getSelection());
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		numberText.addModifyListener(listener);
		nameText.addModifyListener(listener);
		descriptionText.addModifyListener(listener);
		taxCodeText.addModifyListener(listener);
		
		SelectionListener listenerAdapter = new ModifyListenerAdapter(listener);
		financeAccountCheckBox.addSelectionListener(listenerAdapter);
	}


	public void dataChange(CacheModelEvent<Integer> event) {
		try {
			if (event.getSource() == impersonalAccountModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (impersonalAccountVO != null) {
					impersonalAccountVO = impersonalAccountModel.getImpersonalAccountVO(impersonalAccountVO.getPK());
					if (impersonalAccountVO != null) {
						setEntity(impersonalAccountVO);
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
		if (impersonalAccountVO != null && impersonalAccountVO.getPK() != null) {
			impersonalAccountModel.refresh(impersonalAccountVO.getPK());
			
			
			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				impersonalAccountVO = impersonalAccountModel.getImpersonalAccountVO(impersonalAccountVO.getPK());
				if (impersonalAccountVO != null) {
					setEntity(impersonalAccountVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		
		if (impersonalAccountVO.getNo() != null) {
			name = StringHelper.padLeft(String.valueOf(impersonalAccountVO.getNo()), '0', 5);
			
			if (StringHelper.isNotEmpty(impersonalAccountVO.getName())) {
				name += " (" + impersonalAccountVO.getName() + ")";
			}
		}
		else {
			name = FinanceI18N.ImpersonalAccount_Editor_NewName;
		}
		
		return name;
	}


	@Override
	protected String getToolTipText() {
		return FinanceI18N.ImpersonalAccount_Editor_DefaultToolTip;
	}


	public boolean isNew() {
		return impersonalAccountVO.getPK() == null;
	}

	
	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime, 
			UtilI18N.CreateUser, 
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

	
		FormatHelper formatHelper = new FormatHelper();
		
		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(impersonalAccountVO.getPK()),
			formatHelper.formatDateTime(impersonalAccountVO.getNewTime()), 
			impersonalAccountVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(impersonalAccountVO.getEditTime()),
			impersonalAccountVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			InvoiceLabel.ImpersonalAccount.getString() + ": " + UtilI18N.Info,
			labels, 
			values
		);
		infoDialog.open();
	}

}
