package de.regasus.participant.editor.history;

import static com.lambdalogic.util.HtmlHelper.escape;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.history.FieldChange;
import de.regasus.history.FieldChangeGroup;
import de.regasus.history.HistoryLabel;
import de.regasus.history.IHistoryEvent;

/**
 * Represents one record from a HISTORY table, in which it encapsulates
 * <ul>
 * <li>the date of the change</li>
 * <li>the user who did the change</li>
 * <li>the list of all changed fields</li>
 * </ul>
 * The list of changed fields however is not a plain list, but grouped in group changes. The reason is: We actually
 * don't read the plain table record, but instead we read historyVOs, which may contain changes in various detailVOs.
 * The changes in each of these VOs is grouped in one separate group change.
 */
public class EditParticipantEvent implements IHistoryEvent {


	public static FormatHelper formatHelper = new FormatHelper();

	public static String TABLE_OPEN = "<table border='1' frame='void' cellpadding='5' cellspacing='0' width='100%'>";

	private Date editTime;

	private String editUser;

	private List<FieldChangeGroup> fieldChangeGroupList = new ArrayList<>();


	public EditParticipantEvent(Date editTime, String editUser) {
		this.editTime = editTime;
		this.editUser = editUser;
	}


	@Override
	public String getType() {
		return HistoryLabel.ParticipantEdited.getString();
	}


	@Override
	public Date getTime() {
		return editTime;
	}


	@Override
	public String getUser() {
		return editUser;
	}


	@Override
	public String getHtmlDescription() {

		if (fieldChangeGroupList.isEmpty()) {
			return "&#160;";
		}

		StringBuilder sb = new StringBuilder();

		// create inner table
		sb.append(TABLE_OPEN);

		// **************************************************************************
		// * Table Header
		// *

		sb.append(TR_OPEN);

		sb.append(TH_OPEN_COLORED);
		sb.append(HistoryLabel.EditArea.getString());
		sb.append(TH_CLOSE);

		sb.append(TH_OPEN_COLORED);
		sb.append(HistoryLabel.EditField.getString());
		sb.append(TH_CLOSE);

		sb.append(TH_OPEN_COLORED);
		sb.append(HistoryLabel.EditOldValue.getString());
		sb.append(TH_CLOSE);

		sb.append(TH_OPEN_COLORED);
		sb.append(HistoryLabel.EditNewValue.getString());
		sb.append(TH_CLOSE);

		sb.append(TR_CLOSE);


		// *
		// * Table Header
		// **************************************************************************


		for (int groupIndex = 0; groupIndex < fieldChangeGroupList.size(); groupIndex++) {
			FieldChangeGroup fieldChangeGroup = fieldChangeGroupList.get(groupIndex);

			for (int fieldIndex = 0; fieldIndex < fieldChangeGroup.fieldChangeCount(); fieldIndex++) {
				FieldChange fieldChange = fieldChangeGroup.getFieldChange(fieldIndex);
				sb.append(TR_OPEN);

				// Wenn erste Zeile einer Group, dann Name dessen FieldChanges
				if (fieldIndex == 0) {
					String td_tag = TD_OPEN.replace(">", " ROWSPAN='" + fieldChangeGroup.fieldChangeCount() + "'>");
					sb.append(td_tag);
					String escape = escape(
						fieldChangeGroup.getGroupName(),
						true	// replaceLineBreakWithBR
					);
					if (escape != null) {
						sb.append(escape);
					}
					sb.append(TD_CLOSE);
				}

				// Jeden einzelnen fieldChange mit Name, alter und neuer Wert
				if (fieldChange.getField() == null) {
					System.out.println("fieldChange.getField() == null");
				}
				sb.append(TD_OPEN);
				String escape = escape(
					fieldChange.getField(),
					true	// replaceLineBreakWithBR
				);
				if (escape != null) {
					sb.append(escape);
				}
				sb.append(TD_CLOSE);

				sb.append(TD_OPEN);
				sb.append(format(fieldChange.getOldValue(), fieldChange));
				sb.append(TD_CLOSE);

				sb.append(TD_OPEN);
				sb.append(format(fieldChange.getNewValue(), fieldChange));
				sb.append(TD_CLOSE);

				sb.append(TR_CLOSE);
			}

		}
		sb.append(TABLE_CLOSE);

		return sb.toString();
	}


	private static String format(Object o, FieldChange fieldChange) {
		if (o == null || "".equals(o)) {
			return "&nbsp;";
		}
		else if (o instanceof I18NString) {
			return ((I18NString) o).getString();
		}
		else if (o instanceof Boolean) {
			return o.equals(Boolean.TRUE) ? UtilI18N.Yes : UtilI18N.No;
		}
		else {
			return escape(
				String.valueOf(o),
				true	// replaceLineBreakWithBR
			);
		}
	}


	public void add(FieldChangeGroup fieldChangeGroup) {
		if (fieldChangeGroup.containsChanges()) {
			fieldChangeGroupList.add(fieldChangeGroup);
		}
	}


	@Override
	public String toString() {
		return editTime + ": " + fieldChangeGroupList + "\n\n";
	}


	public List<FieldChangeGroup> getFieldChangeGroupList() {
		return fieldChangeGroupList;
	}


	public boolean isEmpty() {
		return fieldChangeGroupList.isEmpty();
	}


	public FieldChangeGroup getFieldChangeGroup(String groupName) {
		FieldChangeGroup fieldChangeGroup = null;
		if (groupName != null) {
			for (FieldChangeGroup dc : fieldChangeGroupList) {
				if (groupName.equals(dc.getGroupName())) {
					fieldChangeGroup = dc;
					break;
				}
			}
		}
		return fieldChangeGroup;
	}

}
