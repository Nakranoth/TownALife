package people;

import java.util.ArrayList;
import java.util.HashMap;

import city.Resource;

/**
 * @author Nathan Killeen
 * @Desc Not threadable. Each person is run serially from AI thread. Reduces context switching.
 */
public class Person{
	
	private static String[] allocSet = {"year0Alloc","yearIAlloc","yearJAlloc","yearKAlloc"},
			yearSet = {"yearI","yearJ","yearK"};
	
	public int age;
	
	protected Preferences preferences = new Preferences();
	
	private int income = 0;	//Tracks how much I made last year, used for resource allocation.
	private long money = 0;
	private HashMap<String, Resource> holdings;
	private HashMap<String, Allocation> savings = new HashMap<String, Allocation>();
	private ArrayList<Person> heirs = new ArrayList<Person>();
	private Person spouse = null; 
	
	public Person()
	{
		//TODO Load preferences
		preferences.put("stubbornness", 20.0);	//Reevaluate inferiority in 20 years.
		preferences.put("year0Alloc", 0.3);
		preferences.put("yearI", 5.0);
		preferences.put("yearIAlloc", 0.3);
		preferences.put("yearJ", 10.0);
		preferences.put("yearJAlloc", 0.2);
		preferences.put("yearK", 20.0);
		preferences.put("yearKAlloc", 0.2);
	}
	
	public Person(Preferences prefs)
	{
		preferences = prefs;
	}
	
	public Person(Person parentA, Person parentB)
	{
		preferences = Preferences.GenPrefs(parentA.preferences, parentB.preferences); 
		preferences.normalize(allocSet, 0.01, 1.0);
		preferences.sort(yearSet);
		preferences.setMinimum(yearSet, 1.0);
	}
	
	//TODO Update each planning scale.
	public void plan()
	{
		//For new buildings, assume output of self @ full time, with no "profit" from extra workers.
		//	income of new building - current income = relative income.
		//	cost / relative income = payoff years.
		//	Interval method:
		//		Evaluate Income in {0, i, j, k} years.
		//		Per building type not marked as "inferior"
		//			examine relative income at t=variable
		//			If QIncome <= 0 mark building as inferior for "stubbornness" years.
		//		Select best building at each time frame, or null if empty set.
		//		Apportion percentage of current wealth 
		//scanBids()
		//
	}
	
	public Preferences getPreferecnces()
	{
		return preferences; 
	}
	
	/*public static void main(String[] args)
	{
		Person a = new Person();
		Person b = new Person();
		for (int i = 0; i < 500; i++)
		{
			a = new Person(a,b);
			b = new Person(a,b);
		}

		for (Map.Entry<String, Double> entry: a.preferences.entrySet() )
		{
			System.out.println(entry.getKey()+ ": "+ entry.getValue());
		}
		
		return;
	}*/
}
