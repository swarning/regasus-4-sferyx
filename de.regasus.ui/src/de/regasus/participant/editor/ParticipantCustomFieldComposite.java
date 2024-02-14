package de.regasus.participant.editor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;

class ParticipantCustomFieldComposite extends LazyComposite {

	/**
	 * When an editor shows the custom fields of another participant of the same event,
	 * I want to show initially the same groups expanded as in the already opened editor.
	 * Since initially all groups are expanded, I store the event when a group get closed.
	 * I use this static variable to share this information between all such composites.
	 * This is not a memory leak, since there can be at most as many set elements as there are
	 * groups defined in the database
	 */
	static Set<Long> closedGroupIDList = new TreeSet<>();
	static Long SUBSTITUTE_ID_FOR_CLASSICAL_CUSTOM_FIELDS = -2L;
	static final String GROUP_ID_KEY = "GROUP_ID";

	// the entity
	private Participant participant;

	// eventPK of participant (must not change)
	private Long eventPK;

	// List of ModifyListeners
	private ModifySupport modifySupport = new ModifySupport(this);

	/* Shows if the widgets are yet initialized.
	 * Is set to true at the end of createPartControl().
	 * Avoids synchronization of the entity to the widgets (syncEntityToWidgets()) before the latter
	 * are initialized.
	 */
	private boolean widgetsInitialized = false;


	private NewParticipantCustomFieldComposite newCustomFieldComposite;
	private OldParticipantCustomFieldComposite oldCustomFieldComposite;

	// Fields that reflect the configuration
	private boolean showNewCustomFields;
	private boolean showOldCustomFields;

	private Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> groupToFieldsMap;

	ParticipantCustomFieldComposite(
		Composite parent,
		int style,
		Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> groupToFieldsMap,
		boolean showNewCustomFields,
		boolean showOldCustomFields
	) {
		super(parent, style);

		this.groupToFieldsMap = groupToFieldsMap;
		this.showNewCustomFields = showNewCustomFields;
		this.showOldCustomFields = showOldCustomFields;
	}


	@Override
	protected void createPartControl() {
		Objects.requireNonNull(eventPK);

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);

		// if parent.layout == GridLayout: set LayoutData to GridData
		if (getParent().getLayout() instanceof GridLayout) {
			this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		if (showNewCustomFields) {
			newCustomFieldComposite = new NewParticipantCustomFieldComposite(
				this,
				SWT.NONE,
				eventPK,
				groupToFieldsMap
			);
			newCustomFieldComposite.setParticipant(participant);
			newCustomFieldComposite.addModifyListener(modifySupport);
		}


		if (showOldCustomFields) {
			oldCustomFieldComposite = new OldParticipantCustomFieldComposite(
				this,
				SWT.NONE,
				eventPK
			);
			oldCustomFieldComposite.setParticipant(participant);
			oldCustomFieldComposite.addModifyListener(modifySupport);
		}

		widgetsInitialized = true;
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


	public void syncEntityToWidgets() {
		if (!widgetsInitialized) {
			return;
		}

		if (participant == null) {
			return;
		}

		if (newCustomFieldComposite != null) {
			newCustomFieldComposite.syncEntityToWidgets();
		}

		if (oldCustomFieldComposite != null) {
			oldCustomFieldComposite.syncEntityToWidgets();
		}
	}


	public void setParticipant(Participant participant) throws Exception {
		Objects.requireNonNull(participant);
		Objects.requireNonNull(participant.getEventId());

		if (eventPK == null) {
			eventPK = participant.getEventId();
		}
		else if ( ! eventPK.equals(participant.getEventId())) {
			throw new IllegalArgumentException("eventPK must not change");
		}

		this.participant = participant;

		if (newCustomFieldComposite != null) {
			newCustomFieldComposite.setParticipant(participant);
		}

		if (oldCustomFieldComposite != null) {
			oldCustomFieldComposite.setParticipant(participant);
		}
	}

}
