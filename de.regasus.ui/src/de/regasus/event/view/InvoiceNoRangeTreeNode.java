package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.ui.Activator;


/**
 * A node representing an Invoice Number Range.
 */
public class InvoiceNoRangeTreeNode
extends TreeNode<InvoiceNoRangeCVO>
implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long inrPK;

	/* Just used to refresh the data of this Invoice No Range.
	 * Observing this Invoice No Range is not necessary, because the parent TreeNode is observing all
	 * its Invoice No Ranges. On any change the value of this TreeNode is set and refreshTreeNode()
	 * of the parent is called.
	 */
	private InvoiceNoRangeModel inrModel = InvoiceNoRangeModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// **************************************************************************
	// * Constructors
	// *

	public InvoiceNoRangeTreeNode(
		TreeViewer treeViewer,
		InvoiceNoRangeListTreeNode parent,
		InvoiceNoRangeCVO invoiceNoRangeCVO
	) {
		super(treeViewer, parent);

		value = invoiceNoRangeCVO;
		inrPK = value.getPK();
	}

	// *
	// * Constructors
	// **************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return InvoiceNoRangeVO.class;
	}


	@Override
	public Object getKey() {
		return inrPK;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			text = value.getVO().getName();
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		return InvoiceLabel.InvoiceNoRange.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.INVOICERANGE);
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
			inrModel.refresh(inrPK);

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
		return value.getEventPK();
	}


	protected Long getInvoiceNoRangePK() {
		return inrPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
