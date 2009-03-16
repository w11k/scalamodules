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
package org.scalamodules.core

import java.util.Dictionary
import scala.collection.Map
import org.osgi.service.cm.{Configuration, ManagedService}
import org.scalamodules.util.jcl.Conversions.dictionaryToMap

trait BaseManagedService extends ManagedService {

  override def updated(properties: Dictionary[_, _]) {
    properties match {
      case null => handleUpdate(None)
      case _    => handleUpdate(Some(properties.asInstanceOf[Dictionary[String, Any]]))
    }
  }
  
  def handleUpdate(properties: Option[Map[String, Any]])
}
