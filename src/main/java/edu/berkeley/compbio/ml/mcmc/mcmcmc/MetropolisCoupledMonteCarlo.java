package edu.berkeley.compbio.ml.mcmc.mcmcmc;

import edu.berkeley.compbio.ml.mcmc.MonteCarlo;
import edu.berkeley.compbio.ml.mcmc.MonteCarloFactory;
import edu.berkeley.compbio.ml.mcmc.MoveTypeSet;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lorax
 * Date: Apr 29, 2004
 * Time: 4:46:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetropolisCoupledMonteCarlo extends MonteCarlo
	{
	private static Logger logger = Logger.getLogger(MetropolisCoupledMonteCarlo.class);

	public ChainList getCurrentChainList()
		{
		return (ChainList) currentState;
		}

	public void setCurrentChainList(ChainList currentChainList)
		{
		this.currentState = currentChainList;
		}

	public MetropolisCoupledMonteCarlo()
		{
		super();
		movetypes = MoveTypeSet.getInstance("edu.berkeley.compbio.ml.mcmc.mcmcmc");
		logger.debug("Coupling chain: got move types " + movetypes.toString());
		}


	public static void run(MonteCarloFactory mcf, int burnIn, int numSteps, List<Double> heatFactors, int swapInterval)
		{
		assert heatFactors.get(0) == 1;
		ChainList chains = new ChainList();
		for (double hf : heatFactors)
			{
			chains.add(mcf.newChain(hf));
			}

		MonteCarlo mc = chains.get(0);
		mc.setColdest(true);

		MetropolisCoupledMonteCarlo couplingChain = new MetropolisCoupledMonteCarlo();
		couplingChain.setCurrentChainList(chains);
		couplingChain.setColdest(true); // suppress any output
		couplingChain.init();

		logger.info("Initialized MCMCMC: " + heatFactors);

		// burn in
		for (int i = 0; i < burnIn; i++)
			{
			couplingChain.doStep(0, swapInterval);
			}

		// reset counts
		for (MonteCarlo chain : chains)
			{
			chain.resetAcceptedCount();
			}
		couplingChain.resetAcceptedCount();

		// do the real run
		for (int i = 0; i < (numSteps / swapInterval); i++)
			{
			couplingChain.doStep(i, swapInterval);
			}
		}


	public void doStep(int step, int swapInterval)
		{

		// run each chain independently for a while
		// ** parallelizable
		for (MonteCarlo chain : getCurrentChainList())
			{
			int maxStep = step + swapInterval;
			for (int i = step; i < maxStep; i++)
				{
				chain.doStep(i);
				}
			}

		// do the temperature swap attempt
		super.doStep(1);
		}
	}
