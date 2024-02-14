package com.lambdalogic.util.rcp;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

/**
 * Support for Composites that work as a container where the user can add and remove sub-Composites.
 *
 * @param <SubComposite>
 */
public class ListCompositeController<SubComposite extends Composite> {

	private ListComposite<SubComposite> listComposite;

	private List<SubComposite> compositeList = new ArrayList<>();


	public ListCompositeController(ListComposite<SubComposite> listComposite) {
		this.listComposite = listComposite;
	}


	public SubComposite getComposite(int index) {
		return compositeList.get(index);
	}


	public List<SubComposite> getCompositeList() {
		return Collections.unmodifiableList(compositeList);
	}


	public SubComposite addComposite() {
		// create Composite
		SubComposite composite = listComposite.createComposite();
		composite.setFocus();

		// add Composite to List
		compositeList.add(composite);

		listComposite.fireModifyEvent();
		listComposite.refreshLayout();

		return composite;
	}


	/**
	 * Remove the given SubComposite.
	 * @param composite
	 */
	public void removeComposite(SubComposite composite) {
		/* if the Composite that will be disposed has the focus:
		 * calculate index of the Composite that will get the focus next
		 */
		SubComposite nextFocusComposite = null;
		boolean containsFocusControl = FocusHelper.containsFocusControl(composite);
		if (containsFocusControl) {
			// calculate focusIdx
			int focusIdx = compositeList.indexOf(composite);
			if (focusIdx > 0) {
				// removed Composite is not the first one: set focus to its predecessor
				nextFocusComposite = compositeList.get(focusIdx - 1);
			}
			// removed Composite is the first one: check if it is the only one
			// if there are more Composites ...
			else if (compositeList.size() > 1) {
				// set focus to the first one
				nextFocusComposite = compositeList.get(0);
			}
		}

		// remove Composite from List
		compositeList.remove(composite);

		// destroy Composite
		composite.dispose();

		// if the disposed Composite had the focus...
		if (containsFocusControl) {
			// set focus to previously calculated
			if (nextFocusComposite != null) {
				nextFocusComposite.setFocus();
			}
			else {
				// let the compositeFactory (parent) set the focus
				listComposite.setFocus();
			}
		}

		// fire ModifyEvent
		listComposite.fireModifyEvent();

		// refresh layout
		listComposite.refreshLayout();
	}


	/**
	 * Set number of Composites.
	 * Composites are created or removed to match the given size.
	 * @param size
	 */
	public void setSize(int size) {
		if (compositeList.size() < size) {
			// create additional Composites
			int diff = size - compositeList.size();
			for (int i = 0; i < diff; i++) {
				addComposite();
			}
		}
		else if (compositeList.size() > size) {
			/* Set focus before disposing any Composite, to avoid losing the focus
			 * However, if the Composite which will be disposed owns the focus, the focus will get lost anyway.
			 */
			setFocus();

			// remove Composites (beginning at the end)
			while (compositeList.size() > size) {
				Composite composite = compositeList.remove(compositeList.size() - 1);
				composite.dispose();
			}

			listComposite.refreshLayout();
		}
	}


	public boolean setFocus() {
		// set focus on first Composite
		if ( notEmpty(compositeList) ) {
			return compositeList.get(0).setFocus();
		}
		return false;
	}

}
