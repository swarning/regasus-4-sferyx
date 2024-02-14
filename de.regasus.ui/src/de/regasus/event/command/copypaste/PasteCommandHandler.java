package de.regasus.event.command.copypaste;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.util.rcp.ClassKeyNameTransfer;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.view.EventTreeNode;
import de.regasus.ui.Activator;

/**
 * Tries to read from the Clipboard identifying data of VOs and CVOs to be pasted in the tree of the
 * EventeMasterDataView, and if there are some, tells the appropriate model to actually perform the copy on the server.
 */
public class PasteCommandHandler extends AbstractHandler {

	private Map<Class<?>, PastePerformer> classToPasteHandlerMap = new HashMap<>();
	{
		classToPasteHandlerMap.put(HotelCancelationTermVO.class,      new HotelCancelationTermPastePerformer());
		classToPasteHandlerMap.put(HotelContingentVO.class,           new HotelContingentPastePerformer());
		classToPasteHandlerMap.put(HotelContingentCVO.class,          new HotelContingentPastePerformer());
		classToPasteHandlerMap.put(HotelOfferingVO.class,             new HotelOfferingPastePerformer());
		classToPasteHandlerMap.put(InvoiceNoRangeCVO.class,           new InvoiceNoRangePastePerformer());

		classToPasteHandlerMap.put(ParticipantCustomFieldGroup.class, new ParticipantCustomFieldGroupPastePerformer());
		classToPasteHandlerMap.put(ParticipantCustomField.class,      new ParticipantCustomFieldPastePerformer());

		classToPasteHandlerMap.put(ProfileCustomFieldGroup.class,     new ProfileCustomFieldGroupPastePerformer());
		classToPasteHandlerMap.put(ProfileCustomField.class,          new ProfileCustomFieldPastePerformer());

		classToPasteHandlerMap.put(ProgrammeCancelationTermVO.class,  new ProgrammeCancelationTermPastePerformer());
		classToPasteHandlerMap.put(ProgrammeOfferingVO.class,         new ProgrammeOfferingPastePerformer());
		classToPasteHandlerMap.put(ProgrammePointVO.class,            new ProgrammePointPastePerformer());
		classToPasteHandlerMap.put(RoomDefinitionVO.class,            new RoomDefinitionPastePerformer());
		classToPasteHandlerMap.put(WorkGroupVO.class,                 new WorkGroupPastePerformer());
	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell activeShell = HandlerUtil.getActiveShell(event);
			Display display = activeShell.getDisplay();

			ISelection selection = HandlerUtil.getCurrentSelection(event);
			TreeNode<?> targetTreeNode = SelectionHelper.getUniqueSelected(selection);

			String[] classesAndKeys = getClassesAndKeysFromClipboard();

			// Beep if nothing is there to be pasted
			if (classesAndKeys != null && classesAndKeys.length > 0) {
				// Fetch subsequently from the array the class name and the key, to paste each "copied" object
				// individually
				for (int i = 0; i < classesAndKeys.length; i += 3) {
					String className = classesAndKeys[i];
					Long id = Long.valueOf(classesAndKeys[i+1]);
					String name = classesAndKeys[i+2];

					Class<?> clazz = Class.forName(className);
					PastePerformer handler = classToPasteHandlerMap.get(clazz);
					boolean pasteSuccess = (handler == null) ? false : handler.perform(targetTreeNode, id);
					if (!pasteSuccess) {
						String message = I18N.PasteOfObjectToNodeNotAllowed;
						message = message.replace("<object>", name);
						message = message.replace("<node>", targetTreeNode.getText());
						MessageDialog.openWarning(activeShell, UtilI18N.Warning, message);
					}
				}
			}
			else {
				display.beep();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

	/**
	 * Walk the tree nodes up until EventTreeNode is found, return it's value
	 */
	private static EventVO findEventVOInAncesters(TreeNode<?> treeNode) {
		while (treeNode != null) {
			if (treeNode instanceof EventTreeNode) {
				EventTreeNode eventTreeNode = (EventTreeNode) treeNode;
				return eventTreeNode.getValue();
			}
			treeNode = treeNode.getParent();
		}
		return null;
	}

	/**
	 * Walk the tree nodes up until EventTreeNode is found, return it's key
	 */
	public static Long findEventPKInAncesters(TreeNode<?> treeNode) {
		EventVO eventVO = findEventVOInAncesters(treeNode);
		if (eventVO != null) {
			return eventVO.getPK();
		}
		else {
			return null;
		}
	}


	/**
	 * Handler is enabled when clipboard contains data
	 */
	@Override
	public boolean isEnabled() {
		String[] classesAndKeys = getClassesAndKeysFromClipboard();
		boolean enabled = classesAndKeys != null && classesAndKeys.length > 0;
		return enabled;
	}


	private String[] getClassesAndKeysFromClipboard() {
		String[] classesAndKeys = null;
		try {
			classesAndKeys = (String[]) ClipboardHelper.readFromClipboard( ClassKeyNameTransfer.getInstance() );
			if (classesAndKeys == null) {
				classesAndKeys = new String[0];
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return classesAndKeys;
	}

}
