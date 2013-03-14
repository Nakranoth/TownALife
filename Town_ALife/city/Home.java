package city;

import java.util.HashSet;

import people.Person;
import city.homes.Homestead;
import city.homes.Shack;
import city.homes.Villa;


//Wrapper class to Building to differentiate between homes and factories.
public abstract class Home extends Building {
	private double improvementScale; 
	private double initialImprovement;
	public double currentImprovement;//how much better than outside this building is. Decays slowly. If it hits 0, breaks.
	public HashSet<Person> occupants = new HashSet<Person>();	//used to determine outputs.
	
	protected Home(Bundle cost, int housing, double initialImprovement, double improvementScale) {
		super(cost, housing);
		currentImprovement = this.initialImprovement = initialImprovement;
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
		if (currentImprovement < initialImprovement / 3.0){
			for(Person occupant:occupants){
				occupant.home = null;
			}
		}
		return currentImprovement <= 0;
	}
	
	public static int getRank(Home check){
		if (check == null) return 0;
		if (check.getClass() == Shack.class) return 1;
		if (check.getClass() == Homestead.class) return 2;
		if (check.getClass() == Villa.class) return 3;
		System.err.println("BadHomeType");
		return -1;
	}

	public static Building getRankedHome(int i) {
		switch (i){
			case 0: return null;
			case 1: return new Shack();
			case 2: return new Homestead();
			case 3: return new Villa();
			default: return null;	//handles "what's past villa"
		}
	}
}
