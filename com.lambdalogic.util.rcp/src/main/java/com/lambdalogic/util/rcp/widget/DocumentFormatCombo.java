package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.ValueHelper;

public class DocumentFormatCombo extends Combo {

	private PrinterData[] printerList;

	public DocumentFormatCombo(Composite parent, int style) {
		super(parent, style);
		
		
		printerList = Printer.getPrinterList();
		
		PrinterData defaultPrinterData = Printer.getDefaultPrinterData();
		
		int indexOfDefaultPrinter = -1;
		
		for (int i = 0; i < printerList.length; i++) {
			 PrinterData printerData = printerList[i];
			 add(printerData.name);
			 
			 if (ValueHelper.isEqual(printerData.name, defaultPrinterData.name)) {
				 indexOfDefaultPrinter = i;
			 }
			
		}
		if (indexOfDefaultPrinter > -1) {
			select(indexOfDefaultPrinter);
		}
	}
	
	@Override
	protected void checkSubclass() {
		// disable subclass checking
	}
	
	

}
