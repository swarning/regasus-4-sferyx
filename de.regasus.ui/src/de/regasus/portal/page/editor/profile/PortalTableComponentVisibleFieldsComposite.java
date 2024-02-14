package de.regasus.portal.page.editor.profile;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityComposite;

import de.regasus.portal.component.profile.PortalTableComponent;

public class PortalTableComponentVisibleFieldsComposite extends EntityComposite<PortalTableComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button showPortalNameButton;
	private Button showPortalOnlineBeginButton;
	private Button showPortalOnlineEndButton;

	private Button showEventMnemonicButton;
	private Button showEventNameButton;
	private Button showEventBeginButton;
	private Button showEventEndButton;

	private Button showRegistrationStatusButton;

	// *
	// * Widgets
	// **************************************************************************


	public PortalTableComponentVisibleFieldsComposite(Composite parent, int style)
	throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		final int COL_COUNT = 4;
		setLayout( new GridLayout(COL_COUNT, false) );

		// row 1
		showPortalNameButton = createButton(parent, PortalTableComponent.SHOW_PORTAL_NAME.getLabel() );
		showPortalOnlineBeginButton = createButton(parent, PortalTableComponent.SHOW_PORTAL_ONLINE_BEGIN.getLabel() );
		showPortalOnlineEndButton = createButton(parent, PortalTableComponent.SHOW_PORTAL_ONLINE_END.getLabel() );
		new Label(this, SWT.NONE);

		// row 2
		showEventMnemonicButton = createButton(parent, PortalTableComponent.SHOW_EVENT_MNEMONIC.getLabel() );
		showEventNameButton = createButton(parent, PortalTableComponent.SHOW_EVENT_NAME.getLabel() );
		showEventBeginButton = createButton(parent, PortalTableComponent.SHOW_EVENT_BEGIN.getLabel() );
		showEventEndButton = createButton(parent, PortalTableComponent.SHOW_EVENT_END.getLabel() );

		// row 3
		showRegistrationStatusButton = createButton(parent, PortalTableComponent.SHOW_REGISTRATION_STATUS.getLabel() );
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
	}


	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(button);
		button.addSelectionListener(modifySupport);
		return button;
	}


	@Override
	protected void syncWidgetsToEntity() {
		showPortalNameButton.setSelection( entity.isShowPortalName() );
		showPortalOnlineBeginButton.setSelection( entity.isShowPortalOnlineBegin() );
		showPortalOnlineEndButton.setSelection( entity.isShowPortalOnlineEnd() );

		showEventMnemonicButton.setSelection( entity.isShowEventMnemonic() );
		showEventNameButton.setSelection( entity.isShowEventName() );
		showEventBeginButton.setSelection( entity.isShowEventBegin() );
		showEventEndButton.setSelection( entity.isShowEventEnd() );

		showRegistrationStatusButton.setSelection( entity.isShowRegistrationStatus() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setShowPortalName( showPortalNameButton.getSelection() );
			entity.setShowPortalOnlineBegin( showPortalOnlineBeginButton.getSelection() );
			entity.setShowPortalOnlineEnd( showPortalOnlineEndButton.getSelection() );

			entity.setShowEventMnemonic( showEventMnemonicButton.getSelection() );
			entity.setShowEventName( showEventNameButton.getSelection() );
			entity.setShowEventBegin( showEventBeginButton.getSelection() );
			entity.setShowEventEnd( showEventEndButton.getSelection() );

			entity.setShowRegistrationStatus( showRegistrationStatusButton.getSelection() );
		}
	}

}
