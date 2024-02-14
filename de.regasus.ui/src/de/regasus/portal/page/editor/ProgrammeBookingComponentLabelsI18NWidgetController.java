package de.regasus.portal.page.editor;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.ProgrammeBookingComponent;


public class ProgrammeBookingComponentLabelsI18NWidgetController implements I18NWidgetController<ProgrammeBookingComponent>{

	// the entity
	private ProgrammeBookingComponent component;

	// widget Maps
	private Map<String, Text> bookColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> programmeColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> detailColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> availableSeatsColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> netAmountColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> taxRateColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> taxAmountColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> grossAmountColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> subtotalColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> fullyBookedLabelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		bookColumnNameWidgetMap.put(           lang, builder.label( ProgrammeBookingComponent.FIELD_BOOK_COLUMN_NAME.getLabel()            ).build());
		programmeColumnNameWidgetMap.put(      lang, builder.label( ProgrammeBookingComponent.FIELD_PROGRAMME_COLUMN_NAME.getLabel()       ).build());
		detailColumnNameWidgetMap.put(         lang, builder.label( ProgrammeBookingComponent.FIELD_DETAIL_COLUMN_NAME.getLabel()          ).build());
		availableSeatsColumnNameWidgetMap.put( lang, builder.label( ProgrammeBookingComponent.FIELD_AVAILABLE_SEATS_COLUMN_NAME.getLabel() ).build());
		netAmountColumnNameWidgetMap.put(      lang, builder.label( ProgrammeBookingComponent.FIELD_NET_AMOUNT_COLUMN_NAME.getLabel()      ).build());
		taxRateColumnNameWidgetMap.put(        lang, builder.label( ProgrammeBookingComponent.FIELD_TAX_RATE_COLUMN_NAME.getLabel()        ).build());
		taxAmountColumnNameWidgetMap.put(      lang, builder.label( ProgrammeBookingComponent.FIELD_TAX_AMOUNT_COLUMN_NAME.getLabel()      ).build());
		grossAmountColumnNameWidgetMap.put(    lang, builder.label( ProgrammeBookingComponent.FIELD_GROSS_AMOUNT_COLUMN_NAME.getLabel()    ).build());
		subtotalColumnNameWidgetMap.put(       lang, builder.label( ProgrammeBookingComponent.FIELD_SUBTOTAL_COLUMN_NAME.getLabel()        ).build());
		fullyBookedLabelWidgetMap.put(         lang, builder.label( ProgrammeBookingComponent.FIELD_FULLY_BOOKED_LABEL.getLabel()          ).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public ProgrammeBookingComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(ProgrammeBookingComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getBookColumnName(), 			bookColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getProgrammeColumnName(), 	programmeColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getDetailColumnName(), 		detailColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getAvailableSeatsColumnName(),availableSeatsColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getNetAmountColumnName(), 	netAmountColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getTaxRateColumnName(), 		taxRateColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getTaxAmountColumnName(), 	taxAmountColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getGrossAmountColumnName(), 	grossAmountColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getSubtotalColumnName(), 		subtotalColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getFullyBookedLabel(), 		fullyBookedLabelWidgetMap);
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
		if (component != null) {
			component.setBookColumnName( 			buildLanguageString(bookColumnNameWidgetMap) );
			component.setProgrammeColumnName( 	buildLanguageString(programmeColumnNameWidgetMap) );
			component.setDetailColumnName( 		buildLanguageString(detailColumnNameWidgetMap) );
			component.setAvailableSeatsColumnName(buildLanguageString(availableSeatsColumnNameWidgetMap) );
			component.setNetAmountColumnName( 	buildLanguageString(netAmountColumnNameWidgetMap) );
			component.setTaxRateColumnName( 		buildLanguageString(taxRateColumnNameWidgetMap) );
			component.setTaxAmountColumnName( 	buildLanguageString(taxAmountColumnNameWidgetMap) );
			component.setGrossAmountColumnName( 	buildLanguageString(grossAmountColumnNameWidgetMap) );
			component.setSubtotalColumnName( 		buildLanguageString(subtotalColumnNameWidgetMap) );
			component.setFullyBookedLabel( 		buildLanguageString(fullyBookedLabelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
