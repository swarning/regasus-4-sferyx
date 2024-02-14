package de.regasus.core.ui.combo;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;


@SuppressWarnings("rawtypes")
public class LanguageCombo extends AbstractComboComposite<Language> implements CacheModelListener {

	private static final Language EMPTY_LANGUAGE = new Language();

	// Model
	private LanguageModel model;

	private Collection<String> filter;


	public LanguageCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Language getEmptyEntity() {
		return EMPTY_LANGUAGE;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				Language language = (Language) element;
				return LanguageString.toStringAvoidNull(language.getName());
			}
		};
	}


	@Override
	protected Collection<Language> getModelData() throws Exception {
		Collection<Language> modelData = model.getAllUndeletedLanguages();
		modelData = filter(modelData);
		return modelData;
	}


	public void setFilter(Collection<String> languageCodes) {
		this.filter = languageCodes;

		try {
			syncComboToModel();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private Collection<Language> filter(Collection<Language> modelData) {
		Collection<Language> filteredModelData = modelData;
		if (filter != null) {
			filteredModelData = new ArrayList<>(modelData.size());
			for (Language language : modelData) {
				if (filter.contains(language.getId())) {
					filteredModelData.add(language);
				}
			}
		}
		return filteredModelData;
	}


	@Override
	protected void initModel() {
		model = LanguageModel.getInstance();
		model.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
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


	public String getLanguageCode() {
		String languageCode = null;
		if (entity != null) {
			languageCode = entity.getId();
		}
		return languageCode;
	}


	public void setLanguageCode(String languageCode) {
		Language language = null;
		if (languageCode != null) {
			try {
				language = model.getLanguage(languageCode);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
		setEntity(language);
	}

}
