package de.regasus.portal.page.editor.membership.ilts;

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
import de.regasus.portal.component.membership.ilts.IltsMembershipComponent;

import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;


public class IltsMembershipI18NWidgetController implements I18NWidgetController<IltsMembershipComponent>{


	// the entity
	private IltsMembershipComponent membershipComponent;

	// widget Maps
	private Map<String, Text> membershipLabelWidgetMap = new HashMap<>();
	private Map<String, Text> memberButtonLabelWidgetMap = new HashMap<>();
	private Map<String, Text> nonMemberButtonLabelWidgetMap = new HashMap<>();


	public IltsMembershipI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		membershipLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IltsMembershipComponent.MEMBERSHIP_LABEL)
				.bold(true)
				.build()
		);

		memberButtonLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IltsMembershipComponent.MEMBER_BUTTON_LABEL)
				.bold(true)
				.build()
		);

		nonMemberButtonLabelWidgetMap.put(
			lang,
			builder
				.fieldMetadata(IltsMembershipComponent.NON_MEMBER_BUTTON_LABEL)
				.bold(true)
				.build()
		);
	}


	@Override
	public void dispose() {
	}


	@Override
	public IltsMembershipComponent getEntity() {
		return membershipComponent;
	}


	@Override
	public void setEntity(IltsMembershipComponent membershipComponent) {
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
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}
