package de.regasus.profile.relationtype;

import org.eclipse.jface.viewers.LabelProvider;

import de.regasus.profile.relationtype.ProfileRelationTypeSelectionPage.ProfileRelationTypeElement;


public class ProfileRelationTypeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ProfileRelationTypeElement) {
			ProfileRelationTypeElement profileRelationTypeElement = (ProfileRelationTypeElement) element;
			return profileRelationTypeElement.label;
		}
		return super.getText(element);
	}

}
