package com.lambdalogic.util.rcp.html;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import sferyx.administration.editors.HTMLEditorSWTBean;


public class LazyHtmlEditor extends LazyComposite {

	private ModifySupport modifySupport = new ModifySupport(this);
	private DocumentListener documentListener = new DocumentListener(modifySupport);

	private HTMLEditorSWTBean htmlEditorSWTBean;


	/**
	 * HTML content is stored here as long as this LazyComposite is not initialized yet and htmlEditorSWTBean therefore is null.
	 */
	private String html;


	public LazyHtmlEditor(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND | SWT.EMBEDDED);
	}


	@Override
	protected void createPartControl() throws Exception {
		setLayout( new FillLayout() );

		// create Frame at first, otherwise the it hangs up for unknown reasons
		Frame frame = SWT_AWT.new_Frame(this);

		htmlEditorSWTBean = HTMLEditorSWTBeanBuilder.build();

		/* Let the HTMLEditor ignore cmd+s and cmd+w on Mac and strg+s on Windows
		 *
		 * If key is added to the key event mask only events generated from this key will be dispatched
		 * as custom events to the underling structure, others will be ignored.
		 * To reset the event mask use resetKeyEventMask method.
		 *
		 * keyCode is the code of the key on the keyboard. Example Enter is 13, Space bar is 32 etc.
		 *
		 * editorComposite is the composite SWT component containing the editor.
		 */
   		htmlEditorSWTBean.addKeyToKeyEventMask(this, java.awt.event.KeyEvent.VK_S, false, false, false, true, false);
		htmlEditorSWTBean.addKeyToKeyEventMask(this, java.awt.event.KeyEvent.VK_W, false, false, false, true, false);

		setHtml(html);

		htmlEditorSWTBean.getHTMLEditorInstance().addDocumentListener(documentListener);

		frame.add(htmlEditorSWTBean);
	}


	public String getHtml() {
		if (htmlEditorSWTBean != null) {
			html = htmlEditorSWTBean.getContent();
		}

		return html;
	}


	public void setHtml(String html) {
		this.html = html;

		if (htmlEditorSWTBean != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						/* Issue 1:   Editor gets dirty after saving
						 *
						 * Issue 2.1: Focus lost
						 *            focus is visible but not really there --> after saving, keyboard events are ignored
						 *            Only DEL is working!
						 *            user has to click into the view to set the focus
						 *
						 * Issue 2.2: Focus lost
						 *            focus is visible but not really there --> after saving, keyboard events are ignored
						 *            user has to click another view and then the editor to set the focus
						 */

//						if ( htmlEditorSWTBean.getHTMLEditorInstance().isSourceEditorVisible() ) {
//							htmlEditorSWTBean.getHTMLEditorInstance().setContentAsynchronously(html);
//						}
//						else {
//							htmlEditorSWTBean.setContent(html);
//						}


						/* Do not call htmlEditorSWTBean.setContent(html), because it will fire events asynchronous in a
						 * separate thread (AWT thread) what makes ignoring them ineffective.
						 */

						/* EmailTemplate
						 * 		visual
						 * 			Focus is visible but not really there --> after save keyboard events are ignored
						 * 			User has to click into the editor to set the focus.
						 *      source
						 *          Nach Bearbeitung im Visual Editor not reacting.
						 *          Wenn gleich im Source gearbeitet wird ist der Editor nach dem Speichern dirty.
						 */
//						htmlEditorSWTBean.setContent(html);

						// do not call  htmlEditorInstance.setContent_(html)
						// It makes the client not reacting after refreshing an editor under Windows

						/* EmailTemplate
						 * 		visual
						 * 			Focus is visible but not really there --> after save keyboard events are ignored
						 * 			User has to click into the editor to set the focus.
						 *      source
						 *          Nach Bearbeitung im Visual Editor not reacting.
						 *          Wenn gleich im Source gearbeitet wird ist der Editor nach dem Speichern dirty.
						 */
//						htmlEditorSWTBean.getHTMLEditorInstance().setContent(html);


						/* EmailTemplate
						 *     visual
						 *         Fokus nach dem Speichern verschwunden.
						 *         Direkter Click in den Editor hilft nicht. Es muss zuvor in einen anderen Part gewechselt werden.
						 *      source
						 *          Nach Bearbeitung im Visual Editor not reacting.
						 *          Wenn gleich im Source gearbeitet wird ist ist das Verhalten wie im Visual Editor.
						 */
//						htmlEditorSWTBean.getHTMLEditorInstance().setContent_(html);
						htmlEditorSWTBean.getHTMLEditorInstance().setContentAsynchronously(html);
					}
					catch (Exception e) {
						ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

}
