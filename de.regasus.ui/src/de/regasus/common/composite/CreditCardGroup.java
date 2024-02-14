package de.regasus.common.composite;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.CreditCard;
import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.CreditCardHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.AutoCorrectionWidgetHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.MonthComposite;
import com.lambdalogic.util.rcp.widget.CreditCardCheckNoText;
import com.lambdalogic.util.rcp.widget.CreditCardText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.combo.CreditCardTypeCombo;
import de.regasus.core.ui.dnd.CopyPasteButtonComposite;
import de.regasus.core.ui.dnd.CreditCardTransfer;
import de.regasus.ui.Activator;

public class CreditCardGroup extends Group  {

	private CreditCard creditCard;

	private ModifySupport modifySupport = new ModifySupport(this);


	// Widgets
	private CreditCardTypeCombo creditCardTypeCombo;
	private Text ownerText;
	private CreditCardText numberText;
	private Text checkSumText;
	private MonthComposite expirationMonthComposite;

	private Label typeLabel;
	private Label ownerLabel;
	private Label numberLabel;
	private Label checkSumLabel;
	private Label expirationDateLabel;

	private CopyPasteButtonComposite cpbc;


	public CreditCardGroup(Composite parent, int style) throws Exception {
		this(parent, style, AbstractPerson.CREDIT_CARD.getString() );
	}

	/**
	 * Create the composite
	 *
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public CreditCardGroup(Composite parent, int style, String title) throws Exception {
		super(parent, style);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		setLayout(gridLayout);
		setText(title);

		{
			typeLabel = new Label(this, SWT.NONE);
			final GridData gd_typeLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			typeLabel.setLayoutData(gd_typeLabel);
			typeLabel.setText( CreditCard.CREDIT_CARD_TYPE.getLabel() );

			creditCardTypeCombo = new CreditCardTypeCombo(this, SWT.READ_ONLY);
			creditCardTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

			creditCardTypeCombo.addModifyListener(modifySupport);
		}

		{
			ownerLabel = new Label(this, SWT.NONE);
			ownerLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			ownerLabel.setText( CreditCard.OWNER.getLabel() );

			ownerText = new Text(this, SWT.BORDER);
			final GridData gd_ownerText = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			ownerText.setLayoutData(gd_ownerText);
			ownerText.setTextLimit( CreditCard.OWNER.getMaxLength() );

			ownerText.addModifyListener(modifySupport);
		}

		{
			numberLabel = new Label(this, SWT.NONE);
			numberLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			numberLabel.setText( CreditCard.NUMBER.getLabel() );

			numberText = new CreditCardText(this, SWT.BORDER);
			// calculate and set the maximum width
			numberText.setText("9999999999999999");
			final GridData gd_numberText = new GridData(SWT.FILL, SWT.FILL, true, false);
			numberText.setLayoutData(gd_numberText);
			gd_numberText.minimumWidth = numberText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gd_numberText.widthHint = gd_numberText.minimumWidth;
			numberText.setText("");
			numberText.setTextLimit( CreditCard.NUMBER.getMaxLength() );

			numberText.addModifyListener(modifySupport);
		}

		{
			checkSumLabel = new Label(this, SWT.NONE);
			final GridData gd_checkSumLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			gd_checkSumLabel.horizontalIndent = 5;
			checkSumLabel.setLayoutData(gd_checkSumLabel);
			checkSumLabel.setText( CreditCard.CHECK_SUM.getLabel() );

			checkSumText = new CreditCardCheckNoText(this, SWT.BORDER);
			// calculate and set the maximum width
			checkSumText.setText("999");
			final GridData gd_checkSumText = new GridData();
			checkSumText.setLayoutData(gd_checkSumText);
			gd_checkSumText.widthHint = checkSumText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			gd_checkSumText.minimumWidth = gd_checkSumText.widthHint;
			checkSumText.setTextLimit( CreditCard.CHECK_SUM.getMaxLength() );

			checkSumText.setText("");

			checkSumText.addModifyListener(modifySupport);
		}

		{
			expirationDateLabel = new Label(this, SWT.NONE);
			expirationDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			expirationDateLabel.setText( CreditCard.EXPIRATION.getLabel() );

			expirationMonthComposite = new MonthComposite(this, SWT.NONE);
			expirationMonthComposite.setMonthLabelText(KernelLabel.Month.getString());
			expirationMonthComposite.setYearLabelText(KernelLabel.Year.getString());
			expirationMonthComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			expirationMonthComposite.addModifyListener(modifySupport);
		}

		// --------- Copy Paste Buttons ----------------

		final Label separatorLabel = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));


		cpbc = new CopyPasteButtonComposite(this, SWT.NONE);
		cpbc.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));

		cpbc.getCopyButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					copyToClipboad();
				}
				catch (Exception e2) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e2);
				}
			}
		});

		cpbc.getPasteButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					pasteFromClipboad();
				}
				catch (Exception e2) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e2);
				}
			}
		});

	}

	@Override
	public void setEnabled(boolean enabled) {
		checkSumText.setEnabled(enabled);
		checkSumLabel.setEnabled(enabled);
		creditCardTypeCombo.setEnabled(enabled);
		typeLabel.setEnabled(enabled);
		expirationMonthComposite.setEnabled(enabled);
		expirationDateLabel.setEnabled(enabled);
		numberText.setEnabled(enabled);
		numberLabel.setEnabled(enabled);
		ownerText.setEnabled(enabled);
		ownerLabel.setEnabled(enabled);
		cpbc.getCopyButton().setEnabled(enabled);
		cpbc.getPasteButton().setEnabled(enabled);
	}


	protected void pasteFromClipboad() {

		Clipboard clipboard = new Clipboard(Display.getDefault());

		Object contents = clipboard.getContents(CreditCardTransfer.getInstance());
		if (contents != null && contents instanceof CreditCard) {
			CreditCard pastedCreditCard = (CreditCard) contents;
			syncWidgetsToEntityInternal(pastedCreditCard, false);
		}
		clipboard.dispose();
	}


	protected void copyToClipboad() throws Exception {

		CreditCard creditCardToBeCopied = new CreditCard();

		syncEntityToWidgetsInternal(creditCardToBeCopied);

		StringBuffer textTransferContent = new StringBuffer();
		textTransferContent.append(StringHelper.avoidNull(creditCardToBeCopied.getOwner()));
		textTransferContent.append("\n");

		CreditCardTypeVO creditCardTypeVO = creditCardTypeCombo.getEntity();
		textTransferContent.append(creditCardTypeVO != null ? creditCardTypeVO.getName() : "");

		textTransferContent.append("\n");
		textTransferContent.append(CreditCardHelper.replaceAllButLast4DigitsByStar(creditCardToBeCopied.getNumber()));
		textTransferContent.append("\n");
		textTransferContent.append(creditCardToBeCopied.getExpirationAsString());


		Clipboard clipboard = new Clipboard(Display.getDefault());
		clipboard.setContents(
			new Object[] { creditCardToBeCopied, textTransferContent.toString() },
			new Transfer[] { CreditCardTransfer.getInstance(), TextTransfer.getInstance() }
		);
		clipboard.dispose();


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
		syncWidgetsToEntityInternal(creditCard, true);
	}

	/**
	 * Puts the data of the given credit card into the widgets, regardless whether
	 * it's THE ENTITY or just a pasted thing from the clipboard. In the latter case,
	 * events will not be avoided in order to make the editor dirty.
	 */
	private void syncWidgetsToEntityInternal(final CreditCard creditCard, final boolean avoidEvents) {
		if (creditCard != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (avoidEvents) {
							modifySupport.setEnabled(false);
						}
						syncCreditCardTypeComboToEntity(creditCard.getCreditCardTypePK());
						ownerText.setText(StringHelper.avoidNull(creditCard.getOwner()));
						numberText.setText(StringHelper.avoidNull(creditCard.getNumber()));
						checkSumText.setText(StringHelper.avoidNull(creditCard.getCheckSum()));
						expirationMonthComposite.setDate(creditCard.getExpiration());
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
		syncEntityToWidgetsInternal(creditCard);
	}


	/**
	 * Puts the data of widgets into the given credit card, regardless whether
	 * it's THE ENTITY or just a thing to be copied to the clipboard.
	 */
	private void syncEntityToWidgetsInternal(CreditCard aCreditCard) {
		if (aCreditCard != null) {
			aCreditCard.setCreditCardTypePK(creditCardTypeCombo.getPK());
			aCreditCard.setOwner(StringHelper.trim(ownerText.getText()));
			aCreditCard.setNumber(StringHelper.trim(numberText.getText()));
			aCreditCard.setCheckSum(StringHelper.trim(checkSumText.getText()));
			try {
				// Although the month composite had a getDate-Method, I set
				// month and year individually for making use of the functionality
				// that adapts any two-digit year.

				Integer month = expirationMonthComposite.getMonth();
				Integer year = expirationMonthComposite.getYear();

				if (month != null && year != null) {
					aCreditCard.setExpiration(month, year);
				}
				else {
					// You CAN delete an expiration date
					aCreditCard.setExpiration((Date) null);
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


	public void setCreditCard(CreditCard creditCard) {
		this.creditCard = creditCard;
		syncWidgetsToEntity();
	}


	public CreditCard getCreditCard() {
		return creditCard;
	}


	/**
	 * Corrects the user input of credit card owner automatically.
	 */
	public void autoCorrection() {
		AutoCorrectionWidgetHelper.correctAndSet(ownerText);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
