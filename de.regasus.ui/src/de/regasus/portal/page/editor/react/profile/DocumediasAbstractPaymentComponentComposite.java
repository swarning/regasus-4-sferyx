package de.regasus.portal.page.editor.react.profile;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.participant.type.combo.ParticipantTypeCombo;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.react.profile.DocumediasAbstractPaymentComponent;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.programme.programmepoint.combo.ProgrammePointCombo;
import de.regasus.users.CurrentUserModel;

public class DocumediasAbstractPaymentComponentComposite extends EntityComposite<DocumediasAbstractPaymentComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean expertMode;

	private Portal portal;

	private Long eventId;


	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;

	private Text webhookUrl;

	private ParticipantTypeCombo participantTypeCombo;

	private ProgrammePointCombo programmePointCombo;

	// *
	// * Widgets
	// **************************************************************************


	public DocumediasAbstractPaymentComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// determine event id
		Long portalPK = (Long) initValues[0];
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		this.eventId = portal.getEventId();
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.DocumediasAbstractPaymentComponent.getString() );
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

		SWTHelper.verticalSpace(parent);

		buildWebhookUrlText(parent);
		buildParticipantTypeCombo(parent);
		buildProgrammePointCombo(parent);
	}


	private void buildWebhookUrlText(Composite parent) throws Exception {
   		SWTHelper.createLabel(parent, DocumediasAbstractPaymentComponent.WEBHOOK_URL);

   		webhookUrl = new Text(parent, SWT.BORDER);
   		GridDataFactory.fillDefaults().grab(true, false).applyTo(webhookUrl);
		webhookUrl.addModifyListener(modifySupport);
	}


	private void buildParticipantTypeCombo(Composite parent) throws Exception {
		SWTHelper.createLabel(parent, DocumediasAbstractPaymentComponent.PARTICIPANT_TYPE_ID);

		participantTypeCombo = new ParticipantTypeCombo(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(participantTypeCombo);
		participantTypeCombo.setEventID(eventId);
		participantTypeCombo.addModifyListener(modifySupport);
	}


	private void buildProgrammePointCombo(Composite parent) throws Exception {
		SWTHelper.createLabel(parent, DocumediasAbstractPaymentComponent.PROGRAMME_POINT_ID);

		programmePointCombo = new ProgrammePointCombo(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(programmePointCombo);
		programmePointCombo.setEventPK(eventId);
		programmePointCombo.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
		}

		webhookUrl.setText( avoidNull(entity.getWebhookUrl()) );

		participantTypeCombo.setParticipantTypePK(entity.getParticipantTypeId());

		programmePointCombo.setProgrammePointPK(entity.getProgrammePointId());
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
			}

			entity.setWebhookUrl( webhookUrl.getText() );
			entity.setParticipantTypeId(participantTypeCombo.getParticipantTypePK());
			entity.setProgrammePointId(programmePointCombo.getProgrammePointPK());
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
			htmlIdText.setEnabled(!fixedStructure);
		}
	}

}
