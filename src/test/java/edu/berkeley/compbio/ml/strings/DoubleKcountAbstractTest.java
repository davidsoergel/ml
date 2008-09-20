package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.dsutils.ContractTest;
import com.davidsoergel.dsutils.ContractTestAwareContractTest;
import com.davidsoergel.dsutils.TestInstanceFactory;

import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class DoubleKcountAbstractTest<T extends DoubleKcount>
		extends ContractTestAwareContractTest<Kcount>//implements TestInstanceFactory<Kcount>
	{
	protected TestInstanceFactory<T> tif;

	public DoubleKcountAbstractTest(TestInstanceFactory<T> tif)
		{
		this.tif = tif;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addContractTestsToQueue(Queue<ContractTest> theContractTests)
		{
		theContractTests.add(new KcountAbstractTest<T>(tif));
		}
	}
