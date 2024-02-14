package de.regasus.core.ui.editor.cache;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class FileCacheStatisticsEditorInput extends AbstractEditorInput<Integer> {

	public FileCacheStatisticsEditorInput() {
		this.key = 0;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CACHE_STATISTICS);
	}


	@Override
	public String getName() {
		return CoreI18N.FileCacheStatisticsEditor_Name;
	}


	@Override
	public String getToolTipText() {
		return CoreI18N.FileCacheStatisticsEditor_Desc;
	}

}
