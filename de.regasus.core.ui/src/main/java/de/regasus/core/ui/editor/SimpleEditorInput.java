package de.regasus.core.ui.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


/**
 * Generic {@link IEditorInput} for editors which don't need their own implementation.
 */
public abstract class SimpleEditorInput implements IEditorInput {

	private ImageDescriptor imageDescriptor;
	private String toolTip;
	private String name;



	public SimpleEditorInput(ImageDescriptor imageDescriptor, String name, String toolTip) {
		this.imageDescriptor = imageDescriptor;
		this.name = name;
		this.toolTip = toolTip;
	}


	@Override
	public boolean exists() {
		return true;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public IPersistableElement getPersistable() {
		return null;
	}


	@Override
	public String getToolTipText() {
		return toolTip;
	}


	@Override
	@SuppressWarnings({ "rawtypes" })
	public Object getAdapter(Class adapter) {
		return null;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleEditorInput other = (SimpleEditorInput) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

}
