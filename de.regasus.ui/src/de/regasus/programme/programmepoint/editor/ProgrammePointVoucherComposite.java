package de.regasus.programme.programmepoint.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class ProgrammePointVoucherComposite extends Composite {

	protected static final int VOUCHER_MIN_LINE_COUNT = 3;

	// the entity
	private ProgrammePointVO programmePointVO;

	// modifyListeners
	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private MultiLineText voucher1;
	private MultiLineText voucher2;
	private MultiLineText voucher3;
	private MultiLineText voucher4;
	private MultiLineText voucher5;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ProgrammePointVoucherComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));
		{
			Label voucher1Label = new Label(this, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				voucher1Label.setLayoutData(gridData);
			}
			voucher1Label.setText(ParticipantLabel.ProgrammePoint_VoucherText1.getString());
		}
		{
			voucher1 = new MultiLineText(this, SWT.BORDER);
			voucher1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			voucher1.setMinLineCount(VOUCHER_MIN_LINE_COUNT);

			voucher1.addModifyListener(modifySupport);
		}

		{
			Label voucher2Label = new Label(this, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				voucher2Label.setLayoutData(gridData);
			}
			voucher2Label.setText(ParticipantLabel.ProgrammePoint_VoucherText2.getString());
		}
		{
			voucher2 = new MultiLineText(this, SWT.BORDER);
			voucher2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			voucher2.setMinLineCount(VOUCHER_MIN_LINE_COUNT);

			voucher2.addModifyListener(modifySupport);
		}
		{
			Label voucher3Label = new Label(this, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				voucher3Label.setLayoutData(gridData);
			}
			voucher3Label.setText(ParticipantLabel.ProgrammePoint_VoucherText3.getString());
		}
		{
			voucher3 = new MultiLineText(this, SWT.BORDER);
			voucher3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			voucher3.setMinLineCount(VOUCHER_MIN_LINE_COUNT);

			voucher3.addModifyListener(modifySupport);
		}
		{
			Label voucher4Label = new Label(this, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				voucher4Label.setLayoutData(gridData);
			}
			voucher4Label.setText(ParticipantLabel.ProgrammePoint_VoucherText4.getString());
		}
		{
			voucher4 = new MultiLineText(this, SWT.BORDER);
			voucher4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			voucher4.setMinLineCount(VOUCHER_MIN_LINE_COUNT);

			voucher4.addModifyListener(modifySupport);
		}
		{
			Label voucher5Label = new Label(this, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				voucher5Label.setLayoutData(gridData);
			}
			voucher5Label.setText(ParticipantLabel.ProgrammePoint_VoucherText5.getString());
		}
		{
			voucher5 = new MultiLineText(this, SWT.BORDER);
			voucher5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			voucher5.setMinLineCount(VOUCHER_MIN_LINE_COUNT);

			voucher5.addModifyListener(modifySupport);
		}

	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	private void syncWidgetsToEntity() {
		if (programmePointVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						voucher1.setText(StringHelper.avoidNull(programmePointVO.getVoucherText1()));
						voucher2.setText(StringHelper.avoidNull(programmePointVO.getVoucherText2()));
						voucher3.setText(StringHelper.avoidNull(programmePointVO.getVoucherText3()));
						voucher4.setText(StringHelper.avoidNull(programmePointVO.getVoucherText4()));
						voucher5.setText(StringHelper.avoidNull(programmePointVO.getVoucherText5()));
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (programmePointVO != null) {
			programmePointVO.setVoucherText1(voucher1.getText());
			programmePointVO.setVoucherText2(voucher2.getText());
			programmePointVO.setVoucherText3(voucher3.getText());
			programmePointVO.setVoucherText4(voucher4.getText());
			programmePointVO.setVoucherText5(voucher5.getText());
		}
	}


	/**
	 * Set programm point VO entity in all GUI components that need it.
	 * @param programmePointVO Programm point VO to set.
	 */
	public void setProgrammePointVO(ProgrammePointVO programmePointVO) {
		this.programmePointVO = programmePointVO;
		syncWidgetsToEntity();
	}

}