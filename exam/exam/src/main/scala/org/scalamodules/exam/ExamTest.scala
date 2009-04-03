/**
 * Copyright 2009 Heiko Seeberger and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalamodules.exam

import scala.collection.mutable.Set
import org.ops4j.pax.exam.CoreOptions._
import org.ops4j.pax.exam.junit.Configuration

/**
 * Base for integration tests.
 * Already adds this bundle and org.scala-lang-osgi:scala-library.
 */
class ExamTest {

  /**
   * Add a "real" bundle containing a manifest.
   */
  def addBundle(groupId: String, artifactId: String, version: String) {
    bundles += mavenUrl(groupId, artifactId, version)
  }

  /**
   * Add a "synthetic" bundle for which a manifest will be generated.
   */
  def addWrappedBundle(groupId: String, artifactId: String, version: String) {
    bundles += wrappedUrl(mavenUrl(groupId, artifactId, version))
  }

  /**
   * Creates a confituration with Felix and all bundles provisioned.
   */
  @Configuration
  def configuration = options(felix, provision(bundles.toArray: _*))

  /**
   * Bundles to be provisioned.
   */
  protected val bundles = Set(
    mavenUrl("org.scalamodules", "scalamodules.exam", "1.0.1"),
    mavenUrl("org.scala-lang-osgi", "scala-library", "2.7.3"))

  private def mavenUrl(groupId: String, artifactId: String, version: String) =
    mavenBundle.groupId(groupId).artifactId(artifactId).version(version).getURL

  private def wrappedUrl(url: String) = wrappedBundle(url).getURL
}
