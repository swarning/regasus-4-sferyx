package com.lambdalogic.util.rcp;

import static com.lambdalogic.util.StringHelper.isEmpty;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class ClipboardHelper {

	public static void copyToClipboard(String text) {
		Clipboard clipboard = new Clipboard( Display.getDefault() );
		try {
    		if ( isEmpty(text) ) {
    			clipboard.clearContents();
    		}
    		else {
        		Object[] data = new Object[] { text };
				Transfer[] dataTypes = new Transfer[] { TextTransfer.getInstance() };
				clipboard.setContents(data, dataTypes);
    		}
		}
		finally {
			clipboard.dispose();
		}
	}


	public static String readStringFromClipboard() {
		String clipboardContent = null;

		Clipboard clipboard = new Clipboard( Display.getDefault() );
		try {
    		Object contents = clipboard.getContents( TextTransfer.getInstance() );
    		if (contents instanceof String) {
    			clipboardContent = (String) contents;
    		}
		}
		finally {
			clipboard.dispose();
		}

		return clipboardContent;
	}


	public static Object readFromClipboard(Transfer transfer) {
		Object content = null;

		Clipboard clipboard = new Clipboard( Display.getDefault() );
		try {
			content = clipboard.getContents(transfer);
		}
		finally {
			clipboard.dispose();
		}

		return content;
	}

}
