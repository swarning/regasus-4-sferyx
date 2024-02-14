package de.regasus.portal.page.editor.hotel;

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
import de.regasus.portal.component.hotel.HotelTotalAmountComponent;

public class HotelTotalAmountComponentCompositeI18NWidgetController implements I18NWidgetController<HotelTotalAmountComponent>{

	// the entity
	private HotelTotalAmountComponent component;

	// widget Maps
	private Map<String, Text> totalAmountHeaderLabelWidgetMap = new HashMap<>();
	private Map<String, Text> grossAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> netAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> taxAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> paidAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> openAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> lodgingLabelWidgetMap = new HashMap<>();
	private Map<String, Text> breakfastLabelWidgetMap = new HashMap<>();
	private Map<String, Text> add1PriceLabelWidgetMap = new HashMap<>();
	private Map<String, Text> add2PriceLabelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		totalAmountHeaderLabelWidgetMap.put(lang, builder.fieldMetadata( HotelTotalAmountComponent.TOTAL_AMOUNT_HEADER_LABEL ).build());
		grossAmountLabelWidgetMap.put(      lang, builder.fieldMetadata( HotelTotalAmountComponent.GROSS_AMOUNT_LABEL        ).build());
		netAmountLabelWidgetMap.put(        lang, builder.fieldMetadata( HotelTotalAmountComponent.NET_AMOUNT_LABEL          ).build());
		taxAmountLabelWidgetMap.put(        lang, builder.fieldMetadata( HotelTotalAmountComponent.TAX_AMOUNT_LABEL          ).build());
		paidAmountLabelWidgetMap.put(       lang, builder.fieldMetadata( HotelTotalAmountComponent.PAID_AMOUNT_LABEL         ).build());
		openAmountLabelWidgetMap.put(       lang, builder.fieldMetadata( HotelTotalAmountComponent.OPEN_AMOUNT_LABEL         ).build());
		lodgingLabelWidgetMap.put(          lang, builder.fieldMetadata( HotelTotalAmountComponent.LODGING_LABEL             ).build());
		breakfastLabelWidgetMap.put(        lang, builder.fieldMetadata( HotelTotalAmountComponent.BREAKFAST_LABEL           ).build());
		add1PriceLabelWidgetMap.put(        lang, builder.fieldMetadata( HotelTotalAmountComponent.ADD_1_PRICE_LABEL         ).build());
		add2PriceLabelWidgetMap.put(        lang, builder.fieldMetadata( HotelTotalAmountComponent.ADD_2_PRICE_LABEL         ).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public HotelTotalAmountComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(HotelTotalAmountComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getTotalAmountHeaderLabel(), totalAmountHeaderLabelWidgetMap);
						setLanguageStringToTextWidget(component.getGrossAmountLabel(), grossAmountLabelWidgetMap);
						setLanguageStringToTextWidget(component.getNetAmountLabel(), netAmountLabelWidgetMap);
						setLanguageStringToTextWidget(component.getTaxAmountLabel(), taxAmountLabelWidgetMap);
						setLanguageStringToTextWidget(component.getPaidAmountLabel(), paidAmountLabelWidgetMap);
						setLanguageStringToTextWidget(component.getOpenAmountLabel(), openAmountLabelWidgetMap);
						setLanguageStringToTextWidget(component.getLodgingLabel(), lodgingLabelWidgetMap);
						setLanguageStringToTextWidget(component.getBreakfastLabel(), breakfastLabelWidgetMap);
						setLanguageStringToTextWidget(component.getAdd1PriceLabel(), add1PriceLabelWidgetMap);
						setLanguageStringToTextWidget(component.getAdd2PriceLabel(), add2PriceLabelWidgetMap);
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
			component.setTotalAmountHeaderLabel( buildLanguageString(totalAmountHeaderLabelWidgetMap) );
			component.setGrossAmountLabel( buildLanguageString(grossAmountLabelWidgetMap) );
			component.setNetAmountLabel( buildLanguageString(netAmountLabelWidgetMap) );
			component.setTaxAmountLabel( buildLanguageString(taxAmountLabelWidgetMap) );
			component.setPaidAmountLabel( buildLanguageString(paidAmountLabelWidgetMap) );
			component.setOpenAmountLabel( buildLanguageString(openAmountLabelWidgetMap) );
			component.setLodgingLabel( buildLanguageString(lodgingLabelWidgetMap) );
			component.setBreakfastLabel( buildLanguageString(breakfastLabelWidgetMap) );
			component.setAdd1PriceLabel( buildLanguageString(add1PriceLabelWidgetMap) );
			component.setAdd2PriceLabel( buildLanguageString(add2PriceLabelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
