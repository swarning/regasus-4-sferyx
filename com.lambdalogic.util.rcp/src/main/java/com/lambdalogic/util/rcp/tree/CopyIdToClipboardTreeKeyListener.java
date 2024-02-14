package com.lambdalogic.util.rcp.tree;

import static com.lambdalogic.util.rcp.KeyEventHelper.*;

import java.util.Objects;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyAdapter;

import com.lambdalogic.util.rcp.ClipboardHelper;

public class CopyIdToClipboardTreeKeyListener extends KeyAdapter {

	private AbstractTreeViewer treeViewer;


	public CopyIdToClipboardTreeKeyListener(AbstractTreeViewer treeViewer) {
		this.treeViewer = Objects.requireNonNull(treeViewer);
	}


	@Override
	public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
		// ctrl+shift+c or ⌘+shift+c
		if ( isCopyPK(e) ) {
			IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
			if (selection != null) {
				Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof TreeNode) {
    				TreeNode<?> treeNode = (TreeNode<?>) selectedElement;
    				if (treeNode != null) {
    					Object key = treeNode.getKey();
    					if (key != null) {
    						ClipboardHelper.copyToClipboard( key.toString() );
    					}
    				}
				}
				else {
					System.err.println("The selected element is not an instance of " + TreeNode.class.getName());
				}
			}
		}
	};

}
