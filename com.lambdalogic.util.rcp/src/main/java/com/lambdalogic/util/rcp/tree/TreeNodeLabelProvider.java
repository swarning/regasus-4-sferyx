package com.lambdalogic.util.rcp.tree;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;

/**
 * This kind of label provider offers also tooltips for trees.
 * <p>
 * Attention: In order to make it work, there must be this call on the involved viewer:
 * <code>ColumnViewerToolTipSupport.enableFor(treeViewer);</code>
 */
public class TreeNodeLabelProvider extends StyledCellLabelProvider {

	private Styler strikeOutStyler = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.strikeout = true;
		}
	};


	@Override
	public void update(ViewerCell cell) {
		try {
			Object element = cell.getElement();
			if (element instanceof TreeNode) {
				TreeNode node = (TreeNode) element;

				String text = node.getText();
				Image image = node.getImage();

				StyledString styledString = new StyledString(text);
				if ( node.isStrikeOut() ) {
					styledString.setStyle(0, text.length(), strikeOutStyler);
				}

				cell.setText(text);
				cell.setImage(image);
				cell.setStyleRanges( styledString.getStyleRanges() );

			}
		}
		catch (StringIndexOutOfBoundsException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.update(cell);
	}


	@Override
	public String getToolTipText(Object element) {
		if (element instanceof TreeNode) {
			TreeNode node = (TreeNode) element;
			return node.getToolTipText();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipShift(java.lang.Object)
	 */
	@Override
	public Point getToolTipShift(Object object) {
		return new Point(5,5);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipDisplayDelayTime(java.lang.Object)
	 */
	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipTimeDisplayed(java.lang.Object)
	 */
	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return 5000;
	}

}
