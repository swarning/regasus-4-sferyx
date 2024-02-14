package com.lambdalogic.util.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Text;

/**
 * A TraverseListener to override tab behavior to traverse out of a text control.
 * The default behaviour of Text-Widgets with the type SWT.MULTI is, that a TAB
 * doesn't traverse out of the control but inserts a the character TAB.
 * Users may traverse out of the control with CTRL+TAB, but the fewest know this.
 * With this TraverseListener Text-Widgets with the type SWT.MULTI can be traversed
 * with TAB like other widgets.
 * However, the user can insert the tab character with CTRL+TAB.
 * 
 * This is a typical code snippet:
 * 
 * Text text = new Text(this, SWT.MULTI | SWT.BORDER);
 * text.addTraverseListener(new MultiLineTextTraverseListener(text));
 * 
 * 
 * See also: http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet241.java?view=co
 * 
 * 
 * @author sacha
 *
 */
public class MultiLineTextTraverseListener implements TraverseListener {

	private Text text;
	
	
	public MultiLineTextTraverseListener(Text text) {
		super();
		this.text = text;
	}

	
	public void keyTraversed(TraverseEvent e) {
		if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
			if (e.stateMask == 0) {
				e.doit = true;
			}
			else if (e.stateMask == SWT.CTRL) {
				// insert tab character
				String s = text.getText();
				int pos = text.getCaretPosition();
				StringBuffer sb = new StringBuffer(s.length() + 1);
				sb.append(s.substring(0, pos));
				sb.append('\t');
				sb.append(s.substring(pos));
				text.setText(sb.toString());
				pos++;
				text.setSelection(pos, pos);

				e.doit = false;
			}
		}
	}

}
