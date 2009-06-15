package edu.berkeley.compbio.ml.strings;

import com.davidsoergel.dsutils.ContractTest;
import com.davidsoergel.dsutils.ContractTestAwareContractTest;
import com.davidsoergel.dsutils.TestInstanceFactory;

import java.util.Queue;

/**
 * @author <a href="mailto:dev@davidsoergel.com">David Soergel</a>
 * @version $Id$
 */
public class IntKcountAbstractTest<T extends IntKcount>
		extends ContractTestAwareContractTest<Kcount>//implements TestInstanceFactory<Kcount>
	{
// ------------------------------ FIELDS ------------------------------

	protected final TestInstanceFactory<T> tif;


// --------------------------- CONSTRUCTORS ---------------------------

	public IntKcountAbstractTest(final TestInstanceFactory<T> tif)
		{
		this.tif = tif;
		}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addContractTestsToQueue(final Queue<ContractTest> theContractTests)
		{
		theContractTests.add(new KcountAbstractTest<T>(tif));
		}
	}
