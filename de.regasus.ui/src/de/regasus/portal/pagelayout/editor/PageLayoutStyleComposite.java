package de.regasus.portal.pagelayout.editor;

import java.lang.reflect.Constructor;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.portal.PortalStyle;
import de.regasus.portal.portal.editor.StyleParameterComposite;
import de.regasus.test.portal.PageLayoutBuilder;
import de.regasus.ui.Activator;


public class PageLayoutStyleComposite extends Composite {

	// the entity
	private PageLayout pageLayout;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	// ScrolledComposite to realize vertical scroll bars
	private ScrolledComposite scrollComposite;

	// parent Composite for sub-Composites
	private Composite styleComposite;

	private GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, true);

	private PageLayoutStyleSelectionComposite selectionComposite;
	private Group editComposite;

	private StyleParameterComposite styleParametersComposite;
	private PageLayoutUserStyleComposite userStyleComposite;

	// *
	// * Widgets
	// **************************************************************************


	public PageLayoutStyleComposite(Composite parent, int style, PageLayout pageLayout) throws Exception {
		super(parent, style);

		this.pageLayout = Objects.requireNonNull(pageLayout);

		createWidgets();

		syncWidgetsToEntity();
	}


	private void createWidgets() throws Exception {
		setLayout( new FillLayout() );

		// ScrolledComposite on the left to contain the PageLinkComposites
		scrollComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setShowFocusedControl(true);

		styleComposite = new Composite(scrollComposite, SWT.NONE);
		GridLayout styleLayout = new GridLayout(1, false);
		styleLayout.verticalSpacing = 20;
		styleComposite.setLayout(styleLayout);

		selectionComposite = new PageLayoutStyleSelectionComposite(styleComposite, SWT.NONE, pageLayout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(selectionComposite);
		selectionComposite.addModifyListener(selectionCompositeListener);
		selectionComposite.addModifyListener(modifySupport);

		editComposite = new Group(styleComposite, SWT.NONE);
		gridDataFactory.applyTo(editComposite);
		editComposite.setText(UtilI18N.Settings);

		// layout without margin, because it works only as a container
		GridLayout editGridLayout = new GridLayout();
		editGridLayout.marginHeight = 0;
		editGridLayout.marginWidth = 0;
		editComposite.setLayout(editGridLayout);

		scrollComposite.setContent(styleComposite);
		scrollComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				refreshScrollbar();
			}
		});
	}


	private void refreshScrollbar() {
		Rectangle clientArea = scrollComposite.getClientArea();
		scrollComposite.setMinSize(styleComposite.computeSize(clientArea.width, SWT.DEFAULT));
	}


	ModifyListener selectionCompositeListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			String styleCompositeClassName = null;
			PortalStyle portalStyle = selectionComposite.getPortalStyle();
			if (portalStyle != null) {
				styleCompositeClassName = portalStyle.getStyleCompositeClassName();

				PageLayoutBuilder.initializeStyle(
					pageLayout,
					portalStyle.getStyleProviderClass(),
					portalStyle.getStyleTemplateFileName()
				);
			}
			else if (styleParametersComposite != null) {
				/* portalStyle == null
				 * --> user selected "User Defined" in PageLayoutStyleSelectionComposite
				 * --> generate CSS style to show it in userStyleComposite
				 */
				styleParametersComposite.syncEntityToWidgets();
			}

			adaptEditComposite(styleCompositeClassName);
		}
	};


	/**
	 * Set editComposite according to required StyleComposite class name.
	 *
	 * @param styleCompositeClassName
	 */
	private void adaptEditComposite(String styleCompositeClassName) {
		if (styleCompositeClassName != null) {
			destroyUserStyleText();

			if (   styleParametersComposite != null
				&& ! styleParametersComposite.getClass().getName().equals(styleCompositeClassName)
			) {
				// if styleComposite has to be replaced: set it to null
				destroyStyleParameterComposite();
			}

			if (styleParametersComposite == null) {
				createStyleParameterComposite(styleCompositeClassName);
			}
		}
		else {
			if (userStyleComposite == null) {
				destroyStyleParameterComposite();
				createUserStyleComposite();
			}
		}


		if (styleParametersComposite != null) {
			styleParametersComposite.setPageLayout(pageLayout);
		}
		if (userStyleComposite != null) {
			userStyleComposite.setPageLayout(pageLayout);
		}


		editComposite.layout();
		editComposite.getParent().layout();
	}


	private void createUserStyleComposite() {
		if (userStyleComposite == null) {
			userStyleComposite = new PageLayoutUserStyleComposite(editComposite, SWT.NONE);
			userStyleComposite.addModifyListener(modifySupport);
    		gridDataFactory.applyTo(userStyleComposite);
		}
	}


	private void destroyUserStyleText() {
		if (userStyleComposite != null) {
			userStyleComposite.removeModifyListener(modifySupport);
			userStyleComposite.dispose();
			userStyleComposite = null;
		}
	}


	private void createStyleParameterComposite(String styleCompositeClassName) {
		try {
			Class<?> styleCompositeClass = Class.forName(styleCompositeClassName);
			Constructor<?> constructor = styleCompositeClass.getConstructor(Composite.class);
			styleParametersComposite = (StyleParameterComposite) constructor.newInstance(editComposite);

			gridDataFactory.applyTo( (Composite) styleParametersComposite);
			styleParametersComposite.createWidgets();
			styleParametersComposite.addModifyListener(modifySupport);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			if (styleParametersComposite != null) {
				styleParametersComposite.dispose();
				styleParametersComposite = null;
			}
		}
	}


	private void destroyStyleParameterComposite() {
		if (styleParametersComposite != null) {
    		styleParametersComposite.removeModifyListener(modifySupport);
    		styleParametersComposite.dispose();
    		styleParametersComposite = null;
		}
	}


	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLayout != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						selectionComposite.setPageLayout(pageLayout);

						/* adapt editComposite
						 * Get the PortalStyle from selectionComposite which has been synced right before
						 */
						String styleCompositeClassName = null;
						PortalStyle portalStyle = selectionComposite.getPortalStyle();
						if (portalStyle != null) {
							styleCompositeClassName = portalStyle.getStyleCompositeClassName();
						}
						adaptEditComposite(styleCompositeClassName);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (pageLayout != null) {

			if (selectionComposite != null) {
				selectionComposite.syncEntityToWidgets();
			}

			if (styleParametersComposite != null) {
				styleParametersComposite.syncEntityToWidgets();
			}

			if (userStyleComposite != null) {
				userStyleComposite.syncEntityToWidgets();
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
