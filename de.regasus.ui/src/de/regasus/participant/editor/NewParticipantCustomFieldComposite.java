package de.regasus.participant.editor;

import static de.regasus.event.customfield.CustomFieldWidgetFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.contact.CustomFieldValue;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValue;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField_Position_Comparator;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.customfield.AbstractCustomFieldWidget;
import de.regasus.ui.Activator;


/**
 * Container class for (new) Participant Custom Fields.
 * Though the class name ends with Composite, this class is not a {@link Composite} anymore.
 * The class name was not changed to beware the history of the file.
 */
class NewParticipantCustomFieldComposite {

	private Participant participant;
	private Long eventPK;

	private List<AbstractCustomFieldWidget> widgets = new ArrayList<>();

	private Map<Long, AbstractCustomFieldWidget> customFieldPK2widgetMap = new HashMap<>();

	private ModifySupport modifySupport;

	NewParticipantCustomFieldComposite(
		final Composite parent,
		int style,
		Long eventPK,
		Map<ParticipantCustomFieldGroup,
		List<ParticipantCustomField>> groupToFieldsMap
	) {

		try {
    		this.eventPK = eventPK;
    		this.modifySupport = new ModifySupport(parent);

			for (ParticipantCustomFieldGroup group : groupToFieldsMap.keySet()) {
				List<ParticipantCustomField> customFields = groupToFieldsMap.get(group);

				boolean makeColumnsEqualWidth = containsLongLabel(customFields);

				String groupLabel = group.getName().toString();
				Group groupWidget = createGroupComposite(parent, makeColumnsEqualWidth, style, groupLabel);

				List<AbstractCustomFieldWidget> widgets = createFieldWidgets(groupWidget, customFields);
				this.widgets.addAll(widgets);
			}

			for (AbstractCustomFieldWidget widget : widgets) {
				widget.addModifyListener(modifySupport);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private List<AbstractCustomFieldWidget> createFieldWidgets(
		Composite parent,
		List<ParticipantCustomField> customFields
	) {
		List<ParticipantCustomField> customFieldList = new ArrayList<>(customFields);
		Collections.sort(customFieldList, ParticipantCustomField_Position_Comparator.getInstance());

		List<AbstractCustomFieldWidget> widgets = new ArrayList<>();
		for (ParticipantCustomField field : customFieldList) {
			AbstractCustomFieldWidget widget = createWithLabel(parent, field);
			widgets.add(widget);
			customFieldPK2widgetMap.put(field.getID(), widget);
		}
		return Collections.unmodifiableList(widgets);
	}


	public void setParticipant(Participant participant) {
		if ( ! eventPK.equals(participant.getEventId())) {
			throw new IllegalArgumentException("EventPK must not change");
		}

		this.participant = participant;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
    	SWTHelper.syncExecDisplayThread(new Runnable() {
    		@Override
			public void run() {
    			try {
    				/* Set to true during synchronization of widgets to the entity to avoid that
    				 * ModifyListeners are informed because the widgets fire a ModifyEvent.
    				 */
    				modifySupport.setEnabled(false);

    				Collection<ParticipantCustomFieldValue> valueList = participant.getCustomFieldValues();

    				/* List of all customFieldPKs where a widget exist.
    				 * We remove those customFieldPKs where a value exists to determine the
    				 * widgets where no value exists to delete their content, too.
    				 *
    				 * Do not operate on the keySet directly because removing keys from the keySet
    				 * would remove map entries!
    				 */
    				List<Long> customFieldPKs = new ArrayList<>(customFieldPK2widgetMap.keySet());

    				for (ParticipantCustomFieldValue pcFieldValue : valueList) {
    					Long customFieldPK = pcFieldValue.getCustomFieldPK();

    					// remove customFieldPK for all custom fields where a value exists
    					customFieldPKs.remove(customFieldPK);

    					AbstractCustomFieldWidget widget = customFieldPK2widgetMap.get(customFieldPK);
    					if (widget != null) {
    						widget.setCustomFieldValue(pcFieldValue);
    						widget.syncWidgetToEntity();
    					}
    				}

    				/* customFieldPKs contains the PKs of the custom fields where a widget exists
    				 * but no value.
    				 *
    				 * Remove the values from all widgets where no custom field value exists.
    				 */
    				for (Long customFieldPK : customFieldPKs) {
    					AbstractCustomFieldWidget widget = customFieldPK2widgetMap.get(customFieldPK);
    					if (widget != null) {
    						widget.setCustomFieldValue(null);
    						widget.syncWidgetToEntity();
    					}
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


	public void syncEntityToWidgets() {
		try {
			if (participant != null && widgets != null) {

				List<Long> customFieldsThisCompositeIsResponsibleFor = CollectionsHelper.createArrayList(widgets.size());
    			List<ParticipantCustomFieldValue> foundValues = CollectionsHelper.createArrayList(widgets.size());
    			for (AbstractCustomFieldWidget widget : widgets) {
    				widget.syncEntityToWidget();
    				CustomFieldValue customFieldValue = widget.getCustomFieldValue();

    				// We memorize which customFields are shown within this tab
    				customFieldsThisCompositeIsResponsibleFor.add(customFieldValue.getCustomFieldPK());

    				// We collect all cfValues from this tab (maybe less than widget count for null values)
    				if (customFieldValue != null && !customFieldValue.isNull()) {
    					ParticipantCustomFieldValue participantCustomFieldValue = new ParticipantCustomFieldValue(
    						customFieldValue,
    						participant.getPrimaryKey()
    					);
						foundValues.add(participantCustomFieldValue);
    				}
    			}

    			// MIRCP-2343: Instead of setting the found values as new list to the participant, we must combine
    			// our list with the list of all other cfValues that OTHER tabs may be responsible for
    			List<ParticipantCustomFieldValue> allPreviousCustomFieldValues = participant.getCustomFieldValues();

    			// We start with an empty list and
    			ArrayList<ParticipantCustomFieldValue> cfValuesToBeSet = new ArrayList<>();

    			// a) add all values that we are NOT responsible for, and
    			for (ParticipantCustomFieldValue cfValue : allPreviousCustomFieldValues) {
    				if (! customFieldsThisCompositeIsResponsibleFor.contains(cfValue.getCustomFieldPK())) {
    					cfValuesToBeSet.add(cfValue);
    				}
				}
    			// b) add all our found values
    			cfValuesToBeSet.addAll(foundValues);

    			// Finally adding the union of our (new) values and the other ones we are not responsible for
    			participant.setCustomFieldValues(cfValuesToBeSet);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
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


	public boolean setFocus() {
		boolean focusSet = false;

		for (AbstractCustomFieldWidget widget : widgets) {
			focusSet = widget.setFocus();

			if (focusSet) {
				break;
			}
		}

		return focusSet;
	}

}
