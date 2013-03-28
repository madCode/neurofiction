// mkdir -p src/{main,test}/{java,scala,R}/com/github/fommil/outsight

name := "outsight"

version := "1.0-SNAPSHOT"

organization := "com.github.fommil"

scalaVersion := "2.10.1"

resolvers ++= Seq(
                Resolver.mavenLocal,
                Resolver.sonatypeRepo("releases"),
                Resolver.sonatypeRepo("snapshots"),
                Resolver.typesafeRepo("releases"),
                Resolver.typesafeRepo("snapshots")
              )


libraryDependencies <<= scalaVersion { scala_version => 
    Seq(
        "com.github.fommil"    %  "java-logging"             % "1.0",
        // "com.github.fommil" %  "scala-java-logging"       % "1.0-SNAPSHOT",
        "com.github.fommil"    %  "common-utils"             % "1.0-SNAPSHOT",
        "com.github.fommil"    %  "emokit"                   % "1.0-SNAPSHOT",
        "com.github.fommil"    %  "common-utils"             % "1.0-SNAPSHOT",
        "org.scala-lang"       %  "scala-swing"              % scala_version,
        "org.springframework"  %  "spring-core"              % "3.2.1.RELEASE" exclude("commons-logging", "commons-logging"),
        "org.springframework"  %  "spring-jdbc"              % "3.2.1.RELEASE" exclude("commons-logging", "commons-logging"),
        "com.typesafe"         %  "config"                   % "1.0.0",
        "org.pegdown"          %  "pegdown"                  % "1.2.1",
        "org.xhtmlrenderer"    %  "flying-saucer-core"       % "9.0.1",
        "org.swinglabs.swingx" %  "swingx-all"               % "1.6.4",
        "com.typesafe.akka"    %% "akka-contrib"             % "2.1.2" intransitive(),
        "com.typesafe.akka"    %% "akka-actor"               % "2.1.2",
        "org.specs2"           %% "specs2"                   % "1.13" % "test",
        "org.scalamock"        %% "scalamock-specs2-support" % "3.0.1" % "test"
    )
}

fork := true

javaOptions += "-Xmx2G"

javaOptions += "-Djava.util.logging.config.file=logging.properties"

net.virtualvoid.sbt.graph.Plugin.graphSettings
