/**
 * GateEditorInput.java
 * created on 24.09.2013 11:48:38
 */
package de.regasus.event.gate.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.GateVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.ui.Activator;

public class GateEditorInput 
extends AbstractEditorInput<Long>
implements ILinkableEditorInput {
	
	/**
	 * The Long of the parent.
	 */
	private Long locationPK = null;
	
	
	private GateEditorInput() {
	}

	
	public static GateEditorInput getEditInstance(
		Long gatePK,
		Long locationPK
	) {
		GateEditorInput gateEditorInput = new GateEditorInput();
		gateEditorInput.key = gatePK;
		gateEditorInput.locationPK = locationPK;
		return gateEditorInput;
	}
	
	
	public static GateEditorInput getCreateInstance(Long locationPK) {
		GateEditorInput gateEditorInput = new GateEditorInput();
		gateEditorInput.locationPK = locationPK;
		return gateEditorInput;
	}
	
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.GATE);
	}
	
	
	public Long getLocationPK() {
		return locationPK;
	}
	

	@Override
	public Class<?> getEntityType() {
		return GateVO.class;
	}

}
