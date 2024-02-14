package de.regasus.participant.editor.lead;

import static de.regasus.LookupService.getLeadMgr;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.LeadVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantLeadsCVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.auth.AuthorizationException;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class LeadsComposite extends Composite implements CacheModelListener<Long>, DisposeListener {

	private AttendenceTableComposite presentAttendenceTableComposite;

	private AttendenceTableComposite passedAttendendenceTableComposite;

	private Table table;

	private TableViewer leadsTableViewer;

	private Participant participant;

	private boolean isLoaded;

	private ParticipantModel participantModel = ParticipantModel.getInstance();



	public LeadsComposite(Composite parent, int style) {
		super(parent, style);

		addDisposeListener(this);

		setLayout(new FillLayout());

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		buildTopComposite(sashForm);
		buildBottomComposite(sashForm);
	}


	private Composite buildTopComposite(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		// First Row: Two tables for current and passed programme point attendencies

		presentAttendenceTableComposite = new AttendenceTableComposite(
			sashForm,
			SWT.NONE,
			ParticipantLabel.Present.getString()
		);
		presentAttendenceTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		passedAttendendenceTableComposite = new AttendenceTableComposite(
			sashForm,
			SWT.NONE,
			ParticipantLabel.Passed.getString()
		);
		passedAttendendenceTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return sashForm;
	}


	private Composite buildBottomComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout( new GridLayout() );

		// Second row: Title for next table

		Label label = new Label(composite, SWT.NONE);
		label.setText(ParticipantLabel.Leads.getString());
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Third row: One table for all leads
		Composite tableComposite = new Composite(composite, SWT.BORDER);

		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		table = new Table(tableComposite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Create the columns Richtung, Erfassungszeit, Programmpunkt, Ort, Quelle, Response

		// Richtung
		final TableColumn directionTableColumn = new TableColumn(table, SWT.CENTER);
		directionTableColumn.setText(ParticipantLabel.Dir.getString());
		layout.setColumnData(directionTableColumn, new ColumnWeightData(10));

		// Erfassungszeit
		final TableColumn collectTimeTableColumn = new TableColumn(table, SWT.RIGHT);
		collectTimeTableColumn.setText(ParticipantLabel.CollectTime.getString());
		layout.setColumnData(collectTimeTableColumn, new ColumnWeightData(12));

		// Programmpunkt
		final TableColumn programmePointTableColumn = new TableColumn(table, SWT.LEFT);
		programmePointTableColumn.setText(ParticipantLabel.ProgrammePoint.getString());
		layout.setColumnData(programmePointTableColumn, new ColumnWeightData(15));

		// Ort
		final TableColumn locationTableColumn = new TableColumn(table, SWT.LEFT);
		locationTableColumn.setText(ParticipantLabel.Location.getString());
		layout.setColumnData(locationTableColumn, new ColumnWeightData(15));

		// Quelle
		final TableColumn sourceTableColumn = new TableColumn(table, SWT.LEFT);
		sourceTableColumn.setText(ParticipantLabel.Source.getString());
		layout.setColumnData(sourceTableColumn, new ColumnWeightData(10));

		// Response
		final TableColumn responseTableColumn = new TableColumn(table, SWT.LEFT);
		responseTableColumn.setText(ParticipantLabel.Response.getString());
		layout.setColumnData(responseTableColumn, new ColumnWeightData(10));

		LeadsTable leadsTable = new LeadsTable(table);
		leadsTableViewer = leadsTable.getViewer();


		return composite;
	}


	public void setParticipant(final Participant participant) {
		if (this.participant != null && this.participant.getID() != null && !this.participant.equals(participant)) {
			participantModel.removeForeignKeyListener(this, this.participant.getID());
		}
		if (participant != null && participant.getID() != null && !participant.equals(this.participant)) {
			participantModel.addForeignKeyListener(this, participant.getID());
		}

		this.participant = participant;

		loadAndShow();
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (participant != null && participant.getID() != null) {
			participantModel.removeForeignKeyListener(this, participant.getID());
		}
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			loadAndShow(true);
		}
	}


	private void loadAndShow() {
		loadAndShow(false);
	}


	public void loadAndShow(final boolean force) {
		// The leads aren't cached or retrieved by any model
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (   participant != null
						&& isVisible()
						&& (!isLoaded || force)
					) {
						Long participantPK = participant.getID();
						ParticipantLeadsCVO participantLeadsCVO = getLeadMgr().getParticipantLeadsCVO(participantPK);

						List<LeadVO> leadVOs = participantLeadsCVO.getLeadVOs();
						leadsTableViewer.setInput(leadVOs);

						Collection<Long> presentProgrammePointPKs = participantLeadsCVO.getPresentProgrammePointPKs();
						presentAttendenceTableComposite.getTableViewer().setInput(presentProgrammePointPKs);

						Collection<Long> passedProgrammePointPKs = participantLeadsCVO.getPassedProgrammePointPKs();
						passedAttendendenceTableComposite.getTableViewer().setInput(passedProgrammePointPKs);

						isLoaded = true;
					}
				}
				catch (AuthorizationException e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	/**
	 * If this composite is showing a participant that is to be refreshed or updated, store the info that the
	 * data needs to be reload; if this composite is even visible, also do the reload
	 */
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (participant != null &&
			(event.getKeyList().isEmpty() || event.getKeyList().contains(participant.getPK()))
		) {
			CacheModelOperation operation = event.getOperation();

			// Don't need to load leads data when participant gets created or deleted
			if (operation == CacheModelOperation.REFRESH || operation == CacheModelOperation.UPDATE) {
				isLoaded = false;

				// Avoid org.eclipse.swt.SWTException: Invalid thread access
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						// If this composite is even visible, also do the reload
						if (!isDisposed() && isVisible()) {
							loadAndShow();
						}
					}
				});
			}
		}
	}

}
