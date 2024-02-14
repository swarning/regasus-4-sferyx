package de.regasus.portal.type.wikonect.profile;


import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.eventgroup.combo.EventGroupCombo;
import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.profile.role.combo.ProfileRoleCombo;
import de.regasus.ui.Activator;

public class WikonectProfilePortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private Portal portal;
	private WikonectProfilePortalConfig portalConfig;
	private List<Language> languageList;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private EventGroupCombo eventGroupCombo;
	private ProfileRoleCombo profileRoleCombo;
	private Text digitalEventUrl;

	// *
	// * Widgets
	// **************************************************************************


	public WikonectProfilePortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		this.portal = portal;

		portalConfig = (WikonectProfilePortalConfig) portal.getPortalConfig();

		// determine Portal languages
		List<String> languageIds = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageIds);
	}


	@Override
	public void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		setLayout( new GridLayout(2, true) );

		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.grab(true,  false);

		Group eventGroup = buildEventGroup(this);
		groupGridDataFactory.applyTo(eventGroup);

		Group profileGroup = buildProfileGroup(this);
		groupGridDataFactory.applyTo(profileGroup);

		Group digitalEventGroup = buildDigitalEventGroup(this);
		groupGridDataFactory.applyTo(digitalEventGroup);
	}


	private Group buildEventGroup(Composite parent) throws Exception {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Assoziierte Veranstaltungen");
		group.setLayout( new GridLayout(2, false) );

		Label label = new Label(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText("Veranstaltungsgruppe");

		eventGroupCombo = new EventGroupCombo(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(eventGroupCombo);
		eventGroupCombo.addModifyListener(modifySupport);

		return group;
	}


	private Group buildProfileGroup(Composite parent) throws Exception {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Profileinstellungen");
		group.setLayout( new GridLayout(2, false) );

		Label label = new Label(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText("Profilrolle");

		profileRoleCombo = new ProfileRoleCombo(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(profileRoleCombo);
		profileRoleCombo.addModifyListener(modifySupport);

		return group;
	}


	private Group buildDigitalEventGroup(Composite parent) throws Exception {
		Group group = new Group(parent, SWT.NONE);
		group.setText( ParticipantLabel.DigitalEvent.getString() );
		group.setLayout( new GridLayout(2, false) );

		Label label = new Label(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText("URL");

		digitalEventUrl = new Text(group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(digitalEventUrl);
		digitalEventUrl.addModifyListener(modifySupport);

		return group;
	}


	@Override
	public void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						eventGroupCombo.setEventGroupId( portalConfig.getEventGroupId() );
						profileRoleCombo.setProfileRoleId( portalConfig.getProfileRoleId() );
						digitalEventUrl.setText( avoidNull(portalConfig.getDigitalEventUrl()) );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (portalConfig != null) {
			portalConfig.setEventGroupId( eventGroupCombo.getEventGroupId() );
			portalConfig.setProfileRoleId( profileRoleCombo.getProfileRoleId() );
			portalConfig.setDigitalEventUrl( digitalEventUrl.getText() );
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
