package de.regasus.profile.customfield.view;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.List;

import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldListValue;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.command.copypaste.CopyCommandHandler;

public class ProfileCustomFieldCopyCommandHandler extends CopyCommandHandler {

	@Override
	protected String buildTextContent(TreeNode<?> treeNode) {
		String key = treeNode.getKey().toString();
		String text = treeNode.getText();
		Object value = treeNode.getValue();

		StringBuilder textContent = new StringBuilder(128)
			.append("Profile Custom Field")
			.append(" [id=").append(key).append(", name=").append(text);

		// add Custom Field List Values
		ProfileCustomField customField = (ProfileCustomField) value;
		List<ProfileCustomFieldListValue> listValues = customField.getCustomFieldListValues();
		if ( notEmpty(listValues) ) {
			textContent.append(", List Values: {");
			for (ProfileCustomFieldListValue listValue : listValues) {
				textContent.append("\n");
				textContent.append( listValue.getID() );
				textContent.append(": ");
				textContent.append( listValue.getLabel().getString() );
			}
			textContent.append("\n}");
		}

		textContent.append("]");

		return textContent.toString();
	}

}
