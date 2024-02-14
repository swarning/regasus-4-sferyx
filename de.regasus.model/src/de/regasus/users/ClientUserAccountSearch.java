package de.regasus.users;

import com.lambdalogic.messeinfo.account.sql.UserAccountSearch;

public class ClientUserAccountSearch extends UserAccountSearch {

	private static final long serialVersionUID = 1L;

	
	protected GroupSearchValuesProvider groupSearchValuesProvider;

	
	public ClientUserAccountSearch()
    throws Exception {
        super();

        groupSearchValuesProvider = new GroupSearchValuesProvider();
        GROUP.setSearchValuesProvider(groupSearchValuesProvider);
    }

}
