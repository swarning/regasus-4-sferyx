package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.ScriptComponent;
import de.regasus.users.CurrentUserModel;

public class ScriptComponentComposite extends EntityComposite<ScriptComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean advancedAccess;

	private Portal portal;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;

	private Text nameText;
	private MultiLineText descriptionText;
	private Button runOnRenderButton;
	private Button runOnExitButton;
	private MultiLineText scriptText;

	// *
	// * Widgets
	// **************************************************************************


	public ScriptComponentComposite(Composite parent, int style, Long portalPK)
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

		// load Portal to get Event and Languages
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);


		/* Determine if the user has advanced access to the ScriptComponent
		 * The condition is: isAdmin || (expertMode && isScriptComponentAvailable)
		 * To improve performance, we evaluate the ConfigParameterSet at the end and only if necessary.
		 */
		advancedAccess = CurrentUserModel.getInstance().isAdmin();
		if (!advancedAccess) {
			advancedAccess =
				   CurrentUserModel.getInstance().isPortalExpert()
				&& isScriptComponentAvailable(portal);
		}
	}


	private boolean isScriptComponentAvailable(Portal portal) throws Exception {
		Long eventId = portal.getEventId();

		boolean scriptComponentAvailable = false;
		ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventId);
		if (eventId == null) {
			scriptComponentAvailable = configParameterSet.getPortal().isScriptComponent();
		}
		else {
			scriptComponentAvailable = configParameterSet.getEvent().getPortal().isScriptComponent();
		}

		return scriptComponentAvailable;
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.ScriptComponent.getString() );

		if (advancedAccess) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

//		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(COL_COUNT, 1).applyTo(sashForm);

		buildName(parent);
		buildDescription(parent);
		buildRunOnRenderButton(parent);
		buildRunOnExitButton(parent);
		buildScript(parent);
	}


	private void buildName(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		SWTHelper.makeBold(label);
		label.setText( ScriptComponent.NAME.getString() );


		nameText = new Text(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(nameText);
		SWTHelper.makeBold(nameText);
		nameText.setEnabled(advancedAccess);
		nameText.addModifyListener(modifySupport);
	}


	private void buildDescription(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText( ScriptComponent.DESCRIPTION.getString() );


		descriptionText = new MultiLineText(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(descriptionText);
		SWTHelper.enableTextWidget(descriptionText, advancedAccess);
		descriptionText.addModifyListener(modifySupport);
	}


	private void buildRunOnRenderButton(Composite parent) {
		if (advancedAccess) {
			new Label(parent, SWT.NONE);

    		runOnRenderButton = new Button(parent, SWT.CHECK);
    		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(runOnRenderButton);
    		runOnRenderButton.setText( ScriptComponent.RUN_ON_RENDER.getString() );
    		runOnRenderButton.addSelectionListener(modifySupport);
		}
	}


	private void buildRunOnExitButton(Composite parent) {
		if (advancedAccess) {
			new Label(parent, SWT.NONE);

    		runOnExitButton = new Button(parent, SWT.CHECK);
    		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(runOnExitButton);
    		runOnExitButton.setText( ScriptComponent.RUN_ON_EXIT.getString() );
    		runOnExitButton.addSelectionListener(modifySupport);
		}
	}


	private void buildScript(Composite parent) {
		if (advancedAccess) {
    		Label label = new Label(parent, SWT.NONE);
    		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
    		label .setText( ScriptComponent.SCRIPT.getString() );


    		scriptText = new MultiLineText(parent, SWT.BORDER);
    		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(scriptText);
    		scriptText.addModifyListener(modifySupport);
		}
	}



	@Override
	protected void syncWidgetsToEntity() {
		if (advancedAccess) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
		}

		nameText.setText( avoidNull(entity.getName()) );
		descriptionText.setText( avoidNull(entity.getDescription()) );

		if (advancedAccess) {
    		runOnRenderButton.setSelection( entity.isRunOnRender() );
    		runOnExitButton.setSelection( entity.isRunOnExit() );
    		scriptText.setText( avoidNull(entity.getScript()) );
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (advancedAccess) {
				entity.setHtmlId( htmlIdText.getText() );
			}

			entity.setName( nameText.getText() );
			entity.setDescription( descriptionText.getText() );

			if (advancedAccess) {
    			entity.setRunOnRender( runOnRenderButton.getSelection() );
    			entity.setRunOnExit( runOnExitButton.getSelection() );
    			entity.setScript( scriptText.getText() );
			}
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (advancedAccess) {
			htmlIdText.setEnabled(!fixedStructure);
		}
	}

}
