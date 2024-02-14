package de.regasus.participant.editor.overview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.profile.PersonLinkData;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.person.PersonLinkModel;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.editor.overview.ProfileCustomFieldGroupSectionContainer;
import de.regasus.ui.Activator;

public class ParticipantOverviewComposite extends LazyComposite implements DisposeListener {

	private ParticipantModel participantModel;
	private PersonLinkModel personLinkModel;
	private ProfileCustomFieldGroupModel profileCustomFieldGroupModel;
	private ParticipantCustomFieldGroupModel participantCustomFieldGroupModel;

	/**
	 * The responsible for creating adapted SWT controls
	 */
	private FormToolkit formToolkit;

	/**
	 * All sections are created in here
	 */
	private Composite body;

	/**
	 * The presented domain object
	 */
	private Long participantID;

	private ScrolledForm form;

	private ConfigParameterSet configParameterSet;


	/**
	 * Constructor
	 *
	 * @param participantCVO
	 */
	public ParticipantOverviewComposite(
		Composite parent,
		Long participantID,
		ConfigParameterSet configParameterSet
	) {
		super(parent, SWT.NONE);

		this.participantID = participantID;
		this.configParameterSet = configParameterSet;

		// initialize models
		participantModel = ParticipantModel.getInstance();
		personLinkModel = PersonLinkModel.getInstance();
		profileCustomFieldGroupModel = ProfileCustomFieldGroupModel.getInstance();
		participantCustomFieldGroupModel = ParticipantCustomFieldGroupModel.getInstance();


		// initialize other stuff
		formToolkit = new FormToolkit(getDisplay());

		setLayout(new FillLayout());

		addDisposeListener(this);
	}


	@Override
	protected void createPartControl() throws Exception {
		form = formToolkit.createScrolledForm(this);
		form.setText(UtilI18N.Overview);
		body = form.getBody();

		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = 3;
		layout.minNumColumns = 1;
		body.setLayout(layout);


		try {
			Participant participant = participantModel.getParticipant(participantID);

			new PersonSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet.getEvent().getParticipant()
			);

			new ParticipantSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet.getEvent().getParticipant()
			);

			new MembershipSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet.getEvent().getParticipant().getMembership()
			);

			new ConnectionSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet
			);

			new AddressSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet.getEvent().getParticipant().getAddress()
			);

			new CommunicationSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet.getEvent().getParticipant().getCommunication()
			);


			new ProgrammeBookingSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet
			);

			new HotelBookingSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet
			);

			new PreferredPaymentTypeContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet.getEvent().getParticipant().getPreferredPaymentType()
			);

			new ParticipantCustomFieldSectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet.getEvent().getParticipant()
			);


			// create participant custom field groups
			Long eventPK = participant.getEventId();
			Collection<ParticipantCustomFieldGroup> groups = participantCustomFieldGroupModel.getParticipantCustomFieldGroupsByEventPK(eventPK);
			List<ParticipantCustomFieldGroup> groupsList = new ArrayList<>(groups);
			Collections.sort(groupsList, ParticipantCustomFieldGroup_Location_Position_Comparator.getInstance());
			for (ParticipantCustomFieldGroup group : groupsList) {
				new ParticipantCustomFieldGroupSectionContainer(
					formToolkit,
					body,
					participantID,
					group,
					configParameterSet.getEvent().getParticipant()
				);
			}


			// create profile custom fields
			Long personLink = participant.getPersonLink();
			if (personLink != null) {
				PersonLinkData personLinkData = personLinkModel.getPersonLinkData(personLink);
				Long profileID = personLinkData.getProfileID();

				if (profileID != null) {

					// profile custom fields in groups
	    			Collection<ProfileCustomFieldGroup> profileCustomFieldGroups = profileCustomFieldGroupModel.getAllProfileCustomFieldGroups();

	    			List<ProfileCustomFieldGroup> profileCustomFieldGroupList = new ArrayList<>(profileCustomFieldGroups);
	    			Collections.sort(profileCustomFieldGroupList, ProfileCustomFieldGroup_Location_Position_Comparator.getInstance());
	    			for (ProfileCustomFieldGroup group : profileCustomFieldGroupList) {
						new ProfileCustomFieldGroupSectionContainer(
							formToolkit,
							body,
							profileID,
							group,
							configParameterSet.getProfile()
						);
					}
				}
			}


			// create Accountancy section
			new AccountancySectionContainer(
				formToolkit,
				body,
				participantID,
				configParameterSet
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		formToolkit.dispose();
	}


	public void refresh() {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (form != null) {
						form.dispose();
						createPartControl();
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}

}
