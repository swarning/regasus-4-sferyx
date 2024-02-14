package de.regasus.participant;

import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.participant.Participant;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.participant.ParticipantModel;

/**
 * A class that provides to the ParticipantEditpor and to the ParticipantTreeNode icons for 
 * participants depending on their cancelled, group, and companion state.
 * 
 * @author manfred
 */
public class ParticipantImageHelper {

	public static Image getImage(Participant participant) {
		
		if (participant.isGroupManager() || participant.get(ParticipantModel.GROUP_MANAGER_KEY) == Boolean.TRUE) {
			if (participant.isCancelled()) {
				return IconRegistry.getImage(IImageKeys.GROUP_MANAGER_CANCELLED);
			}
			else {
				return IconRegistry.getImage(IImageKeys.GROUP_MANAGER);
			}
		}
		else if (participant.isCompanion()) {
			if (participant.isCancelled()) {
				return IconRegistry.getImage(IImageKeys.COMPANION_CANCELLED);
			}
			else {
				return IconRegistry.getImage(IImageKeys.COMPANION);
			}
		}
		else if (participant.isInGroup()) {
			if (participant.isCancelled()) {
				return IconRegistry.getImage(IImageKeys.GROUP_MEMBER_CANCELLED);
			}
			else {
				return IconRegistry.getImage(IImageKeys.GROUP_MEMBER);
			}
		}
		else {
			if (participant.isCancelled()) {
				return IconRegistry.getImage(IImageKeys.PARTICIPANT_CANCELLED);
			}
			else {
				return IconRegistry.getImage(IImageKeys.PARTICIPANT);
			}
		}
	}
}
