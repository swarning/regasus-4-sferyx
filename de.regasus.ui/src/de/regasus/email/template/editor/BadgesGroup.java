package de.regasus.email.template.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

/**
 * A composite used in the {@link EmailTemplateEditor} to control what dynamic attachments are to be added.
 */
public class BadgesGroup extends Group {

	// Widgets
	private Button withBadgesButton;
	private Button alsoDoubleBadgesButton;
	private Button badgesEvenIfUnpaidButton;

	// Other Attributes
	private EmailTemplate emailTemplate;


	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);



	public BadgesGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setLayout(new GridLayout(2, false));
		setText(EmailLabel.Badges.getString());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// With badges (Namensschilder)
		{
			withBadgesButton = new Button(this, SWT.CHECK);
			withBadgesButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			withBadgesButton.addSelectionListener(modifySupport);

			Label withBadgesLabel = new Label(this, SWT.WRAP | SWT.LEFT);
			withBadgesLabel.setText(EmailLabel.WithBadges.getString());
			withBadgesLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}


		// Ignore if badge has already been printed
		{
			alsoDoubleBadgesButton = new Button(this, SWT.CHECK);
			alsoDoubleBadgesButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			alsoDoubleBadgesButton.addSelectionListener(modifySupport);

			Label printAlsoDoubleBadgesLabel = new Label(this, SWT.WRAP | SWT.LEFT);
			printAlsoDoubleBadgesLabel.setText(EmailLabel.PrintAlsoDoubleBadges.getString());
			printAlsoDoubleBadgesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, false));
		}

		// Print even if unpaid bookings
		{
			badgesEvenIfUnpaidButton = new Button(this, SWT.CHECK);
			badgesEvenIfUnpaidButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			badgesEvenIfUnpaidButton.addSelectionListener(modifySupport);

			Label printBadgesEvenIfUnpaidLabel = new Label(this, SWT.WRAP | SWT.LEFT);
			printBadgesEvenIfUnpaidLabel.setText(EmailLabel.PrintBadgesEvenIfUnpaid.getString());
			printBadgesEvenIfUnpaidLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		// Listener wiring

		withBadgesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStates();
			}
		});
	}


	/**
	 * Stores the widgets' contents to the entity.
	 */
	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		// Badges
		emailTemplate.setWithBadges(withBadgesButton.getSelection());
		emailTemplate.setBadgesEvenIfUnpaid(badgesEvenIfUnpaidButton.getSelection());
		emailTemplate.setAlsoDoubleBadges(alsoDoubleBadgesButton.getSelection());
	}


	public void setEmailTemplate(EmailTemplate emailTemplate) {
		this.emailTemplate = emailTemplate;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (emailTemplate != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// Badges
						withBadgesButton.setSelection(emailTemplate.isWithBadges());
						alsoDoubleBadgesButton.setSelection(emailTemplate.isAlsoDoubleBadges());
						badgesEvenIfUnpaidButton.setSelection(emailTemplate.isBadgesEvenIfUnpaid());
						updateStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	/**
	 * When at least one of the buttons is selected that invoices are to be attached (whether open or closed doesn't
	 * matter), the user may also select an invoice number range and a file format.
	 *
	 */
	private void updateStates() {
		boolean withBadges = withBadgesButton.getSelection();
		alsoDoubleBadgesButton.setEnabled(withBadges);
		badgesEvenIfUnpaidButton.setEnabled(withBadges);
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
