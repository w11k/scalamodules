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

package org.scalamodules.demo.java.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.scalamodules.demo.*;

public class Activator implements BundleActivator {

    public void start(BundleContext context) {
        
        // Track services
        //  track = context track classOf[Greeting] on {
        //    case Adding(greeting, _)   => println("Adding Greeting: " + greeting.welcome)
        //    case Modified(greeting, _) =>
        //    case Removed(greeting, _)  => println("Removed Greeting: " + greeting.goodbye)
        //  }
        ServiceTracker tracker = new ServiceTracker(context, Greeting.class.getName(), null) {
            
            @Override
            public Object addingService(ServiceReference reference) {
                Object service = super.addingService(reference);
                Greeting greeting = (Greeting)service;
                System.out.println("Adding Greeting: " + greeting.welcome());
                return service;
            }
            
            @Override
            public void removedService(ServiceReference reference, Object service) {
                Greeting greeting = (Greeting)service;
                System.out.println("Removed Greeting: " + greeting.welcome());
                super.removedService(reference, service);
            }
        };
        tracker.open();
        
        // Register "Hello!" Greeting
        //  val hello = new Greeting {
        //    override def welcome = "Hello!"
        //    override def goodbye = "See you!";
        //  }
        //  context registerAs classOf[Greeting] theService hello
        Greeting hello = new Greeting() {
            public String welcome() { 
                return "Hello!";
            }
            public String goodbye() { 
                return "See you!";
            }
        };
        context.registerService(Greeting.class.getName(), hello, null);
        
        // Register "Welcome!" Greeting with properties
        //  val welcome = new Greeting {
        //    override def welcome = "Welcome!"
        //    override def goodbye = "Goodbye!"
        //  }
        //  context registerAs classOf[Greeting] withProperties Map("name" -> "welcome") theService welcome
        Greeting welcome = new Greeting() {
            public String welcome() { 
                return "Welcome!";
            }
            public String goodbye() { 
                return "Goodbye!";
            }
        };
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("name", "welcome");
        context.registerService(Greeting.class.getName(), welcome, properties);

        // Get one service
        // context getOne classOf[Greeting] andApply { 
        //   _.welcome
        // } match {
        //   case None          => noGreetingService() 
        //   case Some(welcome) => println(welcome)
        // }
        ServiceReference reference = 
            context.getServiceReference(Greeting.class.getName());
        if (reference != null) {
            try {
                Object service = context.getService(reference);
                Greeting greeting = (Greeting) service;
                if (greeting != null) {
                    System.out.println(greeting.welcome());
                } else {
                    noGreetingService();
                }
            } finally {
                context.ungetService(reference);
            }
        } else {
            noGreetingService();
        }

        // Get many services and their properties
        // context getMany classOf[Greeting] andApply { 
        //   (greeting, properties) => {
        //     val name = properties.get("name") match {
        //       case None    => "UNKNOWN"
        //       case Some(s) => s
        //     }
        //     name + " says: " + greeting.welcome
        //   }
        // } match {
        //   case None           => noGreetingService()
        //   case Some(welcomes) => welcomes.foreach { println }
        // }
        try {
            ServiceReference[] refs = context.getServiceReferences(
                    Greeting.class.getName(), null);
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    Object service = context.getService(ref);
                    Greeting greeting = (Greeting) service;
                    if (greeting != null) {
                        Object name = (ref.getProperty("name") != null) 
                                ? ref.getProperty("name") 
                                : "UNKNOWN";
                        String message = name + " says: " + greeting.welcome();
                        System.out.println(message);
                    }
                }
            } else {
                noGreetingService();
            }
        } catch (InvalidSyntaxException e) { // Do something meaningful ...
        }
        
        // Get many services with filter
        // context getMany classOf[Greeting] withFilter "(name=*)" andApply { 
        //   _.welcome 
        // } match {
        //   case None           => noGreetingService()
        //   case Some(welcomes) => welcomes.foreach { println }
        // }
        try {
            ServiceReference[] refs = context.getServiceReferences(
                    Greeting.class.getName(), "(name=*)");
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    Object service = context.getService(ref);
                    Greeting greeting = (Greeting) service;
                    if (greeting != null) {
                        System.out.println(greeting.welcome());
                    }
                }
            } else {
                noGreetingService();
            }
        } catch (InvalidSyntaxException e) { // Do something meaningful ...
        }
    }

    public void stop(BundleContext context) { // Nothing!
    }

    private void noGreetingService() {
        System.out.println("No Greeting service available!");
    }
}
