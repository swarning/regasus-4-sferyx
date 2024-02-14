package de.regasus.portal.type.standard.registration;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static com.lambdalogic.util.rcp.widget.SWTHelper.createTopLabel;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.widget.MultiLineText;

//REFERENCE
public class GoogleSettingsGroup extends EntityGroup<StandardRegistrationPortalConfig> {

	private final int COL_COUNT = 2;


	// **************************************************************************
	// * Widgets
	// *

	private MultiLineText googleTagManagerHead;
	private MultiLineText googleTagManagerBody;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public GoogleSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardRegistrationPortalI18N.GoogleGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

//		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
//			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		/* Row 1 */

		// Google Tag Manager <head>
		{
			createTopLabel(parent, StandardRegistrationPortalConfig.GOOGLE_TAG_MANAGER_HEAD.getString());

    		googleTagManagerHead = new MultiLineText(parent, SWT.BORDER);
    		googleTagManagerHead.setMinLineCount(3);
    		widgetGridDataFactory.applyTo(googleTagManagerHead);
    		googleTagManagerHead.addModifyListener(modifySupport);
		}


		/* Row 2 */

		// Google Tag Manager <body>
		{
			createTopLabel(parent, StandardRegistrationPortalConfig.GOOGLE_TAG_MANAGER_BODY.getString());

    		googleTagManagerBody = new MultiLineText(parent, SWT.BORDER);
    		googleTagManagerBody.setMinLineCount(3);
    		widgetGridDataFactory.applyTo(googleTagManagerBody);
    		googleTagManagerBody.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		googleTagManagerHead.setText( avoidNull(entity.getGoogleTagManagerHead()) );
		googleTagManagerBody.setText( avoidNull( entity.getGoogleTagManagerBody()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setGoogleTagManagerHead( googleTagManagerHead.getText() );
			entity.setGoogleTagManagerBody( googleTagManagerBody.getText() );
		}
	}

}
