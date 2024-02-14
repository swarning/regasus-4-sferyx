package de.regasus.profile.customfield.dialog;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.contact.CustomFieldUpdateParameter;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class CollectiveChangeProfileCustomFieldsWizard extends Wizard {

	private List<CustomFieldUpdateParameter> parameters;
	private List<Long> profilePKs;
	private CollectiveChangeProfileCustomFieldsPage collectiveChangeProfileCustomFieldsPage;


	// **************************************************************************
	// * Constructors
	// *

	public CollectiveChangeProfileCustomFieldsWizard(List<Long> profilePKs) {
		this.profilePKs = profilePKs;
	}

	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {
		collectiveChangeProfileCustomFieldsPage = new CollectiveChangeProfileCustomFieldsPage(profilePKs.size());
		addPage(collectiveChangeProfileCustomFieldsPage);
	}


	@Override
	public String getWindowTitle() {
		return I18N.CollectiveChange;
	}


	@Override
	public boolean performFinish() {
		try {
			parameters = collectiveChangeProfileCustomFieldsPage.getParameters();
			return true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}
	}

	
	public List<CustomFieldUpdateParameter> getParameters() {
		return parameters;
	}
	
}
