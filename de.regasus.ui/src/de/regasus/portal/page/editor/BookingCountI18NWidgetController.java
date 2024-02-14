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
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.portal.component.ProgrammeBookingComponent;


public class BookingCountI18NWidgetController implements I18NWidgetController<ProgrammeBookingComponent>{


	// the entity
	private ProgrammeBookingComponent pbComponent;

	// widget Maps
	private Map<String, Text> minBookCountMessageWidgetMap = new HashMap<>();
	private Map<String, Text> maxBookCountMessageWidgetMap = new HashMap<>();
	private Map<String, Text> bookingRulesMessageWidgetMap = new HashMap<>();


	public BookingCountI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		minBookCountMessageWidgetMap.put(lang, builder.fieldMetadata(ProgrammeBookingComponent.FIELD_MIN_BOOK_COUNT_MESSAGE).build());
		maxBookCountMessageWidgetMap.put(lang, builder.fieldMetadata(ProgrammeBookingComponent.FIELD_MAX_BOOK_COUNT_MESSAGE).build());
		bookingRulesMessageWidgetMap.put(lang, builder.fieldMetadata(ProgrammeBookingComponent.FIELD_BOOKING_RULES_MESSAGE).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public ProgrammeBookingComponent getEntity() {
		return pbComponent;
	}


	@Override
	public void setEntity(ProgrammeBookingComponent programmeBookingComponent) {
		this.pbComponent = programmeBookingComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pbComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(pbComponent.getMinBookCountMessage(), minBookCountMessageWidgetMap);
						setLanguageStringToTextWidget(pbComponent.getMaxBookCountMessage(), maxBookCountMessageWidgetMap);
						setLanguageStringToTextWidget(pbComponent.getBookingRulesMessage(), bookingRulesMessageWidgetMap);
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
		if (pbComponent != null) {
			pbComponent.setMinBookCountMessage( buildLanguageString(minBookCountMessageWidgetMap) );
			pbComponent.setMaxBookCountMessage( buildLanguageString(maxBookCountMessageWidgetMap) );
			pbComponent.setBookingRulesMessage( buildLanguageString(bookingRulesMessageWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
