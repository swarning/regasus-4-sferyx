package de.regasus.report.wizard.generic;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.kernel.report.generic.GenericReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.report.dialog.IReportWizardPage;

public class SqlWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "SqlWizardPage";

	protected static final int LABEL_WIDTH = 80;

	private GenericReportParameter parameter;


	// Widgets
	private ScrolledComposite scrolledComposite;
	private Composite mainComposite;
	private MultiLineText select;
	private MultiLineText from;
	private MultiLineText where;
	private MultiLineText groupBy;
	private MultiLineText orderBy;


	public SqlWizardPage() {
		super(ID);
		setTitle("SQL-Statement");
		setDescription("Please, insert the SQL-Statement in the following fields.");
	}


	private LocalControlListener controlListener = new LocalControlListener();

	private class LocalControlListener extends ControlAdapter {
		@Override
		public void controlResized(ControlEvent e) {
			Rectangle r = scrolledComposite.getClientArea();
			scrolledComposite.setMinSize(mainComposite.computeSize(r.width, SWT.DEFAULT));
		}
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);

		setControl(scrolledComposite);

		mainComposite = new Composite(scrolledComposite, SWT.NONE);
		mainComposite.setLayout(new GridLayout(2, false));

		scrolledComposite.setContent(mainComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		parent.addControlListener(controlListener);

		int multiLineTextStyle = SWT.BORDER;

		/*
		 * SELECT
		 */
		{
			final Label selectLabel = new Label(mainComposite, SWT.NONE);
			final GridData gd_selectLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			selectLabel.setLayoutData(gd_selectLabel);
			selectLabel.setText("SELECT");

			select = new MultiLineText(mainComposite, multiLineTextStyle);
			select.setMinLineCount(5);
			select.addControlListener(controlListener);

			final GridData gd_select = new GridData(SWT.FILL, SWT.FILL, true, false);
			select.setLayoutData(gd_select);
			select.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					// Werte ermitteln
					String selectValue = StringHelper.trim(select.getText());

					// Werte in ReportParameter setzen
					parameter.setSelect(selectValue);

					// description setzen
					String desc = "SELECT: ";
					if (selectValue != null) {
						desc += selectValue;
					}

					parameter.setDescription(GenericReportParameter.SELECT_DESCRIPTION_ID, desc);

					setPageComplete(isPageComplete());
				}
			});
		}

		/*
		 * FROM
		 */
		{
			final Label fromLabel = new Label(mainComposite, SWT.NONE);
			final GridData gd_fromLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			fromLabel.setLayoutData(gd_fromLabel);
			fromLabel.setText("FROM");

			from = new MultiLineText(mainComposite, multiLineTextStyle);
			from.setMinLineCount(5);
			from.addControlListener(controlListener);

			final GridData gd_from = new GridData(SWT.FILL, SWT.FILL, true, false);
			from.setLayoutData(gd_from);
			from.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					// Werte ermitteln
					String fromValue = StringHelper.trim(from.getText());

					// Werte in ReportParameter setzen
					parameter.setFrom(fromValue);

					// description setzen
					String desc = "FROM: ";
					if (fromValue != null) {
						desc += fromValue;
					}

					parameter.setDescription(GenericReportParameter.FROM_DESCRIPTION_ID, desc);

					setPageComplete(isPageComplete());
				}
			});
		}

		/*
		 * WHERE
		 */
		{
			final Label whereLabel = new Label(mainComposite, SWT.NONE);
			final GridData gd_whereLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			whereLabel.setLayoutData(gd_whereLabel);
			whereLabel.setText("WHERE");

			where = new MultiLineText(mainComposite, multiLineTextStyle);
			where.setMinLineCount(5);
			where.addControlListener(controlListener);

			final GridData gd_where = new GridData(SWT.FILL, SWT.FILL, true, false);
			where.setLayoutData(gd_where);
			where.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					// Werte ermitteln
					String whereValue = StringHelper.trim(where.getText());

					// Werte in ReportParameter setzen
					parameter.setWhere(whereValue);

					// description setzen
					String desc = null;
					if (whereValue != null) {
						desc = "WHERE: ";
						desc += whereValue;
					}

					parameter.setDescription(GenericReportParameter.WHERE_DESCRIPTION_ID, desc);

					setPageComplete(isPageComplete());
				}
			});
		}

		/*
		 * GROUP BY
		 */
		{
			final Label groupByLabel = new Label(mainComposite, SWT.NONE);
			final GridData gd_groupByLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			groupByLabel.setLayoutData(gd_groupByLabel);
			groupByLabel.setText("GROUP BY");

			groupBy = new MultiLineText(mainComposite, multiLineTextStyle);
			groupBy.setMinLineCount(2);
			groupBy.addControlListener(controlListener);

			final GridData gd_groupBy = new GridData(SWT.FILL, SWT.FILL, true, false);
			groupBy.setLayoutData(gd_groupBy);
			groupBy.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					// Werte ermitteln
					String groupByValue = StringHelper.trim(groupBy.getText());

					// Werte in ReportParameter setzen
					parameter.setGroupBy(groupByValue);

					// description setzen
					String desc = null;
					if (groupByValue != null) {
						desc = "GROUP BY: ";
						desc += groupByValue;
					}

					parameter.setDescription(GenericReportParameter.GROUP_DESCRIPTION_ID, desc);

					setPageComplete(isPageComplete());
				}
			});
		}

		/*
		 * ORDER BY
		 */
		{
			final Label orderByLabel = new Label(mainComposite, SWT.NONE);
			final GridData gd_orderByLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			orderByLabel.setLayoutData(gd_orderByLabel);
			orderByLabel.setText("ORDER BY");

			orderBy = new MultiLineText(mainComposite, multiLineTextStyle);
			orderBy.setMinLineCount(2);
			orderBy.addControlListener(controlListener);

			final GridData gd_orderBy = new GridData(SWT.FILL, SWT.FILL, true, false);
			orderBy.setLayoutData(gd_orderBy);
			orderBy.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					// Werte ermitteln
					String orderByValue = StringHelper.trim(orderBy.getText());

					// Werte in ReportParameter setzen
					parameter.setOrderBy(orderByValue);

					// description setzen
					String desc = null;
					if (orderByValue != null) {
						desc = "ORDER BY: ";
						desc += orderByValue;
					}

					parameter.setDescription(GenericReportParameter.ORDER_DESCRIPTION_ID, desc);

					setPageComplete(isPageComplete());
				}
			});
		}

	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof GenericReportParameter) {
			parameter = (GenericReportParameter) reportParameter;

			select.setText(StringHelper.avoidNull(parameter.getSelect()));
			from.setText(StringHelper.avoidNull(parameter.getFrom()));
			where.setText(StringHelper.avoidNull(parameter.getWhere()));
			groupBy.setText(StringHelper.avoidNull(parameter.getGroupBy()));
			orderBy.setText(StringHelper.avoidNull(parameter.getOrderBy()));
		}
	}

	@Override
	public boolean isPageComplete() {
		return parameter.isComplete();
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
