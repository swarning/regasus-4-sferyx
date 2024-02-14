package de.regasus.common.composite;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.CreditCardAlias;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import com.lambdalogic.util.rcp.ICustomWizard;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.MonthComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.combo.CreditCardTypeCombo;
import de.regasus.event.EventModel;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.datatrans.dialog.DatatransAliasWizard;
import de.regasus.finance.paymentsystem.dialog.PayEngineAliasWizard;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class CreditCardAliasGroup extends Group {

	private CreditCardAlias creditCardAlias;
	private Long participantID;

	private ModifySupport modifySupport = new ModifySupport(this);


	// Widgets
	private CreditCardTypeCombo creditCardTypeCombo;
	private Text maskedNumberText;
	private Text aliasText;
	private MonthComposite expirationMonthComposite;

	private Label typeLabel;
	private Label maskedNumberLabel;
	private Label aliasLabel;
	private Label expirationDateLabel;

	private Button newAliasButton;


	public CreditCardAliasGroup(Composite parent, int style) throws Exception {
		this(parent, style, AbstractPerson.CREDIT_CARD_ALIAS.getString());
	}

	/**
	 * Create the composite
	 *
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public CreditCardAliasGroup(Composite parent, int style, String title) throws Exception {
		super(parent, style);

		setLayout(new GridLayout(2, false));
		setText(title);

		{
			typeLabel = new Label(this, SWT.NONE);
			final GridData gd_typeLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			typeLabel.setLayoutData(gd_typeLabel);
			typeLabel.setText( CreditCardAlias.CREDIT_CARD_TYPE.getLabel() );

			creditCardTypeCombo = new CreditCardTypeCombo(this, SWT.READ_ONLY);
			creditCardTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			creditCardTypeCombo.addModifyListener(modifySupport);
		}

		{
			maskedNumberLabel = new Label(this, SWT.NONE);
			maskedNumberLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			maskedNumberLabel.setText( CreditCardAlias.MASKED_NUMBER.getLabel() );

			maskedNumberText = new Text(this, SWT.BORDER);
			maskedNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			maskedNumberText.setTextLimit( CreditCardAlias.MASKED_NUMBER.getMaxLength() );

			maskedNumberText.addModifyListener(modifySupport);
		}

		{
			aliasLabel = new Label(this, SWT.NONE);
			aliasLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			aliasLabel.setText( CreditCardAlias.ALIAS.getLabel() );

			// We show the alias in the history, so we can show it here as well
			aliasText = new Text(this, SWT.BORDER);
			aliasText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			aliasText.setTextLimit( CreditCardAlias.ALIAS.getMaxLength() );

			aliasText.addModifyListener(modifySupport);
		}

		{
			expirationDateLabel = new Label(this, SWT.NONE);
			expirationDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			expirationDateLabel.setText( CreditCardAlias.EXPIRATION.getLabel() );

			expirationMonthComposite = new MonthComposite(this, SWT.NONE);
			expirationMonthComposite.setMonthLabelText(KernelLabel.Month.getString());
			expirationMonthComposite.setYearLabelText(KernelLabel.Year.getString());
			expirationMonthComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			expirationMonthComposite.addModifyListener(modifySupport);
		}

		Label ruler = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		ruler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		{
			newAliasButton = new Button(this, SWT.PUSH);
			newAliasButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
			newAliasButton.setText(I18N.CreditCardAliasGroup_NewAliasButton);
			newAliasButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					requestNewAlias();
				}
			});

		}
	}


	private void requestNewAlias() {
		try {
			boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(participantID);
			if (editorSaveCkeckOK) {
				Participant participant = ParticipantModel.getInstance().getParticipant(participantID);
				EventVO eventVO = EventModel.getInstance().getEventVO(participant.getEventId());

				ICustomWizard paymentWizard = null;
				if (eventVO.getPaymentSystem() == PaymentSystem.DATATRANS) {
					paymentWizard = new DatatransAliasWizard(participant);
				}
				else if (eventVO.getPaymentSystem() == PaymentSystem.PAYENGINE) {
					paymentWizard = PayEngineAliasWizard.getInstance(participant, eventVO);
				}

				CustomWizardDialog wizardDialog = new CustomWizardDialog(getShell(), paymentWizard);
				wizardDialog.setFinishButtonText(UtilI18N.Close);

				paymentWizard.setCustomWizardDialog(wizardDialog);

				wizardDialog.create();

				Point preferredSize = paymentWizard.getPreferredSize();
				wizardDialog.getShell().setSize(preferredSize.x, preferredSize.y);

				wizardDialog.open();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void setEnabled(boolean enabled) {
		typeLabel.setEnabled(enabled);
		creditCardTypeCombo.setEnabled(enabled);
		maskedNumberLabel.setEnabled(enabled);
		maskedNumberText.setEnabled(enabled);
		aliasLabel.setEnabled(enabled);
		aliasText.setEnabled(enabled);
		expirationDateLabel.setEnabled(enabled);
		expirationMonthComposite.setEnabled(enabled);
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

	private void syncWidgetsToEntity() {
		syncWidgetsToEntityInternal(creditCardAlias, true);
	}

	/**
	 * Puts the data of the given credit card alias into the widgets, regardless whether
	 * it's THE ENTITY or just a pasted thing from the clipboard. In the latter case,
	 * events will not be avoided in order to make the editor dirty.
	 */
	private void syncWidgetsToEntityInternal(final CreditCardAlias creditCardAlias, final boolean avoidEvents) {
		if (creditCardAlias != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (avoidEvents) {
							modifySupport.setEnabled(false);
						}
						syncCreditCardTypeComboToEntity(creditCardAlias.getCreditCardTypePK());
						maskedNumberText.setText(StringHelper.avoidNull(creditCardAlias.getMaskedNumber()));
						aliasText.setText(StringHelper.avoidNull(creditCardAlias.getAlias()));
						expirationMonthComposite.setDate(creditCardAlias.getExpiration());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						if (avoidEvents) {
							modifySupport.setEnabled(true);
						}
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		syncEntityToWidgetsInternal(creditCardAlias);
	}


	/**
	 * Puts the data of widgets into the given credit card, regardless whether
	 * it's THE ENTITY or just a thing to be copied to the clipboard.
	 */
	private void syncEntityToWidgetsInternal(CreditCardAlias creditCardAlias) {
		if (creditCardAlias != null) {
			creditCardAlias.setCreditCardTypePK(creditCardTypeCombo.getPK());
			creditCardAlias.setMaskedNumber(StringHelper.trim(maskedNumberText.getText()));
			creditCardAlias.setAlias(StringHelper.trim(aliasText.getText()));

			try {
				// Although the month composite had a getDate-Method, I set
				// month and year individually for making use of the functionality
				// that adapts any two-digit year.

				Integer month = expirationMonthComposite.getMonth();
				Integer year = expirationMonthComposite.getYear();

				if (month != null && year != null) {
					creditCardAlias.setExpiration(month, year);
				}
				else {
					// You CAN delete an expiration date
					creditCardAlias.setExpiration((Date) null);
				}
			}
			catch (InvalidValuesException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}

	/**
	 * Synchronisiert creditCardTypeComboViewer mit dem Entity (CreeditCard)
	 * @param pkLong
	 */
	private void syncCreditCardTypeComboToEntity(Long pkLong) {
		if (pkLong != null) {
			creditCardTypeCombo.setPK(pkLong);
		}
		else {
			creditCardTypeCombo.setPK(null);
		}
	}


	public void setCreditCardAlias(CreditCardAlias creditCardAlias) {
		this.creditCardAlias = creditCardAlias;
		syncWidgetsToEntity();
	}


	public void setParticipantID(Long participantID) {
		this.participantID = participantID;

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				newAliasButton.setEnabled(CreditCardAliasGroup.this.participantID != null);
			}
		});
	}


	public CreditCardAlias getCreditCardAlias() {
		return creditCardAlias;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
