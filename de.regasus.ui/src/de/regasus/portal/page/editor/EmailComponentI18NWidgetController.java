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
import de.regasus.portal.component.EmailComponent;
import static de.regasus.portal.component.EmailComponent.*;

public class EmailComponentI18NWidgetController implements I18NWidgetController<EmailComponent>{

	// the entity
	private EmailComponent emailComponent;


	// widget Maps
	private Map<String, Text> labelWidgetMap = new HashMap<>();
	private Map<String, Text> placeholderWidgetMap = new HashMap<>();
	private Map<String, Text> tooltipWidgetMap = new HashMap<>();
	private Map<String, Text> requiredMessageWidgetMap = new HashMap<>();
	private Map<String, Text> invalidMessageWidgetMap = new HashMap<>();
	private Map<String, Text> emailRepetitionLabelWidgetMap = new HashMap<>();
	private Map<String, Text> emailRepetitionNotMatchMessageWidgetMap = new HashMap<>();
	private Map<String, Text> duplicateMessageWidgetMap = new HashMap<>();


	public EmailComponentI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		labelWidgetMap.put(lang, builder.fieldMetadata(LABEL).build());

		placeholderWidgetMap.put(lang, builder.fieldMetadata(PLACEHOLDER).build());

		tooltipWidgetMap.put(lang, builder.fieldMetadata(TOOLTIP).build());

		requiredMessageWidgetMap.put(lang, builder.fieldMetadata(REQUIRED_MESSAGE).build());
		
		invalidMessageWidgetMap.put(lang, builder.fieldMetadata(INVALID_MESSAGE).build());
		
		emailRepetitionLabelWidgetMap.put(lang, builder.fieldMetadata(EMAIL_REPETITION_LABEL).build());
		
		emailRepetitionNotMatchMessageWidgetMap.put(lang, builder.fieldMetadata(EMAIL_REPETITION_NOT_MATCH_MESSAGE).build());
		
		duplicateMessageWidgetMap.put(lang,	builder.fieldMetadata(DUPLICATE_MESSAGE).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public EmailComponent getEntity() {
		return emailComponent;
	}


	@Override
	public void setEntity(EmailComponent fieldComponent) {
		this.emailComponent = fieldComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (emailComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(emailComponent.getLabel(), labelWidgetMap);
						setLanguageStringToTextWidget(emailComponent.getPlaceholder(), placeholderWidgetMap);
						setLanguageStringToTextWidget(emailComponent.getTooltip(), tooltipWidgetMap);
						setLanguageStringToTextWidget(emailComponent.getRequiredMessage(), requiredMessageWidgetMap);
						setLanguageStringToTextWidget(emailComponent.getInvalidMessage(), invalidMessageWidgetMap);
						setLanguageStringToTextWidget(emailComponent.getEmailRepetitionLabel(), emailRepetitionLabelWidgetMap);
						setLanguageStringToTextWidget(emailComponent.getEmailRepetitionNotMatchMessage(), emailRepetitionNotMatchMessageWidgetMap);
						setLanguageStringToTextWidget(emailComponent.getDuplicateMessage(), duplicateMessageWidgetMap);
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
		if (emailComponent != null) {
			emailComponent.setLabel( buildLanguageString(labelWidgetMap) );
			emailComponent.setPlaceholder( buildLanguageString(placeholderWidgetMap) );
			emailComponent.setTooltip( buildLanguageString(tooltipWidgetMap) );
			emailComponent.setRequiredMessage( buildLanguageString(requiredMessageWidgetMap) );
			emailComponent.setInvalidMessage( buildLanguageString(invalidMessageWidgetMap) );
			emailComponent.setEmailRepetitionLabel( buildLanguageString(emailRepetitionLabelWidgetMap));
			emailComponent.setEmailRepetitionNotMatchMessage( buildLanguageString(emailRepetitionNotMatchMessageWidgetMap) );
			emailComponent.setDuplicateMessage( buildLanguageString(duplicateMessageWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
