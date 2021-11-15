name := "Assignment3"

version := "0.1"

scalaVersion := "2.13.7"

lazy val akkaVersion = "2.6.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.6",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-multi-node-testkit"    % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "org.apache.pdfbox" % "pdfbox-tools" % "2.0.24",
)
