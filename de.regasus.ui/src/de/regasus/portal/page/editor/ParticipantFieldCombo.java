package de.regasus.portal.page.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.exception.ErrorMessageException;
import de.regasus.portal.PortalParticipantFieldsModel;
import de.regasus.portal.component.Field;
import de.regasus.portal.participant.PortalParticipantFields;

public class ParticipantFieldCombo extends FieldCombo {

	/**
	 * PK of the Event which Fields are hold by this Combo.
	 */
	protected Long eventID;


	// Model
	private PortalParticipantFieldsModel model;


	public ParticipantFieldCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Collection<Field> getModelData() throws Exception {
		List<Field> modelData = Collections.emptyList();

		if (eventID != null) {
			PortalParticipantFields portalParticipantFields = model.getPortalParticipantFields(eventID);
			modelData = new ArrayList<>( portalParticipantFields.getFields() );
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		model = PortalParticipantFieldsModel.getInstance();
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(modelListener);
		}
	}


	public Long getEventID() {
		return eventID;
	}


	public void setEventID(Long eventID) throws Exception {
		if (this.eventID != null) {
			if ( ! this.eventID.equals(eventID)) {
				throw new ErrorMessageException("Event ID must not change");
			}
		}
		else {
			this.eventID = eventID;

			// register at the model before getting its data, so the data will be put to the models cache
			model.addListener(modelListener, eventID);

			// refresh combo
			handleModelChange();
		}
	}

}
