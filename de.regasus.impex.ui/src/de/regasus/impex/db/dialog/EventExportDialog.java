package de.regasus.impex.db.dialog;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.common.CommonI18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.impex.pref.ExportPreference;
import de.regasus.event.EventTableComposite;
import de.regasus.impex.EventExportSettings;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;


public class EventExportDialog extends TitleAreaDialog {

	private ExportPreference preference;

	private Long initiallySelectedEventPK;
	private EventVO selectedEvent;

	private File file;


	// widgets
	private Button okButton;
	private EventTableComposite eventTableComposite;
	private FileSelectionComposite fileSelectionComposite;

	private Button photoButton;
	private Button participantCorrespondenceButton;
	private Button participantFileButton;


	public EventExportDialog(Shell parentShell) throws Exception {
		super(parentShell);

		preference = ExportPreference.getInstance();
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(ImpexI18N.EventExportDialog_Title);
		setMessage(ImpexI18N.EventExportDialog_Message);

		Composite area = (Composite) super.createDialogArea(parent);

		try {
			area.setLayout(new GridLayout());

			// set initial Event
			List<Long> initSelectedEventPKs = new ArrayList<>(1);
			if (initiallySelectedEventPK != null) {
				initSelectedEventPKs.add(initiallySelectedEventPK);
			}

			eventTableComposite = new EventTableComposite(
				area,
				null,	// hideEventPKs
				initSelectedEventPKs,
				false,	// multiSelection
				SWT.NONE
			);
			eventTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			// observe table
			eventTableComposite.addModifyListener( new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateEventSelection();
				}
			});


			/*
			 * EventExportSettings (Check Boxes)
			 */
			Composite settingsComposite = new Composite(area, SWT.NONE);
			settingsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			buildSettings(settingsComposite);

			/*
			 * file selection
			 */
			fileSelectionComposite = new FileSelectionComposite(area, SWT.SAVE);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fileSelectionComposite.setFilterExtensions(new String[]{"*.zip", "*.*"});

			/* set initial File
			 * Do this after observing fileSelectionComposite!
			 * Otherwise setting the initial File won't be recognized and selectedFile remains empty.
			 */
			fileSelectionComposite.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					file = fileSelectionComposite.getFile();
					updateButtonStatus();
				}
			});


			updateEventSelection();
			proposeFile();

			// update OK Button after initializing the table and the file widget
			updateButtonStatus();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return area;
	}


	private void buildSettings(Composite parent) {
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);

		photoButton = new Button(parent, SWT.CHECK);
		photoButton.setText( CommonI18N.Photos.getString() );
		photoButton.setSelection( preference.isIncludePhoto() );

		participantCorrespondenceButton = new Button(parent, SWT.CHECK);
		participantCorrespondenceButton.setText( ParticipantLabel.ParticipantCorrespondence.getString() );
		participantCorrespondenceButton.setSelection( preference.isIncludeParticipantCorrespondence() );

		participantFileButton = new Button(parent, SWT.CHECK);
		participantFileButton.setText( ParticipantLabel.ParticipantFiles.getString() );
		participantFileButton.setSelection( preference.isIncludeParticipantFile() );
	}


	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
//		shell.setText(UtilI18N.Question);
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, ImpexI18N.ExportBtn, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		updateButtonStatus();
	}


	@Override
	public boolean close() {
		preference.setDir( fileSelectionComposite.getFile().getParent() );
		preference.setIncludePhoto( photoButton.getSelection() );
		preference.setIncludeParticipantCorrespondence( participantCorrespondenceButton.getSelection() );
		preference.setIncludeParticipantFile( participantFileButton.getSelection() );
		preference.save();

		return super.close();
	}


	private void updateEventSelection() {
		List<EventVO> selectedEvents = eventTableComposite.getSelectedEvents();
		if ( notEmpty(selectedEvents) ) {
			selectedEvent = selectedEvents.get(0);
		}
		else {
			selectedEvent = null;
		}

		proposeFile();
		updateButtonStatus();
	}


	private void proposeFile() {
		String fileName = "export.zip";
		if (selectedEvent != null) {
			fileName = selectedEvent.getMnemonic() + ".zip";
		}

		String dir = preference.getDir();
		File currentFile = fileSelectionComposite.getFile();
		if (currentFile != null && currentFile.getParent() != null) {
			dir = currentFile.getParent();
		}

		file = new File(dir, fileName);
		fileSelectionComposite.setFile(file);
	}


	private void updateButtonStatus() {
		if (okButton != null) {
			boolean enabled = selectedEvent != null && file != null;
			okButton.setEnabled(enabled);
		}
	}


	public void setInitiallySelectedEvent(Long eventPK) {
		initiallySelectedEventPK = eventPK;
	}


	public EventVO getEventVO() {
		return selectedEvent;
	}


	public File getFile() {
		return file;
	}


	public boolean isIncludedPhoto() {
		return preference.isIncludePhoto();
	}


	public boolean isIncludedParticipantCorrespondence() {
		return preference.isIncludeParticipantCorrespondence();
	}


	public boolean isIncludedParticipantFile() {
		return preference.isIncludeParticipantFile();
	}


	public EventExportSettings getEventExportSettings() {
		EventExportSettings settings = new EventExportSettings();

		settings.setIncludePhoto( isIncludedPhoto() );
		settings.setIncludeParticipantCorrespondence( isIncludedParticipantCorrespondence() );
		settings.setIncludeParticipantFile( isIncludedParticipantFile() );

		return settings;
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}

}
