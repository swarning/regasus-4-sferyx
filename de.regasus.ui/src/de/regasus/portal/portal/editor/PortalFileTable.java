package de.regasus.portal.portal.editor;

import static de.regasus.portal.PortalFileHelper.extractFileMnemonic;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.BaseFile;
import de.regasus.common.File;
import de.regasus.common.FileModel;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;


enum PortalFileTableColumns {MNEMONIC, LANGUAGE, EXT_PATH, URL};

public class PortalFileTable extends SimpleTable<File, PortalFileTableColumns> {

	public PortalFileTable(Table table) {
		super(table, PortalFileTableColumns.class);
	}


	@Override
	public String getColumnText(
		File file,
		PortalFileTableColumns column
	) {
		String label = null;

		try {
			switch (column) {
				case MNEMONIC:
					String mnemonic = extractFileMnemonic( file.getInternalPath() );
					label = mnemonic;
					break;
				case LANGUAGE:
					label = extractLanguageName(file);
					break;
				case EXT_PATH:
					label = file.getExternalPath();
					break;
				case URL:
					label = FileModel.buildWebServiceUrl(file);
					break;
			}

			if (label == null) {
				label = "";
			}
		}
		catch (Exception e) {
			label = e.getMessage();
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return label;
	}


	private String extractLanguageName(BaseFile baseFile) throws Exception {
		String languageName = null;

		String languageId = baseFile.getLanguage();
		if (languageId != null) {
			Language language = LanguageModel.getInstance().getLanguage(languageId);
			languageName = language.getName().getString();
		}

		return languageName;
	}


	@Override
	protected PortalFileTableColumns getDefaultSortColumn() {
		return PortalFileTableColumns.MNEMONIC;
	}

}
