package de.regasus.email.template.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.dnd.TextDropListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.template.variables.VariablesHelper;
import de.regasus.ui.Activator;

/**
 * An SWT group that allows adding, editing and removing of files which are to be sent as attachments.
 * <ul>
 * <li>To / An</li>
 * <li>Cc / Kopie</li>
 * <li>Bcc / Blindkopie</li>
 * <li>From / Von</li>
 * <li>ReplyTo / Antwort an</li>
 * <li>Subject / Betreff (this is no address)</li>
 * </ul>
 * of an {@link EmailTemplate}. The group is used in the {@link EmailTemplateEditor} and could be expected to appear in
 * a wizard, too.
 *
 * @author manfred
 *
 */
public class AddressesGroup extends Group {

	// *************************************************************************
	// * Widgets
	// *

	private Text toAddr;

	private Text ccAddr;

	private Text bccAddr;

	private Text fromAddr;

	private Text replyToAddr;

	private Text subject;

	// *************************************************************************
	// * Other Attributes
	// *

	private EmailTemplate emailTemplate;

	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);


	// *************************************************************************
	// * Constructor
	// *

	public AddressesGroup(Composite parent, int style) {
		super(parent, style);

		setText(EmailLabel.Addresses.getString());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		// ToAddr
		{
			Label toAddrLabel = new Label(this, SWT.NONE);
			toAddrLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			toAddrLabel.setText(EmailLabel.ToAddr.getString());
			toAddrLabel.setToolTipText(EmailLabel.ToAddrDescription.getString());
			SWTHelper.makeBold(toAddrLabel);

			toAddr = new Text(this, SWT.BORDER);
			toAddr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			toAddr.setText(VariablesHelper.getDefaultToAddrVariable());
			initDragNDrop(toAddr);
			SWTHelper.makeBold(toAddr);

			toAddr.addModifyListener(modifySupport);
		}

		// CcAddr
		{
			Label ccAddrLabel = new Label(this, SWT.NONE);
			ccAddrLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			ccAddrLabel.setText(EmailLabel.CcAddr.getString());
			ccAddrLabel.setToolTipText(EmailLabel.CcAddrDescription.getString());

			ccAddr = new Text(this, SWT.BORDER);
			ccAddr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			initDragNDrop(ccAddr);

			ccAddr.addModifyListener(modifySupport);
		}

		// BccAddr
		{
			Label bccAddrLabel = new Label(this, SWT.NONE);
			bccAddrLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			bccAddrLabel.setText(EmailLabel.BccAddr.getString());
			bccAddrLabel.setToolTipText(EmailLabel.BccAddrDescription.getString());

			bccAddr = new Text(this, SWT.BORDER);
			bccAddr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			initDragNDrop(bccAddr);

			bccAddr.addModifyListener(modifySupport);
		}

		// FromAddr
		{
			Label fromAddrLabel = new Label(this, SWT.NONE);
			fromAddrLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			fromAddrLabel.setText(EmailLabel.FromAddr.getString());
			fromAddrLabel.setToolTipText(EmailLabel.FromAddrDescription.getString());
			SWTHelper.makeBold(fromAddrLabel);

			fromAddr = new Text(this, SWT.BORDER);
			fromAddr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			initDragNDrop(fromAddr);
			SWTHelper.makeBold(fromAddr);

			fromAddr.addModifyListener(modifySupport);
		}

		// ReplyToAddr
		{
			Label replyToAddrLabel = new Label(this, SWT.NONE);
			replyToAddrLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			replyToAddrLabel.setText(EmailLabel.ReplyToAddr.getString());
			replyToAddrLabel.setToolTipText(EmailLabel.ReplyToAddrDescription.getString());

			replyToAddr = new Text(this, SWT.BORDER);
			replyToAddr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			initDragNDrop(replyToAddr);

			replyToAddr.addModifyListener(modifySupport);
		}

		// Dummy label as separator
		Label separatorLabel = new Label(this, SWT.NONE);
		separatorLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

		// Subject
		{
			Label subjectLabel = new Label(this, SWT.NONE);
			subjectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			subjectLabel.setText(EmailLabel.Subject.getString());
			subjectLabel.setToolTipText(EmailLabel.SubjectDescription.getString());
			SWTHelper.makeBold(subjectLabel);

			subject = new Text(this, SWT.BORDER);
			subject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			initDragNDrop(subject);
			SWTHelper.makeBold(subject);

			subject.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	// **************************************************************************
	// * Synching and Modifying
	// *

	/**
	 * Stores the widgets' contents to the entity.
	 */

	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		emailTemplate.setToAddr(StringHelper.trim(toAddr.getText()));
		emailTemplate.setCcAddr(StringHelper.trim(ccAddr.getText()));
		emailTemplate.setBccAddr(StringHelper.trim(bccAddr.getText()));
		emailTemplate.setFromAddr(StringHelper.trim(fromAddr.getText()));
		emailTemplate.setReplyToAddr(StringHelper.trim(replyToAddr.getText()));
		emailTemplate.setSubject(StringHelper.trim(subject.getText()));
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
						toAddr.setText(StringHelper.avoidNull(emailTemplate.getToAddr()));
						ccAddr.setText(StringHelper.avoidNull(emailTemplate.getCcAddr()));
						bccAddr.setText(StringHelper.avoidNull(emailTemplate.getBccAddr()));
						fromAddr.setText(StringHelper.avoidNull(emailTemplate.getFromAddr()));
						replyToAddr.setText(StringHelper.avoidNull(emailTemplate.getReplyToAddr()));
						subject.setText(StringHelper.avoidNull(emailTemplate.getSubject()));
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	/**
	 * The given Text widget should get inserted any dragged text (from the {@link VariablesTableComposite}.
	 *
	 * @param text
	 */
	private void initDragNDrop(Text text) {
		DropTarget target = new DropTarget(text, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		target.addDropListener(new TextDropListener(text));
	}


	public void setEditable(boolean b) {
		bccAddr.setEditable(b);
		ccAddr.setEditable(b);
		fromAddr.setEditable(b);
		replyToAddr.setEditable(b);
		subject.setEditable(b);
		toAddr.setEditable(b);
	}

}
