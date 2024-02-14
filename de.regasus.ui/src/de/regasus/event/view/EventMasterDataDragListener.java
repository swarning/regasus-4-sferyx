package de.regasus.event.view;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.ClassKeyNameTransfer;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.event.customfield.editor.ParticipantCustomFieldEditor;
import de.regasus.event.customfield.editor.ParticipantCustomFieldGroupEditor;
import de.regasus.event.editor.EventEditor;
import de.regasus.hotel.contingent.editor.HotelContingentEditor;
import de.regasus.portal.Page;
import de.regasus.portal.page.editor.PageEditor;
import de.regasus.programme.offering.editor.ProgrammeOfferingEditor;
import de.regasus.programme.programmepoint.editor.ProgrammePointEditor;

public class EventMasterDataDragListener extends DragSourceAdapter {

	private StructuredViewer viewer;


	public EventMasterDataDragListener(StructuredViewer viewer) {
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

		if (selectedTreeNode instanceof EventTreeNode) {
			if ( EventEditor.isEditorsSaved(EventEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragEvent);
			}
		}
		else if (selectedTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
			if ( ParticipantCustomFieldGroupEditor.isEditorsSaved(ParticipantCustomFieldGroupEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragCustomFieldGroup);
			}
		}
		else if (selectedTreeNode instanceof ParticipantCustomFieldTreeNode) {
			if ( ParticipantCustomFieldEditor.isEditorsSaved(ParticipantCustomFieldEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragCustomField);
			}
		}
		else if (selectedTreeNode instanceof PageTreeNode) {
			if ( PageEditor.isEditorsSaved(PageEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragPage);
			}
		}
		else if (selectedTreeNode instanceof ProgrammePointTreeNode) {
			if ( ProgrammePointEditor.isEditorsSaved(ProgrammePointEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragProgrammePoint);
			}
		}
		else if (selectedTreeNode instanceof ProgrammeOfferingTreeNode) {
			if ( ProgrammeOfferingEditor.isEditorsSaved(ProgrammeOfferingEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragProgrammeOffering);
			}
		}
		else if (selectedTreeNode instanceof HotelContingentTreeNode) {
			if ( HotelContingentEditor.isEditorsSaved(HotelContingentEditor.class) ) {
				event.doit = true;
			}
			else {
				SWTHelper.showInfoDialog(UtilI18N.Info, I18N.SafeEditorBeforeDragHotelContingent);
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
		String name = "";

		if (treeNode instanceof EventTreeNode) {
			EventTreeNode eventTreeNode = (EventTreeNode) treeNode;
			EventVO eventVO = eventTreeNode.getValue();

			className = eventVO.getClass().getName();
			id = eventVO.getID().toString();

			LanguageString ls = eventVO.getName();
			if (ls != null) {
				name = ls.getString();
			}
		}
		else if (treeNode instanceof ParticipantCustomFieldGroupTreeNode) {
			ParticipantCustomFieldGroupTreeNode cfGrpTreeNode = (ParticipantCustomFieldGroupTreeNode) treeNode;
			ParticipantCustomFieldGroup cfGrp = cfGrpTreeNode.getValue();

			className = cfGrp.getClass().getName();
			id = cfGrp.getID().toString();

			LanguageString ls = cfGrp.getName();
			if (ls != null) {
				name = ls.getString();
			}
		}
		else if (treeNode instanceof ParticipantCustomFieldTreeNode) {
			ParticipantCustomFieldTreeNode cfTreeNode = (ParticipantCustomFieldTreeNode) treeNode;
			ParticipantCustomField cf = cfTreeNode.getValue();

			className = cf.getClass().getName();
			id = cf.getID().toString();
			name = cf.getName();
		}
		else if (treeNode instanceof PageTreeNode) {
			PageTreeNode pageTreeNode = (PageTreeNode) treeNode;
			Page page = pageTreeNode.getValue();

			className = page.getClass().getName();
			id = page.getId().toString();

			LanguageString ls = page.getName();
			if (ls != null) {
				name = ls.getString();
			}
		}
		else if (treeNode instanceof ProgrammePointTreeNode) {
			ProgrammePointTreeNode ppTreeNode = (ProgrammePointTreeNode) treeNode;
			ProgrammePointVO ppVO = ppTreeNode.getValue();

			className = ppVO.getClass().getName();
			id = ppVO.getID().toString();

			LanguageString ls = ppVO.getName();
			if (ls != null) {
				name = ls.getString();
			}
		}
		else if (treeNode instanceof ProgrammeOfferingTreeNode) {
			ProgrammeOfferingTreeNode poTreeNode = (ProgrammeOfferingTreeNode) treeNode;
			ProgrammeOfferingVO poVO = poTreeNode.getValue();

			className = poVO.getClass().getName();
			id = poVO.getID().toString();

			LanguageString ls = poVO.getDescription();
			if (ls != null) {
				name = ls.getString();
			}
		}
		else if (treeNode instanceof HotelContingentTreeNode) {
			HotelContingentTreeNode hcTreeNode = (HotelContingentTreeNode) treeNode;
			HotelContingentCVO hcCVO = hcTreeNode.getValue();

			className = hcCVO.getClass().getName();
			id = hcCVO.getPK().toString();
			name = hcCVO.getHcName();
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
