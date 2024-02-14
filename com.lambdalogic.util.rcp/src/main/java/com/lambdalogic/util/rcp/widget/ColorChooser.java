package com.lambdalogic.util.rcp.widget;

import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.io.ByteArrayInputStream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.image.ImageHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.ModifySupport;


public class ColorChooser extends Composite {

	private RGB rgb;
	private Color color;

	private Color defaultColor;
	private Image defaultImage;

	// Widgets
	private Label colorLabel;
	private Button chooseButton;
	private Button resetButton;

	private ModifySupport modifySupport = new ModifySupport(this);


	public ColorChooser(Composite parent) {
		super(parent, SWT.NONE);

		createWidgets(this);

		addDisposeListener(disposeListener);
	}


	private void createWidgets(Composite parent) {
		GridLayout mainLayout = new GridLayout(3, false);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		setLayout(mainLayout);

		colorLabel = new Label(parent, SWT.BORDER);
		colorLabel.setText("     ");

		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(2, true);
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonLayout);

		GridDataFactory buttonGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);

		chooseButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(chooseButton);
		chooseButton.setText(UtilI18N.Select);
		chooseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chooseColor();
			}
		});

		resetButton = new Button(buttonComposite, SWT.PUSH);
		buttonGridDataFactory.applyTo(resetButton);
		resetButton.setText(UtilI18N.Reset);
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetColor();
			}
		});
	}


	private DisposeListener disposeListener = new  DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			disposeColor();
			disposeDefaultColor();
		}
	};


	// **************************************************************************
	// * get / set color
	// *

	public RGB getColorAsRGB() {
		return rgb;
	}


	public void setColorAsRGB(RGB rgb) {
		disposeColor();

		this.rgb = rgb;

		if (rgb == null) {
			colorLabel.setBackground(getBackground());
			colorLabel.setToolTipText(null);
		}
		else {
			color = new Color(Display.getCurrent(), rgb);

			colorLabel.setBackground(color);
			colorLabel.setToolTipText( getColorAsString() );
		}
	}


	public String getColorAsString() {
		return toString(rgb);
	}


	public void setColorAsString(String rgbStr) {
		setColorAsRGB( toRGB(rgbStr) );
	}


	public Integer getColorAsInteger() {
		return toInteger(rgb);
	}


	public void setColorAsInteger(Integer rgbInt) {
		setColorAsRGB( toRGB(rgbInt) );
	}

	// *
	// * get / set color
	// **************************************************************************


	protected void chooseColor() {
		ColorDialog colorDialog = new ColorDialog(getShell());

		// Doesn't actually prefill color
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=21812
		if (rgb != null) {
			colorDialog.setRGB(rgb);
		}

		RGB newRgb = colorDialog.open();
		if (newRgb != null) {
			setColorAsRGB(newRgb);
			modifySupport.fire();
		}
	}


	public void setDefaultColor(RGB rgb) {
		disposeDefaultColor();

		defaultColor = new Color(Display.getCurrent(), rgb);

		int width = 10;
		int height = 10;
		byte[] defaultColorBytes = ImageHelper.buildRectangleJPG(width, height, rgb.red, rgb.green, rgb.blue);
		defaultImage = new Image(
			Display.getDefault(),
			new ByteArrayInputStream(defaultColorBytes)
		);


		Font font = resetButton.getFont();
		resetButton.setImage(defaultImage);
		resetButton.setFont(font);
	}


	protected void resetColor() {
		RGB defaultRGB = null;
		if (defaultColor != null) {
			defaultRGB = defaultColor.getRGB();
		}

		setColorAsRGB(defaultRGB);

		modifySupport.fire();
	}


	protected void disposeColor() {
		if (color != null && ! color.isDisposed()) {
			color.dispose();
			color = null;
		}
	}


	protected void disposeDefaultColor() {
		if (defaultColor != null && ! defaultColor.isDisposed()) {
			defaultColor.dispose();
			defaultColor = null;
		}

		if (defaultImage != null && ! defaultImage.isDisposed()) {
			defaultImage.dispose();
			defaultImage = null;
		}
	}


	// **************************************************************************
	// * convert color
	// *

	private static String toString(RGB rgb) {
		StringBuilder sb = new StringBuilder(7);

		if (rgb != null) {
    		String redString = Integer.toHexString(rgb.red).toUpperCase();
    		String greenString = Integer.toHexString(rgb.green).toUpperCase();
    		String blueString = Integer.toHexString(rgb.blue).toUpperCase();

    		sb.append("#");

    		if (redString.length() == 1) {
    			sb.append("0");
    		}
    		sb.append(redString);

    		if (greenString.length() == 1) {
    			sb.append("0");
    		}
    		sb.append(greenString);

    		if (blueString.length() == 1) {
    			sb.append("0");
    		}
    		sb.append(blueString);
		}

		return sb.toString();
	}


	private static Integer toInteger(RGB rgb) {
		Integer rgbInteger = null;

		if (rgb != null) {
			rgbInteger = (rgb.red << 16) + (rgb.green << 8) + rgb.blue;
		}

		return rgbInteger;
	}


	private static RGB toRGB(Integer rgbInt) {
		RGB rgb = null;
		if (rgbInt != null) {
    		int red = (rgbInt >> 16) & 0xFF;
    		int green = (rgbInt >> 8) & 0xFF;
    		int blue = rgbInt & 0xFF;

    		rgb = new RGB(red, green, blue);
		}
		return rgb;
	}


	private static RGB toRGB(String rgbStr) {
		RGB rgb = null;
		if ( isNotEmpty(rgbStr) ) {
			if (rgbStr.length() != 7) {
				throw new IllegalArgumentException(rgbStr + " is not a valid HTML/CSS color, because it does not contain 7 characters");
			}
			if (rgbStr.charAt(0) != '#') {
				throw new IllegalArgumentException(rgbStr + " is not a valid HTML/CSS color, because it does not start with '#'");
			}

			String redStr = rgbStr.substring(1, 3);
			String greenStr = rgbStr.substring(3, 5);
			String blueStr = rgbStr.substring(5, 7);

    		int red;
			try {
				red = Integer.parseInt(redStr, 16);
			}
			catch (Exception e) {
				throw new IllegalArgumentException(rgbStr + " is not a valid HTML/CSS color, because the value for red (" + redStr + ") is not a valid hexadecimal number");
			}

    		int green;
			try {
				green = Integer.parseInt(greenStr, 16);
			}
			catch (Exception e) {
				throw new IllegalArgumentException(rgbStr + " is not a valid HTML/CSS color, because the value for green (" + greenStr + ") is not a valid hexadecimal number");
			}

    		int blue;
			try {
				blue = Integer.parseInt(blueStr, 16);
			}
			catch (Exception e) {
				throw new IllegalArgumentException(rgbStr + " is not a valid HTML/CSS color, because the value for blue (" + blueStr + ") is not a valid hexadecimal number");
			}

    		rgb = new RGB(red, green, blue);
		}
		return rgb;
	}

	// *
	// * convert color
	// **************************************************************************

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
