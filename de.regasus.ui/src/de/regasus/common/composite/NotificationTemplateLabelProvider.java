package de.regasus.common.composite;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.FileHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;

/**
 * LabelProvider for DataStoreVOs in a Table.
 *
 * @author sacha
 *
 */
public class NotificationTemplateLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		return null;
	}


	@Override
	public String getColumnText(Object element, int columnIndex) {
		DataStoreVO dataStoreVO = (DataStoreVO) element;

		if (columnIndex == 1) {
			String languageName = dataStoreVO.getLanguage();
			if (languageName != null) {
				try {
					Language language = LanguageModel.getInstance().getLanguage(languageName);
					languageName = language.getName().getString();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
			return languageName;
		}
		else if (columnIndex == 2) {
			String extFileName = getFileName(dataStoreVO);
			return extFileName;
		}
		else {
			return null;
		}
	}


	private String getFileName(DataStoreVO dataStoreVO) {
		String extFileName = dataStoreVO.getExtFileName();
		extFileName = FileHelper.getName(extFileName);
		return extFileName;
	}

}
