package de.regasus.email.dispatchorder.view;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.email.DispatchOrderStatus;
import com.lambdalogic.messeinfo.email.DispatchStatus;
import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;

/**
 * A class used by the EmailDispatchOrderTreeTable to provide for the three kinds of objects different labels:
 *
 * <ul>
 * <li>{@link EmailTemplate}: the name</li>
 * <li>{@link EmailDispatchOrder}: the creation time, a summery of the {@link DispatchStatus}s and, if appropriate, the schedule date</li>
 * <li>{@link EmailDispatch}: the send date, abstractPersonName, toAddr, status and possibly an error message</li>
 * </ul>
 */
public class EmailDispatchOrderTreeLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	private FormatHelper formatHelper = new FormatHelper();

	@Override
	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof EmailTemplate) {
			EmailTemplate template = (EmailTemplate) element;
			switch (columnIndex) {
				case 0:
					return template.getName();
			}
		}
		else if (element instanceof EmailDispatchOrder) {
			EmailDispatchOrder order = (EmailDispatchOrder) element;
			switch (columnIndex) {
				case 0:
					return formatHelper.formatDateTime( order.getNewTime() );
				case 2:
					return String.valueOf( order.getStatus() );
				case 3: {
					if (order.getStatus() == DispatchOrderStatus.SCHEDULED) {
						return EmailLabel.DispatchScheduledFor.getString() + formatHelper.formatDateTime(order.getScheduledDispatchDate());
					}
					else {
						return order.getStatusCountSummary();
					}
				}
			} // switch
		}
		else if (element instanceof EmailDispatch) {
			EmailDispatch dispatch = (EmailDispatch) element;
			switch (columnIndex) {
				case 0:
					return dispatch.getToAddr();
				case 1:
					return dispatch.getAbstractPersonName();
				case 2:
					return dispatch.getStatus().toString();
				case 3:
					if (! StringHelper.isEmpty(dispatch.getErrorMessage()) ) {
						return dispatch.getErrorMessage();
					}
					else {
						if (dispatch.getStatus() == DispatchStatus.SUCCESS) {
							return EmailLabel.Dispatch.getString() + " " + formatHelper.format(dispatch.getSendDate());
						}
					}
			} // switch
		}
		return "";
	}


	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

}
