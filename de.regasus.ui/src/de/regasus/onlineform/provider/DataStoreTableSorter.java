package de.regasus.onlineform.provider;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.regasus.UploadableFileType;

import de.regasus.core.LanguageModel;

public class DataStoreTableSorter extends ViewerSorter {

	private Collator collator = Collator.getInstance();
	private LanguageModel languageModel = LanguageModel.getInstance();


	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		DataStoreVO ds1 =  (DataStoreVO) o1;
		DataStoreVO ds2 =  (DataStoreVO) o2;

		// 1st sort criteria: name
		String name1 = getName(ds1);
		String name2 = getName(ds2);

		int result = collator.compare(name1, name2);

		// 2nd sort criteria: language
		if (result == 0) {
			String language1 = getLanguage(ds1);
			String language2 = getLanguage(ds2);

			result = collator.compare(language1, language2);
		}

		return result;
	}


	private String getName(DataStoreVO ds) {
		UploadableFileType fileType = UploadableFileType.valueOf(ds.getDocType());
		return fileType.getString();
	}


	private String getLanguage(DataStoreVO ds) {
		// init language with language code
		String language = ds.getLanguage();

		// replace language code with language
		try {
			language = languageModel.getLanguage(language).getName().getString();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return language;
	}

}
