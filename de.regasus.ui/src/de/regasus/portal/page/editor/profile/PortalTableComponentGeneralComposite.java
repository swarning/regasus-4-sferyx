package de.regasus.portal.page.editor.profile;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;

import de.regasus.I18N;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.profile.PortalTableComponent;
import de.regasus.portal.page.editor.ConditionGroup;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class PortalTableComponentGeneralComposite extends EntityComposite<PortalTableComponent> {

	private static final int COL_COUNT = 3;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean expertMode;

	private Portal portal;


	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;

	private Button showPortalWithRegistrationButton;
	private Button showPortalWithoutRegistrationButton;

	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public PortalTableComponentGeneralComposite(
		Composite parent,
		int style,
		Long portalPK
	)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Long portalPK = (Long) initValues[0];

		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// load Portal
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildShowPortalWithRegistrationButton(parent);
		buildShowPortalWithoutRegistrationButton(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	private void buildShowPortalWithRegistrationButton(Composite parent) throws Exception {
   		new Label(parent, SWT.NONE);

   		showPortalWithRegistrationButton = new Button(parent, SWT.CHECK);
   		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(showPortalWithRegistrationButton);
   		showPortalWithRegistrationButton.setText( PortalTableComponent.SHOW_PORTAL_WITH_REGISTRATION.getLabel() );
   		showPortalWithRegistrationButton.addSelectionListener(modifySupport);

   		new Label(parent, SWT.NONE);
	}


	private void buildShowPortalWithoutRegistrationButton(Composite parent) throws Exception {
		new Label(parent, SWT.NONE);

		showPortalWithoutRegistrationButton = new Button(parent, SWT.CHECK);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(showPortalWithoutRegistrationButton);
		showPortalWithoutRegistrationButton.setText( PortalTableComponent.SHOW_PORTAL_WITHOUT_REGISTRATION.getLabel() );
		showPortalWithoutRegistrationButton.addSelectionListener(modifySupport);

   		new Label(parent, SWT.NONE);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		showPortalWithRegistrationButton.setSelection( entity.isShowPortalWithRegistration() );
		showPortalWithoutRegistrationButton.setSelection( entity.isShowPortalWithoutRegistration() );

		visibleConditionGroup.setCondition( entity.getVisibleCondition() );
		visibleConditionGroup.setDescription( entity.getVisibleConditionDescription() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
				entity.setRender( renderText.getText() );
			}

			entity.setShowPortalWithRegistration( showPortalWithRegistrationButton.getSelection() );
			entity.setShowPortalWithoutRegistration( showPortalWithoutRegistrationButton.getSelection() );

			entity.setVisibleCondition( visibleConditionGroup.getCondition() );
			entity.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
			htmlIdText.setEnabled(!fixedStructure);
			renderText.setEnabled(!fixedStructure);
		}
	}


}
