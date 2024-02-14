package de.regasus.portal.page.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.portal.Page;
import de.regasus.ui.Activator;


public class PageEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	private PageEditorInput() {
	}


	public static PageEditorInput getEditInstance(Long pageLayoutPK) {
		PageEditorInput editorInput = new PageEditorInput();
		editorInput.key = pageLayoutPK;
		return editorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PAGE);
	}


	@Override
	public Class<?> getEntityType() {
		return Page.class;
	}

}
