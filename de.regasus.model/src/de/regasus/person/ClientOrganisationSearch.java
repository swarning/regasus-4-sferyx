package de.regasus.person;

import java.rmi.RemoteException;

import com.lambdalogic.messeinfo.contact.sql.OrganisationSearch;

import de.regasus.auth.AuthorizationException;

public class ClientOrganisationSearch extends OrganisationSearch {

	private static final long serialVersionUID = 1L;

	
	private static boolean initialized = false; 
		

	static {
		ClientAbstractPersonSearch.initStaticFields();
	}

	
	public static void initStaticFields() {
		if (! initialized) {
			// initialize static fields
			
			initialized = true;
		}
	}
	
	
	public ClientOrganisationSearch() throws RemoteException, AuthorizationException {
		super();
	}

}
