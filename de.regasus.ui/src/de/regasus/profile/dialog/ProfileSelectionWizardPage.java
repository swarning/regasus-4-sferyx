package de.regasus.profile.dialog;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.sql.ProfileSearch;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.profile.search.ProfileSearchComposite;


public class ProfileSelectionWizardPage extends WizardPage {

	public static final String NAME = MethodHandles.lookup().lookupClass().getName();

	protected SelectionMode selectionMode;

	private SearchInterceptor searchInterceptor;

	/**
	 * If present, is used for initial search.
	 * Though the value is set to participantSearchComposite as well, this field is necessary, because Composite
	 * might not be initialized yet when the value is set!
	 */
	private List<SQLParameter> initialSqlParameters;

	/**
	 * If present, is used for initial search with this last name.
	 * Though the value is set to participantSearchComposite as well, this field is necessary, because Composite
	 * might not be initialized yet when the value is set!
	 */
	private String initialLastName;

	/**
	 * If present, is used for initial search with this first name
	 * Though the value is set to participantSearchComposite as well, this field is necessary, because Composite
	 * might not be initialized yet when the value is set!
	 */
	private String initialFirstName;

	protected ProfileSearchComposite profileSearchComposite;


	public ProfileSelectionWizardPage(SelectionMode selectionMode) {
		super(NAME);

		Objects.requireNonNull(selectionMode);
		this.selectionMode = selectionMode;

		setTitle(I18N.ProfileSelectionWizardPage_Title);
	}


	@Override
	public boolean isPageComplete() {
		// decide if this page is complete depending on the selectionMode and the currently selected Profiles
		switch (selectionMode) {
			case NO_SELECTION:
			case MULTI_OPTIONAL_SELECTION:
				return true;
			case SINGLE_SELECTION:
				return getSelectedPKs().size() == 1;
			case MULTI_SELECTION:
				return getSelectedPKs().size() >= 1;
			default:
				return false;
		}
	}


	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());

		setControl(container);

		profileSearchComposite = new ProfileSearchComposite(
			container,
			selectionMode,
			SWT.NONE,
			true // useDetachedSearchModelInstance
		);

		profileSearchComposite.setSearchInterceptor(searchInterceptor);
		profileSearchComposite.setInitialSQLParameters(initialSqlParameters);
		profileSearchComposite.setInitialLastName(initialLastName);
		profileSearchComposite.setInitialFirstName(initialFirstName);

		TableViewer tableViewer = profileSearchComposite.getTableViewer();
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(isPageComplete());
			}
		});

		setPageComplete(isPageComplete());
	}


	public void setWhereFields(List<WhereField> whereFields) {
		if (notEmpty(whereFields)) {
			profileSearchComposite.setWhereFields(whereFields);
			profileSearchComposite.doSearch();
		}
	}


	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
		if (profileSearchComposite != null) {
			profileSearchComposite.setSearchInterceptor(searchInterceptor);
		}
	}


	public void setInitialSQLParameters(List<SQLParameter> sqlParameters) {
		this.initialSqlParameters = sqlParameters;
		if (profileSearchComposite != null) {
			profileSearchComposite.setInitialSQLParameters(sqlParameters);
		}
	}


	public void setInitialLastName(String lastName) {
		this.initialLastName = lastName;
		if (profileSearchComposite != null) {
			profileSearchComposite.setInitialLastName(lastName);
		}
	}


	public void setInitialFirstName(String firstName) {
		this.initialFirstName = firstName;
		if (profileSearchComposite != null) {
			profileSearchComposite.setInitialFirstName(firstName);
		}
	}


	public void doSearch() {
		if (profileSearchComposite != null) {
			profileSearchComposite.doSearch();
		}
	}


	public ProfileSearch getProfileSearch() {
		return profileSearchComposite.getProfileSearch();
	}


	public List<SQLParameter> getSQLParameters() {
		return profileSearchComposite.getSQLParameters();
	}


	public List<Long> getSelectedPKs() {
		return profileSearchComposite.getSelectedPKs();
	}


	public List<Profile> getSelectedProfiles() {
		return profileSearchComposite.getSelectedProfiles();
	}

}
