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


public class IspadMembershipWantedI18NWidgetController implements I18NWidgetController<IspadMembershipComponent>{


	// the entity
	private IspadMembershipComponent membershipComponent;

	// widget Maps
	private Map<String, Text> membershipWantedAnswerYesWidgetMap = new HashMap<>();
	private Map<String, Text> membershipWantedAnswerNoWidgetMap = new HashMap<>();


	public IspadMembershipWantedI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		membershipWantedAnswerYesWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IspadMembershipComponent.MEMBERSHIP_WANTED_ANSWER_YES)
				.bold(true)
				.build()
		);

		membershipWantedAnswerNoWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IspadMembershipComponent.MEMBERSHIP_WANTED_ANSWER_NO)
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
						setLanguageStringToTextWidget(membershipComponent.getMembershipWantedAnswerYes(), membershipWantedAnswerYesWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getMembershipWantedAnswerNo(), membershipWantedAnswerNoWidgetMap);
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
			membershipComponent.setMembershipWantedAnswerYes( buildLanguageString(membershipWantedAnswerYesWidgetMap));
			membershipComponent.setMembershipWantedAnswerNo( buildLanguageString(membershipWantedAnswerNoWidgetMap));
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
