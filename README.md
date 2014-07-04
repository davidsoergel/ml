A Java library for machine learning
===================================

 * Provides a framework for clustering (both online and batch, both supervised and unsupervised).  Currently implements K-means, UPGMA, and Kohonen Self-Organizing Maps.
 * Implements various Monte Carlo methods, including Metropolis-coupled MCMC.
 * Implements various statistical models of strings, e.g. Markov models, variable-memory Markov models and the closely related Probabilistic Suffix Automata (PSAs) and Trees (PSTs),

This project has some goals in common with [Weka](http://en.wikipedia.org/wiki/Weka_(machine_learning)) and [RapidMiner](http://en.wikipedia.org/wiki/RapidMiner) (aka YALE), but is far less developed.

(This has nothing to do with the [ml programming language](http://en.wikipedia.org/wiki/ML_programming_language))
 
Documentation
-------------

 * API docs (temporarily unavailable)
 * Aggregate API docs (various of my related packages; useful for navigating cross-package dependencies) (temporarily unavailable)

Download
--------

[Maven](http://maven.apache.org/) is by far the easiest way to make use of ml.  Just add these to your pom.xml:
```
<repositories>
	<repository>
		<id>dev.davidsoergel.com releases</id>
		<url>http://dev.davidsoergel.com/nexus/content/repositories/releases</url>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
	</repository>
	<repository>
		<id>dev.davidsoergel.com snapshots</id>
		<url>http://dev.davidsoergel.com/nexus/content/repositories/snapshots</url>
		<releases>
			<enabled>false</enabled>
		</releases>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>edu.berkeley.compbio</groupId>
		<artifactId>ml</artifactId>
		<version>0.921</version>
	</dependency>
</dependencies>
```

If you really want just the jar, please see the Releases section above, or get the [latest stable build](http://dev.davidsoergel.com/jenkins/job/ml/lastStableBuild/edu.berkeley.compbio$ml/) from the build server.
