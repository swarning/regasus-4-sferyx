package de.regasus.finance.invoicenumberrange.combo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.finance.InvoiceNoRangeModel;


@SuppressWarnings("rawtypes")
public class InvoiceNoRangeCombo extends AbstractComboComposite<InvoiceNoRangeCVO> implements CacheModelListener {

	private static final InvoiceNoRangeCVO EMPTY_INVOICE_NO_RANGE;

	static {
		EMPTY_INVOICE_NO_RANGE = new InvoiceNoRangeCVO();
		EMPTY_INVOICE_NO_RANGE.setVO(new InvoiceNoRangeVO());
	}

	// Model
	private InvoiceNoRangeModel model;

	// *************************************************************************
	// * Constructors
	// *

	/**
	 * Show all invoice number ranges
	 */
	public InvoiceNoRangeCombo(Composite parent, int style) throws Exception {
		this(parent, SWT.NONE, null);
	}

	/**
	 * Show the invoice number ranges for a particular event
	 */
	public InvoiceNoRangeCombo(Composite parent, int style, Long eventPK) throws Exception {
		super(parent, SWT.NONE, eventPK);
	}

	/**
	 * Constructor to show initially no entries.
	 * The parameter noEntries must be set to true. Otherwise all invoice no ranges are shown.
	 */
	public InvoiceNoRangeCombo(Composite parent, int style, boolean noEntries) throws Exception {
		super(
			parent,
			SWT.NONE,
			noEntries ? NO_DATA_MODEL_DISCRIMINATOR : null
		);
	}

	@Override
	protected InvoiceNoRangeCVO getEmptyEntity() {
		return EMPTY_INVOICE_NO_RANGE;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				InvoiceNoRangeCVO invoiceNoRangeCVO = (InvoiceNoRangeCVO) element;
				return StringHelper.avoidNull(invoiceNoRangeCVO.getVO().getName());
			}
		};
	}


	@Override
	protected Collection<InvoiceNoRangeCVO> getModelData() throws Exception {
		Collection<InvoiceNoRangeCVO> tmpModelData;

		if (modelDataDiscriminator != null) {
			Long eventPK = (Long) modelDataDiscriminator;
			tmpModelData = model.getInvoiceNoRangeCVOsByEventPK(eventPK);
		}
		else  {
			tmpModelData = model.getAllInvoiceNoRangeCVOs();
		}

		List<InvoiceNoRangeCVO> modelData = new ArrayList<>(tmpModelData);

		return modelData;
	}


	@Override
	protected void initModel() {
		model = InvoiceNoRangeModel.getInstance();

		if (modelDataDiscriminator == null) {
			model.addListener(this);
		}
		else if (modelDataDiscriminator != NO_DATA_MODEL_DISCRIMINATOR) {
			model.addForeignKeyListener(this, modelDataDiscriminator);
		}
	}


	@Override
	protected void disposeModel() {
		if (modelDataDiscriminator == null) {
			model.removeListener(this);
		}
		else if (modelDataDiscriminator != NO_DATA_MODEL_DISCRIMINATOR) {
			model.removeForeignKeyListener(this, modelDataDiscriminator);
		}
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getInvoiceNoRangePK() {
		Long invoiceNoRangePK = null;
		if (entity != null) {
			invoiceNoRangePK = entity.getPK();
		}
		return invoiceNoRangePK;
	}


	public void setInvoiceNoRangeByPK(Long invoiceNoRangePK) {
		InvoiceNoRangeCVO invoiceNoRangeCVO = null;
		if (invoiceNoRangePK != null) {
			try {
				invoiceNoRangeCVO = model.getInvoiceNoRangeCVO(invoiceNoRangePK);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(invoiceNoRangeCVO);
	}


	public void setEventPK(Long eventPK) {
		try {
			if (eventPK == null && modelDataDiscriminator != null
				||
				eventPK != null && ! eventPK.equals(modelDataDiscriminator)
			) {
				// save old modelDataDiscriminator (eventPK)
				Object oldEventPK = modelDataDiscriminator;

				// register at the model before getting its data, so the data will be put to the models cache
				if (eventPK == null) {
					model.addListener(this);
				}
				else {
					model.addForeignKeyListener(this, eventPK);
				}

				// set the new modelDataDiscriminator (eventPK), get the data
				setModelDataDiscriminator(eventPK);


				if (oldEventPK == null) {
					model.removeListener(this);
				}
				else if (oldEventPK != NO_DATA_MODEL_DISCRIMINATOR) {
					model.removeForeignKeyListener(this, oldEventPK);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
