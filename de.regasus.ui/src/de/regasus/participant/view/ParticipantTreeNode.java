package de.regasus.participant.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.EventIdProvider;
import de.regasus.participant.ParticipantImageHelper;
import de.regasus.participant.ParticipantProvider;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.ParticipantTypeModel;

public class ParticipantTreeNode extends TreeNode<Participant> implements ParticipantProvider, EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private boolean sortByName = true;
	private ParticipantStateModel participantStateModel;
	private ParticipantTypeModel participantTypeModel;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public ParticipantTreeNode(
		TreeViewer treeViewer,
		TreeNode<?> parent,
		Participant participant
	) {
		super(treeViewer, parent, participant, true);

		participantStateModel = ParticipantStateModel.getInstance();
		participantTypeModel = ParticipantTypeModel.getInstance();
	}

	// *
	// * Constructors and dispose()
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return Participant.class;
	}


	@Override
	public Object getKey() {
		return getParticipantPK();
	}


	@Override
	public String getText() {
		StringBuilder text = new StringBuilder(256);
		if (value != null) {
			if (sortByName) {
				text.append(value.getName(true));
				text.append(" [");
				text.append(value.getNumber());
				text.append("]");
			}
			else {
				text.append("[");
				text.append(value.getNumber());
				text.append("] ");
				text.append(value.getName(true));
			}
		}
		return text.toString();
	}


	@Override
	public String getToolTipText() {
		StringBuilder text = new StringBuilder(1024);

		try {
			if (value != null) {
    			String participantState = participantStateModel.getParticipantState(value.getParticipantStatePK()).getString();
    			String participantType = participantTypeModel.getParticipantType(value.getParticipantTypePK()).getName().getString();

    			text.append( Participant.PARTICIPANT_STATE.getString() );
    			text.append(": ");
    			text.append(participantState);

    			text.append("\n");

    			text.append( Participant.PARTICIPANT_TYPE.getString() );
    			text.append(": ");
    			text.append(participantType);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return text.toString();
	}


	@Override
	public Image getImage() {
		return ParticipantImageHelper.getImage(value);
	}


	@Override
	public void refresh() {
	}


	@Override
	public boolean isLeaf() {
		/* The children of ParticipantTreeNode are always loaded. They are not loaded on demand.
		 * Therefore a ParticipantTreeNode is a leaf if there are no children.
		 * Overriding this method is crucial, because when TreeNode.getChildren() is called, it
		 * avoid switching into the Display Thread (BusyCursorHelper.busyCursorWhile(Runnable)).
		 * This would happen once for every ParticipantTreeNode when ParticipantTreeView.updateGUI()
		 * calls treeViewer.expandAll().
		 */
		return !hasChildren();
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Implementation of interfaces
	// *

	@Override
	public Long getParticipantPK() {
		if (value == null) {
			return null;
		}

		return value.getID();
	}


	@Override
	public void registerForForeignKey() {
		// nothing to do
	}


	@Override
	public Participant getIParticipant() {
		return value;
	}


	@Override
	public Long getEventId() {
		Long eventPK = null;
		if (value != null) {
			eventPK = value.getEventId();
		}

		return eventPK;
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************


	public void sortByName() {
		sortByName = true;
		for (TreeNode treeNode : getLoadedChildren()) {
			if (treeNode instanceof ParticipantTreeNode) {
				ParticipantTreeNode participantTreeNode = (ParticipantTreeNode) treeNode;
				participantTreeNode.sortByName();
			}
		}
	}


	public void sortByNumber() {
		sortByName = false;
		for (TreeNode treeNode : getLoadedChildren()) {
			if (treeNode instanceof ParticipantTreeNode) {
				ParticipantTreeNode participantTreeNode = (ParticipantTreeNode) treeNode;
				participantTreeNode.sortByNumber();
			}
		}
	}

}
