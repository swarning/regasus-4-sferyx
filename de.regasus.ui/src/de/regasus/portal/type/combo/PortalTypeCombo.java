package de.regasus.portal.type.combo;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.portal.PortalType;
import de.regasus.portal.PortalTypeModel;

public class PortalTypeCombo extends AbstractComboComposite<PortalType> {

	private static final PortalType EMPTY_PORTAL_TYPE = new PortalType();

	// Model
	private PortalTypeModel model;

	private boolean eventDependent;


	public PortalTypeCombo(Composite parent, int style, boolean eventDependent) throws Exception {
		super(parent, SWT.NONE);

		this.eventDependent = eventDependent;
	}


	@Override
	protected PortalType getEmptyEntity() {
		return EMPTY_PORTAL_TYPE;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				PortalType portalType = (PortalType) element;
				return LanguageString.toStringAvoidNull(portalType.getName());
			}
		};
	}


	@Override
	protected Collection<PortalType> getModelData() throws Exception {
		List<PortalType> modelData = model.getPortalTypes(eventDependent);
		return modelData;
	}


	@Override
	protected void initModel() {
		model = PortalTypeModel.getInstance();
	}


	@Override
	protected void disposeModel() {
	}


	public PortalType getPortalType() {
		return entity;
	}


	public void setPortalType(PortalType portalType) {
		setEntity(portalType);
	}

}
