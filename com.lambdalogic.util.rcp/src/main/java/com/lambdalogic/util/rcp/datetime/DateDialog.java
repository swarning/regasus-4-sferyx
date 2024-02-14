package com.lambdalogic.util.rcp.datetime;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.Images;

public class DateDialog extends Dialog {

	private DateTime dateWidget;

	private Image okImage = null;

	private Image cancelImage = null;

	private Shell shell;

	private Point dialogAreaSize;
	private Point buttonBarSize;
	private Point rightUpper;

	private LocalDate localDate;


	public static LocalDate openDateDialog(Shell parentShell, LocalDate initialDate, Point rightUpper) {
		DateDialog dateDialog = new DateDialog(parentShell, initialDate, rightUpper);

		// make the dialog modal
		dateDialog.setShellStyle(SWT.APPLICATION_MODAL);
		dateDialog.setBlockOnOpen(true);

		int result = dateDialog.open();
		if (result == Window.OK) {
			return dateDialog.getLocalDate();
		}
		return null;
	}



	protected DateDialog(Shell parentShell, LocalDate initialDate, Point rightUpper) {
		super(parentShell);

		localDate = initialDate;
		if (localDate == null) {
			localDate = LocalDate.now();
		}

		this.rightUpper = rightUpper;
	}


	@Override
	public boolean close() {
		if (okImage != null) {
			okImage.dispose();
		}

		if (cancelImage != null) {
			cancelImage.dispose();
		}

		return super.close();
	}



	@Override
	protected void configureShell(Shell newShell) {
		this.shell = newShell;
		super.configureShell(newShell);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		dateWidget = new DateTime(container, SWT.CALENDAR | SWT.BORDER);

		int year = localDate.getYear();
		int month = localDate.getMonthValue();
		int date = localDate.getDayOfMonth();

		// Avoid negative years after typo, see comments for MIRCP-1310
		if (year < 1900) {
			year = new GregorianCalendar().get(Calendar.YEAR);
		}

		dateWidget.setDate(year, month - 1, date);

		dateWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int day = dateWidget.getDay();
				int month = dateWidget.getMonth() + 1;
				int year = dateWidget.getYear();

				DateDialog.this.localDate = LocalDate.of(year, month, day);
			}
		});

		container.pack();
		dialogAreaSize = container.getSize();

		return container;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setImage(getOkImage());
		okButton.setText("");

		GridData okGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		okButton.setLayoutData(okGridData);

		Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		cancelButton.setImage(getCancelImage());
		cancelButton.setText("");

		GridData cancelGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		cancelButton.setLayoutData(cancelGridData);

		// set size
		parent.pack();
		buttonBarSize = parent.getSize();
		System.out.println("buttonBarSize: " + buttonBarSize);

		int x = dialogAreaSize.x;
		int y = dialogAreaSize.y + buttonBarSize.y;
		Point dialogSize = new Point(x, y);

		shell.setSize(dialogSize);

		// set location
		Point size = shell.getSize();
		Point location = new Point(
			rightUpper.x - size.x,
			rightUpper.y
		);

		shell.setLocation(location);
	}


	private Image getOkImage() {
		// load the calendar image
		Image image = null;
		if (PlatformUI.isWorkbenchRunning()) {
			image = Images.get(Images.OK);
		}
		else {
			image = new Image(getShell().getDisplay(), "icons/" + Images.OK);

			// save the image to dispose it later
			okImage = image;
		}
		return image;
	}


	private Image getCancelImage() {
		// load the calendar image
		Image image = null;
		if (PlatformUI.isWorkbenchRunning()) {
			image = Images.get(Images.CANCEL);
		}
		else {
			image = new Image(getShell().getDisplay(), "icons/" + Images.CANCEL);

			// save the image to dispose it later
			cancelImage = image;
		}
		return image;
	}


	public LocalDate getLocalDate() {
		return localDate;
	}

}
