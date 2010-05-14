import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  override def compileOptions = Deprecation :: Unchecked :: Nil
  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
  override def libraryDependencies = Set(
    "org.osgi" % "org.osgi.core" % "4.2.0",
    "org.osgi" % "org.osgi.compendium" % "4.2.0",
    "org.scala-tools.testing" % "specs_2.8.0.RC2" % "1.6.5-SNAPSHOT" % "test",
    "org.mockito" % "mockito-all" % "1.8.4" % "test",
    "junit" % "junit" % "4.7" % "test",
    "org.apache.felix" % "org.apache.felix.framework" % "2.0.2" % "test"
  )
}
