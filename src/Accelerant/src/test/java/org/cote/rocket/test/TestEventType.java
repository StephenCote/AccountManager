/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.types.GeographyEnumType;
import org.cote.propellant.objects.types.TraitEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.LocationFactory;
import org.cote.rocket.factory.TraitFactory;
import org.junit.Test;
public class TestEventType extends BaseAccelerantTest {

	/// Location import code migrated over to the GeoCommand Rocket Console command
	/// trait import data is currently in a json object in RocketWeb's Scripts directory
	/// 
	///
	
	private String featuresPath = "/Users/Steve/Downloads/location/featureCodes_en.txt";
	private String countryInfo = "/Users/Steve/Downloads/location/countryInfo.txt";
	private String admin1Codes = "/Users/Steve/Downloads/location/admin1CodesASCII.txt";
	private String admin2Codes = "/Users/Steve/Downloads/location/admin2Codes.txt";
	private String alternateNames = "/Users/Steve/Downloads/location/alternateNames.txt";
	private String countryBasePath = "/Users/Steve/Downloads/location/";
	private String[] countries = new String[]{"AD"};
	private String[] positiveTraits = new String[]{"Accessible","Active","Adaptable","Admirable","Adventurous","Agreeable","Alert","Allocentric","Amiable","Anticipative","Appreciative","Articulate","Aspiring","Athletic","Attractive","Balanced","Benevolent","Brilliant","Calm","Capable","Captivating","Caring","Challenging","Charismatic","Charming","Cheerful","Clean","Clear-headed","Clever","Colorful","Companionly","Compassionate","Conciliatory","Confident","Conscientious","Considerate","Constant","Contemplative","Cooperative","Courageous","Courteous","Creative","Cultured","Curious","Daring","Debonair","Decent","Decisive","Dedicated","Deep","Dignified","Directed","Disciplined","Discreet","Dramatic","Dutiful","Dynamic","Earnest","Ebullient","Educated","Efficient","Elegant","Eloquent","Empathetic","Energetic","Enthusiastic","Esthetic","Exciting","Extraordinary","Fair","Faithful","Farsighted","Felicific","Firm","Flexible","Focused","Forecful","Forgiving","Forthright","Freethinking","Friendly","Fun-loving","Gallant","Generous","Gentle","Genuine","Good-natured","Gracious","Hardworking","Healthy","Hearty","Helpful","Herioc","High-minded","Honest","Honorable","Humble","Humorous","Idealistic","Imaginative","Impressive","Incisive","Incorruptible","Independent","Individualistic","Innovative","Inoffensive","Insightful","Insouciant","Intelligent","Intuitive","Invulnerable","Kind","Knowledge","Leaderly","Leisurely","Liberal","Logical","Lovable","Loyal","Lyrical","Magnanimous","Many-sided","Masculine  (Manly)","Mature","Methodical","Maticulous","Moderate","Modest","Multi-leveled","Neat","Nonauthoritarian","Objective","Observant","Open","Optimistic","Orderly","Organized","Original","Painstaking","Passionate","Patient","Patriotic","Peaceful","Perceptive","Perfectionist","Personable","Persuasive","Planful","Playful","Polished","Popular","Practical","Precise","Principled","Profound","Protean","Protective","Providential","Prudent","Punctual","Pruposeful","Rational","Realistic","Reflective","Relaxed","Reliable","Resourceful","Respectful","Responsible","Responsive","Reverential","Romantic","Rustic","Sage","Sane","Scholarly","Scrupulous","Secure","Selfless","Self-critical","Self-defacing","Self-denying","Self-reliant","Self-sufficent","Sensitive","Sentimental","Seraphic","Serious","Sexy","Sharing","Shrewd","Simple","Skillful","Sober","Sociable","Solid","Sophisticated","Spontaneous","Sporting","Stable","Steadfast","Steady","Stoic","Strong","Studious","Suave","Subtle","Sweet","Sympathetic","Systematic","Tasteful","Teacherly","Thorough","Tidy","Tolerant","Tractable","Trusting","Uncomplaining","Understanding","Undogmatic","Unfoolable","Upright","Urbane","Venturesome","Vivacious","Warm","Well-bred","Well-read","Well-rounded","Winning","Wise","Witty","Youthful"};
	private String[] neutralTraits = new String[]{"Absentminded","Aggressive","Ambitious","Amusing","Artful","Ascetic","Authoritarian","Big-thinking","Boyish","Breezy","Businesslike","Busy","Casual","Crebral","Chummy","Circumspect","Competitive","Complex","Confidential","Conservative","Contradictory","Crisp","Cute","Deceptive","Determined","Dominating","Dreamy","Driving","Droll","Dry","Earthy","Effeminate","Emotional","Enigmatic","Experimental","Familial","Folksy","Formal","Freewheeling","Frugal","Glamorous","Guileless","High-spirited","Huried","Hypnotic","Iconoclastic","Idiosyncratic","Impassive","Impersonal","Impressionable","Intense","Invisible","Irreligious","Irreverent","Maternal","Mellow","Modern","Moralistic","Mystical","Neutral","Noncommittal","Noncompetitive","Obedient","Old-fashioned","Ordinary","Outspoken","Paternalistic","Physical","Placid","Political","Predictable","Preoccupied","Private","Progressive","Proud","Pure","Questioning","Quiet","Religious","Reserved","Restrained","Retiring","Sarcastic","Self-conscious","Sensual","Skeptical","Smooth","Solemn","Solitary","Stern","Stoiid","Strict","Stubborn","Stylish","Subjective","Surprising","Soft","Tough","Unaggressive","Unambitious","Unceremonious","Unchanging","Undemanding","Unfathomable","Unhurried","Uninhibited","Unpatriotic","Unpredicatable","Unreligious","Unsentimental","Whimsical"};
	private String[] negativeTraits = new String[]{"Abrasive","Abrupt","Agonizing","Aimless","Airy","Aloof","Amoral","Angry","Anxious","Apathetic","Arbitrary","Argumentative","Arrogantt","Artificial","Asocial","Assertive","Astigmatic","Barbaric","Bewildered","Bizarre","Bland","Blunt","Biosterous","Brittle","Brutal","Calculating","Callous","Cantakerous","Careless","Cautious","Charmless","Childish","Clumsy","Coarse","Cold","Colorless","Complacent","Complaintive","Compulsive","Conceited","Condemnatory","Conformist","Confused","Contemptible","Conventional","Cowardly","Crafty","Crass","Crazy","Criminal","Critical","Crude","Cruel","Cynical","Decadent","Deceitful","Delicate","Demanding","Dependent","Desperate","Destructive","Devious","Difficult","Dirty","Disconcerting","Discontented","Discouraging","Discourteous","Dishonest","Disloyal","Disobedient","Disorderly","Disorganized","Disputatious","Disrespectful","Disruptive","Dissolute","Dissonant","Distractible","Disturbing","Dogmatic","Domineering","Dull","Easily Discouraged","Egocentric","Enervated","Envious","Erratic","Escapist","Excitable","Expedient","Extravagant","Extreme","Faithless","False","Fanatical","Fanciful","Fatalistic","Fawning","Fearful","Fickle","Fiery","Fixed","Flamboyant","Foolish","Forgetful","Fraudulent","Frightening","Frivolous","Gloomy","Graceless","Grand","Greedy","Grim","Gullible","Hateful","Haughty","Hedonistic","Hesitant","Hidebound","High-handed","Hostile","Ignorant","Imitative","Impatient","Impractical","Imprudent","Impulsive","Inconsiderate","Incurious","Indecisive","Indulgent","Inert","Inhibited","Insecure","Insensitive","Insincere","Insulting","Intolerant","Irascible","Irrational","Irresponsible","Irritable","Lazy","Libidinous","Loquacious","Malicious","Mannered","Mannerless","Mawkish","Mealymouthed","Mechanical","Meddlesome","Melancholic","Meretricious","Messy","Miserable","Miserly","Misguided","Mistaken","Money-minded","Monstrous","Moody","Morbid","Muddle-headed","Naive","Narcissistic","Narrow","Narrow-minded","Natty","Negativistic","Neglectful","Neurotic","Nihilistic","Obnoxious","Obsessive","Obvious","Odd","Offhand","One-dimensional","One-sided","Opinionated","Opportunistic","Oppressed","Outrageous","Overimaginative","Paranoid","Passive","Pedantic","Perverse","Petty","Pharissical","Phlegmatic","Plodding","Pompous","Possessive","Power-hungry","Predatory","Prejudiced","Presumptuous","Pretentious","Prim","Procrastinating","Profligate","Provocative","Pugnacious","Puritanical","Quirky","Reactionary","Reactive","Regimental","Regretful","Repentant","Repressed","Resentful","Ridiculous","Rigid","Ritualistic","Rowdy","Ruined","Sadistic","Sanctimonious","Scheming","Scornful","Secretive","Sedentary","Selfish","Self-indulgent","Shallow","Shortsighted","Shy","Silly","Single-minded","Sloppy","Slow","Sly","Small-thinking","Softheaded","Sordid","Steely","Stiff","Strong-willed","Stupid","Submissive","Superficial","Superstitious","Suspicious","Tactless","Tasteless","Tense","Thievish","Thoughtless","Timid","Transparent","Treacherous","Trendy","Troublesome","Unappreciative","Uncaring","Uncharitable","Unconvincing","Uncooperative","Uncreative","Uncritical","Unctuous","Undisciplined","Unfriendly","Ungrateful","Unhealthy","Unimaginative","Unimpressive","Unlovable","Unpolished","Unprincipled","Unrealistic","Unreflective","Unreliable","Unrestrained","Unself-critical","Unstable","Vacuous","Vague","Venal","Venomous","Vindictive","Vulnerable","Weak","Weak-willed","Well-meaning","Willful","Wishful","Zany"};
	//private String[] alignmentTraits = new String[]{"Lawful Good","Neutral Good","Chaotic Good","Lawful Neutral","Neutral","Chaotic Neutral","Lawful Evil","Neutral Evil","Chaotic Evil"};
	
	private TraitType getCreateTrait(UserType owner, String name) throws FactoryException, ArgumentException{
		DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Traits", testUser.getOrganizationId());
		TraitType l1 = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).getByNameInGroup(name, dir);
		if(l1 != null) return l1;
		
		l1 = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(owner, dir.getId());
		l1.setName(name);
		l1.setTraitType(TraitEnumType.PERSON);
		if(((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).add(l1)){
			return ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).getByNameInGroup(name, dir);
		}
		return null;

	}
	
	private LocationType getCreateLocation(UserType owner, String name) throws FactoryException, ArgumentException{
		DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Locations", testUser.getOrganizationId());
		LocationType l1 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getByNameInGroup(name, dir);
		if(l1 != null) return l1;
		
		l1 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(owner, dir.getId());
		l1.setName(name);
		l1.setGeographyType(GeographyEnumType.PHYSICAL);
		if(((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).add(l1)){
			return ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getByNameInGroup(name, dir);
		}
		return null;

	}
	
	
	/// Test importing GeoNames data into Trait and Location schema - http://download.geonames.org/export/dump/
	
	/*
	 * ---------------------------------------------------
geonameid         : integer id of record in geonames database
name              : name of geographical point (utf8) varchar(200)
asciiname         : name of geographical point in plain ascii characters, varchar(200)
alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
latitude          : latitude in decimal degrees (wgs84)
longitude         : longitude in decimal degrees (wgs84)
feature class     : see http://www.geonames.org/export/codes.html, char(1)
feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
country code      : ISO-3166 2-letter country code, 2 characters
cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80) 
admin3 code       : code for third level administrative division, varchar(20)
admin4 code       : code for fourth level administrative division, varchar(20)
population        : bigint (8 byte int) 
elevation         : in meters, integer
dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
timezone          : the timezone id (see file timeZone.txt) varchar(40)
modification date : date of last modification in yyyy-MM-dd format
	 */	
	
	private Map<String,LocationType> locByCode = new HashMap<>();
	private LocationType findLocationByAdminCode(LocationType parent, DirectoryGroupType dir, String codeName, String code){
		LocationType location = null;
		
		String key = dir.getId() + "-" + codeName + "-" + code;
		if(locByCode.containsKey(key)){
			return locByCode.get(key);
		}
		logger.debug("Lookup: " + key);
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setJoinAttribute(true);

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		fields.add(QueryFields.getStringField("ATR.name", codeName));
		fields.add(QueryFields.getStringField("ATR.value", code));
		if(parent != null){
			fields.add(QueryFields.getFieldParent(parent));
		}
		try{
			List<LocationType> locations = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).list(fields.toArray(new QueryField[0]), instruction, testUser.getOrganizationId());
			if(locations.size() > 0){
				location = locations.get(0);
				locByCode.put(key, location);
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		
		}

		return location;
	}
	
	private void importTraits(CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{
		logger.info("Reading traits ...");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),StandardCharsets.UTF_8));

		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		for(CSVRecord record : csvFileParser){
			TraitType trait = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(testUser, dir.getId());
			trait.setName(record.get(0));
			trait.setDescription(record.get(2));
			trait.getAttributes().add(Factories.getAttributeFactory().newAttribute(trait, "code", record.get(1)));
			trait.setTraitType(TraitEnumType.LOCATION);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TRAIT, trait);
		}
		bir.close();
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
	}
	private void importCountries(CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{
		logger.info("Reading countries ...");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),StandardCharsets.UTF_8));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		//Map<String,String> isoToName = new HashMap<>();
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		locByCode.clear();
		for(CSVRecord record : csvFileParser){
			String iso = record.get(0);
			if(iso.startsWith("#")) continue;
			//isoToName.put(iso, record.get(4));
			LocationType location = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, dir.getId());
			location.setName(record.get(4));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "name", record.get(4)));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "iso", record.get(0)));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "iso3", record.get(1)));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "capital", record.get(5)));
			//location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "area", record.get(6)));
			//location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "population", record.get(7)));
			//location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "continent", record.get(8)));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "currencyCode", record.get(10)));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "currencyName", record.get(11)));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "languages", record.get(15)));
			location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "neighbors", record.get(17)));

			location.setGeographyType(GeographyEnumType.PHYSICAL);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, location);
		}
		bir.close();
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);

	}
	private void importAdmin1Codes(CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{

		logger.info("Reading Admin 1 Codes");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),StandardCharsets.UTF_8));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).clearCache();
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		//Map<String,LocationType> admin1Map = new HashMap<>();
		Map <String, Integer> nameMap = new HashMap<>();
		locByCode.clear();
		for(CSVRecord record : csvFileParser){
			String codePair = record.get(0);
			if(codePair == null || codePair.length() == 0){
				logger.info("Skip invalid code");
				continue;
			}
			String[] code = codePair.split("\\.");
			if(code.length != 2){
				logger.info("Invalid pattern for '" + codePair + "'");
				continue;
			}
			String countryIso = code[0];
			String adminCode = code[1];
			//String name = record.get(1).trim();
			String name = record.get(3).trim();
			LocationType countryLoc = findLocationByAdminCode(null, dir, "iso", countryIso);
			//LocationType countryLoc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getByNameInGroup(isoToName.get(countryIso), ldir);
			if(countryLoc == null){
				logger.error("Failed to find country '" + countryIso + "'");
				continue;
			}
			/*
			String chkName = countryLoc.getId() + "-" + UrnUtil.getNormalizedString(name);
			if(nameMap.containsKey(chkName)){
				nameMap.put(chkName, nameMap.get(chkName) + 1);
				name = name + nameMap.get(chkName);
				logger.info("**** BUMP: " + name + " - " + countryIso + " - " + countryLoc.getId());
			}
			else nameMap.put(chkName, 1);
			*/
			LocationType admin1Loc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, countryLoc);
			admin1Loc.setName(name);
			admin1Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin1Loc, "name", record.get(1).trim()));
			admin1Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin1Loc, "code", codePair));
			admin1Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin1Loc, "geonameid", record.get(3)));
			admin1Loc.setGeographyType(GeographyEnumType.PHYSICAL);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, admin1Loc);
			//admin1Map.put(codePair, admin1Loc);
			
		}
		bir.close();
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
		
	}
	private void importAdmin2Codes(CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{
		logger.info("Reading Admin 2 Codes");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),StandardCharsets.UTF_8));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		String sessionId = BulkFactories.getBulkFactory().newBulkSession();

		String lastAdminLoc = null;
		LocationType lastLoc = null;
		Map <String, Integer> nameMap = new HashMap<>();
		int counter = 0;
		for(CSVRecord record : csvFileParser){
			String codePair = record.get(0);
			if(codePair == null || codePair.length() == 0){
				logger.info("Skip invalid code");
				continue;
			}
			String[] code = codePair.split("\\.");
			if(code.length != 3){
				logger.info("Invalid pattern for '" + codePair + "'");
				continue;
			}
			
			String countryIso = code[0];
			String adminCode1 = code[1];
			String adminCode2 = code[1];
			//String name = record.get(2).trim();
			String name = record.get(3).trim();
			LocationType admin1Loc = null;
			String adminCode = countryIso + "." + adminCode1;
			if(lastLoc == null || lastAdminLoc == null || adminCode.equals(lastAdminLoc) == false){
				lastAdminLoc = adminCode;
				
				admin1Loc = findLocationByAdminCode(null, dir, "code", adminCode);

				if(admin1Loc == null){
					logger.warn("Failed to find admin location '" + countryIso + "." + adminCode1);
					admin1Loc = findLocationByAdminCode(null, dir, "iso", countryIso);
					if(admin1Loc == null){
						logger.error("Failed to find country '" + countryIso + "'");
						continue;
					}
				}
				lastLoc = admin1Loc;
			}
			else{
				admin1Loc = lastLoc;
			}
			/*
			String chkName = admin1Loc.getId() + "-" + UrnUtil.getNormalizedString(name);
			if(nameMap.containsKey(chkName)){
				nameMap.put(chkName,nameMap.get(chkName)+1);
				name = name + " " + nameMap.get(chkName);
				logger.warn("*** BUMP " + name);
			}
			else{
				nameMap.put(chkName, 1);
			}
			*/
			LocationType admin2Loc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, admin1Loc);
			admin2Loc.setName(name);
			admin2Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin2Loc, "code", codePair));
			admin2Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin2Loc, "geonameid", record.get(3)));
			admin2Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin2Loc, "name", record.get(2).trim()));
			admin2Loc.setGeographyType(GeographyEnumType.PHYSICAL);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, admin2Loc);

			if(counter++ > 0 && (counter % BulkFactories.bulkBatchSize) == 0){
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				sessionId = BulkFactories.getBulkFactory().newBulkSession();
			}
			
			
		}
		bir.close();
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
	}
	
	Map<String,String> geoIdToPost = new HashMap<>();
	private void bufferPostalCodes(CSVFormat csvFileFormat, String path) throws IOException{
		

		logger.info("Buffering postal values ...");
		geoIdToPost.clear();
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),StandardCharsets.UTF_8));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		int counter = 0;

		for(CSVRecord record : csvFileParser){
			if(record.get(2).equals("post") && record.get(3) != null && record.get(3).length() > 0){
				geoIdToPost.put(record.get(1), record.get(3));
			}
		}
		
		bir.close();
		logger.info("Mapped " + geoIdToPost.keySet().size() + " postal codes");
	}
	
	private void importCountryData(CSVFormat csvFileFormat, DirectoryGroupType dir, String basePath, String[] countryList) throws ArgumentException, FactoryException, DataAccessException, IOException{
		BufferedReader bir = null;
		
		CSVParser  csvFileParser = null;
		String sessionId = null;
		Map <String, Integer> nameMap = new HashMap<>();



		
		for(int c = 0; c < countryList.length; c++){
			logger.info("Reading country data ... " + countryList[c]);
			String path = countryBasePath + countryList[c] + ".txt";

			LocationType countryLoc = findLocationByAdminCode(null, dir,"iso", countryList[c]);
			if(countryLoc == null){
				logger.error("Failed to find parent country for " + countryList[c]);
				continue;
			}
			bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),StandardCharsets.UTF_8));
			csvFileParser = new CSVParser(bir, csvFileFormat);
			//csvRecords = csvFileParser.getRecords(); 
			sessionId = BulkFactories.getBulkFactory().newBulkSession();
			int counter = 0;
			nameMap.clear();
			locByCode.clear();
			Set<String> adminKey = new HashSet<>();
			for(CSVRecord record : csvFileParser){
				String geoid = record.get(0);
				//String name = record.get(1).trim();
				String name = geoid;
				/*
				if(name == null || name.length() == 0){
					logger.warn(geoid + " null name in " + countryList[c]);
					continue;
				}
				*/
				String feature = record.get(6) + "." + record.get(7);
				if(!feature.equals("P.PPL")){
					//logger.info("Skip non populated feature: " + feature);
					continue;
				}
				/*
				if(geoIdToPost.containsKey(geoid)){
					logger.warn("SKIP Non Postal: " + geoid + " " + name);
					continue;
				}
				*/
				if(counter++ > 0 && (counter % BulkFactories.bulkBatchSize) == 0){
					BulkFactories.getBulkFactory().write(sessionId);
					BulkFactories.getBulkFactory().close(sessionId);
					sessionId = BulkFactories.getBulkFactory().newBulkSession();
					//logger.warn("*** DEBUG BREAK");
					//break;
				}
				
				/// Need to map in value from alternate name
				///
				String adminCode1 = record.get(10);
				String adminCode2 = record.get(11);
				if(adminCode1 == null){
					logger.error("No admin code defined for '" + name + "'");
					continue;
				}
				//logger.info("*** CHECK: " + name + " in " + adminCode1 + "." + adminCode2);
				String regionLocationCode = countryList[c] + "." + adminCode1 + (adminCode2 != null && adminCode2.length() > 0 ? "." + adminCode2 : "");
				/*
				if(adminKey.contains(regionLocationCode + "-" + name)){
					logger.warn("Skipping duplicate key: " + regionLocationCode + " " + name);
					continue;
				}
				*/
				LocationType regionLocation = findLocationByAdminCode(null, dir,"code", regionLocationCode);
				if(regionLocation == null){
					logger.warn("Failed to find region location for " + geoid + " with code: '" + regionLocationCode + "'");
					if(regionLocation == null && adminCode2 != null){
						regionLocation = findLocationByAdminCode(null, dir, "code", countryList[c] + "." + adminCode1);
					}
					if(regionLocation == null){
						logger.error("Failed to find region location for " + geoid);
						continue;
					}
					
					continue;
				}
				//adminKey.add(regionLocationCode + "-" + name);
				
				LocationType location = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, regionLocation);
				/*
				String chkName = regionLocation.getId() + "-" + UrnUtil.getNormalizedString(name);
				if(nameMap.containsKey(chkName)){
					nameMap.put(chkName, nameMap.get(chkName) + 1);
					name = name + nameMap.get(chkName);
					logger.info("**** BUMP: " + name + " - " + feature);
				}
				else nameMap.put(chkName, 1);
				*/
				location.setName(name);
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "geonameid", geoid));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "name", record.get(1).trim()));

				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "latitude", record.get(4)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "longitude", record.get(5)));
				

				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "feature", feature));
				if(geoIdToPost.containsKey(geoid)){
					location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "post", geoIdToPost.get(geoid)));
				}
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, location);

			}
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			bir.close();
		}
	}
	
	@Test
	public void TestGeoImport(){
		


		DirectoryGroupType ldir,tdir = null;
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter('\t').withAllowMissingColumnNames(true).withQuote(null);
		int counter = 0;
		try{
			tdir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(testUser, GroupEnumType.DATA, "~/Traits", testUser.getOrganizationId());
			ldir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(testUser, GroupEnumType.DATA, "~/Locations", testUser.getOrganizationId());
			if(tdir != null){
				((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).deleteTraitsInGroup(tdir);
			}
			else{
				tdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Traits", testUser.getOrganizationId());
			}
			if(ldir != null){
				((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).deleteLocationsInGroup(ldir);
			}
			else{
				ldir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Locations", testUser.getOrganizationId());
			}
			Factories.cleanupOrphans();
			
			//importTraits(csvFileFormat, tdir, featuresPath);
			importCountries(csvFileFormat, ldir, countryInfo);
			importAdmin1Codes(csvFileFormat, ldir, admin1Codes);
			importAdmin2Codes(csvFileFormat, ldir, admin2Codes);
			
			//bufferPostalCodes(csvFileFormat, alternateNames);
			//importCountryData(csvFileFormat, ldir, countryBasePath, countries);

			
			//BulkFactories.getBulkFactory().write(sessionId);
		}
		catch(FactoryException | ArgumentException | DataAccessException | IOException e){
			logger.error(e.getMessage());
		}
	}
	/*
	@Test
	public void TestBulkFactoryCrud(){

		//((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).setAggressiveKeyFlush(false);
		//((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).setAggressiveKeyFlush(false);
		//((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).setAggressiveKeyFlush(false);
		//((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).setAggressiveKeyFlush(false);
		//((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).setAggressiveKeyFlush(false);
		
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		DirectoryGroupType ldir,dir = null;
		try{
			dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(testUser, GroupEnumType.DATA, "~/Traits", testUser.getOrganizationId());
			ldir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(testUser, GroupEnumType.DATA, "~/Locations", testUser.getOrganizationId());
			if(dir != null){
				((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).deleteTraitsInGroup(dir);
			}
			else{
				dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Traits", testUser.getOrganizationId());
			}
			if(ldir != null){
				((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).deleteLocationsInGroup(ldir);
			}
			else{
				ldir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Locations", testUser.getOrganizationId());
			}			
			for(int i = 0; i < positiveTraits.length; i++){
				TraitType trait = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(testUser, dir.getId());
				trait.setName(positiveTraits[i]);
				trait.setScore(1);
				trait.setAlignmentType(AlignmentEnumType.NEUTRALGOOD);
				trait.setTraitType(TraitEnumType.PERSON);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TRAIT, trait);
			}
			for(int i = 0; i < neutralTraits.length; i++){
				TraitType trait = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(testUser, dir.getId());
				trait.setName(neutralTraits[i]);
				trait.setScore(0);
				trait.setAlignmentType(AlignmentEnumType.NEUTRAL);
				trait.setTraitType(TraitEnumType.PERSON);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TRAIT, trait);
			}
			for(int i = 0; i < negativeTraits.length; i++){
				TraitType trait = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(testUser, dir.getId());
				trait.setName(negativeTraits[i]);
				trait.setScore(-1);
				trait.setAlignmentType(AlignmentEnumType.NEUTRALEVIL);
				trait.setTraitType(TraitEnumType.PERSON);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TRAIT, trait);
			}

			Map<String,CountryType> countries = SimpleGeography.getCountries("c:/users/swcot/workspace/Location/src/main/webapp/geo/countries.json");
			SimpleGeography.populateCountry(countries, "US","c:/users/swcot/workspace/Location/src/main/webapp/geo/US.json");
			CountryType us = countries.get("US");
			
			Map<String,Integer> check = new HashMap<String,Integer>();
			LocationType countryLocation = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, ldir.getId());
			countryLocation.setName(us.getName());
			countryLocation.setGeographyType(GeographyEnumType.PHYSICAL);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, countryLocation);
			for(String key : us.getRegions().keySet()){
				LocationType regionLocation = null;
				if(key.length() > 0){
					regionLocation = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, countryLocation);
					regionLocation.setName(key);
					regionLocation.setGeographyType(GeographyEnumType.PHYSICAL);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, regionLocation);
					}
				else regionLocation = countryLocation;
				
				RegionType[] regions = us.getRegions().get(key);
				for(int i = 0; i < regions.length;i++){
					LocationType loc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, regionLocation);
					loc.setGeographyType(GeographyEnumType.PHYSICAL);
					String name = regions[i].getCode() + " " + regions[i].getCity() + ", " + regions[i].getStateCode();
					String chkName = name.replaceAll("\\s+", "").toLowerCase();
					if(check.containsKey(chkName)){
						check.put(chkName, check.get(chkName)+1);
						name = name + " " + check.get(chkName);
						logger.info("Duplicate: '" + name + "'");
					}
					else check.put(chkName, 1);
					loc.setName(name);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, loc);
				}
			}
			
			
			
			BulkFactories.getBulkFactory().write(sessionId);
		}
		catch(FactoryException | ArgumentException | DataAccessException e){
			logger.error(e.getMessage());
		}
	}
	*/
	/*
	@Test
	public void TestFactoryCrud(){
		DirectoryGroupType edir,ldir,tdir, pdir = null;
		String trait1Name = UUID.randomUUID().toString();
		String trait2Name = UUID.randomUUID().toString();
		String location1Name = UUID.randomUUID().toString();
		String location2Name = UUID.randomUUID().toString();
		String location3Name = UUID.randomUUID().toString();
		String event1Name = UUID.randomUUID().toString();
		try {
			edir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Events", testUser.getOrganizationId());
			ldir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Locations", testUser.getOrganizationId());
			tdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Traits", testUser.getOrganizationId());
			pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Persons", testUser.getOrganizationId());
			
			TraitType t1 = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(testUser, tdir.getId());
			t1.setName(trait1Name);
			t1.setTraitType(TraitEnumType.PERSON);
			assertTrue("Failed to add trait", ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).addTrait(t1));
			
			t1 = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).getByNameInGroup(trait1Name, tdir);
			assertNotNull("Failed to retrieve trait", t1);
			
			t1.setDescription("More description");
			assertTrue("Failed to update trait", ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).updateTrait(t1));
			
			
			LocationType l1 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, ldir.getId());
			l1.setName(location1Name);
			l1.setGeographyType(GeographyEnumType.PHYSICAL);
			assertTrue(((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).addLocation(l1));
			l1 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getByNameInGroup(location1Name, ldir);
			
			LocationType l2 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, ldir.getId());
			l2.setName(location2Name);
			l2.setGeographyType(GeographyEnumType.PHYSICAL);
			assertTrue(((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).addLocation(l2));
			l2 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getByNameInGroup(location2Name, ldir);
			
			// logger.info(JSONUtil.exportObject(l1));
			// logger.info(JSONUtil.exportObject(l2));
			
			LocationType l3 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(testUser, ldir.getId());
			l3.setName(location3Name);
			l3.setGeographyType(GeographyEnumType.PHYSICAL);
			l3.getBorders().add(l1);
			l3.getBoundaries().add(l2);
			assertTrue(((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).addLocation(l3));

			l3 = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getByNameInGroup(location3Name, ldir);
			((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).populate(l3);
			assertTrue("Boundaries and Borders not set", l3.getBorders().size() == 1 && l3.getBoundaries().size() == 1);
			
			EventType evt1 = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(testUser, edir.getId());
			evt1.setName(event1Name);
			evt1.setEventType(EventEnumType.INCEPT);
			evt1.setLocation(l3);
			evt1.getEntryTraits().add(t1);
			
			assertTrue(((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).addEvent(evt1));
			

			//assertTrue("Failed to delete trait",((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).deleteTrait(t1));
			
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
	}
	*/
	
}
