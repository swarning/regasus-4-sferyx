package de.regasus.report.model;

import java.util.Comparator;
import java.util.Locale;

import com.lambdalogic.messeinfo.report.data.BaseReportCVO;
import com.lambdalogic.util.ObjectComparator;

public class BaseReportCVOComparator implements Comparator<BaseReportCVO> {

	private static BaseReportCVOComparator DEFAULT_INSTANCE;

	private ObjectComparator objectComparator;
	private Comparator<BaseReportCVO> comparator;


	public static BaseReportCVOComparator getInstance() {
		if (DEFAULT_INSTANCE == null) {
			DEFAULT_INSTANCE = new BaseReportCVOComparator( Locale.getDefault() );
		}
		return DEFAULT_INSTANCE;
	}


	private BaseReportCVOComparator(Locale locale) {
		objectComparator = ObjectComparator.getInstance(locale);

		comparator = Comparator
			.comparing(    BaseReportCVO::getName,  objectComparator)
			.thenComparing(BaseReportCVO::getId,    objectComparator);

		comparator = Comparator.nullsFirst(comparator);
	}


	@Override
	public int compare(BaseReportCVO value1, BaseReportCVO value2) {
		return comparator.compare(value1, value2);
	}

}
