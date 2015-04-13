logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "JAnalyse Repository" at "http://www.janalyse.fr/repository/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

libraryDependencies += "fr.janalyse"   %% "janalyse-ssh" % "0.9.18" % "compile"

