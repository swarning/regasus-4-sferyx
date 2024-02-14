package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;
import java.util.HashSet;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.Activator;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.common.File;
import de.regasus.portal.PortalFileHelper;
import de.regasus.portal.PortalFileModel;
import de.regasus.portal.component.Field;

public class PortalFileMnemonicCombo extends AbstractComboComposite<String> {

	// Model
	private PortalFileModel model;


	/**
	 * PK of the Portal which PortalFile mnemonics are hold by this Combo.
	 */
	private Long portalId;


	public PortalFileMnemonicCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		setKeepEntityInList(true);
	}


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

    			return avoidNull(element);
			}
		};
	}


	@Override
	protected Collection<String> getModelData() throws Exception {
		Collection<String> modelData = new HashSet<>();

		if (portalId != null) {
			Collection<File> portalFiles = model.getPortalFiles(portalId);
			for (File portalFile : portalFiles) {
				String key = PortalFileHelper.extractFileMnemonic( portalFile.getInternalPath() );
				modelData.add(key);
			}
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		model = PortalFileModel.getInstance();
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(modelListener);
		}
	}


	private CacheModelListener<Field> modelListener = new CacheModelListener<Field>() {
		@Override
		public void dataChange(CacheModelEvent<Field> event) {
			try {
				handleModelChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	public String getMnemonic() {
		return entity;
	}


	public void setMnemonic(String mnemonic) {
		try {
			setEntity(mnemonic);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public Long getPortalId() {
		return portalId;
	}


	public void setPortalId(Long portalId) throws Exception {
		if (this.portalId != null) {
			if ( ! this.portalId.equals(portalId)) {
				throw new ErrorMessageException("Portal ID must not change");
			}
		}
		else {
			this.portalId = portalId;

			// register at the model before getting its data, so the data will be put to the models cache
			model.addForeignKeyListener(modelListener, portalId);

			// refresh combo
			handleModelChange();
		}
	}

}
