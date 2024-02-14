package com.lambdalogic.util.rcp.datetime;

public interface CopyAndPasteListener {

	void beforeCopy(CopyAndPasteEvent e);
	
	void afterCopy(CopyAndPasteEvent e);

	void beforeCut(CopyAndPasteEvent e);
	
	void afterCut(CopyAndPasteEvent e);

	void beforePaste(CopyAndPasteEvent e);
	
	void afterPaste(CopyAndPasteEvent e);
	
}
