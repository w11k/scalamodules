import com.weiglewilczek.bnd4sbt._
import sbt._

object ScalaModulesProject {

  trait UnpublishedProject extends BasicManagedProject {
     override def publishLocalAction = task { None }
     override def deliverLocalAction = task { None }
     override def publishAction = task { None }
     override def deliverAction = task { None }
     override def artifacts = Set.empty
  }
}

import ScalaModulesProject._

class ScalaModulesProject(info: ProjectInfo) extends ParentProject(info) with UnpublishedProject {

  // ===================================================================================================================
  // Dependencies
  // ===================================================================================================================

  object Dependencies {

    // Versions
    val OsgiVersion = "4.2.0"
    val PaxExamVersion = "1.2.3"
    val Slf4jVersion = "1.6.1"
    val Slf4sVersion = "1.0.3"
    val (specsVersion, mockitoVersion) = buildScalaVersion match {
      case "2.8.0" => "1.6.5" -> "1.8.4"
      case "2.8.1" => "1.6.7" -> "1.8.5"
      case _ => error("No clue what versions for specs and mockito to use!")
    }

    // Compile
    val slf4s = "com.weiglewilczek.slf4s" %% "slf4s" % Slf4sVersion withSources

    // Provided
    val osgiCore = "org.osgi" % "org.osgi.core" % OsgiVersion % "provided" withSources
    val osgiCompendium = "org.osgi" % "org.osgi.compendium" % OsgiVersion % "provided" withSources

    // Test
    val specs = "org.scala-tools.testing" %% "specs" % specsVersion % "test" withSources
    val mockito = "org.mockito" % "mockito-all" % mockitoVersion % "test" withSources
    val junitIF = "com.novocode" % "junit-interface" % "0.5" % "test"
    val slf4jSimple = "org.slf4j" % "slf4j-simple" % Slf4jVersion % "test" intransitive

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
//  lazy val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  lazy val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
//  lazy val publishTo = Resolver.file("Local Test Repository", Path fileProperty "java.io.tmpdir" asFile)

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

    import ExecutionEnvironment._
    override def bndBundleVendor = Some("WeigleWilczek")
    override def bndBundleLicense =
      Some("Apache 2.0 License (http://www.apache.org/licenses/LICENSE-2.0.html)")
    override def bndExecutionEnvironment = Set(Java5, Java6)
    override def bndExportPackage = "com.weiglewilczek.scalamodules;version=\"%s\"".format(projectVersion.value) :: Nil
    override def bndVersionPolicy = Some("[$(@),$(version;=+;$(@)))")
  }

  // ===================================================================================================================
  // scalamodules-core-it subproject
  // ===================================================================================================================

  lazy val coreITProject = project("core-it", "scalamodules-core-it", new CoreITProject(_), coreProject)

  class CoreITProject(info: ProjectInfo) extends DefaultProject(info) with UnpublishedProject {
    import Dependencies._

    System.setProperty("scalaModules.version", projectVersion.value.toString)
    System.setProperty("scala.version", buildScalaVersion)
    System.setProperty("slf4j.version", Slf4jVersion)
    System.setProperty("slf4s.version", Slf4sVersion)
    System.setProperty("specs.version", specsVersion)

    override def libraryDependencies = Set(specs, mockito, paxExam, paxExamJUnit, paxExamCD, junitIF)
    override def defaultExcludes = super.defaultExcludes || "*-sources.jar"

    override def testAction = super.testAction dependsOn coreProject.`package`
    override def testFrameworks =
      super.testFrameworks ++ Seq(new TestFramework("com.novocode.junit.JUnitFrameworkNoMarker"))
  }
}
