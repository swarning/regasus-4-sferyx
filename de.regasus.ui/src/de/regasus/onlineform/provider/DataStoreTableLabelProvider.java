package de.regasus.onlineform.provider;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.regasus.UploadableFileType;

import de.regasus.core.LanguageModel;

public class DataStoreTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	LanguageModel languageModel = LanguageModel.getInstance();

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}


	@Override
	public String getColumnText(Object element, int columnIndex) {

		DataStoreVO dataStoreVO = (DataStoreVO) element;

		switch(columnIndex) {
		case 0:
			String docType = dataStoreVO.getDocType();
				try {
					// The database should in theory only contain docTypes belonging to
					// one of the enum constants, but you never know for sure...
					return UploadableFileType.valueOf(docType).getString();
				}
				catch (Exception e1) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e1);
				}
			return docType;
		case 1:
			String languageId = dataStoreVO.getLanguage();
			try {
				return languageModel.getLanguage(languageId).getName().getString();
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				return languageId;
			}
		case 2:
			return dataStoreVO.getExtFileName();
		}
		return null;
	}


}
