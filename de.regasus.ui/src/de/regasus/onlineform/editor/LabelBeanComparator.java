package de.regasus.onlineform.editor;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.lambdalogic.messeinfo.regasus.LabelBean;

final class LabelBeanComparator implements Comparator<LabelBean> {

	private Collator collator;
	private String language;


	public LabelBeanComparator(Locale locale) {
		collator = Collator.getInstance(locale);
		language = locale.getLanguage();
	}


	@Override
	public int compare(LabelBean lb1, LabelBean lb2) {
		return collator.compare(lb1.getDefaultValue(language), lb2.getDefaultValue(language));
	}

}