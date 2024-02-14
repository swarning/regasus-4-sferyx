package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.invoice.data.CancelationTermVO;
import com.lambdalogic.messeinfo.invoice.data.CancelationTermVO_Start_End_Comparator;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.programme.ProgrammeCancelationTermModel;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.offering.IProgrammeOfferingIdProvider;
import de.regasus.ui.Activator;

public class ProgrammeOfferingTreeNode
	extends TreeNode<ProgrammeOfferingVO>
	implements EventIdProvider, IProgrammeOfferingIdProvider, CacheModelListener<Long> {

	private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

	// *************************************************************************
	// * Attributes
	// *

	private Long programmeOfferingPK;

	/* Just used to refresh the data of this Programme Offering.
	 * Observing is not necessary, because the parent TreeNode is observing all its Programme Offerings.
	 * On any change the value of this TreeNode is set and the parent calls refreshTreeNode().
	 */
	private ProgrammeOfferingModel poModel = ProgrammeOfferingModel.getInstance();

	// data of child TreeNodes
	private ProgrammeCancelationTermModel pctModel = ProgrammeCancelationTermModel.getInstance();

	// used to get the name of the referenced Participant Type in getText(), observed to keep text label up-to-date
	private ParticipantTypeModel ptModel = ParticipantTypeModel.getInstance();

	/* ignore ModifyEvent from ProgrammeCancelationTermModel that are fired when this TreeNode
	 * requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public ProgrammeOfferingTreeNode(
		TreeViewer treeViewer,
		ProgrammePointTreeNode parent,
		ProgrammeOfferingVO programmeOfferingVO
	) {
		super(treeViewer, parent, programmeOfferingVO);

		programmeOfferingPK = programmeOfferingVO.getID();

		// observe Programme Offering
		poModel.addListener(this, programmeOfferingPK);

		// observe Programme Cancelation Terms that belong to this Programme Offering
		pctModel.addForeignKeyListener(this, programmeOfferingPK);

		// observe all Participant Types to keep the text label up-to-date
		ptModel.addListener(this);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			poModel.removeListener(this, programmeOfferingPK);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			pctModel.removeForeignKeyListener(this, programmeOfferingPK);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			ptModel.removeListener(this);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}

	// *
	// * Constructors and dispose()
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return ProgrammeOfferingVO.class;
	}


	@Override
	public Object getKey() {
		return programmeOfferingPK;
	}


	@Override
	public String getText() {
		/* Build the text label like this:
		 * <Offering description>, <Partcipant Type name>, amount, startTime...endTime
		 */
		StringBuilder text = new StringBuilder(100);

		try {
			final int CHAR_COUNT = 50;

			// append fist CHAR_COUNT characters of Offering description (if available)
			LanguageString description = value.getDescription();
			if (description != null) {
				String s = description.getString();
				if (s != null) {
					if (s.length() > CHAR_COUNT) {
						s = s.substring(0, CHAR_COUNT) + "...";
					}
					if (s.contains("\n")) {
						s = s.substring(0, s.indexOf("\n")) + "...";
					}
					text.append(s);
				}
			}

			// append Partcipant Type name (if available and if it is not already part of the description)
			Long participantTypePK = value.getParticipantTypePK();
			if (participantTypePK != null) {
				ParticipantType participantType = ptModel.getParticipantType(participantTypePK);
				if (participantType != null) {
					String name = participantType.getName().getString();

					// append participant type name only if it is not part of the description (MIRCP-682)
					if (text.indexOf(name) < 0) {
						StringHelper.appendIfNeeded(text, ", ");
						text.append(name);
					}
				}
			}

			// append amount
			StringHelper.appendIfNeeded(text, ", ");
			CurrencyAmount currencyAmount = value.getCurrencyAmountGross();
			text.append(currencyAmount.format(false, true));


			// append startTime and endTime (if available)
			String dates = null;
			Date startTime = value.getStartTime();
			if (startTime != null) {
				dates = dateFormat.format(startTime);
				dates += "...";
			}

			Date endTime = value.getEndTime();
			if (endTime != null) {
				if (dates == null) {
					dates = "...";
				}
				dates += dateFormat.format(endTime);
			}
			if (dates != null) {
				StringHelper.appendIfNeeded(text, ", ");
				text.append(dates);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return text.toString();
	}


	@Override
	public String getToolTipText() {
		return ParticipantLabel.ProgrammeOffering.getString();
	}


	@Override
	public Image getImage() {
		Image image = null;
		try {
			// load CVO to know if the offering is fully booked
			ProgrammeOfferingCVO programmeOfferingCVO = poModel.getProgrammeOfferingCVO(programmeOfferingPK);

			if (programmeOfferingCVO != null && programmeOfferingCVO.isCancelled()) {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_OFFERING_CANCELLED);
			}
			else if (programmeOfferingCVO != null && programmeOfferingCVO.isDisabled()) {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_OFFERING_DISABLED);
			}
			else if (programmeOfferingCVO != null && programmeOfferingCVO.isFullyBooked()) {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_OFFERING_FULLY_BOOKED);
			}
			else {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_OFFERING);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return image;
	}


	@Override
	public boolean isStrikeOut() {
		return value.isCancelled();
	}


	@Override
	protected void loadChildren() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				_loadChildren();
			}
		});
	}


	private void _loadChildren() {
		try {
			ignoreDataChange = true;

    		// load child data from model
			List<ProgrammeCancelationTermVO> pctVOs = pctModel.getProgrammeCancelationTermVOsByProgrammeOfferingPK(value.getID());
			if (pctVOs == null) {
				pctVOs = emptyList();
			}

			/*
			 * Do not just remove all child-nodes and build new ones, because this will close
			 * all nodes, the user already opened. Instead replace the data of all nodes that
			 * still exist, remove nodes of entities that do not exist anymore and create new
			 * nodes for new entities.
			 */

			// If there aren't any children create a TreeNode for every ProgrammeCancelationTerm.
			if (!hasChildren()) {
				// resize children-List
				ensureCapacityOfChildren(pctVOs.size());

				for (ProgrammeCancelationTermVO pctVO : pctVOs) {
					ProgrammeCancelationTermTreeNode treeNode = new ProgrammeCancelationTermTreeNode(
						treeViewer,
						this,
						pctVO
					);

					// add TreeNode to list of children
					addChild(treeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, ProgrammeCancelationTermVO> pctMap = AbstractVO.abstractVOs2Map(pctVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<ProgrammeCancelationTermTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (ProgrammeCancelationTermTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					ProgrammeCancelationTermVO pctVO = pctMap.get(treeNode.getProgrammeCancelationTermPK());

					if (pctVO != null) {
						// Set new data to the TreeNode
						treeNode.setValue(pctVO);
						// Remove data from map, so after the for-block the map
						// only contains new values
						pctMap.remove(pctVO.getID());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + pctMap.size());

				// add new TreeNodes for each new value
				for (ProgrammeCancelationTermVO pctVO : pctMap.values() ) {
					ProgrammeCancelationTermTreeNode treeNode = new ProgrammeCancelationTermTreeNode(
						treeViewer,
						this,
						pctVO
					);

					// add TreeNode to list of children
					addChild(treeNode);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			poModel.refresh(programmeOfferingPK);

			// refresh data of child TreeNodes
			refreshChildren();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
    			// refresh data of children
    			pctModel.refreshForeignKey(programmeOfferingPK);

    			// refresh data of our grandchildren
    			refreshGrandChildren();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return CancelationTermVO_Start_End_Comparator.getInstance().compare(
			(CancelationTermVO) treeNode1.getValue(),
			(CancelationTermVO) treeNode2.getValue()
		);
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Implementation of interfaces
	// *

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!ServerModel.getInstance().isLoggedIn()) {
			// do nothing, because all TreeNodes will be removed from root TreeNode
				return;
			}

			if (ignoreDataChange) {
				return;
			}


			if (event.getSource() == pctModel) {
				// If we receive an Event from HotelCancelationTermModel, children are reloaded
			reloadChildren();
		}
		else if (event.getSource() == ptModel && value.getParticipantTypePK() != null) {
			// check if the Participant Type referenced by this Programme Offering has changed
			if ( event.getKeyList().contains( value.getParticipantTypePK() ) ) {
				// refresh text label
				updateTreeViewer();
			}
		}
		else if (event.getSource() == poModel) {
			refreshTreeViewer();
		}
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	@Override
	public Long getEventId() {
		// parent is always a ProgrammePointTreeNode because of the constructor
		return ((ProgrammePointTreeNode) getParent()).getEventId();
	}


	public Long getProgrammePointPK() {
		return value.getProgrammePointPK();
	}


	public Long getProgrammeOfferingPK() {
		return programmeOfferingPK;
	}


	@Override
	public Long getProgrammeOfferingId() {
		return programmeOfferingPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
