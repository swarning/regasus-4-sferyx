package de.regasus.participant;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

public interface ParticipantProvider {

	/**
	 * Returns the PK of the provided participant.
	 * 
	 * @return
	 */
	Long getParticipantPK();
	
	/**
	 * Requests the ParticipantProvider to register at the ParticipantModel as an Foreign-Key-Listener.
	 * 
	 * The ParticipantTreeView calls this method, so that the ParticipantEditor calls 
	 * ParticipantModel.addForeignKeyListener(). So the participants tree data will be hold in the model
	 * while the editor is open. So, if you have 2 editors of 2 participants open, which belong to different 
	 * groups, both trees will be held in the model. Even if the user switches between the 2 editors.
	 */
	void registerForForeignKey();

	
	IParticipant getIParticipant();
	
}
