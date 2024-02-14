package de.regasus.common;

import de.regasus.hotel.HotelModel;

/**
 * Combines name of a city and a country to server as foreign key for the {@link HotelModel} and as node element in the 
 * HotelTree. 
 * 
 * @author manfred
 *
 */
public class CountryCity {

	private String countryCode;
	private String city;

	
	public CountryCity(String city, String countryCode) {
		this.city = city;
		this.countryCode = countryCode;
	}

	
	public String getCity() {
		return city;
	}

	
	public void setCity(String city) {
		this.city = city;
	}
	
	
	public String getCountryCode() {
		return countryCode;
	}
	
	
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CountryCity))
			return false;
		CountryCity other = (CountryCity) obj;
		if (countryCode == null) {
			if (other.countryCode != null)
				return false;
		}
		else if (!countryCode.equals(other.countryCode))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		}
		else if (!city.equals(other.city))
			return false;
		return true;
	}

	/**
	 * Constructs a <code>String</code> with all attributes.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString() {
	
		StringBuilder builder = new StringBuilder(getClass().getName());
	    builder.append("\n - name=").append(city);
	    builder.append("\n - countryCode=").append(countryCode);
	    builder.append("\n");
	    return builder.toString();
	}


}
