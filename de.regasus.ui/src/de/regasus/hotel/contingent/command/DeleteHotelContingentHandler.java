package de.regasus.hotel.contingent.command;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.view.HotelContingentTreeNode;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.ui.Activator;

public class DeleteHotelContingentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			boolean deleteOK = MessageDialog.openConfirm(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Question,
				I18N.DeleteHotelContingentAction_Confirmation
			);

			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						Iterator<?> iterator = currentSelection.iterator();
						while (iterator.hasNext()) {
							Object object = iterator.next();

							// If you can find out what Programme Offering to delete, do it.
							if (object instanceof HotelContingentTreeNode) {
								HotelContingentTreeNode node = (HotelContingentTreeNode) object;
								HotelContingentCVO hotelContingentCVO = node.getValue();
								try {
									HotelContingentModel.getInstance().delete(hotelContingentCVO);
								}
								catch (Exception e) {
									RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
								}
							}
						}
					}

				});
			}
		}
		return null;
	}

}
