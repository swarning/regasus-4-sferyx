package de.regasus.core;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.regasus.common.Language;
import de.regasus.core.model.MICacheModel;


public class LanguageModel extends MICacheModel<String, Language> {
	private static LanguageModel singleton = null;


	private LanguageModel() {
		super();
	}


	public static LanguageModel getInstance() {
		if (singleton == null) {
			singleton = new LanguageModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected String getKey(Language entity) {
		return entity.getId();
	}


	@Override
	protected Language getEntityFromServer(String languageId) throws Exception {
		Language language = getLanguageMgr().read(languageId);
		return language;
	}


	@Override
	protected List<Language> getEntitiesFromServer(Collection<String> languageIds) throws Exception {
		List<Language> languages = getLanguageMgr().read(languageIds);
		return languages;
	}


	@Override
	protected List<Language> getAllEntitiesFromServer() throws Exception {
		List<Language> languages = null;

		if (serverModel.isLoggedIn()) {
			languages = getLanguageMgr().readAllWithDeleted();
		}
		else {
			languages = Collections.emptyList();
		}

		return languages;
	}


	public List<Language> getAllUndeletedLanguages() throws Exception {
		Collection<Language> allLanguages = getAllEntities();
		List<Language> undeletedLanguages = new ArrayList<>( allLanguages.size() );
		for (Language language : allLanguages) {
			if ( ! language.isDeleted() ) {
				undeletedLanguages.add(language);
			}
		}
		return undeletedLanguages;
	}


	@Override
	protected Language createEntityOnServer(Language language) throws Exception {
		language.validate();
		Language newLanguage = getLanguageMgr().create(language);
		return newLanguage;
	}


	@Override
	public Language create(Language language) throws Exception {
		return super.create(language);
	}


	@Override
	protected Language updateEntityOnServer(Language language) throws Exception {
		language.validate();
		Language newLanguage = getLanguageMgr().update(language);
		return newLanguage;
	}


	@Override
	public Language update(Language language) throws Exception {
		return super.update(language);
	}


	@Override
	protected void deleteEntityOnServer(Language language) throws Exception {
		if (language != null) {
			String languageId = language.getId();
			getLanguageMgr().delete(languageId);
		}
	}


	@Override
	public void delete(Language language) throws Exception {
		super.delete(language);
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<Language> languages) throws Exception {
		if (languages != null) {
			// extract PKs
			List<String> languageIds = Language.getPKs(languages);
			getLanguageMgr().delete(languageIds);
		}
	}


	@Override
	public void delete(Collection<Language> languages) throws Exception {
		super.delete(languages);
	}


	public Language getLanguage(String languageId) throws Exception {
		return super.getEntity(languageId);
	}


	public List<Language> getLanguages(List<String> languageIds) throws Exception {
		return super.getEntities(languageIds);
	}

}
