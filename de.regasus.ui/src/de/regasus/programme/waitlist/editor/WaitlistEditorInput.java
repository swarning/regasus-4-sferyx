package de.regasus.programme.waitlist.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.programme.WaitList;
import de.regasus.ui.Activator;

public class WaitlistEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {
	
	public WaitlistEditorInput(Long programmePointPK) {
		setKey(programmePointPK);
	}

	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.WAITLIST);
	}

	@Override
	public Class<?> getEntityType() {
		return WaitList.class;
	}

}
