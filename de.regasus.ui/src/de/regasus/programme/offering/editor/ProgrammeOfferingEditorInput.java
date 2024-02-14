package de.regasus.programme.offering.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;

public class ProgrammeOfferingEditorInput
extends AbstractEditorInput<Long>
implements ILinkableEditorInput, EventIdProvider {

	/**
	 * For a performant implementation of EventProvider.
	 */
	private Long eventPK = null;


	/**
	 * The Long of the parent.
	 */
	private Long programmePointPK = null;


	private ProgrammeOfferingEditorInput() {
	}


	public static ProgrammeOfferingEditorInput getEditInstance(
		Long programmeOfferingPK,
		Long programmePointPK,
		Long eventPK
	) {
		ProgrammeOfferingEditorInput poEditorInput = new ProgrammeOfferingEditorInput();
		poEditorInput.key = programmeOfferingPK;
		poEditorInput.programmePointPK = programmePointPK;
		poEditorInput.eventPK = eventPK;
		return poEditorInput;
	}


	public static ProgrammeOfferingEditorInput getCreateInstance(
		Long programmePointPK,
		Long eventPK
	) {
		ProgrammeOfferingEditorInput programmeOfferingEditorInput = new ProgrammeOfferingEditorInput();
		programmeOfferingEditorInput.programmePointPK = programmePointPK;
		programmeOfferingEditorInput.eventPK = eventPK;
		return programmeOfferingEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PROGRAMME_OFFERING);
	}


	@Override
	public Class<?> getEntityType() {
		return ProgrammeOfferingVO.class;
	}


	public Long getProgrammePointPK() {
		return programmePointPK;
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}

}
