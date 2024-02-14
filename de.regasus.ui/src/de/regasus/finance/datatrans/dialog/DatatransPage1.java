package de.regasus.finance.datatrans.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CreditCardAlias;

import de.regasus.I18N;
import de.regasus.common.composite.CreditCardAliasGroup;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class DatatransPage1 extends WizardPage {

	public static final String NAME = "DatatransPage1"; 

	private Button useAliasButton;
	private Button dontUseAliasButton;
	
	private CreditCardAliasGroup creditCardAliasGroup;

	private CreditCardAlias creditCardAlias;
	

	public DatatransPage1(CreditCardAlias creditCardAlias) {
		super(NAME);
		this.creditCardAlias = creditCardAlias;

		setTitle(I18N.DatatransPage1_Title);
		setDescription(I18N.DatatransPage1_Description);
	}
	

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		Composite aliasButtonComposite = new Composite(composite, SWT.NONE);
		aliasButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		aliasButtonComposite.setLayout(new RowLayout());
		
		useAliasButton = new Button(aliasButtonComposite, SWT.RADIO);
		useAliasButton.setText(I18N.DatatransPage1_UseAliasButton);
		useAliasButton.setSelection(true);
		
		dontUseAliasButton = new Button(aliasButtonComposite, SWT.RADIO);
		dontUseAliasButton.setText(I18N.DatatransPage1_DontUseAliasButton);

		
		try {
			creditCardAliasGroup = new CreditCardAliasGroup(composite, SWT.NONE);
			creditCardAliasGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			creditCardAliasGroup.setCreditCardAlias(creditCardAlias);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		setControl(composite);
	}
	
	
	public CreditCardAlias getCreditCardAlias() {
		creditCardAliasGroup.syncEntityToWidgets();
		return creditCardAliasGroup.getCreditCardAlias();
	}

	
	public boolean isUseAlias() {
		return useAliasButton.getSelection();
	}
	
}
