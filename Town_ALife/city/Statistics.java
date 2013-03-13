package city;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;

import people.Corporation;
import people.Person;
import city.ResourcePile.Resource;

/**
 * Generates, stores, and writes statistics about a run in CSV format.
 * @author Nathan Killeen
 */
public class Statistics {
	public static ArrayList<String> supplies = null;
	public static ArrayList<String> demands = null;
	public static ArrayList<String> prices = null;
	
	public static ArrayList<String> population = null;	//Total, % male, mean util, util deviation, home type counts.
	public static ArrayList<String> corps = null;	//by type
	
	/**
	 * Initializes this for use. Should be called per new city.
	 */
	public static void init(){
		supplies = new ArrayList<String>();
		demands = new ArrayList<String>();
		prices = new ArrayList<String>();
		
		population = new ArrayList<String>();	//Total, % male, mean util, util deviation, home type counts.
		corps = new ArrayList<String>();
		
		String construct = new String();
		String[] resources = new String[Resource.values().length];
		for(Resource type:Resource.values()){
			resources[type.ordinal()] = type.name();
		}
		for (int i = 0; i < Resource.values().length;i++){
			construct += resources[i] + ",";
		}
		supplies.add(construct);
		demands.add(construct);
		prices.add(construct);
		
		population.add( new String("Total,Percent Male,Mean Utility,Utility Standard Deviation, Homeless,Shack,Homestead,Villa"));
		corps.add(new String("Baker's Huts,Blacksmith's Huts,Cobbler's Hut,Forestry Hut,Workman's Hut"));
	}
	
	/**
	 * Adds all of this year's statistics to itself.
	 * @param meanUtil
	 * @param utilStdDev
	 */
	public static void addThisYear(double meanUtil, double utilStdDev){
		supplies.add(City.economy.getSupplyString());
		demands.add(City.economy.getDemandString());
		prices.add(City.economy.getPriceString());
		
		
		String popString = new String();
		int male = 0;	//must divide by pop
		int[] homeClass = new int[4];
		for(Person curr: City.alive){
			if (curr.male) male++;
			if(curr.home == null) homeClass[0]++;
			else homeClass[curr.home.getRank()]++;
		}
		popString += City.alive.size() + "," + ((double)male / (double)City.alive.size())+","+meanUtil+","+utilStdDev+",";
		for(int i = 0; i<4;i++){
			popString += homeClass[i] + ",";
		}
		population.add(popString);
		
		String corpString = new String();
		int[] corpClass = new int[Factory.samples.size()];
		for(Corporation aCorp:City.allCorps){
			corpClass[aCorp.holding.ordinal]++;
		}
		for(int i = 0; i<Factory.samples.size();i++){
			corpString += corpClass[i] + ",";
		}
		corps.add(corpString);
	}

	/**
	 * Terrible save function. Saves everything. 
	 */
	public static void save() {
		City.cal = Calendar.getInstance();
		try {
			File demandFile = new File(City.sdf.format(City.cal.getTime())+"_demands.csv");
			demandFile.createNewFile();
			FileWriter fWriter = new FileWriter(demandFile);
			BufferedWriter writer = new BufferedWriter(fWriter);
			for(String line:demands){
				line += "\n";
				writer.write(line);		
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			File supplyFile = new File(City.sdf.format(City.cal.getTime())+"_supplies.csv");
			supplyFile.createNewFile();
			FileWriter fWriter = new FileWriter(supplyFile);
			BufferedWriter writer = new BufferedWriter(fWriter);
			for(String line:supplies){
				line += "\n";
				writer.write(line);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			File priceFile = new File(City.sdf.format(City.cal.getTime())+"_prices.csv");
			priceFile.createNewFile();
			FileWriter fWriter = new FileWriter(priceFile);
			BufferedWriter writer = new BufferedWriter(fWriter);
			for(String line:prices){
				line += "\n";
				writer.write(line);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			File popFile = new File(City.sdf.format(City.cal.getTime())+"_pop.csv");
			popFile.createNewFile();
			FileWriter fWriter = new FileWriter(popFile);
			BufferedWriter writer = new BufferedWriter(fWriter);
			for(String line:population){
				line += "\n";
				writer.write(line);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			File corpFile = new File(City.sdf.format(City.cal.getTime())+"_corps.csv");
			corpFile.createNewFile();
			FileWriter fWriter = new FileWriter(corpFile);
			BufferedWriter writer = new BufferedWriter(fWriter);
			for(String line:corps){
				line += "\n";
				writer.write(line);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
