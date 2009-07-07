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
package org.scalamodules.core;

import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.ServiceReference;

public class ArrayHelper {

    public static String[] create(final String... names) {
        List<String> nameList = new LinkedList<String>();
        for (String name : names) nameList.add(name);
        return nameList.toArray(new String[0]);
    }

    public static ServiceReference[] create(final ServiceReference... refs) {
        List<ServiceReference> refList = new LinkedList<ServiceReference>();
        for (ServiceReference ref : refs) refList.add(ref);
        return refList.toArray(new ServiceReference[0]);
    }
}
