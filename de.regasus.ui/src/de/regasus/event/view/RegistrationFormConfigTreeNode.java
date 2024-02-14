package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.ui.Activator;

public class RegistrationFormConfigTreeNode
extends TreeNode<RegistrationFormConfig>
implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long registrationFormConfigID;

	/* Just used to refresh the data of this Registration Form Config.
	 * Observing this Registration Form Config is not necessary, because the parent TreeNode
	 * is observing all its Registration Form Configs. On any change the value of this TreeNode is
	 * set and refreshTreeNode() of the parent is called.
	 */
	private RegistrationFormConfigModel rfcModel = RegistrationFormConfigModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors
	// *

	public RegistrationFormConfigTreeNode(
		TreeViewer treeViewer,
		RegistrationFormConfigListTreeNode parent,
		RegistrationFormConfig registrationFormConfig
	) {
		super(treeViewer, parent);

		value = registrationFormConfig;
		registrationFormConfigID = value.getId();
	}

	// * Constructors
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return RegistrationFormConfig.class;
	}


	@Override
	public Object getKey() {
		return registrationFormConfigID;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			text = value.getWebId();
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		return de.regasus.onlineform.OnlineFormI18N.WebsiteConfiguration;
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.MD_WORLD);
	}


	@Override
	public boolean isLeaf() {
		return true;
	}


	@Override
	public void refresh() {
		/*
		 * The parent node takes the responsibility to refresh this node, therefore we don't have to
		 * be listeners ourselves, but just fire the refresh request to the model.
		 */

		try {
			// refresh data of this TreeNode
			rfcModel.refresh(registrationFormConfigID);

			// no child TreeNodes to refresh
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	public Long getRegistrationFormConfigId() {
		return registrationFormConfigID;
	}

	@Override
	public void setValue(RegistrationFormConfig registrationFormConfig) {
		value = registrationFormConfig;
	}


	@Override
	public Long getEventId() {
		return value.getEventPK();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
