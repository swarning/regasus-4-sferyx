package de.regasus.portal.pagelayout.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.portal.PageLayout;
import de.regasus.ui.Activator;


public class PageLayoutEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	private Long portalPK = null;


	private PageLayoutEditorInput() {
	}


	public static PageLayoutEditorInput getEditInstance(Long pageLayoutPK) {
		PageLayoutEditorInput editorInput = new PageLayoutEditorInput();
		editorInput.key = pageLayoutPK;
		return editorInput;
	}


	public static PageLayoutEditorInput getCreateInstance(Long portalPK) {
		PageLayoutEditorInput editorInput = new PageLayoutEditorInput();
		editorInput.portalPK = portalPK;
		return editorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PAGE_LAYOUT);
	}


	@Override
	public Class<?> getEntityType() {
		return PageLayout.class;
	}


	public Long getPortalPK() {
		return portalPK;
	}

}
