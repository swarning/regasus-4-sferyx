package de.regasus.common.document.editor;

import java.util.Collection;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.BaseFile;
import de.regasus.common.FileSummary;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.PortalFileModel;

enum DocumentTableColumn {LANGUAGE, FILE, URL};


class DocumentTable extends SimpleTable<FileSummary, DocumentTableColumn> {

	public DocumentTable(Table table) {
		super(table, DocumentTableColumn.class);
	}


	@Override
	public String getColumnText(FileSummary fileSummary, DocumentTableColumn column) {
		String label = null;
		try {
    		switch (column) {
    			case LANGUAGE:
    				label = extractLanguageName(fileSummary);
    				break;
    			case FILE: {
    				String fileName = KernelLabel.DefaultTemplate.getString();
    				if (fileSummary.getId() != null) {
    					fileName = fileSummary.getExternalPath();
    					try {
    						fileName = FileHelper.getName(fileName);
    					}
    					catch (Exception e) {
    						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
    					}
    				}
    				label = fileName;
    				break;
    			}
    			case URL:
    				label = PortalFileModel.buildWebServiceUrl(fileSummary);
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


	public void setData(Collection<FileSummary> fileSummary) {
		getViewer().setInput(fileSummary);
	}

}
