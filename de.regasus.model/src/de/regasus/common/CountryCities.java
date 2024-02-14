package de.regasus.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class CountryCities implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private String countryCode;
	private List<String> cityList;
	
	
	public CountryCities(String countryCode, List<String> cityList) {
		super();
		this.countryCode = countryCode;
		this.cityList = cityList;
		
		if (this.cityList == null) {
			this.cityList = Collections.emptyList();
		}
	}
	
	
	public CountryCities(String countryCode) {
		this(countryCode, null);
	}


	public String getCountryCode() {
		return countryCode;
	}


	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}


	public List<String> getCityList() {
		return cityList;
	}


	public void setCityList(List<String> cityList) {
		this.cityList = cityList;
	}

	
	public void addCity(String city) {
		if (!cityList.contains(city)) {
			cityList.add(city);
		}
	}
	

	public void removeCity(String city) {
		cityList.remove(city);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cityList == null) ? 0 : cityList.hashCode());
		result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CountryCities other = (CountryCities) obj;
		if (cityList == null) {
			if (other.cityList != null)
				return false;
		}
		else if (!cityList.equals(other.cityList))
			return false;
		if (countryCode == null) {
			if (other.countryCode != null)
				return false;
		}
		else if (!countryCode.equals(other.countryCode))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "HotelCityData [countryCode=" + countryCode + ", cityList=" + cityList + "]";
	}
	
}
