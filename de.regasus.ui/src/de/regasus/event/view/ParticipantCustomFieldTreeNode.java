package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.ui.Activator;


/**
 * A node representing a Participant Custom Field.
 */
public class ParticipantCustomFieldTreeNode
extends TreeNode<ParticipantCustomField>
implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long customFieldID;

	/* Just used to refresh the data of this Custom Field.
	 * Observing this Custom Field is not necessary, because the parent TreeNode
	 * is observing all Custom Fields of its Group. On any change the value of this TreeNode is set and
	 * refreshTreeNode() of the parent is called.
	 */
	private ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors
	// *

	public ParticipantCustomFieldTreeNode(
		TreeViewer treeViewer,
		ParticipantCustomFieldGroupTreeNode parent,
		ParticipantCustomField participantCustomField
	) {
		super(treeViewer, parent, participantCustomField);

		customFieldID = participantCustomField.getID();
	}

	// *
	// * Constructors
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return ParticipantCustomField.class;
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
		/* The result is a static description of this node type: "Participant Custom Field".
		 * It is followed by the Group's value for toolTip or its description.
		 */
		StringBuilder sb = new StringBuilder(200);

		sb.append(ParticipantLabel.ParticipantCustomField.getString());

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

	public Long getParticipantCustomFieldID() {
		return customFieldID;
	}


	@Override
	public Long getEventId() {
		return value.getEventPK();
	}


	public Long getGroupPK() {
		return value.getGroupPK();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
