package de.regasus.hotel.offering.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.ui.Activator;


@SuppressWarnings("rawtypes")
public class HotelOfferingCombo
extends AbstractComboComposite<HotelOfferingVO>
implements CacheModelListener {

	// Model
	private HotelOfferingModel model;
	
	
	public HotelOfferingCombo(Composite parent, int style, Long hotelContingentPK) throws Exception {
		super(parent, SWT.NONE, hotelContingentPK);
		setWithEmptyElement(false);
	}

	
	protected HotelOfferingVO getEmptyEntity() {
		return null;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				final HotelOfferingVO hotelOfferingVO = (HotelOfferingVO) element;
				StringBuilder sb = new StringBuilder();
				
				LanguageString description = hotelOfferingVO.getDescription();
				if (description != null) {
					sb.append(description.getString());
				}
				else {
					/* Show the name of the room definition if there's no description.
					 * Because we've only a HotelOfferingVO (instead of a HotelOfferingCVO),
					 * we've to load each RoomDefinitionVO separately.
					 */
					Long roomDefinitionPK = hotelOfferingVO.getRoomDefinitionPK();
					try {
						RoomDefinitionVO roomDefinitionVO = RoomDefinitionModel.getInstance().getRoomDefinitionVO(roomDefinitionPK);
						sb.append(roomDefinitionVO.getName().getString());
					}
					catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}
				
				if (hotelOfferingVO.getCurrencyAmountGross() != null) {
					if (sb.length() > 0) {
						sb.append(" - ");
					}
					sb.append(hotelOfferingVO.getCurrencyAmountGross().format(false, true));
				}
				return sb.toString();
			}
		};
	}
	
	
	protected Collection<HotelOfferingVO> getModelData() throws Exception {
		Collection<HotelOfferingVO> modelData = null;
		Long hotelContingentPK = (Long) modelDataDiscriminator;
		if (model != null) {
			modelData = model.getHotelOfferingVOsByHotelContingentPK(hotelContingentPK);
		}
		return modelData;
	}
	
	
	protected void initModel() {
		model = HotelOfferingModel.getInstance();
		model.addForeignKeyListener(this, modelDataDiscriminator);
	}
	
	
	protected void disposeModel() {
		if (model != null) {
			model.removeForeignKeyListener(this, modelDataDiscriminator);
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
	
	
	public Long getHotelOfferingPK() {
		Long hotelOfferingPK = null;
		if (entity != null) {
			Long pk = entity.getPK();
			if (pk != null) {
				hotelOfferingPK = pk;
			}
		}
		return hotelOfferingPK;
	}

	
	public void setHotelOfferingPK(Long hotelOfferingPK) {
		try {
			HotelOfferingVO hotelOfferingVO = null;
			if (model != null && hotelOfferingPK != null) {
				hotelOfferingVO = model.getHotelOfferingVO(hotelOfferingPK);
			}
			setEntity(hotelOfferingVO);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
	
}
