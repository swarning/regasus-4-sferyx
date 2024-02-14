package de.regasus.portal.page.editor;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.users.CurrentUserModel;


public class ProgrammeBookingComponentComposite extends EntityComposite<ProgrammeBookingComponent> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean advancedAccessToOfferingFilter;
	private boolean advancedAccessToBookingRules;

	private Portal portal;

	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private ProgrammeBookingComponentGeneralComposite generalComposite;
	private ProgrammeBookingComponentOfferingFilterComposite offeringFilterComposite;
	private ProgrammeBookingComponentBookingCountComposite bookingCountComposite;
	private ProgrammeBookingComponentBookingRulesComposite bookingRulesComposite;
	private ProgrammeBookingComponentVisibleFieldsComposite visibleFieldsComposite;
	private ProgrammeBookingComponentContentComposite contentComposite;
	private ProgrammeBookingComponentLabelsComposite columnNameComposite;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammeBookingComponentComposite(Composite parent, int style, Long portalPK)
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

		// load Portal
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);

		/* Determine if the user has advanced access to the offering filter
		 * The condition is: isAdmin || (expertMode && isOfferingFilterAvailable)
		 * To improve performance, we evaluate the ConfigParameterSet at the end and only if necessary.
		 */
		advancedAccessToOfferingFilter = CurrentUserModel.getInstance().isAdmin();
		if (!advancedAccessToOfferingFilter) {
			advancedAccessToOfferingFilter =
				   CurrentUserModel.getInstance().isPortalExpert()
				&& isOfferingFilterAvailable(portal);
		}

		/* Determine if the user has advanced access to the booking rules
		 * The condition is: isAdmin || (expertMode && isBookingRulesAvailable)
		 * To improve performance, we evaluate the ConfigParameterSet at the end and only if necessary.
		 */
		advancedAccessToBookingRules = CurrentUserModel.getInstance().isAdmin();
		if (!advancedAccessToBookingRules) {
			advancedAccessToBookingRules =
				   CurrentUserModel.getInstance().isPortalExpert()
				&& isBookingRulesAvailable(portal);
		}
	}


	private boolean isOfferingFilterAvailable(Portal portal) throws Exception {
		Long eventId = portal.getEventId();

		boolean offeringFilterAvailable = false;
		ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventId);
		if (eventId == null) {
			offeringFilterAvailable = configParameterSet.getPortal().isOfferingFilter();
		}
		else {
			offeringFilterAvailable = configParameterSet.getEvent().getPortal().isOfferingFilter();
		}

		return offeringFilterAvailable;
	}


	private boolean isBookingRulesAvailable(Portal portal) throws Exception {
		Long eventId = portal.getEventId();

		boolean bookingRulesAvailable = false;
		ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventId);
		if (eventId == null) {
			bookingRulesAvailable = configParameterSet.getPortal().isBookingRules();
		}
		else {
			bookingRulesAvailable = configParameterSet.getEvent().getPortal().isBookingRules();
		}

		return bookingRulesAvailable;
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.ProgrammeBookingComponent.getString() );

		// tabFolder
		tabFolder = new TabFolder(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);

		// General Tab
		{
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(UtilI18N.General);
    		generalComposite = new ProgrammeBookingComponentGeneralComposite(tabFolder, SWT.NONE, portal.getId());
    		tabItem.setControl(generalComposite);
    		generalComposite.addModifyListener(modifySupport);
		}

		// Offering Filter Tab
		if (advancedAccessToOfferingFilter) {
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText( ProgrammeBookingComponent.FIELD_OFFERING_FILTER.getString() );
    		offeringFilterComposite = new ProgrammeBookingComponentOfferingFilterComposite(tabFolder, SWT.NONE);
    		tabItem.setControl(offeringFilterComposite);
    		offeringFilterComposite.addModifyListener(modifySupport);
		}

		// Booking Count Tab
		{
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText(I18N.ProgrammeBookingComponentComposite_BookingCount);
    		bookingCountComposite = new ProgrammeBookingComponentBookingCountComposite(tabFolder, SWT.NONE, portal.getId());
    		tabItem.setControl(bookingCountComposite);
    		bookingCountComposite.addModifyListener(modifySupport);
		}

		// Booking Rules Tab
		if (advancedAccessToBookingRules) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText( ProgrammeBookingComponent.FIELD_BOOKING_RULES.getString() );
			bookingRulesComposite = new ProgrammeBookingComponentBookingRulesComposite(tabFolder, SWT.NONE, portal.getId());
			tabItem.setControl(bookingRulesComposite);
			bookingRulesComposite.addModifyListener(modifySupport);
		}

		// Visible Fields Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_AvailableItems);
			visibleFieldsComposite = new ProgrammeBookingComponentVisibleFieldsComposite(tabFolder, SWT.NONE);
			tabItem.setControl(visibleFieldsComposite);
			visibleFieldsComposite.addModifyListener(modifySupport);
		}

		// Content Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_FieldContent);
			contentComposite = new ProgrammeBookingComponentContentComposite(tabFolder, SWT.NONE);
			tabItem.setControl(contentComposite);
			contentComposite.addModifyListener(modifySupport);
		}

		// Labels Tab
		{
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(I18N.PageEditor_Labels);
			columnNameComposite = new ProgrammeBookingComponentLabelsComposite(tabFolder, SWT.NONE, portal.getId());
			tabItem.setControl(columnNameComposite);
			columnNameComposite.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		generalComposite.setEntity(entity);
		if (offeringFilterComposite != null) {
			offeringFilterComposite.setEntity(entity);
		}
		bookingCountComposite.setEntity(entity);
		if (bookingRulesComposite != null) {
			bookingRulesComposite.setEntity(entity);
		}
		visibleFieldsComposite.setEntity(entity);
		contentComposite.setEntity(entity);
		columnNameComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		generalComposite.syncEntityToWidgets();
		if (offeringFilterComposite != null) {
			offeringFilterComposite.syncEntityToWidgets();
		}
		bookingCountComposite.syncEntityToWidgets();
		if (bookingRulesComposite != null) {
			bookingRulesComposite.syncEntityToWidgets();
		}
		visibleFieldsComposite.syncEntityToWidgets();
		contentComposite.syncEntityToWidgets();
		columnNameComposite.syncEntityToWidgets();
	}


	public void setFixedStructure(boolean fixedStructure) {
		generalComposite.setFixedStructure(fixedStructure);
	}

}
