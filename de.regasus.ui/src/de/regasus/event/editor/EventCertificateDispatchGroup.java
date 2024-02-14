package de.regasus.event.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.I18NMinute;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.datetime.TimeComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class EventCertificateDispatchGroup extends EntityGroup<EventVO> {


	private final int COL_COUNT = 6;


	// widgets
	private DateComposite beginDateComposite;
	private DateComposite endDateComposite;
	private TimeComposite timeComposite;
	private Label conclusionLabel;


	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public EventCertificateDispatchGroup(Composite parent, int style)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style);

		setText( ParticipantLabel.CertificateEmailDispatch.getString() );
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER);


		Label descriptionLabel = new Label(this, SWT.WRAP);
		GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.span(COL_COUNT, 1)
			.applyTo(descriptionLabel);
		descriptionLabel.setText( ParticipantLabel.CertificateEmailDispatch_Description.getString() );


		SWTHelper.verticalSpace(this);


		// begin
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( EventVO.CERTIFICATE_EMAIL_DISPATCH_BEGIN_DATE.getLabel() );
    		label.setToolTipText( EventVO.CERTIFICATE_EMAIL_DISPATCH_BEGIN_DATE.getDescription() );

    		beginDateComposite = new DateComposite(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(beginDateComposite);
    		beginDateComposite.addModifyListener(modifySupport);
		}

		// end
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
    		label.setText( EventVO.CERTIFICATE_EMAIL_DISPATCH_END_DATE.getLabel() );
    		label.setToolTipText( EventVO.CERTIFICATE_EMAIL_DISPATCH_END_DATE.getDescription() );

			endDateComposite = new DateComposite(this, SWT.BORDER);
			widgetGridDataFactory.applyTo(endDateComposite);
			endDateComposite.addModifyListener(modifySupport);
		}

		// time
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
    		label.setText( EventVO.CERTIFICATE_EMAIL_DISPATCH_TIME.getLabel() );
    		label.setToolTipText( EventVO.CERTIFICATE_EMAIL_DISPATCH_TIME.getDescription() );

			timeComposite = new TimeComposite(this, SWT.BORDER);
			widgetGridDataFactory.applyTo(timeComposite);
			timeComposite.addModifyListener(modifySupport);
		}


		// conclusion
		conclusionLabel = new Label(this, SWT.WRAP);
		GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.span(COL_COUNT, 1)
			.applyTo(conclusionLabel);

		modifySupport.addListener( new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateConclusion();
			}
		});
	}


	private void updateConclusion() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				String conclusionText = "";

				I18NMinute time = timeComposite.getI18NMinute();
				I18NDate begin = beginDateComposite.getI18NDate();
				I18NDate end = endDateComposite.getI18NDate();
				if (time == null) {
					conclusionText = ParticipantLabel.CertificateEmailDispatch_Conclusion_TimeIsNull.getString();
				}
				else if (begin == null && end == null) {
					conclusionText = ParticipantLabel.CertificateEmailDispatch_Conclusion_TimeIsNotNull_BeginIsNull_EndIsNull.getString();
				}
				else if (begin != null && end != null) {
					conclusionText = ParticipantLabel.CertificateEmailDispatch_Conclusion_TimeIsNotNull_BeginIsNotNull_EndIsNotNull.getString();
				}
				else if (begin != null && end == null) {
					conclusionText = ParticipantLabel.CertificateEmailDispatch_Conclusion_TimeIsNotNull_BeginIsNotNull_EndIsNull.getString();
				}
				else if (begin == null && end != null) {
					conclusionText = ParticipantLabel.CertificateEmailDispatch_Conclusion_TimeIsNotNull_BeginIsNull_EndIsNotNull.getString();
				}

				if (time != null) {
					conclusionText = conclusionText.replace("<time>", time.getString());
				}

				if (begin != null) {
					conclusionText = conclusionText.replace("<begin>", begin.getString());
				}

				if (end != null) {
					conclusionText = conclusionText.replace("<end>", end.getString());
				}

				conclusionLabel.setText(conclusionText);

				layout();
			}
		});
	}


	@Override
	protected void syncWidgetsToEntity() {
		beginDateComposite.setI18NDate( entity.getCertificateEmailDispatchBeginDate() );
		endDateComposite.setI18NDate( entity.getCertificateEmailDispatchEndDate() );
		timeComposite.setI18NMinute( entity.getCertificateEmailDispatchTime() );

		updateConclusion();
	}


	@Override
	public void syncEntityToWidgets() {
		entity.setCertificateEmailDispatchBeginDate( beginDateComposite.getI18NDate() );
		entity.setCertificateEmailDispatchEndDate( endDateComposite.getI18NDate() );
		entity.setCertificateEmailDispatchTime( timeComposite.getI18NMinute() );
	}

}
