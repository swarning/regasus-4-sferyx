package de.regasus.event.view;

import java.text.DateFormat;
import java.util.Locale;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.programme.ProgrammeCancelationTermModel;
import de.regasus.programme.cancelterm.IProgrammeCancelationTermIdProvider;
import de.regasus.ui.Activator;

public class ProgrammeCancelationTermTreeNode
	extends TreeNode<ProgrammeCancelationTermVO>
	implements EventIdProvider, IProgrammeCancelationTermIdProvider {

	protected static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

	// *************************************************************************
	// * Attributes
	// *

	private Long cancelTermPK;

	/* Just used to refresh the data of this Cancelation Term.
	 * Observing this Cancelation Term is not necessary, because the parent TreeNode
	 * is observing all its Cancelation Terms. On any change the value of this TreeNode is set and
	 * refreshTreeNode() of the parent is called.
	 */
	private ProgrammeCancelationTermModel pctModel = ProgrammeCancelationTermModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructor
	// *

	public ProgrammeCancelationTermTreeNode(
		TreeViewer treeViewer,
		ProgrammeOfferingTreeNode parent,
		ProgrammeCancelationTermVO programmeCancelationTermVO
	) {
		super(treeViewer, parent, programmeCancelationTermVO);

		cancelTermPK = programmeCancelationTermVO.getID();
	}

	// *
	// * Constructor
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return ProgrammeCancelationTermVO.class;
	}


	@Override
	public Object getKey() {
		return cancelTermPK;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			text = value.getLabel(dateFormat);
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		return ParticipantLabel.ProgrammeCancelationTerm.getString();
	}


	@Override
	public Image getImage() {
		Image image = null;

		if ( value.isCancelled() ) {
			image = IconRegistry.getImage(IImageKeys.PROGRAMME_CANCELATION_TERM_CANCELLED);
		}
		else {
			image = IconRegistry.getImage(IImageKeys.PROGRAMME_CANCELATION_TERM);
		}

		return image;
	}


	@Override
	public boolean isStrikeOut() {
		return value.isCancelled();
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
			pctModel.refresh(cancelTermPK);

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

	@Override
	public Long getEventId() {
		// parent is always a ProgrammeOfferingTreeNode because of the constructor
		return ((ProgrammeOfferingTreeNode) getParent()).getEventId();
	}


	public Long getProgrammeOfferingPK() {
		return value.getOfferingPK();
	}


	public Long getProgrammeCancelationTermPK() {
		return cancelTermPK;
	}


	@Override
	public Long getProgrammeCancleationTermId() {
		return cancelTermPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
