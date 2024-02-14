package com.lambdalogic.util.rcp.i18n;

import java.util.Map;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.util.TypeHelper;

public class I18NLabelProvider extends BaseLabelProvider implements ILabelProvider {

	public I18NLabelProvider() {
		super();
	}
	

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns <code>null</code>.
	 * Subclasses may override.
	 */
	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		Object value = null;
		if (element instanceof Map.Entry) {
			value = ((Map.Entry<?,?>) element).getValue();
		}
		else {
			value = element;
		}

		if (value == null) {
			return "";
		}
		else if (value instanceof I18NString) {
			I18NString i18nString = (I18NString) value;
			return i18nString.getString();
		}
		else {
			return TypeHelper.toString(value);
		}
	}

}
