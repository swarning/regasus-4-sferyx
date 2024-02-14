package de.regasus.profile.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.ProfileRelation;

public class ProfileRelationSelectionHelper {

	public static List<Long> getProfileRelationIDs(ISelection selection) {
		List<Long> profileRelationIDs = null;
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			
			profileRelationIDs = new ArrayList<Long>();
			
			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				
				if (o instanceof ProfileRelation) {
					ProfileRelation profileRelation = (ProfileRelation) o;
					profileRelationIDs.add(profileRelation.getID());
				}
			}
		}
		
		return profileRelationIDs;
	}
	
	
	public static List<ProfileRelation> getProfileRelations(ISelection selection) throws Exception {
		List<ProfileRelation> profileRelationList = null;
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			
			profileRelationList = new ArrayList<ProfileRelation>();
			
			Iterator<?> iterator = sselection.iterator();
			
			while (iterator.hasNext()) {
				Object o = iterator.next();
				
				if (o instanceof ProfileRelation) {
					profileRelationList.add((ProfileRelation) o);
				}
				else {
					throw new RuntimeException("Not a profile relation: " + o.getClass().getName());
				}
			}
		}
		
		return profileRelationList;
	}
	
	
	public static List<ProfileRelation> getProfileRelations(ExecutionEvent event) throws Exception {
		List<ProfileRelation> profileRelationList = null;
		
		// The active part is no ProfileEditor: Get the selected Profile(s).
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null) {
			profileRelationList = getProfileRelations(selection);
		}
		
		return profileRelationList;
	}
	
	
	public static ProfileRelation getProfileRelation(ExecutionEvent event) throws Exception {
		ProfileRelation profileRelation = null;
		
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null) {
			List<ProfileRelation> profileRelationList = getProfileRelations(selection);
			if (profileRelationList != null && !profileRelationList.isEmpty()) {
				if (profileRelationList.size() == 1) {
					profileRelation = profileRelationList.get(0);
				}
				else {
					throw new RuntimeException(
						"There are " + profileRelationList.size() + " profile relations selected.\n" +
						"This command doesn't allow selections with more than one profile relation."
					);
				}
			}
		}
		
		return profileRelation;
	}
	
}
