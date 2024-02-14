package de.regasus.portal.pagelayout.combo;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.portal.PageLayout;
import de.regasus.portal.PageLayoutModel;

public class PageLayoutCombo extends AbstractComboComposite<PageLayout> implements CacheModelListener {

	private static final PageLayout EMPTY_PAGE_LAYOUT = new PageLayout();

	// Model
	private PageLayoutModel model;

	/**
	 * PK of the Portal whose PageLayouts shall be presented.
	 */
	private Long portalId;


	public PageLayoutCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected PageLayout getEmptyEntity() {
		return EMPTY_PAGE_LAYOUT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				PageLayout pageLayout = (PageLayout) element;
				return pageLayout.getName();
			}
		};
	}


	@Override
	protected Collection<PageLayout> getModelData() throws Exception {
		List<PageLayout> modelData = null;
		if (portalId != null) {
			modelData = model.getPageLayoutsByPortal(portalId);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		model = PageLayoutModel.getInstance();
		// do not observe the model now, because we don't know the Portal yet
	}


	@Override
	protected void disposeModel() {
		model.removeListener(this);
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getPageLayoutId() {
		Long id = null;
		if (entity != null) {
			id = entity.getId();
		}
		return id;
	}


	public void setPageLayoutId(Long pageLayoutId) {
		try {
			PageLayout pageLayout = null;
			if (pageLayoutId != null) {
				pageLayout = model.getPageLayout(pageLayoutId);
			}
			setEntity(pageLayout);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Long getPortalId() {
		return portalId;
	}


	public void setPortalId(Long portalId) throws Exception {
		if ( ! EqualsHelper.isEqual(this.portalId, portalId)) {
			Long oldPortalId = this.portalId;
			this.portalId = portalId;

			// save old selection
			final ISelection selection = comboViewer.getSelection();
			entity = null;

			model.removeForeignKeyListener(this, oldPortalId);

			// register at the model before getting its data, so the data will be put to the models cache
			model.addForeignKeyListener(this, portalId);

			// update combo
			handleModelChange();


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);
						comboViewer.setSelection(selection);
						entity = getEntityFromComboViewer();
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}

}
