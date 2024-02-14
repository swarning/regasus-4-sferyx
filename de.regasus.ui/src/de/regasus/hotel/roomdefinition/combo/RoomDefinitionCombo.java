package de.regasus.hotel.roomdefinition.combo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.RoomDefinitionModel;


@SuppressWarnings("rawtypes")
public class RoomDefinitionCombo
extends AbstractComboComposite<RoomDefinitionVO> 
implements CacheModelListener {
	
	private static final RoomDefinitionVO EMPTY_ROOM_DEFINITION;
	
	static {
		EMPTY_ROOM_DEFINITION = new RoomDefinitionVO();
	}

	private Long hotelPK;
	private Long hotelContingentPK;
	
	// Model
	private RoomDefinitionModel roomDefinitionModel;
	private HotelContingentModel hotelContingentModel;
	
	// *************************************************************************
	// * Constructors
	// *

	/**
	 * Constructor to show Room Definitions of one Hotel.
	 */
	public RoomDefinitionCombo(
		Composite parent, 
		int style,
		Long hotelPK
	)
	throws Exception {
		super(parent, SWT.NONE);
		this.hotelPK = hotelPK;
		
		initModel();
		syncComboToModel();
	}

	/**
	 * Constructor to show Room Definitions of one Hotel Contingent.
	 */
	public RoomDefinitionCombo(
		Composite parent, 
		int style,
		Long hotelPK,
		Long hotelContingentPK
	)
	throws Exception {
		/* Diese Combo hat die Besonderheit, dass es zwei Parameter gibt, 
		 * von denen der Inhalt der Werteliste abhängt: hotelPK und hotelContingentPK.
		 * Der Standardmechanismus sieht vor, dass der eine Parameter im Konstruktor
		 * als modelDataDiscriminator übergeben wird. Das funktioniert in diesem Fall 
		 * aber nicht.
		 * Als Lösung sind sind die Methoden initModel() und getModelData() so programmiert, 
		 * dass sie nichts tun, wenn das Attribut hotelPK null ist. 
		 * Die Methoden initModel() und syncComboModel() werden dann im Konstruktor aufgerufen, 
		 * nachdem hotelPK und hotelContingentPK gesetzt sind. 
		 */
		super(parent, SWT.NONE);
		
		if (hotelPK == null) {
			throw new Exception("Parameter 'hotelPK' must not be null");
		}
		
		this.hotelPK = hotelPK;
		this.hotelContingentPK = hotelContingentPK;

		/* nochmal initialisieren (bereits im Superkonstruktor), 
		 * weil erst jetzt hotelPK und hotelContingentPK gesetzt sind
		 */
		initModel();
		syncComboToModel();
	}

	@Override
	protected RoomDefinitionVO getEmptyEntity() {
		return EMPTY_ROOM_DEFINITION;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			public String getText(Object element) {
				final RoomDefinitionVO roomDefinitionVO = (RoomDefinitionVO) element;
				return LanguageString.toStringAvoidNull(roomDefinitionVO.getName());
			}
		};
	}


	protected Collection<RoomDefinitionVO> getModelData() throws Exception {
		List<RoomDefinitionVO> modelData = null;
		
		// erst wenn das Model gesetzt ist
		if (roomDefinitionModel != null) {
			if (hotelContingentPK == null) {
				// all Room Definitions of one Hotel
				modelData = roomDefinitionModel.getRoomDefinitionVOsByHotelPK(hotelPK);
			}
			else {
				// all Room Definitions of one Hotel Contingent
				HotelContingentCVO hotelContingentCVO = hotelContingentModel.getHotelContingentCVO(hotelContingentPK);
				if (hotelContingentCVO != null) {
    				Collection<Long> roomDefinitionPKs = hotelContingentCVO.getRoomDefinitionPKs();
    				modelData = roomDefinitionModel.getRoomDefinitionVOs(roomDefinitionPKs);
				}
			}
			
		}
		
		if (modelData == null) {
			modelData = Collections.emptyList();
		}
//		else {
//			modelData = new ArrayList<RoomDefinitionVO>(modelData);
//		}

		return modelData;
	}


	protected void initModel() {
		// erst initialisieren, wenn hotelPK gesetzt ist
		if (hotelPK != null) {
			roomDefinitionModel = RoomDefinitionModel.getInstance();
			roomDefinitionModel.addForeignKeyListener(this, hotelPK);
	
			if (hotelContingentPK != null) {
				hotelContingentModel = HotelContingentModel.getInstance();
				hotelContingentModel.addListener(this, hotelContingentPK);
			}
		}
	}


	protected void disposeModel() {
		roomDefinitionModel.removeForeignKeyListener(this, hotelPK);
		if (hotelContingentPK != null) {
			hotelContingentModel.removeListener(this, hotelContingentPK);
		}
	}


	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getRoomDefinitionPK() {
		Long roomDefinitionPK = null;
		if (entity != null) {
			roomDefinitionPK = entity.getID();
		}
		return roomDefinitionPK;
	}
	
	
	public void setRoomDefinitionByPK(Long roomDefinitionPK) {
		RoomDefinitionVO roomDefinitionVO = null;
		if (roomDefinitionPK != null) {
			try {
				roomDefinitionVO = roomDefinitionModel.getRoomDefinitionVO(roomDefinitionPK);
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		setEntity(roomDefinitionVO);
	}
	
}
