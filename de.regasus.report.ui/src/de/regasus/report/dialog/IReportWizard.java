package de.regasus.report.dialog;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.xml.XMLContainer;

public interface IReportWizard extends IWizard {

	/**
	 * Initialisiert den Wizard.
	 * 
	 * Achtung: Der übergebene xmlRequest wird vom Wizard evtl. verändert.
	 * Wenn dies nicht gewünscht ist, sollte dieser Parameter zuvor geknont werden.
	 * 
	 * @param userReportPK PK des UserReports
	 * @param xmlRequest Reportparameter in Form von XML, Vorsicht, der Wizard wird diese evtl. verändern!
	 */
	void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest);
	IReportParameter getReportParameter();
	
	void nextPressed(IReportWizardPage currentPage);
	void backPressed(IReportWizardPage currentPage);
	void finishPressed(IReportWizardPage currentPage);
	void cancelPressed(IReportWizardPage currentPage);
	
	Point getPreferredSize();
}
