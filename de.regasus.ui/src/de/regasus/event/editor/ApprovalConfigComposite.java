package de.regasus.event.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.ApprovalConfig;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.EntityProvider;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.template.combo.EmailTemplateCombo;
import de.regasus.event.ParticipantType;
import de.regasus.participant.AbstractParticipantTypeProvider;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.participant.state.ChooseParticipantStatesComposite;
import de.regasus.participant.state.ParticipantStateProvider;
import de.regasus.participant.type.ChooseParticipantTypesComposite;
import de.regasus.ui.Activator;

public class ApprovalConfigComposite extends Composite {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// models
	private ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();


	// **************************************************************************
	// * Widgets
	// *

	private ChooseParticipantStatesComposite chooseParticipantStatesComposite;
	private ChooseParticipantTypesComposite chooseParticipantTypesComposite;

	private EmailTemplateCombo approveEmailTemplateCombo;
	private EmailTemplateCombo pendingEmailTemplateCombo;
	private EmailTemplateCombo declineEmailTemplateCombo;
	private EmailTemplateCombo fraudulentEmailTemplateCombo;

	// *
	// * Widgets
	// **************************************************************************


	/**
	 * Observer for the Participant Types of the Event.
	 * Though {@link ChooseParticipantTypesComposite} observes the {@link ParticipantTypeModel}, it does not notice
	 * when a Participant Type is added or removed to an Event, because it does not know the source of available
	 * Participant Types. This is the job of its {@link ParticipantTypeProvider}. Because the
	 * {@link ParticipantTypeProvider} is implemented here as well, it is our job to observe the
	 * relation between Participant Type and Event.
	 */
	private CacheModelListener<Long> participantTypeModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			/* This if-statement is necessary, because ChooseParticipantTypesComposite requests data from
			 * ParticipantTypeModel during its initialization, which causes a refresh. But at this point
			 * of time the Composites are still null.
			 */
			if (chooseParticipantTypesComposite != null) {
				chooseParticipantTypesComposite.initAvailableEntities();
			}
		}
	};


	private EntityProvider<ParticipantType> participantTypeProvider = new AbstractParticipantTypeProvider() {
		@Override
		public List<ParticipantType> getEntityList() {
			List<ParticipantType> participantTypes = Collections.emptyList();
			try {
				if (eventVO != null) {
					participantTypes = participantTypeModel.getParticipantTypesByEvent( eventVO.getId() );
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			return participantTypes;
		}
	};


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent event) {
			if (participantTypeModel != null && eventVO != null && eventVO.getId() != null) {
				try {
					participantTypeModel.removeForeignKeyListener(participantTypeModelListener, eventVO.getId());
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
	};


	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ApprovalConfigComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		addDisposeListener(disposeListener);

		createWidgets();

		syncWidgetsToEntity();

		addModifyListenerToWidgets();
	}


	private void createWidgets() {
		try {
			setLayout(new GridLayout(1, false));

			SashForm sashForm = new SashForm(this, SWT.VERTICAL);
			sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Composite topComposite = new Composite(sashForm, SWT.NONE);
			Composite middleComposite = new Composite(sashForm, SWT.NONE);
			Composite bottomComposite = new Composite(sashForm, SWT.NONE);

			sashForm.setWeights(new int[] { 1, 1, 1 });

			createParticipantStateArea(middleComposite);
			createParticipantTypeArea(topComposite);
			createEmailTemplateArea(bottomComposite);

			addModifyListenerToWidgets();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void createParticipantStateArea(Composite parent) throws Exception {
		parent.setLayout( new FillLayout() );

		Set<Long> participantStateWhitelistIds = CollectionsHelper.createHashSet(
			1L,	// Prospect / Fraudulent
			2L,	// Registration / Approve
			4L,	// Cancellation by Organizer / Decline
			5L	// Wait List / Pending
		);

		chooseParticipantStatesComposite = new ChooseParticipantStatesComposite(
			parent,
			new ParticipantStateProvider(participantStateWhitelistIds),
			SWT.NONE
		);
	}


	private void createParticipantTypeArea(Composite parent) throws Exception {
		parent.setLayout( new FillLayout() );

		chooseParticipantTypesComposite = new ChooseParticipantTypesComposite(
			parent,
			participantTypeProvider,
			SWT.NONE
		);
	}


	private void createEmailTemplateArea(Composite parent) throws Exception {
		parent.setLayout( new FillLayout() );

		Group group = new Group(parent, SWT.NONE);
		group.setLayout( new GridLayout(2, false) );
		group.setText( EmailLabel.EmailTemplate.getString() );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory comboGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		// approve
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Approve");

			approveEmailTemplateCombo = new EmailTemplateCombo(group, SWT.NONE);
			comboGridDataFactory.applyTo(approveEmailTemplateCombo);
		}

		// pending
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Pending");

			pendingEmailTemplateCombo = new EmailTemplateCombo(group, SWT.NONE);
			comboGridDataFactory.applyTo(pendingEmailTemplateCombo);
		}

		// decline
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Decline");

			declineEmailTemplateCombo = new EmailTemplateCombo(group, SWT.NONE);
			comboGridDataFactory.applyTo(declineEmailTemplateCombo);
		}

		// fraudulent
		{
			Label label = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText("Fraudulent");

			fraudulentEmailTemplateCombo = new EmailTemplateCombo(group, SWT.NONE);
			comboGridDataFactory.applyTo(fraudulentEmailTemplateCombo);
		}
	}


	private void addModifyListenerToWidgets() {
		chooseParticipantStatesComposite.addModifyListener(modifySupport);
		chooseParticipantTypesComposite.addModifyListener(modifySupport);

		approveEmailTemplateCombo.addModifyListener(modifySupport);
		pendingEmailTemplateCombo.addModifyListener(modifySupport);
		declineEmailTemplateCombo.addModifyListener(modifySupport);
		fraudulentEmailTemplateCombo.addModifyListener(modifySupport);
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						ApprovalConfig approvalConfig = eventVO.getApprovalConfig();

						chooseParticipantStatesComposite.initAvailableEntities();
						chooseParticipantStatesComposite.setChosenIds( approvalConfig.getParticipantStateIdList() );

						chooseParticipantTypesComposite.initAvailableEntities();
						chooseParticipantTypesComposite.setChosenIds( approvalConfig.getParticipantTypeIdList() );

						approveEmailTemplateCombo.setEmailTemplatePK( approvalConfig.getApproveEmailTemplateId() );
						pendingEmailTemplateCombo.setEmailTemplatePK( approvalConfig.getPendingEmailTemplateId() );
						declineEmailTemplateCombo.setEmailTemplatePK( approvalConfig.getDeclineEmailTemplateId() );
						fraudulentEmailTemplateCombo.setEmailTemplatePK( approvalConfig.getFraudulentEmailTemplateId() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			syncParticipantStates();
			syncParticipantTypes();

			eventVO.getApprovalConfig().setApproveEmailTemplateId( approveEmailTemplateCombo.getEmailTemplatePK() );
			eventVO.getApprovalConfig().setPendingEmailTemplateId( pendingEmailTemplateCombo.getEmailTemplatePK() );
			eventVO.getApprovalConfig().setDeclineEmailTemplateId( declineEmailTemplateCombo.getEmailTemplatePK() );
			eventVO.getApprovalConfig().setFraudulentEmailTemplateId( fraudulentEmailTemplateCombo.getEmailTemplatePK() );
		}
	}


	private void syncParticipantStates() {
		// sync Participant States for the main Participant
		chooseParticipantStatesComposite.syncEntityToWidgets();
		List<Long> participantStatePKs = new ArrayList<>();
		List<ParticipantState> participantStates = getParticipantStateList();
		for (ParticipantState participantState : participantStates) {
			participantStatePKs.add( participantState.getID() );
		}
		eventVO.getApprovalConfig().setParticipantStateIdList(participantStatePKs);
	}


	private void syncParticipantTypes() {
		// sync Participant Types for the main Participant
		chooseParticipantTypesComposite.syncEntityToWidgets();
		List<Long> participantTypePKs = new ArrayList<>();
		List<ParticipantType> participantTypes = getParticipantTypeList();
		for (ParticipantType participantType : participantTypes) {
			participantTypePKs.add( participantType.getId() );
		}
		eventVO.getApprovalConfig().setParticipantTypeIdList(participantTypePKs);
	}


	public void setEventVO(EventVO eventVO) {
		Objects.requireNonNull(eventVO);

		// assure that Event ID does not change
		if (   this.eventVO != null
			&& this.eventVO.getId() != null
			&& !this.eventVO.getId().equals(eventVO.getId())
		) {
			throw new RuntimeException("Event ID must not change!");
		}

		// start observing ParticipantModel when getting the vent ID for the first time
		if (   eventVO != null
			&& eventVO.getId() != null
			&& (this.eventVO == null || this.eventVO.getId() == null)
		) {
			participantTypeModel.addForeignKeyListener(participantTypeModelListener, eventVO.getId());

			try {
				approveEmailTemplateCombo.setEventID( eventVO.getId() );
				pendingEmailTemplateCombo.setEventID( eventVO.getId() );
				declineEmailTemplateCombo.setEventID( eventVO.getId() );
				fraudulentEmailTemplateCombo.setEventID( eventVO.getId() );
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}

		this.eventVO = eventVO;

		syncWidgetsToEntity();
	}


	public List<ParticipantState> getParticipantStateList() {
		return chooseParticipantStatesComposite.getChosenEntities();
	}


	public List<ParticipantType> getParticipantTypeList() {
		return chooseParticipantTypesComposite.getChosenEntities();
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
