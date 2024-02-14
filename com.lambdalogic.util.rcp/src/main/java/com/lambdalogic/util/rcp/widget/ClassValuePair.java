package com.lambdalogic.util.rcp.widget;

/**
 * When an SWT widget produces a modification or selection event; it isn't sufficient to
 * give a selected entity as data, since (in the case of combos) there can be no entity
 * selected. So we need to transport also the information for what kind entity the 
 * null stands for.
 *   
 * @author manfred
 *
 */
public class ClassValuePair {
	
	public Class<?> clazz;
	
	public Object value;

	public ClassValuePair(Class<?> clazz, Object value) {
		this.clazz = clazz;
		this.value = value;
	}
}
