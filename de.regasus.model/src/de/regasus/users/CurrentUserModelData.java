package de.regasus.users;

import java.util.ArrayList;
import java.util.List;

public class CurrentUserModelData implements Cloneable {

	private String userName;
	private List<String> userGroups;


	public CurrentUserModelData() {
		super();
	}


	@Override
	public CurrentUserModelData clone() {
		CurrentUserModelData clone = new CurrentUserModelData();
		clone.userName = userName;
		clone.userGroups = new ArrayList<>(userGroups);

		return clone;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public List<String> getUserGroups() {
		return userGroups;
	}


	public void setUserGroups(List<String> userGroups) {
		this.userGroups = userGroups;
	}

}
