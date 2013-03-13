package city;

import java.util.HashSet;

import people.Person;
import city.homes.Homestead;
import city.homes.Shack;
import city.homes.Villa;


//Wrapper class to Building to differentiate between homes and factories.
public abstract class Home extends Building {
	private double improvementScale; 
	public double currentImprovement;//how much better than outside this building is. Decays slowly. If it hits 0, breaks.
	public HashSet<Person> occupants = new HashSet<Person>();	//used to determine outputs.
	
	protected Home(Bundle cost, int housing, double initialImprovement, double improvementScale) {
		super(cost, housing);
		currentImprovement = initialImprovement;
		this.improvementScale = improvementScale;
	}
	
	/**
	 * Applies maintenance to this.
	 * @param spent The bundle to use. It is modified by this call.
	 */
	public void maintain(Bundle spent)
	{
		double ratio = spent.over(getCost().over(20));
		currentImprovement += improvementScale*ratio/Math.max(currentImprovement*5, 2);
		spent.extract(getCost().over(20).times(ratio));//drops the used portions.
	}
	
	public boolean decay(){	
		currentImprovement -= improvementScale / 10.0; 
		if (currentImprovement <=0){
			for(Person occupant:occupants){
				occupant.home = null;
			}
		}
		return currentImprovement <= 0;
	}
	
	public int getRank(){
		if (getClass() == Shack.class) return 0;
		if (getClass() == Homestead.class) return 1;
		if (getClass() == Villa.class) return 2;
		System.err.println("BadHomeType");
		return -1;
	}

	public static Building getRankedHome(int i) {
		switch (i){
			case 0: return new Shack();
			case 1: return new Homestead();
			case 2: return new Villa();
			default: return null;
		}
	}
}
