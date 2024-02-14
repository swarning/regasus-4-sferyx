package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.portal.component.ProgrammeBookingComponent;


public class ProgrammeBookingComponentOfferingFilterComposite extends EntityComposite<ProgrammeBookingComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	// **************************************************************************
	// * Widgets
	// *

	private Text offeringFilterDescriptionText;
	private Text offeringFilterText;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammeBookingComponentOfferingFilterComposite(Composite parent, int style)
	throws Exception {
		super(parent, style);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		buildOfferingFilter(parent);
	}


	private void buildOfferingFilter(Composite parent) {
		SWTHelper.createTopLabel(parent, ProgrammeBookingComponent.FIELD_OFFERING_FILTER_DESCRIPTION.getString());

		offeringFilterDescriptionText = new MultiLineText(parent, SWT.BORDER, true);
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT - 1, 1).applyTo(offeringFilterDescriptionText);
		offeringFilterDescriptionText.addModifyListener(modifySupport);

		SWTHelper.createTopLabel(parent, ProgrammeBookingComponent.FIELD_OFFERING_FILTER.getString());

		offeringFilterText = new MultiLineText(parent, SWT.BORDER, true);
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT - 1, 1).applyTo(offeringFilterText);
		offeringFilterText.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		offeringFilterDescriptionText.setText( avoidNull(entity.getOfferingFilterDescription()) );
		offeringFilterText.setText( avoidNull(entity.getOfferingFilter()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setOfferingFilterDescription( offeringFilterDescriptionText.getText() );
			entity.setOfferingFilter( offeringFilterText.getText() );
		}
	}

}
