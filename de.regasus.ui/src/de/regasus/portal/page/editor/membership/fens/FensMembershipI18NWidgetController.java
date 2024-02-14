package de.regasus.portal.page.editor.membership.fens;

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
import de.regasus.portal.component.membership.esska.EsskaMembershipComponent;
import de.regasus.portal.component.membership.fens.FensMembershipComponent;

import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;


public class FensMembershipI18NWidgetController implements I18NWidgetController<FensMembershipComponent>{


	// the entity
	private FensMembershipComponent membershipComponent;

	// widget Maps
	private Map<String, Text> userNameLabelWidgetMap = new HashMap<>();
	private Map<String, Text> passwordLabelWidgetMap = new HashMap<>();
	private Map<String, Text> invalidMembershipMessageWidgetMap = new HashMap<>();
	private Map<String, Text> invalidUserCredentialsMessageWidgetMap = new HashMap<>();


	public FensMembershipI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		userNameLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(EsskaMembershipComponent.USER_NAME_LABEL)
				.bold(true)
				.build()
		);

		passwordLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(EsskaMembershipComponent.PASSWORD_LABEL)
				.bold(true)
				.build()
		);

		invalidMembershipMessageWidgetMap.put(
			lang,
			builder
				.fieldMetadata(EsskaMembershipComponent.INVALID_MEMBERSHIP_MESSAGE)
				.bold(true)
				.build()
		);

		invalidUserCredentialsMessageWidgetMap.put(
			lang,
			builder
				.fieldMetadata(EsskaMembershipComponent.INVALID_USER_CREDENTIALS_MESSAGE)
				.bold(true)
				.build()
		);
	}


	@Override
	public void dispose() {
	}


	@Override
	public FensMembershipComponent getEntity() {
		return membershipComponent;
	}


	@Override
	public void setEntity(FensMembershipComponent membershipComponent) {
		this.membershipComponent = membershipComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (membershipComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(membershipComponent.getUserNameLabel(), userNameLabelWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getPasswordLabel(), passwordLabelWidgetMap);
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
			membershipComponent.setUserNameLabel( buildLanguageString(userNameLabelWidgetMap));
			membershipComponent.setPasswordLabel( buildLanguageString(passwordLabelWidgetMap) );
			membershipComponent.setInvalidMembershipMessage( buildLanguageString(invalidMembershipMessageWidgetMap) );
			membershipComponent.setInvalidUserCredentialsMessage( buildLanguageString(invalidUserCredentialsMessageWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
