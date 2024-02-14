/**
 * ParticipantCustomFieldGroupSectionContainer.java
 * created on 18.07.2013 16:35:03
 */
package de.regasus.participant.editor.overview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.CustomFieldFormatter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValue;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.Tuple;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class ParticipantCustomFieldGroupSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;

	private ParticipantModel participantModel;
	private ParticipantCustomFieldModel customFieldModel;

	private ParticipantCustomFieldGroup group;

	private ParticipantConfigParameterSet partConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public ParticipantCustomFieldGroupSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		ParticipantCustomFieldGroup group,
		ParticipantConfigParameterSet partConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.participantID = participantID;
		this.group = group;
		this.partConfigParameterSet = partConfigParameterSet;

		addDisposeListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this, participantID);

		customFieldModel = ParticipantCustomFieldModel.getInstance();

		refreshSection();
	}


	@Override
	protected String getTitle() {
		String title = group.getName().getString();
		return title;
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;


			ArrayList<Tuple<String, String>> labelValueTuples = null;

			boolean visible =
				partConfigParameterSet == null ||
				partConfigParameterSet.getCustomField().isVisible();

			if (visible) {
				Participant participant = participantModel.getParticipant(participantID);

				Long eventPK = participant.getEventId();

				List<ParticipantCustomField> customFields = customFieldModel.getParticipantCustomFieldsByGroup(
					eventPK,
					group.getID()
				);


				// First collect the fields and values to show, if any
				labelValueTuples = new ArrayList<Tuple<String, String>>(customFields.size());

				for (ParticipantCustomField customField : customFields) {
					ParticipantCustomFieldValue customFieldValue = participant.getCustomFieldValue(customField.getID());
					if (customFieldValue != null) {
						String value = CustomFieldFormatter.format(customField, customFieldValue);

						if (StringHelper.isNotEmpty(value)) {
							Tuple<String, String> valueLabelTuple = new Tuple<String, String>(
								customField.getLabelOrName(),
								value
							);
							labelValueTuples.add(valueLabelTuple);
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
