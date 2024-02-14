package de.regasus.email.template.editor;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.File;
import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventFileModel;
import de.regasus.ui.Activator;


/**
 * An SWT group that shows files to be attached and allows their addition, editing and deletion.
 */
public class NotificationTemplatesGroup extends Group implements DisposeListener {

	// *************************************************************************
	// * Widgets
	// *

	/**
	 * The JFace viewer that allows the selection of notification templates to be used.
	 */
	private CheckboxTableViewer notificationTemplateTableViewer;

	/**
	 * The table that shows the notification templates that may be used.
	 */
	private Table notificationTemplateTable;

	private Button markProgrammeBookingsAsConfirmedButton;

	private Button markHotelBookingsAsConfirmedButton;



	// *************************************************************************
	// * Other Attributes
	// *

	private EmailTemplate emailTemplate;

	private boolean notForEvent = false;

	/**
	 * The notifications which may be attached.
	 */
	private List<FileSummary> notificationTemplateList = new ArrayList<>();

	/**
	 * The listeners who are to be notified when the set of checked notificationTemplates changes
	 */
	private ModifySupport modifySupport = new ModifySupport(this);


	public NotificationTemplatesGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setText(ParticipantLabel.Notes.getString());

		setLayout(new GridLayout(2, false));

		// Vorhandene Benachrichtigungen
		Label availableNotificationsLabel = new Label(this, SWT.NONE);
		availableNotificationsLabel.setText(EmailLabel.AvailableNotifications.getString());
		availableNotificationsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		// The SWT table that shows the available notificationTemplates
		notificationTemplateTable = new Table(this, SWT.CHECK | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		notificationTemplateTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// The JFace viewer that provides text and images for the notificationTemplates
		notificationTemplateTableViewer = new CheckboxTableViewer(notificationTemplateTable);
		notificationTemplateTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonStates();
			}
		});

		notificationTemplateTableViewer.addCheckStateListener( new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				modifySupport.fire(notificationTemplateTable);
			}
		} );

		TableColumn checkTableColumn = new TableColumn(notificationTemplateTable, SWT.RIGHT, 0);
		TableColumn nameTableColumn = new TableColumn(notificationTemplateTable, SWT.LEFT, 1);
		checkTableColumn.pack();
		nameTableColumn.setWidth(300);

		notificationTemplateTableViewer.setContentProvider(new ArrayContentProvider());
		notificationTemplateTableViewer.setLabelProvider(new NotificationTemplateLabelProvider());

		// Programmpunktbuchungen als benachrichtigt markieren
		markProgrammeBookingsAsConfirmedButton = new Button(this, SWT.CHECK);
		markProgrammeBookingsAsConfirmedButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		markProgrammeBookingsAsConfirmedButton.addSelectionListener(modifySupport);

		Label markProgramPointBookingsAsConfirmedLabel = new Label(this, SWT.WRAP | SWT.LEFT);
		markProgramPointBookingsAsConfirmedLabel.setText(ParticipantLabel.MarkProgramPointBookingsAsConfirmed.getString());
		markProgramPointBookingsAsConfirmedLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Hotelbuchungen als benachrichtigt markieren
		markHotelBookingsAsConfirmedButton = new Button(this, SWT.CHECK);
		markHotelBookingsAsConfirmedButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		markHotelBookingsAsConfirmedButton.addSelectionListener(modifySupport);

		Label markHotelBookingsAsConfirmedLabel = new Label(this, SWT.WRAP | SWT.LEFT);
		markHotelBookingsAsConfirmedLabel.setText(ParticipantLabel.MarkHotelBookingsAsConfirmed.getString());
		markHotelBookingsAsConfirmedLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		updateButtonStates();
	}


	private CacheModelListener<Long> eventFileModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			syncWidgetsToEntity();
		}
	};


	public void setEmailTemplate(EmailTemplate emailTemplate) {
		if (this.emailTemplate == null) {
			EventFileModel.getInstance().addForeignKeyListener(eventFileModelListener, emailTemplate.getEventPK());
		}

		this.emailTemplate = emailTemplate;

		syncWidgetsToEntity();
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (emailTemplate != null) {
			EventFileModel.getInstance().removeForeignKeyListener(eventFileModelListener, emailTemplate.getEventPK());
		}
	}


	/**
	 * Editing and deletion is only possible when something is selected.
	 */
	protected void updateButtonStates() {
		if (!notForEvent) {
			boolean useAttachments = notificationTemplateTableViewer.getCheckedElements().length > 0;
			markHotelBookingsAsConfirmedButton.setEnabled(useAttachments);
			markProgrammeBookingsAsConfirmedButton.setEnabled(useAttachments);
		}
	}


	/**
	 * Stores the widgets' contents to the entity.
	 */
	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		if (!notForEvent) {
			Collection<FileSummary> notificationTemplateList = emailTemplate.getNotificationTemplateList();

			if (notificationTemplateList == null) {
				notificationTemplateList = new  ArrayList<>();
				emailTemplate.setNotificationTemplateList(notificationTemplateList);
			}

			notificationTemplateList.clear();

			Object[] checkedElements = notificationTemplateTableViewer.getCheckedElements();
			for (Object object : checkedElements) {
				if (object instanceof FileSummary) {
					notificationTemplateList.add((FileSummary) object);
				}
			}

			emailTemplate.setMarkHotelBookingsAsConfirmed(markHotelBookingsAsConfirmedButton.getSelection());
			emailTemplate.setMarkProgrammeBookingsAsConfirmed(
				markProgrammeBookingsAsConfirmedButton.getSelection()
			);
		}
	}


	private void syncWidgetsToEntity() {
		if (emailTemplate != null && !notForEvent) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Long eventPK = emailTemplate.getEventPK();
						if (eventPK != null) {
							// get Files (without content) from Model
							List<File> noteTemplateFileList = EventFileModel.getInstance().getNoteTemplateFiles(eventPK);

							// convert into List of File Summary
							List<FileSummary> noteTemplateFileSummaryList = noteTemplateFileList
								.stream()
								.map(file -> file.toFileSummary())
								.collect( Collectors.toList() );

							// set data to table
							notificationTemplateTableViewer.setInput(noteTemplateFileSummaryList);

							/* Get the Files referenced by the EmailTemplate.
							 * There may be Files which doesn't exist anymore,
							 * because the EmailTemplate has not been refreshed yet.
							 */

							Collection<FileSummary> currentTemplateList = emailTemplate.getNotificationTemplateList();
							Object[] checkedElements;
							if ( notEmpty(currentTemplateList) ) {
								checkedElements = noteTemplateFileSummaryList
									.stream()
									.filter(file -> currentTemplateList.contains(file))
									.collect( Collectors.toList() )
									.toArray();
							}
							else {
								checkedElements = new Object[0];
							}

							// set referenced templates as checked
							notificationTemplateTableViewer.setCheckedElements(checkedElements);
						}
						else {
							notificationTemplateTableViewer.setInput(new ArrayList<DataStoreVO>(0));
						}




						markHotelBookingsAsConfirmedButton.setSelection(emailTemplate.isMarkHotelBookingsAsConfirmed());
						markProgrammeBookingsAsConfirmedButton.setSelection(emailTemplate.isMarkProgrammeBookingsAsConfirmed());

						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public List<FileSummary> getNotificationTemplateList() {
		return notificationTemplateList;
	}


	public void setNotificationTemplateList(List<FileSummary> notificationTemplateList) {
		this.notificationTemplateList.clear();
		this.notificationTemplateList.addAll(notificationTemplateList);
		notificationTemplateTableViewer.refresh();
		updateButtonStates();
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
