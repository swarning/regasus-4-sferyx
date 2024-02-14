package de.regasus.email;

import com.lambdalogic.messeinfo.contact.data.SimplePersonSearchData;

public interface SampleRecipientListener {

	public void changed(Long eventPK, SimplePersonSearchData psd) throws Exception ;
	
}
