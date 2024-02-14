package com.lambdalogic.util.rcp.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MessageDetailsDialog extends Dialog {

	private String title;
	private String message;
	private String details;

	
	private Composite mainComposite;
	
	
	/**
	 * The SWT text control that displays the details.
	 */
	private Text detailsControl;

	/**
	 * Indicates whether the details viewer is currently created.
	 */
	private boolean listCreated = false;

	/**
	 * The Details button.
	 */
	private Button detailsButton;

	/**
	 * Reserve room for this many list items.
	 */
	private static final int LIST_ITEM_COUNT = 7;

	
	/**
	 * int value of an SWT icon, e.g. SWT.ICON_INFORMATION or SWT.ICON_WARNING.
	 */
	private int swtIcon = SWT.NONE;
	
	
	
	protected MessageDetailsDialog(
		String title,
		String message,
		String details,
		int swtIcon
	) {
		super((Shell) null);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.title = title;
		this.message = message;
		this.details = details;
		this.swtIcon = swtIcon;
	}

	
	public static void open(
		String title,
		String message,
		String details
	) {
		MessageDetailsDialog dialog = new MessageDetailsDialog(
			title,
			message,
			details,
			SWT.NONE
		);
		dialog.open();
	}

	
	public static void openInformation(
		String title,
		String message,
		String details
	) {
		MessageDetailsDialog dialog = new MessageDetailsDialog(
			title,
			message,
			details,
			SWT.ICON_INFORMATION
		);
		dialog.open();
	}

	
	public static void openWarning(
		String title,
		String message,
		String details
	) {
		MessageDetailsDialog dialog = new MessageDetailsDialog(
			title,
			message,
			details,
			SWT.ICON_WARNING
		);
		dialog.open();
	}

	
	public static void openError(
		String title,
		String message,
		String details
	) {
		MessageDetailsDialog dialog = new MessageDetailsDialog(
			title,
			message,
			details,
			SWT.ICON_ERROR
		);
		dialog.open();
	}
	
	
	@Override
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
       
//        if (titleImage != null) {
//			shell.setImage(titleImage);
//		}
	}

	
	@Override
	protected Control createContents(Composite parent) {
		// create the top level composite for the dialog
		mainComposite = new Composite(parent, 0);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(mainComposite);
		// initialize the dialog units
		initializeDialogUnits(mainComposite);
		// create the dialog area and button bar
		dialogArea = createDialogArea(mainComposite);
		buttonBar = createButtonBar(mainComposite);
				
		return mainComposite;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		dialogComposite.setLayout(layout);
		dialogComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		applyDialogFont(dialogComposite);

		// icon
		Label imageLabel = new Label(dialogComposite, SWT.NULL);
		if (swtIcon != SWT.NONE) {
			Image image = getSWTImage(swtIcon);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
		}
		
		final Label label = new Label(dialogComposite, SWT.NONE);
		final GridData gd_label = new GridData();
		gd_label.horizontalIndent = 5;
		label.setLayoutData(gd_label);
		label.setText(message);
		
		return dialogComposite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.marginLeft = 450;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(
			GridData.HORIZONTAL_ALIGN_END | 
			GridData.VERTICAL_ALIGN_CENTER
		);
		buttonComposite.setLayoutData(data);
		buttonComposite.setFont(parent.getFont());
		
		// Add the buttons to the button bar.
		createButtonsForButtonBar(buttonComposite);
		return buttonComposite;
	}

	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		detailsButton = createButton(
			parent, 
			IDialogConstants.DETAILS_ID,
			IDialogConstants.SHOW_DETAILS_LABEL, 
			false
		);
		
		createButton(
			parent,
			IDialogConstants.OK_ID,
			IDialogConstants.OK_LABEL,
			true
		);
	}

	

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.DETAILS_ID) {
			// was the details button pressed?
			toggleDetailsArea();
		}
		else {
			super.buttonPressed(buttonId);
		}
	}


	/**
	 * Get an <code>Image</code> from the provide SWT image constant.
	 * 
	 * @param imageID
	 *            the SWT image constant
	 * @return image the image
	 */
	private Image getSWTImage(final int imageID) {
		Shell shell = getShell();
		final Display display;
		if (shell == null) {
			shell = getParentShell();
		}
		if (shell == null) {
			display = Display.getDefault();
		} else {
			display = shell.getDisplay();
		}

		final Image[] image = new Image[1];
		display.syncExec(new Runnable() {
			public void run() {
				image[0] = display.getSystemImage(imageID);
			}
		});

		return image[0];

	}
	
	
	/**
	 * Toggles the unfolding of the details area. This is triggered by the user
	 * pressing the details button.
	 */
	private Point smallSize = null;
	private Point bigSize = null;
	
	private void toggleDetailsArea() {
		Point windowSize = getShell().getSize();
		
		Point newSize = null;
		if (listCreated) {
			bigSize = windowSize;
			detailsControl.dispose();
			listCreated = false;
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
			
			newSize = smallSize;
		}
		else {
			smallSize = windowSize;
			
			detailsControl = createDropDownArea();
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);

			newSize = bigSize;
			if (newSize == null) {
				newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
		}

		getShell().setSize(
			new Point(
				windowSize.x, 
				newSize.y
			)
		);
	}

	
	/**
	 * Create this dialog's drop-down list component.
	 * 
	 * @param parent
	 *            the parent composite
	 * @return the drop-down list component
	 */
	protected Text createDropDownArea(/*Composite parent*/) {
		// create the list
		detailsControl = new Text(mainComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		// fill the details control
		detailsControl.setText(details);
		
		GridData data = new GridData(
			GridData.HORIZONTAL_ALIGN_FILL | 
			GridData.GRAB_HORIZONTAL | 
			GridData.VERTICAL_ALIGN_FILL | 
			GridData.GRAB_VERTICAL |
			GridData.FILL_BOTH
		);
		
// 	Finally got rid of the monstrous and obnoxiously blinking tooltip 
//		detailsControl.setToolTipText(getDetails());
		
		//data.heightHint = detailsText.getItemHeight() * LIST_ITEM_COUNT;
		data.heightHint = 30 * LIST_ITEM_COUNT;
		data.horizontalSpan = 2;
		detailsControl.setLayoutData(data);
		detailsControl.setFont(mainComposite.getFont());
		
//		Menu copyMenu = new Menu(list);
//		MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
//		copyItem.addSelectionListener(new SelectionListener() {
//			/*
//			 * @see SelectionListener.widgetSelected (SelectionEvent)
//			 */
//			public void widgetSelected(SelectionEvent e) {
//				copyToClipboard();
//			}
//
//			/*
//			 * @see SelectionListener.widgetDefaultSelected(SelectionEvent)
//			 */
//			public void widgetDefaultSelected(SelectionEvent e) {
//				copyToClipboard();
//			}
//		});
//		copyItem.setText(JFaceResources.getString("copy")); 
//		list.setMenu(copyMenu);
		listCreated = true;
		return detailsControl;
	}

}
