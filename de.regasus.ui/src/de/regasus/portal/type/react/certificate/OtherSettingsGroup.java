package de.regasus.portal.type.react.certificate;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.UtilI18N;

//REFERENCE
public class OtherSettingsGroup extends EntityGroup<ReactCertificatePortalConfig> {

	private final int COL_COUNT = 2;


	// **************************************************************************
	// * Widgets
	// *

	private Text urlAfterLogoutText;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public OtherSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(UtilI18N.Other);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
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
    		label.setText( ReactCertificatePortalConfig.URL_AFTER_LOGOUT.getString() );
    		label.setToolTipText( ReactCertificatePortalConfig.URL_AFTER_LOGOUT.getDescription() );

    		urlAfterLogoutText = new Text(parent, SWT.BORDER);
    		widgetGridDataFactory.applyTo(urlAfterLogoutText);
    		urlAfterLogoutText.addModifyListener(modifySupport);
		}


	}


	@Override
	protected void syncWidgetsToEntity() {
		urlAfterLogoutText.setText( avoidNull(entity.getUrlAfterLogout()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setUrlAfterLogout( urlAfterLogoutText.getText() );
		}
	}

}
