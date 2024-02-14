package de.regasus.email.dispatchorder.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.combo.EventCombo;
import de.regasus.ui.Activator;

/**
 * A composite that shows an {@link EventCombo} and a tree table with the following structure: for that event. When the
 * event is changed with the combo, the table shows a different list of entities.
 *
 * @author manfred
 */
public class EmailDispatchOrderComposite extends Composite {

	protected EventCombo eventCombo;

	private EmailDispatchOrderTreeTable treeTable;


	public EmailDispatchOrderTreeTable getTreeTable() {
		return treeTable;
	}


	public EmailDispatchOrderComposite(Composite parent, int style) {
		super(parent, style);

		try {
			setLayout(new GridLayout(3, false));

			// The Event
			final Label eventLabel = new Label(this, SWT.NONE);
			eventLabel.setText(ParticipantLabel.Event.getString());

			eventCombo = new EventCombo(this, SWT.NONE);
			eventCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			eventCombo.setKeepEntityInList(false);
			eventCombo.addModifyListener(new ModifyListener() {
				// When the event is changed with the combo, the table shows a different list of entities.
				@Override
				public void modifyText(ModifyEvent e) {
					// The action needs to know the selected event, because new email templates are to
					// be created for that particular event.
					handleEventChange();
				}
			});

			// The Table
			treeTable = new EmailDispatchOrderTreeTable(this, SWT.NONE);
			treeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			handleEventChange();

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private void handleEventChange() {
		try {
			Long eventPK = getEventPK();
			treeTable.setEventPK(eventPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getEventPK() {
		return eventCombo.getEventPK();
	}


	public void setEventPK(Long eventPK) {
		eventCombo.setEventPK(eventPK);
		handleEventChange();
	}


	public String getEventFilter() {
		return eventCombo.getFilter();
	}


	public void setEventFilter(String filter) {
		eventCombo.setFilter(filter);
	}

}
