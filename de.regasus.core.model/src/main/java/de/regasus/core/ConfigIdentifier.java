package de.regasus.core;

import com.lambdalogic.messeinfo.config.ConfigScope;

public class ConfigIdentifier {

	private ConfigScope scope;
	private String key;
	
	
	public ConfigIdentifier(ConfigScope scope, String key) {
		super();
		this.scope = scope;
		this.key = key;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigIdentifier other = (ConfigIdentifier) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		}
		else if (!key.equals(other.key))
			return false;
		if (scope != other.scope)
			return false;
		return true;
	}


	public ConfigScope getScope() {
		return scope;
	}


	public String getKey() {
		return key;
	}


	@Override
	public String toString() {
		return "ConfigIdentifier [scope=" + scope + ", key=" + key + "]";
	}
	
}
