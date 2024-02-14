package de.regasus.report.wizard.participant.list;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.lambdalogic.i18n.I18NCollator;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.util.rcp.tree.TreeNode;

public class SQLFieldComparator extends ViewerComparator {

	private I18NCollator collator = new I18NCollator();

	public int compare(Viewer viewer, Object e1, Object e2) {
		TreeNode<?> treeNode1 = (TreeNode<?>) e1;
		TreeNode<?> treeNode2 = (TreeNode<?>) e2;
		
		Object o1 = treeNode1.getValue();
		Object o2 = treeNode2.getValue();
		
		if (o1 instanceof SQLDirectory) {
			if (o2 instanceof SQLDirectory) {
				String label1 = ((SQLDirectory) o1).getLabel();
				String label2 = ((SQLDirectory) o2).getLabel();
				return collator.compare(label1, label2);
			}
			else if (o2 instanceof SQLField) {
				return 1;
			}
		}
		else if (o1 instanceof SQLField) {
			if (o2 instanceof SQLDirectory) {
				return -1;
			}
			else if (o2 instanceof SQLField) {
				String label1 = ((SQLField) o1).getLabel();
				String label2 = ((SQLField) o2).getLabel();
				return collator.compare(label1, label2);
			}
		}
		return collator.compare(e1, e2);
	}
}
