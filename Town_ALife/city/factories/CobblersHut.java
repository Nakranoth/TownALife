package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

public class CobblersHut extends Factory {

	static Bundle costs = new Bundle(new ResourcePile(Resource.wood,25)); 
	
	public CobblersHut()
	{
		super(costs, 5);
		name = "Cobbler's Hut";
		tasks.add(new Task(Resource.stone, 5, .5,SkillType.quarryer));	//2x efficiency from homestead. Still slow.
	}
	
}
