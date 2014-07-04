= A Java library for machine learning =

 * Provides a framework for clustering (both online and batch, both supervised and unsupervised).  Currently implements K-means, UPGMA, and Kohonen Self-Organizing Maps.
 * Implements various Monte Carlo methods, including Metropolis-coupled MCMC.
 * Implements various statistical models of strings, e.g. Markov models, variable-memory Markov models and the closely related Probabilistic Suffix Automata (PSAs) and Trees (PSTs),

This project has some goals in common with [http://en.wikipedia.org/wiki/Weka_(machine_learning) Weka] and [http://en.wikipedia.org/wiki/RapidMiner RapidMiner]  (aka YALE), but is far less developed.

(This has nothing to do with the [http://en.wikipedia.org/wiki/ML_programming_language ml programming language])
 
== Documentation ==

 * [http://dev.davidsoergel.com/maven/ml/apidocs API docs] (ml only)
 * [http://dev.davidsoergel.com/apidocs Aggregate API docs] (all projects hosted here; useful for navigating cross-package dependencies)

== Download ==

[http://maven.apache.org/ Maven] is by far the easiest way to make use of ml.  Just add these to your pom.xml:
{{{
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
}}}

If you really want just the jar, you can get it here: [http://dev.davidsoergel.com/artifactory/libs-releases/com/davidsoergel/ml/0.9/ml-0.9.jar ml-0.9.jar] (x KB)  ''May 9, 2008''

Or get the [http://dev.davidsoergel.com/jenkins/job/ml/lastStableBuild/ latest stable build] from the continuous integration server.
