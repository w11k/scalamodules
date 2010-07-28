/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
import com.weiglewilczek.bnd4sbt.BNDPlugin
import sbt._

class ScalaModulesParentProject(info: ProjectInfo) extends ParentProject(info) {

  // ===================================================================================================================
  // Dependencies
  // ===================================================================================================================

  object Dependencies {

    // Versions
    lazy val osgiVersion    = "4.2.0"
    lazy val paxExamVersion = "1.2.0"

    // Provided
    lazy val osgiCore       = "org.osgi" % "org.osgi.core"       % osgiVersion % "provided" withSources
    lazy val osgiCompendium = "org.osgi" % "org.osgi.compendium" % osgiVersion % "provided" withSources

    // Test
    lazy val specs   = "org.scala-tools.testing" %% "specs"           % "1.6.5" % "test" withSources
    lazy val mockito = "org.mockito"             %  "mockito-all"     % "1.8.4" % "test" withSources
    lazy val junitIF = "com.novocode"            %  "junit-interface" % "0.3"   % "test"

    // Test (Pax Exam)
    lazy val paxExam      = "org.ops4j.pax.exam" % "pax-exam"                   % paxExamVersion % "test"
    lazy val paxExamJUnit = "org.ops4j.pax.exam" % "pax-exam-junit"             % paxExamVersion % "test"
    lazy val paxExamCD    = "org.ops4j.pax.exam" % "pax-exam-container-default" % paxExamVersion % "test"
  }

  // ===================================================================================================================
  // Subprojects
  // ===================================================================================================================

  lazy val coreProject =
    project("scalamodules-core", "scalamodules-core", new ScalaModulesCoreProject(_))
  lazy val coreITProject =
    project("scalamodules-core-it", "scalamodules-core-it", new ScalaModulesCoreITProject(_), coreProject)
//  lazy val examplesAPIProject =
//    project("scalamodules-examples" / "scalamodules-examples-api",
//            "scalamodules-examples-api",
//            new ScalaModulesExamplesAPIProject(_))
//  lazy val examplesCreateProject =
//    project("scalamodules-examples" / "scalamodules-examples-create",
//            "scalamodules-examples-create",
//            new ScalaModulesExamplesCreateProject(_))

  // ===================================================================================================================
  // scalamodules-core subproject
  // ===================================================================================================================

  class ScalaModulesCoreProject(info: ProjectInfo) extends DefaultProject(info) with BNDPlugin {
    import Dependencies._
    override lazy val libraryDependencies = Set(osgiCore, osgiCompendium, specs, mockito)
    override lazy val bndExportPackage = "org.eclipse.scalamodules;version=\"%s\"".format(projectVersion.value) :: Nil
  }

  // ===================================================================================================================
  // scalamodules-core-it subproject
  // ===================================================================================================================

  class ScalaModulesCoreITProject(info: ProjectInfo) extends DefaultProject(info) {
    import Dependencies._
    override lazy val testAction = super.testAction dependsOn coreProject.`package`
    override lazy val libraryDependencies = Set(specs, mockito, paxExam, paxExamJUnit, paxExamCD, junitIF)
    override lazy val testFrameworks = super.testFrameworks ++ Seq(new TestFramework("com.novocode.junit.JUnitFrameworkNoMarker"))
  }

  // ===================================================================================================================
  // scalamodules-examples-api subproject
  // ===================================================================================================================

  class ScalaModulesExamplesAPIProject(info: ProjectInfo) extends DefaultProject(info) with BNDPlugin {
    override lazy val bndExportPackage =
      "org.eclipse.scalamodules.examples;version=%s".format(projectVersion.value) :: Nil
  }

  // ===================================================================================================================
  // scalamodules-examples-create subproject
  // ===================================================================================================================

  class ScalaModulesExamplesCreateProject(info: ProjectInfo) extends DefaultProject(info) with BNDPlugin {
  }
}
