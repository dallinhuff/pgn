ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "com.dallinhuff.pgn",
    libraryDependencies ++= Seq(
      "org.typelevel"   %% "cats-parse"   % "0.3.9",
      "org.scalameta"   %% "munit"        % "0.7.29"    % Test
    ),
    testFrameworks += TestFramework("munit.Framework")
  )
