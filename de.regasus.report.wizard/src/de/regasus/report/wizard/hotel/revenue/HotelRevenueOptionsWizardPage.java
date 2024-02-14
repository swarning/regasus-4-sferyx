package de.regasus.report.wizard.hotel.revenue;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.report.revenue.HotelRevenueReportDataBasis;
import com.lambdalogic.messeinfo.hotel.report.revenue.HotelRevenueReportGroupBy;
import com.lambdalogic.messeinfo.hotel.report.revenue.HotelRevenueReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;


public class HotelRevenueOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "HotelRevenueOptionsWizardPage";

	private HotelRevenueReportParameter parameter;


	// Widgets
	private Button dataBasisBookingsButton;
	private Button dataBasisContingentsButton;

	private Button groupByHotelButton;
	private Button groupByHotelChainButton;


	public HotelRevenueOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.HotelRevenueOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.HotelRevenueOptionsWizardPage_Description);
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		//
		setControl(container);

		Composite centerComposite = new Composite(container, SWT.NONE);
		centerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		centerComposite.setLayout( new GridLayout(4, false) );

		// Data Basis
		Group dataBasisGroup = new Group(centerComposite, SWT.NONE);
		dataBasisGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		dataBasisGroup.setText( HotelLabel.HotelRevenueReport_DataBasis.getString() );
		dataBasisGroup.setLayout(new GridLayout(1, false));

		dataBasisBookingsButton = new Button(dataBasisGroup, SWT.RADIO);
		dataBasisBookingsButton.setText( HotelRevenueReportDataBasis.BOOKINGS.getString() );
		dataBasisBookingsButton.setToolTipText(ReportWizardI18N.HotelRevenueOptionsWizardPage_DataBasis_BOOKINGS_Description);

		dataBasisContingentsButton = new Button(dataBasisGroup, SWT.RADIO);
		dataBasisContingentsButton.setText( HotelRevenueReportDataBasis.CONTINGENTS.getString() );
		dataBasisContingentsButton.setToolTipText(ReportWizardI18N.HotelRevenueOptionsWizardPage_DataBasis_CONTINGENTS_Description);


		// Data Basis
		Group groupByGroup = new Group(centerComposite, SWT.NONE);
		groupByGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		groupByGroup.setText( HotelLabel.HotelRevenueReport_DataBasis.getString() );
		groupByGroup.setLayout(new GridLayout(1, false));

		groupByHotelButton = new Button(groupByGroup, SWT.RADIO);
		groupByHotelButton.setText( HotelRevenueReportGroupBy.HOTEL.getString() );
		groupByHotelButton.setToolTipText(ReportWizardI18N.HotelRevenueOptionsWizardPage_GroupBy_HOTEL_Description);

		groupByHotelChainButton = new Button(groupByGroup, SWT.RADIO);
		groupByHotelChainButton.setText( HotelRevenueReportGroupBy.HOTEL_CHAIN.getString() );
		groupByHotelChainButton.setToolTipText(ReportWizardI18N.HotelRevenueOptionsWizardPage_GroupBy_HOTEL_CHAIN_Description);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof HotelRevenueReportParameter) {
			parameter = (HotelRevenueReportParameter) reportParameter;

			HotelRevenueReportDataBasis dataBasis = parameter.getDataBasis();
			if (dataBasis == null) {
				// set default value
				dataBasis = HotelRevenueReportParameter.DEFAULT_DATA_BASIS;
			}
			dataBasisBookingsButton.setSelection(dataBasis == HotelRevenueReportDataBasis.BOOKINGS);
			dataBasisContingentsButton.setSelection(dataBasis == HotelRevenueReportDataBasis.CONTINGENTS);

			HotelRevenueReportGroupBy groupBy = parameter.getGroupBy();
			if (groupBy == null) {
				// set default value
				groupBy = HotelRevenueReportParameter.DEFAULT_GROUP_BY;
			}
			groupByHotelButton.setSelection(groupBy == HotelRevenueReportGroupBy.HOTEL);
			groupByHotelChainButton.setSelection(groupBy == HotelRevenueReportGroupBy.HOTEL_CHAIN);

		}

		// save eventually set default values
		saveReportParameters();
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void saveReportParameters() {
		// determine values from widgets
		HotelRevenueReportDataBasis dataBasis = HotelRevenueReportParameter.DEFAULT_DATA_BASIS;
		if (dataBasisBookingsButton.getSelection()) {
			dataBasis = HotelRevenueReportDataBasis.BOOKINGS;
		}
		else if (dataBasisContingentsButton.getSelection()) {
			dataBasis = HotelRevenueReportDataBasis.CONTINGENTS;
		}

		HotelRevenueReportGroupBy groupBy = HotelRevenueReportParameter.DEFAULT_GROUP_BY;
		if (groupByHotelButton.getSelection()) {
			groupBy = HotelRevenueReportGroupBy.HOTEL;
		}
		else if (groupByHotelChainButton.getSelection()) {
			groupBy = HotelRevenueReportGroupBy.HOTEL_CHAIN;
		}


		// set values in ReportParameter
		parameter.setDataBasis(dataBasis);
		parameter.setGroupBy(groupBy);


		// set description
		StringBuilder desc = new StringBuilder();

		desc.append( HotelLabel.HotelRevenueReport_DataBasis.getString() );
		desc.append(": ");
		desc.append( dataBasis.getString() );
		parameter.setDescription(HotelRevenueReportParameter.DESCRIPTION_ID_DATA_BASIS, desc.toString());

		desc.setLength(0);
		desc.append( HotelLabel.HotelRevenueReport_GroupBy.getString() );
		desc.append(": ");
		desc.append( groupBy.getString() );
		parameter.setDescription(HotelRevenueReportParameter.DESCRIPTION_ID_GROUP_BY, desc.toString());
	}

}
