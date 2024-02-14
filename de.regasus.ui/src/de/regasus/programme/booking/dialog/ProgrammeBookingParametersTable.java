package de.regasus.programme.booking.dialog;

import java.math.BigDecimal;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.programme.WorkGroupModel;
import de.regasus.ui.Activator;

enum ProgrammeBookingParametersColumns {
	PARTICIPANT_NR, 
	PARTICIPANT, 
	INVOICE_RECIPIENT, 
	PROGRAMME_POINT_NAME, 
	OFFERING_DESC, 
	WORKGROUP,
	COUNT, 
	TOTAL_AMOUNT 
};

/**
 * A table that shows for participants, what bookings of programme points and offerings are to be done soon.
 * <p>
 * {@link https://mi2.lambdalogic.de/jira/browse/MIRCP-104 }
 * 
 * @author manfred
 * 
 */
public class ProgrammeBookingParametersTable extends
	SimpleTable<ProgrammeBookingParameter, ProgrammeBookingParametersColumns> {

	public ProgrammeBookingParametersTable(Table table) {
		super(table, ProgrammeBookingParametersColumns.class);
	}


	@Override
	public String getColumnText(
		ProgrammeBookingParameter pbp,
		ProgrammeBookingParametersColumns column
	) {
		try {
    		switch (column) {
    			case PARTICIPANT:
    				return pbp.benefitRecipient.getName();

    			case PARTICIPANT_NR:
    				return String.valueOf(pbp.benefitRecipient.getNumber());
        			
    			case INVOICE_RECIPIENT:
    				return pbp.invoiceRecipient.getName();

    			case PROGRAMME_POINT_NAME: {
    				I18NString programmePointName = pbp.programmePointName;
    				if (programmePointName != null) {
    					String ppName = programmePointName.getString();
    					ppName = StringHelper.removeLineBreaks(ppName);
    					return ppName;
    				}
    				break;
    			}
    			
    			case OFFERING_DESC:
    				I18NString description = pbp.programmeOfferingVO.getDescription();
    				if (description != null) {
    					String desc = description.getString();
    					desc = StringHelper.replace(desc, '\n', ' ');
    					desc = desc.trim();
    					return desc;
    				}
    				break;
        			
    			case WORKGROUP: {
    				Long workGroupPK = pbp.workGroupPK;
    				if (workGroupPK  != null) {
    					try {
    						if (workGroupPK.equals(ProgrammeBookingParameter.AUTO_WORK_GROUP)) {
    							return ParticipantLabel.AUTO.getString();
    						} 
    						else {
    							WorkGroupModel workGroupModel = WorkGroupModel.getInstance();
    							WorkGroupVO workGroupVO = workGroupModel.getWorkGroupVO(workGroupPK);
    							return workGroupVO.getName();
    						}
    					}
    					catch (Exception e) {
    						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
    						return "*** " + UtilI18N.Error + " ***";
    					}
    				}
    			}

    			case COUNT:
    				return String.valueOf(pbp.count);

    			case TOTAL_AMOUNT: {
    				ProgrammeOfferingVO poVO = pbp.programmeOfferingVO;
    				
    				/* The initial value for the total amount is taken from pbp, because the user might 
    				 * have edited the value in the table. Only if it is null, take the main price
    				 * from the offering.
    				 */
    				BigDecimal totalAmount;
    				if (pbp.amount != null) {
    					totalAmount = pbp.amount;
    				}
    				else {
    					totalAmount = poVO.getMainPrice().getAmountGross();
    				}
    				
    				
    				// add additional price 1
    				if (poVO.isWithAdd1Price()) {
    					BigDecimal addAmount = poVO.getAdd1PriceVO().getAmountGross();
    					totalAmount = totalAmount.add(addAmount);
    				}
    
    				// add additional price 2
    				if (poVO.isWithAdd2Price()) {
    					BigDecimal addAmount = poVO.getAdd2PriceVO().getAmountGross();
    					totalAmount = totalAmount.add(addAmount);
    				}
    
    				// multiply with number of bookings
    				if (pbp.count != null && pbp.count.intValue() != 1) {
    					BigDecimal countAsBD = BigDecimal.valueOf(pbp.count);
    					totalAmount = totalAmount.multiply(countAsBD);
    				}
    				
    				// build CurrencyAmount to format
    				CurrencyAmount currencyAmount = new CurrencyAmount(totalAmount, pbp.currency);
    				
    				return currencyAmount.format(false, false);
    			}

    			default:
    				return "";
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return "";
	}
	
	
	@Override
	public Comparable<? extends Object> getColumnComparableValue(
		ProgrammeBookingParameter programmeBooking,
		ProgrammeBookingParametersColumns column) {

		switch (column) {
    		case PARTICIPANT_NR:
    			return programmeBooking.benefitRecipient.getNumber();

    		case TOTAL_AMOUNT:
    			return programmeBooking.amount;
    		
    		case COUNT:
    			return programmeBooking.count;
    			
    		default:
    			return super.getColumnComparableValue(programmeBooking, column);
		}
	}
	
}
