package de.regasus.portal.page.editor.membership.enets;

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
import de.regasus.portal.component.membership.enets.EnetsMembershipComponent;


public class EnetsMembershipI18NWidgetController implements I18NWidgetController<EnetsMembershipComponent>{


	// the entity
	private EnetsMembershipComponent membershipComponent;

	// widget Maps
	private Map<String, Text> enetsIdLabelWidgetMap = new HashMap<>();


	public EnetsMembershipI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);
		
		enetsIdLabelWidgetMap.put(
			lang, 
			builder
				.fieldMetadata(EnetsMembershipComponent.ENETS_ID_LABEL)
				.bold(true)
				.build()
		);
	}


	@Override
	public void dispose() {
	}


	@Override
	public EnetsMembershipComponent getEntity() {
		return membershipComponent;
	}


	@Override
	public void setEntity(EnetsMembershipComponent membershipComponent) {
		this.membershipComponent = membershipComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (membershipComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(membershipComponent.getEnetsIdLabel(), enetsIdLabelWidgetMap);
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
		if (membershipComponent != null) {
			membershipComponent.setEnetsIdLabel( buildLanguageString(enetsIdLabelWidgetMap));
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
