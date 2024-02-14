package de.regasus.portal.type.react.hotelspeaker;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static com.lambdalogic.util.rcp.widget.SWTHelper.createTopLabel;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.portal.type.react.hotelspeaker.ReactHotelSpeakerPortalConfig;

//REFERENCE
public class OtherSettingsGroup extends EntityGroup<ReactHotelSpeakerPortalConfig> {

	private final int COL_COUNT = 2;


	// **************************************************************************
	// * Widgets
	// *

	private Text alternativeDomainText;
	private Text urlAfterLogoutText;
	private MultiLineText googleTagManagerHead;
	private MultiLineText googleTagManagerBody;

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

		// alternativeDomain
		{
    		Label label = new Label(parent, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( ReactHotelSpeakerPortalConfig.ALTERNATIVE_DOMAIN.getString() );
    		label.setToolTipText( ReactHotelSpeakerPortalConfig.ALTERNATIVE_DOMAIN.getDescription() );

    		alternativeDomainText = new Text(parent, SWT.BORDER);
    		widgetGridDataFactory.applyTo(alternativeDomainText);
    		alternativeDomainText.addModifyListener(modifySupport);
		}


		/* Row 2 */

		// urlAfterLogout
		{
    		Label label = new Label(parent, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( ReactHotelSpeakerPortalConfig.URL_AFTER_LOGOUT.getString() );
    		label.setToolTipText( ReactHotelSpeakerPortalConfig.URL_AFTER_LOGOUT.getDescription() );

    		urlAfterLogoutText = new Text(parent, SWT.BORDER);
    		widgetGridDataFactory.applyTo(urlAfterLogoutText);
    		urlAfterLogoutText.addModifyListener(modifySupport);
		}


		/* Row 3 */

		// Google Tag Manager <head>
		{
			createTopLabel(parent, ReactHotelSpeakerPortalConfig.GOOGLE_TAG_MANAGER_HEAD.getString());

    		googleTagManagerHead = new MultiLineText(parent, SWT.BORDER);
    		googleTagManagerHead.setMinLineCount(3);
    		widgetGridDataFactory.applyTo(googleTagManagerHead);
    		googleTagManagerHead.addModifyListener(modifySupport);
		}


		/* Row 4 */

		// Google Tag Manager <body>
		{
			createTopLabel(parent, ReactHotelSpeakerPortalConfig.GOOGLE_TAG_MANAGER_BODY.getString());

    		googleTagManagerBody = new MultiLineText(parent, SWT.BORDER);
    		googleTagManagerBody.setMinLineCount(3);
    		widgetGridDataFactory.applyTo(googleTagManagerBody);
    		googleTagManagerBody.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		alternativeDomainText.setText( avoidNull(entity.getAlternativeDomain()) );
		urlAfterLogoutText.setText( avoidNull(entity.getUrlAfterLogout()) );
		googleTagManagerHead.setText( avoidNull(entity.getGoogleTagManagerHead()) );
		googleTagManagerBody.setText( avoidNull( entity.getGoogleTagManagerBody()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setAlternativeDomain( alternativeDomainText.getText() );
			entity.setUrlAfterLogout( urlAfterLogoutText.getText() );
			entity.setGoogleTagManagerHead( googleTagManagerHead.getText() );
			entity.setGoogleTagManagerBody( googleTagManagerBody.getText() );
		}
	}

}
