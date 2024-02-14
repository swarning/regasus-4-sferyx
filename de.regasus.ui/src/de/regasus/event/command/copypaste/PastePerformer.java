package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

public interface PastePerformer {

	<T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception;

}
