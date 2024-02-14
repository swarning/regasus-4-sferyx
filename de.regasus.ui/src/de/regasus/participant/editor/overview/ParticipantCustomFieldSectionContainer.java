/**
 * CustomFieldSectionContainer.java
 * created on 18.07.2013 15:35:34
 */
package de.regasus.participant.editor.overview;

import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.Tuple;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

/**
 * SectionContainer to show old custom fields and new custom fields with out group in overview tab
 * of Participant Editor.
 */
public class ParticipantCustomFieldSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;

	private ParticipantModel participantModel;

	private ParticipantConfigParameterSet partConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public ParticipantCustomFieldSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		ParticipantConfigParameterSet partConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.participantID = participantID;
		this.partConfigParameterSet = partConfigParameterSet;

		addDisposeListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this, participantID);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return ContactLabel.CustomFields.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

			boolean visible =
				partConfigParameterSet == null ||
				partConfigParameterSet.getSimpleCustomField().isVisible() ||
				partConfigParameterSet.getCustomField().isVisible();

			// map from custom field names to custom field values (old and new)
			ArrayList<Tuple<String, String>> labelValueTuples = new ArrayList<Tuple<String, String>>();

			if (visible) {
				Participant participant = participantModel.getParticipant(participantID);

				Long eventPK = participant.getEventId();
				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);


				// add old custom field values to customFieldMap
				if (partConfigParameterSet.getSimpleCustomField().isVisible()) {
					String[] oldCustomFieldNames = eventVO.getCustomFieldNames();

					if (oldCustomFieldNames != null && oldCustomFieldNames.length > 0) {
						for (int i = 0; i < oldCustomFieldNames.length; i++) {
							String oldCustomFieldName = oldCustomFieldNames[i];
							if (oldCustomFieldName != null) {
								String oldCustomFieldValue = participant.getCustomField(i + 1);
								if (oldCustomFieldValue != null && !oldCustomFieldValue.isEmpty()) {
									Tuple<String, String> valueLabelTuple = new Tuple<String, String>(
										oldCustomFieldName,
										oldCustomFieldValue
										);
									labelValueTuples.add(valueLabelTuple);
								}
							}
						}
					}
				}

				visible = ! labelValueTuples.isEmpty();
			}

			setVisible(visible);
			if (visible) {
				for (Tuple<String, String> tuple : labelValueTuples) {
					addIfNotEmpty(tuple.getA(), tuple.getB());
				}
			}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (participantModel != null && participantID != null) {
			try {
				participantModel.removeListener(this, participantID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
