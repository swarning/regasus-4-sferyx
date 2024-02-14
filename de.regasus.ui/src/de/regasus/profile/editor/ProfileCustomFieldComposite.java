package de.regasus.profile.editor;

import static de.regasus.event.customfield.CustomFieldWidgetFactory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.contact.CustomFieldValue;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomField_Position_Comparator;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldValue;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.customfield.AbstractCustomFieldWidget;
import de.regasus.ui.Activator;

public class ProfileCustomFieldComposite extends LazyComposite {

	/**
	 * When an editor shows the custom fields of another profil, I want to show initially the same groups expanded as in
	 * the already opened editor. Since initially all groups are expanded, I store the event when a group get closed. I
	 * use this static variable to share this information between all such composites. This is not a memory leak, since
	 * there can be at most as many set elements as there are groups defined in the database
	 */
	static Set<Long> closedGroupIDList = new TreeSet<>();
	static Long SUBSTITUTE_ID_FOR_UNGROUPED_PROFILE_CUSTOM_FIELDS = -1L;
	static final String GROUP_ID_KEY = "GROUP_ID";

	// the entity
	private Profile profile;

	// List of ModifyListeners
	private ModifySupport modifySupport = new ModifySupport(this);

	/*
	 * Shows if the widgets are yet initialized. Is set to true at the end of createPartControl(). Avoids
	 * synchronization of the entity to the widgets (syncEntityToWidgets()) before the latter are initialized.
	 */
	private boolean widgetsInitialized = false;

	/*
	 * Widgets / Composites
	 *
	 * Structure of Composites:
	 *
	 * ParticipantCustomFieldComposite expandBar widgets
	 */
	private List<AbstractCustomFieldWidget> widgets;

	private Map<Long, AbstractCustomFieldWidget> customFieldPK2widgetMap = new HashMap<>();

	private Map<ProfileCustomFieldGroup, List<ProfileCustomField>> groupToCustomFieldsMap;


	public ProfileCustomFieldComposite(
		Composite parent, int style,
		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> groupToCustomFieldsMap
	) {
		super(parent, style);
		this.groupToCustomFieldsMap = groupToCustomFieldsMap;
	}


	/**
	 * Create the composite. It shows scroll bars when the space is not enough for all the custom fields.
	 */
	@Override
	protected void createPartControl() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);

		// if parent.layout == GridLayout: set LayoutData to GridData
		if (getParent().getLayout() instanceof GridLayout) {
			this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		try {
			this.widgets = createCustomFieldWidgets(this, groupToCustomFieldsMap);
			for (AbstractCustomFieldWidget widget : widgets) {
				widget.addModifyListener(modifySupport);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		widgetsInitialized = true;

		syncWidgetsToEntity();
	}


	private List<AbstractCustomFieldWidget> createCustomFieldWidgets(
		Composite parent,
		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> groupToCustomFieldsMap
	) {
		if (groupToCustomFieldsMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<AbstractCustomFieldWidget> allWidgets = new ArrayList<>();
		for (Entry<ProfileCustomFieldGroup, List<ProfileCustomField>> eachGroup : groupToCustomFieldsMap.entrySet()) {
			List<ProfileCustomField> fieldsOfGroup = eachGroup.getValue();

			if (!fieldsOfGroup.isEmpty()) {
				List<AbstractCustomFieldWidget> widgets = createWidgetsForFieldsPerGroup(parent, eachGroup.getKey(), fieldsOfGroup);
				allWidgets.addAll(widgets);
			}
		}
		return allWidgets;
	}


	private List<AbstractCustomFieldWidget> createWidgetsForFieldsPerGroup(
		Composite parent,
		ProfileCustomFieldGroup customFieldGroup,
		List<ProfileCustomField> customFields
	) {
		if (customFields.isEmpty()) {
			return Collections.emptyList();
		}

		List<ProfileCustomField> customFieldList = new ArrayList<>(customFields);
		Collections.sort(customFieldList, ProfileCustomField_Position_Comparator.getInstance());

		boolean makeColumnsEqualWidth = containsLongLabel(customFields);

		String groupLabelText = customFieldGroup.getName().toString();
		Group group = createGroupComposite(parent, makeColumnsEqualWidth, SWT.NONE, groupLabelText);

		List<AbstractCustomFieldWidget> widgets = new ArrayList<>();
		for (ProfileCustomField field : customFieldList) {
			AbstractCustomFieldWidget widget = createWithLabel(group, field);
			widget.addModifyListener(modifySupport);
			widgets.add(widget);
			customFieldPK2widgetMap.put(field.getID(), widget);
		}

		return widgets;
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

	/**
	 * Represent data from entity fields on the widgets
	 */
	private void syncWidgetsToEntity() {
		if (widgetsInitialized && !widgets.isEmpty() && profile != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						/*
						 * Set to false during synchronization of widgets to the entity to avoid that ModifyListeners
						 * are informed because the widgets fire a ModifyEvent.
						 */
						modifySupport.setEnabled(false);

						// Remove all custom field values, because some may have been deleted in the DB by someone els
						for (AbstractCustomFieldWidget widget : widgets) {
							widget.setCustomFieldValue(null);
						}

						// For all existing custom field values, put them in the widget
						List<ProfileCustomFieldValue> valueList = profile.getCustomFieldValues();
						if (valueList != null) {
    						for (ProfileCustomFieldValue pcFieldValue : valueList) {
    							Long customFieldPK = pcFieldValue.getCustomFieldPK();
    							AbstractCustomFieldWidget widget = customFieldPK2widgetMap.get(customFieldPK);
    							if (widget != null) {
    								widget.setCustomFieldValue(pcFieldValue);
    							}
    						}
						}

						// For all widgets, let them show (their possibly deleted) value
						for (AbstractCustomFieldWidget widget : widgets) {
							widget.syncWidgetToEntity();
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	/**
	 * Copy data that represent on the widgets to the entity fields
	 */
	public void syncEntityToWidgets() {
		if ( ! widgetsInitialized) {
			return;
		}

		if (profile == null) {
			return;
		}

		if (widgets.isEmpty()) {
			return;
		}

		try {
			List<Long> customFieldsThisCompositeIsResponsibleFor = CollectionsHelper.createArrayList(widgets.size());
			List<ProfileCustomFieldValue> values = new ArrayList<>(widgets.size());
			for (AbstractCustomFieldWidget widget : widgets) {
				widget.syncEntityToWidget();
				CustomFieldValue customFieldValue = widget.getCustomFieldValue();

				// We memorize which customFields are shown within this tab
				customFieldsThisCompositeIsResponsibleFor.add(customFieldValue.getCustomFieldPK());

				if (customFieldValue != null && !customFieldValue.isNull()) {
					ProfileCustomFieldValue profileCustomFieldValue = new ProfileCustomFieldValue(
						customFieldValue,
						profile.getPrimaryKey()
					);
					values.add(profileCustomFieldValue);
				}
			}

			// Instead of setting the found values as new list to the profile, we must combine
			// our list with the list of all other cfValues that OTHER tabs may be responsible for

			ArrayList<ProfileCustomFieldValue> cfValuesToBeSet = new ArrayList<>();

			// a) add all values that we are NOT responsible for, and
			List<ProfileCustomFieldValue> allPreviousCustomFieldValues = profile.getCustomFieldValues();
			if (allPreviousCustomFieldValues != null) {
    			for (ProfileCustomFieldValue cfValue : allPreviousCustomFieldValues) {
    				if (! customFieldsThisCompositeIsResponsibleFor.contains(cfValue.getCustomFieldPK())) {
    					cfValuesToBeSet.add(cfValue);
    				}
    			}
			}
			// b) add all our found values
			cfValuesToBeSet.addAll(values);

			// Finally adding the union of our (new) values and the other ones we are not responsible for
			profile.setCustomFieldValues(cfValuesToBeSet);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Profile getProfile() {
		return profile;
	}


	public void setProfile(Profile profile) {
		this.profile = profile;

		syncWidgetsToEntity();
	}


	/**
	 * I don't register myself with models, so nothing to do here
	 */
	public void widgetDisposed(DisposeEvent e) {
	}

}
