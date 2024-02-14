package de.regasus.portal.page.editor.membership.ispad;

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
import de.regasus.portal.component.membership.ispad.IspadMembershipComponent;

import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;


public class IspadMembershipI18NWidgetController implements I18NWidgetController<IspadMembershipComponent>{


	// the entity
	private IspadMembershipComponent membershipComponent;

	// widget Maps
	private Map<String, Text> invalidMembershipMessageWidgetMap = new HashMap<>();
	private Map<String, Text> invalidUserCredentialsMessageWidgetMap = new HashMap<>();


	public IspadMembershipI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		invalidMembershipMessageWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IspadMembershipComponent.INVALID_MEMBERSHIP_MESSAGE)
				.bold(true)
				.build()
		);

		invalidUserCredentialsMessageWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IspadMembershipComponent.INVALID_USER_CREDENTIALS_MESSAGE)
				.bold(true)
				.build()
		);
	}


	@Override
	public void dispose() {
	}


	@Override
	public IspadMembershipComponent getEntity() {
		return membershipComponent;
	}


	@Override
	public void setEntity(IspadMembershipComponent membershipComponent) {
		this.membershipComponent = membershipComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (membershipComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(membershipComponent.getInvalidMembershipMessage(), invalidMembershipMessageWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getInvalidUserCredentialsMessage(), invalidUserCredentialsMessageWidgetMap);
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
			membershipComponent.setInvalidMembershipMessage( buildLanguageString(invalidMembershipMessageWidgetMap) );
			membershipComponent.setInvalidUserCredentialsMessage( buildLanguageString(invalidUserCredentialsMessageWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
