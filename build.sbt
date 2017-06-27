import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT",
      publishMavenStyle := true
    )),
    name := "LogQuery",
    libraryDependencies := Seq(  
    scalaTest % Test,
    "org.apache.spark" % "spark-core_2.10" % "2.1.1"
    )
  )
