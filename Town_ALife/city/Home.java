package city;


//Wrapper class to Building to differentiate between homes and factories.
public abstract class Home extends Building {
	private double baseImprovement;	
	public double currentImprovement;//how much better than outside this building is. Decays slowly. If it hits 0, breaks.
	
	protected Home(Bundle cost, int housing, double baseImprovement) {
		super(cost, housing);
		this.baseImprovement = currentImprovement = baseImprovement;
	}
	
	public void maintain(Bundle spent)
	{
		double ratio = spent.over(getCost().over(20));
		currentImprovement += ratio / (currentImprovement / baseImprovement);
	}
	
	public void degrade(){	
		currentImprovement -= (currentImprovement / baseImprovement) * (currentImprovement / baseImprovement) / (10*baseImprovement); 
	}
}
