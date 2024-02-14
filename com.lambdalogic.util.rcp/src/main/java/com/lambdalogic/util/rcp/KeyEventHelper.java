package com.lambdalogic.util.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

public class KeyEventHelper {

	public static boolean isCopy(KeyEvent e) {
		return e.stateMask == SWT.MOD1 && e.keyCode == 'c';
	}


	public static boolean isCopyPK(KeyEvent e) {
		return e.stateMask == (SWT.MOD1 | SWT.SHIFT) && e.keyCode == 'c';
	}


	public static boolean isCopyVigenere1(KeyEvent e) {
		return e.stateMask == SWT.MOD1 && e.keyCode == '1';
	}


	public static boolean isCopyVigenere1Hex(KeyEvent e) {
		return e.stateMask == (SWT.MOD1 | SWT.SHIFT) && e.keyCode == '1';
	}



	public static boolean isCopyVigenere2(KeyEvent e) {
		return e.stateMask == SWT.MOD1 && e.keyCode == '2';
	}


	public static boolean isCopyVigenere2Hex(KeyEvent e) {
		return e.stateMask == (SWT.MOD1 | SWT.SHIFT) && e.keyCode == '2';
	}


	public static boolean isInsert(KeyEvent e) {
		return e.stateMask == 0 && e.keyCode == SWT.INSERT;
	}


	public static boolean isCtrlInsert(KeyEvent e) {
		return e.stateMask == SWT.CTRL && e.keyCode == SWT.INSERT;
	}


	public static boolean isDelete(KeyEvent e) {
		return e.keyCode == SWT.DEL && e.stateMask == 0;
	}


	public static boolean isF1(KeyEvent e) {
		return e.stateMask == 0 && e.keyCode == SWT.F1;
	}


	public static boolean isF2(KeyEvent e) {
		return e.stateMask == 0 && e.keyCode == SWT.F2;
	}


	public static boolean isF3(KeyEvent e) {
		return e.stateMask == 0 && e.keyCode == SWT.F3;
	}


	public static boolean isF4(KeyEvent e) {
		return e.stateMask == 0 && e.keyCode == SWT.F4;
	}

}
