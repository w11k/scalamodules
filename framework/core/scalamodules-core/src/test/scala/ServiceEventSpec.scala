/**
 * Copyright (c) 2009-2010 WeigleWilczek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Heiko Seeberger   - initial API and implementation
 *   Roman Roelofsen   - initial API and implementation
 *   Kjetil Valstadsve - initial API and implementation
 */
package org.eclipse.scalamodules

import org.specs._
import org.specs.mock.Mockito

class ServiceEventSpec extends SpecificationWithJUnit with Mockito {

  "Creating a ServiceEvent (subclass)" should {
    "throw an IllegalArgumentException given a null service" in {
      new AddingService(null, Map[String, Any]()) must throwA [IllegalArgumentException]
    }
    "throw an IllegalArgumentException given null service properties" in {
      new AddingService(new TestClass1, null) must throwA [IllegalArgumentException]
    }
  }
}
