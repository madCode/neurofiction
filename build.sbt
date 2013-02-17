// mkdir -p src/{main,test}/{java,scala,R}/com/github/fommil/outsight

/** Project */
name := "outsight"

version := "1.0-SNAPSHOT"

organization := "com.github.fommil"

scalaVersion := "2.10.0"

/** Dependencies */
resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Sonatype" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies <<= scalaVersion { scala_version => 
    Seq(
        "com.github.fommil"    %  "java-logging"        % "1.0-SNAPSHOT",
        // "com.github.fommil" %  "scala-java-logging"  % "1.0-SNAPSHOT",
        "com.github.fommil"    %  "emokit"              % "1.0-SNAPSHOT",
        "org.springframework"  %  "spring-core"         % "3.2.1.RELEASE" intransitive(),
        "com.typesafe"         %  "config"              % "1.0.0",
        "com.typesafe.akka"    %% "akka-contrib"        % "2.1.0",
        "org.specs2"           %% "specs2"              % "1.13" % "test"
    )
}


/** Compilation */
fork := true

javaOptions += "-Xmx2G"

javaOptions += "-Djava.util.logging.config.file=logging.properties"

outputStrategy := Some(StdoutOutput)
