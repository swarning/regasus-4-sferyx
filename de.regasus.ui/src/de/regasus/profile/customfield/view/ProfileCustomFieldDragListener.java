package de.regasus.profile.customfield.view;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.util.rcp.ClassKeyNameTransfer;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.profile.customfield.editor.ProfileCustomFieldEditor;
import de.regasus.profile.customfield.editor.ProfileCustomFieldGroupEditor;

public class ProfileCustomFieldDragListener extends DragSourceAdapter {

	private StructuredViewer viewer;


	public ProfileCustomFieldDragListener(StructuredViewer viewer) {
		this.viewer = viewer;
	}


	/**
	 * Signals via doit-flag what kind of nodes may be dragged.
	 * Interrupts the drag operation if any editor of the dragged item is dirty and shows an info dialog instead.
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		Object selectedTreeNode = ((IStructuredSelection) viewer.getSelection()).getFirstElement();

		event.doit = false;

		if (selectedTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
			if ( ProfileCustomFieldGroupEditor.isEditorsSaved(ProfileCustomFieldGroupEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragCustomFieldGroup);
			}
		}
		else if (selectedTreeNode instanceof ProfileCustomFieldTreeNode) {
			if ( ProfileCustomFieldEditor.isEditorsSaved(ProfileCustomFieldEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragCustomField);
			}
		}
	}


	/**
	 * When the dragging user lets the mouse go, the receiving application tells what data type
	 * transfers it wants. Depending on which, either the complete data for a
	 * {@link ClassKeyNameTransfer} is transmitted, or only a string.
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
		Object treeNode = ((IStructuredSelection) viewer.getSelection()).getFirstElement();

		String className = null;
		String id = null;
		String name = null;

		if (treeNode instanceof ProfileCustomFieldGroupTreeNode) {
			ProfileCustomFieldGroupTreeNode cfGrpTreeNode = (ProfileCustomFieldGroupTreeNode) treeNode;
			ProfileCustomFieldGroup cfGrp = cfGrpTreeNode.getValue();

			className = cfGrp.getClass().getName();
			id = cfGrp.getID().toString();

			LanguageString ls = cfGrp.getName();
			if (ls != null) {
				name = ls.getString();
			}
		}
		else if (treeNode instanceof ProfileCustomFieldTreeNode) {
			ProfileCustomFieldTreeNode cfTreeNode = (ProfileCustomFieldTreeNode) treeNode;
			ProfileCustomField cf = cfTreeNode.getValue();

			className = cf.getClass().getName();
			id = cf.getID().toString();

			name = cf.getName();
		}

		/* name is only set if the dragged node has the correct type.
		 * In addition name must not be null for each event.dataType.
		 */
		if (name != null) {
    		if (ClassKeyNameTransfer.getInstance().isSupportedType(event.dataType)) {
    			String[] data = new String[3];
    			data[0] = className;
    			data[1] = id;
    			data[2] = name;

    			event.data = data;
    		}
    		else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
    			event.data = name;
    		}
		}
	}

}
