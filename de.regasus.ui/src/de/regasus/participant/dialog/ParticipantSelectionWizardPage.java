package de.regasus.participant.dialog;

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
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.participant.search.ParticipantSearchComposite;


public class ParticipantSelectionWizardPage extends WizardPage {

	public static final String NAME = MethodHandles.lookup().lookupClass().getName();

	protected SelectionMode selectionMode;

	protected Long initialEventPK;

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

	protected ParticipantSearchComposite participantSearchComposite;


	public ParticipantSelectionWizardPage(SelectionMode selectionMode, Long eventPK) {
		super(NAME);

		Objects.requireNonNull(selectionMode);
		this.selectionMode = selectionMode;

		this.initialEventPK = eventPK;

		setTitle(I18N.ParticipantSelectionWizardPage_Title);
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

		participantSearchComposite = new ParticipantSearchComposite(
			container,
			selectionMode,
			SWT.NONE,
			true, // useDetachedSearchModelInstance
			initialEventPK
		);

		participantSearchComposite.setSearchInterceptor(searchInterceptor);
		participantSearchComposite.setInitialSQLParameters(initialSqlParameters);
		participantSearchComposite.setInitialLastName(initialLastName);
		participantSearchComposite.setInitialFirstName(initialFirstName);

		TableViewer tableViewer = participantSearchComposite.getTableViewer();
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
			participantSearchComposite.setWhereFields(whereFields);
			participantSearchComposite.doSearch();
		}
	}


	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
		if (participantSearchComposite != null) {
			participantSearchComposite.setSearchInterceptor(searchInterceptor);
		}
	}


	public void setInitialSQLParameters(List<SQLParameter> sqlParameters) {
		this.initialSqlParameters = sqlParameters;
		if (participantSearchComposite != null) {
			participantSearchComposite.setInitialSQLParameters(sqlParameters);
		}
	}


	public void setInitialLastName(String lastName) {
		this.initialLastName = lastName;
		if (participantSearchComposite != null) {
			participantSearchComposite.setInitialLastName(lastName);
		}
	}


	public void setInitialFirstName(String firstName) {
		initialFirstName = firstName;
		if (participantSearchComposite != null) {
			participantSearchComposite.setInitialFirstName(firstName);
		}
	}


	public void setEventPK(Long eventPK) {
		initialEventPK = eventPK;
		if (participantSearchComposite != null) {
			participantSearchComposite.setEventPK(eventPK);
		}
	}


	public void doSearch() {
		if (participantSearchComposite != null) {
			participantSearchComposite.doSearch();
		}
	}


	public ParticipantSearch getParticipantSearch() {
		return participantSearchComposite.getParticipantSearch();
	}


	public List<SQLParameter> getSQLParameters() {
		return participantSearchComposite.getSQLParameters();
	}


	public List<Long> getSelectedPKs() {
		return participantSearchComposite.getSelectedPKs();
	}


	public List<ParticipantSearchData> getSelectedParticipants() {
		return participantSearchComposite.getSelectedParticipants();
	}

}
