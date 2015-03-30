name := "crun"

version := "1.0"

lazy val `crun` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

//libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

val akka = "2.3.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-contrib" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "commons-io" % "commons-io" % "2.4" % "test",
  "com.github.nscala-time" %% "nscala-time" % "1.2.0"
  // -- testing --
  // -- Logging --
  ,"ch.qos.logback" % "logback-classic" % "1.1.1"
  // -- Akka --
  ,"com.typesafe.akka" %% "akka-testkit" % akka % "test"
  ,"com.typesafe.akka" %% "akka-actor" % akka
//  ,"org.iq80.leveldb" %% "leveldb" % "0.7"
//  ,"org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  ,"com.typesafe.akka" %% "akka-slf4j" % akka
  ,"com.typesafe.akka" %% "akka-cluster" % akka
  ,"com.typesafe.akka" %% "akka-persistence-experimental" % akka
  // -- json --
  ,"org.json4s" %% "json4s-jackson" % "3.2.10"
  // -- config --
  ,"com.typesafe" % "config" % "1.2.0",
  "com.typesafe.akka" %% "akka-contrib" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6",
  "com.amazonaws" % "aws-java-sdk" % "1.0.002",
  "com.jcraft" % "jsch" % "0.1.51",
  "commons-io" % "commons-io" % "2.4" % "test")


fork in run := true