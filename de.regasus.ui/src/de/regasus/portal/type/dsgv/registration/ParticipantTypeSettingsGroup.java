package de.regasus.portal.type.dsgv.registration;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.combo.PortalParticipantTypeCombo;

public class ParticipantTypeSettingsGroup extends EntityGroup<DsgvRegistrationPortalConfig> {

	private final int COL_COUNT = 2;

	private Long portalId;


	// **************************************************************************
	// * Widgets
	// *

	private PortalParticipantTypeCombo defaultParticipantTypeCombo;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public ParticipantTypeSettingsGroup(Composite parent, int style, Long portalPK)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);

		setText( Participant.PARTICIPANT_TYPE.getString() );
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		portalId = (Long) initValues[0];
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
		defaultParticipantTypeLabel.setText(DsgvRegistrationPortalI18N.DefaultParticipantType);

		defaultParticipantTypeCombo = new PortalParticipantTypeCombo(parent, SWT.NONE);
		widgetGridDataFactory.applyTo(defaultParticipantTypeCombo);
		defaultParticipantTypeCombo.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() throws Exception {
		defaultParticipantTypeCombo.setPortalId(portalId);
		defaultParticipantTypeCombo.setParticipantTypePK( entity.getDefaultParticipantTypeId() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setDefaultParticipantTypeId( defaultParticipantTypeCombo.getParticipantTypePK() );
		}
	}

}
