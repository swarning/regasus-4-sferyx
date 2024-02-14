package de.regasus.core.ui.editor;

import org.eclipse.ui.IEditorPart;

/**
 * An interface that an editor may implement. When he does so, the refresh-command is 
 * enabled (via configuration in pluging.xml) when such an editor is open and active.
 * 
 * @author manfred
 */
public interface IRefreshableEditorPart extends IEditorPart {
	
	/**
	 * Is to be called if an editor shall update it's data from its 
	 * original data source (database, file, or whatever) using the
	 * key-information he finds in his EditorInput. 
	 * 
	 * @throws Exception
	 */
	public void refresh() throws Exception;
	
	/**
	 * Some refreshable editors may contain new data that does not
	 * yet find itself in the target datastore (database, file, or whatever),
	 * so it doesn't make sense to refreh it.
	 */
	public boolean isNew();

}
