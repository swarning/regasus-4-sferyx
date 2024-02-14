package de.regasus.participant.editor.finance;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.hotel.data.HotelInvoicePositionType;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.BookingCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.rcp.Images;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

/**
 * A label provider for the first column, which is to show the invoices and their positions
 * 
 * @author manfred
 * 
 */
public class InvoicesAndPositionsColumnLabelProvider extends ColumnLabelProvider {

	private ClassLoader invoicePositionClassLoader = InvoicePositionVO.class.getClassLoader();
	
	private FormatHelper formatHelper = FormatHelper.getDefaultLocaleInstance();

	private ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

	private ParticipantModel participantModel;
	
	private AccountancyHelper accountancyHelper;
	

	public InvoicesAndPositionsColumnLabelProvider(AccountancyHelper accountancyHelper) {
		this.accountancyHelper = accountancyHelper;
		
		participantModel = ParticipantModel.getInstance();
	}


	@Override
	public Image getImage(Object element) {
		if (element instanceof InvoiceVO) {
			return sharedImages.getImage(ISharedImages.IMG_OBJ_FILE);
		}
		else if (element instanceof String) {
			return Images.get(Images.SUM);
		}
		else if (element instanceof UnbalancedRowIndicator) {
			return Images.get(Images.EURO);
		}
		else {
			return null;
		}
	}


	@Override
	public String getText(Object element) {
		try {
			if (element instanceof InvoiceVO) {
				InvoiceVO invoiceVO = (InvoiceVO) element;
				StringBuilder sb = new StringBuilder();

				// prepend with "Invoice" or "Credit"
				if (invoiceVO.isInvoice()) {
					sb.append(InvoiceLabel.Invoice.getString());
				}
				else {
					sb.append(InvoiceLabel.Credit.getString());
				}
				sb.append(" ");
				
				if (invoiceVO.getNumber() != null) {
					String numberPrefix = invoiceVO.getNumberPrefix();
					if (numberPrefix != null) {
						sb.append(invoiceVO.getNumberPrefix());
					}
					sb.append(invoiceVO.getNumber());
					sb.append(" - ");
					sb.append(formatHelper.formatDate(invoiceVO.getInvoiceDate()));
				}
				if (!invoiceVO.isClosed()) {
					sb.append(" (");
					sb.append(InvoiceLabel.NotClosed.getString());
					sb.append(")");
				}
				return sb.toString();
			}
			else if (element instanceof InvoicePositionVO) {
				InvoicePositionVO invoicePositionVO = (InvoicePositionVO) element;
				StringBuilder sb = new StringBuilder();
				I18NPattern description = invoicePositionVO.getDescription();
				description.setClassLoader(invoicePositionClassLoader);
				sb.append(description.getString());

				// Find the Benefit Recipient from somewhere
				Long bookingPK = invoicePositionVO.getBookingPK();
				BookingCVO bookingCVO = accountancyHelper.getBookingCVO(bookingPK);
				/* bookingCVO is null if the recipient of the invoice in not the invoice 
				 * recipient of the booking anymore!
				 */
				if (bookingCVO != null && 
					!HotelInvoicePositionType.BREAKFAST.getDbKey().equals(invoicePositionVO.getBookingType())
				) {
					List<Long> benefitRecipientPKs = bookingCVO.getBookingVO().getBenefitRecipientPKs();
					List<Participant> participants = participantModel.getParticipants(benefitRecipientPKs);
					
					sb.append(" (");
					
					int i = 0;
					for (Participant participant : participants) {
						if (i++ > 0) {
							sb.append(", ");
						}
						sb.append(participant.getName());
					}
					
					sb.append(")");
				}
				return sb.toString();
			}
			else if (element instanceof String) {
				StringBuilder sb = new StringBuilder();
				sb.append(InvoiceLabel.Total.getString());
				sb.append(" (");
				sb.append( CurrencyAmount.getSymbol((String)element));
				sb.append(")");
				return sb.toString();
			}
			else if (element instanceof UnbalancedRowIndicator) {
				String currency = ((UnbalancedRowIndicator)element).getCurrency();
				StringBuilder sb = new StringBuilder();
				sb.append(InvoiceLabel.Unbalanced.getString());
				sb.append(" (");
				sb.append( CurrencyAmount.getSymbol(currency));
				sb.append(")");
				return sb.toString();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return "";
	}

}
