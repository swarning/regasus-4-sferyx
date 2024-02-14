package de.regasus.portal.type.standard.hotel;

import java.util.Properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.ui.Activator;


public class StandardHotelStyleSizeComposite extends Composite {

	// widths and heights
	private static final String PAGE_WIDTH_KEY = "page-width";
	private static final Integer PAGE_WIDTH_DEFAULT = 1500; // px

	// 'none' for full-width layout, '@page-width' for wrapped layout
	private static final String PAGE_WRAPPER_WIDTH_KEY = "page-wrapper-width"; // none
	private static final String PAGE_WRAPPER_WIDTH_VALUE_WRAPPED = "@my-page-width";
	private static final String PAGE_WRAPPER_WIDTH_VALUE_NONE = "none";

	private static final String HEADER_HEIGHT_KEY = "header-height";
	private static final Integer HEADER_HEIGHT_DEFAULT = 200; // px

	private static final String FOOTER_HEIGHT_KEY = "footer-height";
	private static final Integer FOOTER_HEIGHT_DEFAULT = 50; // px
	private static final Integer FOOTER_HEIGHT_CLASSIC_DEFAULT = 50; // px

	private static final String ASIDE_BORDER_WIDTH_KEY = "aside-border-width";
	private static final Integer ASIDE_BORDER_WIDTH_DEFAULT = 1; // px

	private static final String NAVBAR_HEIGHT_KEY = "navbar-height";
	private static final Integer NAVBAR_HEIGHT_DEFAULT = 56; // px

	// 'fixed' for fixing navbar on top or 'relative' for natural scrolling
	private static final String NAVBAR_POSITION_KEY = "navbar-position";
	private static final String NAVBAR_POSITION_VALUE_RELATIVE = "relative";
	private static final String NAVBAR_POSITION_VALUE_FIXED = "fixed";
	private static final String NAVBAR_POSITION_DEFAULT = NAVBAR_POSITION_VALUE_RELATIVE;


	// entity
	private PageLayout pageLayout;


	// **************************************************************************
	// * Widgets
	// *

	private NullableSpinner pageWidthSpinner;
	private Button wrappedLayoutButton;
	private Button fullWidthButton;
	private NullableSpinner headerHeightSpinner;
	private NullableSpinner footerHeightSpinner;
	private NullableSpinner asideBorderWidthSpinner;
	private NullableSpinner navbarHeightSpinner;
	private Button navbarPositionRelativeButton;
	private Button navbarPositionFixedButton;

	// *
	// * Widgets
	// **************************************************************************

	private ModifySupport modifySupport = new ModifySupport(this);


	public StandardHotelStyleSizeComposite(Composite parent) {
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


		final int NUM_COLS = 4;
		GridLayoutFactory compositeGridLayoutFactory = GridLayoutFactory.swtDefaults().numColumns(NUM_COLS);
		GridDataFactory compositeGridDataFactory = GridDataFactory.fillDefaults().grab(true, true);

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory spinnerGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);
		GridDataFactory pushGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);
		GridDataFactory separatorGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(NUM_COLS, 1);

		// leftComposite
		{
			Composite composite = new Composite(parent, SWT.NONE);
			compositeGridDataFactory.applyTo(composite);
			compositeGridLayoutFactory.applyTo(composite);

    		// page width
    		{
    			Label label = new Label(composite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_PageWidth);

    			pageWidthSpinner = new NullableSpinner(composite, SWT.BORDER);
    			spinnerGridDataFactory.applyTo(pageWidthSpinner);
    			pageWidthSpinner.setStartValue( PAGE_WIDTH_DEFAULT.longValue() );
    			pageWidthSpinner.setMinimum(100);
    			pageWidthSpinner.setMaximum(10000);
    			pageWidthSpinner.setNullable(false);
    			WidgetSizer.setWidth(pageWidthSpinner);

    			pageWidthSpinner.addModifyListener(modifySupport);

    			Button resetButton = new Button(composite, SWT.PUSH);
    			pushGridDataFactory.applyTo(resetButton);
    			resetButton.setText(PAGE_WIDTH_DEFAULT + "px");
    			resetButton.addSelectionListener( new SelectionAdapter() {
    				@Override
    				public void widgetSelected(SelectionEvent e) {
    					pageWidthSpinner.setValue(PAGE_WIDTH_DEFAULT);
    				}
    			} );

    			new Label(composite, SWT.NONE);
    		}

    		// full width
    		{
    			new Label(composite, SWT.NONE); // placeholder

    			Composite radioComposite = new Composite(composite, SWT.NONE);
    			GridDataFactory.fillDefaults().span(NUM_COLS - 1, 1).applyTo(radioComposite);
    			RowLayoutFactory.swtDefaults().margins(0, 0).pack(false).justify(false).applyTo(radioComposite);

    			wrappedLayoutButton = new Button(radioComposite, SWT.RADIO);
    			wrappedLayoutButton.setText(I18N.PageLayoutEditor_StandardStyleComposite_WrappedLayout);
    			wrappedLayoutButton.addSelectionListener(modifySupport);

    			fullWidthButton = new Button(radioComposite, SWT.RADIO);
    			fullWidthButton.setText(I18N.PageLayoutEditor_StandardStyleComposite_FullWidth);
    			fullWidthButton.addSelectionListener(modifySupport);
    		}

    		// separator
    		separatorGridDataFactory.applyTo( new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR) );

    		// header height
    		{
    			Label label = new Label(composite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_HeaderHeight);

    			headerHeightSpinner = new NullableSpinner(composite, SWT.BORDER);
    			spinnerGridDataFactory.applyTo(headerHeightSpinner);
    			headerHeightSpinner.setStartValue( HEADER_HEIGHT_DEFAULT.longValue() );
    			headerHeightSpinner.setMinimum(0);
    			headerHeightSpinner.setMaximum(10000);
    			headerHeightSpinner.setNullable(false);
    			WidgetSizer.setWidth(headerHeightSpinner);

    			headerHeightSpinner.addModifyListener(modifySupport);

    			Button resetButton = new Button(composite, SWT.PUSH);
    			pushGridDataFactory.applyTo(resetButton);
    			resetButton.setText(HEADER_HEIGHT_DEFAULT + "px");
    			resetButton.addSelectionListener( new SelectionAdapter() {
    				@Override
    				public void widgetSelected(SelectionEvent e) {
    					headerHeightSpinner.setValue(HEADER_HEIGHT_DEFAULT);
    				}
    			} );

    			new Label(composite, SWT.NONE);
    		}

    		// footer height
    		{
    			Label label = new Label(composite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_FooterHeight);

    			footerHeightSpinner = new NullableSpinner(composite, SWT.BORDER);
    			spinnerGridDataFactory.applyTo(footerHeightSpinner);
    			Integer footerHeightDefault = isStandard() ? FOOTER_HEIGHT_DEFAULT : FOOTER_HEIGHT_CLASSIC_DEFAULT;
				footerHeightSpinner.setStartValue( footerHeightDefault.longValue() );
    			footerHeightSpinner.setMinimum(0);
    			footerHeightSpinner.setMaximum(10000);
    			footerHeightSpinner.setNullable(false);
    			WidgetSizer.setWidth(footerHeightSpinner);

    			footerHeightSpinner.addModifyListener(modifySupport);

    			Button resetButton = new Button(composite, SWT.PUSH);
    			pushGridDataFactory.applyTo(resetButton);
    			resetButton.setText(footerHeightDefault + "px");
    			resetButton.addSelectionListener( new SelectionAdapter() {
    				@Override
    				public void widgetSelected(SelectionEvent e) {
    					footerHeightSpinner.setValue(FOOTER_HEIGHT_DEFAULT);
    				}
    			} );

    			new Label(composite, SWT.NONE);
    		}

    		// separator
    		separatorGridDataFactory.applyTo( new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR) );

    		// aside border width height
    		{
    			Label label = new Label(composite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_AsideBorderWidth);

    			asideBorderWidthSpinner = new NullableSpinner(composite, SWT.BORDER);
    			spinnerGridDataFactory.applyTo(asideBorderWidthSpinner);
    			asideBorderWidthSpinner.setStartValue( ASIDE_BORDER_WIDTH_DEFAULT.longValue() );
    			asideBorderWidthSpinner.setMinimum(0);
    			asideBorderWidthSpinner.setMaximum(1000);
    			asideBorderWidthSpinner.setNullable(false);
    			WidgetSizer.setWidth(asideBorderWidthSpinner);

    			asideBorderWidthSpinner.addModifyListener(modifySupport);

    			Button resetButton = new Button(composite, SWT.PUSH);
    			pushGridDataFactory.applyTo(resetButton);
    			resetButton.setText(ASIDE_BORDER_WIDTH_DEFAULT + "px");
    			resetButton.addSelectionListener( new SelectionAdapter() {
    				@Override
    				public void widgetSelected(SelectionEvent e) {
    					asideBorderWidthSpinner.setValue(ASIDE_BORDER_WIDTH_DEFAULT);
    				}
    			} );

    			new Label(composite, SWT.NONE);
    		}

    		// separator
    		separatorGridDataFactory.applyTo( new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR) );

    		// navbar height
    		{
    			Label label = new Label(composite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_NavigationBarHeight);

    			navbarHeightSpinner = new NullableSpinner(composite, SWT.BORDER);
    			spinnerGridDataFactory.applyTo(navbarHeightSpinner);
    			navbarHeightSpinner.setStartValue( NAVBAR_HEIGHT_DEFAULT.longValue() );
    			navbarHeightSpinner.setMinimum(0);
    			navbarHeightSpinner.setMaximum(1000);
    			navbarHeightSpinner.setNullable(false);
    			WidgetSizer.setWidth(navbarHeightSpinner);

    			navbarHeightSpinner.addModifyListener(modifySupport);

    			Button resetButton = new Button(composite, SWT.PUSH);
    			pushGridDataFactory.applyTo(resetButton);
    			resetButton.setText(NAVBAR_HEIGHT_DEFAULT + "px");
    			resetButton.addSelectionListener( new SelectionAdapter() {
    				@Override
    				public void widgetSelected(SelectionEvent e) {
    					navbarHeightSpinner.setValue(NAVBAR_HEIGHT_DEFAULT);
    				}
    			} );

    			new Label(composite, SWT.NONE);
    		}

    		// navbar position
    		{
    			Label label = new Label(composite, SWT.NONE);
    			labelGridDataFactory.applyTo(label);
    			label.setText(I18N.PageLayoutEditor_StandardStyleComposite_NavigationBarPosition);

    			Composite radioComposite = new Composite(composite, SWT.NONE);
    			GridDataFactory.fillDefaults().span(NUM_COLS - 1, 1).applyTo(radioComposite);
    			RowLayoutFactory.swtDefaults().margins(0, 0).pack(false).justify(false).applyTo(radioComposite);

    			navbarPositionRelativeButton = new Button(radioComposite, SWT.RADIO);
    			navbarPositionRelativeButton.setText(I18N.PageLayoutEditor_StandardStyleComposite_NavigationBarPositionRelative);
    			navbarPositionRelativeButton.addSelectionListener(modifySupport);

    			navbarPositionFixedButton = new Button(radioComposite, SWT.RADIO);
    			navbarPositionFixedButton.setText(I18N.PageLayoutEditor_StandardStyleComposite_NavigationBarPositionFixed);
    			navbarPositionFixedButton.addSelectionListener(modifySupport);
    		}
		} // leftComposite


		// rightComposite
		{
			Composite composite = new Composite(parent, SWT.NONE);
			compositeGridDataFactory.applyTo(composite);
			compositeGridLayoutFactory.applyTo(composite);

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
    						Integer width = TypeHelper.toInteger( styleParameters.getProperty(PAGE_WIDTH_KEY) );
    						if (width == null) {
    							width = PAGE_WIDTH_DEFAULT;
    						}
    						pageWidthSpinner.setValue(width);
						}

						{
    						String pageWrapperWidth = styleParameters.getProperty(PAGE_WRAPPER_WIDTH_KEY);

    						boolean isWrapped = PAGE_WRAPPER_WIDTH_VALUE_WRAPPED.equals(pageWrapperWidth);
							wrappedLayoutButton.setSelection( isWrapped );

							boolean isFull = PAGE_WRAPPER_WIDTH_VALUE_NONE.equals(pageWrapperWidth);
							fullWidthButton.setSelection( isFull );
						}

						{
    						Integer height = TypeHelper.toInteger( styleParameters.getProperty(HEADER_HEIGHT_KEY) );
    						if (height == null) {
    							height = HEADER_HEIGHT_DEFAULT;
    						}
    						headerHeightSpinner.setValue(height);
						}

						{
							Integer height = TypeHelper.toInteger( styleParameters.getProperty(FOOTER_HEIGHT_KEY) );
							if (height == null) {
								height = isStandard() ? FOOTER_HEIGHT_DEFAULT : FOOTER_HEIGHT_CLASSIC_DEFAULT;
							}
							footerHeightSpinner.setValue(height);
						}

						{
							Integer width = TypeHelper.toInteger( styleParameters.getProperty(ASIDE_BORDER_WIDTH_KEY) );
							if (width == null) {
								width = ASIDE_BORDER_WIDTH_DEFAULT;
							}
							asideBorderWidthSpinner.setValue(width);
						}

						{
							Integer height = TypeHelper.toInteger( styleParameters.getProperty(NAVBAR_HEIGHT_KEY) );
							if (height == null) {
								height = NAVBAR_HEIGHT_DEFAULT;
							}
							navbarHeightSpinner.setValue(height);
						}

						{
							String position = styleParameters.getProperty(NAVBAR_POSITION_KEY);
							navbarPositionFixedButton.setSelection( NAVBAR_POSITION_VALUE_FIXED.equals(position) );
							navbarPositionRelativeButton.setSelection( NAVBAR_POSITION_VALUE_RELATIVE.equals(position) );
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
	    		Integer width = pageWidthSpinner.getValueAsInteger();
	    		if (width == null) {
	    			width = PAGE_WIDTH_DEFAULT;
	    		}
	    		styleParameters.setProperty(PAGE_WIDTH_KEY, TypeHelper.toString(width) );
			}

			{
	    		String pageWrapperWidth = PAGE_WRAPPER_WIDTH_VALUE_WRAPPED;
	    		if ( wrappedLayoutButton.getSelection() ) {
	    			pageWrapperWidth = PAGE_WRAPPER_WIDTH_VALUE_WRAPPED;
	    		}
	    		else if ( fullWidthButton.getSelection() ) {
	    			pageWrapperWidth = PAGE_WRAPPER_WIDTH_VALUE_NONE;
	    		}
	    		styleParameters.setProperty(PAGE_WRAPPER_WIDTH_KEY, pageWrapperWidth);
			}

			{
	    		Integer height = headerHeightSpinner.getValueAsInteger();
	    		if (height == null) {
	    			height = HEADER_HEIGHT_DEFAULT;
	    		}
	    		styleParameters.setProperty(HEADER_HEIGHT_KEY, TypeHelper.toString(height) );
			}

			{
				Integer height = footerHeightSpinner.getValueAsInteger();
				if (height == null) {
					height = isStandard() ? FOOTER_HEIGHT_DEFAULT : FOOTER_HEIGHT_CLASSIC_DEFAULT;
				}
				styleParameters.setProperty(FOOTER_HEIGHT_KEY, TypeHelper.toString(height) );
			}

			{
				Integer width = asideBorderWidthSpinner.getValueAsInteger();
				if (width == null) {
					width = ASIDE_BORDER_WIDTH_DEFAULT;
				}
				styleParameters.setProperty(ASIDE_BORDER_WIDTH_KEY, TypeHelper.toString(width) );
			}

			{
				Integer height = navbarHeightSpinner.getValueAsInteger();
				if (height == null) {
					height = NAVBAR_HEIGHT_DEFAULT;
				}
				styleParameters.setProperty(NAVBAR_HEIGHT_KEY, TypeHelper.toString(height) );
			}

			{
				String position = NAVBAR_POSITION_DEFAULT;
				if ( navbarPositionFixedButton.getSelection() ) {
					position = NAVBAR_POSITION_VALUE_FIXED;
				}
				else if ( navbarPositionRelativeButton.getSelection() ) {
					position = NAVBAR_POSITION_VALUE_RELATIVE;
				}
				styleParameters.setProperty(NAVBAR_POSITION_KEY, position);
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
