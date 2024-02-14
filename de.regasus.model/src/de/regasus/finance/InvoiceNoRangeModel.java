package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceNoRangeCVOSettings;
import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceTemplateType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;


/**
 * This model represents a list of all Invoice-No-Ranges and provides
 * CRUD-Methods for them.
 * This model doesn't offer methods for templates. They are offered in the
 * {@link EventInvoiceNoRangeListModel}.
 */
public class InvoiceNoRangeModel extends MICacheModel<Long, InvoiceNoRangeCVO> {

	private static InvoiceNoRangeModel singleton = null;

	private EventModel eventModel;


	private static final InvoiceNoRangeCVOSettings standardSettings;
	private static final InvoiceNoRangeCVOSettings extendedSettings;


	static {
		standardSettings = new InvoiceNoRangeCVOSettings();

		extendedSettings = new InvoiceNoRangeCVOSettings();
		extendedSettings.withInvoiceTemplates = true;
	}


	public static InvoiceNoRangeModel getInstance() {
		if (singleton == null) {
			singleton = new InvoiceNoRangeModel();
			singleton.initModels();
		}
		return singleton;
	}


	private InvoiceNoRangeModel() {
	}


	/**
	 * Initialize references to other Models.
	 * Models are initialized outside the constructor to avoid OutOfMemoryErrors when two Models
	 * reference each other.
	 * This happens because the variable is set after the constructor is finished.
	 * If the constructor calls getInstance() of another Model that calls getInstance() of this Model,
	 * the variable instance is still null. So this Model would be created again and so on.
	 * To avoid this, the constructor has to finish before calling getInstance() of another Model.
	 * The initialization of references to other Models is done in getInstance() right after
	 * the constructor has finished.
	 */
	private void initModels() {
		eventModel = EventModel.getInstance();

		eventModel.addListener(new CacheModelListener<Long>() {
			@Override
			public void dataChange(CacheModelEvent<Long> event) {
				if (!serverModel.isLoggedIn()) {
					return;
				}

				try {
					if (event.getOperation() == CacheModelOperation.DELETE) {

						Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

						for (Long eventPK : event.getKeyList()) {
							for (InvoiceNoRangeCVO invoiceNoRangeCVO : getLoadedAndCachedEntities()) {
								if (eventPK.equals(invoiceNoRangeCVO.getEventPK())) {
									deletedPKs.add(invoiceNoRangeCVO.getPK());
								}
							}

							/* Remove the foreign key whose entity has been deleted from the model before firing the
							 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
							 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
							 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
							 * shall get an empty list.
							 */
							removeForeignKeyData(eventPK);
						}

						if (!deletedPKs.isEmpty()) {
							fireDelete(deletedPKs);
							removeEntities(deletedPKs);
						}
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});


		// if all Invoice No Ranges are loaded: remove Invoice No Ranges of closed Events from Model
		eventModel.addOpenCloseListener(new CacheModelListener<Long>() {
			@Override
			public void dataChange(CacheModelEvent<Long> event) {
				if ( !serverModel.isLoggedIn() || ! isAllEntiesLoaded()) {
					return;
				}

				try {
					Long eventPK = event.getFirstKey();
					EventVO eventVO = eventModel.getEventVO(eventPK);

					if (eventVO.isClosed()) {
						List<Long> deletePKs = new ArrayList<>();
						for (InvoiceNoRangeCVO invoiceNoRangeCVO : getAllEntities()) {
							if (invoiceNoRangeCVO.getEventPK().equals(eventPK)) {
								deletePKs.add( invoiceNoRangeCVO.getPK() );
							}
						}

						if ( ! deletePKs.isEmpty()) {
							removeEntities(deletePKs);
							fireDataChange();
						}
					}
					else {
						refresh();
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	@Override
	protected Long getKey(InvoiceNoRangeCVO entity) {
		return entity.getPK();
	}


	@Override
	protected InvoiceNoRangeCVO getEntityFromServer(Long invoiceNoRangePK) throws Exception {
		System.out.println(getClass().getName() + ".getEntityFromServer()");

		InvoiceNoRangeCVO invoiceNoRangeCVO = getInvoiceNoRangeMgr().getInvoiceNoRangeCVO(
			invoiceNoRangePK,
			standardSettings
		);
		return invoiceNoRangeCVO;
	}


	public InvoiceNoRangeCVO getInvoiceNoRangeCVO(Long invoiceNoRangePK) throws Exception {
		return super.getEntity(invoiceNoRangePK);
	}


	@Override
	protected List<InvoiceNoRangeCVO> getEntitiesFromServer(Collection<Long> invoiceNoRangePKs) throws Exception {
		System.out.print(getClass().getName() + ".getEntitiesFromServer() --> ");

		List<InvoiceNoRangeVO> invoiceNoRangeVOs = getInvoiceNoRangeMgr().getInvoiceNoRangeVOs(invoiceNoRangePKs);
		List<InvoiceNoRangeCVO> invoiceNoRangeCVOs = InvoiceNoRangeCVO.convertInvoiceNoRangeVO2CVO(invoiceNoRangeVOs);

		System.out.println(invoiceNoRangeCVOs.size());

		return invoiceNoRangeCVOs;
	}


	public List<InvoiceNoRangeCVO> getInvoiceNoRangeCVOs(Collection<Long> invoiceNoRangePKs) throws Exception {
		return super.getEntities(invoiceNoRangePKs);
	}


	@Override
	protected InvoiceNoRangeCVO createEntityOnServer(InvoiceNoRangeCVO invoiceNoRangeCVO) throws Exception {
		InvoiceNoRangeVO invoiceNoRangeVO = invoiceNoRangeCVO.getVO();
		invoiceNoRangeVO.validate();
		invoiceNoRangeVO = getInvoiceNoRangeMgr().createInvoiceNoRange(invoiceNoRangeVO);
		invoiceNoRangeCVO.setVO(invoiceNoRangeVO);

		List<DataStoreVO> dataStoreVOs = Collections.emptyList();
		invoiceNoRangeCVO.setInvoiceTemplateVOList(dataStoreVOs);

		return invoiceNoRangeCVO;
	}


	@Override
	public InvoiceNoRangeCVO create(InvoiceNoRangeCVO invoiceNoRangeCVO) throws Exception {
		return super.create(invoiceNoRangeCVO);
	}


	@Override
	protected InvoiceNoRangeCVO updateEntityOnServer(InvoiceNoRangeCVO invoiceNoRangeCVO) throws Exception {
		InvoiceNoRangeVO invoiceNoRangeVO = invoiceNoRangeCVO.getVO();
		invoiceNoRangeVO.validate();
		invoiceNoRangeVO = getInvoiceNoRangeMgr().updateInvoiceNoRange(invoiceNoRangeVO);
		invoiceNoRangeCVO.setVO(invoiceNoRangeVO);
		return invoiceNoRangeCVO;
	}


	@Override
	public InvoiceNoRangeCVO update(InvoiceNoRangeCVO invoiceNoRangeCVO) throws Exception {
		return super.update(invoiceNoRangeCVO);
	}


	@Override
	protected void deleteEntityOnServer(InvoiceNoRangeCVO invoiceNoRangeCVO) throws Exception {
		if (invoiceNoRangeCVO != null) {
			Long id = invoiceNoRangeCVO.getPK();
			getInvoiceNoRangeMgr().deleteInvoiceNoRange(id);
		}
	}


	@Override
	public void delete(InvoiceNoRangeCVO invoiceNoRangeCVO) throws Exception {
		super.delete(invoiceNoRangeCVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(InvoiceNoRangeCVO invoiceNoRangeCVO) {
		Long eventID = null;
		if (invoiceNoRangeCVO != null) {
			eventID= invoiceNoRangeCVO.getEventPK();
		}
		return eventID;
	}


	@Override
	protected List<InvoiceNoRangeCVO> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// foreignKey can also be Long
		Long eventPK = (Long) foreignKey;

		List<InvoiceNoRangeVO> invoiceNoRangeVOs = getInvoiceNoRangeMgr().getInvoiceNoRangeVOsByEventPK(
			eventPK
		);

		List<InvoiceNoRangeCVO> invoiceNoRangeCVOs = InvoiceNoRangeCVO.convertInvoiceNoRangeVO2CVO(invoiceNoRangeVOs);

		return invoiceNoRangeCVOs;
	}


	public List<InvoiceNoRangeCVO> getInvoiceNoRangeCVOsByEventPK(Long eventPK) throws Exception {
		return getEntityListByForeignKey(eventPK);
	}


	@Override
	protected List<InvoiceNoRangeCVO> getAllEntitiesFromServer() throws Exception {
		List<InvoiceNoRangeCVO> invoiceNoRangeCVOs = getInvoiceNoRangeMgr().getInvoiceNoRangeCVOs(
			false,	// withInvoiceNoRangeOfClosedEvents
			standardSettings
		);

		return invoiceNoRangeCVOs;
	}


	public Collection<InvoiceNoRangeCVO> getAllInvoiceNoRangeCVOs() throws Exception {
		return getAllEntities();
	}


	// **************************************************************************
	// * Extensions
	// *

	@Override
	protected boolean isExtended(InvoiceNoRangeCVO invoiceNoRangeCVO) {
		// Do not check the data, but always the settings, because the data may initialized lazily.
		// Here there are no settings, so we can only check the data.
		return
			invoiceNoRangeCVO != null &&
			invoiceNoRangeCVO.getInvoiceTemplateVOList() != null;
	}


	@Override
	protected void copyExtendedValues(InvoiceNoRangeCVO from, InvoiceNoRangeCVO to) {
		to.copyTransientValuesFrom(from, false);
	}


	@Override
	protected InvoiceNoRangeCVO getExtendedEntityFromServer(Long pk) throws Exception {
		System.out.println(getClass().getName() + ".getExtendedEntityFromServer(" + pk + ")");

		final InvoiceNoRangeCVO invoiceNoRangeCVO = getInvoiceNoRangeMgr().getInvoiceNoRangeCVO(
			pk,
			extendedSettings
		);
		if (invoiceNoRangeCVO == null) {
			throw new ErrorMessageException("InvoiceNoRange not found: " + pk);
		}

		return invoiceNoRangeCVO;
	}

	@Override
	protected List<InvoiceNoRangeCVO> getExtendedEntitiesFromServer(List<Long> invoiceNoRangePKs)
	throws Exception {
		List<InvoiceNoRangeCVO> invoiceNoRangeCVOs = getInvoiceNoRangeMgr().getInvoiceNoRangeCVOs(invoiceNoRangePKs, extendedSettings);

		return invoiceNoRangeCVOs;
	}


	public InvoiceNoRangeCVO getExtendedInvoiceNoRangeCVO(Long invoiceNoRangePK)
	throws Exception {
		return super.getExtendedEntity(invoiceNoRangePK);
	}


	public List<InvoiceNoRangeCVO> getExtendedInvoiceNoRangeCVOs(List<Long> invoiceNoRangePKs)
	throws Exception {
		return super.getExtendedEntities(invoiceNoRangePKs);
	}

	// *
	// * Extensions
	// **************************************************************************


	public void uploadTemplate(
		InvoiceTemplateType type,
		Long invoiceNoRangePK,
        String language,
        String extFilePath,
        byte[] content
    )
	throws Exception {
		getInvoiceNoRangeMgr().uploadTemplate(
			type,
			invoiceNoRangePK,
			language,
			extFilePath,
			content
		);

		handleUpdate(invoiceNoRangePK);
	}


	public void deleteTemplate(
		Long invoiceNoRangePK,
        Long dataStorePK
    )
	throws Exception {
		getDataStoreMgr().delete(dataStorePK);

		handleUpdate(invoiceNoRangePK);
	}


	public void deleteTemplates(Long invoiceNoRangePK, Collection<DataStoreVO> dataStoreVOs)
	throws Exception {

		for (DataStoreVO dataStoreVO : dataStoreVOs) {
			getDataStoreMgr().delete(dataStoreVO.getPK());
		}

		handleUpdate(invoiceNoRangePK);
	}


	public InvoiceNoRangeCVO copyInvoiceNoRange(
		Long sourceInvoiceNoRangePK,
		Long destEventPK
	)
	throws ErrorMessageException {
		InvoiceNoRangeVO inrVO = getInvoiceNoRangeMgr().copyInvoiceNoRange(
			sourceInvoiceNoRangePK,
			destEventPK,
			Locale.getDefault().getLanguage()
		);

		InvoiceNoRangeCVO inrCVO = new InvoiceNoRangeCVO();
		inrCVO.setVO(inrVO);

		put(inrCVO);

		List<Long> primaryKeyList = Collections.singletonList(inrVO.getID());
		try {
			fireCreate(primaryKeyList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return inrCVO;
	}

}
