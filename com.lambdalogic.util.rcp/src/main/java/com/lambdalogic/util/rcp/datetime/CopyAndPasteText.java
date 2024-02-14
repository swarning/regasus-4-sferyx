package com.lambdalogic.util.rcp.datetime;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ClipboardHelper;

public class CopyAndPasteText extends Text {

	private Collection<CopyAndPasteListener> listenerCol = new HashSet<>();


	public CopyAndPasteText(Composite parent, int style) {
		super(parent, style);
	}


	/**
	 * Add the given {@link CopyAndPasteListener} as observer.
	 *
	 * @param listener
	 */
	public void addCopyAndPasteListener(CopyAndPasteListener listener) {
		listenerCol.add(listener);
	}


	/**
	 * Remove the given {@link CopyAndPasteListener} as observer.
	 *
	 * @param listener
	 */
	public void removeCopyAndPasteListener(CopyAndPasteListener listener) {
		listenerCol.remove(listener);
	}


	@Override
	public void copy() {
		CopyAndPasteEvent event = null;
		String clipboardText = null;

		if ( ! listenerCol.isEmpty()) {
			// build Event with selected text
			clipboardText = getSelectionText();
			event = new CopyAndPasteEvent(clipboardText);

			for (CopyAndPasteListener listener : listenerCol) {
				listener.beforeCopy(event);
			}
		}


		if (event != null && ! event.isCanceled()) {
			// let the Text widget do its copy operation, what will set its actually selected text to the clipboard
			super.copy();


			if (clipboardText != event.getText()) {
				// set text from event to clipboard (overwrite the text that the widget has set), so CopyAndPasteListeners are able to change it
				clipboardText = event.getText();
				setTextToClipboard(clipboardText);
			}

			event = new CopyAndPasteEvent(clipboardText);

			for (CopyAndPasteListener listener : listenerCol) {
				listener.afterCopy(event);
			}
		}
	}


	@Override
	public void cut() {
		CopyAndPasteEvent event = null;
		String clipboardText = null;

		if ( ! listenerCol.isEmpty()) {
			// build Event with selected text
			clipboardText = getSelectionText();
			event = new CopyAndPasteEvent(clipboardText);

			for (CopyAndPasteListener listener : listenerCol) {
				listener.beforeCut(event);
			}
		}


		if (event != null && ! event.isCanceled()) {
			// let the Text widget do its cut operation, what will set its actually selected text to the clipboard
			super.cut();


			if (clipboardText != event.getText()) {
				// set text from event to clipboard (overwrite the text that the widget has set), so CopyAndPasteListeners are able to change it
				clipboardText = event.getText();
				setTextToClipboard(clipboardText);
			}

			// build Event with text from clipboard
			event = new CopyAndPasteEvent(clipboardText);

			for (CopyAndPasteListener listener : listenerCol) {
				listener.afterCopy(event);
			}
		}
	}


	@Override
	public void paste() {
		CopyAndPasteEvent event = null;
		String clipboardText = null;

		if ( ! listenerCol.isEmpty()) {
			// build Event with text from clipboard
			clipboardText = getTextFromClipboard();
			event = new CopyAndPasteEvent(clipboardText);

			for (CopyAndPasteListener listener : listenerCol) {
				listener.beforePaste(event);
			}

			if ( ! event.isCanceled() && clipboardText != event.getText()) {
				// set text from event to clipboard, so CopyAndPasteListeners are able to change it
				clipboardText = event.getText();
				setTextToClipboard(clipboardText);
			}
		}


		if (event != null && ! event.isCanceled()) {
			// let the Text widget do the paste operation
			super.paste();


			// build Event with text from clipboard
			event = new CopyAndPasteEvent(clipboardText);

			for (CopyAndPasteListener listener : listenerCol) {
				listener.afterPaste(event);
			}
		}
	}


	private String getTextFromClipboard() {
		String clipboardText = ClipboardHelper.readStringFromClipboard();
		return StringHelper.trim(clipboardText);
	}


	private void setTextToClipboard(String text) {
		ClipboardHelper.copyToClipboard(text);
	}



	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
