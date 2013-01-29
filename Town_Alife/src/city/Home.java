package city;


public abstract class Home extends Building {

	int housing;
	
	protected Home(Resource[] cost, int housing) {
		super(cost);
		this.housing = housing;
	}

	
	@Override
	public void trimToCapacity()
	{
		while(workerList.get(0).size() > housing)
		{
			//TODO: Remove this from person's action pool.
			workerList.get(0).remove(workerList.get(0).size() -1);	//trim last person from list until small enough
		}
		while(workerList.get(0).size() + workerList.get(1).size() > housing)
		{
			//TODO: Remove this from person's action pool.
			workerList.get(1).remove(workerList.get(1).size() -1);	//trim last person from list until small enough
		}
		while(workerList.get(0).size() + workerList.get(1).size() + workerList.get(2).size() > housing)
		{
			//TODO: Remove this from person's action pool.
			workerList.get(2).remove(workerList.get(2).size() -1);	//trim last person from list until small enough
		}
	}
}
