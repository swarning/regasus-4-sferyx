package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;


/**
 * This class can be used in place of the regular TabItem when scrolling and lazy loading is desired.
 */
public class LazyScrolledTabItem extends org.eclipse.swt.widgets.TabItem {

	/**
	 * Direct child of this TabItem; parent of the contentComposite.
	 */
	private LazyScrolledComposite lazyScrolledComposite;

	/**
	 * Parent for widgets that shall be shown in this ScrolledTabItem
	 */
	private Composite contentComposite;


	public LazyScrolledTabItem(TabFolder parent, int style) {
		super(parent, style);

		lazyScrolledComposite = new LazyScrolledComposite(parent, SWT.V_SCROLL);
		super.setControl(lazyScrolledComposite);
		lazyScrolledComposite.setExpandVertical(true);
		lazyScrolledComposite.setExpandHorizontal(true);
		lazyScrolledComposite.setShowFocusedControl(true);
		lazyScrolledComposite.setLayout(new FillLayout());

		contentComposite = new Composite(lazyScrolledComposite, style);
		lazyScrolledComposite.setContent(contentComposite);

		contentComposite.setLayout(new FillLayout());
	}


	/**
	 * Use this contentComposite (and not the tabFolder) as parent for what you want to show within the tab.
	 */
	public Composite getContentComposite() {
		return contentComposite;
	}


	public void refreshScrollbars() {
		lazyScrolledComposite.refreshScrollbars();
	}


	public boolean getShowFocusedControl() {
		return lazyScrolledComposite.getShowFocusedControl();
	}


	public void setShowFocusedControl(boolean show) {
		lazyScrolledComposite.setShowFocusedControl(show);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
