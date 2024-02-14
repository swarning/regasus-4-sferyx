package de.regasus.core.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ui.CoreI18N;

/**
 * Container-Widget for <code>WhereTerm</code>s.
 *
 * @author sacha
 */
public class WhereComposite extends Composite {

	private ScrolledComposite parent;
	private List<WhereTerm> whereTermList = new ArrayList<WhereTerm>();
	private WhereTerm firstWhereTerm;
	private List<SQLField> sqlFields;
	private GridData valueGridData;
	private ISearcher searcher;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public WhereComposite(ScrolledComposite parent, int style) {
		super(parent, style);
		this.parent = parent;
//		setFont(SWTResourceManager.getFont("", 10, SWT.BOLD));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		setLayout(gridLayout);


		// **************************************************************************
		// *
		// *

//		final Composite buttonComposite = new Composite(this, SWT.NONE);
//		buttonComposite.setLayout(new RowLayout());
//		final GridData gd_buttonComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1);
//		buttonComposite.setLayoutData(gd_buttonComposite);
//
//		final Button packButton = new Button(buttonComposite, SWT.NONE);
//		packButton.setText("pack");
//		packButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				WhereComposite.this.pack();
//			}
//		});
//
//		final Button layoutButton = new Button(buttonComposite, SWT.NONE);
//		layoutButton.setText("layout");
//		layoutButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				WhereComposite.this.layout();
//			}
//		});
//
//		final Button redrawButton = new Button(buttonComposite, SWT.NONE);
//		redrawButton.setText("redraw");
//		redrawButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				WhereComposite.this.redraw();
//			}
//		});

		// *
		// *
		// **************************************************************************



		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		final Label fieldLabel = new Label(this, SWT.NONE);
		fieldLabel.setLayoutData(new GridData());
		fieldLabel.setText(CoreI18N.WhereComposite_Field);

		final Label operatorLabel = new Label(this, SWT.NONE);
		operatorLabel.setText(CoreI18N.WhereComposite_Operator);

		final Label valueLabel = new Label(this, SWT.NONE);
		valueGridData = new GridData(SWT.DEFAULT);
//		valueLabel.setLayoutData(new GridData(200, SWT.DEFAULT));
		valueLabel.setText(CoreI18N.WhereComposite_Value);

		final Label activeLabel = new Label(this, SWT.NONE);
		activeLabel.setText(CoreI18N.WhereComposite_Active);

		addFirstWhereTerm();
		refreshLayout();
	}


	private void addFirstWhereTerm() {
		firstWhereTerm = new WhereTerm(this, null);
		firstWhereTerm.setSqlFields(sqlFields);
		firstWhereTerm.setRemoveEnabled(false);
		whereTermList.add(firstWhereTerm);

		refreshLayout();
	}


	public void setValueWidth(int width) {
		valueGridData.widthHint = width;
		refreshLayout();
	}


	public void addWhereTerm() {
		WhereTerm whereTerm = new WhereTerm(this, null);
		whereTerm.setSqlFields(sqlFields);

		whereTermList.add(whereTerm);

		if (whereTermList.size() == 2) {
			whereTermList.get(0).setRemoveEnabled(true);
		}

		refreshLayout();
	}


	public void removeWhereTerm(WhereTerm whereTerm) {
		if (whereTermList.size() > 0) {
			whereTermList.remove(whereTerm);
			whereTerm.dispose();
			refreshLayout();

			if (whereTermList.size() == 1) {
				whereTermList.get(0).setRemoveEnabled(false);
			}
		}

		// if we removed the last WhereTerm, add a new first one
		if (whereTermList.isEmpty()) {
			addFirstWhereTerm();
		}

		refreshLayout();
	}


	public void removeWhereTerm(int index) {
		if (index == 0 && whereTermList.size() == 1) {
			WhereTerm whereTerm = whereTermList.get(index);
			whereTerm.initialize();
		}
		else if (index < whereTermList.size()) {
			WhereTerm whereTerm = whereTermList.get(index);
			whereTermList.remove(index);
			whereTerm.dispose();

			if (whereTermList.size() == 1) {
				whereTermList.get(0).setRemoveEnabled(false);
			}
		}

		// if we removed the last WhereTerm, add a new first one
//		if (whereTermList.isEmpty()) {
//			addFirstWhereTerm();
//		}

		refreshLayout();
	}



	public void refreshLayout() {

		/* An dieser muss asynchron in den Display-Thread gewechselt werden, weil
		 * das synchrone Wechseln in einigen Situationen zu einem Deadlock führt!
		 * Beispiel: Schließen einer Veranstaltung.
		 */
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				int maxWidth = -1;
				for (WhereTerm whereTerm : whereTermList) {
					if (whereTerm.getValueWidgetsPreferredWidth() > maxWidth) {
						maxWidth = whereTerm.getValueWidgetsPreferredWidth();
					}
				}
				valueGridData.widthHint = maxWidth;

				WhereComposite.this.layout();

//				this.pack();

				Point prefSize = WhereComposite.this.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//				System.out.println("prefSize: " + prefSize);
				parent.setMinWidth(prefSize.x);
				parent.setMinHeight(prefSize.y);

//				this.redraw();
			}
		});
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	/**
	 * Sets the possible Search-Fields.
	 * This is the list of all fields, which the user can choose to constrain
	 * the number of result records (Where-Clause).
	 * This accects the data model of the most left Combo-Widgets.
	 * SQLFields contain NOT the Parameter itself!
	 *
	 * @param sqlFields
	 */
	public void setSqlFields(List<SQLField> sqlFields) {
		this.sqlFields = sqlFields;
		// Set the list of SQLFields in each WhereTerm.
		for (WhereTerm whereTerm : whereTermList) {
			whereTerm.setSqlFields(sqlFields);
		}
		refreshLayout();
	}


	/**
	 * Sets the actual choosen search parameters.
	 *
	 * @param sqlParameterList
	 */
	public void setSQLParameterList(List<SQLParameter> sqlParameterList) {
		int i = 0;

		// set WhereTerms and add them if there're not enough
		if (sqlParameterList != null) {
			for (SQLParameter sqlParameter : sqlParameterList) {
				// Add a new WhereTerm if the last one is reached.
				if (whereTermList.size() == i) {
					addWhereTerm();
				}
				// get the WhereTerm at position i
				WhereTerm whereTerm = whereTermList.get(i);
				// set the Parameters
				whereTerm.setSqlParameter(sqlParameter);
				i++;
			}
		}

		// remove spare WhereTerms
		while (i < whereTermList.size()) {
			removeWhereTerm(i);
			i++;
		}

		refreshLayout();
	}


	/**
	 * Collects from visible WhereTerm composites a list of all active SQLParameters. When one contains
	 * an invalid value, it shows an error dialog and is ommitted from the list.
	 */
	public List<SQLParameter> getSQLParameterList() {
		List<SQLParameter> sqlParameterList = new ArrayList<SQLParameter>(whereTermList.size());

		for (WhereTerm whereTerm : whereTermList) {
			SQLParameter sqlParameter = whereTerm.getSQLParameter();

			// If no search attribute (field) is selected, we get no sqlParameter;
			// neither when the active button is not selected.
			if (sqlParameter != null) {
				sqlParameterList.add(sqlParameter);
			}
		}

		return sqlParameterList;
	}


	/**
	 * Collects from visible WhereTerm composites a list of all SQLParameters, containing search fields, operators and
	 * values, regardless whether they are active or not, or have an empty or even invalid value. Only if no search
	 * field is selected at all in a WhereTerm, the WhereTerm returns a null that gets omitted from the list.
	 * <p>
	 * Is used to store the current search fields in a memento, so that the user finds the same fields when they start
	 * the client anew.
	 *
	 * @return
	 */
	public List<SQLParameter> getSQLParameterListForPreferences() {
		List<SQLParameter> sqlParameterList = new ArrayList<SQLParameter>(whereTermList.size());

		for (WhereTerm whereTerm : whereTermList) {
			SQLParameter sqlParameter = whereTerm.getSQLParameterForMemento();

			// If no search attribute (field) is selected, we get no sqlParameter.
			if (sqlParameter != null) {
				sqlParameterList.add(sqlParameter);
			}
		}

		return sqlParameterList;
	}


	public String getDescription() {
		StringBuffer sb = new StringBuffer();
		for (WhereTerm whereTerm : whereTermList) {
			String description = whereTerm.getDescription();
			if (description != null) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(description);
			}
		}
		return sb.toString();
	}


	public void setActive(boolean active) {
		for (WhereTerm whereTerm : whereTermList) {
			whereTerm.setActive(active);
		}

	}

	public void setSearcher(ISearcher searcher) {
		this.searcher = searcher;
	}


	public void doSearch() {
		if (this.searcher != null) {
			searcher.doSearch();
		}
	}


}
