package city;

import java.util.List;

import people.Person;

/**
 * Container class for all buildings and people.
 * Includes driving loop.
 */

public class City {
	List<Building> places;
	List<Person> people;
	int numYears = 500; 
	
	public City() {
		Init();
		while(numYears >= 0){
			// TODO main logic loop.
		}
	}

	private void Init() {
		// TODO Initialize starting population and structures.
		
	}
}
