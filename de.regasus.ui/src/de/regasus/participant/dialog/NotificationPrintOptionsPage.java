package de.regasus.participant.dialog;

import java.util.Collection;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.report.od.DocumentFormat;
import com.lambdalogic.report.oo.OpenOfficeHelper;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.widget.PrinterCombo;

import de.regasus.common.combo.OpenOfficeFormatCombo;
import com.lambdalogic.util.rcp.UtilI18N;

public class NotificationPrintOptionsPage extends WizardPage implements TemplateChangeListener {

	public static final String NAME = "NotificationPrintOptionsPage";


	private static boolean markProgBooksAsConfirmedLastValue = true;
	private static boolean markHotelBooksAsConfirmedLastValue = true;

	private Collection<String> selectedTemplateExtensions;


	private Button openRadioButton;
	private Button printRadioButton;
	private PrinterCombo printerCombo;

	private Button markProgBooksAsConfirmedButton;
	private Button markHotelBooksAsConfirmedButton;

	private OpenOfficeFormatCombo openOfficeFormatCombo;


	public NotificationPrintOptionsPage() {
		super(NAME);
		setTitle(UtilI18N.Options);
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));


		Label formatLabel = new Label(composite, SWT.NONE);
		formatLabel.setText(UtilI18N.Format);

		openOfficeFormatCombo = new OpenOfficeFormatCombo(composite, SWT.READ_ONLY);


		new Label(composite, SWT.NONE); // Dummy for vertical gap

		openRadioButton = new Button(composite, SWT.RADIO);
		openRadioButton.setText(ParticipantLabel.Show.getString());
		openRadioButton.setSelection(true);

		// "Einzeldateien erzeugen und direkt an folgenden Drucker senden"

		printRadioButton = new Button(composite, SWT.RADIO);
		printRadioButton.setText(ParticipantLabel.SingleDocument.getString());



		printerCombo = new PrinterCombo(composite, SWT.READ_ONLY);

		new Label(composite, SWT.NONE); // Dummy for vertical gap

		// Programmpunktbuchungen als benachrichtigt markieren
		markProgBooksAsConfirmedButton = new Button(composite, SWT.CHECK);
		markProgBooksAsConfirmedButton.setText(
			ParticipantLabel.MarkProgramPointBookingsAsConfirmed.getString()
		);
		markProgBooksAsConfirmedButton.setSelection(markProgBooksAsConfirmedLastValue);
		markProgBooksAsConfirmedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				markProgBooksAsConfirmedLastValue = markProgBooksAsConfirmedButton.getSelection();
			}

		});

		// Hotelbuchungen als benachrichtigt markieren
		markHotelBooksAsConfirmedButton = new Button(composite, SWT.CHECK);
		markHotelBooksAsConfirmedButton.setText(ParticipantLabel.MarkHotelBookingsAsConfirmed.getString());
		markHotelBooksAsConfirmedButton.setSelection(markHotelBooksAsConfirmedLastValue);
		markHotelBooksAsConfirmedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				markHotelBooksAsConfirmedLastValue = markHotelBooksAsConfirmedButton.getSelection();
			}
		});

		setControl(composite);
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			// collect all DocumentFormats
			Collection<DocumentFormat> documentFormats = CollectionsHelper.createHashSet();
			for (String extension : selectedTemplateExtensions) {
				DocumentFormat documentFormat = OpenOfficeHelper.getDocumentFormatByFileExtension(extension);
				documentFormats.add(documentFormat);
			}

			openOfficeFormatCombo.setTemplateFormats(documentFormats);
		}
		super.setVisible(visible);
	}


	public boolean shouldPrint() {
		return printRadioButton.getSelection();
	}


	public String getPrinterName() {
		return printerCombo.getText();
	}


	public boolean isMarkProgrammeBookingsAsConfirmed() {
		return markProgBooksAsConfirmedButton.getSelection();
	}


	public boolean isMarkHotelBookingsAsConfirmedButton() {
		return markHotelBooksAsConfirmedButton.getSelection();
	}


	public DocumentFormat getSelectedFormat() {
		return openOfficeFormatCombo.getFormat();
	}


	@Override
	public void templateChanged(Collection<String> fileExtensions) {
		selectedTemplateExtensions = fileExtensions;
	}

}
