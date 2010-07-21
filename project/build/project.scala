/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
import sbt._

class ScalaModulesParentProject(info: ProjectInfo) extends ParentProject(info) {

  // ===================================================================================================================
  // Dependencies
  // ===================================================================================================================

  object Dependencies {

    // Versions
    lazy val osgiVersion = "4.2.0"

    // Provided
    lazy val osgiCore       = "org.osgi" % "org.osgi.core"       % osgiVersion % "provided" withSources
    lazy val osgiCompendium = "org.osgi" % "org.osgi.compendium" % osgiVersion % "provided" withSources

    // Test
    lazy val specs   = "org.scala-tools.testing" %% "specs"       % "1.6.5" % "test" withSources
    lazy val mockito = "org.mockito"             %  "mockito-all" % "1.8.4" % "test" withSources
  }

  // ===================================================================================================================
  // Subprojects
  // ===================================================================================================================

  val coreProject = project("scalamodules-core", "scalamodules-core", new ScalaModulesCoreProject(_))

  // ===================================================================================================================
  // scalamodules-core subprojects
  // ===================================================================================================================
  import Dependencies._

  class ScalaModulesCoreProject(info: ProjectInfo) extends DefaultProject(info) {
    override lazy val libraryDependencies = Set.empty + 
      osgiCore + osgiCompendium +
      specs + mockito
  }
}
