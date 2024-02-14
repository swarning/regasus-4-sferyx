package de.regasus.portal.type.standard.hotel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.hotel.contingent.combo.HotelContingentTypeCombo;


public class GeneralSettingsGroup extends EntityGroup<StandardHotelPortalConfig> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private HotelContingentTypeCombo hotelContingentTypeCombo;

	// *
	// * Widgets
	// **************************************************************************


	public GeneralSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardHotelPortalI18N.GeneralSettingsGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.fieldMetadata(StandardHotelPortalConfig.HOTEL_CONTINGENT_TYPE).createLabel();
		hotelContingentTypeCombo = new HotelContingentTypeCombo(parent, SWT.NONE);
		hotelContingentTypeCombo.addModifyListener(modifySupport);

		widgetBuilder.fieldMetadata(StandardHotelPortalConfig.SHOW_START_PAGE).createCheckbox();
	}


	@Override
	protected void syncWidgetsToEntity() throws Exception {
		super.syncWidgetsToEntity();

		hotelContingentTypeCombo.setHotelContingentType( entity.getHotelContingentType() );
	}


	@Override
	public void syncEntityToWidgets() {
		super.syncEntityToWidgets();

		if (entity != null) {
			entity.setHotelContingentType( hotelContingentTypeCombo.getHotelContingentType() );
		}
	}

}
