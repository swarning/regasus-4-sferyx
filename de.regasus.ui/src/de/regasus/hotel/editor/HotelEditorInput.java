package de.regasus.hotel.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.common.CountryCity;
import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class HotelEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput{

	private CountryCity countryCity;
	

	public HotelEditorInput(CountryCity countryCity) {
		this.countryCity = countryCity;
	}
	
	
	public HotelEditorInput(Long hotelID) {
		this.key = hotelID;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.MD_HOTEL);
	}



	public CountryCity getCity() {
		return countryCity;
	}


	public Class<?> getEntityType() {
		return Hotel.class;
	}

}
