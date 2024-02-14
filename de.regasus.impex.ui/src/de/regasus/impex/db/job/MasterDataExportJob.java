package de.regasus.impex.db.job;

import java.io.File;

import de.regasus.core.ServerModel;
import de.regasus.impex.Constants;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.MasterDataExportSettings;

public class MasterDataExportJob extends AbstractExportJob {

	private String url;


	public MasterDataExportJob(MasterDataExportSettings settings, File file) throws Exception {
		super(ImpexI18N.MasterDataExportJob_Name, file);

		// build URL to RWS
		url = settings.buildURL(ServerModel.getInstance().getWebServiceUrl() + "/" + Constants.EXPORT_RESOURCE_NAME);
	}


	@Override
	protected String getURL() {
		return url.toString();
	}

}
