package de.regasus.users;

import java.util.Comparator;
import java.util.function.Function;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.util.ObjectComparator;

import de.regasus.auth.api.ACLObject;

public class ACLObjectComparator implements Comparator<ACLObject> {

	private static ACLObjectComparator DEFAULT_INSTANCE;

	private ObjectComparator objectComparator;
	private Comparator<ACLObject> comparator;


	public static ACLObjectComparator getInstance() {
		if (DEFAULT_INSTANCE == null) {
			DEFAULT_INSTANCE = new ACLObjectComparator();
		}
		return DEFAULT_INSTANCE;
	}


	private static final Function<ACLObject, I18NString> labelFunction = new Function<>() {
		@Override
		public I18NString apply(ACLObject aclObject) {
			return AccountLabel.valueOf(aclObject.object);
		}
	};


	private ACLObjectComparator() {
		objectComparator = ObjectComparator.getInstance();

		comparator = Comparator.comparing(labelFunction, objectComparator);

		comparator = Comparator.nullsFirst(comparator);
	}


	@Override
	public int compare(ACLObject value1, ACLObject value2) {
		return comparator.compare(value1, value2);
	}

}
