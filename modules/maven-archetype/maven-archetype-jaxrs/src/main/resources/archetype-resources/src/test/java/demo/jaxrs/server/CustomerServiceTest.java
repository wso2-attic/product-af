/*
 * Copyright 2011-2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.jaxrs.server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Customer Service in JAXRS Project.
 */
public class CustomerServiceTest extends TestCase {
    public CustomerServiceTest( String testName ) {
        super( testName );
    }

    public static Test suite() {
        return new TestSuite( CustomerServiceTest.class );
    }

    /**
     * Tests whether the existing customer information is correct
     */
    public void testGetExistingCustomer() {
        CustomerService cs = new CustomerService();
        Customer c = cs.getCustomer("123");
        assertTrue(c.getName() == "John");
    }
    
    /**
     * Tests whether the non-existing customer is correctly returned
     */
    public void testGetNonExistingCustomer() {
        CustomerService cs = new CustomerService();
        Customer c = cs.getCustomer("124");
        assertNull(c);
    }
    
    /**
     * Tests whether the existing order is valid 
     */
    public void testGetExistingOrder() {
        CustomerService cs = new CustomerService();
        Order o = cs.getOrder("223");
        assertNotNull (o);
    }
}
