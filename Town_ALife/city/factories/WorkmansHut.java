package city.factories;

import people.Person.SkillType;
import city.Bundle;
import city.Factory;
import city.ResourcePile;
import city.ResourcePile.Resource;
import city.Task;

/**
 * Gathers any raw resource badly.
 * @author Nathan Killeen
 */
public class WorkmansHut extends Factory {

	private static Bundle costs = new Bundle(new ResourcePile(Resource.wood, 150));

	public WorkmansHut(int ordinal)
	{
		super(costs, 3, ordinal);
		name = "Workman's Hut";
		
		tasks.add(new Task(Resource.wood, 2, 20,SkillType.logger));		//Huts are Terrible sources of income.
		
		tasks.add(new Task(Resource.stone, 2, 20,SkillType.quarryer));
		
		tasks.add(new Task(Resource.metal, 2, 10,SkillType.miner));		//1 yr = 5 metal
		
		tasks.add(new Task(Resource.crops, 3, 30,SkillType.farmer));		//1 unskilled person can grow 30 units.
	}

	public WorkmansHut(WorkmansHut clone){
		super(costs, 3, clone.ordinal,clone.tasks);
		name = clone.name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends Factory> E shallowClone() {
		return (E) new WorkmansHut(this);
	}

}
