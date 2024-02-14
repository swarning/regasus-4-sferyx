package de.regasus.users.user.dialog;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import com.lambdalogic.messeinfo.report.ReportLabel;
import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.view.UserReportTreeContentProvider;
import de.regasus.report.view.UserReportTreeLabelProvider;
import de.regasus.users.ui.Activator;



public class SelectReportConstraintPage extends WizardPage implements ISelectionChangedListener {

	public static final String NAME = "SelectReportConstraintPage";


	private TreeViewer treeViewer;


	private UserReportDirVO userReportDirRoot;

	protected SelectReportConstraintPage() {
		super(NAME);

		String title = UtilI18N.SelectX.replace("<x>", ReportLabel.userReport.getString());
		setTitle(title);
	}

	@Override
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());

		setControl(container);

		Tree tree = new Tree(container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		treeViewer = new TreeViewer(tree);

		treeViewer.setContentProvider(new UserReportTreeContentProvider());
		treeViewer.setLabelProvider(new UserReportTreeLabelProvider());

		// Sortierung
		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1.getClass() == e2.getClass()) {
					return super.compare(viewer, e1, e2);
				}
				else if (e1 instanceof UserReportDirVO) {
					return -1;
				}
				else if (e1 instanceof UserReportVO) {
					return 1;
				}
				return super.compare(viewer, e1, e2);
			}
		});


		treeViewer.addSelectionChangedListener(this);

		setPageComplete(false);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = treeViewer.getSelection();
		Object o = SelectionHelper.getUniqueSelected(selection);
		boolean	reportSelected = (o instanceof UserReportVO);
		setPageComplete(reportSelected);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && userReportDirRoot == null) {
			try {
				UserReportDirListModel userReportDirListModel = UserReportDirListModel.getInstance();
				userReportDirRoot = userReportDirListModel.getRoot();
				treeViewer.setInput(userReportDirRoot);
				UserReportDirVO visibleRoot = userReportDirListModel.getVisibleRoot();
				if (visibleRoot != null) {
					treeViewer.setExpandedState(visibleRoot, true);
				}
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}

		}
		super.setVisible(visible);
	}


	@Override
	public IWizardPage getNextPage() {
		return getWizard().getPage(SetCrudRightsAndPriorityPage.NAME);
	}

	@Override
	public IWizardPage getPreviousPage() {
		return getWizard().getPage(SelectConstraintTypePage.NAME);
	}

	public UserReportVO getUserReportVO() {
		return SelectionHelper.getUniqueSelected(treeViewer.getSelection());
	}
}
