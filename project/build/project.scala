/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
import com.weiglewilczek.bnd4sbt.BNDPlugin
import sbt._

class ScalaModulesProject(info: ProjectInfo) extends ParentProject(info) with UnpublishedProject {

  // ===================================================================================================================
  // Dependencies
  // ===================================================================================================================

  object Dependencies {

    // Versions
    val Slf4sVersion = "1.0.0"
    val OsgiVersion = "4.2.0"
    val PaxExamVersion = "1.2.0"

    // Compile
    val slf4s = "com.weiglewilczek.slf4s" %% "slf4s" % Slf4sVersion withSources

    // Provided
    val osgiCore = "org.osgi" % "org.osgi.core" % OsgiVersion % "provided" withSources
    val osgiCompendium = "org.osgi" % "org.osgi.compendium" % OsgiVersion % "provided" withSources

    // Test
    val specs = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test" withSources
    val mockito = "org.mockito" % "mockito-all" % "1.8.4" % "test" withSources
    val junitIF = "com.novocode" % "junit-interface" % "0.3" % "test"
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.6.1" % "test"

    // Test (Pax Exam)
    val paxExam = "org.ops4j.pax.exam" % "pax-exam" % PaxExamVersion % "test"
    val paxExamJUnit = "org.ops4j.pax.exam" % "pax-exam-junit" % PaxExamVersion % "test"
    val paxExamCD = "org.ops4j.pax.exam" % "pax-exam-container-default" % PaxExamVersion % "test"
  }

  // ===================================================================================================================
  // Publishing
  // ===================================================================================================================

  override def managedStyle = ManagedStyle.Maven
  override def deliverAction = super.deliverAction dependsOn(publishLocal) // Fix for issue 99!
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
  lazy val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
//  lazy val publishTo = Resolver.file("Local Test Repository", Path fileProperty "java.io.tmpdir" asFile)

  // ===================================================================================================================
  // System properties
  // ===================================================================================================================

  System.setProperty("scalamodules.version", projectVersion.value.toString)

  // ===================================================================================================================
  // scalamodules-core subproject
  // ===================================================================================================================

  lazy val coreProject = project("core", "scalamodules-core", new CoreProject(_))

  class CoreProject(info: ProjectInfo) extends DefaultProject(info) with BNDPlugin {
    import Dependencies._

    override def libraryDependencies = Set(slf4s, osgiCore, osgiCompendium, specs, mockito, slf4jSimple)
    override def defaultExcludes = super.defaultExcludes || "*-sources.jar"

    override def packageSrcJar = defaultJarPath("-sources.jar")
    lazy val sourceArtifact = Artifact.sources(artifactID)
    override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)

    override def bndExportPackage = "com.weiglewilczek.scalamodules;version=\"%s\"".format(projectVersion.value) :: Nil
  }

  // ===================================================================================================================
  // scalamodules-core-it subproject
  // ===================================================================================================================

  lazy val coreITProject = project("core-it", "scalamodules-core-it", new CoreITProject(_), coreProject)

  class CoreITProject(info: ProjectInfo) extends DefaultProject(info) with UnpublishedProject {
    import Dependencies._

    override def libraryDependencies = Set(specs, mockito, paxExam, paxExamJUnit, paxExamCD, junitIF)
    override def defaultExcludes = super.defaultExcludes || "*-sources.jar"

    override def testAction = super.testAction dependsOn coreProject.`package`
    override def testFrameworks =
      super.testFrameworks ++ Seq(new TestFramework("com.novocode.junit.JUnitFrameworkNoMarker"))
  }
}

trait UnpublishedProject extends BasicManagedProject {
   override def publishLocalAction = task { None }
   override def deliverLocalAction = task { None }
   override def publishAction = task { None }
   override def deliverAction = task { None }
   override def artifacts = Set.empty
}
