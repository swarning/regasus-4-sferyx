package de.regasus.portal.type.standard.feedback;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.combo.PortalParticipantTypeCombo;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.ui.Activator;

public class StandardFeedbackPortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private Portal portal;
	private StandardFeedbackPortalConfig portalConfig;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	// start page settings
	private Button showStartPageButton;
	private Button startPageRequiresPasswordButton;

	// participant type settings
	private PortalParticipantTypeCombo defaultParticipantTypeCombo;

	// participant last name settings
	private Text defaultParticipantLastName;


	// *
	// * Widgets
	// **************************************************************************

	public StandardFeedbackPortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		this.portal = portal;

		portalConfig = (StandardFeedbackPortalConfig) portal.getPortalConfig();
	}


	@Override
	public void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		setLayout( new GridLayout(2, true) );

		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.grab(true,  false);

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		// start page settings
		{
    		Group group = new Group(this, SWT.NONE);
    		groupGridDataFactory.applyTo(group);
    		group.setText(StandardFeedbackPortalI18N.StartPageGroup);
    		group.setLayout( new GridLayout(1, false) );

    		showStartPageButton = new Button(group, SWT.CHECK);
    		showStartPageButton.setText(StandardFeedbackPortalI18N.ShowStartPage);
    		showStartPageButton.addSelectionListener(modifySupport);
    		showStartPageButton.addSelectionListener(refreshStateSelectionListener);


    		startPageRequiresPasswordButton = new Button(group, SWT.CHECK);
    		startPageRequiresPasswordButton.setText(StandardFeedbackPortalI18N.StartPageRequiresPassword);
    		startPageRequiresPasswordButton.addSelectionListener(modifySupport);
		}

		// default settings
		{
			Group group = new Group(this, SWT.NONE);
			groupGridDataFactory.applyTo(group);
			group.setText(StandardFeedbackPortalI18N.Default);
			group.setLayout( new GridLayout(2, false) );

			// Row 1
			Label defaultParticipantTypeLabel = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(defaultParticipantTypeLabel);
			defaultParticipantTypeLabel.setText( Participant.PARTICIPANT_TYPE.getString() );

			defaultParticipantTypeCombo = new PortalParticipantTypeCombo(group, SWT.NONE);
			widgetGridDataFactory.applyTo(defaultParticipantTypeCombo);
			defaultParticipantTypeCombo.addModifyListener(modifySupport);

			// Row 2
			Label defaultParticipantLastNameLabel = new Label(group, SWT.NONE);
			labelGridDataFactory.applyTo(defaultParticipantLastNameLabel);
			defaultParticipantLastNameLabel.setText( Person.LAST_NAME.getString() );
			defaultParticipantLastName = new Text(group, SWT.BORDER);
			widgetGridDataFactory.applyTo(defaultParticipantLastName);
			defaultParticipantLastName.addModifyListener(modifySupport);
		}
	}


	@Override
	public void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// start page settings
						showStartPageButton.setSelection( portalConfig.isShowStartPage() );
						startPageRequiresPasswordButton.setSelection( portalConfig.isStartPageRequiresPassword() );

						// participant type settings
						defaultParticipantTypeCombo.setPortalId( portal.getId() );
						defaultParticipantTypeCombo.setParticipantTypePK( portalConfig.getDefaultParticipantTypeId() );

						defaultParticipantLastName.setText( StringHelper.avoidNull(portalConfig.getDefaultParticipantLastName()) );

						refreshState();
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
		if (portalConfig != null) {
			// start page settings
			portalConfig.setShowStartPage( showStartPageButton.getSelection() );
			portalConfig.setStartPageRequiresPassword( startPageRequiresPasswordButton.getSelection() );

			// participant type settings
			portalConfig.setDefaultParticipantTypeId( defaultParticipantTypeCombo.getParticipantTypePK() );

			// participant last name
			portalConfig.setDefaultParticipantLastName( defaultParticipantLastName.getText() );
		}
	}


	private SelectionListener refreshStateSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			refreshState();
		}
	};


	private void refreshState() {
		startPageRequiresPasswordButton.setEnabled( showStartPageButton.getSelection() );
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
