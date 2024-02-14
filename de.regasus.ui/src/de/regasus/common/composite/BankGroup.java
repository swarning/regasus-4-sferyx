package de.regasus.common.composite;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.util.rcp.AutoCorrectionWidgetHelper;
import com.lambdalogic.util.rcp.EntityGroup;

//REFERENCE
@SuppressWarnings("unused")
public class BankGroup extends EntityGroup<Bank> {

	private final int COL_COUNT = 2;


	// widgets
	private Text bankOwnerText;
	private Text bankAccountNumberText;
	private Text bankIdentifierCodeText;
	private Text bankNameText;
	private Text ibanText;
	private Text bicText;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public BankGroup(Composite parent, int style) throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style);

		setText( ContactLabel.Banking.getString() );
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		bankOwnerText = widgetBuilder.fieldMetadata(Bank.BANK_OWNER).createTextWithLabel();
		bankNameText = widgetBuilder.fieldMetadata(Bank.BANK_NAME).createTextWithLabel();
		ibanText = widgetBuilder.fieldMetadata(Bank.IBAN).createTextWithLabel();
		bicText = widgetBuilder.fieldMetadata(Bank.BIC).createTextWithLabel();

		widgetBuilder.horizontalLine();

		bankAccountNumberText = widgetBuilder.fieldMetadata(Bank.BANK_ACCOUNT_NUMBER).createTextWithLabel();
		bankIdentifierCodeText = widgetBuilder.fieldMetadata(Bank.BANK_IDENTIFIER_CODE).createTextWithLabel();
	}


	public Bank getBank() {
		return entity;
	}


	public void setBank(Bank bank) {
		setEntity(bank);
	}


	/**
	 * Corrects the user input of owner and bank name automatically.
	 */
	public void autoCorrection() {
		AutoCorrectionWidgetHelper.correctAndSet(bankOwnerText);
		AutoCorrectionWidgetHelper.correctAndSet(bankNameText);
	}

}
