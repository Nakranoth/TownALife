package city;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

import people.CorpRatioComp;
import people.CorpWageComp;
import people.Corporation;
import people.Person;
import people.Person.SkillType;
import people.Preferences;
import city.ResourcePile.Resource;
import city.factories.BakersHut;
import city.factories.BlacksmithsHut;
import city.factories.CobblersHut;
import city.factories.ForestryHut;
import city.factories.Mine;
import city.factories.WorkmansHut;
import city.homes.Shack;
import economy.Economy;
import economy.Listing;

/**
 * Container class for all buildings and people.
 * Includes driving loop.
 */

public class City {
	final static String DATE_FORMAT = "yyyyMMddHHmmss";
	static Calendar cal = Calendar.getInstance();
	static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	
	
	public static Economy economy = new Economy();

	public static ArrayList<Person> alive = new ArrayList<Person>();
	public static ArrayList<Building> places = new ArrayList<Building>();
	public static ArrayList<Corporation> allCorps = new ArrayList<Corporation>();

	public ArrayList<Person> alivePointer = alive;//so I can see this in the debugger.
	public ArrayList<Building> placesPointer = places;//so I can see this in the debugger.
	public ArrayList<Corporation> corpPointer = allCorps;//so I can see this in the debugger.
	
	public static ArrayList<Listing> listings = new ArrayList<Listing>();

	public static int[] avgSkill = new int[SkillType.values().length];
	private static int[] avgSkillHelper = new int[SkillType.values().length];
	private static int workingPopSize = 0;
	
	private static int[] buildingsOfType = null;
	
	int yearsToRun = 100;

	private double meanUtility;
	private double utilDeviation;

	public static int year = 1;	//avoid difficult starting nonsense.
	
	public City() {
		init();
		Statistics.init();
		while(year < yearsToRun && alive.size() > 4){
			clearHelpers();
			
			//check for folded corps.
			Corporation foldCheck;
			for(Iterator<Corporation> i = allCorps.iterator();i.hasNext();){
				foldCheck = i.next();
				if(foldCheck.redistribute()||foldCheck.checkHoldings()) i.remove();
			}
			
			for(Person ready:alive){	//ready minimum wage and give resources to corps.
				ready.preWorkInit();
			}
			
			//Building owners place work bids
			ArrayList<Corporation> canPay = new ArrayList<Corporation>();
			for(Corporation toWage:allCorps){
				if (toWage.setWage() > 0){
					canPay.add(toWage);
				}
				buildingsOfType[toWage.holding.ordinal]++;	//Used for modifying wages.
			}
			
			//ready workforce
			ArrayList<Person> workForce = new ArrayList<Person>(alive);
			Iterator<Person> iter = workForce.iterator();
			while(iter.hasNext()){
				Person worker = iter.next();
				if (worker.realAge < 8) iter.remove();
				else{
					workingPopSize++;
					for(SkillType i:SkillType.values()){
						avgSkillHelper[i.ordinal()] += worker.getSkill(i);
					}
				}
			}
			
			//Beginning on resource with highest bid,
			//	select person with highest skill (can be owner) until full.
			//	Do work
			//	Pay both employees and owners.
			//	add to salaryHelper, skillHelper and workForce.
			Collections.sort(canPay,new CorpWageComp());
			for(Corporation toHire:canPay){
				if (toHire.hire(workForce) > 0) toHire.doWork();
			}

			//Update corp stats for next year 
			updateFromHelpers();
			
			//Decay buildings
			for (Iterator<Building> i = places.iterator();i.hasNext();){
				Building curr = i.next();
				if (curr.decay()) {
					i.remove();
				}
			}
			
			//People plan for future, and allocate current resources.
			for(Person curr: alive){
				curr.readyAllocations();
				listings.addAll(curr.listings);
			}

			economy.updateSupplyDemand(alive);
			
			//People trade for what they need.
			economy.doTrading();
			
			economy.updatePrices();
			
			double utilityHelper = 0;
			ArrayList<Person> marriable = new ArrayList<Person>();
			ArrayList<Person> marriableWithHome = new ArrayList<Person>();
			
			Collections.sort(allCorps, new CorpRatioComp());	//sorts corps for buying shares faster.
			
			//People consume, build, reproduce, age, find mates, move.
			for(Person curr:alive){
				curr.handleAllocations();
				utilityHelper += curr.utility;
				if(!curr.hasSpouse() && curr.realAge >= 16 && curr.realAge <= 60){
					marriable.add(curr);
					if(curr.home != null && !curr.livesAtHome()) marriableWithHome.add(curr);
				}
			}
			
			ArrayList<Person> born = new ArrayList<Person>();
			for(iter = alive.iterator();iter.hasNext();){
				Person reproducing = iter.next();
				if(!reproducing.male && reproducing.hasSpouse()){
					Person child = reproducing.tryMate();
					if (child != null){
						born.add(child);
					}
				}
			}
			alive.addAll(born);
			
			for(Person homeowner:marriableWithHome){
				Person curr;
				for(Iterator<Person> single = marriable.iterator();single.hasNext();){
					curr = single.next();
					if(curr.hasSpouse()) single.remove();//previously married this pass.
					if(curr != homeowner && curr.male != homeowner.male && Math.abs(homeowner.utility - curr.utility) < homeowner.getUtilityThreshold() && curr.acceptProposal(homeowner)){
						curr.wed(homeowner);
						break;
					}
				}
			}
			
			meanUtility = utilityHelper / alive.size();
			utilDeviation = stdDev(alive, meanUtility);
			
			Person curr;
			for(Iterator<Person> i = alive.iterator();i.hasNext();){
				curr = i.next();
				curr.realAge++;
				do{
					curr.age();
					curr.utility += utilDeviation;
				}while (curr.utility < meanUtility && curr.alive);
				if (!curr.alive){
					i.remove();
				}
			}
			Statistics.addThisYear(meanUtility, utilDeviation);
			year++;
		}
		if(year >= 100 && alive.size() > 300){
			
			System.out.println("Exit: "+alive.size());
			try {
				File file = new File("preferences");
				if(!file.exists()){
					file.createNewFile();
				}
				FileWriter writer = new FileWriter(file,true);
				BufferedWriter bWriter = new BufferedWriter(writer);
				for(Person living:alive){
					bWriter.write(living.preferences.toString());
					bWriter.flush();
				}
				bWriter.close();
			} catch (IOException e) {
				System.err.println("City: Buffered Writer");
				e.printStackTrace();
			}
			if (alive.size()>500)Statistics.save();//TODO find good value to save
		}
	}

	private void init() {
		//clear out any old data.
		alive.clear();
		allCorps.clear();
		places.clear();
		listings.clear();
		year = 0;
		Factory.samples.clear();
		Preferences.clearLoads();
		
		Factory.samples.add(new WorkmansHut(0));	//All factories of this type clone this sample.
		Factory.samples.add(new ForestryHut(1));
		Factory.samples.add(new CobblersHut(2));
		Factory.samples.add(new BlacksmithsHut(3));
		Factory.samples.add(new BakersHut(4));
		Factory.samples.add(new Mine(5));
		
		File loadFrom = new File("clean");
		
		buildingsOfType = new int[Factory.samples.size()];
		


		for(int i = 0; i < Factory.samples.size();i++){
			ResourcePile[] startingGoods = {
					new ResourcePile(Resource.crafts,10),
					new ResourcePile(Resource.crops,10),
					new ResourcePile(Resource.food,10),
					new ResourcePile(Resource.goods,10),
					new ResourcePile(Resource.metal,10),
					new ResourcePile(Resource.tools,10),
					new ResourcePile(Resource.wood,10),
					new ResourcePile(Resource.stone,10)
			};
			
			Person a = Person.loadPerson(loadFrom);
			a.realAge = 16;
			a.effectiveAge = 16;
			Corporation aCorp = new Corporation(Factory.samples.get(i).shallowClone(), a);
			a.addCorp(aCorp);
			allCorps.add(aCorp);
			places.add(aCorp.holding);
			Home shack = new Shack();
			places.add(shack);
			a.newRealestate(shack);
			a.income.insert(new Bundle(startingGoods));
			alive.add(a);
		}
		
		for(int i = 0; i < 9; i++){	//now with and extra 9 ppl, but with no corps.
			ResourcePile[] startingGoods = {
					new ResourcePile(Resource.crafts,10),
					new ResourcePile(Resource.crops,10),
					new ResourcePile(Resource.food,10),
					new ResourcePile(Resource.goods,10),
					new ResourcePile(Resource.metal,10),
					new ResourcePile(Resource.tools,10),
					new ResourcePile(Resource.wood,10),
					new ResourcePile(Resource.stone,10)
			};
			Person a = Person.loadPerson(loadFrom);
			a.realAge = 16;
			a.effectiveAge = 16;
			Home shack = new Shack();
			places.add(shack);
			a.newRealestate(shack);
			a.income.insert(new Bundle(startingGoods));
			alive.add(a);
		}
	}
	
	private void updateFromHelpers(){
		for(int i = 0; i < SkillType.values().length; i++){
			avgSkill[i] = avgSkillHelper[i] / Math.max(1,workingPopSize);
		}
	}
	
	private void clearHelpers() {
		for(int i = 0; i < SkillType.values().length; i++){
			avgSkillHelper[i] = 0;
		}
		for(Factory type:Factory.samples){
			buildingsOfType[type.ordinal] = 0;
		}
		workingPopSize = 0;
		listings.clear();
	}
	
	private double stdDev(ArrayList<Person> population, double meanUtility) {
		ArrayList<Double> devSq = new ArrayList<Double>();
		for(Person curr:population){
			devSq.add(Math.pow(curr.utility - meanUtility,2));
		}
		double stdev = 0;
		for(double num:devSq){
			stdev += num;
		}
		return Math.sqrt(stdev / Math.max(population.size()-1, 1));
	}
	
	public static void main(String[] args){
		while (true){
			for(int i = 0; i < 100; i++){
				new City();
			}
			try{
				File newPrefs = new File("preferences");
				if(newPrefs.canRead()){	//makes sure I can actually read from preferences.
					File clean = new File("clean");
					cal = Calendar.getInstance();
					clean.renameTo(new File(sdf.format(cal.getTime())));
					newPrefs.renameTo(new File("clean"));
				}
			}
			catch(Exception e){
				System.err.println("Main");
				e.printStackTrace();
			}
			System.out.println("Done");
		}
	}
}
