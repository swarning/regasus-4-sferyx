package de.regasus.report.dialog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.regasus.common.Language;

public class LanguageFilter extends ViewerFilter {

	private Set<String> languages = new HashSet<>();


	public LanguageFilter() {
	}


	public LanguageFilter(List<String> languages) {
		this.languages.addAll(languages);
	}


	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		final Language language = (Language) element;
		return languages.contains( language.getId() );
	}


	public void addLanguage(String languageCode) {
		languages.add(languageCode);
	}

}
