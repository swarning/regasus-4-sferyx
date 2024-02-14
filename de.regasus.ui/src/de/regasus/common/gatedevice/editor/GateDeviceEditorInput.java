/**
 * GateDeviceEditorInput.java
 * created on 25.09.2013 11:28:46
 */
package de.regasus.common.gatedevice.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class GateDeviceEditorInput 
extends AbstractEditorInput<Long> {

	public GateDeviceEditorInput() {
	}
	
	
	public GateDeviceEditorInput(Long gateDevicePK) {
		key = gateDevicePK;
	}
	
	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, null);
	}
	
}
