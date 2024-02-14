package de.regasus.finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO_Name_Comparator;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;

public class InvoiceNoRangeSearchValuesProvider implements SearchValuesProvider {

	private InvoiceNoRangeModel inrModel = InvoiceNoRangeModel.getInstance();

	private Long eventPK;
	private boolean withYesKey = false;


	public InvoiceNoRangeSearchValuesProvider(boolean withYesKey) {
	}


	public InvoiceNoRangeSearchValuesProvider(boolean withYesKey, Long eventPK) {
		this.eventPK = eventPK;
		this.withYesKey = withYesKey;
	}


	@Override
	public LinkedHashMap<Object, Object> getValues() throws Exception {
        List<InvoiceNoRangeCVO> invoiceNoRangeCVOs = null;
        if (eventPK == null) {
			invoiceNoRangeCVOs = new ArrayList<>( inrModel.getAllInvoiceNoRangeCVOs() );
		}
		else {
			invoiceNoRangeCVOs = new ArrayList<>( inrModel.getInvoiceNoRangeCVOsByEventPK(eventPK) );
		}

        Collections.sort(invoiceNoRangeCVOs, InvoiceNoRangeCVO_Name_Comparator.getInstance());

        LinkedHashMap<Object, Object> values = MapHelper.createLinkedHashMap(invoiceNoRangeCVOs.size() + 1);

        if (withYesKey) {
        	values.put(YES_KEY, KernelLabel.Yes);
        }

        for (InvoiceNoRangeCVO invoiceNoRangeCVO : invoiceNoRangeCVOs) {
			values.put(invoiceNoRangeCVO.getPK(), invoiceNoRangeCVO.getVO().getName());
		}

		return values;
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
	}

}
