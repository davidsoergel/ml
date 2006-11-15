package edu.berkeley.compbio.ml.mcmc;

/**
 * @author lorax
 * @version 1.0
 */
public abstract class MonteCarloFactory
	{
	private DataCollector dataCollector;
	private int id = 0;

	public abstract MonteCarlo newChain(double heatFactor);

	public DataCollector getDataCollector()
		{
		return dataCollector;
		}

	public void setDataCollector(DataCollector dataCollector)
		{
		this.dataCollector = dataCollector;
		}

	public int getId()
		{
		return id;
		}

	public void setId(int id)
		{
		this.id = id;
		}

	public void initChain(MonteCarlo mc)
		{
		mc.setDataCollector(dataCollector);
		mc.setId("" + id);
		id++;


		mc.init();
		}
	}
