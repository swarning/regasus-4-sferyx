package de.regasus.event.command.copypaste;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.util.rcp.ClassKeyNameTransfer;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

/**
 * Puts into the system clipboard the class names and the keys of VOs and CVOs that are selected in the
 * master data tree.
 * <p>
 * Nothing happens apart from that; only when the {@link PasteCommandHandler} fetches that data
 * in the proper place, the server is then contacted to copy the VOs and CVOs.
 * </p>
 */
public class CopyCommandHandler extends AbstractHandler {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);

			// The nodes that are currently selected
			List<TreeNode<?>> selectedTreeNodes = SelectionHelper.toList(selection);

			log.debug("Selected TreeNodes: " + selectedTreeNodes);

			List<String> transferredObjectData = new ArrayList<>();
			StringBuilder transferredTextData = new StringBuilder();

			for (TreeNode<?> treeNode : selectedTreeNodes) {
				Object value = treeNode.getValue();

				// If the nodes contains something that may be copied...
				if (CopyPasteHelper.isCopyOK(value)) {

					// ...gather it's identifying data to be stored in the clipboard.
					String className = value.getClass().getName();
					String key = treeNode.getKey().toString();
					String text = treeNode.getText();

					transferredObjectData.add(className);
					transferredObjectData.add(key);
					transferredObjectData.add(text);


					if (transferredTextData.length() > 0) {
						transferredTextData.append(", ");
					}
					String textContent = buildTextContent(treeNode);
					transferredTextData.append(textContent);
				}
			}

			// Now store in clipboard; or beep if there was nothing that could be copied
			Display display = HandlerUtil.getActiveShell(event).getDisplay();
			if ( !transferredObjectData.isEmpty() ) {
				String[] data = transferredObjectData.toArray(new String[0]);
				String text = transferredTextData.toString();

				log.debug("Copy to clipboard: " + text);

				Clipboard clipboard = new Clipboard(display);
				clipboard.setContents(
					new Object[] {
						data,
						text
					},
					new Transfer[] {
						ClassKeyNameTransfer.getInstance(),
						TextTransfer.getInstance()
					}
				);
			}
			else {
				display.beep();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	protected String buildTextContent(TreeNode<?> treeNode) {
		String key = treeNode.getKey().toString();
		String text = treeNode.getText();
		String simpleClassName = treeNode.getValue().getClass().getSimpleName();

		return new StringBuilder(128)
			.append(simpleClassName)
			.append(" [id=").append(key).append(", name=").append(text).append("]")
			.toString();
	}

}
