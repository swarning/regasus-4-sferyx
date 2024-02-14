package de.regasus.participant.dialog;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.lambdalogic.messeinfo.participant.Participant;

public class ParticipantLabelSorter extends ViewerSorter {
	
	private Collator collator = Collator.getInstance();
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		if (e1 instanceof Participant && e2 instanceof Participant) {
			Participant p1 = (Participant) e1;
			Participant p2 = (Participant) e2 ;
			String n1 = p1.getName(true);
			String n2 = p2.getName(true);
			return collator.compare(n1, n2);
			
		}
		else {
			return super.compare(viewer, e1, e2);
		}
	}

}
