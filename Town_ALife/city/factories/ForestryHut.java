package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class ForestryHut extends Factory {

	static Bundle costs = new Bundle(new ResourcePile(Resource.wood, 25));	//1 man/years of solid homestead work.

	public ForestryHut() {
		super(costs, 5);
		
		name = "Forestry Hut";
		tasks.add(new Task(Resource.wood, 5, .5,SkillType.logger));	//2x efficiency from homestead. Still slow.
	}


}
