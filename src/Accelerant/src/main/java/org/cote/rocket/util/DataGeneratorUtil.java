package org.cote.rocket.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PersonParticipationFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.propellant.objects.EventType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.types.AlignmentEnumType;
import org.cote.propellant.objects.types.EventEnumType;
import org.cote.propellant.objects.types.GeographyEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.RocketSecurity;
import org.cote.rocket.factory.EventFactory;
import org.cote.rocket.factory.EventParticipationFactory;
import org.cote.rocket.factory.LocationFactory;
import org.cote.rocket.factory.LocationParticipationFactory;
import org.cote.rocket.factory.TraitFactory;


/*
 * NOTE: This class is primarily a big playground for generating out psuedo-organic populations of people and evolving them based on on events and rules
 *  It's a big mess at the moment, with the concepts being:
 *  1) Determine pain points to address in the future framework design (eg: the object <-> attribute loading performance is abysmal for large data sets)
 *  2) Enhance rules definition and evaluation so that all the various rules and fact points are externalized into the rules engine
 *  3) Have fun with dynamically generated worlds!
 */
public class DataGeneratorUtil {
	public static final Logger logger = LogManager.getLogger(DataGeneratorUtil.class);
	
	private LifecycleType lifecycle = null;
	private ProjectType project = null;
	private String lifecycleName = null;
	private String projectName = null;
	private UserType user = null;
	
	
	/// baseWordPath refers to the Princeton word dictionary location, such as: c:\\users\\swcot\\Downloads\\wn3.1.dict.tar\\dict\\
	private String baseWordPath = null;
	private String adjPath = null;
	private String advPath = null;
	private String verPath = null;
	private String nouPath = null;
	private String baseNamePath = null;
	private String namesPath = null;
	private String traitsPath = null;
	private String tradesPath = null;
	
	private String sourceLocationPath = null;
	private String sourceTraitPath = null;
	
	private List<String> advList = new ArrayList<>();
	private List<String> verList = new ArrayList<>();
	private List<String> nouList = new ArrayList<>();
	private List<String> adjList = new ArrayList<>();
	
	private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long YEAR = 365 * DAY;
    
	
	private Set<String> nameHash = new HashSet<>();
	private Map<String,String[]> traits = null;
	private Map<String,String[]> names = null;
	private Map<String,TradeType[]> trades = null;
    
    private static Map<AlignmentEnumType,Integer> alignmentScoreMap = new HashMap<>();
    static{
    	alignmentScoreMap.put(AlignmentEnumType.CHAOTICEVIL, -4);
    	alignmentScoreMap.put(AlignmentEnumType.NEUTRALEVIL, -3);
    	alignmentScoreMap.put(AlignmentEnumType.LAWFULEVIL, -2);
    	alignmentScoreMap.put(AlignmentEnumType.CHAOTICNEUTRAL, -1);
    	alignmentScoreMap.put(AlignmentEnumType.NEUTRAL, 0);
    	alignmentScoreMap.put(AlignmentEnumType.LAWFULNEUTRAL, 1);
    	alignmentScoreMap.put(AlignmentEnumType.CHAOTICGOOD, 2);
    	alignmentScoreMap.put(AlignmentEnumType.NEUTRALGOOD, 3);
    	alignmentScoreMap.put(AlignmentEnumType.LAWFULGOOD, 4);
    }
    
	private Random rand = new Random();
	

	private DirectoryGroupType populationDir = null;
	private DirectoryGroupType personsDir = null;
	private DirectoryGroupType eventsDir = null;
	private DirectoryGroupType locationsDir = null;
	private DirectoryGroupType traitsDir = null;
	private DirectoryGroupType sourceLocationsDir = null;
	private DirectoryGroupType sourceTraitsDir = null;
	
	private LocationType regionLocation = null;
	
	private int maxChildAge = 10;
	private int adultAge = 16;
	private int seniorAge = 72;
	private int minMarryAge = 14;
	private int maxMarryAge = 65;
	private int maxFertilityAge = 50;
	private int maxAge = 110;
	private int avgDeathAge = 75;
	private double marriageRate = 0.15;
	private double divorceRate = 0.01;
	private boolean isPatriarchal = true;
	
	/// odds someone will just magically show up
	/// Note: may be an individual, or a family, or groups of families
	///
	private double immigrateRate = 0.075;
	
	/// odds someone will just decide to leave
	/// Note: if a family, should drag the whole group with them
	///
	private double emmigrateRate = 0.01;
	

	private Map<Long,List<PersonType>> populationCache = new HashMap<>();
	
	public DataGeneratorUtil(UserType u, String lcName, String pjName, String srcLocationPath, String srcTraitPath, String wordPath, String namePath){
		this.baseWordPath = wordPath;
		this.baseNamePath = namePath;
		this.user = u;
		this.lifecycleName = lcName;
		this.projectName = pjName;
		this.sourceLocationPath = srcLocationPath;
		this.sourceTraitPath = srcTraitPath;
		((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).setUseThreadSafeCollections(false);
		((LocationParticipationFactory)Factories.getFactory(FactoryEnumType.LOCATIONPARTICIPATION)).setUseThreadSafeCollections(false);
		((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).setUseThreadSafeCollections(false);
		((EventParticipationFactory)Factories.getFactory(FactoryEnumType.EVENTPARTICIPATION)).setUseThreadSafeCollections(false);
		((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).setUseThreadSafeCollections(false);
		((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).setUseThreadSafeCollections(false);
		((PersonParticipationFactory)Factories.getFactory(FactoryEnumType.PERSONPARTICIPATION)).setUseThreadSafeCollections(false);
		
		configureDataPaths();
	}
	
	public DirectoryGroupType getPopulationDir() {
		return populationDir;
	}
	public DirectoryGroupType getPersonsDir() {
		return personsDir;
	}
	public DirectoryGroupType getEventsDir() {
		return eventsDir;
	}
	public DirectoryGroupType getLocationsDir() {
		return locationsDir;
	}
	public DirectoryGroupType getTraitsDir() {
		return traitsDir;
	}
	
	
	public boolean deleteLifecycle(){
		LifecycleType lc = getLifecycle();
		if(lc != null){
			lifecycle = null;
			project = null;
			return Rocket.deleteLifecycle(lc);
		}
		return false;
	}
	public boolean deleteProject(){
		ProjectType proj = getProject();
		if(proj != null){
			project = null;
			return Rocket.deleteProject(proj);
		}
		return false;
	}
	public boolean initialize() throws FactoryException, ArgumentException{
		if(user == null){
			logger.error("Null user");
			return false;
		}
		LifecycleType lc = getLifecycle(true);
		if(lc == null){
			logger.error("Null lifecycle");
			return false;
		}
		ProjectType proj = getProject(true);
		if(proj == null){
			logger.error("Null project");
			return false;
		}
		sourceLocationsDir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, sourceLocationPath, user.getOrganizationId());
		sourceTraitsDir = (DirectoryGroupType) ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, sourceTraitPath, user.getOrganizationId());
		if(sourceLocationsDir == null || sourceTraitsDir == null){
			logger.error("One or more source data locations not found");
			return false;
		}
		populationDir = RocketSecurity.getProjectDirectory(user, proj, "Populations");
		personsDir = RocketSecurity.getProjectDirectory(user, proj, "Persons");
		eventsDir = RocketSecurity.getProjectDirectory(user, proj, "Events");
		locationsDir = RocketSecurity.getProjectDirectory(user, proj, "Locations");
		traitsDir = RocketSecurity.getProjectDirectory(user, proj, "Traits");
		
		reloadNameHash(personsDir);
		
		return true;
	}
	private void configureDataPaths(){
		namesPath= baseNamePath + "names.json";
		traitsPath= baseNamePath + "traits.json";
		tradesPath= baseNamePath + "trades.json";
		adjPath = baseWordPath + "data.adj";
		advPath = baseWordPath + "data.adv";
		verPath = baseWordPath + "data.verb";
		nouPath = baseWordPath + "data.noun";
				
	}
	public List<EventType> getEvents() throws FactoryException, ArgumentException{
		return getEvents(false);
	}
	public List<EventType> getEvents(boolean deepPopulate) throws FactoryException, ArgumentException{
		Set<Long> deepSet = new HashSet<Long>();
		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldGroup(eventsDir.getId()));
		fields.add(QueryFields.getFieldParent(0L));
		ProcessingInstructionType pi = getPaginatedInstruction(0);
		pi.setOrderClause("startDate ASC");
		List<EventType> events = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).list(fields.toArray(new QueryField[0]), pi, eventsDir.getOrganizationId());
		if(deepPopulate) deepPopulate(events, deepSet,0);
		return events;
	}
	private void deepPopulate(List<EventType> events, Set<Long> deepSet, int depth) throws FactoryException, ArgumentException{
		int counter = 0;

		for(EventType evt : events){
			if(deepSet.contains(evt.getId())){
				logger.warn("Id " + evt.getId() + " already populated");
				continue;
			}
			deepSet.add(evt.getId());
			((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(evt);
			logger.info("Populating " + depth + "." + counter);
			counter++;
			deepPopulate(evt.getChildEvents(), deepSet, depth + 1);
		}
	}


	public LifecycleType getLifecycle(){
		return getLifecycle(false);
	}
	private LifecycleType getLifecycle(boolean create){
	
		LifecycleType lc = null;
		if(lifecycle != null) return lifecycle;
		try{

			lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());

			if(lc == null && create){
				AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "Unit Test", AuditEnumType.USER, user.getUrn());
				if(Rocket.enrollAdminInCommunity(audit, user)){
					AuditService.permitResult(audit, "Enrolled " + user.getName() + " as community administrator");
				}
				else{
					AuditService.denyResult(audit, "Failed to enroll as administrator");
				}
				lc = Rocket.createLifecycle(user, lifecycleName);
			}
			lifecycle = lc;
		}
		catch(FactoryException | ArgumentException | DataAccessException e){
			logger.error(e.getMessage());
			
		}
		return lifecycle;
	}
	public ProjectType getProject(){
		return getProject(false);
	}
	private ProjectType getProject(boolean create){
		ProjectType proj = null;
		if(project != null) return project;
		LifecycleType lc = getLifecycle();
		if(lc == null){
			logger.error("Lifecycle is null");
			return null;
		}
		try{


			proj = Rocket.getProject(projectName, lc, user.getOrganizationId());

			if(proj == null && create){
				logger.info("Setting up project");
				proj = Rocket.createProject(user, lc, projectName);
			}
			project = proj;
		}
		catch(ArgumentException | FactoryException | DataAccessException e){
			logger.error(e.getMessage());
		}
		return proj;
	}
	
	public Map<String,String[]> getTraits(){
		if(traits == null) traits = JSONUtil.getMap(traitsPath, String.class,String[].class);
		return traits;
	}
	public Map<String,String[]> getNames(){
		if(names == null) names = JSONUtil.getMap(namesPath, String.class,String[].class);
		return names;
	}
	public Map<String,TradeType[]> getTrades(){
		if(trades == null) trades = JSONUtil.getMap(tradesPath, String.class,TradeType[].class);
		return trades;
	}

	
	private String getWord(List<String> list){
		String word = list.get(rand.nextInt(list.size()));
		word = word.replace("_", " ");
		return word;
	}
	private List<QueryField> getRandomByGroup(DirectoryGroupType dir){
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		QueryField f = new QueryField(SqlDataEnumType.DOUBLE, "random()",new Double(0.1));
		f.setComparator(ComparatorEnumType.LESS_THAN);
		fields.add(f);
		return fields;
	}
	private ProcessingInstructionType getPaginatedInstruction(int count){
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setOrderClause("name ASC");
		pi.setStartIndex(0L);
		pi.setRecordCount(count);
		return pi;
	}
/*
	private <T> Map<String,T> getMap(String path, Class mapClass){
		ObjectMapper mapper = new ObjectMapper();
		Map<String,T> map = null;
		try {
			TypeFactory t = TypeFactory.defaultInstance();
			byte[] data = FileUtil.getFile(path);
			map = mapper.readValue(data, t.constructMapType(Map.class, String.class, mapClass));
		} catch (IOException e) {
			
			logger.error("Error",e);
		}
		return map;
	}
	*/
	public String getObjectLabel(NameIdType object, String attrName){
		String val = (attrName != null ? Factories.getAttributeFactory().getAttributeValueByName(object, attrName) : null);
		if(val == null || val.length() == 0) val = object.getName();
		return val;
	}
	private String getPersonLabel(PersonType person){

		long age = (System.currentTimeMillis() - CalendarUtil.getDate(person.getBirthDate()).getTime())/YEAR;
		String trade = "";
		AttributeType attr = Factories.getAttributeFactory().getAttributeByName(person, "trade");
		trade = attr.getValues().get(0);
		if(attr.getValues().size() > 1) trade += ", " + attr.getValues().get(1);
		return person.getName() + " " + person.getGender() + " aged " + age + " is a " + trade;
	}
	
	/// From stackoverflow: http://stackoverflow.com/questions/1972392/java-pick-a-random-value-from-an-enum
	///
    public static <T extends Enum<?>> T randomEnum(Class<T> cls){
        int x = (new Random()).nextInt(cls.getEnumConstants().length);
        return cls.getEnumConstants()[x];
    }
    
    public static AlignmentEnumType getAlignmentFromScore(int alignment){
		AlignmentEnumType aType = AlignmentEnumType.NEUTRAL;
		for(Map.Entry<AlignmentEnumType,Integer> aet : alignmentScoreMap.entrySet()){
			if(aet.getValue() == alignment){
				aType = aet.getKey();
				break;
			}
		}
		return aType;
    }
	public static int getAlignmentScore(NameIdType obj){
		return getAlignmentScore(Factories.getAttributeFactory().getAttributeValueByName(obj, "alignment"));
	}
	public static int getAlignmentScore(String alignment){
		if(alignment != null && alignment.length() > 0){
			return getAlignmentScore(AlignmentEnumType.valueOf(alignment));
		}
		else{
			logger.error("Invalid alignmnet");
		}
		return 0;
	}
	public static int getAlignmentScore(AlignmentEnumType alignment){
		if(alignment != null){
			return alignmentScoreMap.get(alignment);
		}
		else{
			logger.error("Invalid alignment");
		}
		return 0;
	}
	private PersonType randomPerson(UserType user, DirectoryGroupType dir) throws ArgumentException{
		return randomPerson(user,dir,null);
	}
	private PersonType randomPerson(UserType user, DirectoryGroupType dir, String preferredLastName) throws ArgumentException{
		PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(user, dir.getId());
		boolean isMale = (Math.random() < 0.5);
		TradeType[] trades = getTrades().get("trades");
		Map<String,String[]> names = getNames();
		
		Random r = new Random();
		
		long birthEpoch = System.currentTimeMillis() - (YEAR * r.nextInt(75));
		Date birthDate = new Date(birthEpoch);
		person.setBirthDate(CalendarUtil.getXmlGregorianCalendar(birthDate));
		person.setGender(isMale ? "male":"female");
		
		boolean unique = false;
		String[] firstNames = (isMale ? names.get("male") : names.get("female"));
		String firstName = firstNames[r.nextInt(firstNames.length)];
		String middleName = firstNames[r.nextInt(firstNames.length)];
		String lastName = (preferredLastName != null ? preferredLastName : names.get("common")[r.nextInt(names.get("common").length)]);
		String name = firstName + " " + middleName + " " + lastName;
		while(nameHash.contains(name) == true){
			firstName = firstNames[r.nextInt(firstNames.length)];
			middleName = firstNames[r.nextInt(firstNames.length)];
			lastName = (preferredLastName != null ? preferredLastName : names.get("common")[r.nextInt(names.get("common").length)]);
			name = firstName + " " + middleName + " " + lastName;
		}
		
		person.setFirstName(firstName);
		person.setMiddleName(middleName);
		person.setLastName(lastName);
		person.setName(name);
		
		AttributeType attr = new AttributeType();
		attr.setName("trade");
		attr.setDataType(SqlDataEnumType.VARCHAR);
		attr.getValues().add(trades[r.nextInt(trades.length)].getName());
		
		if(Math.random() < .15) attr.getValues().add(trades[r.nextInt(trades.length)].getName());
		person.getAttributes().add(attr);
		
		AttributeType attr2 = new AttributeType();
		attr2.setName("alignment");
		attr2.setDataType(SqlDataEnumType.VARCHAR);
		AlignmentEnumType alignment = randomEnum(AlignmentEnumType.class);
		/// People can't be neutral
		while(alignment == AlignmentEnumType.NEUTRAL) alignment = randomEnum(AlignmentEnumType.class);
		attr2.getValues().add(alignment.toString());
		person.getAttributes().add(attr2);
		
		nameHash.add(name);
		
		return person;
	}
	/*
	public EventType organizePopulation(UserType user, LocationType location, DirectoryGroupType personsDir, List<PersonType> persons) throws ArgumentException{
		EventType event = new EventType();
		
		event.setEventType(EventEnumType.STABLIZE);

		event.setName("Stabilize " + getObjectLabel(location,"name"));
		int len = persons.size();
		PersonGroupType leaders = new PersonGroupType();
		
		for(int i = 0; i < len; i++){
			PersonType person = randomPerson(user, personsDir);
			event.getActors().add(person);
		}
		return event;
	}
	*/
	private String[] leaderPopulation = new String[]{"Political","Religious","Military","Business","Social","Trade"};
	public EventType populateRegion(String sessionId, LocationType location, int popCount){
		EventType event = null;
		try {
			PersonGroupType populationGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newPersonGroup(user, getObjectLabel(location,"name"), populationDir, user.getOrganizationId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, populationGroup);
			for(String name : leaderPopulation){
				PersonGroupType leadersGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newPersonGroup(user, name + " Leaders", populationGroup, user.getOrganizationId());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, leadersGroup);
			}
			PersonGroupType cemGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newPersonGroup(user, "Cemetary", populationGroup, user.getOrganizationId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, cemGroup);
			event = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, eventsDir.getId());
			event.setEventType(EventEnumType.INCEPT);
			event.setName("Populate " + getObjectLabel(location,"name"));
			event.setLocation(location);
			event.getGroups().add(populationGroup);
			Random r = new Random();
			int len = r.nextInt(popCount);
			if(len == 0){
				logger.error("Empty population");
				event.setDescription("Decimated");
			}
			else{
				long totalAge = 0;
				int totalAlignment = 0;
				int totalAbsoluteAlignment = 0;
				for(int i = 0; i < len; i++){
					PersonType person = randomPerson(user, personsDir);
					int alignment = getAlignmentScore(person);
					long years = Math.abs(CalendarUtil.getTimeSpanFromNow(person.getBirthDate())) / YEAR;
					totalAge += years;
					totalAlignment += alignment;
					totalAbsoluteAlignment += (alignment + 4);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, person);
					event.getActors().add(person);
					BaseParticipantType bpt = ((GroupParticipationFactory)Factories.getBulkFactory(FactoryEnumType.GROUPPARTICIPATION)).newPersonGroupParticipation(populationGroup, person);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, bpt);
				}
				long avgAge = (totalAge > 0 ? (totalAge / len) : 0);
				int eventAlignment = (totalAbsoluteAlignment / len) - 4;
				AlignmentEnumType aType = getAlignmentFromScore(eventAlignment);
				AttributeType attr = new AttributeType();
				attr.setName("alignment");
				attr.setDataType(SqlDataEnumType.VARCHAR);
				attr.getValues().add(aType.toString());
				event.getAttributes().add(attr);
				
				AttributeType attr2 = new AttributeType();
				attr2.setName("averageAge");
				attr2.setDataType(SqlDataEnumType.VARCHAR);
				attr2.getValues().add(Long.toString(avgAge));
				event.getAttributes().add(attr2);
			}
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, event);
			

		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return event;
	}
	private void cloneObjectToGroup(FactoryEnumType factType, NameIdDirectoryGroupType dObj, DirectoryGroupType newParentGroup){
		NameIdFactory factory = Factories.getFactory(factType);
		factory.removeFromCache(dObj);
		Factories.getAttributeFactory().populateAttributes(dObj);
		
		dObj.setId(0L);
		dObj.setParentId(0L);
		dObj.setUrn(null);
		dObj.setGroupId(newParentGroup.getId());
		dObj.setGroupPath(newParentGroup.getPath());
		
		for(AttributeType attr : dObj.getAttributes()){
			attr.setReferenceId(dObj.getId());
		}

	}
	public <T> List<T> getRandomType(UserType owner, FactoryEnumType factType, DirectoryGroupType sourceDir, int count){
		List<T> objs = new ArrayList<>();
		NameIdFactory factory = Factories.getFactory(factType);
		try {
			objs = factory.list(getRandomByGroup(sourceDir).toArray(new QueryField[0]), getPaginatedInstruction(count), owner.getOrganizationId());
			for(int i = 0; i < objs.size(); i++){
				NameIdDirectoryGroupType obj = (NameIdDirectoryGroupType)objs.get(i);
				Factories.getAttributeFactory().populateAttributes(obj);
			}
			
		} catch (FactoryException | ArgumentException e) {
			
			logger.error("Error",e);
		}
		
		return objs;
	}
	public List<String> getAdvList(){
		if(advList.size() == 0) advList = loadFile(advPath);
		return advList;
	}
	public List<String> getAdjList(){
		if(adjList.size() == 0) adjList = loadFile(adjPath);
		return adjList;
	}
	public List<String> getVerList(){
		if(verList.size() == 0) verList = loadFile(verPath);
		return verList;
	}
	public List<String> getNouList(){
		if(nouList.size() == 0) nouList = loadFile(nouPath);
		return nouList;
	}
	public String generateEpochTitle(AlignmentEnumType alignment){
		List<String> adv = getAdvList();
		List<String> ver = getVerList();
		List<String> nou = getNouList();
		List<String> adj = getAdjList();

		String advWord = getWord(adv);
		String verWord = getWord(ver);
		String nouWord = getWord(nou);
		String adjWord = getWord(adj);
		
		String title = null;
		switch(alignment){
			case CHAOTICEVIL:
				title = "Vile period of " + adjWord + " " + nouWord;
				break;
			case CHAOTICGOOD:
				title = "The " + advWord + " " + verWord + " upheavel";
				break;
			case CHAOTICNEUTRAL:
				title = "All quiet on the " + adjWord + " " + nouWord;
				break;
			case LAWFULEVIL:
				title = "The " + verWord + " " + nouWord + " circumstance";
				break;
			case LAWFULGOOD:
				title = "Triumph of " + adjWord + " " + nouWord;
				break;
			case LAWFULNEUTRAL:
				title = "Quiet of " + adjWord + " " + nouWord;
				break;
			case NEUTRAL:
				title = "Stillness of " + nouWord;
				break;
			case NEUTRALEVIL:
				title = "The " + adjWord + " " + nouWord + " confusion";
				break;
			case NEUTRALGOOD:
				title = "The " + verWord + " of the " + nouWord;
				break;
			default:
				break;
		}


		return title;
		
	}

	public LocationType getRegionLocation(DirectoryGroupType targetLocationDir){
		LocationType loc = null;
		if(regionLocation != null) return regionLocation;
		try{
			
			ProcessingInstructionType instruction = new ProcessingInstructionType();
			instruction.setJoinAttribute(true);

			List<QueryField> fields = new ArrayList<>();
			fields.add(QueryFields.getFieldGroup(targetLocationDir.getId()));
			fields.add(QueryFields.getStringField("ATR.name", "is-region"));
			fields.add(QueryFields.getStringField("ATR.value", "true"));
			fields.add(QueryFields.getFieldParent(0L));
			List<LocationType> locations = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).list(fields.toArray(new QueryField[0]), instruction, targetLocationDir.getOrganizationId());
			if(locations.size() > 0){
				loc = locations.get(0);
				((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).populate(loc);
				Factories.getAttributeFactory().populateAttributes(loc);
			}
			regionLocation = loc;
		}
		catch (ArgumentException | FactoryException e) {
			
			logger.error(e.getMessage());
		}
		return loc;
	}

	public void ageRegion(LocationType location, long addTimeMS){
		
		AttributeType attrTime = Factories.getAttributeFactory().getAttributeByName(location, "currentTimeMS");
		if(attrTime == null){
			logger.warn("Attribute 'currentTimeMS' not found");
			return;
		}
		long startTimeMS = Long.parseLong(attrTime.getValues().get(0));
		attrTime.getValues().clear();
		attrTime.getValues().add(Long.toString(startTimeMS + addTimeMS));
	}
	
	/// Don't clear the nameHash when reloading because there will be names generated during evolution that won't be in the database yet
	///
	private void reloadNameHash(DirectoryGroupType dir){
		//nameHash.clear();

		String[] names = new String[0];
		try {
			names = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getNamesInGroup(dir);
			nameHash.addAll(Arrays.asList(names));
			logger.info("Populated " + names.length + " names");
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
	}
	public Map<String,List<PersonType>> getDemographics(EventType parentEvent) throws FactoryException, ArgumentException{
		return getDemographics(parentEvent.getLocation(), parentEvent);
	}
	public Map<String,List<PersonType>> getDemographics(LocationType location, EventType parentEvent) throws FactoryException, ArgumentException{
		Map<String,List<PersonType>> demographicMap = newDemographicMap();
		PersonGroupType population = getPopulation(location);
		List<PersonType> persons = getPersonPopulation(population);
		for(PersonType person : persons){
			setDemographicMap(demographicMap, parentEvent, person);
		}
		return demographicMap;
		
	}
	public PersonGroupType getPopulation(LocationType location) throws FactoryException, ArgumentException{
		Factories.getAttributeFactory().populateAttributes(location);
		String name = getObjectLabel(location, "name");
		return (PersonGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupByName(name, GroupEnumType.PERSON, populationDir, populationDir.getOrganizationId());
		
	}
	public boolean isDecimated(LocationType location){
		Factories.getAttributeFactory().populateAttributes(location);
		String dec = Factories.getAttributeFactory().getAttributeValueByName(location, "decimated");
		return (dec != null && dec.equalsIgnoreCase("true"));
	}
	public boolean isDeceased(PersonType person){
		String dec = Factories.getAttributeFactory().getAttributeValueByName(person, "deceased");
		return (dec != null && dec.equalsIgnoreCase("true"));
	}

	public EventType generateRegion(String sessionId, int locCount, int popSeed){
		
		List<EventType> events = new ArrayList<>(); 
		EventType root = null;
		LocationType rootLoc = null;
		try{
			List<LocationType> locations = getRandomType(user, FactoryEnumType.LOCATION, sourceLocationsDir, locCount+1);
			if(locations.isEmpty()){
				logger.error("Expected a positive number of locations");
				return root;
			}
			List<TraitType> ts = getRandomType(user, FactoryEnumType.TRAIT, sourceTraitsDir, 40);
			if(ts.size() <= 3){
				logger.error("Expected at least three traits");
				return root;
			}
			Random r = new Random();
			for(int i = 0; i < locations.size();i++){
				LocationType loc = locations.get(i);
				String locName = Factories.getAttributeFactory().getAttributeValueByName(loc, "name");
				cloneObjectToGroup(FactoryEnumType.LOCATION,loc,locationsDir);
				loc.setGeographyType(GeographyEnumType.PHYSICAL);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, loc);
				EventType event = null;
				if(root == null){
					root = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, eventsDir.getId());
					event = root;
					event.setName("Construct Region " + locName);
					AttributeType attr = new AttributeType();
					attr.setName("is-region");
					attr.setDataType(SqlDataEnumType.VARCHAR);
					attr.getValues().add("true");
					loc.getAttributes().add(attr);
					
					AttributeType attr2 = new AttributeType();
					attr2.setName("startTimeMS");
					attr2.setDataType(SqlDataEnumType.VARCHAR);
					attr2.getValues().add(Long.toString(CalendarUtil.getDate(root.getStartDate()).getTime()));
					loc.getAttributes().add(attr2);

					AttributeType attr3 = new AttributeType();
					attr3.setName("currentTimeMS");
					attr3.setDataType(SqlDataEnumType.VARCHAR);
					attr3.getValues().add(Long.toString(CalendarUtil.getDate(root.getEndDate()).getTime()));
					loc.getAttributes().add(attr3);
					rootLoc = loc;
				}
				else{
					EventType popEvent = populateRegion(sessionId,loc, popSeed);
					popEvent.setParentId(root.getId());
					events.add(popEvent);
					loc.setParentId(rootLoc.getId());
					//event = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, eventsDir.getId());
					event = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, root);
					
					event.setName("Construct " + locName);
					Set<Long> tset = new HashSet<>();
					for(int e = 0; e < 3; e++){
						TraitType t = ts.get(r.nextInt(ts.size()));
						if(tset.contains(t.getId())) continue;
						tset.add(t.getId());
						event.getEntryTraits().add(t);
					}
					for(int e = 0; e < 3; e++){
						TraitType t = ts.get(r.nextInt(ts.size()));
						if(tset.contains(t.getId())) continue;
						tset.add(t.getId());
						event.getExitTraits().add(t);
					}
				}
				
				event.setLocation(loc);
				event.setEventType(EventEnumType.CONSTRUCT);
				//logger.info("Should have " + event.getEntryTraits().size() + " and " + event.getExitTraits().size());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, event);
	
				events.add(event);
				
			}
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		// return events;
		return root;
	}
	
	public List<PersonType> getPersonPopulation(PersonGroupType population) throws FactoryException, ArgumentException{
		List<PersonType> personPopulation = new ArrayList<>();
		if(population == null) return personPopulation;
		if(populationCache.containsKey(population.getId())) personPopulation = populationCache.get(population.getId());
		else{
			personPopulation = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getPersonsInGroup(population);
			for(PersonType p : personPopulation) Factories.getAttributeFactory().populateAttributes(p);
			populationCache.put(population.getId(), personPopulation);
		}
		return personPopulation;

	}
	
	public EventType generateEpoch(String sessionId, int evolutions, int increment){

		long startTime = System.currentTimeMillis();
		long stopTime = startTime;
		EventType epoch = null;

		LocationType region = getRegionLocation(locationsDir);
		if(region == null){
			logger.error("Failed to find location marked as a region");
			return null;
		}
		
		AlignmentEnumType alignment = randomEnum(AlignmentEnumType.class);
		/// Epoch's can't be neutral
		while(alignment == AlignmentEnumType.NEUTRAL) alignment = randomEnum(AlignmentEnumType.class);

		int alignmentScore = getAlignmentScore(alignment);
		int invertedScore = -1 * alignmentScore;
		AlignmentEnumType invertedAlignment = getAlignmentFromScore(invertedScore);

		String title = generateEpochTitle(alignment);
		try {
			logger.info("Generating Epoch: " + title + " (" + alignment.toString() + ")");
			epoch = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, eventsDir.getId());
			epoch.setEventType((alignmentScore < 0 ? EventEnumType.DESTABILIZE : EventEnumType.STABLIZE));
			epoch.setName("Epoch: " + title);
			epoch.setLocation(region);
			AttributeType attr = new AttributeType();
			attr.setDataType(SqlDataEnumType.VARCHAR);
			attr.setName("is-epoch");
			attr.getValues().add("true");
			epoch.getAttributes().add(attr);
			AttributeType attr2 = new AttributeType();
			attr2.setDataType(SqlDataEnumType.VARCHAR);
			attr2.setName("alignment");
			attr2.getValues().add(alignment.toString());
			epoch.getAttributes().add(attr2);
			
			
			long startTimeMS = Long.parseLong(Factories.getAttributeFactory().getAttributeValueByName(region, "currentTimeMS"));
			Date startDate = new Date(startTimeMS);
			Date stopDate = new Date(startTimeMS + (YEAR*increment));
			ageRegion(region,(YEAR*increment));
			epoch.setStartDate(CalendarUtil.getXmlGregorianCalendar(startDate));
			epoch.setEndDate(CalendarUtil.getXmlGregorianCalendar(stopDate));
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, epoch);
			
			List<LocationType> childLocations = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getChildLocationList(region);

			if(childLocations.isEmpty()){
				logger.error("Failed to find child locations");
			}
			
			for(LocationType childLocation : childLocations){
				long subStartTime = System.currentTimeMillis();
				Factories.getAttributeFactory().populateAttributes(childLocation);
				if(isDecimated(childLocation)){
					logger.debug(getObjectLabel(childLocation, "name") + " is decimated");
					continue;
				}
				PersonGroupType population = getPopulation(childLocation);
				if(population == null){
					logger.error("Population group not found");
					continue;
				}
				
				List<PersonType> personPopulation = getPersonPopulation(population);
				
				if(personPopulation.isEmpty()){
					logger.warn("Location population is decimated");
					AttributeType attr4 = new AttributeType();
					attr4.setName("decimated");
					attr4.getValues().add("true");
					attr4.setDataType(SqlDataEnumType.VARCHAR);
					childLocation.getAttributes().add(attr4);
					
					BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.LOCATION, childLocation);
				}
				else{
					AlignmentEnumType useAlignment = (Math.random() < .35 ? invertedAlignment : alignment);
					String childTitle = generateEpochTitle(useAlignment);
					EventType childEpoch = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, epoch);
					childEpoch.setLocation(childLocation);
					childEpoch.setName("Location " + getObjectLabel(childLocation, "name") + " experienced a " + useAlignment.toString() + " event: " + childTitle);
					AttributeType attr3 = new AttributeType();
					attr3.setName("alignment");
					attr3.setDataType(SqlDataEnumType.VARCHAR);
					attr3.getValues().add(alignment.toString());
					childEpoch.getAttributes().add(attr3);
					childEpoch.getGroups().add(population);
					logger.info("Location " + getObjectLabel(childLocation, "name") + " experienced a " + useAlignment.toString() + " event: " + childTitle);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, childEpoch);

					evolvePopulation(sessionId, childEpoch, alignment,population,evolutions);
				}
				
				
			}
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.LOCATION, region);

		} catch (ArgumentException | FactoryException e) {
			
			logger.error("Error",e);
		}
		
		
		return epoch;
	}
	
	
	private String[] demographicLabels = new String[]{"Alive","Child","Young Adult","Adult","Available","Senior","Mother","Coupled","Deceased"};
	private Map<String,List<PersonType>> newDemographicMap(){
		Map<String,List<PersonType>> map = new HashMap<>();
		for(String label : demographicLabels){
			map.put(label, new ArrayList<>());
		}
		return map;
	}
	private void setDemographicMap(Map<String,List<PersonType>>map, EventType parentEvent, PersonType person) throws FactoryException, ArgumentException{
			int age = (int)(CalendarUtil.getTimeSpan(person.getBirthDate(),parentEvent.getEndDate()) / YEAR);
			if(isDeceased(person)){
				map.get("Deceased").add(person);
			}
			else{
				map.get("Alive").add(person);

				if(age <= maxChildAge){
					map.get("Child").add(person);
				}
				else if(age >= seniorAge){
					map.get("Senior").add(person);
				}
				else{
					if(age < adultAge) map.get("Young Adult").add(person);
					else map.get("Adult").add(person);
					if(age >= minMarryAge && age <= maxMarryAge && person.getPartners().isEmpty()) map.get("Available").add(person);
					else if(person.getPartners().isEmpty() == false) map.get("Coupled").add(person);
					if("female".equals(person.getGender()) && age >=minMarryAge && age <= maxFertilityAge) map.get("Mother").add(person);
				}
			}
	}
	
	private Map<String,List<PersonType>> getPotentialPartnerMap(Map<String,List<PersonType>>map){
		Map<String,List<PersonType>> potentials = new HashMap<>();
		potentials.put("male", new ArrayList<>());
		potentials.put("female", new ArrayList<>());
		for(PersonType person : map.get("Available")){
			if(person.getPartners().isEmpty() == false){
				logger.warn(person.getName() + " is not a viable partner");
				continue;
			}
			if("male".equalsIgnoreCase(person.getGender())){
				potentials.get("male").add(person);
			}
			else{
				potentials.get("female").add(person);
			}
		}
		return potentials;
	}
	/*
	private boolean isLeader(PersonType person, PersonGroupType populationGroup) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		for(String name : leaderPopulation){
			PersonGroupType leaderGroup = (PersonGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupByName(name + " Leaders", GroupEnumType.PERSON, populationGroup, populationGroup.getOrganizationId());
			if(leaderGroup == null){
				logger.error("Group not found in " + populationGroup.getUrn() + ": " + name);
				continue;
			}
			out_bool = GroupService.getIsPersonInGroup(leaderGroup, person);
			if(out_bool) break;
		}
		return out_bool;
	}
	*/
	private boolean rulePersonBirth(AlignmentEnumType eventAlignmentType, PersonGroupType populationGroup, PersonType mother, int age) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		if("female".equalsIgnoreCase(mother.getGender()) == false || age < minMarryAge || age > maxFertilityAge) return out_bool;
		double odds = 0.001 + (mother.getPartners().isEmpty() ? 0.001 : 0.025 - (mother.getDependents().size() * 0.001));
		double rand = Math.random();
		if(rand < odds){
			out_bool = true;
		}
		
		return out_bool;
	}
	
	
	private boolean rulePersonDeath(AlignmentEnumType eventAlignmentType, PersonGroupType populationGroup, PersonType person, int age) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		boolean personIsLeader = false;//isLeader(person, populationGroup);
		double odds = 0.0001 + (age < maxChildAge ? 0.0025 : 0.0) + (age > avgDeathAge ? (age - avgDeathAge) * (personIsLeader ? 0.0001 : 0.0002) : 0.0) + (age >= maxAge ? 1.0 : 0.0);
		double rand = Math.random();
		if(rand < odds){
			out_bool = true;
		}
		return out_bool;
	}
	
	private void evolvePopulation(String sessionId, EventType parentEvent, AlignmentEnumType eventAlignment, PersonGroupType population, int iterations){

		List<PersonType> personPopulation = new ArrayList<>();
		if(populationCache.containsKey(population.getId())) personPopulation = populationCache.get(population.getId());
		else{
			try {
				personPopulation = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getPersonsInGroup(population);
				for(PersonType person : personPopulation){
					((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
					Factories.getAttributeFactory().populateAttributes(person);
				}
				populationCache.put(population.getId(), personPopulation);
			} catch (FactoryException | ArgumentException e) {
				
				logger.error("Error",e);
			}
		}
		if(personPopulation.isEmpty()){
			logger.warn("Population is decimated");
			return;
		}
		try{
			/*
			for(PersonType person : personPopulation){
				if(Factories.getAttributeFactory().getAttributeValueByName(person, "alignment") == null){
					logger.error("Null alignment when retrieving person " + person.getUrn());
				}
			}
			*/
		} catch (Exception e) {
			logger.error("Error",e);
		}
		
		for(int i = 0; i < iterations; i++){
			evolvePopulation(sessionId, parentEvent, eventAlignment, population, personPopulation);
		}
		
		try {
			for(PersonType person : personPopulation){
				if(Factories.getAttributeFactory().getAttributeValueByName(person, "alignment") == null){
					logger.error("Null alignment when " + (person.getId() > 0L ? "updating" : "adding") + " " + person.getName() + (person.getId() > 0L ? " " + person.getUrn() : ""));
				}
				if(person.getId() > 0L){

					BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.PERSON, person);
				}
			}

		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	private void evolvePopulation(String sessionId, EventType parentEvent, AlignmentEnumType eventAlignment, PersonGroupType population, List<PersonType> personPopulation){
		try {
			Map<String,List<PersonType>> demographicMap = newDemographicMap();
			ruleImmigration(sessionId, parentEvent, population);

			List<PersonType> newAdditions = new ArrayList<>();
			for(PersonType person : personPopulation){
				if(isDeceased(person)){
					continue;
				}
				int age = (int)(CalendarUtil.getTimeSpan(person.getBirthDate(),parentEvent.getStartDate()) / YEAR);
				
				/// If a female is ruled to be a mother, generate the baby
				///
				if("female".equalsIgnoreCase(person.getGender()) && rulePersonBirth(eventAlignment, population, person,age)){
					PersonType partner = (person.getPartners().isEmpty() ? null : person.getPartners().get(0));
					PersonType baby = randomPerson(user, personsDir, (partner != null && isPatriarchal ? partner : person).getLastName());
					baby.setBirthDate(parentEvent.getStartDate());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, baby);
					person.getDependents().add(baby);
					if(partner != null){
						partner.getDependents().add(baby);
					}
					BaseParticipantType bpt = ((GroupParticipationFactory)Factories.getBulkFactory(FactoryEnumType.GROUPPARTICIPATION)).newPersonGroupParticipation(population, baby);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, bpt);
					parentEvent.getActors().add(baby);
					newAdditions.add(baby);
					EventType birth = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, parentEvent);
					birth.setName("Birth of " + baby.getName());
					birth.setEventType(EventEnumType.INGRESS);
					birth.getOrchestrators().add(person);
					if(partner != null) birth.getInfluencers().add(partner);
					birth.getActors().add(baby);
					birth.setLocation(parentEvent.getLocation());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, birth);
				}
				if(rulePersonDeath(eventAlignment, population, person, age)){
					AttributeType attr4 = new AttributeType();
					attr4.setDataType(SqlDataEnumType.VARCHAR);
					attr4.setName("deceased");
					attr4.getValues().add("true");
					person.getAttributes().add(attr4);
					if(person.getPartners().isEmpty() == false){
						person.getPartners().get(0).getPartners().clear();
						person.getPartners().clear();
					}
					
					EventType death = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, parentEvent);
					death.setName("Death of " + person.getName() + " at the age of " + age);
					death.setEventType(EventEnumType.EGRESS);
					death.getActors().add(person);
					death.setLocation(parentEvent.getLocation());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, death);
				}
				setDemographicMap(demographicMap, parentEvent, person);

			}
			populationCache.get(population.getId()).addAll(newAdditions);
			for(PersonType person : newAdditions){
				setDemographicMap(demographicMap, parentEvent, person);
			}
			
			ruleCouples(sessionId,parentEvent,demographicMap);
			
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
		}

	}
	private void ruleImmigration(String sessionId, EventType parentEvent, PersonGroupType population) throws ArgumentException{
		double rand = Math.random();
		double odds = immigrateRate;
		List<PersonType> immigration = new ArrayList<>();
		if(rand < odds){
			/// Single
			
			PersonType person = randomPerson(user,personsDir);
			int age = (int)(CalendarUtil.getTimeSpan(person.getBirthDate(),parentEvent.getEndDate()) / YEAR);
			immigration.add(person);
			if(rand < odds/2){
				PersonType partner = randomPerson(user,personsDir,person.getLastName());
				int page = (int)(CalendarUtil.getTimeSpan(partner.getBirthDate(),parentEvent.getEndDate()) / YEAR);
				immigration.add(partner);
				if(age >= minMarryAge && page >= minMarryAge){
					partner.getPartners().add(person);
					person.getPartners().add(partner);
				}
				else if(age > minMarryAge && page < minMarryAge){
					person.getDependents().add(partner);
				}
				
				if(rand < odds / 4){
					int count = 1 + (int)(Math.random() * 10);
					for(int i = 0; i < count; i++){
						PersonType other = randomPerson(user,personsDir,person.getLastName());
						int cage = (int)(CalendarUtil.getTimeSpan(other.getBirthDate(),parentEvent.getEndDate()) / YEAR);
						immigration.add(other);
						if(cage > age){
							other.getDependents().add(person);
						}
						if(cage > page){
							other.getDependents().add(partner);
						}
						if(cage <= age && cage <= page){
							partner.getDependents().add(other);
							person.getDependents().add(other);
						}
					}
				}
			}
		}
		if(immigration.isEmpty() == false){
			
			for(PersonType person : immigration){
				/*
				if(Factories.getAttributeFactory().getAttributeValueByName(person, "alignment") == null){
					logger.error("Null alignment when creating immigrant " + person.getName());
				}
				*/
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, person);
				BaseParticipantType bpt = ((GroupParticipationFactory)Factories.getBulkFactory(FactoryEnumType.GROUPPARTICIPATION)).newPersonGroupParticipation(population, person);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, bpt);

			}
			populationCache.get(population.getId()).addAll(immigration);
			EventType immig = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, parentEvent);
			immig.setName("Immigration of " + immigration.get(0).getName() + (immigration.size() > 1 ? " and " + (immigration.size() - 1) + " others" : ""));
			immig.setEventType(EventEnumType.INGRESS);
			immig.getActors().addAll(immigration);
			immig.setLocation(parentEvent.getLocation());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, immig);
			//logger.info("Immigration of " + immigration.size() + " new people");
		}

	}
	private void ruleCouples(String sessionId, EventType parentEvent, Map<String,List<PersonType>> demographicMap) throws ArgumentException{
		Map<String,List<PersonType>> potentials = getPotentialPartnerMap(demographicMap);
		if(potentials.get("male").size() > 0 && potentials.get("female").size() > 0){
			for(int i = 0; i < potentials.get("female").size(); i++){
				PersonType fem = potentials.get("female").get(i);
				if(fem.getPartners().isEmpty() == false) continue;
				//boolean partnered = false;
				for(int m = 0; m < potentials.get("male").size(); m++){
					PersonType mal = potentials.get("male").get(m);
					if(mal.getPartners().isEmpty() == false) continue;
					double rand = Math.random();
					if(rand < marriageRate){
						//partnered = true;
						//logger.info(fem.getName() + " got married to " + mal.getName() + "! because " + rand + " < " + marriageRate + " (" + mal.getPartners().size() + ":" + fem.getPartners().size() + ")");
						fem.getPartners().add(mal);
						mal.getPartners().add(fem);
						EventType marriage = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, parentEvent);
						marriage.setName("Marriage of " + fem.getName() + " to " + mal.getName() + " (" + UUID.randomUUID().toString() + ")");
						marriage.setEventType(EventEnumType.STABLIZE);
						marriage.getActors().add(mal);
						marriage.getActors().add(fem);
						marriage.setLocation(parentEvent.getLocation());
						BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, marriage);
						break;
					}
				}
			}
		}
		else{
			// logger.debug("Population is currently unviable for new partners");
		}
		Set<PersonType> evaluated = new HashSet<PersonType>();
		for(PersonType person : demographicMap.get("Coupled")){
			if(person.getPartners().isEmpty()){
				//logger.error("Person " + person.getName() + " is marked as being coupled, but has no partners");
				continue;
			}
			PersonType partner = person.getPartners().get(0);
			if(evaluated.contains(partner) || evaluated.contains(person)) continue; 
			evaluated.add(person);
			evaluated.add(partner);
			double rand = Math.random();
			if(rand < divorceRate){
				//`logger.info(person.getName() + " got divorced from " + partner.getName() + "! because " + rand + " < " + divorceRate);
				EventType divorce = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(user, parentEvent);
				divorce.setName("Divorce of " + person.getName() + " from " + partner.getName() + " (" + UUID.randomUUID().toString() + ")");
				divorce.setEventType(EventEnumType.DESTABILIZE);
				divorce.getActors().add(person);
				divorce.getActors().add(partner);
				divorce.setLocation(divorce.getLocation());
				person.getPartners().clear();
				partner.getPartners().clear();
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.EVENT, divorce);
			}
		}

	}
	private List<String> loadFile(String path){
		List<String> words = new ArrayList<>();
		long start = System.currentTimeMillis();
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter(' ').withAllowMissingColumnNames(true).withQuote(null);
		CSVParser  csvFileParser = null;
		BufferedReader bir = null;
		try{
			bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			csvFileParser = new CSVParser(bir, csvFileFormat);

			for(CSVRecord record : csvFileParser){
				String id = record.get(0);
				if(id.matches("^\\d{8}$")){
					words.add(record.get(4));
				}
				else{
					//logger.debug(id);
				}
			}
			//logger.info("Imported: " + words.size() + " in " + (System.currentTimeMillis() - start) + "ms");
		}
		catch(IOException e){
			logger.error(e.getMessage());
		}
		return words;
	}
	
	public static class TradeType{
		private String name = null;
		private String description = null;
		public TradeType(){
			
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
	}
	
}