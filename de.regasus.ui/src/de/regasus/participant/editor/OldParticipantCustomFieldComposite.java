package de.regasus.participant.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;


/**
 * Container class for old / classical custom fields.
 * Though the class name ends with Composite, this class is not a {@link Composite} anymore.
 * Once upon a time it extended {@link Composite}, but then {@link ExpandBar} was introduced.
 * The class name was not changed to beware the history of the file.
 */
class OldParticipantCustomFieldComposite {

	private EventModel eventModel = EventModel.getInstance();

	private Participant participant;
	private Long eventPK = null;

	private Text[] customFields;

	private ModifySupport modifySupport;


	OldParticipantCustomFieldComposite(
		Composite parent,
		int style,
		Long eventPK
	) {

		try {
			this.eventPK = eventPK;
			EventVO eventVO = eventModel.getEventVO(eventPK);

			modifySupport = new ModifySupport(parent);

			/* set GridLayout with 2 columns of equal width
			 * The left colums shows old custom field names and values with odd numbers (1, 3, 5, ...).
			 * The right colums shows old custom field names and values with even numbers (2, 4, 6, ...).
			 */
			Group group = new Group(parent, SWT.NONE);
			group.setLayout(new GridLayout(2, true));
			group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


			// calc and set item text
			String name = null;
			LanguageString simpleCustomFieldsGroupName = eventVO.getSimpleCustomFieldsGroupName();
			if (simpleCustomFieldsGroupName != null) {
				name = simpleCustomFieldsGroupName.getString();
			}
			if (StringHelper.isEmpty(name)) {
				name = ContactLabel.SimpleCustomFields.getString();
			}
			group.setText(name);


			// determine custom field names
			String[] customFieldNames = eventVO.getCustomFieldNames();

			// Find maximum index of field name not null, to avoid creating unnecessary widgets.
			int maxCustomFieldIdx = -1;
			for (int i = 0; i < customFieldNames.length; i++) {
				String customFieldName = customFieldNames[i];
				if (customFieldName != null) {
					maxCustomFieldIdx = i;
				}
			}

			customFields = new Text[maxCustomFieldIdx + 1];

			Composite leftComposite = new Composite(group, SWT.NONE);
			leftComposite.setLayout(new GridLayout(2, false));
			leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			Composite rightComposite = new Composite(group, SWT.NONE);
			rightComposite.setLayout(new GridLayout(2, false));
			rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			for (int i = 0; i < customFields.length; i++) {
				String customFieldName = customFieldNames[i];
				if (customFieldName != null) {
					Label label = null;

					/* add custom fields with odd numbers to leftComposite, those with even numbers
					 * to rightComposite
					 */
					if (i % 2 == 0) {
						label = new Label(leftComposite, SWT.NONE);
						customFields[i] = new Text(leftComposite, SWT.BORDER);
					}
					else {
						label = new Label(rightComposite, SWT.NONE);
						customFields[i] = new Text(rightComposite, SWT.BORDER);
					}

					label.setText(customFieldName);
					label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
					customFields[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

					// set number of custom field as tool tip
					String customFieldToolTipText = ContactLabel.CustomField.getString() + " " + (i + 1);
					label.setToolTipText(customFieldToolTipText);
					customFields[i].setToolTipText(customFieldToolTipText);
				}
				else {
					// create placeholders
					if (i % 2 == 0) {
						new Label(leftComposite, SWT.NONE);
						new Label(leftComposite, SWT.NONE);
					}
					else {
						new Label(rightComposite, SWT.NONE);
						new Label(rightComposite, SWT.NONE);
					}
				}
			}

			for (Text text : customFields) {
				if (text != null) {
					text.addModifyListener(modifySupport);
				}
			}

			syncWidgetsToEntity();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public boolean setFocus() {
		boolean focusSet = false;

		if (customFields != null) {
			for (int i = 0; i < customFields.length; i++) {
				if (customFields[i] != null) {
					focusSet = customFields[i].setFocus();

					if (focusSet) {
						break;
					}
				}
			}
		}

		return focusSet;
	}


	public void setParticipant(Participant participant) {
		if ( ! eventPK.equals(participant.getEventId())) {
			throw new IllegalArgumentException("EventPK must not change");
		}

		this.participant = participant;

		syncWidgetsToEntity();
	}


	public Long getEventPK() {
		return eventPK;
	}


	private void syncWidgetsToEntity() {
		if (participant != null && customFields != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
	    				/* Set to true during synchronization of widgets to the entity to avoid that
	    				 * ModifyListeners are informed because the widgets fire a ModifyEvent.
	    				 */
						modifySupport.setEnabled(false);

						for (int i = 0; i < customFields.length; i++) {
							if (customFields[i] != null) {
								// Attention: The participants custom fields start with no 1
								String customFieldValue = participant.getCustomField(i + 1);
								customFields[i].setText(StringHelper.avoidNull(customFieldValue));
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
	}


	public void syncEntityToWidgets() {
		try {
			if (participant != null && customFields != null) {
				for (int i = 0; i < customFields.length; i++) {
					if (customFields[i] != null) {
						String text = customFields[i].getText();
						// Attention: The participants custom fields start with no 1
						participant.setCustomField(i + 1, text);
					}
				}
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

}
