package de.regasus.profile.customfield.combo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomField_Label_Comparator;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;

/**
 * This combo is currently only used in a modal dialog, where no change
 * of any data can take place, so some of the normal combo functions
 * are not implemented. Especially this Combo is not observing any model.
 */
@SuppressWarnings("rawtypes")
public class ProfileCustomFieldCombo extends AbstractComboComposite<ProfileCustomField> {

	// Model
	private ProfileCustomFieldModel customFieldModel;
	private ProfileCustomFieldGroupModel groupModel;


	public ProfileCustomFieldCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected void initModel() {
		customFieldModel = ProfileCustomFieldModel.getInstance();
		groupModel = ProfileCustomFieldGroupModel.getInstance();
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
					String label = customField.getLabelOrName();

					// append group name
					Long groupPK = customField.getGroupPK();
					if (groupPK != null) {
						try {
							ProfileCustomFieldGroup group = groupModel.getProfileCustomFieldGroup(groupPK);
							label += " (" + group.getName().getString() + ")";
						}
						catch (Exception e) {
							com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
						}

					}
					return label;
				}

				return super.getText(element);
			}
		};
	}


	@Override
	@SuppressWarnings("unchecked")
	protected Collection<ProfileCustomField> getModelData() throws Exception {
		Collection<ProfileCustomField> customFieldList = customFieldModel.getAllProfileCustomFields();

		// copy List before sorting, to avoid impact to model
		List modelData = CollectionsHelper.createArrayList(customFieldList);

		Collections.sort(modelData, ProfileCustomField_Label_Comparator.getInstance());

		return modelData;
	}

}
