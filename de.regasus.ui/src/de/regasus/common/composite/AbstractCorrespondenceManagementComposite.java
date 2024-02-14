package de.regasus.common.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.contact.AbstractCorrespondence;
import com.lambdalogic.util.rcp.ListComposite;
import com.lambdalogic.util.rcp.ListCompositeController;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;

public abstract class AbstractCorrespondenceManagementComposite<T extends AbstractCorrespondence> 
extends LazyComposite 
implements ListComposite<CorrespondenceComposite<T>> {

	protected ModifySupport modifySupport = new ModifySupport(this);
	
	protected ListCompositeController<CorrespondenceComposite<T>> compositeListSupport = new ListCompositeController<CorrespondenceComposite<T>>(this);
	
	// ScrolledComposite to realize vertical scroll bars
	protected ScrolledComposite scrollComposite;

	protected Composite contentComposite;

	protected Button addButton;

	
	/**
	 * Create the composite. It shows scroll bars when the space is not enough
	 * for all the custom fields.
	 * 
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public AbstractCorrespondenceManagementComposite(final Composite tabFolder, int style) {
		super(tabFolder, style);
		this.setLayout(new GridLayout(1, false));
	}
	
	
	abstract protected T createEntity() throws Exception;
	
	/**
	 * Load Correspondences from server (if necessary) and show them. 
	 */
	abstract protected void refreshData();
	
	abstract public void syncEntityToWidgets();


	@Override
	protected void createPartControl() throws Exception {
		// make the folders contentComposite scrollable
		scrollComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setShowFocusedControl(true);
		
		contentComposite = new Composite(scrollComposite, SWT.NONE);
		contentComposite.setLayout(new GridLayout(1, false));
		
		scrollComposite.setContent(contentComposite);
		scrollComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				refreshScrollbar();
			}
		});
		
		
		// horizontal line
		Label separatorLabel = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// Button to add new Composites
		addButton = new Button(this, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		addButton.setText(UtilI18N.Add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
   				addItem();
			}
		});
		
		refreshData();
	}
	
	
	@Override
	public CorrespondenceComposite<T> createComposite() {
		final CorrespondenceComposite<T> composite = new CorrespondenceComposite<T>(contentComposite, SWT.NONE);
		
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		composite.addModifyListener(modifySupport);
		
		return composite;
	}
	
	
	@SuppressWarnings("unchecked")
	protected void addItem() {
		try {
			// create composite
			CorrespondenceComposite<T> composite = compositeListSupport.addComposite();
			
			// create entity
			AbstractCorrespondence entity = createEntity();
			
			// add entity to composite
			composite.setCorrespondence((T) entity);
			
			// scroll to the end
			scrollComposite.setOrigin(0, Integer.MAX_VALUE);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
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


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#fireModifyEvent()
	 */
	@Override
	public void fireModifyEvent() {
		modifySupport.fire();
	}
	
	
	public void refreshScrollbar() {
		Rectangle clientArea = scrollComposite.getClientArea();
		scrollComposite.setMinSize(contentComposite.computeSize(clientArea.width, SWT.DEFAULT));
	}

	
	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#refreshLayout()
	 */
	@Override
	public void refreshLayout() {
		layout(true, true);
		refreshScrollbar();
	}

}
