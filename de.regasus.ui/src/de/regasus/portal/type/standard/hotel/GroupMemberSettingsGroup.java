package de.regasus.portal.type.standard.hotel;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.portal.Portal;

public class GroupMemberSettingsGroup extends EntityGroup<StandardHotelPortalConfig> {

	private final int COL_COUNT = 2;


	// **************************************************************************
	// * Widgets
	// *

	private NullableSpinner groupMemberCountSpinner;
	private NullableSpinner defaultAvailabeRoomsFilterSpinner;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public GroupMemberSettingsGroup(Composite parent, int style, Portal portal)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(
			parent,
			style,
			Objects.requireNonNull(portal)
		);

		setText(StandardHotelPortalI18N.GroupMemberSettingsGroup);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		// Row 1
		Label groupMemberCountLabel = new Label(parent, SWT.NONE);
		labelGridDataFactory.applyTo(groupMemberCountLabel);
		groupMemberCountLabel.setText( StandardHotelPortalConfig.GROUP_MEMBER_COUNT.getString() );

		groupMemberCountSpinner = new NullableSpinner(parent, SWT.NONE);
		WidgetSizer.setWidth(groupMemberCountSpinner);
		groupMemberCountSpinner.setMinimum(0);
		groupMemberCountSpinner.setMaximum(100);
		groupMemberCountSpinner.addModifyListener(e -> refreshState());
		groupMemberCountSpinner.addModifyListener(modifySupport);
		
		// Row 2
		Label defaultAvaialbeRoomsFilterLabel = new Label(parent, SWT.NONE);
		labelGridDataFactory.applyTo(defaultAvaialbeRoomsFilterLabel);
		defaultAvaialbeRoomsFilterLabel.setText( StandardHotelPortalConfig.DEFAULT_AVAILABLE_ROOMS_FILTER.getString() );
		
		defaultAvailabeRoomsFilterSpinner = new NullableSpinner(parent, SWT.NONE);
		WidgetSizer.setWidth(defaultAvailabeRoomsFilterSpinner);
		defaultAvailabeRoomsFilterSpinner.setMinimum(1);
		defaultAvailabeRoomsFilterSpinner.setMaximum(groupMemberCountSpinner.getValue());
		defaultAvailabeRoomsFilterSpinner.addModifyListener(e -> refreshState());
		defaultAvailabeRoomsFilterSpinner.addModifyListener(modifySupport);
	}
	
	
	private void refreshState() {
		// companionCountSpinner
		if (groupMemberCountSpinner.getValue() > 0) {
			defaultAvailabeRoomsFilterSpinner.setEnabled(true);
			if (defaultAvailabeRoomsFilterSpinner.getValueAsInteger() > groupMemberCountSpinner.getValueAsInteger()) {
				defaultAvailabeRoomsFilterSpinner.setValue(groupMemberCountSpinner.getValueAsInteger());
			}
			defaultAvailabeRoomsFilterSpinner.setMaximum(groupMemberCountSpinner.getValue());
		}
		else {
			defaultAvailabeRoomsFilterSpinner.setEnabled(false);
		}
	}


	@Override
	protected void syncWidgetsToEntity() throws Exception {
		groupMemberCountSpinner.setValue( entity.getGroupMemberCount() );
		defaultAvailabeRoomsFilterSpinner.setValue( entity.getDefaultAvailabeRoomsFilter() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setGroupMemberCount( groupMemberCountSpinner.getValueAsInteger() );
			entity.setDefaultAvailabeRoomsFilter( defaultAvailabeRoomsFilterSpinner.getValueAsInteger() );
		}
	}

}
