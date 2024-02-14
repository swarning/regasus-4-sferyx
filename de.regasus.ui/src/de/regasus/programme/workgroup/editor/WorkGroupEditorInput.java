package de.regasus.programme.workgroup.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.ui.Activator;

public class WorkGroupEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {
	
	
	Long programmePointPK;
	
	public static WorkGroupEditorInput getEditInstance(Long workGroupPK) {
		WorkGroupEditorInput editorInput = new WorkGroupEditorInput();
		editorInput.setKey(workGroupPK);
		return editorInput;
	}
	
	
	public static WorkGroupEditorInput getCreateInstance(Long programmePointPK) {
		WorkGroupEditorInput editorInput = new WorkGroupEditorInput();
		editorInput.setProgrammePointPK(programmePointPK);
		return editorInput;
	}

	
	private  WorkGroupEditorInput() {
	}
	
	

	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PARTICIPANT_TYPE);
	}


	public Long getProgrammePointPK() {
		return programmePointPK;
	}


	public void setProgrammePointPK(Long programmePointPK) {
		this.programmePointPK = programmePointPK;
	}


	public Class<?> getEntityType() {
		return WorkGroupVO.class;
	}
	
}
