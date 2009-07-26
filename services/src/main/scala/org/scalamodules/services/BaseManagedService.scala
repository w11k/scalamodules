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
package org.scalamodules.services

import core.Util.dictionaryToMap

import java.util.Dictionary
import org.osgi.service.cm.ManagedService
import scala.collection.Map

/**
 * Makes handling managed services more convenient.
 */
trait BaseManagedService extends ManagedService {

  /**
   * Handles update. If the given Option is None the configuration is deleted.
   */
  def handleUpdate(props: Option[Map[String, Any]]): Unit

  /**
   * Transforms eventually null Dictionary to Option[Map[String, Any]]
   * and delegates to handleUpdate.
   */
  override def updated(props: Dictionary[_, _]) {
    props match {
      case null => handleUpdate(None)
      case _    => handleUpdate(Some(props.asInstanceOf[Dictionary[String, Any]]))
    }
  }
}
