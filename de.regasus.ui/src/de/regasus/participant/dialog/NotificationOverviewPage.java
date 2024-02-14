package de.regasus.participant.dialog;

import static de.regasus.LookupService.getNoteMgr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.contact.data.NoteCVO;
import com.lambdalogic.messeinfo.invoice.data.BookingNoteCVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class NotificationOverviewPage extends WizardPage {

	public static final String NAME = "NotificationOverviewPage";
	private List<? extends IParticipant> participantList;
	private Table table;
	private NotificationsOverviewTable notificationsOverviewTable;
	private TableViewer tableViewer;


	private List<BookingNoteCVO> bookingNoteCVOs;


	public NotificationOverviewPage(List<? extends IParticipant> participants) {
		super(NAME);
		this.participantList = participants;
		setTitle(ParticipantLabel.Notes.getString());
		setMessage(I18N.NotificationOverviewPage_Message);
	}


	@Override
	public void createControl(Composite parent) {

		Composite tableComposite = new Composite(parent, SWT.NONE);

		ArrayList<Long> recipientPKList = new ArrayList<>();

		for(IParticipant participant : participantList) {
			recipientPKList.add(participant.getPK());
		}

		bookingNoteCVOs = new ArrayList<>();

		try {
			// I'm not fetching these notes from a CacheModel, since after the imminent printing, they
			// are outdated anyway.
			List<NoteCVO> noteCVOs = getNoteMgr().getNoteCVOs(recipientPKList);

			// In _each_ object of that list may be both ProgrammeNoteCVOs and HotelNoteCVOs
			for (NoteCVO noteCVO : noteCVOs) {
				bookingNoteCVOs.addAll( noteCVO.getProgrammeNoteCVOs() );
				bookingNoteCVOs.addAll( noteCVO.getHotelNoteCVOs() );
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}


		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		table = new Table(tableComposite, SWT.NONE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// The following code might have been the reason for the bug off disappearing rows
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.deselectAll();
			}
		});

		// Name
		final TableColumn nameTableColumn = new TableColumn(table, SWT.LEFT);
		layout.setColumnData(nameTableColumn, new ColumnWeightData(20));
		nameTableColumn.setText(UtilI18N.Name);

		// Typ
		final TableColumn typColumn = new TableColumn(table, SWT.LEFT);
		layout.setColumnData(typColumn, new ColumnWeightData(20));
		typColumn.setText(ParticipantLabel.Type.getString());

		// SubTyp
		final TableColumn subtypColumn = new TableColumn(table, SWT.LEFT);
		layout.setColumnData(subtypColumn, new ColumnWeightData(40));
		subtypColumn.setText(ParticipantLabel.Subtype.getString());

		// Description
		final TableColumn descriptionColumn = new TableColumn(table, SWT.LEFT);
		layout.setColumnData(descriptionColumn, new ColumnWeightData(20));
		descriptionColumn.setText(UtilI18N.Description);

		notificationsOverviewTable = new NotificationsOverviewTable(table);

		tableViewer = notificationsOverviewTable.getViewer();
		tableViewer.setInput(bookingNoteCVOs);



		setControl(tableComposite);
	}


	public int getBookingNoteCount() {
		return bookingNoteCVOs.size();
	}
}
