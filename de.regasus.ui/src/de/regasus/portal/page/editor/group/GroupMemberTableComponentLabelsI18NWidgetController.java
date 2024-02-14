package de.regasus.portal.page.editor.group;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.group.GroupMemberTableComponent;


public class GroupMemberTableComponentLabelsI18NWidgetController implements I18NWidgetController<GroupMemberTableComponent>{

	// the entity
	private GroupMemberTableComponent component;


	// widget Maps

	// table column names
	private Map<String, Text> lastNameColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> firstNameColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> email1ColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> participantTypeColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> participantStateColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> country1ColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> hasBookingsColumnNameWidgetMap = new HashMap<>();

	// button labels and tooltips
	private Map<String, Text> addGroupMemberLabelWidgetMap = new HashMap<>();
	private Map<String, Text> addGroupMemberDescriptionWidgetMap = new HashMap<>();

	private Map<String, Text> editGroupMemberPersonalDataLabelWidgetMap = new HashMap<>();
	private Map<String, Text> editGroupMemberPersonalDataDescriptionWidgetMap = new HashMap<>();

	private Map<String, Text> editGroupMemberProgrammeBookingLabelWidgetMap = new HashMap<>();
	private Map<String, Text> editGroupMemberProgrammeBookingDescriptionWidgetMap = new HashMap<>();

	private Map<String, Text> cancelGroupMemberLabelWidgetMap = new HashMap<>();
	private Map<String, Text> cancelGroupMemberDescriptionWidgetMap = new HashMap<>();

	private Map<String, Text> sendConfirmationEmailLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sendConfirmationEmailDescriptionWidgetMap = new HashMap<>();



	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		GridDataFactory groupGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_NamesOfTableColumns);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		lastNameColumnNameWidgetMap.put(		lang, builder.label(Participant.LAST_NAME).build());
    		firstNameColumnNameWidgetMap.put(		lang, builder.label(Participant.FIRST_NAME).build());
    		email1ColumnNameWidgetMap.put(			lang, builder.label(ContactLabel.email).build());
    		participantTypeColumnNameWidgetMap.put(	lang, builder.label(Participant.PARTICIPANT_TYPE).build());
    		participantStateColumnNameWidgetMap.put(lang, builder.label(Participant.PARTICIPANT_STATE).build());
    		country1ColumnNameWidgetMap.put(		lang, builder.label(Address.COUNTRY).build());
    		hasBookingsColumnNameWidgetMap.put(		lang, builder.label(GroupMemberTableComponent.FIELD_HAS_BOOKINGS_COLUMN_NAME).build());
		}

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_ButtonToAddNewGroupMember);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		addGroupMemberLabelWidgetMap.put(		lang, builder.label(I18N.Label).build());
    		addGroupMemberDescriptionWidgetMap.put(	lang, builder.label(I18N.Tooltip).build());
		}

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_ButtonToEditGroupMembersPersonalData);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		editGroupMemberPersonalDataLabelWidgetMap.put(			lang, builder.label(I18N.Label).build());
    		editGroupMemberPersonalDataDescriptionWidgetMap.put(	lang, builder.label(I18N.Tooltip).build());
		}

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_ButtonToEditGroupMembersProgrammeBookings);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		editGroupMemberProgrammeBookingLabelWidgetMap.put(		lang, builder.label(I18N.Label).build());
    		editGroupMemberProgrammeBookingDescriptionWidgetMap.put(lang, builder.label(I18N.Tooltip).build());
		}

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_ButtonToCancelGroupMember);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		cancelGroupMemberLabelWidgetMap.put(		lang, builder.label(I18N.Label).build());
    		cancelGroupMemberDescriptionWidgetMap.put(	lang, builder.label(I18N.Tooltip).build());
		}

		{
			Group group = new Group(parent, SWT.BORDER);
			groupGridDataFactory.applyTo(group);
			group.setText(I18N.GroupMemberTable_GroupText_ButtonToSendConfirmationEmail);
			group.setLayout( new GridLayout(2, false) );
			builder.parent(group);

			sendConfirmationEmailLabelWidgetMap.put(		lang, builder.label(I18N.Label).build());
			sendConfirmationEmailDescriptionWidgetMap.put(	lang, builder.label(I18N.Tooltip).build());
		}
	}


	@Override
	public void dispose() {
	}


	@Override
	public GroupMemberTableComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(GroupMemberTableComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getLastNameColumnName(), 			lastNameColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getFirstNameColumnName(), 			firstNameColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getEmail1ColumnName(),				email1ColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getParticipantTypeColumnName(),		participantTypeColumnNameWidgetMap	);
						setLanguageStringToTextWidget(component.getParticipantStateColumnName(),	participantStateColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getCountry1ColumnName(),			country1ColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getHasBookingsColumnName(),			hasBookingsColumnNameWidgetMap);

						setLanguageStringToTextWidget(component.getAddGroupMemberLabel(),							addGroupMemberLabelWidgetMap);
						setLanguageStringToTextWidget(component.getAddGroupMemberDescription(),						addGroupMemberDescriptionWidgetMap);

						setLanguageStringToTextWidget(component.getEditGroupMemberPersonalDataLabel(),				editGroupMemberPersonalDataLabelWidgetMap);
						setLanguageStringToTextWidget(component.getEditGroupMemberPersonalDataDescription(),		editGroupMemberPersonalDataDescriptionWidgetMap);

						setLanguageStringToTextWidget(component.getEditGroupMemberProgrammeBookingLabel(),			editGroupMemberProgrammeBookingLabelWidgetMap);
						setLanguageStringToTextWidget(component.getEditGroupMemberProgrammeBookingDescription(),	editGroupMemberProgrammeBookingDescriptionWidgetMap);

						setLanguageStringToTextWidget(component.getCancelGroupMemberLabel(),						cancelGroupMemberLabelWidgetMap);
						setLanguageStringToTextWidget(component.getCancelGroupMemberDescription(),					cancelGroupMemberDescriptionWidgetMap);

						setLanguageStringToTextWidget(component.getSendConfirmationEmailLabel(),					sendConfirmationEmailLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSendConfirmationEmailDescription(),				sendConfirmationEmailDescriptionWidgetMap);
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
		if (component != null) {
			component.setLastNameColumnName(		 buildLanguageString(lastNameColumnNameWidgetMap)         );
			component.setFirstNameColumnName(		 buildLanguageString(firstNameColumnNameWidgetMap)        );
			component.setEmail1ColumnName(			 buildLanguageString(email1ColumnNameWidgetMap)           );
			component.setParticipantTypeColumnName(	 buildLanguageString(participantTypeColumnNameWidgetMap)  );
			component.setParticipantStateColumnName( buildLanguageString(participantStateColumnNameWidgetMap) );
			component.setCountry1ColumnName(		 buildLanguageString(country1ColumnNameWidgetMap)         );
			component.setHasBookingsColumnName(		 buildLanguageString(hasBookingsColumnNameWidgetMap)      );

			component.setAddGroupMemberLabel(						 buildLanguageString(addGroupMemberLabelWidgetMap)                        );
			component.setAddGroupMemberDescription(					 buildLanguageString(addGroupMemberDescriptionWidgetMap)                  );

			component.setEditGroupMemberPersonalDataLabel(			 buildLanguageString(editGroupMemberPersonalDataLabelWidgetMap)           );
			component.setEditGroupMemberPersonalDataDescription(	 buildLanguageString(editGroupMemberPersonalDataDescriptionWidgetMap)     );

			component.setEditGroupMemberProgrammeBookingLabel(		 buildLanguageString(editGroupMemberProgrammeBookingLabelWidgetMap)       );
			component.setEditGroupMemberProgrammeBookingDescription( buildLanguageString(editGroupMemberProgrammeBookingDescriptionWidgetMap) );

			component.setCancelGroupMemberLabel(					 buildLanguageString(cancelGroupMemberLabelWidgetMap)                     );
			component.setCancelGroupMemberDescription(				 buildLanguageString(cancelGroupMemberDescriptionWidgetMap)               );

			component.setSendConfirmationEmailLabel(				 buildLanguageString(sendConfirmationEmailLabelWidgetMap)                 );
			component.setSendConfirmationEmailDescription(			 buildLanguageString(sendConfirmationEmailDescriptionWidgetMap)           );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
