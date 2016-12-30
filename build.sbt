import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt.Keys._

import scalariform.formatter.preferences._

SbtScalariform.scalariformSettings

unmanagedBase <<= baseDirectory { base => base / "libs" }

lazy val root = (project in file("."))
  .settings(
    name := "midget",
    organization := "xyz.toebes",
    scalaVersion := "2.11.8")
  .settings(scalariformSettings :+ (
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignArguments, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 90)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(RewriteArrowSymbols, false)))
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang"                 % "scala-compiler"                    % scalaVersion.value,
    "pl.project13.scala"            %% "rainbow"                           % "0.2",
    "com.nrinaudo"                  %% "kantan.csv"                        % "0.1.9",
    "com.nrinaudo"                  %% "kantan.csv-generic"                % "0.1.9",
    "com.github.nscala-time"        %% "nscala-time"                       % "2.12.0",
    "net.incongru.watchservice"      % "barbary-watchservice"              % "1.0",
    "com.typesafe"                   % "config"                            % "1.3.0",
    "io.github.cloudify"            %% "spdf"                              % "1.3.1",
    "com.typesafe.akka"             %% "akka-http-spray-json-experimental" % "2.4.8",
    "org.scalaz"                    %% "scalaz-core"                       % "7.2.4",
    "org.seleniumhq.selenium"        % "selenium-java"                     % "2.53.1",
    "org.scalatest"                 %% "scalatest"                         % "2.2.6"      % "test"
))
