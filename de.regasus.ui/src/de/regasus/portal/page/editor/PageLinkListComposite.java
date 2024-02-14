package de.regasus.portal.page.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ListComposite;
import com.lambdalogic.util.rcp.ListCompositeController;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Page;
import de.regasus.portal.PageLink;
import de.regasus.ui.Activator;


public class PageLinkListComposite extends Composite implements ListComposite<PageLinkComposite> {

	private List<Language> languageList;

	// parent entity that contains the list of sub-entities managed by this Composite
	private Page page;

	// support for ModifyEvents
	private ModifySupport modifySupport = new ModifySupport(this);

	// support for handling a List of sub-Composites
	private ListCompositeController<PageLinkComposite> listCompositeController = new ListCompositeController<>(this);

	// ScrolledComposite to realize vertical scroll bars
	private ScrolledComposite scrollComposite;

	// parent Composite for sub-Composites
	private Composite linkListComposite;


	public PageLinkListComposite(Composite tabFolder, int style, List<Language> languageList) {
		super(tabFolder, style);

		this.languageList = languageList;

		this.setLayout(new GridLayout(1, false));

		createPartControl();
	}


	protected void createPartControl() {
		setLayout( new FillLayout() );

		// ScrolledComposite on the left to contain the PageLinkComposites
		scrollComposite = new ScrolledComposite(this, SWT.V_SCROLL);
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


		// copy values of sub-entities to the widgets of sub-Composites
		// (in the case of lazy instantiation the entity is already there)
		syncWidgetsToEntity();
	}


	/**
	 * Copy values from sub-entities to widgets of sub-Composites.
	 */
	private void syncWidgetsToEntity() {
		if (page != null && page.getId() != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						// get sub-entity list
						List<PageLink> subEntityList = page.getPageLinkList();
						if (subEntityList == null) {
							subEntityList = new ArrayList<>();
						}

						// set number of necessary Composites
						listCompositeController.setSize( subEntityList.size() );

						// set n sub-entities to n sub-Composites
						for (int i = 0; i < subEntityList.size(); i++) {
							// set sub-entity to sub-Composite
							PageLink entity = subEntityList.get(i);
							listCompositeController.getComposite(i).setPageLink(entity);
						}
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
		List<PageLinkComposite> compositeList = listCompositeController.getCompositeList();

		// get sub-entity list
		List<PageLink> pageLinkList = new ArrayList<>( compositeList.size() );

		// add sub-entities from the sub-Composites to subEntityList
		for (PageLinkComposite subComposite : compositeList) {
			subComposite.syncEntityToWidgets();
			PageLink pageLink = subComposite.getPageLink();
			if ( ! pageLink.isEmpty()) {
				pageLinkList.add(pageLink);
			}
		}

		page.setPageLinkList(pageLinkList);
	}


	private void refreshScrollbar() {
		Rectangle clientArea = scrollComposite.getClientArea();
		scrollComposite.setMinSize(linkListComposite.computeSize(clientArea.width, SWT.DEFAULT));
	}


	public void setPage(Page page) {
		this.page = page;
		syncWidgetsToEntity();
	}



	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.ListComposite#createComposite()
	 */
	@Override
	public PageLinkComposite createComposite() {
		PageLinkComposite composite = new PageLinkComposite(
			linkListComposite,
			SWT.BORDER,
			languageList,
			page.getPortalId()
		);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		composite.addModifyListener(modifySupport);

		return composite;
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.ListComposite#fireModifyEvent()
	 */
	@Override
	public void fireModifyEvent() {
		modifySupport.fire();
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.ListComposite#refresh()
	 */
	@Override
	public void refreshLayout() {
		layout(true, true);
		refreshScrollbar();
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

}
