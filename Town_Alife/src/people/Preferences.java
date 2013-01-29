package people;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class Preferences {
	
	private static double MUT_CHANCE = 0.2;
	private static double MUT_DEGREE = 0.05;	//should be < 1, percent of current value
	
	public HashMap<String,Double> preferences = new HashMap<String,Double>();	//core object handled via call through functions.
	/**
	 * @param prefs1 Parent 1 for genetics
	 * @param prefs2 Parent 2 for genetics
	 */
	public static Preferences GenPrefs(Preferences prefs1, Preferences prefs2) {
		Preferences newPrefs = new Preferences();
		//Iterator prefs1i = prefs1.preferences.entrySet().iterator();
		
		Double pref1, pref2, curr;
		Random gen = new Random();
		
		for(Map.Entry<String, Double> i: prefs1.entrySet()){
			String key = i.getKey();
			pref1 = i.getValue();
			pref2 = prefs2.get(key);	//TODO add null checks?
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
	
	public Double put(String key, Double value){
		return preferences.put(key, value);
	}
	
	public Double get(String key){
		return preferences.get(key);
	}

	public Set<Entry<String, Double>> entrySet(){
		return preferences.entrySet();
	}

	public void normalize(String[] keys, double minimum, double maximum) {
		double min = Double.MAX_VALUE;
		double curr;
		for (String key : keys)
		{
			curr = preferences.get(key);
			if (curr < min)
			{
				min = curr;
			}
		}
		//shift to 0, and sum.
		double sum = 0.0;
		for (String key : keys)
		{
			preferences.put(key, preferences.get(key) - min);
			sum += preferences.get(key);
		}
		
		double scale = (maximum - minimum) / sum;
		for (String key : keys)
		{
			preferences.put(key, preferences.get(key) * scale + minimum);
		}
	}

	public void sort(String[] sortSet) {
		HashSet<String> sorted = new HashSet<String>();
		String curr;
		do
		{
			curr = null;
			for (String key : sortSet)
			{
				if (!sorted.contains(key))
				{
					curr = key;
					break;
				}
			}
			if (curr == null) break; //save some time here
			
			double minVal = Double.MAX_VALUE;
			String minKey = null;
			for (String key : sortSet)
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
	
	public void setMinimum(String[] keys,double minimum)
	{
		for(String key: keys)
		{
			if (preferences.get(key) < minimum)
			{
				preferences.put(key, minimum);
			}
		}
	}
}
