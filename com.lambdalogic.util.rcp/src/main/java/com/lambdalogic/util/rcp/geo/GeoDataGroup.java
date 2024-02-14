package com.lambdalogic.util.rcp.geo;

import static com.lambdalogic.util.StringHelper.*;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.geo.GeoData;
import de.regasus.common.geo.GeoDataFactory;
import de.regasus.common.geo.GeoDataPatternDD;
import de.regasus.common.geo.GeoDataPatternDDM;
import de.regasus.common.geo.GeoDataPatternInternal;
import de.regasus.common.geo.IGeoDataPattern;


public class GeoDataGroup extends Group {

	private IGeoDataPattern geoDataPattern;

	private ModifySupport modifySupport = new ModifySupport(this);

	private Text geoDataText;
	private Button pasteButton;
	private Button intButton;
	private Button ddButton;
	private Button ddmButton;
	private Text descriptionText;


	// colors
	private final Color BLACK = getDisplay().getSystemColor(SWT.COLOR_BLACK);
	private final Color RED = getDisplay().getSystemColor(SWT.COLOR_RED);


	public GeoDataGroup(Composite parent, int style) {
		super(parent, SWT.NONE);

		final int COLUMN_COUNT = 4;

		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = COLUMN_COUNT;
		gridLayout.makeColumnsEqualWidth = false;
		setLayout(gridLayout);


		/*
		 * Row 1
		 */

		// Geo Data Text
		Label geoDataLabel = new Label(this, SWT.NONE);
		geoDataLabel.setText(UtilI18N.GeoData_Coordinates);
		geoDataLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		geoDataText = new Text(this, SWT.BORDER);
		geoDataText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		geoDataText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String input = geoDataText.getText();
				geoDataPattern.setInput(input);
				adaptColor();
			}
		});

		geoDataText.addModifyListener(modifySupport);


		// format buttons: INT, DD and DDM
		Composite buttonComposite = new Composite(this, SWT.NONE);

		RowLayout buttonLayout = new RowLayout();
		buttonLayout.pack = false;
		buttonComposite.setLayout(buttonLayout);

		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

		// create Button for INT format
		intButton = new Button(buttonComposite, SWT.RADIO);
		intButton.setText(UtilI18N.GeoData_INT);
		intButton.setToolTipText(UtilI18N.GeoData_INT_desc);
		intButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (intButton.getSelection()) {
					setGeoDataPattern(new GeoDataPatternInternal());
				}
			}
		});

		// create Button for DD format
		ddButton = new Button(buttonComposite, SWT.RADIO);
		ddButton.setText(UtilI18N.GeoData_DD);
		ddButton.setToolTipText(UtilI18N.GeoData_DD_desc);
		ddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ddButton.getSelection()) {
					setGeoDataPattern(new GeoDataPatternDD());
				}
			}
		});

		// create Button for DDM format
		ddmButton = new Button(buttonComposite, SWT.RADIO);
		ddmButton.setText(UtilI18N.GeoData_DDM);
		ddmButton.setToolTipText(UtilI18N.GeoData_DDM_desc);
		ddmButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ddmButton.getSelection()) {
					setGeoDataPattern(new GeoDataPatternDDM());
				}
			}
		});


		// init
		intButton.setSelection(true);
		geoDataPattern = new GeoDataPatternInternal();


		// Paste Button
		pasteButton = new Button(this, SWT.PUSH);
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		pasteButton.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_PASTE));
		pasteButton.setToolTipText(UtilI18N.GeoData_Paste_desc);
		pasteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pasteFromClipboad();
			}
		});


		/*
		 * Row 2
		 */

		Label descriptionLabel = new Label(this, SWT.NONE);
		descriptionLabel.setText( GeoData.DESCRIPTION.getLabel() );
		descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		descriptionText = new Text(this, SWT.BORDER);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_COUNT - 1, 1));
		descriptionText.addModifyListener(modifySupport);
	}


	private void setGeoDataPattern(IGeoDataPattern newGeoDataPattern) {
		modifySupport.setEnabled(false);

		try {
			if (geoDataPattern.matches()) {
				BigDecimal latitude = geoDataPattern.getLatitude();
				BigDecimal longitude = geoDataPattern.getLongitude();

				String newText = newGeoDataPattern.format(latitude, longitude);
				newGeoDataPattern.setInput(newText);
				geoDataText.setText(newText);

			}

			geoDataPattern = newGeoDataPattern;

    		adaptColor();
		}
		finally {
			modifySupport.setEnabled(true);
		}
	}


	private void adaptColor() {
		Color color = null;
		if ( geoDataPattern.matches() ) {
			color = BLACK;
		}
		else {
			color = RED;
		}
		geoDataText.setForeground(color);
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}


	@Override
	public void setEnabled(boolean enabled) {
		geoDataText.setEnabled(enabled);
		descriptionText.setEnabled(enabled);
		super.setEnabled(enabled);
	}


	/**
	 * Makes that the text fields visually appear to be not usable
	 */
	public void setEditable(boolean editable) {
		geoDataText.setEditable(editable);
		descriptionText.setEditable(editable);
	}


	public GeoData getGeoData() {
		GeoData geoData = new GeoData(
			geoDataPattern.getLatitude(),
			geoDataPattern.getLongitude(),
			trim(descriptionText.getText())
		);
		return geoData;
	}


	public void setGeoData(GeoData geoData) {
		String geoDataString = "";
		String description = "";

		if (geoData != null) {
			if (intButton.getSelection()) {
				geoDataString = geoData.toInternal();
			}
			else if (ddButton.getSelection()) {
				geoDataString = geoData.toDD();
			}
			else if (ddmButton.getSelection()) {
				geoDataString = geoData.toDDM();
			}

			description = geoData.getDescription();
		}

		geoDataText.setText( avoidNull(geoDataString) );
		descriptionText.setText( avoidNull(description) );
	}


	protected void pasteFromClipboad() {
		String text = ClipboardHelper.readStringFromClipboard();
		GeoData geoData = GeoDataFactory.createGeoData(text);
		if (geoData != null) {
			String description = descriptionText.getText();
			geoData.setDescription(description);
		}
		setGeoData(geoData);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
