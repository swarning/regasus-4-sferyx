package de.regasus.programme.programmepoint.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;

public class ProgrammePointEditorInput
extends AbstractEditorInput<Long>
implements ILinkableEditorInput, EventIdProvider {

	private Long eventPK = null;


	private ProgrammePointEditorInput() {
	}


	public static ProgrammePointEditorInput getEditInstance(Long programmePointPK, Long eventPK) {
		ProgrammePointEditorInput programmePointEditorInput = new ProgrammePointEditorInput();
		programmePointEditorInput.key = programmePointPK;
		programmePointEditorInput.eventPK = eventPK;
		return programmePointEditorInput;
	}


	public static ProgrammePointEditorInput getCreateInstance(Long eventPK) {
		ProgrammePointEditorInput programmePointEditorInput = new ProgrammePointEditorInput();
		programmePointEditorInput.eventPK = eventPK;
		return programmePointEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PROGRAMME_POINT);
	}


	@Override
	public Class<?> getEntityType() {
		return ProgrammePointVO.class;
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}

}
