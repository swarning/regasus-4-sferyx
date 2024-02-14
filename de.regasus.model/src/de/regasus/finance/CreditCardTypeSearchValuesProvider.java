package de.regasus.finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO_Name_Comparator;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;

import de.regasus.core.CreditCardTypeModel;

public class CreditCardTypeSearchValuesProvider implements SearchValuesProvider {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LinkedHashMap getValues() throws Exception {
		CreditCardTypeModel cctModel = CreditCardTypeModel.getInstance();
		List<CreditCardTypeVO> creditCardTypeVOs = new ArrayList<>( cctModel.getAllCreditCardTypeVOs() );

		Collections.sort(creditCardTypeVOs, CreditCardTypeVO_Name_Comparator.getInstance());

		LinkedHashMap<Long, String> values = MapHelper.createLinkedHashMap(creditCardTypeVOs.size());

		for (CreditCardTypeVO creditCardTypeVO : creditCardTypeVOs) {
			values.put(creditCardTypeVO.getID(), creditCardTypeVO.getName());
		}

		return values;
	}

}
