package de.regasus.hotel.contingent.editor;

import static com.lambdalogic.messeinfo.hotel.data.HotelContingentVO.*;
import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabel;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;

import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.VolumeVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.simpleviewer.ITableEditListener;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class CapacityComposite extends Composite {

	// the entity
	private HotelContingentCVO hotelContingentCVO;

	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private DateComposite firstDayDateComposite;
	private DateComposite lastDayDateComposite;
	private NullableSpinner minimumStaySpinner;
	private VolumeTable volumeTable;
	private TableViewer volumeTableViewer;

	// additional variables
	private boolean ignoreDayChange;
	private boolean volumeRangeOK = true;


	public CapacityComposite(
		Composite parent,
		int style,
		HotelConfigParameterSet hotelConfigParameterSet
	) {
		super(parent, style);

		if (hotelConfigParameterSet == null) {
			hotelConfigParameterSet = new HotelConfigParameterSet();
		}

		try {
			setLayout(new GridLayout(1, false));

			// create Widgets

			final Composite dayComposite = new Composite(this, SWT.NONE);
			{
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
				dayComposite.setLayoutData(layoutData);
				dayComposite.setLayout(new GridLayout(6, false));
			}

			// firstDay
			{
    			createLabel(dayComposite, HotelLabel.First_Day.getString());

    			firstDayDateComposite = new DateComposite(dayComposite, SWT.BORDER);
    			WidgetSizer.setWidth(firstDayDateComposite);

    			// when the firstDay is changed, volumes need to be added or removed (if possible at all)
    			firstDayDateComposite.addModifyListener(new ModifyListener() {
    				@Override
					public void modifyText(ModifyEvent e) {
    					if (!ignoreDayChange) {
    						adaptStartVolumes();
    					}
    				}
    			});

    			firstDayDateComposite.addModifyListener(modifySupport);
			}

			// lastDay
			{
    			createLabel(dayComposite, HotelLabel.Last_Day.getString());

    			lastDayDateComposite = new DateComposite(dayComposite, SWT.BORDER);
    			WidgetSizer.setWidth(lastDayDateComposite);


    			// when the lastDay is changed, volumes need to be added or removed (if possible at all)
    			lastDayDateComposite.addModifyListener(new ModifyListener() {
    				@Override
					public void modifyText(ModifyEvent e) {
    					if (!ignoreDayChange) {
    						adaptEndVolumes();
    					}
    				}
    			});

    			lastDayDateComposite.addModifyListener(modifySupport);
			}

			// minimumStay
			{
				Label label = createLabel(dayComposite, HotelLabel.MinimumStay.getString());
				label.setToolTipText( HotelLabel.MinimumStay_ToolTip.getString() );


				minimumStaySpinner = new NullableSpinner(dayComposite, SWT.BORDER);
				minimumStaySpinner.setMinimumAndMaximum(MIN_MINIMUM_STAY, MAX_MINIMUM_STAY);
				WidgetSizer.setWidth(minimumStaySpinner);
				minimumStaySpinner.addModifyListener(modifySupport);
			}

			// The table of volumes
			Group volumeTableGroup = new Group(this, SWT.NONE);
			volumeTableGroup.setText(I18N.HotelContingentEditor_RoomCapacities);
			volumeTableGroup.setLayout(new FillLayout());
			{
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
				layoutData.heightHint = 50;
				volumeTableGroup.setLayoutData(layoutData);
			}

			Composite volumeTableComposite = new Composite(volumeTableGroup, SWT.NONE);
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			volumeTableComposite.setLayout(tableColumnLayout);
			Table volumeSWTTable = new Table(volumeTableComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
			volumeSWTTable.setHeaderVisible(true);
			volumeSWTTable.setLinesVisible(true);

			// Date
			TableColumn dateTableColumn = new TableColumn(volumeSWTTable, SWT.RIGHT);
			tableColumnLayout.setColumnData(dateTableColumn, new ColumnWeightData(20));
			dateTableColumn.setText(UtilI18N.Date);

			// Mandatory
			TableColumn mandatoryTableColumn = new TableColumn(volumeSWTTable, SWT.LEFT);
			tableColumnLayout.setColumnData(mandatoryTableColumn, new ColumnWeightData(20));
			mandatoryTableColumn.setText(HotelLabel.Mandatory.getString());

			// TrueSize
			TableColumn trueSizeTableColumn = new TableColumn(volumeSWTTable, SWT.RIGHT);
			tableColumnLayout.setColumnData(trueSizeTableColumn, new ColumnWeightData(20));
			trueSizeTableColumn.setText(I18N.HotelContingentEditor_RoomCapacitiesTable_TrueSize);

			// BookSize
			TableColumn bookSizeTableColumn = new TableColumn(volumeSWTTable, SWT.RIGHT);
			{
    			ColumnWeightData columnWeightData = new ColumnWeightData(20);
    			if (hotelConfigParameterSet.getBookSize().isVisible()) {
    				bookSizeTableColumn.setText(I18N.HotelContingentEditor_RoomCapacitiesTable_BookSize);
    			}
    			else {
    				columnWeightData.weight = 0;
    				columnWeightData.minimumWidth = 0;
    				columnWeightData.resizable = false;
    				bookSizeTableColumn.setResizable(false);
    			}
    			tableColumnLayout.setColumnData(bookSizeTableColumn, columnWeightData);
			}

			// PublicSize
			TableColumn publicSizeTableColumn = new TableColumn(volumeSWTTable, SWT.RIGHT);
			{
				ColumnWeightData columnWeightData = new ColumnWeightData(20);
				if (hotelConfigParameterSet.getPublicSize().isVisible()) {
					publicSizeTableColumn.setText(I18N.HotelContingentEditor_RoomCapacitiesTable_PublicSize);
				}
				else {
    				columnWeightData.weight = 0;
    				columnWeightData.minimumWidth = 0;
    				columnWeightData.resizable = false;
    				publicSizeTableColumn.setResizable(false);
				}
    			tableColumnLayout.setColumnData(publicSizeTableColumn, columnWeightData);
			}

			// Booked
			TableColumn bookedTableColumn = new TableColumn(volumeSWTTable, SWT.RIGHT);
			tableColumnLayout.setColumnData(bookedTableColumn, new ColumnWeightData(20));
			bookedTableColumn.setText(HotelLabel.Booked.getString());

			// Free
			TableColumn freeTableColumn = new TableColumn(volumeSWTTable, SWT.RIGHT);
			tableColumnLayout.setColumnData(freeTableColumn, new ColumnWeightData(20));
			freeTableColumn.setText(HotelLabel.Free.getString());



			volumeTable = new VolumeTable(volumeSWTTable);

			volumeTableViewer = volumeTable.getViewer();

			volumeTable.addEditListener(new ITableEditListener() {
				@Override
				public void tableCellChanged() {
					// fire ModifyEvent via modifySupport
					modifySupport.fire();
				}
			});

		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			ignoreDayChange = true;
			firstDayDateComposite.setLocalDate( TypeHelper.toLocalDate(hotelContingentCVO.getFirstDay()) );
			lastDayDateComposite.setLocalDate( TypeHelper.toLocalDate(hotelContingentCVO.getLastDay()) );
			minimumStaySpinner.setValue( hotelContingentCVO.getMinimumStay() );
			ignoreDayChange = false;

			volumeTableViewer.setInput( hotelContingentCVO.getVolumes() );
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


	public void setEntity(HotelContingentCVO hotelContingentCVO) {
		this.hotelContingentCVO = hotelContingentCVO;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (hotelContingentCVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// set values of entity to widgets

						ignoreDayChange = true;
						firstDayDateComposite.setLocalDate( TypeHelper.toLocalDate(hotelContingentCVO.getFirstDay()) );
						lastDayDateComposite.setLocalDate( TypeHelper.toLocalDate(hotelContingentCVO.getLastDay()) );
						minimumStaySpinner.setValue( hotelContingentCVO.getMinimumStay() );
						ignoreDayChange = false;


						volumeTableViewer.setInput(hotelContingentCVO.getVolumes());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		hotelContingentCVO.getVO().setMinimumStay( minimumStaySpinner.getValueAsInteger() );
	}


	public boolean isNew() {
		return hotelContingentCVO.getPK() == null;
	}

	// **************************************************************************
	// * handle changes of firstDay and lastDay
	// *

	/**
	 * When the first day is changed, volumes need to be added or removed; however, removal is only
	 * possible for days which don't have any size of more then zero.
	 */
	private void adaptStartVolumes() {
		List<VolumeVO> volumeVOs = hotelContingentCVO.getVolumes();

		// determine the desired first date
		I18NDate newFirstDate = firstDayDateComposite.getI18NDate();
		I18NDate newLastDate = lastDayDateComposite.getI18NDate();

		if (newFirstDate != null && newLastDate != null) {
    		int firstYear = newFirstDate.getYear();
    		int lastYear = newLastDate.getYear();
    		int yearRange = lastYear - firstYear;
    		if (yearRange > 1) {
    			if (volumeRangeOK) {
    				MessageDialog.openWarning(
    					getShell(),
    					I18N.HotelContingentEditor_TimePeriodTooLarge_Title,
    					I18N.HotelContingentEditor_TimePeriodTooLarge_Message
    				);
    				volumeRangeOK = false;
    			}
    			return;
    		}
    		else if (!volumeRangeOK) {
    			volumeRangeOK = true;
    			adaptEndVolumes();
    		}

    		// determine the date of the first volume
    		VolumeVO firstVolume = volumeVOs.get(0);
    		I18NDate firstVolumeDate = firstVolume.getDay();


    		// if desired first date is before first volume date, add necessary volumes
    		if ( newFirstDate.isBefore(firstVolumeDate) ) {
    			I18NDate dateToAdd = firstVolumeDate;
    			do {
    				dateToAdd = dateToAdd.minusDays(1);

    				VolumeVO volumeVO = new VolumeVO(
    					hotelContingentCVO.getPK(),
    					hotelContingentCVO.getEventPK(),
    					dateToAdd,	// day
    					false,		// mandatory
    			        0,			// trueSize
    			        0,			// bookSize
    			        null,		// publicSize
    			        0			// used
    				);

    				volumeVOs.add(0, volumeVO);
    			}
    			while ( !newFirstDate.equals(dateToAdd) );

    			volumeTableViewer.refresh();
    		}
    		else if (
    			   !newFirstDate.isAfter(lastDayDateComposite.getI18NDate())
    			&&  newFirstDate.isAfter(firstVolumeDate)
    		) {
    			/* if desired first date after first volume date and all volumes in between have
    			 * zero size: remove volumes
    			 */
    			boolean allVolumesInBetweenHaveZeroSize = true;

    			// Go through all Volumes before desired first date
    			int index = 0;
    			while (index < volumeVOs.size()) {
    				VolumeVO volumeVO = volumeVOs.get(index);
    				if (!volumeVO.getDay().isBefore(newFirstDate)) {
    					break;
    				}
    				if ((volumeVO.getTrueSize()   != null && volumeVO.getTrueSize().intValue() > 0) ||
    					(volumeVO.getBookSize()   != null && volumeVO.getBookSize().intValue() > 0) ||
    					(volumeVO.getPublicSize() != null && volumeVO.getPublicSize().intValue() > 0)
    				) {
    					allVolumesInBetweenHaveZeroSize = false;
    					break;
    				}
    				index++;
    			}
    			// Always remove the first, since buffer shrinks and indices get shifted
    			if (allVolumesInBetweenHaveZeroSize) {
    				for (int i = 0; i < index; i++) {
    					volumeVOs.remove(0);
    				}
    				volumeTableViewer.refresh();
    			}
    			else {
    				resetDayDateComposite(firstDayDateComposite, firstVolumeDate);
    			}
    		}
    		else {
    			resetDayDateComposite(firstDayDateComposite, firstVolumeDate);
    		}
		}
	}


	/**
	 * When the first day is changed, volumes need to be added or removed; however, removal is only
	 * possible for days which don't have any size of more then zero.
	 */
	private void adaptEndVolumes() {
		List<VolumeVO> volumeVOs = hotelContingentCVO.getVolumes();

		// determine the desired last date
		I18NDate newFirstDate = firstDayDateComposite.getI18NDate();
		I18NDate newLastDate = lastDayDateComposite.getI18NDate();

		if (newFirstDate != null && newLastDate != null) {
    		int firstYear = newFirstDate.getYear();
    		int lastYear = newLastDate.getYear();

    		int yearRange = lastYear - firstYear;
    		if (yearRange > 1) {
    			if (volumeRangeOK) {
    				MessageDialog.openWarning(
    					getShell(),
    					I18N.HotelContingentEditor_TimePeriodTooLarge_Title,
    					I18N.HotelContingentEditor_TimePeriodTooLarge_Message
    				);
    				volumeRangeOK = false;
    			}
    			return;
    		}
    		else if (!volumeRangeOK) {
    			volumeRangeOK = true;
    			adaptStartVolumes();
    		}

    		// determine the date of the last volume
    		int lastVolumeIndex = volumeVOs.size() - 1;
    		VolumeVO lastVolume = volumeVOs.get(lastVolumeIndex);
    		I18NDate lastVolumeDate = lastVolume.getDay();


    		// if desired last date is after last volume date, add necessary volumes
    		if (newLastDate.isAfter(lastVolumeDate)) {
    			I18NDate dateToAdd = lastVolumeDate;
    			do {
    				dateToAdd = dateToAdd.plusDays(1);

    				VolumeVO volumeVO = new VolumeVO(
    					hotelContingentCVO.getPK(),
    					hotelContingentCVO.getEventPK(),
    					dateToAdd,	// day
    					false,		// mandatory
    			        0,			// trueSize
    			        0,			// bookSize
    			        null,		// publicSize
    			        0			// used
    				);

    				volumeVOs.add(volumeVO);
    			}
    			while ( !newLastDate.equals(dateToAdd) );

    			volumeTableViewer.refresh();
    		}
    		else if (
    			   !newLastDate.isBefore(firstDayDateComposite.getI18NDate())
    			&&  newLastDate.isBefore(lastVolumeDate)
    		) {
    			/* if desired last date after last volume date and all volumes in between have zero
    			 * size: remove volumes
    			 */
    			boolean allVolumesInBetweenHaveZeroSize = true;

    			// Go through all Volumes after desired last date
    			int index = lastVolumeIndex;
    			while (index > -1) {
    				VolumeVO volumeVO = volumeVOs.get(index);
    				if ( !volumeVO.getDay().isAfter(newLastDate) ) {
    					break;
    				}
    				if ((volumeVO.getTrueSize()   != null && volumeVO.getTrueSize().intValue() > 0) ||
    					(volumeVO.getBookSize()   != null && volumeVO.getBookSize().intValue() > 0) ||
    					(volumeVO.getPublicSize() != null && volumeVO.getPublicSize().intValue() > 0)
    				) {
    					allVolumesInBetweenHaveZeroSize = false;
    					break;
    				}
    				index--;
    			}
    			// Remove volumes from the end, since buffer shrinks and indices get shifted
    			if (allVolumesInBetweenHaveZeroSize) {
    				for (int i = lastVolumeIndex; i > index; i--) {
    					volumeVOs.remove(i);
    				}
    				volumeTableViewer.refresh();
    			}
    			else {
    				resetDayDateComposite(lastDayDateComposite, lastVolumeDate);
    			}
    		}
    		else {
    			resetDayDateComposite(lastDayDateComposite, lastVolumeDate);
    		}
		}
	}


	/**
	 * The given day composite is set to the given (original) value, without generating events, but with issuing a beep.
	 */
	private void resetDayDateComposite(DateComposite dateComposite, I18NDate date) {
		ignoreDayChange = true;
		dateComposite.setI18NDate(date);
		Display.getCurrent().beep();
		ignoreDayChange = false;
	}

	// *
	// * handle changes of firstDay and lastDay
	// **************************************************************************


	/**
	 * Make that Ctrl+C copies table contents to clipboard.
	 */
	public void registerCopyAction(IActionBars actionBars) {
		volumeTable.registerCopyAction(actionBars);
	}


	public Date getFirstDayDate() {
		Date date = firstDayDateComposite.getDate();
		return date;
	}


	public Date getLastDayDate() {
		Date date = lastDayDateComposite.getDate();
		return date;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
