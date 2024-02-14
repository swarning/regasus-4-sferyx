package de.regasus.participant.editor;

/**
 * Interface for clients who want to be informed
 * if an editor has been saved.
 * 
 * The only editor which supports ISaveListener is
 * the ParticipantEditor. But there may be more in 
 * future.
 * 
 * @author sacha
 *
 */
public interface ISaveListener {

	void saved(Object source, boolean create);
	
}
