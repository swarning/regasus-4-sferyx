package com.lambdalogic.util.rcp.widget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class MessageDialogBuilder {

	private Shell parentShell;
	private int imageType = MessageDialog.NONE;
	private String title;
	private String message;
	private List<String> buttonLabelList = new ArrayList<>();
	private int defaultIndex = 0;
	private int trueIndex = 0;


	private MessageDialogBuilder(Shell parentShell, int imageType) {
		this.parentShell = parentShell;
		this.imageType = imageType;
	}


	/**
	 * Create a {@link MessageDialogBuilder} for a dialog without image.
	 * @param parentShell
	 * @return
	 */
	public static MessageDialogBuilder getInstance(Shell parentShell) {
		return new MessageDialogBuilder(parentShell, MessageDialog.NONE);
	}


	/**
	 * Create a {@link MessageDialogBuilder} for a dialog an error image.
	 * @param parentShell
	 * @return
	 */
	public static MessageDialogBuilder getErrorInstance(Shell parentShell) {
		return new MessageDialogBuilder(parentShell, MessageDialog.ERROR);
	}


	/**
	 * Create a {@link MessageDialogBuilder} for a dialog an information image.
	 * @param parentShell
	 * @return
	 */
	public static MessageDialogBuilder getInformationInstance(Shell parentShell) {
		return new MessageDialogBuilder(parentShell, MessageDialog.INFORMATION);
	}


	/**
	 * Create a {@link MessageDialogBuilder} for a dialog a question image.
	 * @param parentShell
	 * @return
	 */
	public static MessageDialogBuilder getQuestionInstance(Shell parentShell) {
		return new MessageDialogBuilder(parentShell, MessageDialog.QUESTION);
	}


	/**
	 * Create a {@link MessageDialogBuilder} for a dialog a warning image.
	 * @param parentShell
	 * @return
	 */
	public static MessageDialogBuilder getWarningInstance(Shell parentShell) {
		return new MessageDialogBuilder(parentShell, MessageDialog.WARNING);
	}



	public MessageDialogBuilder title(String title) {
		this.title = title;
		return this;
	}


	public MessageDialogBuilder message(String message) {
		this.message = message;
		return this;
	}


	public MessageDialogBuilder addButtonLabel(String buttonLabel) {
		buttonLabelList.add(buttonLabel);
		return this;
	}


	public MessageDialogBuilder addDefaultButtonLabel(String buttonLabel) {
		this.defaultIndex = buttonLabelList.size();
		buttonLabelList.add(buttonLabel);
		return this;
	}


	public MessageDialogBuilder yesButton() {
		return addButtonLabel(IDialogConstants.YES_LABEL);
	}


	public MessageDialogBuilder noButton() {
		return addButtonLabel(IDialogConstants.NO_LABEL);
	}


	public MessageDialogBuilder okButton() {
		return addButtonLabel(IDialogConstants.OK_LABEL);
	}


	public MessageDialogBuilder cancelButton() {
		return addButtonLabel(IDialogConstants.CANCEL_LABEL);
	}


	public MessageDialogBuilder defaultIndex(int defaultIndex) {
		this.defaultIndex = defaultIndex;
		return this;
	}


	/**
	 * Set the index that is interpreted as true.
	 * @param trueIndex
	 * @return
	 */
	public MessageDialogBuilder trueIndex(int trueIndex) {
		this.trueIndex = trueIndex;
		return this;
	}


	public MessageDialog build() {
    	Image titleImage = null;

    	String[] buttonLabels =  buttonLabelList.toArray(String[]::new);

    	MessageDialog dialog = new MessageDialog(parentShell, title, titleImage, message, imageType, defaultIndex, buttonLabels);
    	return dialog;
	}


	public int openReturnIndex() {
    	MessageDialog dialog = build();
    	return dialog.open();
	}


	public boolean openReturnBool() {
    	MessageDialog dialog = build();
    	return dialog.open() == trueIndex;
	}


	public static boolean openQuestion(Shell parentShell, String title, String message, boolean defaultSelection) {
		return MessageDialogBuilder
			.getQuestionInstance(parentShell)
			.title(title)
			.message(message)
			.yesButton()
			.noButton()
			.trueIndex(0)
			.defaultIndex(defaultSelection ? 0 : 1) // define the button that is selected by default
			.openReturnBool();
	}


	/**
	 * Open a primary confirmation dialog with OK and Cancel buttons whereby the OK button is selected by default.
	 * @param parentShell
	 * @param title
	 * @param message
	 * @return
	 */
	public static boolean open1stConfirmation(Shell parentShell, String title, String message) {
		return MessageDialogBuilder
			.getQuestionInstance(parentShell)
			.title(title)
			.message(message)
			.okButton()
			.cancelButton()
			.trueIndex(0)
			.defaultIndex(0)
			.openReturnBool();
	}


	/**
	 * Open a secondary confirmation dialog with Yes and No buttons whereby the No button is selected by default.
	 *
	 * @param parentShell
	 * @param title
	 * @param message
	 * @return
	 */
	public static boolean open2ndConfirmation(Shell parentShell, String title, String message) {
		return MessageDialogBuilder
			.getQuestionInstance(parentShell)
			.title(title)
			.message(message)
			.yesButton()
			.noButton()
			.trueIndex(0)
			.defaultIndex(1)
			.openReturnBool();
	}

}
