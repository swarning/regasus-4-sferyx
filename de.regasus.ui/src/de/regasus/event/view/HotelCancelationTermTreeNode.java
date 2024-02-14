package de.regasus.event.view;

import java.text.DateFormat;
import java.util.Locale;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.hotel.HotelCancelationTermModel;
import de.regasus.ui.Activator;

public class HotelCancelationTermTreeNode
extends TreeNode<HotelCancelationTermVO>
implements EventIdProvider {

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
	private HotelCancelationTermModel hctModel = HotelCancelationTermModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructor
	// *

	public HotelCancelationTermTreeNode(
		TreeViewer treeViewer,
		HotelOfferingTreeNode parent,
		HotelCancelationTermVO hotelCancelationTermVO
	) {
		super(treeViewer, parent, hotelCancelationTermVO);

		cancelTermPK = hotelCancelationTermVO.getID();
	}

	// *
	// * Constructor
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return HotelCancelationTermVO.class;
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
		return HotelLabel.HotelCancelationTerm.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.HOTEL_CANCELATION_TERM);
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
			hctModel.refresh(cancelTermPK);

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

	public Long getHotelOfferingPK() {
		return value.getOfferingPK();
	}


	@Override
	public Long getEventId() {
		// parent is always a HotelOfferingTreeNode because of the constructor
		return ((HotelOfferingTreeNode) getParent()).getEventId();
	}


	public Long getHotelCancelationTermPK() {
		return cancelTermPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
