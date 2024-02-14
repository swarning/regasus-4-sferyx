package de.regasus.portal.pagelayout.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.portal.PageLayoutLink;
import de.regasus.portal.PortalI18N;


public class PageLayoutLinkDialog extends TitleAreaDialog {

	private Long portalId;
	private String language;

	private String link;
	private String description;

	// widgets
	private Button okButton;
	private TabFolder tabFolder;
	private PageLayoutLinkDialogFileComposite fileComposite;
	private PageLayoutLinkDialogPageComposite pageComposite;
	private Text linkText;
	private Text descriptionText;


	public PageLayoutLinkDialog(Shell shell, Long portalId, String language) {
		super(shell);
		this.portalId = portalId;
		this.language = language;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(I18N.PageLayoutLinkDialog_Title);
		setMessage(I18N.PageLayoutLinkDialog_Message);

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite mainComposite = new Composite(dialogArea, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout( new GridLayout(2, false) );

		// tabFolder
		tabFolder = new TabFolder(mainComposite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(tabFolder);

		// Files Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(UtilI18N.Files);

			fileComposite = new PageLayoutLinkDialogFileComposite(tabFolder, SWT.NONE, language);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(fileComposite);
			fileComposite.setPortalId(portalId);
			fileComposite.addModifyListener(fileCompositeModifyListener);

			tabItem.setControl(fileComposite);
		}

		// Pages Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(PortalI18N.Pages.getString());

			pageComposite = new PageLayoutLinkDialogPageComposite(tabFolder, SWT.NONE);
			pageComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			pageComposite.setPortalId(portalId);
			pageComposite.addModifyListener(pageCompositeModifyListener);

			tabItem.setControl(pageComposite);
		}


		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// description
		{
			Label label = new Label(mainComposite, SWT.NONE);
			label.setText(UtilI18N.Description);
			labelGridDataFactory.applyTo(label);

			descriptionText = new Text(mainComposite, SWT.BORDER);
			textGridDataFactory.applyTo(descriptionText);
			SWTHelper.disableTextWidget(descriptionText);
		}

		// link
		{
			Label label = new Label(mainComposite, SWT.NONE);
			label.setText( PageLayoutLink.GLOBAL_LINK.getString() );
			labelGridDataFactory.applyTo(label);

			linkText = new Text(mainComposite, SWT.BORDER);
			textGridDataFactory.applyTo(linkText);
			SWTHelper.disableTextWidget(linkText);
		}

		return dialogArea;
	}


	private ModifyListener fileCompositeModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			link = fileComposite.getLink();
			description = fileComposite.getDescription();

			linkText.setText( avoidNull(link) );
			descriptionText.setText( avoidNull(description) );

			updateButtonStatus();
		}
	};


	private ModifyListener pageCompositeModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			link = pageComposite.getLink();
			description = pageComposite.getDescription();

			linkText.setText(link);
			descriptionText.setText(description);

			updateButtonStatus();
		}
	};


	private void updateButtonStatus() {
		okButton.setEnabled( StringHelper.isNotEmpty(link) );
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}


//	@Override
//	protected void configureShell(Shell newShell) {
//		super.configureShell(newShell);
//		newShell.setText(EmailI18N.CitySelectionDialog_ShellText);
//	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK) {
//			isSection = sectionButton.getSelection();
//			isTextComponent = textComponentButton.getSelection();
//			isFieldComponent = fieldComponentButton.getSelection();
//			isFileComponent = fileComponentButton.getSelection();
//			isProgrammeBookingComponent = programmeBookingComponentButton.getSelection();
		}
		super.buttonPressed(buttonId);
	}


	public String getLink() {
		return link;
	}


	public String getDescription() {
		return description;
	}

}
