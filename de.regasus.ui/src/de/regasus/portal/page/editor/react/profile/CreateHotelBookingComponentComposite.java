package de.regasus.portal.page.editor.react.profile;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.react.profile.CreateHotelBookingComponent;
import de.regasus.portal.component.react.profile.CreateHotelBookingComponentButtonVisibility;
import de.regasus.portal.page.editor.PageWidgetBuilder;
import de.regasus.users.CurrentUserModel;

public class CreateHotelBookingComponentComposite extends EntityComposite<CreateHotelBookingComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean expertMode;

	private Portal portal;

	private List<Language> languageList;

	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;

	private Text hotelBookingUrlText;

	private Button buttonVisibilityAlways;
	private Button buttonVisibilityWithoutHotelBooking;
	private Button buttonVisibilityWithHotelBooking;

	private I18NComposite<CreateHotelBookingComponent> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************


	public CreateHotelBookingComponentComposite(Composite parent, int style, Long portalPK) throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		expertMode = CurrentUserModel.getInstance().isPortalExpert();

		// determine Portal languages
		Long portalPK = (Long) initValues[0];
		this.portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		widgetBuilder.buildTypeLabel( PortalI18N.CreateHotelBookingComponent.getString() );
		if (expertMode) {
			htmlIdText = widgetBuilder.buildHtmlId();
		}

		buildHotelBookingUrlText(parent);

		SWTHelper.verticalSpace(parent);
		buildButtonVisibility(parent);
		SWTHelper.verticalSpace(parent);

		buildI18NComposite(parent);
	}


	private void buildHotelBookingUrlText(Composite parent) throws Exception {
   		SWTHelper.createLabel(parent, CreateHotelBookingComponent.HOTEL_BOOKING_URL.getString());

   		hotelBookingUrlText = new Text(parent, SWT.BORDER);
   		GridDataFactory.fillDefaults().grab(true, false).applyTo(hotelBookingUrlText);
		hotelBookingUrlText.addModifyListener(modifySupport);
	}


	private void buildButtonVisibility(Composite parent) throws Exception {
		Group group = new Group(parent, SWT.NONE);
		GridDataFactory.fillDefaults().span(COL_COUNT, 1).grab(true, false).applyTo(group);
		group.setLayout( new RowLayout(SWT.VERTICAL) );
		group.setText( CreateHotelBookingComponent.BUTTON_VISIBILITY.getString() );

		buttonVisibilityAlways = new Button(group, SWT.RADIO);
		buttonVisibilityAlways.setText( CreateHotelBookingComponentButtonVisibility.ALWAYS.getString() );
		buttonVisibilityAlways.setToolTipText( CreateHotelBookingComponentButtonVisibility.ALWAYS.getDesription() );
		buttonVisibilityAlways.addSelectionListener(modifySupport);

		buttonVisibilityWithoutHotelBooking = new Button(group, SWT.RADIO);
		buttonVisibilityWithoutHotelBooking.setText( CreateHotelBookingComponentButtonVisibility.WITHOUT_HOTEL_BOOKING.getString() );
		buttonVisibilityWithoutHotelBooking.setToolTipText( CreateHotelBookingComponentButtonVisibility.WITHOUT_HOTEL_BOOKING.getDesription() );
		buttonVisibilityWithoutHotelBooking.addSelectionListener(modifySupport);

		buttonVisibilityWithHotelBooking = new Button(group, SWT.RADIO);
		buttonVisibilityWithHotelBooking.setText( CreateHotelBookingComponentButtonVisibility.WITH_HOTEL_BOOKING.getString() );
		buttonVisibilityWithHotelBooking.setToolTipText( CreateHotelBookingComponentButtonVisibility.WITH_HOTEL_BOOKING.getDesription() );
		buttonVisibilityWithHotelBooking.addSelectionListener(modifySupport);
	}


	private void buildI18NComposite(Composite parent) {
		i18nComposite = new I18NComposite<>(
			parent,
			SWT.BORDER,
			languageList,
			new CreateHotelBookingComponentCompositeI18NWidgetController()
		);

		GridDataFactory
			.fillDefaults()
			.grab(true, false)
			.span(COL_COUNT, 1)
			.applyTo(i18nComposite);

		i18nComposite.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (expertMode) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
		}

		hotelBookingUrlText.setText( avoidNull(entity.getHotelBookingUrl()) );
		setButtonVisibility( entity.getButtonVisibility() );
		i18nComposite.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (expertMode) {
				entity.setHtmlId( htmlIdText.getText() );
			}

			entity.setHotelBookingUrl( hotelBookingUrlText.getText() );

			entity.setButtonVisibility( getButtonVisibility() );

			i18nComposite.syncEntityToWidgets();
		}
	}


	private CreateHotelBookingComponentButtonVisibility getButtonVisibility() {
		CreateHotelBookingComponentButtonVisibility buttonVisibility = CreateHotelBookingComponentButtonVisibility.ALWAYS;
		if ( buttonVisibilityWithoutHotelBooking.getSelection() ) {
			buttonVisibility = CreateHotelBookingComponentButtonVisibility.WITHOUT_HOTEL_BOOKING;
		}
		else if ( buttonVisibilityWithHotelBooking.getSelection() ) {
			buttonVisibility = CreateHotelBookingComponentButtonVisibility.WITH_HOTEL_BOOKING;
		}

		return buttonVisibility;
	}


	private void setButtonVisibility(CreateHotelBookingComponentButtonVisibility buttonVisibility) {
		buttonVisibilityAlways.setSelection(buttonVisibility == CreateHotelBookingComponentButtonVisibility.ALWAYS);
		buttonVisibilityWithoutHotelBooking.setSelection(buttonVisibility == CreateHotelBookingComponentButtonVisibility.WITHOUT_HOTEL_BOOKING);
		buttonVisibilityWithHotelBooking.setSelection(buttonVisibility == CreateHotelBookingComponentButtonVisibility.WITH_HOTEL_BOOKING);
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (expertMode) {
    		htmlIdText.setEnabled(!fixedStructure);
		}
	}

}
