package de.regasus.participant.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.messeinfo.profile.ProfileRelationTypeRole;
import com.lambdalogic.util.Triple;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.ParticipantType;
import de.regasus.participant.state.combo.ParticipantStateCombo;
import de.regasus.participant.type.combo.ParticipantTypeCombo;
import de.regasus.profile.ProfileRelationTypeModel;
import de.regasus.ui.Activator;


public class ParticipantTypeAndStateWizardPage extends WizardPage {
	private ParticipantTypeCombo participantTypeCombo;
	private ParticipantStateCombo participantStateCombo;

	private Long currentEventPK;

	/**
	 * Indicates that the Participant-State must not be empty/null.
	 */
	private boolean stateMustNotBeNull = false;

	/**
	 * Indicates that the Participant-Type must not be empty/null.
	 */
	private boolean typeMustNotBeNull = false;

	private boolean createFromProfile = false;


	private Long defaultParticipantStatePK = null;

	private Long defaultParticipantTypePK = null;

	private ProfileRelationTypeModel profileRelationTypeModel;

	/**
	 * a list of all widgets to be created.
	 */
	private List<ProfileParticipantTypeWidget> profileParticipantTypeWidgets;

	/**
	 * 	List of Triples that contain
     *  a: Long of a ProfileRelationType
     *  b: Role in the ProfileRelation
     *  c: ParticipantType of the created Participant
	 */
	private List<Triple<Long, ProfileRelationTypeRole, Long>> profileRelations;


	/**
	 * a helper class to create a combination of widgets consists of:
	 * 		- a button to select a role of a profile relation, what should be mapped to a participant type
	 * 		- a label for the name of the profile relation
	 * 		- a combo to choose the participant type, what should be mapped to a role of a profile relation
	 */
	private class ProfileParticipantTypeWidget {
		ProfileRelationType profileRelationType;
		ProfileRelationTypeRole profileRelationTypeRole;
		Button roleButton;
		Label profileRelationLabel;
		ParticipantTypeCombo participantTypeCombo;

		public ProfileParticipantTypeWidget(
			Composite parent,
			ProfileRelationType profileRelationType,
			ProfileRelationTypeRole profileRelationTypeRole
		)
		throws Exception {
			this.profileRelationType = profileRelationType;
			this.profileRelationTypeRole = profileRelationTypeRole;

			roleButton = new Button(parent, SWT.CHECK);
			String role = "";
			switch (this.profileRelationTypeRole) {
				case ROLE1:
				case BOTH:
					role = profileRelationType.getDescription12().getString();
					break;
				case ROLE2:
					role = profileRelationType.getDescription21().getString();
					break;
			}
			roleButton.setText(role);
			roleButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					participantTypeCombo.setEnabled(roleButton.getSelection());
					setPageComplete(isPageComplete());
				}
			});

			profileRelationLabel = new Label(parent, SWT.NONE);
			profileRelationLabel.setText(profileRelationType.getName().getString());
			profileRelationLabel.setEnabled(roleButton.getSelection());

			participantTypeCombo = new ParticipantTypeCombo(parent, SWT.NONE);
			participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			participantTypeCombo.setEnabled(roleButton.getSelection());
			participantTypeCombo.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					setPageComplete(isPageComplete());
				}
			});
		}
	}


	/**
	 * @param stateMustNotBeNull Indicates that the Participant-State must not be empty/null.
	 * @param typeMustNotBeNull Indicates that the Participant-Type must not be empty/null.
	 */
	public ParticipantTypeAndStateWizardPage(
		boolean stateMustNotBeNull,
		boolean typeMustNotBeNull,
		boolean createFromProfile
	) {
		super( ParticipantTypeAndStateWizardPage.class.getSimpleName() );

		profileRelationTypeModel = ProfileRelationTypeModel.getInstance();
		profileParticipantTypeWidgets = new ArrayList<>();
		profileRelations = new ArrayList<>();

		this.stateMustNotBeNull = stateMustNotBeNull;
		this.typeMustNotBeNull = typeMustNotBeNull;
		this.createFromProfile = createFromProfile;
		setTitle(I18N.ParticipantTypeAndStatePage_Title);
		setDescription(I18N.ParticipantTypeAndStatePage_Description);
	}


	@Override
	public void createControl(Composite parent) {
		try {
			Composite controlComposite = new Composite(parent, SWT.NONE);
			controlComposite.setLayout(new GridLayout());
			controlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			Collection<ProfileRelationType> profileRelationTypes = profileRelationTypeModel.getAllProfileRelationTypes();

			{ // main profile group
				Composite composite = controlComposite;
				if (profileRelationTypes != null && profileRelationTypes.size() > 0) {
					Group mainProfileGroup = new Group(controlComposite, SWT.SHADOW_IN);
					mainProfileGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					mainProfileGroup.setText(I18N.ParticipantTypeAndStatePage_MainProfile);
					composite = mainProfileGroup;
				}
				composite.setLayout(new GridLayout(2, false));

    			Label participantStateLabel = new Label(composite, SWT.NONE);
    			participantStateLabel.setText( Participant.PARTICIPANT_STATE.getString() );
    			participantStateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    			participantStateCombo = new ParticipantStateCombo(composite, SWT.NONE);
    			participantStateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    			if (defaultParticipantStatePK != null) {
    				participantStateCombo.setParticipantStateID(defaultParticipantStatePK);
    			}
    			participantStateCombo.addModifyListener(new ModifyListener() {
    				@Override
					public void modifyText(ModifyEvent e) {
    					setPageComplete(isPageComplete());
    				}
    			});
    			Label participantTypeLabel = new Label(composite, SWT.NONE);
    			participantTypeLabel.setText( Participant.PARTICIPANT_TYPE.getString() );
    			participantTypeLabel.setToolTipText(I18N.ParticipantTypeAndStatePage_ParticipantType_Tooltip);
    			participantTypeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    			participantTypeCombo = new ParticipantTypeCombo(composite, SWT.NONE);
    			participantTypeCombo.setEventID(currentEventPK);

    			if (defaultParticipantTypePK != null) {
    				if (participantTypeCombo.containsParticipantTypePK(defaultParticipantTypePK)) {
    					participantTypeCombo.setParticipantTypePK(defaultParticipantTypePK);
    				}
    			}

    			participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    			participantTypeCombo.addModifyListener(new ModifyListener() {
    				@Override
					public void modifyText(ModifyEvent e) {
    					setPageComplete(isPageComplete());
    				}
    			});
			}

			{ // connected profile group
				if (createFromProfile && profileRelationTypes != null && profileRelationTypes.size() > 0) {
					ScrolledComposite scrollComposite = new ScrolledComposite(controlComposite, SWT.V_SCROLL);
					scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					scrollComposite.setExpandVertical(true);
					scrollComposite.setExpandHorizontal(true);
					scrollComposite.setShowFocusedControl(true);
					Group connectedProfileGroup = new Group(scrollComposite, SWT.SHADOW_IN);
					scrollComposite.setContent(connectedProfileGroup);

					connectedProfileGroup.setLayout(new GridLayout(3, false));
					connectedProfileGroup.setText(I18N.ParticipantTypeAndStatePage_ConnectedProfiles);

					/* To enable automatic line breaks in a label use SWT.WRAP and set
					 * horizontalAlignment to SWT.FILL and grabExcessHorizontalSpace to true.
					 */
					Label description = new Label(connectedProfileGroup, SWT.WRAP);
					description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
					description.setText(I18N.ParticipantTypeAndStatePage_RelatedProfileDescription);

					Label role = new Label(connectedProfileGroup, SWT.NONE);
					role.setText(I18N.ParticipantTypeAndStatePage_Role);
					SWTHelper.makeBold(role);

					Label relation = new Label(connectedProfileGroup, SWT.NONE);
					relation.setText(I18N.ParticipantTypeAndStatePage_Relation);
					SWTHelper.makeBold(relation);

					Label participantType = new Label(connectedProfileGroup, SWT.NONE);
					participantType.setText("Teilnehmerart");
					SWTHelper.makeBold(participantType);

					Label separator = new Label(connectedProfileGroup, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
					separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

					for (ProfileRelationType profileRelationType : profileRelationTypes) {
						if (profileRelationType.isDirected()) {
							ProfileParticipantTypeWidget widget = new ProfileParticipantTypeWidget(connectedProfileGroup, profileRelationType, ProfileRelationTypeRole.ROLE1);
							widget.participantTypeCombo.setEventID(currentEventPK);
							if (defaultParticipantTypePK != null) {
								if (widget.participantTypeCombo.containsParticipantTypePK(defaultParticipantTypePK)) {
									widget.participantTypeCombo.setParticipantTypePK(defaultParticipantTypePK);
								}
							}
							profileParticipantTypeWidgets.add(widget);

							widget = new ProfileParticipantTypeWidget(connectedProfileGroup, profileRelationType, ProfileRelationTypeRole.ROLE2);
							widget.participantTypeCombo.setEventID(currentEventPK);
							if (defaultParticipantTypePK != null) {
								if (widget.participantTypeCombo.containsParticipantTypePK(defaultParticipantTypePK)) {
									widget.participantTypeCombo.setParticipantTypePK(defaultParticipantTypePK);
								}
							}
							profileParticipantTypeWidgets.add(widget);
						}
						else {
							ProfileParticipantTypeWidget widget = new ProfileParticipantTypeWidget(connectedProfileGroup, profileRelationType, ProfileRelationTypeRole.BOTH);
							widget.participantTypeCombo.setEventID(currentEventPK);
							if (defaultParticipantTypePK != null) {
								if (widget.participantTypeCombo.containsParticipantTypePK(defaultParticipantTypePK)) {
									widget.participantTypeCombo.setParticipantTypePK(defaultParticipantTypePK);
								}
							}
							profileParticipantTypeWidgets.add(widget);
						}
					}

					Rectangle r = scrollComposite.getClientArea();
					scrollComposite.setMinSize(connectedProfileGroup.computeSize(r.width, SWT.DEFAULT));
				}
			}

			setControl(controlComposite);
			setPageComplete(isPageComplete());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void dispose() {
		super.dispose();

		try {
			participantStateCombo.dispose();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			participantTypeCombo.dispose();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public void setEvent(EventVO eventVO) {
		try {
			if (eventVO != null) {
				currentEventPK = eventVO.getID();
			}
			else {
				currentEventPK = null;
			}


			if (participantTypeCombo != null) {
				// set current Event to participantTypeCombo, what will reset its List of Participant Types
				participantTypeCombo.setEventID(currentEventPK);


				// determine initial Participant Type
				Long initialParticipantTypePK = null;

				// the default Participant Type is the initial Participant Type if it exists in ParticipantTypeCombo
				if (defaultParticipantTypePK != null && participantTypeCombo.containsParticipantTypePK(defaultParticipantTypePK)) {
					initialParticipantTypePK = defaultParticipantTypePK;
				}
				// otherwise the Participant Type of the Event if there is only one
				else if (initialParticipantTypePK == null && participantTypeCombo.getEntities().size() == 1) {
					initialParticipantTypePK = participantTypeCombo.getEntities().iterator().next().getId();
				}

				// set initial Participant Type
				if (initialParticipantTypePK != null) {
					participantTypeCombo.setParticipantTypePK(initialParticipantTypePK);
				}


				setPageComplete(isPageComplete());


				for (ProfileParticipantTypeWidget profileParticipantTypeWidget : profileParticipantTypeWidgets) {
					if (profileParticipantTypeWidget.participantTypeCombo != null) {
						profileParticipantTypeWidget.participantTypeCombo.setEventID(currentEventPK);
						if (initialParticipantTypePK != null) {
							profileParticipantTypeWidget.participantTypeCombo.setParticipantTypePK(initialParticipantTypePK);
						}
					}
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public ParticipantState getSelectedParticipantState() {
		ParticipantState participantState = participantStateCombo.getEntity();
		return participantState;
	}


	public Long getSelectedParticipantStatePK() {
		ParticipantState participantState = getSelectedParticipantState();
		if (participantState != null) {
			return participantState.getID();
		}
		return null;
	}


	public ParticipantType getSelectedParticipantType() {
		ParticipantType entity = participantTypeCombo.getEntity();
		return entity;
	}


	public Long getSelectedParticipantTypePK() {
		ParticipantType participantType = getSelectedParticipantType();
		if (participantType != null) {
			return participantType.getId();
		}
		return null;
	}


	@Override
	public boolean isPageComplete() {
		boolean result = (!stateMustNotBeNull || getSelectedParticipantState() != null) &&
						 (!typeMustNotBeNull || getSelectedParticipantType() != null);

		for (ProfileParticipantTypeWidget widget : profileParticipantTypeWidgets) {
			Triple<Long, ProfileRelationTypeRole, Long> triple = new Triple<>(
				widget.profileRelationType.getID(),
				widget.profileRelationTypeRole,
				null // participantTypePK
			);
			if (widget.participantTypeCombo.getEntity() != null) {
				triple.setC(widget.participantTypeCombo.getEntity().getId());
				if (widget.roleButton.getSelection()) {
					if (!profileRelations.contains(triple)) {
						profileRelations.add(triple);
					}
				}
				else {
					profileRelations.remove(triple);
				}
			}
			else {
				/* try to remove all elements in profileRelations, what have no participantTypePK.
				 * That is the case, that the user chose a participantTypePK and then chose again
				 * the empty item of combo box.
				 */
				for (Iterator<Triple<Long, ProfileRelationTypeRole, Long>> it = profileRelations.iterator(); it.hasNext();) {
					Triple<Long, ProfileRelationTypeRole, Long> currentElement = it.next();
					if (currentElement.getA().equals(triple.getA()) && currentElement.getB().equals(triple.getB())) {
						it.remove();
					}
				}
				if (widget.roleButton.getSelection()) {
					result = false;
					break;
				}
			}
		}

		return result;
	}


	/**
	 * @return the defaultParticipantState
	 */
	public Long getDefaultParticipantStatePK() {
		return defaultParticipantStatePK;
	}


	/**
	 * @param defaultParticipantStatePK the defaultParticipantState to set
	 */
	public void setDefaultParticipantStatePK(Long defaultParticipantStatePK) {
		this.defaultParticipantStatePK = defaultParticipantStatePK;
		if (defaultParticipantStatePK != null && participantStateCombo != null) {
			participantStateCombo.setParticipantStateID(defaultParticipantStatePK);
		}
	}


	/**
	 * @return the defaultParticipantTypePK
	 */
	public Long getDefaultParticipantTypePK() {
		return defaultParticipantTypePK;
	}


	/**
	 * @param defaultParticipantTypePK the defaultParticipantTypePK to set
	 */
	public void setDefaultParticipantTypePK(Long defaultParticipantTypePK) {
		this.defaultParticipantTypePK = defaultParticipantTypePK;
	}


	public List<Triple<Long, ProfileRelationTypeRole, Long>> getProfileRelations() {
		return profileRelations;
	}

}
