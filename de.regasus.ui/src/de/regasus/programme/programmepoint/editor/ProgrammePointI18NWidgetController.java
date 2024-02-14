package de.regasus.programme.programmepoint.editor;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import com.lambdalogic.util.rcp.UtilI18N;


public class ProgrammePointI18NWidgetController implements I18NWidgetController<ProgrammePointVO> {

	// the entity
	private ProgrammePointVO programmePointVO;

	// widget Maps
	private Map<String, Text> nameWidgetMap = new HashMap<>();
	private Map<String, Text> infoWidgetMap = new HashMap<>();
	private Map<String, Text> onlineInfoWidgetMap = new HashMap<>();
	private Map<String, Text> meetingPointWidgetMap = new HashMap<>();
	private Map<String, Text> endPointWidgetMap = new HashMap<>();



	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		builder.bold(true);
		nameWidgetMap.put(lang, builder.label(UtilI18N.Name).build());

		builder.bold(false).multiLine(true);
		infoWidgetMap.put(lang, builder.label(UtilI18N.Info).build());
		onlineInfoWidgetMap.put(lang, builder.label(ParticipantLabel.ProgrammePoint_OnlineInfo).build());
		meetingPointWidgetMap.put(lang, builder.label(ParticipantLabel.ProgrammePoint_MeetingPoint).build());
		endPointWidgetMap.put(lang, builder.label(ParticipantLabel.ProgrammePoint_EndPoint).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public ProgrammePointVO getEntity() {
		return programmePointVO;
	}


	@Override
	public void setEntity(ProgrammePointVO programmePointVO) {
		this.programmePointVO = programmePointVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (programmePointVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(programmePointVO.getName(), nameWidgetMap);
						setLanguageStringToTextWidget(programmePointVO.getInfo(), infoWidgetMap);
						setLanguageStringToTextWidget(programmePointVO.getOnlineInfo(), onlineInfoWidgetMap);
						setLanguageStringToTextWidget(programmePointVO.getMeetingPoint(), meetingPointWidgetMap);
						setLanguageStringToTextWidget(programmePointVO.getEndPoint(), endPointWidgetMap);
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
		if (programmePointVO != null) {
			programmePointVO.setName( buildLanguageString(nameWidgetMap) );
			programmePointVO.setInfo( buildLanguageString(infoWidgetMap) );
			programmePointVO.setOnlineInfo( buildLanguageString(onlineInfoWidgetMap) );
			programmePointVO.setMeetingPoint( buildLanguageString(meetingPointWidgetMap) );
			programmePointVO.setEndPoint( buildLanguageString(endPointWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		addFocusListenerToWidgets(
			listener,
			nameWidgetMap, infoWidgetMap, onlineInfoWidgetMap, meetingPointWidgetMap, endPointWidgetMap
		);
	}

}
