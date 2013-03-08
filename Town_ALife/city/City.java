package city;

import java.util.ArrayList;

import people.Person;
import people.Person.SkillType;
import city.ResourcePile.Resource;
import economy.Economy;

/**
 * Container class for all buildings and people.
 * Includes driving loop.
 */

public class City {
	public static Economy economy = new Economy();
	public static ArrayList<Person> alive = new ArrayList<Person>();
	public static ArrayList<Building> places = new ArrayList<Building>();
	public static int[] avgSkill = new int[SkillType.values().length];
	
	double salaryHelper = 0;	//Summation of salaries this year. Used to determine mean salary.
	double meanSalary = 1.0;	//average value paid to workers last year.
	
	double[] netDemand = new double[Resource.values().length];	//generally, how much people want a resource vs how much there is. Also, average value the resource has.
	
	int yearsToRun = 100; 
	
	public City() {
		Init();
		while(yearsToRun >= 0){
			// TODO main logic loop.
			//Building owners place work bids
			//Beginning on resource with highest (quantity * price),
			//	select person with highest skill (can be owner) until full.
			//	Do work
			//	Pay both employees and owners.
			
			//People plan for future, and allocate current resources.
			//People trade for what they need.

			//People consume, build, reproduce, age, find mates, move.
			yearsToRun--;
		}
	}

	private void Init() {
		// TODO Initialize starting population and structures.
		
	}
}
