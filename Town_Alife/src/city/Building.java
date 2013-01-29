package city;

import java.util.ArrayList;
import java.util.List;

import people.Person;

public abstract class Building {
	List<Task> tasks;
	ArrayList<ArrayList<Person>> workerList;	//used to determine worker standings.
	Resource[] cost;
	
	protected Building(Resource[] cost)
	{
		this.cost = cost;
		workerList.add(new ArrayList<Person>());	//Owner(s)
		workerList.add(new ArrayList<Person>());	//Heir(s)
		workerList.add(new ArrayList<Person>());	//Worker(s)
	}
	
	protected void addTask(Task newTask)
	{
		if (!tasks.contains(newTask))
		{
			tasks.add(newTask);
		}
	}
	
	public void registerWorker(Person newWorker)
	{
		
	}
	
	public abstract String toString();
	public abstract void trimToCapacity();

}
