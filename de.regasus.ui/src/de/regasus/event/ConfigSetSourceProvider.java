package de.regasus.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.config.parameterset.ConfigConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.CustomFieldConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.EventPortalConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.FieldConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ProgrammeConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.ConfigParameterSetModel;

public class ConfigSetSourceProvider extends AbstractSourceProvider {

	// The following fields have to be defined as variables in de.regasus.event/plugin.xml
	public static final String ALLOW_CUSTOMER_CONFIG = "configuration.allowCustomerConfig";
	public static final String EVENT = "configuration.event";
	public static final String INVOICE = "configuration.event.invoice";
	public static final String PROGRAMME = "configuration.event.programme";
	public static final String HOTEL = "configuration.event.hotel";
	public static final String FORM_EDITOR = "configuration.event.formEditor";
	public static final String PORTAL = "configuration.event.portal";
	public static final String CREATE_PORTAL = "configuration.event.portal.create";
	public static final String SPECIAL_CONDITIONS = "configuration.event.portal.specialConditions";
	public static final String SCRIPT_COMPONENT = "configuration.event.portal.scriptComponent";
	public static final String OFFERING_FILTER = "configuration.event.portal.offeringFilter";
	public static final String BOOKING_RULES = "configuration.event.portal.bookingRules";
	public static final String PARTICIPANT_CUSTOM_FIELD = "configuration.event.participant.customField";
	public static final String PARTICIPANT_SIMPLE_CUSTOM_FIELD = "configuration.event.participant.simpleCustomField";
	public static final String PROFILE = "configuration.profile";
	public static final String PROFILE_CUSTOM_FIELD = "configuration.profile.customField";
	public static final String PROFILE_RELATION = "configuration.profileRelation";
	public static final String BADGE = "configuration.badge";
	public static final String LEAD = "configuration.lead";
	public static final String REGISTER_DATE = "configuration.registerDate";
	public static final String CERTIFICATE_PRINT = "configuration.certificatePrint";
	public static final String PROGRAMME_NOTE_TIME = "configuration.programmeNoteTime";
	public static final String HOTEL_NOTE_TIME = "configuration.hotelNoteTime";
	public static final String LOCATION = "configuration.event.location";
	public static final String ONSITE_WORKFLOW = "configuration.onsiteWorkflow";

	/* The values "true" and "false" does not work for SourceProviders!
	 * Therefore we use "yes" and "no" to encode boolean values.
	 */
	public static final String YES = "yes";
	public static final String NO = "no";


	public static final String[] SOURCE_NAMES = {
		ALLOW_CUSTOMER_CONFIG,
		EVENT,
		INVOICE,
		PROGRAMME,
		HOTEL,
		FORM_EDITOR,
		PORTAL,
		CREATE_PORTAL,
		SPECIAL_CONDITIONS,
		SCRIPT_COMPONENT,
		OFFERING_FILTER,
		BOOKING_RULES,
		PARTICIPANT_CUSTOM_FIELD,
		PARTICIPANT_SIMPLE_CUSTOM_FIELD,
		PROFILE_CUSTOM_FIELD,
		PROFILE,
		PROFILE_RELATION,
		BADGE,
		LEAD,
		REGISTER_DATE,
		CERTIFICATE_PRINT,
		PROGRAMME_NOTE_TIME,
		HOTEL_NOTE_TIME,
		LOCATION,
		ONSITE_WORKFLOW
	};


	/**
	 * Safe the window because we cannot get it in dispose() with
	 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().
	 */
	private IWorkbenchWindow window;

    private ConfigParameterSetModel configSetModel;

    private Long eventPK = null;


	public ConfigSetSourceProvider() {
		configSetModel = ConfigParameterSetModel.getInstance();

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			init(window);
		}
		else {
    		/* SelectionListener and PartListener can not be initialized now, because there is no
    		 * ActiveWorkbenchWindow at this time. Therefore it is done later, when the WorkbenchWindow
    		 * is opened.
    		 */
    		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
    			@Override
    			public void windowOpened(IWorkbenchWindow window) {
    				init(window);
    				PlatformUI.getWorkbench().removeWindowListener(this);
    			}

    			@Override
    			public void windowDeactivated(IWorkbenchWindow window) {
    			}

    			@Override
    			public void windowClosed(IWorkbenchWindow window) {
    			}

    			@Override
    			public void windowActivated(IWorkbenchWindow window) {
    			}
    		});
		}
	}


	private void init(IWorkbenchWindow window) {
		this.window = window;

		window.getSelectionService().addSelectionListener(selectionListener);
		window.getPartService().addPartListener(partListener2);

		configSetModel.addListener(modelListener);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(selectionListener);
		window.getPartService().removePartListener(partListener2);

		configSetModel.removeListener(modelListener);
	}


	@Override
	public Map getCurrentState() {
        Map<String, String> currentStateMap = new HashMap<>(1);

        try {
			ConfigParameterSet configParameterSet = configSetModel.getConfigParameterSet(eventPK);

			if (configParameterSet != null) { // is null during shutdown
    			ConfigConfigParameterSet configConfigParameterSet = configParameterSet.getConfig();
    			currentStateMap.put(ALLOW_CUSTOMER_CONFIG, configConfigParameterSet.isAllowCustomerConfig() ? YES : NO);

    	        FieldConfigParameterSet eventConfigParameterSet = configParameterSet.getEvent();
    	        currentStateMap.put(EVENT, eventConfigParameterSet.isVisible() ? YES : NO);

    	        FieldConfigParameterSet invoiceConfigParameterSet = configParameterSet.getEvent().getInvoice();
    	        currentStateMap.put(INVOICE, invoiceConfigParameterSet.isVisible() ? YES : NO);

    	        ProgrammeConfigParameterSet programmeConfigParameterSet = configParameterSet.getEvent().getProgramme();
    	        currentStateMap.put(PROGRAMME, programmeConfigParameterSet.isVisible() ? YES : NO);

    	        HotelConfigParameterSet hotelConfigParameterSet = configParameterSet.getEvent().getHotel();
    	        currentStateMap.put(HOTEL, hotelConfigParameterSet.isVisible() ? YES : NO);

    	        FieldConfigParameterSet formEditor = configParameterSet.getEvent().getFormEditor();
    	        currentStateMap.put(FORM_EDITOR, formEditor.isVisible() ? YES : NO);

    	        EventPortalConfigParameterSet portal = configParameterSet.getEvent().getPortal();
    	        currentStateMap.put(PORTAL,             portal.isVisible()           ? YES : NO);
    	        currentStateMap.put(CREATE_PORTAL,      portal.isCreate()            ? YES : NO);
    	        currentStateMap.put(SPECIAL_CONDITIONS, portal.isSpecialConditions() ? YES : NO);
    	        currentStateMap.put(SCRIPT_COMPONENT,   portal.isScriptComponent()   ? YES : NO);
    	        currentStateMap.put(OFFERING_FILTER,    portal.isOfferingFilter()    ? YES : NO);
    	        currentStateMap.put(BOOKING_RULES,      portal.isBookingRules()      ? YES : NO);

    	        FieldConfigParameterSet customField = configParameterSet.getEvent().getParticipant().getCustomField();
    	        currentStateMap.put(PARTICIPANT_CUSTOM_FIELD, customField.isVisible() ? YES : NO);

    	        FieldConfigParameterSet simpleCustomField = configParameterSet.getEvent().getParticipant().getSimpleCustomField();
    	        currentStateMap.put(PARTICIPANT_SIMPLE_CUSTOM_FIELD, simpleCustomField.isVisible() ? YES : NO);

    	        ProfileConfigParameterSet profile = configParameterSet.getProfile();
    	        currentStateMap.put(PROFILE, profile.isVisible() ? YES : NO);

    	        CustomFieldConfigParameterSet profileCustomField = profile.getCustomField();
    	        currentStateMap.put(PROFILE_CUSTOM_FIELD, profileCustomField.isVisible() ? YES : NO);

    	        FieldConfigParameterSet profileRelation = profile.getProfileRelation();
    	        currentStateMap.put(PROFILE_RELATION, profile.isVisible() && profileRelation.isVisible() ? YES : NO);

    	        FieldConfigParameterSet badge = configParameterSet.getEvent().getParticipant().getBadge();
    	        currentStateMap.put(BADGE, badge.isVisible() ? YES : NO);

    	        FieldConfigParameterSet lead = configParameterSet.getEvent().getParticipant().getLead();
    	        currentStateMap.put(LEAD, lead.isVisible() ? YES : NO);

    	        FieldConfigParameterSet registerDate = configParameterSet.getEvent().getParticipant().getRegisterDate();
    	        currentStateMap.put(REGISTER_DATE, registerDate.isVisible() ? YES : NO);

    	        FieldConfigParameterSet certificatePrint = configParameterSet.getEvent().getParticipant().getCertificatePrint();
    	        currentStateMap.put(CERTIFICATE_PRINT, certificatePrint.isVisible() ? YES : NO);

    	        FieldConfigParameterSet programmeNoteTime = configParameterSet.getEvent().getParticipant().getProgrammeNoteTime();
    	        currentStateMap.put(PROGRAMME_NOTE_TIME, programmeNoteTime.isVisible() ? YES : NO);

    	        FieldConfigParameterSet hotelNoteTime = configParameterSet.getEvent().getParticipant().getHotelNoteTime();
    	        currentStateMap.put(HOTEL_NOTE_TIME, hotelNoteTime.isVisible() ? YES : NO);

    	        FieldConfigParameterSet location = configParameterSet.getEvent().getLocation();
    	        currentStateMap.put(LOCATION, location.isVisible() ? YES : NO);

    	        FieldConfigParameterSet onsiteWorkflow = configParameterSet.getEvent().getOnsiteWorkflow();
    	        currentStateMap.put(ONSITE_WORKFLOW, onsiteWorkflow.isVisible() ? YES : NO);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return currentStateMap;
	}


	@Override
	public String[] getProvidedSourceNames() {
		return SOURCE_NAMES;
	}


	private CacheModelListener modelListener = new CacheModelListener() {
    	@Override
    	public void dataChange(CacheModelEvent event) {
    		fireSourceChanged(ISources.WORKBENCH, getCurrentState());
    	}
	};


	private ISelectionListener selectionListener = new  ISelectionListener() {
    	@Override
    	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) {
    		List<Long> eventIDs = EventSelectionHelper.getEventIDs(selection);

    		if (eventIDs != null && !eventIDs.isEmpty()) {
    			Long newEventPK = eventIDs.get(0);
    			if (newEventPK != null) {
    				// Update if not showing an event, or showing a different event
    				if (ConfigSetSourceProvider.this.eventPK == null ||
    					! ConfigSetSourceProvider.this.eventPK.equals(newEventPK)
    				) {
    					ConfigSetSourceProvider.this.eventPK = newEventPK;

    					fireSourceChanged(ISources.WORKBENCH, getCurrentState());
    				}
    			}
    		}
    	}
	};


	private IPartListener2 partListener2 = new IPartListener2() {

    	@Override
    	public void partActivated(IWorkbenchPartReference part) {
    		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    		IWorkbenchPart activePart = workbenchWindow.getPartService().getActivePart();

    		if (activePart != null && activePart instanceof EventIdProvider) {
    			EventIdProvider eventProvider = (EventIdProvider) activePart;
    			Long newEventPK = eventProvider.getEventId();
    			if (newEventPK != null) {
    				Long previousEventPK = ConfigSetSourceProvider.this.eventPK;
    				// Update if not showing an event, or showing a different event
    				if (previousEventPK == null || ! previousEventPK.equals(newEventPK)) {
    					ConfigSetSourceProvider.this.eventPK = newEventPK;
    					fireSourceChanged(ISources.WORKBENCH, getCurrentState());
    				}
    			}
    		}
    	}

    	@Override
    	public void partVisible(IWorkbenchPartReference part) {
    	}

    	@Override
    	public void partOpened(IWorkbenchPartReference part) {
    	}

    	@Override
    	public void partInputChanged(IWorkbenchPartReference part) {
    	}

    	@Override
    	public void partHidden(IWorkbenchPartReference part) {
    	}

    	@Override
    	public void partDeactivated(IWorkbenchPartReference part) {
    	}

    	@Override
    	public void partClosed(IWorkbenchPartReference part) {
    	}

    	@Override
    	public void partBroughtToTop(IWorkbenchPartReference part) {
    	}

	};

}
