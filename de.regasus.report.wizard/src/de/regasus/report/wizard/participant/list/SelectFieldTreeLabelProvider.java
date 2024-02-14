package de.regasus.report.wizard.participant.list;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.report.IImageKeys;
import de.regasus.report.IconRegistry;

public class SelectFieldTreeLabelProvider extends LabelProvider {

	public Image getImage(Object element) {
		TreeNode treeNode = (TreeNode) element;
		Object value = treeNode.getValue();
		
		Image image = null;
		if (value instanceof SQLDirectory){
			image = IconRegistry.getImage(IImageKeys.DIRECTORY); 
		}
		else if (value instanceof SQLField) {
			image = IconRegistry.getImage(IImageKeys.SQL_SELECT_FIELD); 
		}
		return image;
	}


	public String getText(Object element) {
		TreeNode treeNode = (TreeNode) element;
		Object value = treeNode.getValue();
		String text;
		if (value instanceof SQLDirectory){
			SQLDirectory sqlDirectory = (SQLDirectory) value;
			text = sqlDirectory.getLabel(); 
		}
		else if (value instanceof SQLField){
			SQLField sqlField = (SQLField) value;
			text =  sqlField.getLabel();
		}
		else{
			text =  super.getText(element);
		}
		return text;
	}
}
