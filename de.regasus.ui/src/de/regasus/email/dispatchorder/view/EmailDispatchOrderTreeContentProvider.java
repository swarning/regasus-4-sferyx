package de.regasus.email.dispatchorder.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchComparator;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailDispatchOrderComparator;
import com.lambdalogic.messeinfo.email.EmailTemplate;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailDispatchModel;
import de.regasus.email.EmailDispatchOrderModel;
import de.regasus.email.EmailTemplateModel;
import de.regasus.ui.Activator;

/**
 * A content provider for a tree who is to show the following three-level structure:
 *
 * <pre>
 *  +-{@link EmailTemplate}
 *  |  +-{@link EmailDispatchOrder}
 *  |      +-{@link EmailDispatch}
 *  |      +-{@link EmailDispatch}
 *  |  +-{@link EmailDispatchOrder}
 *  |      +-{@link EmailDispatch}
 *  |      +-{@link EmailDispatch}
 *  +-{@link EmailTemplate}
 *  |  +-{@link EmailDispatchOrder}
 *  |      +-{@link EmailDispatch}
 * </pre>
 *
 * @author manfred
 *
 */
public class EmailDispatchOrderTreeContentProvider implements ITreeContentProvider {

	private static Object[] EMPTY_ARRAY = new Object[0];

	// models
	private EmailTemplateModel emailTemplateSearchModel = EmailTemplateModel.getInstance();
	private EmailDispatchOrderModel emailDispatchOrderModel = EmailDispatchOrderModel.getInstance();
	private EmailDispatchModel emailDispatchModel = EmailDispatchModel.getInstance();


	/**
	 * Gives {@link EmailDispatchOrder}s as children of {@link EmailTemplate}, and gives {@link EmailDispatch}
	 * es as children of {@link EmailDispatchOrder}.
	 *
	 * @param parentElement
	 * @return
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		try {
			if (parentElement instanceof EmailTemplate) {
				EmailTemplate emailTemplateSearchData = (EmailTemplate) parentElement;
				Long emailTemplateID = emailTemplateSearchData.getID();
				List<EmailDispatchOrder> emailDispatchesOrders = emailDispatchOrderModel.getEmailDispatchOrdersByEmailTemplate(emailTemplateID);

				// order by newTime and id
				emailDispatchesOrders = new ArrayList<>(emailDispatchesOrders);
				Collections.sort(emailDispatchesOrders, EmailDispatchOrderComparator.getInstance());

				return emailDispatchesOrders.toArray();
			}
			else if (parentElement instanceof EmailDispatchOrder) {
				EmailDispatchOrder emailDispatchOrder = (EmailDispatchOrder) parentElement;
				List<EmailDispatch> emailDispatches = emailDispatchModel.getEmailDispatchesByEmailDispatchOrder( emailDispatchOrder.getID() );

				// order by newTime and id
				emailDispatches = new ArrayList<>(emailDispatches);
				Collections.sort(emailDispatches, EmailDispatchComparator.getInstance());

				return emailDispatches.toArray();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return EMPTY_ARRAY;
	}


	@Override
	public Object getParent(Object element) {
		try {
			if (element instanceof EmailDispatch) {
				EmailDispatch emailDispatch = (EmailDispatch) element;
				return emailDispatchOrderModel.getEmailDispatchOrder(emailDispatch.getEmailDispatchOrderPK());
			}
			else if (element instanceof EmailDispatchOrder) {
				EmailDispatchOrder emailDispatchOrder = (EmailDispatchOrder) element;
				return emailTemplateSearchModel.getEmailTemplateSearchData(emailDispatchOrder.getEmailTemplatePK());
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		return null;
	}


	/**
	 * {@link EmailDispatch}es are the deepest elements, they never have any children. For the others, we say yes at
	 * first, and the viewer is clever enough to remove the + sign in case we actually don't deliver any children.
	 */
	@Override
	public boolean hasChildren(Object element) {
		return !(element instanceof EmailDispatch);
	}


	/**
	 * We expect that the viewer got the list of {@link EmailTemplate}s as inputElement.
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			return ((List<?>) inputElement).toArray();
		}
		else {
			return EMPTY_ARRAY;
		}
	}


	@Override
	public void dispose() {
	}


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
