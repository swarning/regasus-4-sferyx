package de.regasus.portal.type.standard.hotel;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.i18n.I18NComposite;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;

public class RegistrationSettingsGroup extends EntityGroup<StandardHotelPortalConfig> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;


	// **************************************************************************
	// * Widgets
	// *

	private Text profilePortalUrlText;
	private I18NComposite<StandardHotelPortalConfig> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************


	public RegistrationSettingsGroup(Composite parent, int style, Portal portal)
	throws Exception {
		super(parent, style, Objects.requireNonNull(portal));

		setText(StandardHotelPortalI18N.RegistrationGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Portal portal = (Portal) initValues[0];

		// determine Portal languages
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		// Row 1
		{
    		Label label = new Label(parent, SWT.NONE);
    		labelGridDataFactory.copy().applyTo(label);
    		label.setText( StandardHotelPortalConfig.PROFILE_PORTAL_URL.getString() );
//		    		label.setToolTipText(ReactRegistrationPortalI18N.ProfilePortalUrlDescription);

    		profilePortalUrlText = new Text(parent, SWT.BORDER);
    		textGridDataFactory.applyTo(profilePortalUrlText);
    		profilePortalUrlText.addModifyListener(modifySupport);
		}

		// Row 2
		{
    		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new RegistrationSettingsGroupI18NWidgetController());
    		GridDataFactory
    			.fillDefaults()
    			.grab(true, false)
    			.span(COL_COUNT, 1)
    			.applyTo(i18nComposite);
    		i18nComposite.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		profilePortalUrlText.setText( avoidNull(entity.getProfilePortalUrl()) );
		i18nComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setProfilePortalUrl( profilePortalUrlText.getText() );
			i18nComposite.syncEntityToWidgets();
		}
	}

}
