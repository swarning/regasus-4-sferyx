package de.regasus.core.ui.search;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;

public class SQLOperatorLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element) {

		if (element instanceof SQLOperator) {
			return ((SQLOperator) element).getSymbol();
		}
		else {
			return super.getText(element);
		}
	}
}