package de.regasus.portal.type.standard;

import java.util.Properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.ui.Activator;


public class StandardStyleFontComposite extends Composite {

	private static final String FONT_SIZE_BASE_KEY = "font-size-base";
	private static final Integer FONT_SIZE_BASE_DEFAULT = 16; // px
	private static final Integer FONT_SIZE_BASE_CLASSIC_DEFAULT = 14; // px

	private static final String INPUT_FONT_SIZE_KEY = "input-font-size";
	private static final Integer INPUT_FONT_SIZE_DEFAULT = 18; // px
	private static final Integer INPUT_FONT_SIZE_CLASSIC_DEFAULT = 14; // px

	private static final String FONT_FAMILY_REGULAR_KEY = "font-family-regular";
	private static final String FONT_FAMILY_REGULAR_DEFAULT = "Arial, sans-serif";

	private static final String FONT_FAMILY_BOLD_KEY = "font-family-bold";

	private static final String FONT_FAMILY_ITALIC_KEY = "font-family-italic";


	// entity
	private PageLayout pageLayout;


	// **************************************************************************
	// * Widgets
	// *

	private NullableSpinner fontSizeBaseSpinner;
	private NullableSpinner inputFontSizeSpinner;
	private Text webfontUrlText;
	private Text fontFamilyText;
	private Text fontFamilyBoldText;
	private Text fontFamilyLightText;

	// *
	// * Widgets
	// **************************************************************************

	private ModifySupport modifySupport = new ModifySupport(this);


	public StandardStyleFontComposite(Composite parent) {
		super(parent, SWT.NONE);

		createWidgets(this);
	}


	public void createWidgets(Composite parent) {
		GridLayoutFactory.swtDefaults()
			.numColumns(4)
			.equalWidth(false)
			.spacing(10, 5)
//			.margins(0, 0)
			.applyTo(this);

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory spinnerGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);
		GridDataFactory pushGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);
		GridDataFactory placeHolderGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
		GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(3, 1).grab(true, false);

		// font size base
		{
			Label label = new Label(parent, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_BaseFontSize);

			fontSizeBaseSpinner = new NullableSpinner(parent, SWT.BORDER);
			spinnerGridDataFactory.applyTo(fontSizeBaseSpinner);
			Integer fontSizeBaseDefault = isStandard() ? FONT_SIZE_BASE_DEFAULT : FONT_SIZE_BASE_CLASSIC_DEFAULT;
			fontSizeBaseSpinner.setStartValue( fontSizeBaseDefault.longValue());
			fontSizeBaseSpinner.setMinimum(1);
			fontSizeBaseSpinner.setMaximum(100);
			fontSizeBaseSpinner.setNullable(false);
			WidgetSizer.setWidth(fontSizeBaseSpinner);

			fontSizeBaseSpinner.addModifyListener(modifySupport);

			Button resetButton = new Button(parent, SWT.PUSH);
			pushGridDataFactory.applyTo(resetButton);
			resetButton.setText(fontSizeBaseDefault + "px");
			resetButton.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fontSizeBaseSpinner.setValue(fontSizeBaseDefault);
				}
			} );

			Label placeHolder = new Label(parent, SWT.NONE);
			placeHolderGridDataFactory.applyTo(placeHolder);
		}


		// input font size
		{
			Label label = new Label(parent, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_InputFontSize);

			inputFontSizeSpinner = new NullableSpinner(parent, SWT.BORDER);
			spinnerGridDataFactory.applyTo(inputFontSizeSpinner);
			Integer inputFontSizeDefault = isStandard() ? INPUT_FONT_SIZE_DEFAULT : INPUT_FONT_SIZE_CLASSIC_DEFAULT;
			inputFontSizeSpinner.setStartValue( inputFontSizeDefault.longValue() );
			inputFontSizeSpinner.setMinimum(1);
			inputFontSizeSpinner.setMaximum(100);
			inputFontSizeSpinner.setNullable(false);
			WidgetSizer.setWidth(inputFontSizeSpinner);

			inputFontSizeSpinner.addModifyListener(modifySupport);

			Button resetButton = new Button(parent, SWT.PUSH);
			pushGridDataFactory.applyTo(resetButton);
			resetButton.setText(inputFontSizeDefault + "px");
			resetButton.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					inputFontSizeSpinner.setValue(inputFontSizeDefault);
				}
			} );

			Label placeHolder = new Label(parent, SWT.NONE);
			placeHolderGridDataFactory.applyTo(placeHolder);
		}

		// web font url
		{
			Label label = new Label(parent, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_WebfontUrl);

			webfontUrlText = new Text(parent, SWT.BORDER);
			textGridDataFactory.applyTo(webfontUrlText);
			webfontUrlText.addModifyListener(modifySupport);
		}

		// font family
		{
			Label label = new Label(parent, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_FontFamilyRegular);

			fontFamilyText = new Text(parent, SWT.BORDER);
			textGridDataFactory.applyTo(fontFamilyText);
			fontFamilyText.addModifyListener(modifySupport);
		}

		// font family bold
		{
			Label label = new Label(parent, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_FontFamilyBold);

			fontFamilyBoldText = new Text(parent, SWT.BORDER);
			textGridDataFactory.applyTo(fontFamilyBoldText);
			fontFamilyBoldText.addModifyListener(modifySupport);
		}

		// font family light
		{
			Label label = new Label(parent, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_FontFamilyItalic);

			fontFamilyLightText = new Text(parent, SWT.BORDER);
			textGridDataFactory.applyTo(fontFamilyLightText);
			fontFamilyLightText.addModifyListener(modifySupport);
		}
	}


	/**
	 * Set a new entity.
	 * @param foo
	 */
	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
		syncWidgetsToEntity();
	}


	private boolean isStandard() {
		boolean result = true;
		try {
			if (pageLayout != null) {
				result = "standard".equals(FileHelper.getNameWithoutExtension(pageLayout.getStyleTemplateFileName()));
			}
		}
		catch (ClassNotFoundException e) {
			// ignore
		}
		return result;
	}


	/**
	 * Copy the values from the entity to the widgets.
	 */
	private void syncWidgetsToEntity() {
		if (pageLayout != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Properties styleParameters = pageLayout.getStyleParameters();

						{
    						Integer fontSize = TypeHelper.toInteger( styleParameters.getProperty(FONT_SIZE_BASE_KEY) );
    						if (fontSize == null) {
    							fontSize = isStandard() ? FONT_SIZE_BASE_DEFAULT : FONT_SIZE_BASE_CLASSIC_DEFAULT;
    						}
    						fontSizeBaseSpinner.setValue(fontSize);
						}

						{
    						Integer fontSize = TypeHelper.toInteger( styleParameters.getProperty(INPUT_FONT_SIZE_KEY) );
    						if (fontSize == null) {
    							fontSize = isStandard() ? INPUT_FONT_SIZE_DEFAULT : INPUT_FONT_SIZE_CLASSIC_DEFAULT;
    						}
    						inputFontSizeSpinner.setValue(fontSize);
						}

						webfontUrlText.setText(StringHelper.avoidNull(pageLayout.getWebfontUrl()));

						{
							String fontFamily = styleParameters.getProperty(FONT_FAMILY_REGULAR_KEY);
							if (StringHelper.isEmpty(fontFamily)) {
								fontFamily = FONT_FAMILY_REGULAR_DEFAULT;
							}
							fontFamilyText.setText(fontFamily);
						}

						{
							String fontFamily = styleParameters.getProperty(FONT_FAMILY_BOLD_KEY);
							if (StringHelper.isEmpty(fontFamily)) {
								fontFamily = FONT_FAMILY_REGULAR_DEFAULT;
							}
							fontFamilyBoldText.setText(fontFamily);
						}

						{
							String fontFamily = styleParameters.getProperty(FONT_FAMILY_ITALIC_KEY);
							if (StringHelper.isEmpty(fontFamily)) {
								fontFamily = FONT_FAMILY_REGULAR_DEFAULT;
							}
							fontFamilyLightText.setText(fontFamily);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	/**
	 * Copy the values from the widgets to the entity.
	 */
	public void syncEntityToWidgets() {
		if (pageLayout != null) {
			Properties styleParameters = pageLayout.getStyleParameters();

			{
	    		Integer fontSize = fontSizeBaseSpinner.getValueAsInteger();
	    		if (fontSize == null) {
	    			fontSize = isStandard() ? FONT_SIZE_BASE_DEFAULT : FONT_SIZE_BASE_CLASSIC_DEFAULT;
	    		}
	    		styleParameters.setProperty(FONT_SIZE_BASE_KEY, TypeHelper.toString(fontSize) );
			}

			{
	    		Integer fontSize = inputFontSizeSpinner.getValueAsInteger();
	    		if (fontSize == null) {
	    			fontSize = isStandard() ? INPUT_FONT_SIZE_DEFAULT : INPUT_FONT_SIZE_CLASSIC_DEFAULT;
	    		}
	    		styleParameters.setProperty(INPUT_FONT_SIZE_KEY, TypeHelper.toString(fontSize) );
			}

			pageLayout.setWebfontUrl(webfontUrlText.getText());

			{
				String fontFamily = fontFamilyText.getText();
				if (StringHelper.isEmpty(fontFamily)) {
					fontFamily = FONT_FAMILY_REGULAR_DEFAULT;
				}
				styleParameters.setProperty(FONT_FAMILY_REGULAR_KEY, fontFamily);
			}

			{
				String fontFamily = fontFamilyBoldText.getText();
				if (StringHelper.isEmpty(fontFamily)) {
					fontFamily = styleParameters.getProperty(FONT_FAMILY_REGULAR_KEY);
				}
				styleParameters.setProperty(FONT_FAMILY_BOLD_KEY, fontFamily);
			}

			{
				String fontFamily = fontFamilyLightText.getText();
				if (StringHelper.isEmpty(fontFamily)) {
					fontFamily = styleParameters.getProperty(FONT_FAMILY_REGULAR_KEY);
				}
				styleParameters.setProperty(FONT_FAMILY_ITALIC_KEY, fontFamily);
			}
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
