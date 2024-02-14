package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.regasus.core.model.MICacheModel;

public class PaymentSystemSetupModel extends MICacheModel<Long, PaymentSystemSetup> {

	private static PaymentSystemSetupModel singleton = null;


	private PaymentSystemSetupModel() {
		super();
	}


	public static PaymentSystemSetupModel getInstance() {
		if (singleton == null) {
			singleton = new PaymentSystemSetupModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(PaymentSystemSetup paymentSystemSetup) {
		PaymentSystem fk = null;
		if (paymentSystemSetup != null) {
			fk = paymentSystemSetup.getPaymentSystem();
		}
		return fk;
	}


	@Override
	protected Long getKey(PaymentSystemSetup entity) {
		return entity.getId();
	}


	@Override
	protected PaymentSystemSetup getEntityFromServer(Long id) throws Exception {
		System.out.println(getClass().getName() + ".getEntityFromServer()");

		PaymentSystemSetup payEngineSetup = getPaymentSystemSetupMgr().read(id);
		return payEngineSetup;
	}


	public PaymentSystemSetup getPaymentSystemSetup(Long paymentSystemSetupPK) throws Exception {
		return super.getEntity(paymentSystemSetupPK);
	}


	@Override
	protected List<PaymentSystemSetup> getEntitiesFromServer(Collection<Long> pks) throws Exception {
		List<PaymentSystemSetup> paymentSystemSetups = getPaymentSystemSetupMgr().read(pks);
		return paymentSystemSetups;
	}


	public List<PaymentSystemSetup> getPaymentSystemSetups(Collection<Long> paymentSystemSetupIds) throws Exception {
		return super.getEntities(paymentSystemSetupIds);
	}


	public List<PaymentSystemSetup> getPaymentSystemSetups(PaymentSystem paymentSystem) throws Exception {
		// assure that all entities are loaded
		super.getAllEntities();
		return super.getEntityListByForeignKey(paymentSystem);
	}


	@Override
	protected List<PaymentSystemSetup> getAllEntitiesFromServer() throws Exception {
		List<PaymentSystemSetup> payEngineSetups = null;

		if (serverModel.isLoggedIn()) {
			payEngineSetups = getPaymentSystemSetupMgr().readAll();
		}
		else {
			payEngineSetups = Collections.emptyList();
		}

		return payEngineSetups;
	}


	public Collection<PaymentSystemSetup> getAllPaymentSystemSetups() throws Exception {
		return super.getAllEntities();
	}


	@Override
	protected PaymentSystemSetup createEntityOnServer(PaymentSystemSetup paymentSystemSetup) throws Exception {
		paymentSystemSetup.validate();
		PaymentSystemSetup newPayEngineSetup = getPaymentSystemSetupMgr().create(paymentSystemSetup);
		return newPayEngineSetup;
	}


	@Override
	public PaymentSystemSetup create(PaymentSystemSetup paymentSystemSetup) throws Exception {
		return super.create(paymentSystemSetup);
	}

	@Override
	protected PaymentSystemSetup updateEntityOnServer(PaymentSystemSetup paymentSystemSetup) throws Exception {
		paymentSystemSetup.validate();
		paymentSystemSetup = getPaymentSystemSetupMgr().update(paymentSystemSetup);
		return paymentSystemSetup;
	}


	@Override
	public PaymentSystemSetup update(PaymentSystemSetup paymentSystemSetup) throws Exception {
		return super.update(paymentSystemSetup);
	}


	@Override
	protected void deleteEntityOnServer(PaymentSystemSetup paymentSystemSetup) throws Exception {
		if (paymentSystemSetup != null) {
			Long id = paymentSystemSetup.getId();
			getPaymentSystemSetupMgr().delete(id);
		}
	}


	@Override
	public void delete(PaymentSystemSetup paymentSystemSetup) throws Exception {
		super.delete(paymentSystemSetup);
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<PaymentSystemSetup> paymentSystemSetups) throws Exception {
		if (paymentSystemSetups != null) {
			List<Long> pks = PaymentSystemSetup.getPrimaryKeyList(paymentSystemSetups);
			getPaymentSystemSetupMgr().delete(pks);
		}
	}


	@Override
	public void delete(Collection<PaymentSystemSetup> paymentSystemSetups) throws Exception {
		super.delete(paymentSystemSetups);
	}

}
