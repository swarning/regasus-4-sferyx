package de.regasus.finance;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.PaymentReceiptType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.FileHelper;

import de.regasus.core.model.MICacheModel;


public class PaymentReceiptTemplateModel extends MICacheModel<String, DataStoreVO> {

	private static PaymentReceiptTemplateModel singleton = null;


	private PaymentReceiptTemplateModel() {
		super();
	}


	public static PaymentReceiptTemplateModel getInstance() {
		if (singleton == null) {
			singleton = new PaymentReceiptTemplateModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected String getKey(DataStoreVO entity) {
		return entity.getPath();
	}


	@Override
	protected DataStoreVO getEntityFromServer(String dsPath) throws Exception {
		DataStoreVO dataStoreVO = getDataStoreMgr().readWithPathEqualTo(dsPath, false);
		return dataStoreVO;
	}


	@Override
	protected List<DataStoreVO> getAllEntitiesFromServer() throws Exception {
		List<DataStoreVO> dataStoreVOs = getPaymentMgr().getPaymentReceiptTemplates(false);
		return dataStoreVOs;
	}


	@Override
	protected DataStoreVO createEntityOnServer(DataStoreVO dataStoreVO) throws Exception {
		dataStoreVO.validate();

		/* Data is set by path, not by ID, because the identity of a payment receipt is its path, not its PK
		 * which is more technical.
		 */
		String path = dataStoreVO.getPath();

		// safe content to avoid loading it again later
		byte[] content = dataStoreVO.getContent();

		getDataStoreMgr().setDataByPath(dataStoreVO);
		dataStoreVO = getDataStoreMgr().readWithPathEqualTo(path, false);

		// restore content
		dataStoreVO.setContent(content);

		return dataStoreVO;
	}


	@Override
	protected DataStoreVO updateEntityOnServer(DataStoreVO dataStoreVO) throws Exception {
		/* The identity of a payment receipt is its path.
		 * The PK is only a technical identity.
		 * Therefore create and update use the same code.
		 */
		return createEntityOnServer(dataStoreVO);
	}


	@Override
	protected void deleteEntityOnServer(DataStoreVO dataStoreVO) throws Exception {
		if (dataStoreVO != null) {
			String path = dataStoreVO.getPath();
			getDataStoreMgr().deleteWithPathEqualTo(path);
		}
	}


	public Collection<DataStoreVO> getAllTemplates() throws Exception {
		return getAllEntities();
	}


	@Override
	public DataStoreVO update(DataStoreVO dataStoreVO) throws Exception {
		return super.update(dataStoreVO);
	}


	public DataStoreVO uploadPaymentReceiptTemplate(
		String filePath,
		PaymentReceiptType paymentReceiptType,
		PaymentType paymentType,
		String language,
		byte[] content
	)
	throws Exception {
		String dsPath = "/" + paymentReceiptType.name() + "/" + paymentType.name();
		if (language != null) {
			dsPath += "/" + language;
		}

		String docType = paymentReceiptType.name() + "." + paymentType.name();

		DataStoreVO dataStoreVO = new DataStoreVO(
			filePath,								// extFileName
			dsPath,									// path
			FileHelper.getExtension(filePath),		// extension
			language,
			docType,
			null,									// tag
			false,									// compressed
			null,									// eventPK
			null,									// description
			content
		);

		return super.create(dataStoreVO);
	}


	@Override
	public void delete(DataStoreVO dataStoreVO) throws Exception {
		/* Do NOT delete if there is no ID, because it means that this DataStoreVO does not represent a record in table
		 * DATA_STORE but a file in the JAR.
		 */
		if (dataStoreVO.getID() != null) {
			super.delete(dataStoreVO);
		}
	}

}
