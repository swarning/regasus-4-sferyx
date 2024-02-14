package de.regasus.portal.type.openid.connect;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static com.lambdalogic.util.rcp.widget.SWTHelper.createTopLabel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;

//REFERENCE
public class ExternalSystemsSettingsGroup extends EntityGroup<OpenIDConnectPortalConfig> {

	// **************************************************************************
	// * Widgets
	// *

	private Text urlAfterLogoutText;
	private MultiLineText validRedirectUrlsText;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public ExternalSystemsSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(OpenIDConnectPortalI18N.ExternalSystemsGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		final int COL_COUNT = 2;
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		/* Row 1 */

		// urlAfterLogout
		{
    		Label label = new Label(parent, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( OpenIDConnectPortalConfig.URL_AFTER_LOGOUT.getString() );
    		label.setToolTipText( OpenIDConnectPortalConfig.URL_AFTER_LOGOUT.getDescription() );

    		urlAfterLogoutText = new Text(parent, SWT.BORDER);
    		widgetGridDataFactory.applyTo(urlAfterLogoutText);
    		urlAfterLogoutText.addModifyListener(modifySupport);
		}


		/* Row 2 */
		{
			createTopLabel(
				parent,
				OpenIDConnectPortalConfig.VALID_REDIRECT_URLS_TEXT.getString(),
				OpenIDConnectPortalConfig.VALID_REDIRECT_URLS_TEXT.getDescription()
			);

    		validRedirectUrlsText = new MultiLineText(parent, SWT.BORDER);
    		validRedirectUrlsText.setMinLineCount(3);
    		widgetGridDataFactory.applyTo(validRedirectUrlsText);
    		validRedirectUrlsText.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		urlAfterLogoutText.setText( avoidNull(entity.getUrlAfterLogout()) );
		validRedirectUrlsText.setText( avoidNull( entity.getValidRedirectUrlsText()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setUrlAfterLogout( urlAfterLogoutText.getText() );


			// valid redirect URLs
			String validRedirectUrlsStr = validRedirectUrlsText.getText();

			List<String> validRedirectUrlsLines = StringHelper.getLines(validRedirectUrlsStr);

			int lineNumber = 0;
			for (String line : validRedirectUrlsLines) {
				lineNumber++;

				if (line.length()> 0 && !line.startsWith("//")) {
					try {
						new URL(line);
					}
    				catch (MalformedURLException e) {
    					throw new RuntimeException(I18N.MalformedUrlInLineN.replace("<n>", String.valueOf(lineNumber)));
    				}
				}
			}

			entity.setValidRedirectUrlsText(validRedirectUrlsStr);
		}
	}

}
