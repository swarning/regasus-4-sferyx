package de.regasus.portal.type.react.profile;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;
import de.regasus.portal.type.standard.registration.StandardRegistrationPortalConfig;
import de.regasus.portal.type.standard.registration.ValidRedirectUrlsProvider;

//REFERENCE
public class ValidRedirectUrlsGroup extends EntityGroup<ValidRedirectUrlsProvider> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private MultiLineText validRedirectUrlsText;

	// *
	// * Widgets
	// **************************************************************************


	public ValidRedirectUrlsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText( StandardRegistrationPortalConfig.VALID_REDIRECT_URLS_TEXT.getString() );
		setToolTipText( StandardRegistrationPortalConfig.VALID_REDIRECT_URLS_TEXT.getDescription() );
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		validRedirectUrlsText = new MultiLineText(parent, SWT.BORDER);
		validRedirectUrlsText.setMinLineCount(3);
		GridDataFactory.swtDefaults()
    		.align(SWT.FILL, SWT.CENTER)
    		.grab(true, false)
    		.applyTo(validRedirectUrlsText);

		validRedirectUrlsText.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		validRedirectUrlsText.setText( avoidNull( entity.getValidRedirectUrlsText()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
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
