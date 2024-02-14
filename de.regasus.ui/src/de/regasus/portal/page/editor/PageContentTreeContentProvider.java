package de.regasus.portal.page.editor;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.regasus.portal.Page;
import de.regasus.portal.Section;
import de.regasus.portal.component.Component;


public class PageContentTreeContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY_ELEMENTS = new Section[0];

	private Page page;

	private Map<Object, Object> elementToParentMap = new HashMap<>();


	@Override
	public Object[] getElements(Object inputElement) {
		Object[] elements = EMPTY_ELEMENTS;

		if (inputElement instanceof Page) {
    		Page page = (Page) inputElement;
    		List<Section> sectionList = page.getSectionList();
    		if ( notEmpty(sectionList) ) {
    			elements = sectionList.toArray();
    		}
		}

		return elements;
	}


	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = EMPTY_ELEMENTS;

		if (parentElement instanceof Section) {
			Section section = (Section) parentElement;
			List<Component> componentList = section.getComponentList();
			if ( notEmpty(componentList) ) {
				children = componentList.toArray();
			}
		}

		return children;
	}


	@Override
	public Object getParent(Object element) {
		Object parent = elementToParentMap.get(element);
		return parent;
	}


	@Override
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;

		if (element instanceof Section) {
			Section section = (Section) element;
			List<Component> componentList = section.getComponentList();
			hasChildren = notEmpty(componentList);
		}

		return hasChildren;
	}


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		page = null;
		elementToParentMap.clear();

		if (newInput instanceof Page) {
			page = (Page) newInput;
			initElementToParentMap();
		}
	}


	public void initElementToParentMap() {
		if (page != null) {
    		List<Section> sectionList = page.getSectionList();
    		if (sectionList != null) {
    			for (Section section : sectionList) {
    				elementToParentMap.put(section, page);

    				List<Component> componentList = section.getComponentList();
    				if (componentList != null) {
    					for (Component component : componentList) {
    						elementToParentMap.put(component, section);
    					}
    				}
    			}
    		}
		}
	}


	@Override
	public void dispose() {
	}


	/**
	 * Return all Sections and Components in a List.
	 * @return
	 */
	public List<?> getElementList() {
		// put all elements into a List
		List<Object> elementList = new ArrayList<>();
		for (Section section : page.getSectionList()) {
			elementList.add(section);
			for (Component component : section.getComponentList()) {
				elementList.add(component);
			}
		}
		return elementList;
	}

}
