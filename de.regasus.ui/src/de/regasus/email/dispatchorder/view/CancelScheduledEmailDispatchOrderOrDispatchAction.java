package de.regasus.email.dispatchorder.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.email.DispatchOrderStatus;
import com.lambdalogic.messeinfo.email.DispatchStatus;
import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailLabel;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.email.EmailDispatchModel;
import de.regasus.email.EmailDispatchOrderModel;
import de.regasus.ui.Activator;

/**
 * An action that is enabled when one or more scheduled {@link EmailDispatch}es and 
 * {@link EmailDispatchOrder}s are selected; upon execution the user is asked for confirmation, 
 * after which the entities' ids are given to the model for setting their state to cancelled so 
 * that they aren't got to be dispatched anymore..
 * 
 * @author manfred
 */
public class CancelScheduledEmailDispatchOrderOrDispatchAction extends Action implements
	ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID =
		"de.regasus.email.action.CancelScheduledEmailDispatchOrderOrDispatchAction"; 

	private final IWorkbenchWindow window;

	// currently selected IDs
	private List<Long> selectedEmailDispatchOrderIDs = new ArrayList<Long>();
	private List<Long> selectedEmailDispatchIDs = new ArrayList<Long>();
	
	// models
	private EmailDispatchOrderModel emailDispatchOrderModel = EmailDispatchOrderModel.getInstance();
	private EmailDispatchModel emailDispatchModel = EmailDispatchModel.getInstance();


	public CancelScheduledEmailDispatchOrderOrDispatchAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(UtilI18N.Cancel);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			"icons/cancel.png"));
		setEnabled(false);
		window.getSelectionService().addSelectionListener(this);
	}


	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	/**
	 * Cancels the selected EmailDispatchOrders or EmailDispatches. We can assueme that at least one of the
	 * collections is not empty, so there is something to delete.
	 */
	public void run() {

		boolean pressedOK =
			MessageDialog.openQuestion(
				window.getShell(),
				UtilI18N.Question,
				EmailLabel.ReallyCancelSelectedDispatches.getString());

		if (pressedOK) {
			try {
				emailDispatchModel.cancel(selectedEmailDispatchIDs);
				emailDispatchOrderModel.cancel(selectedEmailDispatchOrderIDs);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	/**
	 * This action is executable when only scheduled dispatches or dispatch orders are selected, and 
	 * there is at least one of such objects.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		boolean enabled = false;
		
		selectedEmailDispatchIDs.clear();
		selectedEmailDispatchOrderIDs.clear();

		if (selection instanceof IStructuredSelection) {

			IStructuredSelection sselection = (IStructuredSelection) selection;
			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof EmailDispatchOrder) {
					EmailDispatchOrder emailDispatchOrder = (EmailDispatchOrder) o;
					if (emailDispatchOrder.getStatus() == DispatchOrderStatus.SCHEDULED
						||
						emailDispatchOrder.getStatus() == DispatchOrderStatus.PROCESSING
						) {
						selectedEmailDispatchOrderIDs.add(emailDispatchOrder.getID());
						enabled = true;
					}
					else {
						enabled = false;
						break;
					}

				}
				else if (o instanceof EmailDispatch) {
					EmailDispatch emailDispatch = (EmailDispatch) o;
					if (emailDispatch.getStatus() == DispatchStatus.SCHEDULED) {
						selectedEmailDispatchIDs.add(emailDispatch.getID());
						enabled = true;
					}
					else {
						enabled = false;
						break;
					}

				}
				else {
					enabled = false;
					break;
				}
			}
			// This action is executable when no dispatch or dispatch order is selected that is not scheduled,
			// and there is at least one scheduled dispatch or dispatch order
			setEnabled(enabled);
		}
	}
}
