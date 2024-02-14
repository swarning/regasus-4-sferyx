package de.regasus.portal.type.openid.connect;

import java.util.Properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.ColorChooser;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.ui.Activator;


public class OpenIDStyleColorComposite extends Composite {

	private static final String PRIMARY_COLOR_KEY = "color-primary";
	private static final RGB PRIMARY_COLOR_PROFILE_DEFAULT = new RGB(0x53, 0xbb, 0xe6);

	private static final String PRIMARY_DARK_COLOR_KEY = "color-primary-dark";
	private static final RGB PRIMARY_DARK_COLOR_PROFILE_DEFAULT = new RGB(0x1c, 0x88, 0xb9);

	private static final String PRIMARY_LIGHT_COLOR_KEY = "color-primary-light";
	private static final RGB PRIMARY_LIGHT_COLOR_PROFILE_DEFAULT = new RGB(0xc3, 0xe4, 0xe4);

	private static final String BACKGROUND_COLOR_KEY = "color-background";
	private static final RGB BACKGROUND_COLOR_PROFILE_DEFAULT = new RGB(0xff, 0xff, 0xff);
	
	private static final String PAGE_BACKGROUND_COLOR_KEY = "color-page";
	private static final RGB PAGE_BACKGROUND_COLOR_PROFILE_DEFAULT = new RGB(0xff, 0xff, 0xff);

	private static final String ACCENT_COLOR_KEY = "color-accent";
	private static final RGB ACCENT_COLOR_PROFILE_DEFAULT = new RGB(0x53, 0xbb, 0xe6);

	private static final String TEXT_1_COLOR_KEY = "color-text-primary";
	private static final RGB TEXT_1_COLOR_PROFILE_DEFAULT = new RGB(0x21, 0x21, 0x21);

	private static final String TEXT_2_COLOR_KEY = "color-text-secondary";
	private static final RGB TEXT_2_COLOR_PROFILE_DEFAULT = new RGB(0x75, 0x75, 0x75);
	
	private static final String INPUT_COLOR_KEY = "color-input";
	private static final RGB INPUT_COLOR_PROFILE_DEFAULT = new RGB(0x21, 0x21, 0x21);

	private static final String DIVIDER_COLOR_KEY = "color-divider";
	private static final RGB DIVIDER_COLOR_PROFILE_DEFAULT = new RGB(0xaa, 0xaa, 0xaa);
	
	private static final String ASIDE_COLOR_KEY = "color-aside";
	private static final RGB ASIDE_COLOR_PROFILE_DEFAULT = new RGB(0xff, 0xff, 0xff);
	
	private static final String LINK_COLOR_KEY = "color-link";
	private static final RGB LINK_COLOR_PROFILE_DEFAULT = new RGB(0x1c, 0x88, 0xb9);
	
	private static final String HEADER_COLOR_KEY = "color-header";
	private static final RGB HEADER_COLOR_PROFILE_DEFAULT = new RGB(0x53, 0xbb, 0xe6);
	
	private static final String HEADER_TEXT_COLOR_KEY = "color-header-text";
	private static final RGB HEADER_TEXT_COLOR_PROFILE_DEFAULT = new RGB(0xff, 0xff, 0xff);
	
	private static final String FOOTER_COLOR_KEY = "color-footer";
	private static final RGB FOOTER_COLOR_PROFILE_DEFAULT = new RGB(0x53, 0xbb, 0xe6);
	
	private static final String FOOTER_TEXT_COLOR_KEY = "color-footer-text";
	private static final RGB FOOTER_TEXT_COLOR_PROFILE_DEFAULT = new RGB(0xff, 0xff, 0xff);


	// entity
	private PageLayout pageLayout;


	// **************************************************************************
	// * Widgets
	// *

	private ColorChooser primaryColorChooser;
	private ColorChooser primaryDarkColorChooser;
	private ColorChooser primaryLightColorChooser;
	private ColorChooser backgroundColorChooser;
	private ColorChooser pageBackgroundColorChooser;
	private ColorChooser accentColorChooser;
	private ColorChooser text1ColorChooser;
	private ColorChooser text2ColorChooser;
	private ColorChooser inputColorChooser;
	private ColorChooser dividerColorChooser;
	private ColorChooser asideColorChooser;
	private ColorChooser linkColorChooser;
	private ColorChooser headerColorChooser;
	private ColorChooser headerTextColorChooser;
	private ColorChooser footerColorChooser;
	private ColorChooser footerTextColorChooser;

	// *
	// * Widgets
	// **************************************************************************

	private ModifySupport modifySupport = new ModifySupport(this);


	public OpenIDStyleColorComposite(Composite parent) {
		super(parent, SWT.NONE);

		createWidgets(this);
	}


	public void createWidgets(Composite parent) {
		GridLayoutFactory.swtDefaults()
			.numColumns(2)
			.equalWidth(false)
			.spacing(10, SWT.DEFAULT)
			.margins(0, 0)
			.applyTo(this);


		GridLayoutFactory compositeGridLayoutFactory = GridLayoutFactory.swtDefaults().numColumns(2);
		GridDataFactory compositeGridDataFactory = GridDataFactory.fillDefaults().grab(true, true);

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory colorChooserGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
		
		// leftComposite
		{
			Composite leftComposite = new Composite(parent, SWT.NONE);
			compositeGridDataFactory.applyTo(leftComposite);
			compositeGridLayoutFactory.applyTo(leftComposite);

    		// primary color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_PrimaryColor);

    			primaryColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(primaryColorChooser);
    			primaryColorChooser.addModifyListener(modifySupport);
    		}

    		// primary dark color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_PrimaryDarkColor);

    			primaryDarkColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(primaryDarkColorChooser);
    			primaryDarkColorChooser.addModifyListener(modifySupport);
    		}

    		// primary light color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_PrimaryLightColor);

    			primaryLightColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(primaryLightColorChooser);
    			primaryLightColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// link color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_LinkColor);

    			linkColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(linkColorChooser);
    			linkColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// header color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_HeaderColor);

    			headerColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(headerColorChooser);
    			headerColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// header text color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_HeaderTextColor);

    			headerTextColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(headerTextColorChooser);
    			headerTextColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// footer color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_FooterColor);

    			footerColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(footerColorChooser);
    			footerColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// footer text color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_FooterTextColor);

    			footerTextColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(footerTextColorChooser);
    			footerTextColorChooser.addModifyListener(modifySupport);
    		}

    		// background color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_BackgroundColor);

    			backgroundColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(backgroundColorChooser);
    			backgroundColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// page background color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_PageBackgroundColor);

    			pageBackgroundColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(pageBackgroundColorChooser);
    			pageBackgroundColorChooser.addModifyListener(modifySupport);
    		}

    		// accent color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_AccentColor);

    			accentColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(accentColorChooser);
    			accentColorChooser.addModifyListener(modifySupport);
    		}

    		// text 1 color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_PrimaryTextColor);

    			text1ColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(text1ColorChooser);
    			text1ColorChooser.addModifyListener(modifySupport);
    		}

    		// text 2 color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_SecondaryTextColor);

    			text2ColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(text2ColorChooser);
    			text2ColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// input color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_InputColor);

    			inputColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(inputColorChooser);
    			inputColorChooser.addModifyListener(modifySupport);
    		}

    		// divider color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_DividerColor);

    			dividerColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(dividerColorChooser);
    			dividerColorChooser.addModifyListener(modifySupport);
    		}
    		
    		// aside color
    		{
    			Label label = new Label(leftComposite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_AsideColor);

    			asideColorChooser = new ColorChooser(leftComposite);
    			colorChooserGridDataFactory.applyTo(asideColorChooser);
    			asideColorChooser.addModifyListener(modifySupport);
    		}
    		
    		setStandardColor();

		} // leftComposite


		// rightComposite
		{
			Composite rightComposite = new Composite(parent, SWT.NONE);
			compositeGridDataFactory.applyTo(rightComposite);
			compositeGridLayoutFactory.applyTo(rightComposite);

		} // rightComposite
	}


	/**
	 * Set a new entity.
	 * @param foo
	 */
	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
		
		syncWidgetsToEntity();
	}
	
	
	private void setStandardColor() {
		primaryColorChooser.setDefaultColor(PRIMARY_COLOR_PROFILE_DEFAULT);
		
		primaryDarkColorChooser.setDefaultColor(PRIMARY_DARK_COLOR_PROFILE_DEFAULT);
		
		primaryLightColorChooser.setDefaultColor(PRIMARY_LIGHT_COLOR_PROFILE_DEFAULT);
		
		linkColorChooser.setDefaultColor(LINK_COLOR_PROFILE_DEFAULT);
		
		headerColorChooser.setDefaultColor(HEADER_COLOR_PROFILE_DEFAULT);
		
		headerTextColorChooser.setDefaultColor(HEADER_TEXT_COLOR_PROFILE_DEFAULT);
		
		footerColorChooser.setDefaultColor(FOOTER_COLOR_PROFILE_DEFAULT);
		
		footerTextColorChooser.setDefaultColor(FOOTER_TEXT_COLOR_PROFILE_DEFAULT);
		
		backgroundColorChooser.setDefaultColor(BACKGROUND_COLOR_PROFILE_DEFAULT);
		
		pageBackgroundColorChooser.setDefaultColor(PAGE_BACKGROUND_COLOR_PROFILE_DEFAULT);
		
		accentColorChooser.setDefaultColor(ACCENT_COLOR_PROFILE_DEFAULT);
		
		text1ColorChooser.setDefaultColor(TEXT_1_COLOR_PROFILE_DEFAULT);
		
		text2ColorChooser.setDefaultColor(TEXT_2_COLOR_PROFILE_DEFAULT);
		
		inputColorChooser.setDefaultColor(INPUT_COLOR_PROFILE_DEFAULT);
		
		dividerColorChooser.setDefaultColor(DIVIDER_COLOR_PROFILE_DEFAULT);
		
		asideColorChooser.setDefaultColor(ASIDE_COLOR_PROFILE_DEFAULT);
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

						primaryColorChooser.setColorAsString( styleParameters.getProperty(PRIMARY_COLOR_KEY) );
						primaryDarkColorChooser.setColorAsString( styleParameters.getProperty(PRIMARY_DARK_COLOR_KEY) );
						primaryLightColorChooser.setColorAsString( styleParameters.getProperty(PRIMARY_LIGHT_COLOR_KEY) );
						linkColorChooser.setColorAsString( styleParameters.getProperty(LINK_COLOR_KEY) );
						headerColorChooser.setColorAsString( styleParameters.getProperty(HEADER_COLOR_KEY) );
						headerTextColorChooser.setColorAsString( styleParameters.getProperty(HEADER_TEXT_COLOR_KEY) );
						footerColorChooser.setColorAsString( styleParameters.getProperty(FOOTER_COLOR_KEY) );
						footerTextColorChooser.setColorAsString( styleParameters.getProperty(FOOTER_TEXT_COLOR_KEY) );
						backgroundColorChooser.setColorAsString( styleParameters.getProperty(BACKGROUND_COLOR_KEY) );
						pageBackgroundColorChooser.setColorAsString( styleParameters.getProperty(PAGE_BACKGROUND_COLOR_KEY) );
						accentColorChooser.setColorAsString( styleParameters.getProperty(ACCENT_COLOR_KEY) );
						text1ColorChooser.setColorAsString( styleParameters.getProperty(TEXT_1_COLOR_KEY) );
						text2ColorChooser.setColorAsString( styleParameters.getProperty(TEXT_2_COLOR_KEY) );
						inputColorChooser.setColorAsString( styleParameters.getProperty(INPUT_COLOR_KEY));
						dividerColorChooser.setColorAsString( styleParameters.getProperty(DIVIDER_COLOR_KEY) );
						asideColorChooser.setColorAsString( styleParameters.getProperty(ASIDE_COLOR_KEY) );
						
						setStandardColor();
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

			styleParameters.setProperty(PRIMARY_COLOR_KEY, primaryColorChooser.getColorAsString() );
			styleParameters.setProperty(PRIMARY_DARK_COLOR_KEY, primaryDarkColorChooser.getColorAsString() );
			styleParameters.setProperty(PRIMARY_LIGHT_COLOR_KEY, primaryLightColorChooser.getColorAsString() );
			styleParameters.setProperty(LINK_COLOR_KEY, linkColorChooser.getColorAsString() );
			styleParameters.setProperty(HEADER_COLOR_KEY, headerColorChooser.getColorAsString() );
			styleParameters.setProperty(HEADER_TEXT_COLOR_KEY, headerTextColorChooser.getColorAsString() );
			styleParameters.setProperty(FOOTER_COLOR_KEY, footerColorChooser.getColorAsString() );
			styleParameters.setProperty(FOOTER_TEXT_COLOR_KEY, footerTextColorChooser.getColorAsString() );
			styleParameters.setProperty(BACKGROUND_COLOR_KEY, backgroundColorChooser.getColorAsString() );
			styleParameters.setProperty(PAGE_BACKGROUND_COLOR_KEY, pageBackgroundColorChooser.getColorAsString() );
			styleParameters.setProperty(ACCENT_COLOR_KEY, accentColorChooser.getColorAsString() );
			styleParameters.setProperty(TEXT_1_COLOR_KEY, text1ColorChooser.getColorAsString() );
			styleParameters.setProperty(TEXT_2_COLOR_KEY, text2ColorChooser.getColorAsString() );
			styleParameters.setProperty(INPUT_COLOR_KEY, inputColorChooser.getColorAsString() );
			styleParameters.setProperty(DIVIDER_COLOR_KEY, dividerColorChooser.getColorAsString() );
			styleParameters.setProperty(ASIDE_COLOR_KEY, asideColorChooser.getColorAsString() );
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
