package de.regasus.impex.db.job;

import java.io.File;

import com.lambdalogic.util.TypeHelper;

import de.regasus.core.ServerModel;
import de.regasus.impex.Constants;
import de.regasus.impex.EventExportSettings;
import de.regasus.impex.ImpexI18N;

public class EventExportJob extends AbstractExportJob {

	private String url;


	public EventExportJob(Long eventPK, EventExportSettings settings, File file) throws Exception {
		super(ImpexI18N.EventExportJob_Name, file);

		// build URL to RWS
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder(1024);
		sb.append( ServerModel.getInstance().getWebServiceUrl() );
		sb.append("/").append(Constants.EXPORT_RESOURCE_NAME);
		sb.append("/").append(Constants.EVENT_PATH);
		sb.append("/").append(eventPK);

		sb.append("?").append(Constants.QUERY_PARAMETER_PHOTO).append("=");
		sb.append( TypeHelper.toString(settings.isIncludePhoto()) );

		sb.append("&").append(Constants.QUERY_PARAMETER_PARTICIPANT_CORRESPONDENCE).append("=");
		sb.append( TypeHelper.toString(settings.isIncludeParticipantCorrespondence()) );

		sb.append("&").append(Constants.QUERY_PARAMETER_PARTICIPANT_FILE).append("=");
		sb.append( TypeHelper.toString(settings.isIncludeParticipantFile()) );

		url = sb.toString();
	}


	@Override
	protected String getURL() {
		return url;
	}

}
