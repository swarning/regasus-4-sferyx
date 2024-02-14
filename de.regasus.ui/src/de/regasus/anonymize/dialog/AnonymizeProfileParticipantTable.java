package de.regasus.anonymize.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ColorHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

public class AnonymizeProfileParticipantTable extends ProfileParticipantTable {

	public AnonymizeProfileParticipantTable(Table table) {
		super(table);
	}


	@Override
	public Color getBackground(Object element, int columnIndex) {
		Color color = ColorHelper.getSystemColor(SWT.COLOR_WHITE);

		try {
			Object colorValue = ((Person) element).get(GROUP_KEY);
			boolean lightColor = colorValue == Boolean.TRUE;

			boolean active = isActive(element);

			if (active) {
				// red
				if (lightColor) {
					color = ColorHelper.RED_2;
				}
				else {
					color = ColorHelper.RED_3;
				}
			}
			else {
				// green
				if (lightColor) {
					color = ColorHelper.GRAY_1;
				}
				else {
					color = ColorHelper.GRAY_2;
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return color;
	}


	public void checkAll() {
		try {
			TableViewer tableViewer = getViewer();
			if (tableViewer instanceof CheckboxTableViewer) {
				List<Person> personList = (List<Person>) tableViewer.getInput();
				List<Person> checkedPersonList = new ArrayList<>(personList.size());
				for (Person person : personList) {
					if (!isActive(person)) {
						checkedPersonList.add(person);
					}
				}

				((CheckboxTableViewer) tableViewer).setCheckedElements(  checkedPersonList.toArray() );
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private boolean isActive(Object person) throws Exception {
		boolean active = false;

		if (person instanceof Participant) {
			Participant participant = ((Participant) person);
			if (!participant.isCancelled()) {
				EventVO eventVO = EventModel.getInstance().getEventVO( participant.getEventId() );
				if (eventVO.getEndTime().after( new Date() )) {
					active = true;
				}
			}
		}

		return active;
	}

}
