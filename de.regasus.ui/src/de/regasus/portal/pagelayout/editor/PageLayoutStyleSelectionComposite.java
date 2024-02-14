package de.regasus.portal.pagelayout.editor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.PortalStyle;
import de.regasus.portal.PortalType;
import de.regasus.portal.StyleProvider;
import de.regasus.ui.Activator;
import de.regasus.users.CurrentUserModel;


public class PageLayoutStyleSelectionComposite extends Group {

	private final boolean expertMode;

	// the entity
	private PageLayout pageLayout;


	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Map<String, PortalStyle> resourceLocator2portalStyleMap = new LinkedHashMap<>();
	private Map<String, Button> resourceLocator2styleButtonMap = new HashMap<>();
	private Button userDefinedStyleButton;

	// *
	// * Widgets
	// **************************************************************************


	public PageLayoutStyleSelectionComposite(Composite parent, int style, PageLayout pageLayout) throws Exception {
		super(parent, style);

		this.pageLayout = Objects.requireNonNull(pageLayout);

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		StyleProvider styleProvider = getStyleProvider();

		for (PortalStyle portalStyle : styleProvider.getPortalStyles()) {
			resourceLocator2portalStyleMap.put(portalStyle.getStyleResourceLocator(), portalStyle);
		}

		setText(UtilI18N.Selection);

		createWidgets();
	}


	private StyleProvider getStyleProvider() {
		try {
			Portal portal = PortalModel.getInstance().getPortal( pageLayout.getPortalId() );
			PortalType portalType = portal.getPortalType();
			Class<?> styleProviderClass = Class.forName( portalType.getStyleProviderClassName() );
			StyleProvider styleProvider = (StyleProvider) styleProviderClass.newInstance();
			return styleProvider;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}


	private void createWidgets() throws Exception {
		RowLayoutFactory.swtDefaults().pack(false).justify(false).applyTo(this);

		for (PortalStyle portalStyle : resourceLocator2portalStyleMap.values()) {
			Button styleButton = new Button(this, SWT.RADIO);

			styleButton.setText( portalStyle.getName().getString() );
			styleButton.setToolTipText( portalStyle.getDescription().getString() );

			styleButton.addSelectionListener(modifySupport);

			resourceLocator2styleButtonMap.put(portalStyle.getStyleResourceLocator(), styleButton);
		}


		userDefinedStyleButton = new Button(this, SWT.RADIO);
		userDefinedStyleButton.setText(I18N.PageLayoutStyleSelectionComposite_UserDefined);
		userDefinedStyleButton.addSelectionListener(modifySupport);
		/* Only in expert mode the user is able to select the button.
		 * However, the button has to be visible to show every user that the current style is user defined.
		 */
		userDefinedStyleButton.setEnabled(expertMode);

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


	private void syncWidgetsToEntity() {
		if (pageLayout != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Button styleButton = null;

						// determine styleButton for special style
						Class<?> styleTemplateClass = pageLayout.getStyleTemplateClass();
						String styleTemplateFileName = pageLayout.getStyleTemplateFileName();
						Properties styleParameters = pageLayout.getStyleParameters();
						if (styleTemplateClass != null &&
							styleTemplateFileName != null &&
							styleParameters != null && !styleParameters.isEmpty()
						) {
    						String resourceLocator = PortalStyle.buildStyleResourceLocator(
    							styleTemplateClass.getName(),
    							styleTemplateFileName
    						);
    						styleButton = resourceLocator2styleButtonMap.get(resourceLocator);
						}

						// if no special style is selected, select userDefinedStyleButton
						if (styleButton == null) {
							styleButton = userDefinedStyleButton;
						}

						styleButton.setSelection(true);
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
			if ( isUserDefined() ) {
				/* only set the styleParameters = null, DO NOT set the styleTemplate = null because the styleTemplate
				 * is always needed to determine which template the user defined template is based on.
				 */
				pageLayout.setStyleParameters(null);
			}
			else {
				String styleResourceLocator = null;
				PortalStyle portalStyle = getPortalStyle();
				if (portalStyle != null) {
					styleResourceLocator = portalStyle.getStyleResourceLocator();
				}
				pageLayout.setStyleTemplate(styleResourceLocator);
			}

		}
	}


	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
		syncWidgetsToEntity();
	}


	public PortalStyle getPortalStyle() {
		for (Map.Entry<String, Button> entry : resourceLocator2styleButtonMap.entrySet()) {
			String resourceLocator = entry.getKey();
			Button button = entry.getValue();

			if ( button.getSelection() ) {
				PortalStyle portalStyle = resourceLocator2portalStyleMap.get(resourceLocator);
				return portalStyle;
			}
		}
		return null;
	}


	public boolean isUserDefined() {
		return userDefinedStyleButton.getSelection();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
