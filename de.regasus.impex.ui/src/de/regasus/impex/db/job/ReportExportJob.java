package de.regasus.impex.db.job;

import java.io.File;

import de.regasus.core.ServerModel;
import de.regasus.impex.Constants;
import de.regasus.impex.ImpexI18N;

public class ReportExportJob extends AbstractExportJob {

	private String url;


	public ReportExportJob(Long userReportDirPK, File file) throws Exception {
		super(ImpexI18N.ReportExportJob_Name, file);

		// build URL to RWS
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder(1024);
		sb.append( ServerModel.getInstance().getWebServiceUrl() );
		sb.append("/").append(Constants.EXPORT_RESOURCE_NAME);
		sb.append("/").append(Constants.REPORT_DATA_PATH);
		sb.append("/").append(userReportDirPK);

		url = sb.toString();
	}


	@Override
	protected String getURL() {
		return url;
	}

}
