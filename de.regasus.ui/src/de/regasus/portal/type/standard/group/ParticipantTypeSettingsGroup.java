package de.regasus.portal.type.standard.group;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.i18n.I18NComposite;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.combo.PortalParticipantTypeCombo;

public class ParticipantTypeSettingsGroup extends EntityGroup<StandardGroupPortalConfig> {

	private final int COL_COUNT = 2;

	private Long portalId;
	private List<Language> languageList;


	// **************************************************************************
	// * Widgets
	// *

	private PortalParticipantTypeCombo defaultParticipantTypeCombo;
	private I18NComposite<StandardGroupPortalConfig> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public ParticipantTypeSettingsGroup(Composite parent, int style, Portal portal)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portal)
		);

		setText( Participant.PARTICIPANT_TYPE.getString() );
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Portal portal = (Portal) initValues[0];
		portalId = portal.getId();

		// determine Portal languages
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
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
		Label defaultParticipantTypeLabel = new Label(parent, SWT.NONE);
		labelGridDataFactory.applyTo(defaultParticipantTypeLabel);
		defaultParticipantTypeLabel.setText(StandardGroupPortalI18N.DefaultParticipantType);

		defaultParticipantTypeCombo = new PortalParticipantTypeCombo(parent, SWT.NONE);
		widgetGridDataFactory.applyTo(defaultParticipantTypeCombo);
		defaultParticipantTypeCombo.addModifyListener(modifySupport);

		// Row 2
		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new ParticipantTypeSettingsGroupI18NWiedgetController());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).indent(0, 20).applyTo(i18nComposite);
		i18nComposite.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() throws Exception {
		defaultParticipantTypeCombo.setPortalId(portalId);
		defaultParticipantTypeCombo.setParticipantTypePK( entity.getDefaultParticipantTypeId() );

		i18nComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setDefaultParticipantTypeId( defaultParticipantTypeCombo.getParticipantTypePK() );
			i18nComposite.syncEntityToWidgets();
		}
	}

}
