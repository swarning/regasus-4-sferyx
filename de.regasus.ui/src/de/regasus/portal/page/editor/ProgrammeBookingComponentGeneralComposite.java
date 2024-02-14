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

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.users.CurrentUserModel;


public class ProgrammeBookingComponentGeneralComposite extends EntityComposite<ProgrammeBookingComponent> {

	private static final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private boolean portalExpert;

	private Portal portal;


	// **************************************************************************
	// * Widgets
	// *

	private Text htmlIdText;
	private Text renderText;
	private CurrencyCombo currencyCombo;

	private ProgrammePointListComposite ppListComposite;

	private Button onlyOnlineAvailableOfferingsButton;
	private Text offeringFilterDescriptionText;

	private ConditionGroup visibleConditionGroup;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammeBookingComponentGeneralComposite(
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

		// load Portal to get Event
		Objects.requireNonNull(portalPK);
		this.portal = PortalModel.getInstance().getPortal(portalPK);


		/* Determine if the user has advanced access to the ScriptComponent
		 * The condition is: isAdmin || (expertMode && isOfferingFilterAvailable)
		 * To improve performance, we evaluate the ConfigParameterSet at the end and only if necessary.
		 */
		portalExpert = CurrentUserModel.getInstance().isAdmin() || CurrentUserModel.getInstance().isPortalExpert();
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		PageWidgetBuilder widgetBuilder = new PageWidgetBuilder(parent, COL_COUNT, modifySupport, portal);

		setLayout( new GridLayout(COL_COUNT, false) );

		if (portalExpert) {
			htmlIdText = widgetBuilder.buildHtmlId();
			renderText = widgetBuilder.buildRender();
		}

		buildCurrency(parent);
		buildProgrammePoints(parent);
		buildButtons(parent);
		buildOfferingFilter(parent);

		visibleConditionGroup = widgetBuilder.buildConditionGroup(I18N.PageEditor_Visibility);
		visibleConditionGroup.setDefaultCondition(true);
	}


	public void buildCurrency(Composite parent) throws Exception {
   		SWTHelper.createLabel(parent, ProgrammeBookingComponent.FIELD_CURRENCY.getString(), true);

   		currencyCombo = new CurrencyCombo(this, SWT.BORDER);
   		GridDataFactory.swtDefaults().grab(true, false).applyTo(currencyCombo);
		SWTHelper.makeBold(currencyCombo);
		currencyCombo.addModifyListener(modifySupport);
	}


	private void buildProgrammePoints(Composite parent) {
		ppListComposite = new ProgrammePointListComposite(parent, SWT.NONE, portal.getId());
		GridDataFactory
			.fillDefaults()
			.grab(true, true)
			.span(COL_COUNT, 1)
			.applyTo(ppListComposite);

		ppListComposite.addModifyListener(modifySupport);
	}


	private void buildButtons(Composite parent) {
		new Label(parent, SWT.NONE);

		onlyOnlineAvailableOfferingsButton = new Button(parent, SWT.CHECK);
		onlyOnlineAvailableOfferingsButton.setText( ProgrammeBookingComponent.FIELD_ONLY_ONLINE_AVAILABLE_OFFERINGS.getLabel() );
		onlyOnlineAvailableOfferingsButton.setToolTipText( ProgrammeBookingComponent.FIELD_ONLY_ONLINE_AVAILABLE_OFFERINGS.getDescription() );
		onlyOnlineAvailableOfferingsButton.addSelectionListener(modifySupport);

		GridDataFactory.fillDefaults().span(COL_COUNT - 1, 1).applyTo(onlyOnlineAvailableOfferingsButton);
	}


	private void buildOfferingFilter(Composite parent) {
		SWTHelper.createTopLabel(parent, ProgrammeBookingComponent.FIELD_OFFERING_FILTER_DESCRIPTION.getString());

		offeringFilterDescriptionText = new MultiLineText(parent, SWT.BORDER, true);
		GridDataFactory.fillDefaults().grab(true, false).span(COL_COUNT - 1, 1).applyTo(offeringFilterDescriptionText);
		SWTHelper.enableTextWidget(offeringFilterDescriptionText, false);
		offeringFilterDescriptionText.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (portalExpert) {
			htmlIdText.setText( avoidNull(entity.getHtmlId()) );
			renderText.setText( avoidNull(entity.getRender()) );
		}

		currencyCombo.setCurrencyCode( entity.getCurrency() );
		ppListComposite.setProgrammePointIdListProvider(entity);
		onlyOnlineAvailableOfferingsButton.setSelection( entity.isOnlyOnlineAvailableOfferings() );
		offeringFilterDescriptionText.setText( avoidNull(entity.getOfferingFilterDescription()) );

		visibleConditionGroup.setCondition( entity.getVisibleCondition() );
		visibleConditionGroup.setDescription( entity.getVisibleConditionDescription() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (portalExpert) {
				entity.setHtmlId( htmlIdText.getText() );
				entity.setRender( renderText.getText() );
			}

			entity.setCurrency( currencyCombo.getCurrencyCode() );
			entity.setProgrammePointIdList( ppListComposite.getProgrammePointIds() );
			entity.setOnlyOnlineAvailableOfferings( onlyOnlineAvailableOfferingsButton.getSelection() );
			// ignore offeringFilterDescriptionText, because it is read-only

			entity.setVisibleCondition( visibleConditionGroup.getCondition() );
			entity.setVisibleConditionDescription( visibleConditionGroup.getDescription() );
		}
	}


	public void setFixedStructure(boolean fixedStructure) {
		if (portalExpert) {
			htmlIdText.setEnabled(!fixedStructure);
			renderText.setEnabled(!fixedStructure);
		}
	}

}
