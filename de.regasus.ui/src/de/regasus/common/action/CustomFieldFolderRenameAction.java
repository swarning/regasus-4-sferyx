package de.regasus.common.action;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.ICustomFieldFolderLocation;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.I18N;
import de.regasus.common.Property;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.event.view.EventMasterDataView;
import de.regasus.event.view.ParticipantCustomFieldGroupLocationTreeNode;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupLocationTreeNode;
import de.regasus.profile.customfield.view.ProfileCustomFieldTreeView;
import de.regasus.ui.Activator;

public class CustomFieldFolderRenameAction extends AbstractAction implements ISelectionListener {

	private final Tree targetTree;
	private final IWorkbenchWindow window;

	/**
	 * This regular expression is used to separate the text into 2 groups.
	 * The input text would be like <b>Group Location 1 (17)</b>
	 * The 1st group is the group location which is <b>Group Location 1</b>
	 * The 2nd group is the total amount of custom field which is <b>(17)</b>
	 * P.S. There is a space separate between these 2 groups
	 */
	private final Pattern textPattern = Pattern.compile("^(.*)\\s(\\([\\d]+\\))*$");


	public CustomFieldFolderRenameAction(IWorkbenchWindow window, Tree targetTree) {
		this.window = window;
		this.targetTree = targetTree;
		setText(I18N.Rename_Text);
		this.window.getSelectionService().addSelectionListener(this);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				de.regasus.core.ui.Activator.PLUGIN_ID,
				de.regasus.core.ui.IImageKeys.EDIT));
	}


	@Override
	public void run() {
		final TreeItem[] selections = targetTree.getSelection();
		if (selections.length != 1) {
			return;
		}
		final TreeItem item = selections[0];
		final Object data = item.getData();

		if (! (data instanceof ProfileCustomFieldGroupLocationTreeNode || data instanceof ParticipantCustomFieldGroupLocationTreeNode)) {
			return;
		}

		@SuppressWarnings("unchecked")
		final TreeNode<ICustomFieldFolderLocation> targetItem = (TreeNode<ICustomFieldFolderLocation>) data;
		final String currentDisplayText = targetItem.getText();
		final Matcher matcher = textPattern.matcher(currentDisplayText);
		final String oldText = matcher.matches() ? matcher.group(1) : currentDisplayText;

		final Text text = new Text(targetTree, SWT.NONE);
		text.setText(oldText);
		text.selectAll();
		text.setFocus();

		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String newText = text.getText();
				if (!newText.equals(oldText)) {
					updateItemAndVO(item, targetItem, newText);
				}
				text.dispose();
			}
		});

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
					case SWT.CR:
						String newText = text.getText();
						updateItemAndVO(item, targetItem, newText);
						text.dispose();
						break;
					case SWT.ESC:
						text.dispose();
						break;
				}
			}
		});
		final TreeEditor editor = new TreeEditor(targetTree);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.setEditor(text, item);
	}


	private void updateItemAndVO(final TreeItem item, TreeNode<ICustomFieldFolderLocation> targetItem, String newText) {
		if (newText.trim().length() > 0) {
			try {
				String key = targetItem.getValue().getKey();
				Property property = new Property(key, newText);
				PropertyModel.getInstance().update(Arrays.asList(property));
				item.setText(newText);
				targetItem.refreshTreeViewer();
			}
			catch (Exception e) {
				String message = I18N.UpdateProfileCustomFieldGroupLocationnErrorMessage;
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, message);
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		boolean enabled = false;
		if ((part instanceof ProfileCustomFieldTreeView || part instanceof EventMasterDataView) && selection instanceof IStructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;

			Object element = structuredSelection.getFirstElement();
			if (element instanceof ProfileCustomFieldGroupLocationTreeNode || element instanceof ParticipantCustomFieldGroupLocationTreeNode) {
				@SuppressWarnings("unchecked")
				final TreeNode<ICustomFieldFolderLocation> targetItem = (TreeNode<ICustomFieldFolderLocation>) element;
				enabled = targetItem.getValue() == ProfileCustomFieldGroupLocation.TAB_1 || 
						targetItem.getValue() == ProfileCustomFieldGroupLocation.TAB_2 ||
						targetItem.getValue() == ProfileCustomFieldGroupLocation.TAB_3 ||
						targetItem.getValue() == ParticipantCustomFieldGroupLocation.TAB_1 ||
						targetItem.getValue() == ParticipantCustomFieldGroupLocation.TAB_2 ||
						targetItem.getValue() == ParticipantCustomFieldGroupLocation.TAB_3;
			}
		}
		setEnabled(enabled);
	}
	
}
