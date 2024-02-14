package de.regasus.portal.pagelayout.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.FocusHelper;
import com.lambdalogic.util.rcp.ListComposite;
import com.lambdalogic.util.rcp.ListCompositeController;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IUpDownListener;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.UpDownComposite;
import de.regasus.portal.PageLayout;
import de.regasus.portal.PageLayoutLink;
import de.regasus.ui.Activator;


public class PageLayoutLinkListComposite extends Composite implements ListComposite<PageLayoutLinkComposite> {

	private List<String> languageList;

	// parent entity that contains the list of sub-entities managed by this Composite
	private PageLayout pageLayout;

	// support for ModifyEvents
	private ModifySupport modifySupport = new ModifySupport(this);

	// support for handling a List of sub-Composites
	private ListCompositeController<PageLayoutLinkComposite> listCompositeController = new ListCompositeController<>(this);

	// ScrolledComposite to realize vertical scroll bars
	private ScrolledComposite scrollComposite;

	// parent Composite for sub-Composites
	private Composite linkListComposite;

	private Button addButton;
	private Button removeButton;
	private UpDownComposite upDownComposite;


	public PageLayoutLinkListComposite(Composite tabFolder, int style, List<String> languageList) {
		super(tabFolder, style);

		this.languageList = languageList;

		this.setLayout(new GridLayout(1, false));

		createPartControl();
	}


	protected void createPartControl() {
		setLayout( new GridLayout(2, false) );

		createLinkComposite();
		createButtonComposite();


		// copy values of sub-entities to the widgets of sub-Composites
		// (in the case of lazy instantiation the entity is already there)
		syncWidgetsToEntity();

		adjustButtonState();
	}


	private void createLinkComposite() {
		// ScrolledComposite on the left to contain the PageLayoutLinkComposites
		scrollComposite = new ScrolledComposite(this, SWT.V_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setShowFocusedControl(true);

		linkListComposite = new Composite(scrollComposite, SWT.NONE);
		GridLayout linkListLayout = new GridLayout(1, false);
		linkListLayout.verticalSpacing = 20;
		linkListComposite.setLayout(linkListLayout);

		scrollComposite.setContent(linkListComposite);
		scrollComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				refreshScrollbar();
			}
		});
	}


	private void createButtonComposite() {
		// Composite for Buttons on the right
		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		buttonComposite.setLayout( new GridLayout(1, false) );

		GridDataFactory buttonGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.CENTER, SWT.CENTER)
//			.hint(40, 40)
			.grab(false, false);

		createAddButton(	buttonComposite, buttonGridDataFactory.create());
		createRemoveButton(	buttonComposite, buttonGridDataFactory.create());


		// horizontal line
//		Label separatorLabel = new Label(buttonComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
//		buttonGridDataFactory.applyTo(separatorLabel);

		upDownComposite = new UpDownComposite(buttonComposite, SWT.NONE);
		upDownComposite.setUpDownListener(upDownListener);
	}


	private void createAddButton(Composite parent, GridData gridData) {
		addButton = new Button(parent, SWT.PUSH);
		addButton.setLayoutData(gridData);
		addButton.setImage( IconRegistry.getImage("icons/add.png") );
		addButton.setAlignment(SWT.CENTER);
//		addButton.setText(UtilI18N.Add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
   				addItem();
			}
		});
	}


	private void createRemoveButton(Composite parent, GridData gridData) {
		removeButton = new Button(parent, SWT.PUSH);
		removeButton.setLayoutData(gridData);
		removeButton.setImage( IconRegistry.getImage("icons/delete.png") );
//		removeButton.setText(UtilI18N.Remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeItem();
			}
		});
	}


	private void adjustButtonState() {
		boolean removeEnabled = false;
		boolean upEnabled = false;
		boolean downEnabled = false;

		// determine index of selected Composite
		PageLayoutLinkComposite selectedComposite = getSelectedComposite();
		if (selectedComposite != null) {
    		List<PageLayoutLinkComposite> compositeList = listCompositeController.getCompositeList();
    		int indexOfSelectedComposite = compositeList.indexOf(selectedComposite);

    		removeEnabled = true;
    		upEnabled = indexOfSelectedComposite > 0;
    		downEnabled = indexOfSelectedComposite < compositeList.size() - 1;
		}

		removeButton.setEnabled(removeEnabled);
		upDownComposite.setTopEnabled(upEnabled);
		upDownComposite.setUpEnabled(upEnabled);
		upDownComposite.setDownEnabled(downEnabled);
		upDownComposite.setBottomEnabled(downEnabled);
	}


	private IUpDownListener upDownListener = new IUpDownListener() {
		@Override
		public void topPressed() {
			moveToFirst();
		}

		@Override
		public void upPressed() {
			moveUp();
		}

		@Override
		public void downPressed() {
			moveDown();
		}

		@Override
		public void bottomPressed() {
			moveToLast();
		}
	};


	private void addItem() {
		try {
			// create Composite
			PageLayoutLinkComposite composite = listCompositeController.addComposite();

			// create entity
			PageLayoutLink entity = createEntity();

			// add entity to Composite
			composite.setPageLayoutLink(entity);

			// scroll to the end
			scrollComposite.setOrigin(0, Integer.MAX_VALUE);

			/* Finally adjust Buttons, because for unknown reasons they have not been adjusted yet, although
			 * adjustButtonState() has already been called.
			 */
			adjustButtonState();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void removeItem() {
		try {
			PageLayoutLinkComposite selectedComposite = getSelectedComposite();
			if (selectedComposite != null) {
				listCompositeController.removeComposite(selectedComposite);
			}

			// scroll to the end
//			scrollComposite.setOrigin(0, Integer.MAX_VALUE);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void moveToFirst() {
		PageLayoutLinkComposite selectedComposite = getSelectedComposite();

		if (selectedComposite != null) {
			// determine index of selected Composite
			List<PageLayoutLinkComposite> compositeList = listCompositeController.getCompositeList();
			int indexOfSelectedComposite = compositeList.indexOf(selectedComposite);
			if (indexOfSelectedComposite > 0) {
    			syncEntityToWidgets();

    			// move data
    			List<PageLayoutLink> pageLayoutLinkList = pageLayout.getPageLayoutLinkList();

    			CollectionsHelper.moveFirst(pageLayoutLinkList, indexOfSelectedComposite);

    			syncWidgetsToEntity();

    			// set focus to the new Composite at the target index to the same Control
    			int targetIndex = 0;
    			Object focusData = selectedComposite.getFocusData();
    			compositeList.get(targetIndex).setFocus(focusData);

    			modifySupport.fire();
			}
		}
	}


	private void moveUp() {
		PageLayoutLinkComposite selectedComposite = getSelectedComposite();

		if (selectedComposite != null) {
			// determine index of selected Composite
			List<PageLayoutLinkComposite> compositeList = listCompositeController.getCompositeList();
			int indexOfSelectedComposite = compositeList.indexOf(selectedComposite);
			if (indexOfSelectedComposite > 0) {
				syncEntityToWidgets();

				// move data
				List<PageLayoutLink> pageLayoutLinkList = pageLayout.getPageLayoutLinkList();

				CollectionsHelper.moveUp(pageLayoutLinkList, indexOfSelectedComposite);

				syncWidgetsToEntity();

				// set focus to the new Composite at the target index to the same Control
				int targetIndex = indexOfSelectedComposite - 1;
				Object focusData = selectedComposite.getFocusData();
				compositeList.get(targetIndex).setFocus(focusData);

				modifySupport.fire();
			}
		}
	}


	private void moveDown() {
		PageLayoutLinkComposite selectedComposite = getSelectedComposite();

		if (selectedComposite != null) {
			// determine index of selected Composite
			List<PageLayoutLinkComposite> compositeList = listCompositeController.getCompositeList();
			int indexOfSelectedComposite = compositeList.indexOf(selectedComposite);
			if (indexOfSelectedComposite < compositeList.size() - 1) {
				syncEntityToWidgets();

				// move data
				List<PageLayoutLink> pageLayoutLinkList = pageLayout.getPageLayoutLinkList();

				CollectionsHelper.moveDown(pageLayoutLinkList, indexOfSelectedComposite);

				syncWidgetsToEntity();

				// set focus to the new Composite at the target index to the same Control
				int targetIndex = indexOfSelectedComposite + 1;
				Object focusData = selectedComposite.getFocusData();
				compositeList.get(targetIndex).setFocus(focusData);

				modifySupport.fire();
			}
		}
	}


	private void moveToLast() {
		PageLayoutLinkComposite selectedComposite = getSelectedComposite();

		if (selectedComposite != null) {
			// determine index of selected Composite
			List<PageLayoutLinkComposite> compositeList = listCompositeController.getCompositeList();
			int indexOfSelectedComposite = compositeList.indexOf(selectedComposite);
			if (indexOfSelectedComposite < compositeList.size() - 1) {
				syncEntityToWidgets();

				// move data
				List<PageLayoutLink> pageLayoutLinkList = pageLayout.getPageLayoutLinkList();

				CollectionsHelper.moveLast(pageLayoutLinkList, indexOfSelectedComposite);

				syncWidgetsToEntity();

				// set focus to the new Composite at the target index to the same Control
				int targetIndex = compositeList.size() - 1;
				Object focusData = selectedComposite.getFocusData();
				compositeList.get(targetIndex).setFocus(focusData);

				modifySupport.fire();
			}
		}
	}


	private PageLayoutLinkComposite getSelectedComposite() {
		// determine selected Composite
		for (PageLayoutLinkComposite composite : listCompositeController.getCompositeList()) {
			if ( FocusHelper.containsFocusControl(composite) ) {
				return composite;
			}
		}
		return null;
	}


	private PageLayoutLink createEntity() throws Exception {
		PageLayoutLink entity = new PageLayoutLink();
		return entity;
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


	/**
	 * Copy values from sub-entities to widgets of sub-Composites.
	 */
	private void syncWidgetsToEntity() {
		if (pageLayout != null && pageLayout.getId() != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						// get sub-entity list
						List<PageLayoutLink> subEntityList = pageLayout.getPageLayoutLinkList();
						if (subEntityList == null) {
							subEntityList = new ArrayList<>();
						}

						// set number of necessary Composites
						listCompositeController.setSize( subEntityList.size() );

						// set n sub-entities to n sub-Composites
						for (int i = 0; i < subEntityList.size(); i++) {
							// set sub-entity to sub-Composite
							PageLayoutLink entity = subEntityList.get(i);
							listCompositeController.getComposite(i).setPageLayoutLink(entity);
						}

						// adjust Button state, because in some cases the focus gets lost without notifying focusListener
						adjustButtonState();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	/**
	 * Copy sub-entities from the sub-Composites to main entity.
	 */
	public void syncEntityToWidgets() {
		List<PageLayoutLinkComposite> compositeList = listCompositeController.getCompositeList();

		// get sub-entity list
		List<PageLayoutLink> pageLayoutLinkList = new ArrayList<>( compositeList.size() );

		// add sub-entities from the sub-Composites to subEntityList
		for (PageLayoutLinkComposite subComposite : compositeList) {
			subComposite.syncEntityToWidgets();
			PageLayoutLink pageLayoutLink = subComposite.getPageLayoutLink();
			if ( ! pageLayoutLink.isEmpty()) {
				pageLayoutLinkList.add(pageLayoutLink);
			}
		}

		pageLayout.setPageLayoutLinkList(pageLayoutLinkList);
	}


	private void refreshScrollbar() {
		Rectangle clientArea = scrollComposite.getClientArea();
		scrollComposite.setMinSize(linkListComposite.computeSize(clientArea.width, SWT.DEFAULT));
	}


	public void setPageLayout(PageLayout pageLayout) {
		this.pageLayout = pageLayout;
		syncWidgetsToEntity();
	}



	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.ListComposite#createComposite()
	 */
	@Override
	public PageLayoutLinkComposite createComposite() {
		PageLayoutLinkComposite composite = new PageLayoutLinkComposite(
			linkListComposite,
			SWT.BORDER,
			languageList,
			pageLayout.getPortalId()
		);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		composite.addModifyListener(modifySupport);
		composite.addFocusListener(focusListener);

		return composite;
	}


	private FocusListener focusListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			adjustButtonState();
		}

		@Override
		public void focusGained(FocusEvent e) {
			adjustButtonState();
		}
	};


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.ListComposite#fireModifyEvent()
	 */
	@Override
	public void fireModifyEvent() {
		modifySupport.fire();
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.ListComposite#refreshLayout()
	 */
	@Override
	public void refreshLayout() {
		layout(true, true);
		refreshScrollbar();
	}

}
