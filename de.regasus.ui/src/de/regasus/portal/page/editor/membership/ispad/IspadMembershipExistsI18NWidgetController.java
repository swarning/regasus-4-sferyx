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


public class IspadMembershipExistsI18NWidgetController implements I18NWidgetController<IspadMembershipComponent>{


	// the entity
	private IspadMembershipComponent membershipComponent;

	// widget Maps
	private Map<String, Text> membershipExistsQuestionWidgetMap = new HashMap<>();
	private Map<String, Text> membershipExistsAnswerYesWidgetMap = new HashMap<>();
	private Map<String, Text> membershipExistsAnswerNoWidgetMap = new HashMap<>();


	public IspadMembershipExistsI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		membershipExistsQuestionWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IspadMembershipComponent.MEMBERSHIP_EXISTS_QUESTION)
				.bold(true)
				.build()
		);

		membershipExistsAnswerYesWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IspadMembershipComponent.MEMBERSHIP_EXISTS_ANSWER_YES)
				.bold(true)
				.build()
		);

		membershipExistsAnswerNoWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IspadMembershipComponent.MEMBERSHIP_EXISTS_ANSWER_NO)
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
						setLanguageStringToTextWidget(membershipComponent.getMembershipExistsQuestion(), membershipExistsQuestionWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getMembershipExistsAnswerYes(), membershipExistsAnswerYesWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getMembershipExistsAnswerNo(), membershipExistsAnswerNoWidgetMap);
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
			membershipComponent.setMembershipExistsQuestion( buildLanguageString(membershipExistsQuestionWidgetMap));
			membershipComponent.setMembershipExistsAnswerYes( buildLanguageString(membershipExistsAnswerYesWidgetMap));
			membershipComponent.setMembershipExistsAnswerNo( buildLanguageString(membershipExistsAnswerNoWidgetMap));
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
