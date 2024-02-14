package de.regasus.person;

import java.rmi.RemoteException;

import com.lambdalogic.messeinfo.contact.sql.PersonSearch;

import de.regasus.auth.AuthorizationException;

public abstract class ClientPersonSearch extends PersonSearch {

	private static final long serialVersionUID = 1L;

	
	private static boolean initialized = false; 
		

	static {
		ClientAbstractPersonSearch.initStaticFields();
	}

	
	public static void initStaticFields() {
		if (! initialized) {
	        PersonSearch.NATIONALITY.setSearchValuesProvider(ClientAbstractPersonSearch.getCountrySearchValuesProvider());
			
			initialized = true;
		}
	}
	
	
	public ClientPersonSearch() throws RemoteException, AuthorizationException {
		super();
	}

}
