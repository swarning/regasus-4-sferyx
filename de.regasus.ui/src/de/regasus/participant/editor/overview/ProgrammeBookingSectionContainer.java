/**
 * ProgrammeBookingSectionContainer.java
 * created on 18.07.2013 12:48:24
 */
package de.regasus.participant.editor.overview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO_Position_BenRecipName_Comparator;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.Tuple;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.ui.Activator;

public class ProgrammeBookingSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantPK;

	private ProgrammeBookingModel programmeBookingModel;

	private ConfigParameterSet configParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public ProgrammeBookingSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(formToolkit, body, 2);

		this.participantPK = participantID;
		this.configParameterSet = configParameterSet;

		addDisposeListener(this);

		programmeBookingModel = ProgrammeBookingModel.getInstance();
		programmeBookingModel.addForeignKeyListener(this, participantPK);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return ParticipantLabel.ProgrammeBookings.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

    		List<ProgrammeBookingCVO> bookingCVOs = programmeBookingModel.getProgrammeBookingCVOsByRecipient(participantPK);
    		bookingCVOs = CollectionsHelper.createArrayList(bookingCVOs);

    		boolean visible =
    			configParameterSet == null ||
    			configParameterSet.getEvent().getProgramme().isVisible();

    		if (visible) {
    			// set visible true, if at least 1 booking exists that is not canceled
    			visible = false;
        		if (CollectionsHelper.notEmpty(bookingCVOs)) {
        			for (ProgrammeBookingCVO bookingCVO : bookingCVOs) {
        				if ( ! bookingCVO.isCanceled()) {
        					visible = true;
        					break;
        				}
        			}
        		}
    		}

    		setVisible(visible);

    		if (visible) {
    			Map<Long, Tuple<String, Integer>> pp2labelValueTupleMap = MapHelper.createHashMap(bookingCVOs.size());

    			List<Tuple<String, Integer>> labelValueTuples =
    				new ArrayList<Tuple<String, Integer>>(bookingCVOs.size());

    			// sort programme bookings to order them by programme point position
    			Collections.sort(bookingCVOs, ProgrammeBookingCVO_Position_BenRecipName_Comparator.getInstance());

    			for (ProgrammeBookingCVO bookingCVO : bookingCVOs) {
    				if ( ! bookingCVO.isCanceled()) {
    					Long ppPK = bookingCVO.getProgrammeOfferingCVO().getProgrammePointCVO().getPK();
        				String ppName = bookingCVO.getPpName().getString();

        				Tuple<String, Integer> labelValueTuple = pp2labelValueTupleMap.get(ppPK);
        				if (labelValueTuple == null) {
        					labelValueTuple = new Tuple<String, Integer>(ppName, 1);
        					pp2labelValueTupleMap.put(ppPK, labelValueTuple);
        					labelValueTuples.add(labelValueTuple);
        				}
        				else {
        					int count = labelValueTuple.getB();
        					count++;
        					labelValueTuple.setB(count);
        				}
    				}
    			}


    			for (Tuple<String, Integer> tuple : labelValueTuples) {
    				addIfNotEmpty(tuple.getA(), tuple.getB());
    			}
    		}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (programmeBookingModel != null && participantPK != null) {
			try {
				programmeBookingModel.removeForeignKeyListener(this, participantPK);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
