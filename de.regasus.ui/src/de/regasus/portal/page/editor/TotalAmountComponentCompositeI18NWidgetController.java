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
import de.regasus.portal.component.TotalAmountComponent;

public class TotalAmountComponentCompositeI18NWidgetController implements I18NWidgetController<TotalAmountComponent>{

	// the entity
	private TotalAmountComponent component;

	// widget Maps
	private Map<String, Text> totalAmountHeaderLabelWidgetMap = new HashMap<>();
	private Map<String, Text> grossAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> netAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> taxAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> paidAmountLabelWidgetMap = new HashMap<>();
	private Map<String, Text> openAmountLabelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		totalAmountHeaderLabelWidgetMap.put(lang, builder.fieldMetadata(TotalAmountComponent.TOTAL_AMOUNT_HEADER_LABEL).build());
		grossAmountLabelWidgetMap.put(lang, builder.fieldMetadata(TotalAmountComponent.GROSS_AMOUNT_LABEL).build());
		netAmountLabelWidgetMap.put(lang, builder.fieldMetadata(TotalAmountComponent.NET_AMOUNT_LABEL).build());
		taxAmountLabelWidgetMap.put(lang, builder.fieldMetadata(TotalAmountComponent.TAX_AMOUNT_LABEL).build());
		paidAmountLabelWidgetMap.put(lang, builder.fieldMetadata(TotalAmountComponent.PAID_AMOUNT_LABEL).build());
		openAmountLabelWidgetMap.put(lang, builder.fieldMetadata(TotalAmountComponent.OPEN_AMOUNT_LABEL).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public TotalAmountComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(TotalAmountComponent component) {
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
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
