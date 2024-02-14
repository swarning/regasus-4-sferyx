package de.regasus.programme.programmepoint.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.location.combo.LocationCombo;
import de.regasus.ui.Activator;


public class ProgrammePointAccessControlComposite extends Composite {

	// the entity
	private ProgrammePointVO programmePointVO;

	// modifyListeners
	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private DateTimeComposite realStartTime;
	private DateTimeComposite realEndTime;
	private DateTimeComposite entranceTime;
	private DateTimeComposite exitTime;
	private LocationCombo location;
	private DecimalNumberText creditPoints;
	private NullableSpinner creditDuration;
	private MultiLineText leadPolicy;
	private Text cmeEventNoText;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ProgrammePointAccessControlComposite(Composite parent, int style) throws Exception {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(4, false);
		setLayout(gridLayout);

		GridDataFactory labelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory dateGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER)
			.grab(true, false);

		GridDataFactory singleColumnGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		GridDataFactory multiColumnGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false)
			.span(gridLayout.numColumns - 1, 1);

		SWTHelper.verticalSpace(this, 5);


		// realStartTime
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.ProgrammePoint_RealStartTime.getString() );
		}
		{
			realStartTime = new DateTimeComposite(this, SWT.NONE);
			dateGridDataFactory.applyTo(realStartTime);
			WidgetSizer.setWidth(realStartTime);

			realStartTime.addModifyListener(modifySupport);
		}


		// realEndTime
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.ProgrammePoint_RealEndTime.getString() );
		}
		{
			realEndTime = new DateTimeComposite(this, SWT.NONE);
			dateGridDataFactory.applyTo(realEndTime);
			WidgetSizer.setWidth(realEndTime);

			realEndTime.addModifyListener(modifySupport);
		}


		// entranceTime
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.ProgrammePoint_EntranceTime.getString() );
		}
		{
			entranceTime = new DateTimeComposite(this, SWT.NONE);
			dateGridDataFactory.applyTo(entranceTime);
			WidgetSizer.setWidth(entranceTime);

			entranceTime.addModifyListener(modifySupport);
		}


		// exitTime
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.ProgrammePoint_ExitTime.getString() );
		}
		{
			exitTime = new DateTimeComposite(this, SWT.NONE);
			dateGridDataFactory.applyTo(exitTime);
			WidgetSizer.setWidth(exitTime);

			exitTime.addModifyListener(modifySupport);
		}


		SWTHelper.verticalSpace(this, 5);


		// location
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.Location.getString() );
		}
		{
			location = new LocationCombo(this, SWT.READ_ONLY);
			multiColumnGridDataFactory.applyTo(location);

			location.addModifyListener(modifySupport);
		}


		// cmeEventNo
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.CmeEventNo.getString() );
		}
		{
			cmeEventNoText = new Text(this, SWT.BORDER);
			multiColumnGridDataFactory.applyTo(cmeEventNoText);

			cmeEventNoText.addModifyListener(modifySupport);
		}


		// creditPoints
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.ProgrammePoint_CreditPoints.getString() );
		}
		{
			creditPoints = new DecimalNumberText(this, SWT.NONE);
			creditPoints.setFractionDigits(2);
			creditPoints.setValue(0.00);
			creditPoints.setNullAllowed(true);
			singleColumnGridDataFactory.applyTo(creditPoints);

			creditPoints.addModifyListener(modifySupport);
		}


		// creditDuration
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.copy().indent(10, 0).applyTo(label);

			label.setText( ParticipantLabel.ProgrammePoint_CreditDuration.getString() );
		}
		{
			creditDuration = new NullableSpinner(this, SWT.NONE);
			creditDuration.setMinimum(0);
			singleColumnGridDataFactory.applyTo(creditDuration);

			creditDuration.addModifyListener(modifySupport);
		}


		// leadPolicy
		{
			Label label = new Label(this, SWT.NONE);
			GridDataFactory
    			.swtDefaults()
    			.align(SWT.RIGHT, SWT.TOP)
    			.indent(0, SWTConstants.VERTICAL_INDENT)
    			.applyTo(label);
			label.setText( ParticipantLabel.ProgrammePoint_LeadPolicy.getString() );
		}
		{
			leadPolicy = new MultiLineText(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, false);
			GridDataFactory
				.fillDefaults()
				.grab(true, true)
				.span(3, 1)
				.applyTo(leadPolicy);

			leadPolicy.addModifyListener(modifySupport);
		}
	}


	private void syncWidgetsToEntity() {
		if (programmePointVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						realStartTime.setDate(programmePointVO.getRealStartTime());
						realEndTime.setDate(programmePointVO.getRealEndTime());
						entranceTime.setDate(programmePointVO.getEntranceTime());
						exitTime.setDate(programmePointVO.getExitTime());

						location.setEventPK(programmePointVO.getEventPK());
						location.setLocationPK(programmePointVO.getLocationPK());

						creditPoints.setValue(programmePointVO.getCreditPoints());
						creditDuration.setValue(programmePointVO.getCreditDuration());

						leadPolicy.setText( avoidNull( programmePointVO.getLeadPolicySource() ) );

						cmeEventNoText.setText(StringHelper.avoidNull(programmePointVO.getCmeEventNo()));
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
			programmePointVO.setRealStartTime( realStartTime.getDate() );
			programmePointVO.setRealEndTime( realEndTime.getDate() );
			programmePointVO.setEntranceTime( entranceTime.getDate() );
			programmePointVO.setExitTime( exitTime.getDate() );

			programmePointVO.setLocationPK(location.getLocationPK());
			programmePointVO.setCreditPoints(creditPoints.getValue());
			programmePointVO.setCreditDuration(creditDuration.getValueAsInteger());

			String leadPolicySource = leadPolicy.getText();
			if (leadPolicySource != null && leadPolicySource.length() > 0) {
				XMLContainer leadPolicyXMLContainer = new XMLContainer(leadPolicySource);
				programmePointVO.setLeadPolicyXML(leadPolicyXMLContainer);
			}
			else {
				programmePointVO.setLeadPolicyXML(null);
			}

			programmePointVO.setCmeEventNo(StringHelper.trim(cmeEventNoText.getText()));
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


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
