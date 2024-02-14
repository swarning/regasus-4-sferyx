package de.regasus.participant.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.composite.BankGroup;
import de.regasus.common.composite.CreditCardAliasGroup;
import de.regasus.common.composite.CreditCardGroup;

public class ParticipantBankingComposite extends LazyComposite implements DisposeListener {

	private Participant participant;

	private CreditCardGroup creditCardGroup;

	private CreditCardAliasGroup creditCardAliasGroup;

	private BankGroup bankGroup;

	private boolean withCreditCardAlias;

	private ParticipantConfigParameterSet participantConfigParameterSet;


	private ModifySupport modifySupport = new ModifySupport(this);


	public ParticipantBankingComposite(
		Composite tabFolder,
		int style,
		boolean withCreditCardAlias,
		ParticipantConfigParameterSet participantConfigParameterSet
	) {
		super(tabFolder, style);
		setLayout(new GridLayout(2, false));

		this.withCreditCardAlias = withCreditCardAlias;
		this.participantConfigParameterSet = participantConfigParameterSet;
	}


	@Override
	protected void createPartControl() throws Exception {
		if (participantConfigParameterSet.getCreditCard().isVisible()) {
			if (withCreditCardAlias) {
				// CreditCardAliasGroup
				creditCardAliasGroup = new CreditCardAliasGroup(this, SWT.NONE);
				creditCardAliasGroup.setText( Participant.CREDIT_CARD_ALIAS.getString() );
				final GridData gd_creditCardAliasGroup = new GridData(SWT.FILL, SWT.TOP, true, false);
				creditCardAliasGroup.setLayoutData(gd_creditCardAliasGroup);
				creditCardAliasGroup.setCreditCardAlias(participant.getCreditCardAlias());
				creditCardAliasGroup.setParticipantID(participant.getID());

				creditCardAliasGroup.addModifyListener(modifySupport);
			}
			else {
				// CreditCardGroup
				creditCardGroup = new CreditCardGroup(this, SWT.NONE);
				creditCardGroup.setText( Participant.CREDIT_CARD.getString() );
				final GridData gd_creditCardGroup = new GridData(SWT.FILL, SWT.TOP, true, false);
				creditCardGroup.setLayoutData(gd_creditCardGroup);
				creditCardGroup.setCreditCard(participant.getCreditCard());

				creditCardGroup.addModifyListener(modifySupport);
			}
		}

		// BankGroup
		if (participantConfigParameterSet.getBank().isVisible()) {
			bankGroup = new BankGroup(this, SWT.NONE);
			bankGroup.setText(ContactLabel.bankAccount.getString());
			final GridData gd_bankGroup = new GridData(SWT.FILL, SWT.TOP, true, false);
			bankGroup.setLayoutData(gd_bankGroup);
			bankGroup.setBank(participant.getBank());

			bankGroup.addModifyListener(modifySupport);
		}

		SWTHelper.refreshSuperiorScrollbar(this);
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


	public void setParticipant(Participant participant) {
		if (participant == null) {
			throw new IllegalArgumentException("Parameter 'participant' must not ne null");
		}

		this.participant = participant;

		if (creditCardGroup != null) {
			creditCardGroup.setCreditCard(participant.getCreditCard());
		}
		if (creditCardAliasGroup != null) {
			creditCardAliasGroup.setCreditCardAlias(participant.getCreditCardAlias());
			creditCardAliasGroup.setParticipantID(participant.getID());
		}
		if (bankGroup != null) {
			bankGroup.setBank(participant.getBank());
		}
	}


	public void syncEntityToWidgets() {
		if (participant != null) {
			if (creditCardAliasGroup != null) {
				creditCardAliasGroup.syncEntityToWidgets();
				participant.setCreditCardAlias(creditCardAliasGroup.getCreditCardAlias());
			}
			if (creditCardGroup != null) {
				creditCardGroup.syncEntityToWidgets();
				participant.setCreditCard(creditCardGroup.getCreditCard());
			}
			if (bankGroup != null) {
				bankGroup.syncEntityToWidgets();
				participant.setBank(bankGroup.getBank());
			}
		}
	}


	public void autoCorrection() {
		if (creditCardGroup != null) {
			creditCardGroup.autoCorrection();
		}
		if (bankGroup != null) {
			bankGroup.autoCorrection();
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
	}

}
