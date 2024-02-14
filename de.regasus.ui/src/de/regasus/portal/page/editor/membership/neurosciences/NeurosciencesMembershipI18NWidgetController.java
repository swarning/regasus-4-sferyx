package de.regasus.portal.page.editor.membership.neurosciences;

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
import de.regasus.portal.component.membership.neurosciences.NeurosciencesMembershipComponent;


public class NeurosciencesMembershipI18NWidgetController implements I18NWidgetController<NeurosciencesMembershipComponent>{


	// the entity
	private NeurosciencesMembershipComponent membershipComponent;

	// widget Maps
	private Map<String, Text> membershipLabelWidgetMap = new HashMap<>();
	private Map<String, Text> memberButtonLabelWidgetMap = new HashMap<>();
	private Map<String, Text> nonMemberButtonLabelWidgetMap = new HashMap<>();
	private Map<String, Text> membershipNumberLabelWidgetMap = new HashMap<>();


	public NeurosciencesMembershipI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		membershipLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(NeurosciencesMembershipComponent.MEMBERSHIP_LABEL)
				.bold(true)
				.build()
		);

		memberButtonLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(NeurosciencesMembershipComponent.MEMBER_BUTTON_LABEL)
				.bold(true)
				.build()
		);

		nonMemberButtonLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(NeurosciencesMembershipComponent.NON_MEMBER_BUTTON_LABEL)
				.bold(true)
				.build()
		);

		membershipNumberLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(NeurosciencesMembershipComponent.MEMBERSHIP_NUMBER_LABEL)
				.bold(true)
				.build()
		);
	}


	@Override
	public void dispose() {
	}


	@Override
	public NeurosciencesMembershipComponent getEntity() {
		return membershipComponent;
	}


	@Override
	public void setEntity(NeurosciencesMembershipComponent membershipComponent) {
		this.membershipComponent = membershipComponent;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (membershipComponent != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(membershipComponent.getMembershipLabel(), membershipLabelWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getMemberButtonLabel(), memberButtonLabelWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getNonMemberButtonLabel(), nonMemberButtonLabelWidgetMap);
						setLanguageStringToTextWidget(membershipComponent.getMembershipNumberLabel(), membershipNumberLabelWidgetMap);
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
			membershipComponent.setMembershipLabel( buildLanguageString(membershipLabelWidgetMap));
			membershipComponent.setMemberButtonLabel( buildLanguageString(memberButtonLabelWidgetMap) );
			membershipComponent.setNonMemberButtonLabel( buildLanguageString(nonMemberButtonLabelWidgetMap) );
			membershipComponent.setMembershipNumberLabel( buildLanguageString(membershipNumberLabelWidgetMap));
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
