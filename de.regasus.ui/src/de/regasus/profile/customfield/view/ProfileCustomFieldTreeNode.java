package de.regasus.profile.customfield.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.ui.Activator;


/**
 * A node representing a Profile Custom Field.
 */
public class ProfileCustomFieldTreeNode extends TreeNode<ProfileCustomField> {

	// *************************************************************************
	// * Attributes
	// *

	private Long customFieldID;

	/* Just used to refresh the data of this Custom Field.
	 * Observing this Custom Field is not necessary, because the parent TreeNode
	 * is observing all Custom Fields of its Group. On any change the value of this TreeNode is set and
	 * refreshTreeNode() of the parent is called.
	 */
	private ProfileCustomFieldModel pcfModel = ProfileCustomFieldModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors
	// *

	public ProfileCustomFieldTreeNode(
		TreeViewer treeViewer,
		ProfileCustomFieldGroupTreeNode parent,
		ProfileCustomField profileCustomField
	) {
		super(treeViewer, parent, profileCustomField);

		customFieldID = profileCustomField.getID();
	}

	// *
	// * Constructors
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return ProfileCustomField.class;
	}


	@Override
	public Object getKey() {
		return customFieldID;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			LanguageString label = value.getLabel();
			if (label != null && ! label.isEmpty()) {
				text = label.getString();
			}
			else {
				text = value.getName();
			}
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		/* The result is a static description of this node type: "Profile Custom Field".
		 * It is followed by the Group's value for toolTip or its description.
		 */
		StringBuilder sb = new StringBuilder(200);

		sb.append(ProfileLabel.ProfileCustomField.getString());

		LanguageString toolTip = value.getToolTip();
		if (toolTip != null) {
			String toolTipText = toolTip.getString();
			if (StringHelper.isEmpty(toolTipText)) {
				toolTipText = value.getDescription();
			}
			if (!StringHelper.isEmpty(toolTipText)) {
				sb.append("\n\n");
				sb.append(toolTipText);
			}
		}

		return sb.toString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.MD_CUSTOM_FIELD);
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
			pcfModel.refresh(customFieldID);

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

	public Long getProfileCustomFieldID() {
		return customFieldID;
	}


	public Long getGroupPK() {
		return value.getGroupPK();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
