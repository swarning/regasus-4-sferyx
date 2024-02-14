package com.lambdalogic.util.rcp.widget;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.i18n.ILanguageProvider;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.ColorHelper;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.i18n.I18NText;

public class SWTHelper {

	public static final Image REQUIRED_IMAGE =
		FieldDecorationRegistry.getDefault().
		getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage();

	public static final Image ERROR_IMAGE =
		FieldDecorationRegistry.getDefault().
		getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

	public static final String REQUIRED_TEXT = UtilI18N.InputRequired;

	/**
	 * Computes the height of a Text widget that it needs to show the given count of text lines, depending on the
	 * currently visible font.
	 */
	public static int computeTextWidgetHeightForLineCount(Text text, int lineCount) {
		GC gc = new GC(text);
		FontMetrics fm = gc.getFontMetrics();
		int height = fm.getHeight() * lineCount;

		// add space for top and bottom
		height += 4;

		// add space between lines
		int space = height / 30;
		height += space;

		gc.dispose();
		return height;
	}


	/**
	 * Creates a ScrolledComposite and a content Composite, configured such that they both know
	 * each other.
	 */
	public static Composite createScrolledContentComposite(Composite parent) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setLayout(new FillLayout());

		Composite contentComposite = new Composite(scrolledComposite, SWT.NONE);
		contentComposite.setLayout(new FillLayout());
		scrolledComposite.setContent(contentComposite);
		return contentComposite;
	}


	public static void refreshSuperiorScrollbar(Control control) {
		Control parent = control;
		while (parent != null) {
			parent = parent.getParent();
			if (parent instanceof ScrolledComposite) {
				ScrolledComposite scrolledComposite = (ScrolledComposite) parent;

				// calculate new height based on current width
				Control contentControl = scrolledComposite.getContent();
				Rectangle clientArea = scrolledComposite.getClientArea();
				Point size = contentControl.computeSize(clientArea.width, SWT.DEFAULT);

				// set new height as minHeight
				scrolledComposite.setMinHeight(size.y);
			}
			else if (parent instanceof ExpandBar) {
				ExpandBar expandBar = (ExpandBar) parent;
				if ((expandBar.getStyle() & SWT.V_SCROLL) != SWT.NONE) {
					computeItemHeights(expandBar);
    				expandBar.getParent().layout(true, true);
    				return;
				}
			}
		}
	}


	/**
	 * Update the height of all {@link ExpandItem} of the given {@link ExpandBar}.
	 */
	public static void computeItemHeights(ExpandBar expandBar) {
		/*
    	 * MIRCP-1935 - Don't use SWT.DEFAULT for width hint but instead the actual width
    	 * to compute the proper height; ignore width in situations where it is 0 because
    	 * it is not yet known.
		 */
		int width =  expandBar.getParent().getSize().x;
		if (width != 0) {
			for (ExpandItem item : expandBar.getItems()) {
				Point size = item.getControl().computeSize(width, SWT.DEFAULT);
				item.setHeight(size.y);
			}
		}
	}



	public static void scrollSuperiorScrollbarToEnd(Control control) {
		Control parent = control;
		while (parent != null) {
			parent = parent.getParent();
			if (parent instanceof ScrolledComposite) {
				ScrolledComposite scrolledComposite = (ScrolledComposite) parent;
				scrolledComposite.setOrigin(0, Integer.MAX_VALUE);
				return;
			}
		}
	}


	/**
	 * Computes the width of a Text widget that it needs to show the given number if characters, depending on the
	 * currently visible font.
	 */
	public static int computeTextWidgetWidthForCharCount(Control control, int charCount) {
		GC gc = new GC(control);
		FontMetrics fm = gc.getFontMetrics();
		int width = (fm.getAverageCharWidth() + 1) * charCount;
		gc.dispose();
		return width + 6;
	}


	/**
	 * Layouts the given composite after the given time in milliseconds.
	 * This is a workaround for the stupid problem that a table that is opened in a wizard decides
	 * to need horizontal scrollbars instead of shrinking the column widths a bit.
	 */
	public static void deferredLayout(final int millis, final Composite composite) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(millis);
				}
				catch (Exception e) {
					System.err.println(e);
				}
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						composite.layout();
					}
				});
			}
		}).start();
	}


	public static void recursiveLayout(Composite composite) {
		Composite topComposite = null;
		while (composite != null) {
			topComposite = composite;
			composite = composite.getParent();
		}

		if (topComposite != null) {
			topComposite.layout(true, true);
		}
	}


	public static Label fillGridLayout(int numberOfColumns, Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, numberOfColumns, 1));
		return label;
	}


	public static Label createLabel(Composite composite, String labelText, boolean required) {
		Label label = new Label(composite, SWT.RIGHT);
		GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		label.setLayoutData(layoutData);

		label.setText( prepareLabelText(labelText) );

		if (required) {
			makeBold(label);
		}

		return label;
	}


	public static Label createLabel(Composite composite, I18NString i18nString) {
		return createLabel(composite, i18nString, false /*required*/);
	}


	public static Label createLabel(Composite composite, I18NString i18nString, boolean required) {
		return createLabel(composite, i18nString.getString(), required);
	}


	public static Label createLabel(Composite composite, String labelText) {
		return createLabel(composite, labelText, false);
	}


	/**
	 * Replace every "&" by "&&".
	 *
	 * https://download.eclipse.org/rt/rap/doc/2.2/guide/reference/api/org/eclipse/swt/widgets/Label.html#setText(java.lang.String)
	 *
	 * This method sets the widget label. The label may include the mnemonic character and line delimiters.
	 * Mnemonics are indicated by an '&' that causes the next character to be the mnemonic.
	 * When the user presses a key sequence that matches the mnemonic, focus is assigned to the control that
	 * follows the label. On most platforms, the mnemonic appears underlined but may be emphasised in a platform
	 * specific manner. The mnemonic indicator character '&' can be escaped by doubling it in the string, causing
	 * a single '&' to be displayed.
	 *
	 * @param labelText
	 * @return
	 */
	public static String prepareLabelText(String labelText) {
		if (labelText == null) {
			labelText = "";
		}
		else {
			labelText = labelText.replace("&", "&&");
		}
		return labelText;
	}


	public static Label createTopLabel(Composite composite, String labelText, String tooltipText) {
		Label label = new Label(composite, SWT.RIGHT);

		top(label);

		label.setText( prepareLabelText(labelText) );
		label.setToolTipText( prepareLabelText(tooltipText) );

		return label;
	}


	public static Label createTopLabel(Composite composite, String labelText) {
		return createTopLabel(composite, labelText, "");
	}


	public static Label top(Label label) {
		GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		label.setLayoutData(gridData);
		return label;
	}


	public static Text createLabelAndText(Composite composite, String labelText) {
		createLabel(composite, labelText, false);
		return createText(composite, false);
	}


	public static Text createLabelAndText(Composite composite, String labelText, boolean required) {
		createLabel(composite, labelText, required);
		return createText(composite, required);
	}


	public static Text createText(Composite composite, boolean required) {
		Text text = new Text(composite, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		text.setLayoutData(layoutData);

		if (required) {
			makeBold(text);
		}
		return text;
	}


	public static Text createLabelAndReadOnlyText(Composite composite, I18NString labelI18NString, String text) {
		createLabel(composite, labelI18NString.getString());

		Text textWidget = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		textWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		textWidget.setText(text);
		return textWidget;
	}


	public static MultiLineText createLabelAndMultiText(Composite composite, String labelText) {
		return createLabelAndMultiText(composite, labelText, false);
	}


	public static MultiLineText createLabelAndMultiText(Composite composite, String labelText, boolean dynamic) {
		createTopLabel(composite, labelText);

		MultiLineText text = new MultiLineText(composite, SWT.BORDER, dynamic);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return text;
	}


	public static DateComposite createLabelAndDateComposite(Composite composite, String labelText) {
		createLabel(composite, labelText);

		DateComposite dateComposite = new DateComposite(composite, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		dateComposite.setLayoutData(layoutData);
		return dateComposite;
	}


	public static DateTimeComposite createLabelAndDateTimeComposite(Composite composite, String labelText) {
		createLabel(composite, labelText);

		DateTimeComposite dateComposite = new DateTimeComposite(composite, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		dateComposite.setLayoutData(layoutData);

		return dateComposite;
	}


	public static I18NText createLabelAndI18NMultiText(Composite composite, String labelText, ILanguageProvider languageProvider) {
		createTopLabel(composite, labelText);

		I18NText i18NText = new I18NText(composite, SWT.MULTI, languageProvider);
		i18NText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return i18NText;
	}


	public static void enableTextWidget(final Text text, boolean enable) {
		if (enable) {
			enableTextWidget(text);
		}
		else {
			disableTextWidget(text);
		}
	}


	public static void enableTextWidget(final Text text) {
		if (text != null) {
    		text.setEditable(true);

    		if (SystemHelper.isMacOSX()) {
    			// set default background color
    			text.setBackground(null);

    			// enable all key events
    			text.removeKeyListener(DISABLED_TEXT_KEY_LISTENER);
    		}
		}
	}


	public static void disableTextWidget(final Text text) {
		if (text != null) {
    		text.setEditable(false);

    		if (SystemHelper.isMacOSX()) {
    			// set background color of parent component

				// get background color of parent
    			Color background = text.getParent().getBackground();
    			if (background.getRGB().equals( new RGB(255, 255, 255) )) {
    				/* Unfortunately the parent widget returned white as background color which is usually wrong.
    				 * Therefore we set the system color COLOR_TITLE_INACTIVE_BACKGROUND instead which is a light gray.
    				 * System colors must not be disposed which is exactly what we want here.
    				 */
    				background = ColorHelper.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
    			}
    			text.setBackground(background);

    			// disable key events but allow select and copy
    			// make sure that only one listener is added, even if this method is called multiple times
    			if (text.getListeners(SWT.KeyUp).length < 0) {
    				text.addKeyListener(DISABLED_TEXT_KEY_LISTENER);
    			}
    		}
		}
	}


	public static KeyListener DISABLED_TEXT_KEY_LISTENER = new KeyListener() {

		@Override
		public void keyReleased(KeyEvent e) {
		}


		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.keyCode;
			int stateMask = e.stateMask;
			if (!(keyCode == SWT.ARROW_LEFT || keyCode == SWT.ARROW_RIGHT || (stateMask == SWT.COMMAND && keyCode == 'c'))) {
				e.doit = false;
			}
		}
	};


	public static void enableDeep(Composite composite, boolean enable) {
		for ( Control control : composite.getChildren()) {
			control.setEnabled(enable);
		}
		composite.setEnabled(enable);
	}


	/**
	 * Checks whether the current thread is the display-thread.
	 * If yes, the given runnable is executed immediately by straight method call,
	 * otherwise the runnable is synchronous executed in the display-thread.
	 */
	public static void syncExecDisplayThread(Runnable runnable) {
		if (Display.getDefault().getThread() == Thread.currentThread()) {
			runnable.run();
		}
		else {
			Display.getDefault().syncExec(runnable);
		}
	}

	/*
	 * Code snippet for syncExecDisplayThread(Runnable)

	SWTHelper.syncExecDisplayThread(new Runnable() {
		public void run() {
			try {
				// INSERT CODE HERE
			}
			catch (Exception e) {
				ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	});

	*/


	/**
	 * Checks whether the current thread is the display-thread.
	 * If yes, the given runnable is executed immediately by straight method call,
	 * otherwise the runnable is asynchronous executed in the display-thread.
	 * In the latter case the runnable won't be executed, if the workbench is closing.
	 */
	public static void asyncExecDisplayThread(Runnable runnable) {
		if (Display.getDefault().getThread() == Thread.currentThread()) {
			runnable.run();
		}
		else if (!PlatformUI.getWorkbench().isClosing()) {
			Display.getDefault().asyncExec(runnable);
		}
	}


	public static void showInfoDialog(final String title, final String text) {
		asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), title, text);
			}
		});
	}


	public static void makeBold(Control control) {
		setBold(control, true);
	}


	public static void setBold(Control control, boolean bold) {
		if (control != null) {
			FontRegistry fontRegistry = JFaceResources.getFontRegistry();
			FontData[] fontData = control.getFont().getFontData();
			String symbolicName = fontData[0].getName();
			if (! fontRegistry.hasValueFor(symbolicName)) {
				fontRegistry.put(symbolicName, fontData);
			}

			Font font = null;
			if (bold) {
				font = fontRegistry.getBold(symbolicName);
			}
			else {
				font = fontRegistry.get(symbolicName);
			}

			control.setFont(font);

			// bold and non-bold fonts need different amount of space
			control.getParent().layout();
		}
	}




	public static MenuItem buildMenuItem(Menu menu, String text, Listener listener, Image image) {
		MenuItem menuItem = new MenuItem (menu, SWT.PUSH);
		menuItem.setText(text);
		menuItem.setImage(image);
		menuItem.addListener(SWT.Selection, listener);
		return menuItem;
	}


	public static MenuItem buildMenuItem(Menu menu, String text, Listener listener) {
		return buildMenuItem(menu, text, listener, null /*image*/);
	}


	/**
	 * Create a {@link Label} with the purpose to add some vertical space in the context of a {@link GridLayout}.
	 * @param parent {@link Composite} with {@link GridLayout}
	 * @param verticalSize amount of vertical space to be added (height of the label)
	 * @param horizontalSpan (number of columns of the parent's GridLayout)
	 * @return
	 */
	public static Label verticalSpace(Composite parent, int verticalSize) {
		Label label = new Label(parent, SWT.NONE);

		GridLayout gridLayout = (GridLayout) parent.getLayout();

		GridDataFactory
    		.swtDefaults()
    		.span(gridLayout.numColumns, 1)
    		.hint(SWT.DEFAULT, verticalSize)
    		.applyTo(label);

		return label;
	}


	public static Label verticalSpace(Composite parent) {
		int verticalSize = 5;
		return verticalSpace(parent, verticalSize);
	}


	/**
	 * Create a {@link Label} with the purpose to add some vertical space in the context of a {@link GridLayout}.
	 * @param parent {@link Composite} with {@link GridLayout}
	 * @param verticalSize amount of vertical space to be added (height of the label)
	 * @param horizontalSpan (number of columns of the parent's GridLayout)
	 * @return
	 */
	public static Label horizontalLine(Composite parent) {
		Label label = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);

		GridLayout gridLayout = (GridLayout) parent.getLayout();

		GridDataFactory
    		.swtDefaults()
    		.align(SWT.FILL, SWT.CENTER)
    		.span(gridLayout.numColumns, 1)
    		.applyTo(label);

		return label;
	}

}
