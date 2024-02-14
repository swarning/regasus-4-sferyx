package de.regasus.portal.type.dsgv.registration;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.UtilI18N;

public class ExternalSystemsSettingsGroup extends EntityGroup<DsgvRegistrationPortalConfig> {

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


	public ExternalSystemsSettingsGroup(Composite parent, int style)
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


		// Row 1
		Label urlAfterLogoutLabel = new Label(parent, SWT.NONE);
		labelGridDataFactory.applyTo(urlAfterLogoutLabel);
		urlAfterLogoutLabel.setText(DsgvRegistrationPortalI18N.UrlAfterLogout);
		urlAfterLogoutLabel.setToolTipText(DsgvRegistrationPortalI18N.UrlAfterLogoutDescription);

		urlAfterLogoutText = new Text(parent, SWT.BORDER);
		widgetGridDataFactory.applyTo(urlAfterLogoutText);
		urlAfterLogoutText.addModifyListener(modifySupport);
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
