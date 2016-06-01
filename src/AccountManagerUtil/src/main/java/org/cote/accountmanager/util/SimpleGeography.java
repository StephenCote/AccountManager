package org.cote.accountmanager.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cote.accountmanager.util.SimpleGeography.CountryType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SimpleGeography {
	
	private static Map<String,CountryType> countries = null;
	public static Map<String,RegionType[]> populateCountry(Map<String,CountryType> cnt, String code, String dataPath){
		CountryType ctr = cnt.get(code);
		if(ctr.getRegions().containsKey(code)) return ctr.getRegions();
		String fileStr = FileUtil.getFileAsString(dataPath);
		ObjectMapper mapper = new ObjectMapper();
		try {
			TypeFactory t = TypeFactory.defaultInstance();
			ctr.setRegions(mapper.readValue(fileStr, t.constructMapType(Map.class, String.class, RegionType[].class)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ctr.getRegions();
	}
	public static Map<String, CountryType> getCountries(String dataPath){
		if(countries != null) return countries;
		
		String fileStr = FileUtil.getFileAsString(dataPath);
		ObjectMapper mapper = new ObjectMapper();
		try {
			TypeFactory t = TypeFactory.defaultInstance();
			countries = mapper.readValue(fileStr, t.constructMapType(Map.class, String.class, CountryType.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return countries;
	}
	
	public static class RegionType{
		private String code = null;
		private String city = null;
		private String stateCode = null;
		private String stateName = null;
		private String county = null;
		public RegionType(){
			
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getStateCode() {
			return stateCode;
		}
		public void setStateCode(String stateCode) {
			this.stateCode = stateCode;
		}
		public String getStateName() {
			return stateName;
		}
		public void setStateName(String stateName) {
			this.stateName = stateName;
		}
		public String getCounty() {
			return county;
		}
		public void setCounty(String county) {
			this.county = county;
		}
		
	}
	public static class CountryType{
		private String iso2 = null;
		private String iso3 = null;
		private String name = null;
		private String capital = null;
		private String continent = null;
		private List<String> codes = new ArrayList<>();
		private Map<String,RegionType[]> regions = new HashMap<>();
		public CountryType(){
			
		}
		
		public Map<String, RegionType[]> getRegions() {
			return regions;
		}

		public void setRegions(Map<String, RegionType[]> regions) {
			this.regions = regions;
			for(String key : regions.keySet()){
				RegionType[] reg = regions.get(key);
				for(int i = 0; i < reg.length;i++) reg[i].setCode(key);
			}
		}

		public String getIso2() {
			return iso2;
		}

		public void setIso2(String iso2) {
			this.iso2 = iso2;
		}

	

		public String getIso3() {
			return iso3;
		}
		public void setIso3(String iso3) {
			this.iso3 = iso3;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCapital() {
			return capital;
		}
		public void setCapital(String capital) {
			this.capital = capital;
		}
		public String getContinent() {
			return continent;
		}
		public void setContinent(String continent) {
			this.continent = continent;
		}
		public List<String> getCodes() {
			return codes;
		}
		public void setCodes(List<String> codes) {
			this.codes = codes;
		}
		
	}
}
