/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appfactory.jenkinsext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * This context listener will cleanup all thread local variables created by jenkins.
 * Note:Once the actual memory leak is fixed in jenkins code we do not use this listener
 */
public class ClassLoaderLeakPreventor implements ServletContextListener {
    //Could not use log4j for logging because there is no way to clean up the log4j(not loaded
    // from WEB-INF)
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //do nothing
    }

    public void contextDestroyed(ServletContextEvent event) {
        checkThreadLocalsForLeaksAndClear();
        System.out.println("Cleared all thread locals successfully ");
    }

    /*
    * Get the set of current threads as an array.
    *
    */
    private Thread[] getThreads() {
        // Get the current thread group
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        // Find the root thread group
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }

        int threadCountGuess = tg.activeCount() + 50;
        Thread[] threads = new Thread[threadCountGuess];
        int threadCountActual = tg.enumerate(threads);
        // Make sure we don't miss any threads
        while (threadCountActual == threadCountGuess) {
            threadCountGuess *= 2;
            threads = new Thread[threadCountGuess];
            // Note tg.enumerate(Thread[]) silently ignores any threads that
            // can't fit into the array
            threadCountActual = tg.enumerate(threads);
        }

        return threads;
    }

    private void checkThreadLocalsForLeaksAndClear() {
        Thread[] threads = getThreads();

        try {
            // Make the fields in the Thread class that store ThreadLocals
            // accessible
            Field threadLocalsField =
                    Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Field inheritableThreadLocalsField =
                    Thread.class.getDeclaredField("inheritableThreadLocals");
            inheritableThreadLocalsField.setAccessible(true);
            // Make the underlying array of ThreadLoad.ThreadLocalMap.Entry objects
            // accessible
            Class<?> tlmClass =
                    Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Field tableField = tlmClass.getDeclaredField("table");
            tableField.setAccessible(true);

            for (Thread thread : threads) {
                Object threadLocalMap;
                if (thread != null) {
                    // Clear the first map
                    threadLocalMap = threadLocalsField.get(thread);
                    checkThreadLocalMapForLeaks(threadLocalMap, tableField);
                    // Clear the second map
                    threadLocalMap =
                            inheritableThreadLocalsField.get(thread);
                    checkThreadLocalMapForLeaks(threadLocalMap, tableField);
                }
            }
        } catch (SecurityException e) {
            System.out.println("Check for ThreadLocals ForLeaks Failed for security reason " + e.getCause());
        } catch (NoSuchFieldException e) {
            System.out.println("There is no threadLocals or inheritableThreadLocals field " + e.getCause());
        } catch (ClassNotFoundException e) {
            System.out.println("java.lang.ThreadLocal$ThreadLocalMap is not found " + e.getCause());
        } catch (IllegalArgumentException e) {
            System.out.println("Check for ThreadLocals ForLeaks Failed " + e.getCause());
        } catch (IllegalAccessException e) {
            System.out.println("Check for ThreadLocals ForLeaks Failed " + e.getCause());
        }
    }


    /**
     * Analyzes the given thread local map object. Also pass in the field that
     * points to the internal table to save re-calculating it on every
     * call to this method.
     */
    private void checkThreadLocalMapForLeaks(Object map,
                                             Field internalTableField) throws IllegalAccessException,
            NoSuchFieldException {
        if (map != null) {
            Object[] table = (Object[]) internalTableField.get(map);
            if (table != null) {
                for (int j = 0; j < table.length; j++) {
                    if (table[j] != null) {
                        boolean potentialLeak = false;
                        // Check the key
                        Object key = ((WeakReference<?>) table[j]).get();
                        if (this.getClass().getClassLoader().equals(key) || loadedByThisOrChild(key)) {
                            potentialLeak = true;
                        }
                        // Check the value
                        Field valueField =
                                table[j].getClass().getDeclaredField("value");
                        valueField.setAccessible(true);
                        Object value = valueField.get(table[j]);
                        if (this.getClass().getClassLoader().equals(value) || loadedByThisOrChild(value)) {
                            potentialLeak = true;
                        }
                        if (potentialLeak) {
                            System.out.println("Clearing thread locals with key " + key + " value " + value);
                            Array.set(table, j, null);
                        }
                    }
                }
            }
        }
    }


    /**
     * @param o object to test, may be null
     * @return <code>true</code> if o has been loaded by the current classloader
     *         or one of its descendants.
     */
    private boolean loadedByThisOrChild(Object o) {
        if (o == null) {
            return false;
        }

        Class<?> clazz;
        if (o instanceof Class) {
            clazz = (Class<?>) o;
        } else {
            clazz = o.getClass();
        }

        ClassLoader cl = clazz.getClassLoader();
        while (cl != null) {
            if (cl == this.getClass().getClassLoader()) {
                return true;
            }
            cl = cl.getParent();
        }
        return false;
    }
}
