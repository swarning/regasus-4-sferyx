package de.regasus.onlineform.combo;

import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;

public class RegistrationFormConfigCombo 
extends AbstractComboComposite<RegistrationFormConfig> 
implements CacheModelListener {
	
	private static final RegistrationFormConfig EMPTY_ENTITY = new RegistrationFormConfig();
	
	// Model
	private RegistrationFormConfigModel model;

	private Long eventPK;
	
	public RegistrationFormConfigCombo(Composite parent, int style, Long eventPK) throws Exception {
		super(parent, SWT.NONE);
		setWithEmptyElement(true);
		
		this.eventPK = eventPK;
		
		initModel();
		syncComboToModel();
	}

	
	protected RegistrationFormConfig getEmptyEntity() {
		return EMPTY_ENTITY;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				RegistrationFormConfig config = (RegistrationFormConfig) element;
				if (config.getWebId() != null) {
					return StringHelper.avoidNull(config.getWebId());
				}
				else {
					return UtilI18N.All;
				}
			}
		};
	}
	
	
	protected Collection<RegistrationFormConfig> getModelData() throws Exception {
		Collection<RegistrationFormConfig> modelData = model.getRegistrationFormConfigsByEventPK(eventPK);
		return modelData;
	}
	
	
	protected void initModel() {
		model = RegistrationFormConfigModel.getInstance();
		model.addListener(this);
	}
	
	
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
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
	
	
	public Long getRegistrationFormConfigID() {
		if (entity != null) {
			return entity.getId();
		}
		else {
			return null;
		}
	}

	
	public void setRegistrationFormConfigID(Long registrationFormConfigID) {
		RegistrationFormConfig anEntity = EMPTY_ENTITY;
		if (registrationFormConfigID != null) {
			try {
				anEntity = model.getRegistrationFormConfig(registrationFormConfigID);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
		setEntity(anEntity);
	}
}
