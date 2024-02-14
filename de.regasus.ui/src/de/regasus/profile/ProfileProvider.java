package de.regasus.profile;

/**
 * Interface for editors who show a Profile (e.g. ProfileEditor), so that other components 
 * (e.g. ProfileRelationView) can update to the currently visible Profile.
 */
public interface ProfileProvider {
	
	/**
	 * Return the PK of the currently visible Profile.
	 */
	Long getProfilePK();

}
