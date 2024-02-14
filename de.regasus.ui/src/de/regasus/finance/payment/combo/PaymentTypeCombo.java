package de.regasus.finance.payment.combo;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.I18NStringComparator;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.finance.PaymentType;


public class PaymentTypeCombo extends AbstractComboComposite<PaymentType> {

	protected List<PaymentType> paymentTypes = null;


	/**
	 * Create PaymentTypeCombo with all {@link PaymentType}s.
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public PaymentTypeCombo(Composite parent, int style) throws Exception {
		super(parent, style);
	}


	/**
	 * Create PaymentTypeCombo with defined {@link PaymentType}s.
	 * @param parent
	 * @param style
	 * @param paymentTypes
	 * @throws Exception
	 */
	public PaymentTypeCombo(Composite parent, int style, List<PaymentType> paymentTypes)
	throws Exception {
		/* This is a misuse of the modelDataDiscriminator,
		 * but it works.
		 * See getModelData() also.
		 */
		super(parent, style, paymentTypes);
	}


	@Override
	protected void disposeModel() {
	}


	@Override
	protected Object getEmptyEntity() {
		return null;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				PaymentType type = (PaymentType) element;
				return type.getString();
			}
		};
	}


	@Override
	protected Collection<PaymentType> getModelData() throws Exception {
		if (paymentTypes == null) {
			if (modelDataDiscriminator != null) {
				paymentTypes = (List<PaymentType>) modelDataDiscriminator;
			}
			else {
				// add only those PaymentTypes the users are allowed to choose manually
				paymentTypes = createArrayList(PaymentType.values().length - 1);
				for (PaymentType paymentType : PaymentType.values()) {
					if (paymentType != PaymentType.CLEARING) {
						paymentTypes.add(paymentType);
					}
				}


				// sort by current language
				Collections.sort(paymentTypes, I18NStringComparator.getInstance());
			}
		}

		return paymentTypes;
	}


	@Override
	protected void initModel() {
	}

}