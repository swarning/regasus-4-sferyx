package de.regasus.event.customfield.combo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField_Label_Comparator;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;

/**
 * This combo is currently only used in a modal dialog, where no change
 * of any data can take place, so some of the normal combo functions
 * are not implemented. Especially this Combo is not observing any model.
 */
public class ParticipantCustomFieldCombo extends AbstractComboComposite<ParticipantCustomField> {

	// Model
	private ParticipantCustomFieldModel customField;
	private ParticipantCustomFieldGroupModel groupModel;

	/**
	 * PK of the Event which ParticipantCustomField are shown.
	 */
	private Long eventID;


	public ParticipantCustomFieldCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected void initModel() {
		customField = ParticipantCustomFieldModel.getInstance();
		groupModel = ParticipantCustomFieldGroupModel.getInstance();
	}


	@Override
	protected void disposeModel() {
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				if (element instanceof CustomField) {
					CustomField customField = (CustomField) element;
					String name = customField.getLabelOrName();

					// append group name
					Long groupPK = customField.getGroupPK();
					if (groupPK != null) {
						try {
							ParticipantCustomFieldGroup group = groupModel.getParticipantCustomFieldGroup(groupPK);
							name += " (" + group.getName().getString() + ")";
						}
						catch (Exception e) {
							com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
						}

					}
					return name;
				}

				return super.getText(element);
			}
		};
	}


	@Override
	protected Collection<ParticipantCustomField> getModelData() throws Exception {
		List<ParticipantCustomField> modelData = customField.getParticipantCustomFieldsByEventPK(eventID);

		// copy List before sorting, to avoid impact to model
		modelData = CollectionsHelper.createArrayList(modelData);

		Collections.sort(modelData, ParticipantCustomField_Label_Comparator.getInstance());

		return modelData;
	}


	public void setEventID(Long eventID) throws Exception {
		this.eventID = eventID;

		syncComboToModel();
	}

}
