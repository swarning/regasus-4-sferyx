package de.regasus.hotel.eventhotelinfo.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistical data of a combination of Room Definitions of one Hotel in the context of one Event.
 */
public class StatisticData {

	/**
	 * Names of the combination of Room Definitions.
	 */
	private List<String> roomDefinitionNames = new ArrayList<String>();

	/**
	 * Statistical information about the combination of Room Definitions.
	 * The data refers to one Hotel in the context of one Event.
	 */
	private List<StatisticDatum> statisticDatumList = new ArrayList<StatisticDatum>();


	public void addRoomDefinitionName(String name) {
		roomDefinitionNames.add(name);
	}


	public void addStatisticDatum(StatisticDatum statisticDatum) {
		statisticDatumList.add(statisticDatum);
	}


	public List<String> getRoomDefinitionNames() {
		return roomDefinitionNames;
	}


	public List<StatisticDatum> getStatisticDatumList() {
		return statisticDatumList;
	}


	@Override
	public String toString() {
		return "StatisticData [roomDefinitionNames=" + roomDefinitionNames + ", statisticDatumList="
			+ statisticDatumList + "]";
	}

}
