package people;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Preferences {

	//private static final int FILE_SIZE = 1713;
	
	//Set of all preferences for easy access.
	public static enum Preference {stubbornness,timeScale,incomeAlloc,goodsAlloc,
		upkeepAlloc,upkeepCap,newHomeAlloc,childAlloc,greed,craftUtil,
		goodUtil,foodUtil,homeUtil,need,love, smallestAlloc};
		
	public static File loadFrom = null;
	private static ArrayList<Preferences> loaded = new ArrayList<Preferences>();
	private static Random gen = new Random();
	
	private static double MUT_CHANCE = 0.2;
	private static double MUT_DEGREE = 0.05;	//should be < 1, percent of current value
	
	public HashMap<Preference,Double> preferences = new HashMap<Preference,Double>();	//core object handled via call through functions.

	public Preferences() {
		// TODO Auto-generated constructor stub
	}
	
	public static Preferences loadPref(File fromFile){
		if (loadFrom == null) loadPreferences(fromFile);
		return GenPrefs(loaded.get(gen.nextInt(loaded.size())),loaded.get(gen.nextInt(loaded.size())));
	}
	
	public static void loadPreferences(File prefSet) {
		if (loadFrom == null){
			try {
				loadFrom = prefSet;
				Scanner lineIter = new Scanner(prefSet);
				lineIter.useDelimiter(",\n?");
				String flag = null;
				double value = 0;
				int i = 0;
				Preferences insert = new Preferences();
				while (lineIter.hasNext()){
					flag = lineIter.next();	//gets a pair
					value = lineIter.nextDouble();
					insert.put(Enum.valueOf(Preference.class, flag), value);
					if (i%Preference.values().length == Preference.values().length - 1){
						//insert.put(Preference.smallestAlloc, 0.01); i++;	//temporary to load new value type.
						loaded.add(insert);
						insert = new Preferences();
					}
					i++;
				}
				lineIter.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param prefs1 Parent 1 for genetics
	 * @param prefs2 Parent 2 for genetics
	 */
	public static Preferences GenPrefs(Preferences prefs1, Preferences prefs2) {
		Preferences newPrefs = new Preferences();
		//Iterator prefs1i = prefs1.preferences.entrySet().iterator();
		
		Double pref1, pref2, curr;
		
		for(Map.Entry<Preference, Double> i: prefs1.entrySet()){
			Preference key = i.getKey();
			pref1 = i.getValue();
			pref2 = prefs2.get(key);
			if(gen.nextInt(2) == 0){
				curr = pref1;
			}
			else
			{
				curr = pref2;
			}
			if (gen.nextDouble() < MUT_CHANCE){
				double scale = MUT_DEGREE * curr;
				curr += gen.nextDouble() * scale * 2 - scale;
			}
			newPrefs.put(key, curr);
		}
		return newPrefs;
	}

	public Double put(Preference key, Double value){
		return preferences.put(key, value);
	}
	
	public Double get(Preference key){
		return preferences.get(key);
	}

	public Set<Entry<Preference, Double>> entrySet(){
		return preferences.entrySet();
	}

	//linear transformation; maps smallest value to minimum; maximum is what you would get if all values are same, except one larger.  
	public void normalize(Preference[] keys, double minimum, double maximum) {
		double min = Double.MAX_VALUE;
		double curr;
		for (Preference key : keys)
		{
			curr = preferences.get(key);
			if (curr < min)
			{
				min = curr;
			}
		}
		//shift to 0, and sum.
		double sum = 0.0;
		for (Preference key : keys)
		{
			preferences.put(key, preferences.get(key) - min);
			sum += preferences.get(key);
		}
		
		double scale = (maximum - minimum) / sum;
		for (Preference key : keys)
		{
			preferences.put(key, preferences.get(key) * scale + minimum);
		}
	}

	public void sort(Preference[] sortSet) {
		HashSet<Preference> sorted = new HashSet<Preference>();
		Preference curr;
		do
		{
			curr = null;
			for (Preference key : sortSet)
			{
				if (!sorted.contains(key))
				{
					curr = key;
					break;
				}
			}
			if (curr == null) break; //save some time here
			
			double minVal = Double.MAX_VALUE;
			Preference minKey = null;
			for (Preference key : sortSet)
			{
				if (!sorted.contains(key) && preferences.get(key) < minVal)
				{
					minVal = preferences.get(key);
					minKey = key;
				}
			}
			preferences.put(minKey, preferences.get(curr));
			preferences.put(curr, minVal);
			sorted.add(curr);
			if (minKey == null)
			{
				System.err.println("Woah! Null minVal in Preferences.sort()");
			}
		}while(curr != null);
	}
	
	public void setMinimum(Preference key,double minimum)
	{
		if (preferences.get(key) < minimum){
			preferences.put(key, minimum);
		}
	}

	public void setMaximum(Preference key, double maximum) {
		if (preferences.get(key) > maximum){
			preferences.put(key, maximum);
		}
	}
	
	public String toString(){
		String string = new String();
		
		for(Map.Entry<Preference, Double> i: entrySet()){
			Preference key = i.getKey();
			Double val = i.getValue();
			
			string +=key.toString()+","+val.toString()+",";
		}
		string += '\n';
		return string;
	}

	/**
	 * Cleans up statics for next run.
	 */
	public static void clearLoads() {
		loaded.clear();
		loadFrom = null;
	}
}
