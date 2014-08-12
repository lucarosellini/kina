Kina Examples
=============

This project contains java and scala examples for Kina. These examples are
available also online at [Doc website](http://wordpress.dev.strat.io/examples/) where references to
specific datamodels have been rewritten in a generic form.


Requirements
============

  * Cassandra 2.0.8
  * Kina 0.1.0
  * Apache Maven >= 3.0.4
  * Java7


How to start
============

  * Clone the project

  * Eclipse: import as Maven project. IntelliJ: open the project by selecting the POM file

  * To compile the project:
        mvn compile

  * To package the project:
        mvn package


Kina context configuration
==========================

Edit kina.testutils.ContextProperties Java class and set the attributes for your environment.


Datasets used in the examples
=============================

Datasets can be found in the [Doc download area](http://docs.dev.strat.io/doc/tutorials/datasets/)
and instructions for importing them at:

  * Tweets dataset: [Creating a POJO tutorial](http://wordpress.dev.strat.io/devguides/tutorials/creating-a-pojo-for-stratio-kina/#creatingDataModel)
  * Crawler dataset: [First steps with Kina tutorial](http://wordpress.dev.strat.io/devguides/tutorials/first-steps-with-stratio-kina/#__RefHeading__2448_21369393)


Running an example
==================

  java -cp $CLASSPATH ExampleClassOrObject

Where CLASSPATH should contain all the jars from KINA_HOME/jars/ and the kina-examples one.


List of Examples
================

Java:

  * kina.examples.java.AggregatingData
  * kina.examples.java.GroupingByColumn
  * kina.examples.java.MapReduceJob
  * kina.examples.java.WritingEntityToCassandra
  * kina.examples.java.CreatingCellRDD
  * kina.examples.java.GroupingByKey
  * kina.examples.java.WritingCellToCassandra

Scala:

  * kina.examples.scala.AggregatingData
  * kina.examples.scala.GroupingByColumn
  * kina.examples.scala.MapReduceJob
  * kina.examples.scala.WritingEntityToCassandra
  * kina.examples.scala.CreatingCellRDD
  * kina.examples.scala.GroupingByKey
  * kina.examples.scala.WritingCellToCassandra
  * kina.examples.scala.UsingScalaEntity
  * kina.examples.scala.UsingScalaCollectionEntity
