package de.regasus.portal.type.standard.group;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;


public class StartPageSettingsGroup extends EntityGroup<StandardGroupPortalConfig> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button showStartPageButton;

	// *
	// * Widgets
	// **************************************************************************


	public StartPageSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardGroupPortalI18N.StartPageGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		showStartPageButton = new Button(parent, SWT.CHECK);
		showStartPageButton.setText( StandardGroupPortalConfig.SHOW_START_PAGE.getString() );
		showStartPageButton.addSelectionListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		showStartPageButton.setSelection( entity.isShowStartPage() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
    		entity.setShowStartPage( showStartPageButton.getSelection() );
		}
	}

}
