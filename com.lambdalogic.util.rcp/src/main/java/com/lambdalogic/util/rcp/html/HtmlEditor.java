package com.lambdalogic.util.rcp.html;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import sferyx.administration.editors.HTMLEditorSWTBean;


public class HtmlEditor extends Composite {

	private ModifySupport modifySupport = new ModifySupport(this);
	private DocumentListener documentListener = new DocumentListener(modifySupport);

	private HTMLEditorSWTBean htmlEditorSWTBean;


	public HtmlEditor(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND | SWT.EMBEDDED);

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				createPartControl();
			}
		});

	}


	protected void createPartControl() {
		setLayout( new FillLayout() );

		// create Frame at first, otherwise the it hands up for unknown reasons
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
		if ( SystemHelper.isWindows() ) {
    		htmlEditorSWTBean.addKeyToKeyEventMask(this, java.awt.event.KeyEvent.VK_S, false, false, false, true, false);
		}
		htmlEditorSWTBean.addKeyToKeyEventMask(this, java.awt.event.KeyEvent.VK_W, false, false, false, true, false);

		htmlEditorSWTBean.getHTMLEditorInstance().addDocumentListener(documentListener);

		frame.add(htmlEditorSWTBean);
	}


	public String getHtml() {
		return htmlEditorSWTBean.getContent();
	}


	public void setHtml(String html) {

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					htmlEditorSWTBean.getHTMLEditorInstance().setContentAsynchronously(html);
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

}
