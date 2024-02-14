package com.lambdalogic.util.rcp.dnd;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Text;

/**
 * Inserts dragged text to this listener's SWT {@link Text} widget when added to a DropTarget for that same widget.
 * <pre>
 * DropTarget target = new DropTarget(text, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
 * target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
 * target.addDropListener(new TextDropListener(text));
 * </pre>
 *
 * @author manfred
 *
 */
public class TextDropListener extends DropTargetAdapter {

	private Text text;


	public TextDropListener(Text text) {
		this.text = text;
	}


	@Override
	public void dragEnter(DropTargetEvent e) {
		if (e.detail == DND.DROP_DEFAULT) {
			e.detail = DND.DROP_COPY;
		}
	}


	@Override
	public void dragOperationChanged(DropTargetEvent e) {
		if (e.detail == DND.DROP_DEFAULT) {
			e.detail = DND.DROP_COPY;
		}
	}


	@Override
	public void drop(DropTargetEvent e) {
		text.insert((String)e.data);
	}

}
