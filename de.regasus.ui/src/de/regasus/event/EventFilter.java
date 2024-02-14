package de.regasus.event;

import static com.lambdalogic.util.StringHelper.containsAny;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.observer.DefaultEvent;
import com.lambdalogic.util.observer.Observer;
import com.lambdalogic.util.observer.ObserverSupport;


public class EventFilter {

	private String filterText;
	private List<String> filterTerms = new ArrayList<>();

	private final ObserverSupport<DefaultEvent> observerSupport = new ObserverSupport<>(this);


	public String getFilterText() {
		return filterText;
	}


	public void setFilterText(String filterText) {
		if (filterText == null || filterText.length() == 0) {
			this.filterText = null;
		}
		else {
			filterText = filterText.toLowerCase();
			this.filterText = filterText;
		}

		filterTerms = null;
		if (filterText != null) {
			filterTerms = StringHelper.getSegments(filterText);
		}

        observerSupport.fire();
	}


	public boolean hasFilter() {
		return filterText != null;
	}


	private boolean match(EventVO eventVO) {
		Objects.requireNonNull(eventVO);

		return matchFilterText(eventVO);
	}


	private boolean matchFilterText(EventVO eventVO) {
		boolean match = true;

		if ( !filterTerms.isEmpty() ) {
			String label = eventVO.getLabel().getString().toLowerCase();
			String mnemonic = eventVO.getMnemonic().toLowerCase();

			match = containsAny(label, filterTerms) || containsAny(mnemonic, filterTerms);
		}

		return match;
	}


	public Collection<EventVO> filter(Collection<EventVO> eventVOs) {
		Objects.requireNonNull(eventVOs);

		Collection<EventVO> filteredEventVOs = eventVOs;

		if ( hasFilter() ) {
			filteredEventVOs = new ArrayList<>( eventVOs.size() );

			for (EventVO eventVO : eventVOs) {
				if ( match(eventVO) ) {
					filteredEventVOs.add(eventVO);
				}
			}
		}

		return filteredEventVOs;
	}


    public void addObserver(Observer<DefaultEvent> observer) {
        this.observerSupport.addObserver(observer);
    }


    public void removeObserver(Observer<DefaultEvent> observer) {
        this.observerSupport.removeObserver(observer);
    }

}
