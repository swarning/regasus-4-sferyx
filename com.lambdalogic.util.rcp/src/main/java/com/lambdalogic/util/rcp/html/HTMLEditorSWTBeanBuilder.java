package com.lambdalogic.util.rcp.html;

import java.net.URL;
import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import sferyx.administration.editors.HTMLEditor;
import sferyx.administration.editors.HTMLEditorSWTBean;

/**
 * Create and customize a {@link HTMLEditorSWTBean}.
 */
public class HTMLEditorSWTBeanBuilder {

	public static HTMLEditorSWTBean build() {
		HTMLEditorSWTBean htmlEditorSWTBean = new HTMLEditorSWTBean();
		customize(htmlEditorSWTBean);
		return htmlEditorSWTBean;
	}


	/**
	 * Customize the HTML Editor Component:
	 * - remove main menu
	 * - remove several items from the toolbar
	 *
	 * See: https://www.sferyx.com/htmleditor/installation.htm#Customization%20of%20the%20user%20interface
	 */
	@SuppressWarnings("static-access")
	private static void customize(HTMLEditorSWTBean htmlEditorSWTBean) {
		htmlEditorSWTBean.setDefaultCharset("UFT-8");

		HTMLEditor htmlEditor = htmlEditorSWTBean.getHTMLEditorInstance();
		htmlEditor.setStatusBarVisible(false);
		htmlEditor.setMainMenuVisible(false);
		htmlEditor.setSuppressDocumentEventsOnSetContent(true);

		// Enable the handling of special script tags such as server side scripts as JSP, ASP, PHP or similar.
		htmlEditor.setHandlingOfSpecialScriptTagsEnabled(true);
		htmlEditor.setScriptTokens("<%", "%>");
		htmlEditor.setSpecialScriptDisplay(HTMLEditor.DISPLAY_SPECIAL_SCRIPTS_AS_TEXT);


		/*
		 * Calling this method is crucial, because otherwise the RCP application is not responding if a </table>
		 * appears directly before </body>.
		 *
		 * This is the actual method description:
		 * Enables/disables the automatic insertion of editable space between adjacent block elements where is
		 * difficult to place the cursor and insert text. The default is false
		 */
		htmlEditor.setInsertEditableSpace(true);


		removeToolbarItems(htmlEditorSWTBean);
		initStyle(htmlEditorSWTBean);

		internationalize(htmlEditorSWTBean);
	}


	/**
	 * Remove several items from the toolbar.
	 *
	 * This is the list of all toolbar items (see
	 * https://www.sferyx.com/htmleditor/installation.htm#Customization%20of%20the%20user%20interface):
	 *
	 * The "+" indicates that the item is visible, "-" mean that is is removed.
	 *
	 * + insertImageButton - the insert image toolbar button;
	 * + tableBtn - the insert table toolbar button;
	 * + undoButton - the undo toolbar button;
	 * + redoButton - the redo toolbar button;
	 * + insertHyperlinkButton - the insert hyperlink toolbar button;
	 * + increaseIndentButton - the increase indent toolbar button;
	 * + decreaseIndentButton - the decrease indent toolbar button;
	 * + fontSizeButton - the font properties button;
	 * + setForegroundButton - the font foreground toolbar button;
	 * + unorderedListButton - the unordered list toolbar button;
	 * + orderedListButton - the ordered list toolbar button;
	 * - newFileButton - the new file toolbar button;
	 * - openFileButton - the open file toolbar button;
	 * - saveFileButton - the save file button;
	 * - printFileButton - the print file button;
	 * + pasteButton - the paste toolbar button;
	 * + copyButton - the copy toolbar button;
	 * + cutButton - the cut toolbar button;
	 * + alignRightButton - the align right toolbar button;
	 * + alignCenterButton - the align center toolbar button;
	 * + alignLeftButton - the align left toolbar button;
	 * + fontUnderlineButton - the font underline toolbar button;
	 * + fontStrikethroughButton- the strikethrough toolbar button
	 * + fontItalicButton - the font italic toolbar button;
	 * + fontBoldButton - the font bold toolbar button;
	 * + fontStrikethroughButton - the strikethrough button on the toolbar.
	 * + copyFormattingButton - the copy formatting toolbar button;
	 * + fontsList - the fonts list toolbar combo box;
	 * + fontSizes - the font sizes toolbar combo box;
	 * + headingStyles - the headings toolbar combo box;
	 *
	 * + insertTableButton - insert table toolbar button
	 * + insertTableRowButton - insert row toolbar button
	 * + insertTableColumnButton - insert column toolbar button
	 * + deleteTableCellsButton - delete table cells toolbar button
	 * + selectTableButton - select table toolbar button
	 * + selectTableColumnButton - select table column toolbar button
	 * + selectTableRowButton - select table row toolbar button
	 * + selectTableCellButton - select table cell toolbar button
	 * + splitTableCellButton - split table cell toolbar button
	 * + mergeTableCellButton - merge table cell toolbar button
	 * + tablePropertiesButton - table properties toolbar button
	 * + tableCellPropertiesButton - table cell properties toolbar button
	 *
	 * - styleClasses - the style classes toolbar combo box
	 *
	 * + showParagraphsButton - the button for revealing paragraphs
	 * + fontBackgroundButton - font background (text highlight) button
	 * - insertEquationButton - the button for inserting mathematical equations through Sferyx EquationEditor
	 *
	 * + alignJustifyButton - the button for align justify paragraphs
	 * + superscriptButton - the button for superscipt
	 * + subscriptButton - the button for subscript
	 * + insertSymbolButton- the button for inserting symbols
	 *
	 *
	 *   --- the separators between toolbar item can remain even if the items are removed ---
	 * + tableToolbarSeparator- the separator between the table items
	 * + pasteToolbarSeparator- the separator between the pasting items
	 * + undoToolbarSeparator- the separator between the undo items
	 * - saveToolbarSeparator- the separator between save items
	 *
	 * + tableInsertItemsToolbarSeparator - the separator between the table insert items
	 * + tableSelectItemsToolbarSeparator - the separator between the table select items
	 *
	 * - printToolbarSeparator - the separator before the print item
	 * + fontToolbarSeparator - the separator between formatting items
	 * + fontStyleToolbarSeparator - the separator before the style combo
	 * + alignmentToolbarSeparator - the separator before the paragraph alignment items
	 * + listsToolbarSeparator- the separator before the list items
	 *
	 *
	 * + zoomoutTextButton - the text zoom out button
	 * + zoominTextButton - the text zoom in button
	 * - pdfExportButton - the PDF export toolbar button when enabled
	 * +
	 * + imageMapRectButton - the image map insert rectangle button
	 * + imageMapCircleButton - the image map insert circle button
	 * + imageMapPolyButton - the image map insert polygon button
	 *
	 * + replaceTextButton - replace text toolbar button
	 * + findTextButton - find text toolbar button
	 * + bordersToolbarButton - edit borders toolbar button
	 *
	 * + increaseFontSizeButton - the increase font size button
	 * + decreaseFontSizeButton - the decrease font size button
	 * - spellCheckerButton - the spellchecker button
	 * - spellCheckerAYTButton - the as-you-type spellchecker button
	 * - printPreviewButton - the print preview button
	 * + pageLayoutButton - the page layout view button
	 * - saveasDocxButton- returns whether the embedding of images is enabled. - save as MS docx file button on the toolbar
	 * - openDocxButton- returns whether the embedding of images is enabled. - open MS docx file button on the toolbar
	 */
	private static void removeToolbarItems(HTMLEditorSWTBean htmlEditorSWTBean) {
		htmlEditorSWTBean.setRemovedToolbarItems(
			  "newFileButton, openFileButton, saveFileButton, printFileButton"
			+ ", styleClasses"
			+ ", insertEquationButton"
			+ ", saveToolbarSeparator, printToolbarSeparator"
			+ ", pdfExportButton"
			+ ", spellCheckerButton, spellCheckerAYTButton, printPreviewButton, pageLayoutButton, saveasDocxButton, openDocxButton"
		);
	}


	private static void initStyle(HTMLEditorSWTBean htmlEditorSWTBean) {
		/* Actually we could load a CSS file.
		 * But the following example only loads the default, which does not reflect changes that the users made.
		URL cssURL = StandardStyleProvider.class.getResource("standard.default.css");
		htmlEditorSWTBean.loadExternalStyleSheet( cssURL.toString() );
		*/
	}


	private static void internationalize(HTMLEditorSWTBean htmlEditorSWTBean) {
		Locale locale = Locale.getDefault();
		String language = locale.getLanguage();

		try {
			Bundle bundle = Platform.getBundle("com.sferyx");
			URL transUrl = bundle.getEntry("/translations/" + language + "-translation.txt");
    		int contentLength = transUrl.openConnection().getContentLength();

    		if (contentLength > 0) {
    			htmlEditorSWTBean.getHTMLEditorInstance().loadInterfaceLanguageFile( transUrl.toString() );
    		}
		}
		catch (Exception e) {
			System.err.println( e.getMessage() );
		}
	}

}
