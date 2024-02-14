package com.lambdalogic.util.rcp.widget;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;


public class FontComboViewer extends AbstractComboComposite<String>{

	public FontComboViewer(Composite parent, int style) throws Exception {
		super(parent, style);
	}

	@Override
	protected void disposeModel() {
	}

	@Override
	protected Object getEmptyEntity() {
		return "";
	}

	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider();
	}

	@Override
	protected Collection<String> getModelData() {
		FontData[] fd = getShell().getDisplay().getFontList(null, true);
		
		// TreeSet cares for sorted and unique entries
		Set<String> fontNameList = new TreeSet<String>();
		
        for( int i = 0; i < fd.length; i++ ) {
        	fontNameList.add(fd[i].getName());
        }
        return fontNameList;
	}

	@Override
	protected void initModel() {
	}


}
