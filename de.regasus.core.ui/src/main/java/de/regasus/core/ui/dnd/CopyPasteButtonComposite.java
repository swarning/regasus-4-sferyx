package de.regasus.core.ui.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.UtilI18N;

public class CopyPasteButtonComposite extends Composite {

	private Button copyButton;
	private Button pasteButton;


	public CopyPasteButtonComposite(Composite parent, int style, boolean horizontal) {
		super(parent, style);

		if (horizontal) {
			setLayout( new FillLayout(SWT.HORIZONTAL) );
		}
		else {
			setLayout( new FillLayout(SWT.VERTICAL) );
		}

		copyButton = createCopyButton(this);
		pasteButton = createPasteButton(this);
	}


	public CopyPasteButtonComposite(Composite parent, int style) {
		this(parent, style, true);
	}


	public Button getCopyButton() {
		return copyButton;
	}

	public Button getPasteButton() {
		return pasteButton;
	}


	public static Button createCopyButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);

		Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY);
		button.setImage(image);
		button.setToolTipText(UtilI18N.CopyToClipboard);
		return button;
	}


	public static Button createPasteButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);

		Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_PASTE);
		button.setImage(image);
		button.setToolTipText(UtilI18N.PasteFromClipboard);
		return button;
	}

}
