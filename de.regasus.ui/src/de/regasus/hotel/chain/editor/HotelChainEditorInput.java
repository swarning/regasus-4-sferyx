package de.regasus.hotel.chain.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.IImageKeys;
import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class HotelChainEditorInput extends AbstractEditorInput<Long> {

	public HotelChainEditorInput() {
	}
	
	
	public HotelChainEditorInput(Long hotelChainPK) {
		this.key = hotelChainPK;
	}


	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.MD_HOTEL);
	}
		
}
