package de.regasus.portal.page.editor.react.profile;

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
import de.regasus.portal.component.react.profile.DocumediasAbstractSubmissionComponent;

public class DocumediasAbstractSubmissionComponentCompositeI18NWidgetController
	implements I18NWidgetController<DocumediasAbstractSubmissionComponent> {

	// the entity
	private DocumediasAbstractSubmissionComponent component;

	// widget Maps
	private Map<String, Text> submissionButtonLabelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		submissionButtonLabelWidgetMap.put(lang, builder.fieldMetadata(DocumediasAbstractSubmissionComponent.SUBMISSION_BUTTON_LABEL).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public DocumediasAbstractSubmissionComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(DocumediasAbstractSubmissionComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getSubmissionButtonLabel(), submissionButtonLabelWidgetMap);
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
			component.setSubmissionButtonLabel( buildLanguageString(submissionButtonLabelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
