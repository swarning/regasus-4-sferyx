package de.regasus.core.ui.search;

import java.util.List;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;

/**
 * Interface to modify the behaviour of some SearchComposites.
 * Examples: AssignCompanionsWizard, InvoiceSearchComposite, ParticipantSearchComposite, ProfileSearchComposite.
 * An example is to add SQLParameters which shall be invisible for the user.
 * E.g. a dialog to search for companions shall always show only companions.
 * 
 * @author sacha
 *
 */
public interface SearchInterceptor {

	/**
	 * Modifies the given List of SQLParameters.
	 * @param searchParameter
	 */
	void changeSearchParameter(List<SQLParameter> sqlParameters);
	
}
