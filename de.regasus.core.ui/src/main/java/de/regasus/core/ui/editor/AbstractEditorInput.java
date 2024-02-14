package de.regasus.core.ui.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;



public abstract class AbstractEditorInput<KeyType> implements IEditorInput {
	protected KeyType key = null;
	protected String name = null;
	protected String toolTipText = null;
	

	
	public KeyType getKey() {
		return key;
	}
	
	public void setKey(KeyType key) {
		this.key = key;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public String getToolTipText() {
		return toolTipText;
	}
	
	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	

	
	public boolean exists() {
		return key != null;
	}

	
	public IPersistableElement getPersistable() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		if (key == null) {
			/* Editors with key == null are never equal to other Editors.
			 * Otherwise one cannot open more than one Editor for new data.
			 */
			return false;
		}
		final AbstractEditorInput<?> other = (AbstractEditorInput<?>) obj;
		return (key.equals(other.key));
	}
	
}
