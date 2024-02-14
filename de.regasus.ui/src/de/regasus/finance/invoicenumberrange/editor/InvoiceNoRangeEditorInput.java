package de.regasus.finance.invoicenumberrange.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;


public class InvoiceNoRangeEditorInput
extends AbstractEditorInput<Long>
implements ILinkableEditorInput, EventIdProvider {

	private Long eventPK = null;


	private InvoiceNoRangeEditorInput() {
	}


	public static InvoiceNoRangeEditorInput getEditInstance(Long invoiceNoRangePK, Long eventPK) {
		InvoiceNoRangeEditorInput invoiceNoRangeEditorInput = new InvoiceNoRangeEditorInput();
		invoiceNoRangeEditorInput.key = invoiceNoRangePK;
		invoiceNoRangeEditorInput.eventPK = eventPK;
		return invoiceNoRangeEditorInput;
	}


	public static InvoiceNoRangeEditorInput getCreateInstance(Long eventPK) {
		InvoiceNoRangeEditorInput invoiceNoRangeEditorInput = new InvoiceNoRangeEditorInput();
		invoiceNoRangeEditorInput.eventPK = eventPK;
		return invoiceNoRangeEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.INVOICERANGE);
	}


	@Override
	public Class<?> getEntityType() {
		return InvoiceNoRangeVO.class;
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}


	/**
	 * @param eventPK the eventPK to set
	 */
	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
	}

}
