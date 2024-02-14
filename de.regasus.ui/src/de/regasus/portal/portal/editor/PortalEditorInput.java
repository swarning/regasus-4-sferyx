package de.regasus.portal.portal.editor;

import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.portal.Portal;
import de.regasus.ui.Activator;


public class PortalEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	private PortalEditorInput() {
	}


	public static PortalEditorInput getEditInstance(Long portalPK) {
		Objects.requireNonNull(portalPK);

		PortalEditorInput editorInput = new PortalEditorInput();
		editorInput.key = portalPK;
		return editorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PORTAL);
	}


	@Override
	public Class<?> getEntityType() {
		return Portal.class;
	}

}
