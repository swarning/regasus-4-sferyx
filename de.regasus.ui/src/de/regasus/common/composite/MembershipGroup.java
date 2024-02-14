package de.regasus.common.composite;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.MembershipConfigParameterSet;
import com.lambdalogic.messeinfo.contact.data.Membership;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.datetime.DateComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

//REFERENCE
public class MembershipGroup extends EntityGroup<Membership> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	// widgets
	private Text statusText;
	private Text typeText;
	private DateComposite beginDateComposite;
	private DateComposite endDateComposite;


	// config flags
	private boolean showStatus;
	private boolean showType;
	private boolean showBegin;
	private boolean showEnd;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @param membershipConfigParameterSet
	 * @throws Exception
	 */
	public MembershipGroup(
		Composite parent,
		int style,
		MembershipConfigParameterSet membershipConfigParameterSet
	)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style, membershipConfigParameterSet);

		setText( Participant.MEMBERSHIP.getString() );
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		try {
			MembershipConfigParameterSet configParameterSet = (MembershipConfigParameterSet) initValues[0];
    		if (configParameterSet != null) {
    			showStatus	= configParameterSet.getStatus().isVisible();
    			showType	= configParameterSet.getType().isVisible();
    			showBegin	= configParameterSet.getBegin().isVisible();
    			showEnd		= configParameterSet.getEnd().isVisible();
    		}
    		else {
    			showStatus = true;
    			showType = true;
    			showBegin = true;
    			showEnd = true;
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		// status
		if (showStatus) {
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( Membership.STATUS.getLabel() );
    		label.setToolTipText( Membership.STATUS.getDescription() );

    		statusText = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(statusText);
    		statusText.setTextLimit( Membership.STATUS.getMaxLength() );
    		statusText.addModifyListener(modifySupport);
		}

		// type
		if (showType) {
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( Membership.TYPE.getLabel() );
    		label.setToolTipText( Membership.TYPE.getDescription() );

    		typeText = new Text(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(typeText);
    		typeText.setTextLimit( Membership.TYPE.getMaxLength() );
    		typeText.addModifyListener(modifySupport);
		}

		// begin
		if (showBegin) {
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( Membership.BEGIN.getLabel() );
    		label.setToolTipText( Membership.BEGIN.getDescription() );

    		beginDateComposite = new DateComposite(this, SWT.BORDER);
    		widgetGridDataFactory.applyTo(beginDateComposite);
    		beginDateComposite.addModifyListener(modifySupport);
		}

		// end
		if (showEnd) {
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( Membership.END.getLabel() );
			label.setToolTipText( Membership.END.getDescription() );

			endDateComposite = new DateComposite(this, SWT.BORDER);
			widgetGridDataFactory.applyTo(endDateComposite);
			endDateComposite.addModifyListener(modifySupport);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		if (statusText != null) {
			statusText.setText( avoidNull(entity.getStatus()) );
		}

		if (typeText != null) {
			typeText.setText( avoidNull(entity.getType()) );
		}

		if (beginDateComposite != null) {
			beginDateComposite.setI18NDate( entity.getBegin() );
		}

		if (endDateComposite != null) {
			endDateComposite.setI18NDate( entity.getEnd() );
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (statusText != null) {
			entity.setStatus( statusText.getText() );
		}

		if (typeText != null) {
			entity.setType( typeText.getText() );
		}

		if (beginDateComposite != null) {
			entity.setBegin( beginDateComposite.getI18NDate() );
		}

		if (endDateComposite != null) {
			entity.setEnd( endDateComposite.getI18NDate() );
		}
	}

}
