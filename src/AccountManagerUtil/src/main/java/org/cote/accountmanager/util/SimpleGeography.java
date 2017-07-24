/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SimpleGeography {
	public static final Logger logger = LogManager.getLogger(SimpleGeography.class);
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
			
			logger.error("Error",e);
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
			
			logger.error("Error",e);
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
