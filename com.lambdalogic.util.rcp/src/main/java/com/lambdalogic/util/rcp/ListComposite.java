package com.lambdalogic.util.rcp;

public interface ListComposite<SubComposite> {

	SubComposite createComposite();

	void fireModifyEvent();

	void refreshLayout();

	boolean setFocus();

}