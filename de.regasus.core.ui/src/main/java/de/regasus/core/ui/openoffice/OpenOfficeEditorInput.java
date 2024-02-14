package de.regasus.core.ui.openoffice;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class OpenOfficeEditorInput extends AbstractEditorInput<Long>{


	private String extension;

	private String docType;
	
	private String extFileName;


	public OpenOfficeEditorInput(DataStoreVO dataStoreVO) {
		this.key = dataStoreVO.getID();
		this.extension = dataStoreVO.getExtension();
		this.extFileName = dataStoreVO.getExtFileName();
		this.docType = dataStoreVO.getDocType();
		this.toolTipText = dataStoreVO.getSmallInfo();
	}


	public boolean exists() {
		return key != null;
	}


	public ImageDescriptor getImageDescriptor() {
		String imageKey = getImageKey();
		if (imageKey != null) {
			return IconRegistry.getImageDescriptor(imageKey);
		}
		return null;

	}



	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}





	public String getImageKey() {
		if ("odt".equals(extension)) {
			return IImageKeys.SWRITER;
		}
		else if ("ods".equals(extension)) {
			return IImageKeys.SCALC;
		}
		else {
			return null;
		}
	}


	/**
	 * @return the extension
	 */
	public String getExtension() {
		return extension;
	}


	/**
	 * @return the docType
	 */
	public String getDocType() {
		return docType;
	}


	/**
	 * @return the extFileName
	 */
	public String getExtFileName() {
		return extFileName;
	}


}
