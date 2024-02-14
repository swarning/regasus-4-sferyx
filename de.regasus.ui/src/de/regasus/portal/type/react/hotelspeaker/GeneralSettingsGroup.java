package de.regasus.portal.type.react.hotelspeaker;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.type.react.hotelspeaker.ReactHotelSpeakerPortalConfig;



public class GeneralSettingsGroup extends EntityGroup<ReactHotelSpeakerPortalConfig> {

	private final int COL_COUNT = 2;

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


	public GeneralSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(ReactSpeakerHotelPortalI18N.GeneralSettingsGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );


		showStartPageButton = widgetBuilder.fieldMetadata(ReactHotelSpeakerPortalConfig.SHOW_START_PAGE).createCheckbox();

		showStartPageButton.addSelectionListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() throws Exception {
		super.syncWidgetsToEntity();

		showStartPageButton.setSelection( entity.isShowStartPage() );
	}


	@Override
	public void syncEntityToWidgets() {
		super.syncEntityToWidgets();

		if (entity != null) {
    		entity.setShowStartPage( showStartPageButton.getSelection() );
		}
	}

}
