package de.regasus.event.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.data.BankVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class EventBankGroup extends Group {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Text bankOwnerText;
	private Text bankNameText;
	private Text bankIdentifierCodeText;
	private Text bankAccountNoText;
	private Text ibanText;
	private Text bicText;
	private Text transferPattern1Text;
	private Text transferPattern2Text;
	private Text transferPattern3Text;

	// *
	// * Widgets
	// **************************************************************************


	public EventBankGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		setText(I18N.EventEditor_BankConnection);

		final int numColumns = 2;
		setLayout( new GridLayout(numColumns, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		// bankOwner
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( Bank.BANK_OWNER.getString() );

			bankOwnerText = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(bankOwnerText);
		}

		// bankName
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( Bank.BANK_NAME.getString() );

			bankNameText = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(bankNameText);
		}

		// iban
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( Bank.IBAN.getString() );

			ibanText = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(ibanText);
		}

		// bic
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( Bank.BIC.getString() );

			bicText = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(bicText);
		}

		SWTHelper.horizontalLine(this);

		// bankIdentifierCode
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(ContactLabel.bankIdentifierCode_short.getString());

			bankIdentifierCodeText = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(bankIdentifierCodeText);
		}

		// bankAccountNo
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(ContactLabel.bankAccountNumber_short.getString());

			bankAccountNoText = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(bankAccountNoText);
		}

		SWTHelper.verticalSpace(this);

		// transferPattern1
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(ParticipantLabel.Event_TransferPattern.getString());

			transferPattern1Text = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(transferPattern1Text);
		}

		// transferPattern2
		{
			new Label(this, SWT.NONE);

			transferPattern2Text = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(transferPattern2Text);
		}

		// transferPattern3
		{
			new Label(this, SWT.NONE);

			transferPattern3Text = new Text(this, SWT.BORDER);
			textGridDataFactory.applyTo(transferPattern3Text);
		}

		addModifyListenerToWidgets();
	}


	private void addModifyListenerToWidgets() {
		bankOwnerText.addModifyListener(modifySupport);
		bankNameText.addModifyListener(modifySupport);
		ibanText.addModifyListener(modifySupport);
		bicText.addModifyListener(modifySupport);
		bankIdentifierCodeText.addModifyListener(modifySupport);
		bankAccountNoText.addModifyListener(modifySupport);
		transferPattern1Text.addModifyListener(modifySupport);
		transferPattern2Text.addModifyListener(modifySupport);
		transferPattern3Text.addModifyListener(modifySupport);
	}


	public void setEvent(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						bankOwnerText.setText( avoidNull(eventVO.getBankVO().getBankOwner()) );
						bankNameText.setText( avoidNull(eventVO.getBankVO().getBankName()) );
						bankIdentifierCodeText.setText( avoidNull(eventVO.getBankVO().getBankIdentifierCode()) );
						bankAccountNoText.setText( avoidNull(eventVO.getBankVO().getBankAccountNumber()) );
						ibanText.setText( avoidNull(eventVO.getBankVO().getIban()) );
						bicText.setText( avoidNull(eventVO.getBankVO().getBic()) );
						transferPattern1Text.setText( avoidNull(eventVO.getTransferPattern1()) );
						transferPattern2Text.setText( avoidNull(eventVO.getTransferPattern2()) );
						transferPattern3Text.setText( avoidNull(eventVO.getTransferPattern3()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			BankVO bankVO = eventVO.getBankVO();
			bankVO.setBankOwner( bankOwnerText.getText() );
			bankVO.setBankName( bankNameText.getText() );
			bankVO.setBankIdentifierCode( bankIdentifierCodeText.getText() );
			bankVO.setBankAccountNumber( bankAccountNoText.getText() );
			bankVO.setIban( ibanText.getText() );
			bankVO.setBic( bicText.getText() );

			eventVO.setTransferPattern1( transferPattern1Text.getText() );
			eventVO.setTransferPattern2( transferPattern2Text.getText() );
			eventVO.setTransferPattern3( transferPattern3Text.getText() );
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
