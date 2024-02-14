package de.regasus.report.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.report.parameter.DefaultReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ReportI18N;
import de.regasus.report.ui.Activator;

public class DefaultReportWizard extends Wizard implements IReportWizard {

	protected IReportParameter reportParameter;

	protected IWizardPage currentWizardPage;


	public DefaultReportWizard() {
		super();
	}


	@Override
	public IReportParameter getReportParameter() {
		/* Save the parameters of the current page.
		 * This is actually not necessary. However, some Pages that contain tables have the problem that
		 * ISelectionChangedListener.selectionChanged(SelectionChangedEvent event) propagates wrong selections.
		 * As a work around we save the parameters again. Just to assure to get the correct values.
		 */
		IReportWizardPage currentPage = (IReportWizardPage) getCurrentWizardPage();
		currentPage.saveReportParameters();

		return reportParameter;
	}


	public void setReportParameter(IReportParameter reportParameter) {
		this.reportParameter = reportParameter;
	}


	public void setReportName(String reportName) {
		super.setWindowTitle(ReportI18N.DefaultWizard_TitlePrefix + ": " + reportName);
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
	        reportParameter = new DefaultReportParameter(xmlRequest);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void addPages() {
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
							doAddPages();
							addPage(new LanguageWizardPage());
							addPage(new DocumentFormatWizardPage());
						}
					});
				}
			});
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	protected void doAddPages() {
		// override this to add additional pages
	}


	@Override
	public boolean performFinish() {
		return true;
	}


	@Override
	public IWizardPage getStartingPage() {
		IWizardPage page = super.getStartingPage();
		if (page instanceof IReportWizardPage) {
			IReportWizardPage reportWizardPage = (IReportWizardPage) page;
			reportWizardPage.init(reportParameter);

			currentWizardPage = reportWizardPage;
		}
		return page;
	}


	@Override
	public void nextPressed(final IReportWizardPage currentPage) {
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					SWTHelper.syncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
							doNextPressed(currentPage);
						}
					});
				}
			});
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	protected void doNextPressed(IReportWizardPage currentPage) {
		// save the user input of the current page
		currentPage.saveReportParameters();

		// determine and initialize next page
		IWizardPage nextWizardPage =  getNextPage(currentPage);
		if (nextWizardPage instanceof IReportWizardPage) {
			IReportWizardPage nextReportWizardPage = (IReportWizardPage) nextWizardPage;
			nextReportWizardPage.init(reportParameter);

			currentWizardPage = nextWizardPage;
		}
	}


	@Override
	public void backPressed(IReportWizardPage currentPage) {
		// do NOT save the user input of the current page

		// determine and initialize previous page
		IWizardPage prevWizardPage =  getPreviousPage(currentPage);
		if (prevWizardPage instanceof IReportWizardPage) {
			IReportWizardPage prevReportWizardPage = (IReportWizardPage) prevWizardPage;
			prevReportWizardPage.init(reportParameter);

			currentWizardPage = prevReportWizardPage;
		}
	}


	@Override
	public void finishPressed(IReportWizardPage currentPage) {
		// save the user input of the current page
		currentPage.saveReportParameters();
	}


	@Override
	public void cancelPressed(IReportWizardPage currentPage) {
	}


	/**
	 * Determine the first {@link IWizardPage} on which the user is able to finish the {@link Wizard}.
	 * By default this is the last {@link IWizardPage} except {@link LanguageWizardPage} and
	 * {@link DocumentFormatWizardPage}.
	 * @return
	 */
	protected IWizardPage getFirstFinishablePage() {
		IWizardPage firstFinishablePage = null;
		for (IWizardPage page : getPages()) {
			if (page instanceof LanguageWizardPage || page instanceof DocumentFormatWizardPage) {
				if (firstFinishablePage != null) {
					break;
				}
			}
			firstFinishablePage = page;
		}
		return firstFinishablePage;
	}


	protected Collection<IWizardPage> getFinishablePages() {
		List<IWizardPage> finishablePages = new ArrayList<>();

		IWizardPage firstFinishablePage = getFirstFinishablePage();

		boolean canFinish = false;
		for (IWizardPage page : getPages()) {
			if (page == firstFinishablePage) {
				canFinish = true;
			}

			if (canFinish) {
				finishablePages.add(page);
			}
		}

		return finishablePages;
	}


	@Override
	public boolean canFinish() {
		/* The wizard can not be finished unless the user reached the last non-standard page.
		 * Herewith we force the user to pass all non-standard pages.
		 * This is necessary, because the user might change a parameter on the first page which makes parameters on
		 * following pages invalid. E.g. selecting another Event makes the selection of Programme Points invalid.
		 */
		IReportWizardPage currentPage = (IReportWizardPage) getCurrentWizardPage();
		Collection<IWizardPage> finishablePages = getFinishablePages();
		if ( finishablePages.contains(currentPage) ) {
			currentPage.saveReportParameters();
			return reportParameter.isComplete();
		}
		return false;
	}


	@Override
	public Point getPreferredSize() {
		return new Point(800, 400);
	}


	public IWizardPage getCurrentWizardPage() {
		return currentWizardPage;
	}

}

