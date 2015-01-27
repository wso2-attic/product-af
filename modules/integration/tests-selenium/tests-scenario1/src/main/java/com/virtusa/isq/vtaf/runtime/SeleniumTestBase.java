/*
 * Copyright 2004 ThoughtWorks, Inc. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.virtusa.isq.vtaf.runtime;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.ImageTarget;
import org.sikuli.api.ScreenRegion;
import org.sikuli.api.Target;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.google.common.base.Function;
import com.thoughtworks.selenium.SeleneseTestBaseVir;
import com.thoughtworks.selenium.SeleneseTestNgHelperVir;
import com.virtusa.isq.vtaf.objectmap.ObjectLocator;
import com.virtusa.isq.vtaf.objectmap.ObjectMap;
import com.virtusa.isq.vtaf.runtime.DataTable;
import com.virtusa.isq.vtaf.utils.DiffSort;
import com.virtusa.isq.vtaf.utils.ErrorMessageHandler;
import com.virtusa.isq.vtaf.utils.KeyCodes;
import com.virtusa.isq.vtaf.utils.PropertyHandler;

/**
 * The Class SeleniumTestBase.
 */
public class SeleniumTestBase extends SeleneseTestNgHelperVir {

    /** The clip board. */
    private Clipboard clipboard = null;
    
    private static final int RETRY_INTERVAL = 1000;
    
    public JavascriptExecutor jsExecutor;
//
//    
    /**
     * The Enum TableValidationType.
     */
    public static enum TableValidationType {

        /** The colcount. */
        COLCOUNT,
        /** The rowcount. */
        ROWCOUNT,
        /** The tabledata. */
        TABLEDATA,
        /** The relative. */
        RELATIVE,
        /** The tablecell. */
        TABLECELL
    };

    /**
     * The Enum WindowValidationType.
     */
    public static enum WindowValidationType {

        /** The windowpresent. */
        WINDOWPRESENT,
        /** The checktitle. */
        CHECKTITLE
    }

    /** The retry interval. */
    private int retryInterval;

    /** The tables. */
    private HashMap<String, DataTable> tables = null;

    /** The identifire. */
    private String locatorIdentifire = "";
    private static String identifire = "";
    /**
     * The Constructor.
     */
    public SeleniumTestBase() {
        super();
        PropertyHandler propHandler = new PropertyHandler("runtime.properties");
        retryInterval =Integer.parseInt(propHandler
                        .getRuntimeProperty("RETRY_INTERVAL"));
    }

    /**
     * Opens an URL in the test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param objectName
     *            : Logical name of the object
     * @param waitTime
     *            : Time to wait for open command to complete
     */

    public final void open(final String objectName, final String waitTime) {
        open(objectName, "", waitTime);
    }

    /**
     * Open.
     * 
     * @param objectName
     *            the object name
     * @param identifier
     *            the identifier
     * @param waitTime
     *            the wait time
     */
    public final void open(final String objectName, final String identifier,
            final String waitTime) {

        String url = ObjectMap.getResolvedSearchPath(objectName, identifier);
        ObjectLocator locator = new ObjectLocator(url, identifier, url);
        doOpen(locator, waitTime);
    }

    /**
     * Opens an URL in the test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param locator
     *            : url of the openning page
     * @param waitTime
     *            : time to wait till the page is loaded.
     * 
     * */
    private void doOpen(final ObjectLocator locator, final String waitTime) {
        String url = "";
        WebDriver driver = getDriver();
        try {
            url = locator.getActualLocator();
            if ("default".equalsIgnoreCase(url)) {
                PropertyHandler propertyHandler = new PropertyHandler(
                        "runtime.properties");
                url = propertyHandler.getRuntimeProperty("DEFAULT_URL");
                if ("".equals(url)) {
                    throw new WebDriverException("Empty URL : " + url);
                }
            }
            setCommandStartTime(getCurrentTime());
            driver.get(url);

            try {
                driver.manage()
                        .timeouts()
                        .implicitlyWait(Integer.parseInt(waitTime),
                                TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }

            reportresult(true, "OPEN : " + url + "", "PASSED", url);
        } catch (WebDriverException e) {

            String errorString = e.getMessage();
            reportresult(true, "OPEN : " + url + "", "FAILED",
                    "Cannot access the empty URL. URL : " + url
                            + ". Actual Error : " + errorString);
            checkTrue(false, true, "Cannot access the empty URL. URL : " + url
                    + ". Actual Error : " + errorString);
        } catch (Exception e) {
            String errorString = e.getMessage();
            reportresult(true, "OPEN : " + url + "", "FAILED",
                    "Cannot access the URL. URL : " + url + ". Actual Error : "
                            + errorString);
            checkTrue(false, true, "Cannot access the URL. URL : " + url
                    + ". Actual Error : " + errorString);
        }

    }

    /**
     * Check null object.
     * 
     * @param obj
     *            the obj
     * @param command
     *            the command
     * @return the string
     */
    private String checkNullObject(final Object obj, final String command) {
        String value = null;
        try {
            value = obj.toString();
        } catch (NullPointerException e) {
            reportresult(true, command + " command:", "FAILED", command
                    + " command: Invalid input. cannot use null as input");
            checkTrue(false, true,
                    "STORE command: Invalid input. cannot use null as input");
        }
        return value;
    }

    /**
     * Opens an URL in a new test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param url
     *            : Logical name of the object
     * @param identifier
     *            the identifier
     * @param waitTime
     *            the wait time
     */

    public final void navigateToURL(final String url, final String identifier,
            final String waitTime) {

        String actualURL = ObjectMap.getResolvedSearchPath(url, identifier);
        ObjectLocator locator = new ObjectLocator(actualURL, identifier,
                 actualURL);
        doNavigateToURL(locator, waitTime);
    }

    /**
     * Navigate to url.
     * 
     * @param url
     *            the url
     * @param waitTime
     *            the wait time
     */
    public final void navigateToURL(final String url, final String waitTime) {

        navigateToURL(url, "", waitTime);
    }

    /**
     * Opens an URL in a new test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param locator
     *            the locator
     * @param waitTime
     *            the wait time
     */
    private void doNavigateToURL(final ObjectLocator locator,
            final String waitTime) {
        String url = "";
        WebDriver driver = getDriver();
        try {

            url = locator.getActualLocator();
            setCommandStartTime(getCurrentTime());
            if (url.toLowerCase(Locale.getDefault()).startsWith("openwindow=")) {

                Set<String> oldWindowHandles = getAllWindows();
                String actualUrl = url.substring(url.indexOf('=') + 1,
                         url.length());

                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.open('" + actualUrl
                        + "', '_newWindow');");
                super.sleep(Integer.parseInt(waitTime));

                Set<String> newWindowHandles = getAllWindows();
                newWindowHandles.removeAll(oldWindowHandles);
                Object[] newWindowArr = newWindowHandles.toArray();
                driver.switchTo().window(newWindowArr[0].toString());

            } else {
                driver.get(url);
                super.sleep(Integer.parseInt(waitTime));
            }
            reportresult(true, "NAVIGATE TO URL Command :" + url + "",
                    "PASSED", url);
        } catch (Exception e) {
            String errorString = e.getMessage();
            reportresult(true, "NAVIGATE TO URL :" + url + "", "FAILED",
                    "NAVIGATE TO URL command : URL " + url
                            + " failed. Actual Error : " + errorString);
            checkTrue(false, true, "NAVIGATE TO URL command : URL " + url
                    + " failed. Actual Error : " + errorString);
        }

    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * ClickAt is capable of perform clicking on a relative location to the
     * specified element. use locator to specify the respective X,Y coordinates
     * to click
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param identifier
     *            the identifier
     * @param coordinateString
     *            the coordinate string
     */

    public final void clickAt(final String objectName, final String identifier,
            final String coordinateString) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        doClickAt(locator, coordinateString);
    }

    /**
     * Click at.
     * 
     * @param objectName
     *            the object name
     * @param coordinateString
     *            the coordinate string
     */
    public final void clickAt(final String objectName,
            final String coordinateString) {
        clickAt(objectName, "", coordinateString);
    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * ClickAt is capable of perform clicking on a relative location to the
     * specified element. use locator to specify the respective X,Y coordinates
     * to click
     * 
     * @param locator
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param coordinateString
     *            the coordinate string
     */

    private void doClickAt(final ObjectLocator locator,
            final String coordinateString) {
        String objectID = "";
        int counter = getRetryCount();
        int xOffset = 0;
        int yOffset = 0;
        WebDriver driver = getDriver();
        String objectName = locator.getLogicalName();
        try {
            // Retrieve the correct object locator from the object map
            objectID = locator.getActualLocator();

            // first verify whether the element is present in the current web
            // page
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);

            try {
                xOffset =
                        Integer.parseInt((coordinateString.split(",")[0])
                                .trim());
                yOffset =
                        Integer.parseInt((coordinateString.split(",")[1])
                                .trim());
            } catch (Exception e) {

                e.printStackTrace();
                reportresult(true, "CLICKAT :" + objectName + "", "FAILED",
                        "CLICKAT coordinate string (" + coordinateString
                                + ") for :Element (" + objectName + ") ["
                                + objectID + "] is invalid");

                checkTrue(false, true, "CLICKAT coordinate string ("
                        + coordinateString + ") " + "for :Element ("
                        + objectName + ") [" + objectID + "] is invalid");
            }
            /*
             * START DESCRIPTION following while loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    // call for real selenium command
                    /* selenium.clickAt(objectID, coordinateString); */

                    Actions clickAt = new Actions(driver);
                    clickAt.moveToElement(element, xOffset, yOffset).click();
                    clickAt.build().perform();
                    // if not exception is called consider and report the result
                    // as passed
                    reportresult(true, "CLICKAT :" + locator.getLogicalName()
                            + "", "PASSED", "");
                    // if the testcase passed move out from the loop
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {

                    sleep(retryInterval);
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "CLICKAT :" + objectName + "",
                                "FAILED",
                                "CLICKAT command cannot access Element ("
                                        + locator + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "CLICKAT command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {

            e.printStackTrace();
            /*
             * VTAF result reporter call
             */
            reportresult(true, "CLICKAT :" + objectName + "", "FAILED",
                    "CLICKAT command  :Element (" + objectName + ") ["
                            + objectID + "] not present");

            /*
             * VTAF specific validation framework reporting
             */
            checkTrue(false, true, "CLICKAT command  :Element (" + objectName
                    + ") [" + objectID + "] not present");
        }

    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param identifier
     *            the identifier
     */

    public final void click(final String objectName, final String identifier) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        doClick(locator);
    }

    /**
     * Click.
     * 
     * @param objectName
     *            the object name
     */
    public final void click(final String objectName) {
        click(objectName, "");
    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * 
     * @param locator
     *            the locator
     */

    private void doClick(final ObjectLocator locator) {
        String objectID = "";
        int counter = getRetryCount();
        WebDriver driver = getDriver();
        try {
            // Retrieve the correct object locator from the object map
            objectID = locator.getActualLocator();
            // first verify whether the element is present in the current web
            // page
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following while loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exitted from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    // call for real selenium command
                    /* selenium.click(objectID); */

                    element.click();
                    // if not exception is called consider and report the result
                    // as passed
                    reportresult(true, "CLICK :" + locator.getLogicalName()
                            + "", "PASSED", "");
                    // if the test case passed move out from the loop
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (ElementNotVisibleException ex) {
                    try {
                        JavascriptExecutor jsExecutor =
                                (JavascriptExecutor) driver;
                        jsExecutor.executeScript("arguments[0].click();",
                                element);
                        reportresult(true, "CLICK :" + locator.getLogicalName()
                                + "", "PASSED", "");
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                } catch (Exception e) {

                    sleep(retryInterval);
                    String objectName = locator.getLogicalName();
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "CLICK :" + objectName + "",
                                "FAILED",
                                "CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {

            e.printStackTrace();
            String objectName = locator.getLogicalName();
            /*
             * VTAF result reporter call
             */
            reportresult(true, "CLICK :" + objectName + "", "FAILED",
                    "CLICK command  :Element (" + objectName + ") [" + objectID
                            + "] not present");

            /*
             * VTAF specific validation framework reporting
             */
            checkTrue(false, true, "CLICK command  :Element (" + objectName
                    + ") [" + objectID + "] not present");
        }

    }

    /**
     * Stores a value in a given element property and return it as a double
     * value.
     * 
     * @param objectName
     *            logical name of the object
     * @param identifier
     *            the identifier
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an element TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/> *
     * @return the integer property
     */

    public final double getDoubleProperty(final String objectName,
            final String identifier, final String component) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        return doGetDoubleProperty(locator, component);
    }

    /**
     * Gets the double property.
     * 
     * @param objectName
     *            the object name
     * @param component
     *            the component
     * @return the integer property
     */
    public final double getDoubleProperty(final String objectName,
            final String component) {
        return getDoubleProperty(objectName, "", component);
    }

    /**
     * Stores a value in a given element property and return it as a double
     * value.
     * 
     * @param locator
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/>.
     * @return the int
     */

    private double doGetDoubleProperty(final ObjectLocator locator,
            final String component) {
        String returnValue = "";
        // retrieve the actual object ID from object repository
        String objectID = locator.getActualLocator();

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            if (component.startsWith("TEXT:")) {

                returnValue =
                        getVarPropertyTextValue(locator, element, component);

            } else if (component.startsWith("VALUE:")) {

                returnValue = element.getAttribute("value");
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "",
                        "PASSED", "Object value = " + returnValue);
            } else if (component.startsWith("ATTR:")) {

                returnValue =
                        getVarPropertyAttributeValue(locator, element,
                                component);
            }

        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            if (e.getMessage().startsWith("Attribute")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setVarProperty() :Atrribute (" + component
                                + ")of  [" + locator + "] not present");
                checkTrue(false, true, " command setVarProperty() :Atrribute ("
                        + component + ")of  [" + locator + "] not present");
            } else if (e.getMessage().startsWith("Element")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setVarProperty()  :Element (" + locator
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, " command setVarProperty()  :Element ("
                        + locator + ") [" + objectID + "] not present");
            }
        }
        double returnval = -1;
        try {
            returnval = Double.parseDouble(returnValue);
        } catch (Exception e) {
            reportresult(true, "SET VARIABLE PROPERTY :" + locator, "FAILED",
                    " command setVarProperty() :input value mismatch with double , "
                            + "user input:" + returnValue);

            checkTrue(false, true,
                    " command setVarProperty() :input value mismatch with double ("
                            + "[" + objectID + "user input:" + returnValue);
        }

        return returnval;
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value.
     * 
     * @param objectName
     *            logical name of the object
     * @param identifier
     *            the identifier
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/> *
     * @return the string property
     */

    public final String getStringProperty(final String objectName,
            final String identifier, final String component) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        return doGetStringProperty(locator, component);
    }

    /**
     * Gets the string property.
     * 
     * @param objectName
     *            the object name
     * @param component
     *            the component
     * @return the string property
     */
    public final String getStringProperty(final String objectName,
            final String component) {
        return getStringProperty(objectName, "", component);
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value.
     * 
     * @param locator
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/> *
     * @return the string
     */

    private String doGetStringProperty(final ObjectLocator locator,
            final String component) {

        String returnValue = "";
        // retrieve the actual object ID from object repository
        String objectID = locator.getActualLocator();

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            if (component.startsWith("TEXT:")) {

                returnValue =
                        getVarPropertyTextValue(locator, element, component);

            } else if (component.startsWith("VALUE:")) {

                returnValue = element.getAttribute("value");

                reportresult(true,
                        "SET VARIABLE PROPERTY :" + locator.getLogicalName()
                                + "", "PASSED", "Object value = " + returnValue);
            } else if (component.startsWith("ATTR:")) {

                returnValue =
                        getVarPropertyAttributeValue(locator, element,
                                component);
            }

        } catch (Exception e) {
            /*
             * after the retry amount, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            if (e.getMessage().startsWith("Attribute")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setvarProperty()  :Atrribute (" + component
                                + ")of  [" + locator + "] not present");
                checkTrue(false, true,
                        " command setvarProperty()  :Atrribute (" + component
                                + ")of  [" + locator + "] not present");
            } else if (e.getMessage().startsWith("Element")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setvarProperty()  :Element (" + locator
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, " command setvarProperty()  :Element ("
                        + locator + ") [" + objectID + "] not present");
            }
        }
        return returnValue;
    }

    /**
     * Gets the var property attribute value.
     * 
     * @param locator
     *            the locator
     * @param webElement
     *            the web element
     * @param component
     *            the component
     * @return the var property attribute value
     * @throws Exception
     *             the exception
     */
    private String getVarPropertyAttributeValue(final ObjectLocator locator,
            final WebElement webElement, final String component)
            throws Exception {
        int counter = getRetryCount();
        WebElement element = webElement;
        String returnValue = null;
        final int compSubstrIndex = 5;
        /*
         * START DESCRIPTION following for loop was added to make the command
         * more consistent try the command for give amount of time (can be
         * configured through class variable RETRY) command will be tried for
         * "RETRY" amount of times or until command works. any exception thrown
         * within the tries will be handled internally.
         * 
         * can be exited from the loop under 2 conditions 1. if the command
         * succeeded 2. if the RETRY count is exceeded
         */
        while (counter > 0) {
            try {
                counter--;

                returnValue =
                        validateObjectProperty(element,
                                component.substring(compSubstrIndex));
                reportresult(true,
                        "SET VARIABLE PROPERTY :" + locator.getLogicalName()
                                + "." + component.substring(compSubstrIndex),
                        "PASSED", "Object value = " + returnValue);
                break;
            } catch (StaleElementReferenceException staleElementException) {

                element = checkElementPresence(locator.getActualLocator());
            } catch (Exception e) {
                sleep(retryInterval);
                /*
                 * after the retry amout, if still the object is not found,
                 * report the failure error will be based on the exception
                 * message, if e contains attribute report attribute failure
                 * else if e contains element, report object not found
                 */
                if (!(counter > 0)) {
                    if (e.getMessage().startsWith("Attribute")) {
                        reportresult(true, "SET VARIABLE PROPERTY :" + locator
                                + "." + component.substring(compSubstrIndex),
                                "FAILED",
                                " command setvarProperty()  :Atrribute ("
                                        + component.substring(compSubstrIndex)
                                        + ")of  [" + locator.getLogicalName()
                                        + "] not present");
                        checkTrue(false, true,
                                " command setvarProperty()  :Atrribute ("
                                        + component.substring(compSubstrIndex)
                                        + ")of  [" + locator + "] not present");
                    }

                } else if (e.getMessage().startsWith("Element")) {
                    String logicalName = locator.getLogicalName();
                    String actualLocator = locator.getActualLocator();
                    reportresult(true, "SET VARIABLE PROPERTY :" + logicalName
                            + "." + component.substring(compSubstrIndex),
                            "FAILED", " command setvarProperty()  :Element ("
                                    + logicalName + ") [" + actualLocator
                                    + "] not present");
                    checkTrue(false, true,
                            " command setvarProperty()  :Element ("
                                    + logicalName + ") [" + actualLocator
                                    + "] not present");
                }
            }
        }
        /*
         * END DESCRIPTION
         */
        return returnValue;
    }

    /**
     * Gets the var property text value.
     * 
     * @param locator
     *            the locator
     * @param element
     *            the element
     * @param component
     *            the component
     * @return the var property text value
     */
    private String getVarPropertyTextValue(final ObjectLocator locator,
            final WebElement element, final String component) {
        String returnValue;
        final int componentAttributes = 3;
        if (component.split(":").length == componentAttributes) {
            returnValue = element.getText();
            if (component.split(":")[1].contains("-")) {
                returnValue =
                        returnValue.substring(Integer.parseInt(component
                                .split(":")[1].split("-")[0]),
                                Integer.parseInt(component.split(":")[1]
                                        .split("-")[1]));
                reportresult(true,
                        "SET VARIABLE PROPERTY :" + locator.getLogicalName()
                                + "", "PASSED", "Object value = " + returnValue);
            } else {

                returnValue =
                        returnValue.substring(Integer.parseInt(component
                                .split(":")[1]));
                reportresult(true,
                        "SET VARIABLE PROPERTY :" + locator.getLogicalName()
                                + "", "PASSED", "Object value = " + returnValue);
            }
        } else {
            returnValue = element.getText();

            reportresult(true,
                    "SET VARIABLE PROPERTY :" + locator.getLogicalName() + "",
                    "PASSED", "Object value = " + returnValue);
        }
        return returnValue;
    }

    /**
     * Fail.
     * 
     * @param message
     *            the message
     */
    public final void fail(final Object message) {
        reportresult(true, "Fail Command : ", "FAILED", " command Fail("
                + message + ")");
        SeleneseTestBaseVir.failure(message);
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value.
     * 
     * @param objectName
     *            logical name of the object
     * @param identifier
     *            the identifier
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an element TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/> *
     * @return the integer property
     */

    public final int getIntegerProperty(final String objectName,
            final String identifier, final String component) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        return doGetIntegerProperty(locator, component);
    }

    /**
     * Gets the integer property.
     * 
     * @param objectName
     *            the object name
     * @param component
     *            the component
     * @return the integer property
     */
    public final int getIntegerProperty(final String objectName,
            final String component) {
        return getIntegerProperty(objectName, "", component);
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value.
     * 
     * @param locator
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/>.
     * @return the int
     */

    private int doGetIntegerProperty(final ObjectLocator locator,
            final String component) {
        String returnValue = "";
        // retrieve the actual object ID from object repository
        String objectID = locator.getActualLocator();

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            if (component.startsWith("TEXT:")) {

                returnValue =
                        getVarPropertyTextValue(locator, element, component);

            } else if (component.startsWith("VALUE:")) {

                returnValue = element.getAttribute("value");
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "",
                        "PASSED", "Object value = " + returnValue);
            } else if (component.startsWith("ATTR:")) {

                returnValue =
                        getVarPropertyAttributeValue(locator, element,
                                component);
            }

        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            if (e.getMessage().startsWith("Attribute")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setVarProperty() :Atrribute (" + component
                                + ")of  [" + locator + "] not present");
                checkTrue(false, true, " command setVarProperty() :Atrribute ("
                        + component + ")of  [" + locator + "] not present");
            } else if (e.getMessage().startsWith("Element")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setVarProperty()  :Element (" + locator
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, " command setVarProperty()  :Element ("
                        + locator + ") [" + objectID + "] not present");
            }
        }
        int returnval = 0;
        try {
            returnval = Integer.parseInt(returnValue);
        } catch (Exception e) {
            reportresult(true, "SET VARIABLE PROPERTY :" + locator, "FAILED",
                    " command setVarProperty() :input value mismatch with int, "
                            + "user input:" + returnValue);

            checkTrue(false, true,
                    " command setVarProperty() :input value mismatch with int ("
                            + "[" + objectID + "user input:" + returnValue);
        }

        return returnval;
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value.
     * 
     * @param objectName
     *            logical name of the object
     * @param identifier
     *            the identifier
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/> *
     * @return the boolean property
     */

    public final boolean getBooleanProperty(final String objectName,
            final String identifier, final String component) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        return doGetBooleanProperty(locator, component);
    }

    /**
     * Gets the boolean property.
     * 
     * @param objectName
     *            the object name
     * @param component
     *            the component
     * @return the boolean property
     */
    public final boolean getBooleanProperty(final String objectName,
            final String component) {
        return getBooleanProperty(objectName, "", component);
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value.
     * 
     * @param locator
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute/> *
     * @return true, if do get boolean property
     */

    private boolean doGetBooleanProperty(final ObjectLocator locator,
            final String component) {
        String returnValue = "";
        // retrieve the actual object ID from object repository
        String objectID = locator.getActualLocator();

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            if (component.startsWith("TEXT:")) {

                returnValue =
                        getVarPropertyTextValue(locator, element, component);

            } else if (component.startsWith("VALUE:")) {
                returnValue = element.getAttribute("value");

            } else if (component.startsWith("ATTR:")) {

                returnValue =
                        getVarPropertyAttributeValue(locator, element,
                                component);
            }

        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */

            if (e.getMessage().startsWith("Attribute")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setVarProperty() :Atrribute (" + component
                                + ")of  [" + locator + "] not present");
                checkTrue(false, true, " command setVarProperty() :Atrribute ("
                        + component + ")of  [" + locator + "] not present");

            } else if (e.getMessage().startsWith("Element")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + locator + "."
                        + component, "FAILED",
                        " command setVarProperty() :Element (" + locator
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, " command setVarProperty() :Element ("
                        + locator + ") [" + objectID + "] not present");
            }
        }

        if ("true".equalsIgnoreCase(returnValue)
                || "false".equalsIgnoreCase(returnValue)) {
            reportresult(true, "SET VARIABLE PROPERTY :" + locator + "",
                    "PASSED", "Object value = " + returnValue);

        } else {
            reportresult(true, "CHECK VARIABLE PROPERTY :" + locator, "FAILED",
                    " command setVarProperty() :input value mismatch with boolean, "
                            + "user input:" + returnValue);

            checkTrue(false, true,
                    " command setVarProperty() :input value mismatch with boolean ("
                            + "[" + objectID + "user input:" + returnValue);
        }
        return Boolean.parseBoolean(returnValue);
    }

    /**
     * Arguments: <br>
     * <br>
     * 
     * windowID - the JavaScript window ID of the window to select<br>
     * 
     * Selects a popup window using a window locator; once a popup window has
     * been selected, all commands go to that window. To select the main window
     * again, use null as the target.<br>
     * <br>
     * 
     * Window locators provide different ways of specifying the window object:
     * by title, by internal JavaScript "name," or by JavaScript variable.<br>
     * <br>
     * 
     * title=My Special Window: Finds the window using the text that appears in
     * the title bar. Be careful; two windows can share the same title. If that
     * happens, this locator will just pick one.<br>
     * name=myWindow: Finds the window using its internal JavaScript "name"
     * property. This is the second parameter "windowName" passed to the
     * JavaScript method window.open(url, windowName, windowFeatures,
     * replaceFlag) (which Selenium intercepts).<br>
     * var=variableName: Some pop-up windows are unnamed (anonymous), but are
     * associated with a JavaScript variable name in the current application
     * window, e.g. "window.foo = window.open(url);". In those cases, you can
     * open the window using "var=foo".<br>
     * <br>
     * 
     * If no window locator prefix is provided, we'll try to guess what you mean
     * like this:<br>
     * <br>
     * 
     * 1.) if windowID is null, (or the string "null") then it is assumed the
     * user is referring to the original window instantiated by the browser).<br>
     * <br>
     * 
     * 2.) if the value of the "windowID" parameter is a JavaScript variable
     * name in the current application window, then it is assumed that this
     * variable contains the return value from a call to the JavaScript
     * window.open() method.<br>
     * <br>
     * 
     * 3.) Otherwise, selenium looks in a hash it maintains that maps string
     * names to window "names".<br>
     * <br>
     * 
     * 4.) If that fails, we'll try looping over all of the known windows to try
     * to find the appropriate "title". Since "title" is not necessarily unique,
     * this may have unexpected behavior.<br>
     * <br>
     * 
     * If you're having trouble figuring out the name of a window that you want
     * to manipulate, look at the Selenium log messages which identify the names
     * of windows created via window.open (and therefore intercepted by
     * Selenium). You will see messages like the following for each window as it
     * is opened:<br>
     * <br>
     * 
     * debug: window.open call intercepted; window ID (which you can use with
     * selectWindow()) is "myNewWindow"<br>
     * <br>
     * 
     * In some cases, Selenium will be unable to intercept a call to window.open
     * (if the call occurs during or before the "onLoad" event, for example).
     * (This is bug SEL-339.) In those cases, you can force Selenium to notice
     * the open window's name by using the Selenium openWindow command, using an
     * empty (blank) url, like this: openWindow("", "myFunnyWindow").<br>
     * <br>
     * 
     * @param windowName
     *            : Logical name of the window assigned by the test scriptor
     * @param identifier
     *            the identifier
     */

    public final void selectWindow(final String windowName,
            final String identifier) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(windowName, identifier);
        ObjectLocator locator =
                new ObjectLocator(windowName, identifier, actualLocator);
        doSelectWindow(locator);
    }

    /**
     * Select window.
     * 
     * @param windowName
     *            the window name
     */
    public final void selectWindow(final String windowName) {
        selectWindow(windowName, "");
    }

    /**
     * Arguments: <br>
     * <br>
     * 
     * windowID - the JavaScript window ID of the window to select<br>
     * 
     * Selects a popup window using a window locator; once a popup window has
     * been selected, all commands go to that window. To select the main window
     * again, use null as the target.<br>
     * <br>
     * 
     * Window locators provide different ways of specifying the window object:
     * by title, by internal JavaScript "name," or by JavaScript variable.<br>
     * <br>
     * 
     * title=My Special Window: Finds the window using the text that appears in
     * the title bar. Be careful; two windows can share the same title. If that
     * happens, this locator will just pick one.<br>
     * name=myWindow: Finds the window using its internal JavaScript "name"
     * property. This is the second parameter "windowName" passed to the
     * JavaScript method window.open(url, windowName, windowFeatures,
     * replaceFlag) (which Selenium intercepts).<br>
     * var=variableName: Some pop-up windows are unnamed (anonymous), but are
     * associated with a JavaScript variable name in the current application
     * window, e.g. "window.foo = window.open(url);". In those cases, you can
     * open the window using "var=foo".<br>
     * <br>
     * 
     * If no window locator prefix is provided, we'll try to guess what you mean
     * like this:<br>
     * <br>
     * 
     * 1.) if windowID is null, (or the string "null") then it is assumed the
     * user is referring to the original window instantiated by the browser).<br>
     * <br>
     * 
     * 2.) if the value of the "windowID" parameter is a JavaScript variable
     * name in the current application window, then it is assumed that this
     * variable contains the return value from a call to the JavaScript
     * window.open() method.<br>
     * <br>
     * 
     * 3.) Otherwise, selenium looks in a hash it maintains that maps string
     * names to window "names".<br>
     * <br>
     * 
     * 4.) If that fails, we'll try looping over all of the known windows to try
     * to find the appropriate "title". Since "title" is not necessarily unique,
     * this may have unexpected behavior.<br>
     * <br>
     * 
     * If you're having trouble figuring out the name of a window that you want
     * to manipulate, look at the Selenium log messages which identify the names
     * of windows created via window.open (and therefore intercepted by
     * Selenium). You will see messages like the following for each window as it
     * is opened:<br>
     * <br>
     * 
     * debug: window.open call intercepted; window ID (which you can use with
     * selectWindow()) is "myNewWindow"<br>
     * <br>
     * 
     * In some cases, Selenium will be unable to intercept a call to window.open
     * (if the call occurs during or before the "onLoad" event, for example).
     * (This is bug SEL-339.) In those cases, you can force Selenium to notice
     * the open window's name by using the Selenium openWindow command, using an
     * empty (blank) url, like this: openWindow("", "myFunnyWindow").<br>
     * <br>
     * 
     * @param locator
     *            : Logical name of the window assigned by the test scriptor
     * 
     * 
     * */
    private void doSelectWindow(final ObjectLocator locator) {
        int counter = getRetryCount();
        String targetWindow = null;
        WebDriver driver = getDriver();

        // Getting the actual object identification from the object map
        String window = locator.getActualLocator();
        try {
            checkForNewWindowPopups();

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    targetWindow =
                            getMatchingWindowFromCurrentWindowHandles(driver,
                                    window);

                    if (targetWindow != null) {

                        driver.switchTo().window(targetWindow);

                        driver.manage().window().maximize();
                        focusToCurrentWindow(driver);

                        reportresult(true,
                                "SELECT WINDOW :" + locator.getLogicalName()
                                        + "", "PASSED", "");
                        break;
                    } else {
                        throw new WebDriverException("Window Not Found"
                                + window);
                    }
                } catch (WebDriverException ex) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {
                        String errorString = ex.getMessage();
                        String objectName = locator.getLogicalName();
                        reportresult(
                                true,
                                "SELECT WINDOW :" + objectName + "",
                                "FAILED",
                                "selectWindow command  :Element ("
                                        + objectName
                                        + ") ["
                                        + window
                                        + "] is not accessible. Actual Error : "
                                        + errorString);
                        checkTrue(
                                false,
                                true,
                                "selectWindow command  :Element ("
                                        + objectName
                                        + ") ["
                                        + window
                                        + "] is not accessible. Actual Error : "
                                        + errorString);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            String objectName = locator.getLogicalName();
            // if any exception is raised, report failure
            reportresult(true, "SELECT WINDOW :" + objectName + "", "FAILED",
                    "selectWindow command  :Element (" + objectName + ") ["
                            + window + "] not present");
            checkTrue(false, true, "selectWindow command  :Element ("
                    + objectName + ") [" + window + "] not present");

        }

    }

	private void focusToCurrentWindow(WebDriver driver){
    	
    	 try {
    		 JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
			 if(driver.getPageSource().toLowerCase(Locale.getDefault()).contains("<html")) {
				 jsExecutor.executeScript("window.focus();");
			 }
		} catch (Exception e) {
			getLog().error(e);
			
		}
    }

	
    /**
     * Gets the matching window from current window handles.
     * 
     * @param driver
     *            the driver
     * @param inputWindowName
     *            the input window name
     * @return the matching window from current window handles
     * @throws Exception
     *             the exception
     */
    private String getMatchingWindowFromCurrentWindowHandles(
            final WebDriver driver, final String inputWindowName)
            throws Exception {
        String targetWindow = null;
        Set<String> windowarr = getAllWindows();
        if (inputWindowName.startsWith("index=")) {
            int winIndex =
                    Integer.parseInt(inputWindowName.substring(
                            inputWindowName.indexOf('=') + 1,
                            inputWindowName.length()));
            targetWindow = getOpenWindowHandleIndex().get(winIndex);

        } else {
            boolean objectFound = false;
            for (String windowname : windowarr) {

                if (inputWindowName.startsWith("regexp:")
                        || inputWindowName.startsWith("glob:")) {

                    objectFound =
                            isMatchingPattern(inputWindowName.substring(
                                    inputWindowName.indexOf(':') + 1,
                                    inputWindowName.length()), driver
                                    .switchTo().window(windowname).getTitle());

                } else if (driver.switchTo().window(windowname).getTitle()
                        .equals(inputWindowName)) {
                    objectFound = true;
                }
                if (objectFound) {
                    targetWindow = windowname;
                    break;
                }
            }
        }
        return targetWindow;
    }

    /**
     * Checks if is matching pattern.
     * 
     * @param patternString
     *            the pattern string
     * @param matcherString
     *            the matcher string
     * @return true, if checks if is matching pattern
     */
    private boolean isMatchingPattern(final String patternString,
            final String matcherString) {

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(matcherString);
        return matcher.matches();
    }

    /**
     * Sets the value of an input field, as though you typed it in.<br>
     * Can also be used to set the value of comboboxes, check boxes, etc. In
     * these cases, value should be the value of the option selected, not the
     * visible text.<br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param identifier
     *            the identifier
     * @param value
     *            : value to be typed in the object
     */

    public final void type(final String objectName, final String identifier,
            final Object value) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        doType(locator, value);
    }

    /**
     * Type.
     * 
     * @param objectName
     *            the object name
     * @param value
     *            the value
     */
    public final void type(final String objectName, final Object value) {
        type(objectName, "", value);
    }

    /**
     * Sets the value of an input field, as though you typed it in.<br>
     * Can also be used to set the value of comboboxes, check boxes, etc. In
     * these cases, value should be the value of the option selected, not the
     * visible text.<br>
     * <br>
     * 
     * @param locator
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param objValue
     *            the obj value
     */
    public final void doType(final ObjectLocator locator, final Object objValue) {
        String value = checkNullObject(objValue, "TYPE");

        int counter = getRetryCount();

        // Getting the actual object identification from the object map

        String objectID = locator.getActualLocator();
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            try {
                element.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (counter > 0) {
                try {
                    counter--;
                    // Calling the actual command

                    element.sendKeys(value);

                    reportresult(true,
                            "TYPE :" + locator.getLogicalName() + "", "PASSED",
                            " [Input value = " + value + "]");
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        String objectName = locator.getLogicalName();
                        reportresult(true, "TYPE :" + objectName + "",
                                "FAILED",
                                "TYPE command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]"
                                        + " [Input value = " + value + "]");
                        checkTrue(false, true,
                                "TYPE command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]"
                                        + " [Input value = " + value + "]");
                    }
                }

            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            // if any exception was raised, report a test failure
            e.printStackTrace();
            String objectName = locator.getLogicalName();
            reportresult(true, "TYPE :" + objectName + "", "FAILED",
                    "TYPE command  :Element (" + objectName + ") [" + objectID
                            + "] [Input value = " + value + "] not present");
            checkTrue(false, true, "TYPE command  :Element (" + objectName
                    + ") [" + objectID + "] [Input value = " + value
                    + "] not present");

        }

    }

    /**
     * Select an option\options from a drop-down using an option locator. <br>
     * 
     * Option locators provide different ways of specifying options of an HTML
     * Select element (e.g. for selecting a specific option, or for asserting
     * that the selected option satisfies a specification). There are several
     * forms of Select Option Locator.<br>
     * <br>
     * 
     * <b>label=labelPattern:</b> matches options based on their labels, i.e.
     * the visible text. (This is the default.) label=regexp:^[Oo]ther<br>
     * <b>value=valuePattern:</b> matches options based on their values.
     * value=other<br>
     * <b>id=id:</b> matches options based on their ids. id=option1<br>
     * <b>index=index:</b> matches an option based on its index (offset from
     * zero). index=2<br>
     * <br>
     * 
     * If no option locator prefix is provided, the default behaviour is to
     * match on label. <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter <br>
     * <br>
     * @param identifier
     *            the identifier
     * @param objValue
     *            the obj value
     */

    public final void select(final String objectName, final String identifier,
            final Object objValue) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        doSelect(locator, objValue);
    }

    /**
     * Select.
     * 
     * @param objectName
     *            the object name
     * @param objValue
     *            the obj value
     */
    public final void select(final String objectName, final Object objValue) {
        select(objectName, "", objValue);
    }

    /**
     * Select an option\options from a drop-down using an option locator. <br>
     * 
     * Option locators provide different ways of specifying options of an HTML
     * Select element (e.g. for selecting a specific option, or for asserting
     * that the selected option satisfies a specification). There are several
     * forms of Select Option Locator.<br>
     * <br>
     * 
     * <b>label=labelPattern:</b> matches options based on their labels, i.e.
     * the visible text. (This is the default.) label=regexp:^[Oo]ther<br>
     * <b>value=valuePattern:</b> matches options based on their values.
     * value=other<br>
     * <b>id=id:</b> matches options based on their ids. id=option1<br>
     * <b>index=index:</b> matches an option based on its index (offset from
     * zero). index=2<br>
     * <br>
     * 
     * If no option locator prefix is provided, the default behaviour is to
     * match on label. <br>
     * <br>
     * 
     * @param locator
     *            : Logical name of the web element assigned by the automation
     *            scripter <br>
     * <br>
     * @param objValue
     *            the obj value
     */
    private void doSelect(final ObjectLocator locator, final Object objValue) {

        String value = checkNullObject(objValue, "SELECT");
        int counter = getRetryCount();
        String[] actualOptions = {};
        boolean multiSelect = false;
        String objectName = locator.getLogicalName();

        String objectID = locator.getActualLocator();
        try {
            // Checking whether the list box is available
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            // Checking whether the list option is available
            Select selectElement = new Select(element);

            List<WebElement> actualElementOptions = selectElement.getOptions();

            actualOptions = new String[actualElementOptions.size()];
            for (int i = 0; i < actualElementOptions.size(); i++) {
                actualOptions[i] = actualElementOptions.get(i).getText();
            }
            multiSelect = checkSelectOptions(value, actualOptions);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;

                    if (!multiSelect) {

                        selectSingleOptionFromActualDropdown(selectElement,
                                actualOptions, value);

                        if (!checkSelectedOptionValue(objectID, value)) {
                            continue;
                        }

                        reportresult(true, "SELECT :" + objectName + "",
                                "PASSED", "");
                        break;
                    } else {

                        selectMultipleOptionFromActualDropDown(selectElement,
                                value);
                        reportresult(true, "SELECT :" + objectName + "",
                                "PASSED", "");
                    }

                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception ex) {
                    sleep(retryInterval);

                    if (!(counter > 0)) {
                        ex.printStackTrace();

                        reportresult(true, "SELECT :" + objectName + "",
                                "FAILED",
                                "SELECT command cannot access :Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "SELECT command cannot access  :Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // waiting for the maximum amount of waiting time before failing the
            // test case
            // Several checks were introduced to narrow down to the failure to
            // the exact cause.
            ErrorMessageHandler messages = new ErrorMessageHandler();
            String error =
                    messages.getSelectCommandErrorMessages(e.getMessage());
            String formattedError =
                    error.replaceAll("<locator>", objectName)
                            .replaceAll("<objectID>", objectID)
                            .replaceAll("<inputValue>", objValue.toString())
                            .replaceAll("<actualOptions>",
                                    Arrays.asList(actualOptions).toString());

            e.printStackTrace();
            reportresult(true, "SELECT :" + objectName + "", "FAILED",
                    formattedError);
            checkTrue(false, true, formattedError);
        }
    }

    /**
     * Check selected option value.
     * 
     * @param objectID
     *            the object id
     * @param expectedSelectedValue
     *            the expected selected value
     * @return true, if check selected option value
     */
    private boolean checkSelectedOptionValue(final String objectID,
            final String expectedSelectedValue) {

        try {
            WebElement element = checkElementPresence(objectID);
            Select selectElement = new Select(element);
            String selectedValue =
                    selectElement.getFirstSelectedOption().getText();

            if (expectedSelectedValue.startsWith("regexp:")) {

                Pattern pattern =
                        Pattern.compile(expectedSelectedValue.substring(
                                expectedSelectedValue.indexOf(':') + 1,
                                expectedSelectedValue.length()));
                Matcher matcher = pattern.matcher(selectedValue);
                return matcher.matches();

            } else if (expectedSelectedValue.startsWith("index=")) {

                int indexNo =
                        Integer.parseInt(expectedSelectedValue.replace(
                                "index=", ""));
                List<WebElement> actualOptions = selectElement.getOptions();
                List<String> stringOptions = new ArrayList<String>();
                for (WebElement actualOption : actualOptions) {
                    stringOptions.add(actualOption.getText());
                }
                return selectedValue.equals(stringOptions.get(indexNo));

            } else {

                return selectedValue.equals(expectedSelectedValue);
            }

        } catch (WebDriverException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Select multiple option from actual drop down.
     * 
     * @param selectElement
     *            the select element
     * @param value
     *            the value
     */
    private void selectMultipleOptionFromActualDropDown(
            final Select selectElement, final String value) {

        String[] options = value.split("#");
        for (String option : options) {
            if (option.startsWith("index=")) {

                int indexNo = Integer.parseInt(option.replace("index=", ""));
                selectElement.selectByIndex(indexNo);

            } else {
                selectElement.selectByVisibleText(option);
            }
            break;
        }
    }

    /**
     * Select single option from actual dropdown.
     * 
     * @param selectElement
     *            the select element
     * @param actualOptions
     *            the actual options
     * @param value
     *            the value
     * @return the string
     */
    private String selectSingleOptionFromActualDropdown(
            final Select selectElement, final String[] actualOptions,
            final String value) {
        String selectedValue = "";
        if (value.startsWith("regexp:")) {

            Pattern pattern =
                    Pattern.compile(value.substring(value.indexOf(':') + 1,
                            value.length()));
            for (String actualOption : actualOptions) {
                Matcher matcher = pattern.matcher(actualOption);
                if (matcher.matches()) {
                    selectedValue = actualOption;
                    selectElement.selectByVisibleText(actualOption);
                    break;
                }
            }

        } else if (value.startsWith("index=")) {

            int indexNo = Integer.parseInt(value.replace("index=", ""));

            selectElement.selectByIndex(indexNo);

        } else {
            selectedValue = value;
            selectElement.selectByVisibleText(value);
        }
        return selectedValue;
    }

    /**
     * Sleeps for the specified number of milliseconds.
     * 
     * @param waitingTime
     *            the waiting time
     */
    public final void pause(final String waitingTime) {
        int waitingMilliSeconds = Integer.parseInt(waitingTime);
        super.sleep(waitingMilliSeconds);
        reportresult(true, "PAUSE Command: (" + waitingMilliSeconds + " ms)",
                "PASSED", "Pausing for " + waitingTime + " Milliseconds.");

    }

    /**
     * Double clicks on a link, button, checkbox or radio button. If the double
     * click action causes a new page to load <br>
     * (like a link usually does), call waitForPageToLoad. <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the object to be doubleclicked.
     * @param identifier
     *            the identifier
     */

    public final void doubleClick(final String objectName,
            final String identifier) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        doDoubleClick(locator);
    }

    /**
     * Double click.
     * 
     * @param objectName
     *            the object name
     */
    public final void doubleClick(final String objectName) {
        doubleClick(objectName, "");
    }

    /**
     * Double clicks on a link, button, checkbox or radio button. If the double
     * click action causes a new page to load <br>
     * (like a link usually does), call waitForPageToLoad. <br>
     * <br>
     * 
     * @param locator
     *            the locator
     */

    private void doDoubleClick(final ObjectLocator locator) {
        // Retrieve the actual object name from the object repository

        int counter = getRetryCount();
        WebDriver driver = getDriver();
        String objectName = locator.getLogicalName();
        String objectID = locator.getActualLocator();
        try {
            // First chacking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;
                    element.click();
                    Actions dClick = new Actions(driver);
                    dClick.moveToElement(element).doubleClick();
                    dClick.build().perform();
                    /* selenium.doubleClick(objectID); */
                    reportresult(true, "DOUBLE CLICK :" + objectName + "",
                            "PASSED", "");
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {

                    sleep(retryInterval);
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "DOUBLE CLICK :" + objectName + "",
                                "FAILED",
                                "DOUBLE CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "DOUBLE CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // if object not found exception is raised fail the test cases
            e.printStackTrace();
            reportresult(true, "DOUBLE CLICK :" + objectName + "", "FAILED",
                    "DOUBLE CLICK command  :Element (" + objectName + ") ["
                            + objectID + "] not present");
            checkTrue(false, true, "DOUBLE CLICK command  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    /**
     * check(locator) <br>
     * Arguments:<br>
     * locator - an element locator<br>
     * Check a toggle-button (checkbox/radio)<br>
     * <br>
     * .
     * 
     * @param objectName
     *            : logical name of the object assigned by the user
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * @param isSelect
     *            : specify whether to check or uncheck the button
     */
    public final void check(final String objectName, final String identifire,
            final boolean isSelect) {
        this.locatorIdentifire = identifire;
        check(objectName, isSelect);
        this.locatorIdentifire = "";
    }

    /**
     * check(locator) <br>
     * Arguments:<br>
     * locator - an element locator<br>
     * Check a toggle-button (checkbox/radio)<br>
     * <br>
     * .
     * 
     * @param objectName
     *            : logical name of the object assigned by the user
     * @param isSelect
     *            : specify whether to check or uncheck the button
     */
    public final void check(final String objectName, final boolean isSelect) {
        int counter = getRetryCount();
        String option = "";
        // Getting the actual object identification from the object map
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    if (isSelect) {
                        option = "Select";
                        // Calling the actual command
                        if (!element.isSelected()) {
                            element.click();
                        }
                        /* selenium.check(objectID); */
                        reportresult(true,
                                "CHECK (Select) :" + objectName + "", "PASSED",
                                "");
                    } else {
                        option = "DeSelect";
                        if (element.isSelected()) {
                            element.click();
                        }
                        /* selenium.uncheck(objectID); */
                        reportresult(true, "CHECK (DeSelect) :" + objectName
                                + "", "PASSED", "");
                    }

                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);

                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "CHECK : (" + option + ")"
                                + objectName + "", "FAILED",
                                "CHECK command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]");
                        checkTrue(false, true,
                                "CHECK command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]");
                    }
                }

            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            // if any exception was raised, report a test failure
            e.printStackTrace();
            reportresult(true, "CHECK (" + option + "):" + objectName + "",
                    "FAILED", "CHECK (" + option + ") (" + objectName + ") ["
                            + objectID + "] element not present");
            checkTrue(false, true, "CHECK (" + option + ") (" + objectName
                    + ") [" + objectID + "] element not present");

        }

    }

    /**
     * Doubleclicks on a link, button, checkbox or radio button. If the action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad.<br>
     * The main differentiator of <b> DoubleClickAt </b> is, user can make the
     * script click on a relative location to the element <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the object provided by the user
     * @param identifier
     *            the identifier
     * @param coordinateString
     *            the coordinate string
     */

    public final void doubleClickAt(final String objectName,
            final String identifier, final String coordinateString) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(objectName, identifier);
        ObjectLocator locator =
                new ObjectLocator(objectName, identifier, actualLocator);
        doDoubleClickAt(locator, coordinateString);
    }

    /**
     * Double click at.
     * 
     * @param objectName
     *            the object name
     * @param coordinateString
     *            the coordinate string
     */
    public final void doubleClickAt(final String objectName,
            final String coordinateString) {
        doubleClickAt(objectName, "", coordinateString);
    }

    /**
     * Doubleclicks on a link, button, checkbox or radio button. If the action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad.<br>
     * The main differentiator of <b> DoubleClickAt </b> is, user can make the
     * script click on a relative location to the element <br>
     * <br>
     * 
     * @param locator
     *            : the coordination string to be click which is relative to the
     *            element <br>
     *            this should be specified using relative X and Y coordinates,
     *            in the following format "X,Y"
     * @param coordinateString
     *            the coordinate string
     */

    private void doDoubleClickAt(final ObjectLocator locator,
            final String coordinateString) {
        int counter = getRetryCount();
        int xOffset = 0;
        int yOffset = 0;
        WebDriver driver = getDriver();
        // Retrieve the actual object identification from the OR
        String objectID = locator.getActualLocator();
        String objectName = locator.getLogicalName();
        try {

            // Precheck done to check whether the element is available if
            // element is not
            // present, code will move to the catch block and report an error
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);

            try {
                xOffset =
                        Integer.parseInt((coordinateString.split(",")[0])
                                .trim());
                yOffset =
                        Integer.parseInt((coordinateString.split(",")[1])
                                .trim());
            } catch (Exception e) {

                e.printStackTrace();

                reportresult(true, "DOUBLE CLICK AT :" + objectName + "",
                        "FAILED", "DOUBLE CLICK AT coordinate string ("
                                + coordinateString + ") for :Element ("
                                + objectName + ") [" + objectID
                                + "] is invalid");

                checkTrue(false, true, "DOUBLE CLICK AT coordinate string ("
                        + coordinateString + ") " + "for :Element ("
                        + objectName + ") [" + objectID + "] is invalid");
            }

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                counter--;
                try {

                    Actions doubleClickAt = new Actions(driver);
                    doubleClickAt.moveToElement(element, xOffset, yOffset)
                            .doubleClick();
                    doubleClickAt.build().perform();

                    reportresult(true, "DOUBLE CLICK AT :" + objectName + "",
                            "PASSED", "");
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);
                    // The main possibility of throwing exception at this point
                    // should be due to the element was not
                    // fully loaded, in this catch block handle the exception
                    // untill retry amount of attempts
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "DOUBLE CLICK AT :" + objectName
                                + "", "FAILED",
                                "DOUBLE CLICK AT command  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                        checkTrue(false, true,
                                "DOUBLE CLICK AT command  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                    }
                }
            }

            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {
            // if any exception was raised report report an error and fail the
            // test case
            e.printStackTrace();
            reportresult(true, "DOUBLE CLICK AT :" + objectName + "", "FAILED",
                    "DOUBLE CLICK AT command  :Element (" + objectName + ") ["
                            + objectID + "] not present");
            checkTrue(false, true, "DOUBLE CLICK AT command  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    /**
     * checks whether the object referred by the logical id exists.<br>
     * if the object is not exists, further continuation of the script execution
     * will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * 
     * @param objectName
     *            : logical name of the object provided by the user
     * @param identifier
     *            the identifier
     * @param stopExecution
     *            : if <b> true </b> : stop the execution after the failure <br>
     *            if <b> false </b>: Continue the execution after the failure
     */

    public final void checkElementPresent(final String objectName,
            final String identifier, final boolean stopExecution, final Object... customError) {

        String actualLocator = ObjectMap.getObjectSearchPath(objectName,
                identifier);
        ObjectLocator locator = new ObjectLocator(objectName, identifier,
                actualLocator);
        doCheckElementPresent(locator, stopExecution, customError);
    }

    /**
     * Check element present.
     * 
     * @param objectName
     *            the object name
     * @param stopExecution
     *            the stop execution
     */
    public final void checkElementPresent(final String objectName,
            final boolean stopExecution, final Object... customError) {
        checkElementPresent(objectName, "", stopExecution,customError);
    }

    /**
     * checks whether the object referred by the logical id exists.<br>
     * if the object is not exists, further continuation of the script execution
     * will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * 
     * @param locator
     *            the locator
     * @param stopExecution
     *            : if <b> true </b> : stop the execution after the failure <br>
     *            if <b> false </b>: Continue the execution after the failure
     */
    private void doCheckElementPresent(final ObjectLocator locator,
            final boolean stopExecution, final Object [] customError) {
        // Retrieve the actual object id from the OR
        String objectID = locator.getActualLocator();
        String objectName = locator.getLogicalName();
        try {
            // Check whether the element is present, the validation
            checkForNewWindowPopups();
            checkElementPresence(objectID);

            // if reached this point, test case should be passed
            reportresult(true, "CHECK ELEMENT PRESENT :" + objectName + "",
                    "PASSED", "");
        } catch (Exception e) {
            // if any exception was thrown the faliure should be reported
            // but the continuation will be decided by stpoExecution
            e.printStackTrace();
         
            
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                  reportresult(stopExecution, "CHECK ELEMENT PRESENT :" + objectName
                          + "", "FAILED",
                          "Custom Error :"+generateCustomError(customError)+" System generated Error : command checkElementPresent()  :Element (" + objectName
                                  + ") [" + objectID + "] not present");
                  checkTrue(false, stopExecution,
                          "Custom Error :"+generateCustomError(customError)+" System generated Error : command checkElementPresent()  :Element (" + objectName
                          + ") [" + objectID + "] not present");
              } else {
              
              e.printStackTrace();
              reportresult(stopExecution, "CHECK ELEMENT PRESENT :" + objectName
                      + "", "FAILED",
                      " command checkElementPresent()  :Element (" + objectName
                              + ") [" + objectID + "] not present");
              checkTrue(false, stopExecution,
                      " command checkElementPresent()  :Element (" + objectName
                              + ") [" + objectID + "] not present");
              }
        }
    }

    /**
     * checks whether the object referred by the logical id exists.<br>
     * Returs true or false based on the availability of the element. <br>
     * 
     * @param objectName
     *            : logical name of the object provided by the user
     * @return true, if check element present
     */

    public final boolean checkElementPresent(final String objectName) {
        // Retrieve the actual object id from the OR
        String objectID = ObjectMap.getObjectSearchPath(objectName,
                locatorIdentifire);
        if (objectID.isEmpty()) {
            objectID = objectName;
        }
        boolean isElementPresent = false;
        try {
            // Check whether the element is present, the validation
            objectLocator(objectID);
            return true;

        } catch (Exception e) {
            return isElementPresent;
        }
    }

    /**
     * checks whether the Text referred by the logical id exists.<br>
     * if the text does not exists, further continuation of the script execution
     * will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * 
     * @param objSearchText
     *            the obj search text
     * @param stopOnFailure
     *            : if <b> true </b> : stop the execution after the failure <br>
     *            if <b> false </b>: Continue the execution after the failure
     */
    public final void checkTextPresent(final Object objSearchText,
            final boolean stopOnFailure, final Object... customError) {

        String searchText = checkNullObject(objSearchText, "CHECK TEXT PRESENT");
        int counter = getRetryCount();
        // retrieves the objectid from the object repository
        String objectID = ObjectMap.getObjectSearchPath(searchText,
                locatorIdentifire);

        // if, in case, the user has given the search text itself instead of
        // objectid this
        // code segment acts as a contingency

        if ("".equalsIgnoreCase(objectID)) {
            objectID = searchText;
        }
        /*
         * START DESCRIPTION following for loop was added to make the command
         * more consistent try the command for give amount of time (can be
         * configured through class variable RETRY) command will be tried for
         * "RETRY" amount of times or until command works. any exception thrown
         * within the tries will be handled internally.
         * 
         * can be exited from the loop under 2 conditions 1. if the command
         * succeeded 2. if the RETRY count is exceeded
         */
        while (counter > 0) {

            boolean objectFound = false;
            try {
                counter--;

                objectFound = getDriver().getPageSource().contains(objectID);
                if (objectFound) {
                    reportresult(true,
                            "CHECK TEXT PRESENT :" + searchText + "", "PASSED",
                            "");
                    break;
                }
                sleep(retryInterval);

                if ((!(counter > 0)) && !objectFound) {
                    // if the retry count has exceeded and still the text is not
                    // present,
                    // report a test failure
              
                    
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(stopOnFailure, "CHECK TEXT PRESENT :"
                                + searchText + "", "FAILED",
                                " Custom Error :"+generateCustomError(customError)+" System generated Error : command checkTextPresent()  :Text (" + searchText
                                        + ") [" + objectID + "] not present");
                        checkTrue(false, stopOnFailure,
                                " Custom Error :"+generateCustomError(customError)+" System generated Error : command checkTextPresent()  :Text (" + searchText
                                + ") [" + objectID + "] not present");
                        break;
                        
                    } else {
                        reportresult(stopOnFailure, "CHECK TEXT PRESENT :"
                                + searchText + "", "FAILED",
                                " command checkTextPresent()  :Text (" + searchText
                                        + ") [" + objectID + "] not present");
                        checkTrue(false, stopOnFailure,
                                " command checkTextPresent()  :Text (" + searchText
                                        + ") [" + objectID + "] not present");
                        break;
                    }
                }
                /*
                 * END DESCRIPTION
                 */
            } catch (Exception e) {

                if (!(counter > 0)) {
                    // if the retry count has exceeded and still the text is not
                    // present,
                    // report a test failure
                    e.printStackTrace();
                 
                    
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(stopOnFailure, "CHECK TEXT PRESENT :"
                                + searchText + "", "FAILED",
                                " Custom Error :"+generateCustomError(customError)+" System generated Error : command checkTextPresent()  :Text (" + searchText
                                        + ") [" + objectID + "] not present");
                        checkTrue(false, stopOnFailure,
                                " Custom Error :"+generateCustomError(customError)+" System generated Error : command checkTextPresent()  :Text (" + searchText
                                + ") [" + objectID + "] not present");
                     
                        
                    } else {
                        reportresult(stopOnFailure, "CHECK TEXT PRESENT :"
                                + searchText + "", "FAILED",
                                " command checkTextPresent()  :Text (" + searchText
                                        + ") [" + objectID + "] not present");
                        checkTrue(false, stopOnFailure,
                                " command checkTextPresent()  :Text (" + searchText
                                        + ") [" + objectID + "] not present");
                   
                    }
                }
            }
        }

    }

    /**
     * Checks whether the Object property given by the property name is exists
     * if the property does not exists, further continuation of the script
     * execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page.
     * 
     * @param objectName
     *            : object name alias given by the user.
     * @param identifire
     *            the identifire
     * @param propertyname
     *            : Name of the object property
     * @param expectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     */
    public final void checkObjectProperty(final String objectName,
            final String identifire, final String propertyname,
            final Object expectedvale, final boolean stopOnFailure, final Object... customError) {
        this.locatorIdentifire = identifire;
        checkObjectProperty(objectName, propertyname, expectedvale,
                stopOnFailure,customError);
        this.locatorIdentifire = "";
    }

    /**
     * The Enum ObjectValidationType.
     */
    public static enum ObjectValidationType {

        /** The alloptions. */
        ALLOPTIONS,
        /** The selectedoption. */
        SELECTEDOPTION,
        /** The missingoption. */
        MISSINGOPTION,
        /** The elementpresent. */
        ELEMENTPRESENT,
        /** The propertypresent. */
        PROPERTYPRESENT
    };

    /**
     * Checks whether the Object property given by the property name is exists
     * if the property does not exists, further continuation of the script
     * execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page.
     * 
     * @param objectName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param objExpectedvale
     *            the obj expectedvale
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     */

    public final void checkObjectProperty(final String objectName,
            final String propertyname, final Object objExpectedvale,
            final boolean stopOnFailure, final Object... customError) {

        String expectedvale =
                checkNullObject(objExpectedvale, "CHECK OBJECT PROPERTY");

        // Call the relavant internal method based on the
        // TableValidationType provided by the user
        if (propertyname.equals(ObjectValidationType.ALLOPTIONS.toString())) {

            checkAllSelectOptions(objectName, propertyname, expectedvale,
                    stopOnFailure, customError);
        } else if (propertyname.equals(ObjectValidationType.SELECTEDOPTION
                .toString())) {

            checkSelectedOption(objectName, propertyname, expectedvale,
                    stopOnFailure, customError);
        } else if (propertyname.equals(ObjectValidationType.MISSINGOPTION
                .toString())) {

            checkMissingOption(objectName, propertyname, expectedvale,
                    stopOnFailure, customError);

        } else if (propertyname.equals(ObjectValidationType.ELEMENTPRESENT
                .toString())) {

            checkElementNotPresent(objectName, propertyname, expectedvale,
                    stopOnFailure, customError);
        } else if (propertyname.equals(ObjectValidationType.PROPERTYPRESENT
                .toString())) {

            checkPropertyPresent(objectName, propertyname, expectedvale,
                    stopOnFailure, customError);
        } else {

            checkObjectOtherProperty(objectName, propertyname, expectedvale,
                    stopOnFailure, customError);
        }
    }

    /**
     * Check if a property value is present or not in an element.
     * 
     * @param objectName
     *            the object name
     * @param property
     *            the property
     * @param expectedvale
     *            the expectedvale
     * @param stopOnFailure
     *            the stop on failure
     */
    private void checkPropertyPresent(final String objectName,
            final String property, final String expectedvale,
            final boolean stopOnFailure, final Object [] customError) {

        int counter = getRetryCount();
        // retrieve the actual object ID from object repository
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);

        String propertyName = "";
        String condition = "";

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);

            try {
                String[] commandSet = expectedvale.split("\\|");
                propertyName = commandSet[0];
                condition = commandSet[1];

            } catch (Exception ex) {
                
            
                
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {

                      reportresult(
                              stopOnFailure,
                              "CHECK OBJECT PROPERTY :" + objectName + "." + property,
                              "FAILED",
                              "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty : User inputs ["
                                      + expectedvale
                                      + "] are not in the correct format. Correct format: attributeName|condition");
                      checkTrue(
                              false,
                              stopOnFailure,
                              "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty : User inputs ["
                                      + expectedvale
                                      + "] are not in the correct format. Correct format: attributeName|condition");
                      return;
                      
                      
                } else {

                    reportresult(
                            stopOnFailure,
                            "CHECK OBJECT PROPERTY :" + objectName + "." + property,
                            "FAILED",
                            " command checkObjectProperty : User inputs ["
                                    + expectedvale
                                    + "] are not in the correct format. Correct format: attributeName|condition");
                    checkTrue(
                            false,
                            stopOnFailure,
                            " command checkObjectProperty : User inputs ["
                                    + expectedvale
                                    + "] are not in the correct format. Correct format: attributeName|condition");
                    return;  
                }
                
                
            }
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;
                    boolean isAttributePresent =
                            checkAttributePresent(element, propertyName);
                    if (String.valueOf(isAttributePresent).equalsIgnoreCase(
                            condition.trim())) {
                        reportresult(true, "CHECK OBJECT PROPERTY PRESENT :"
                                + objectName + "." + property, "PASSED",
                                "Input Value = " + expectedvale);

                        break;
                    } else {
                        reportresult(
                                stopOnFailure,
                                "CHECK OBJECT PROPERTY :" + objectName + "."
                                        + property,
                                "FAILED",
                                " command checkObjectProperty : "
                                        + objectName
                                        + " OBJECT PROPERTY PRESENT : ["
                                        + property
                                        + "] condition is different from the expected. Actual : "
                                        + isAttributePresent
                                        + " Expected condition : " + condition
                                        + "");
                        checkTrue(
                                false,
                                stopOnFailure,
                                " command checkObjectProperty : "
                                        + objectName
                                        + " OBJECT PROPERTY PRESENT : ["
                                        + property
                                        + "] condition is different from the expected. Actual : "
                                        + isAttributePresent
                                        + " Expected condition : " + condition
                                        + "");
                        break;
                    }

                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {
                        reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + property, "FAILED",
                                " command checkObjectProperty :Element : ["
                                        + objectName + "] is not accessible");
                        checkTrue(false, stopOnFailure,
                                " command checkObjectProperty :Element : ["
                                        + property + "] is not accessible");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName
                    + "." + property, "FAILED",
                    " command checkObjectProperty :Element : [" + objectName
                            + "] is not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty :Element : [" + objectName
                            + "] is not present");
        }
    }

    /**
     * Check attribute present.
     * 
     * @param element
     *            the element
     * @param propertyName
     *            the property name
     * @return true, if check attribute present
     */
    private boolean checkAttributePresent(final WebElement element,
            final String propertyName) {

        boolean isAttributePresent;
        if ("textContent".equalsIgnoreCase(propertyName)) {
            String textValue = element.getText();
            if ("".equals(textValue) || textValue == null) {
                isAttributePresent = false;
            } else {
                isAttributePresent = true;
            }
        } else {
            if (element.getAttribute(propertyName.toUpperCase(Locale
                    .getDefault())) != null) {
                isAttributePresent = true;
            } else {
                isAttributePresent = false;
            }
        }
        return isAttributePresent;
    }

    /**
     * Check if a option is not displaying in the webpage.
     * 
     * @param objectName
     *            the object name
     * @param propertyname
     *            the propertyname
     * @param expectedvale
     *            the expectedvale
     * @param stopOnFailure
     *            the stop on failure
     */
    private void checkMissingOption(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure,final Object [] customError) {

        int counter = getRetryCount();
        // retrieve the actual object ID from object repository
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            Select selectElement = new Select(element);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;

                    /*
                     * String[] selectOptions = selenium
                     * .getSelectOptions(objectID);
                     */
                    List<WebElement> elementOptions =
                            selectElement.getOptions();
                    String[] selectOptions = new String[elementOptions.size()];
                    for (int i = 0; i < elementOptions.size(); i++) {
                        selectOptions[i] = elementOptions.get(i).getText();
                    }
                    for (String option : selectOptions) {

                        if (option.equals(expectedvale)) {
                            
                         
                            
                           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                                  reportresult(stopOnFailure,
                                          "CHECK OBJECT PROPERTY :" + objectName
                                                  + "." + propertyname, "FAILED",
                                          "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty "
                                                  + propertyname + ": OPTION :"
                                                  + expectedvale + "  : of object ["
                                                  + objectName + "] is present");
                                  checkTrue(false, stopOnFailure,
                                          "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty "
                                                  + propertyname + ": OPTION :"
                                                  + expectedvale + "  : of object ["
                                                  + objectName + "] is present");
                                  break;
                            }else {
                                reportresult(stopOnFailure,
                                        "CHECK OBJECT PROPERTY :" + objectName
                                                + "." + propertyname, "FAILED",
                                        " command checkObjectProperty "
                                                + propertyname + ": OPTION :"
                                                + expectedvale + "  : of object ["
                                                + objectName + "] is present");
                                checkTrue(false, stopOnFailure,
                                        " command checkObjectProperty "
                                                + propertyname + ": OPTION :"
                                                + expectedvale + "  : of object ["
                                                + objectName + "] is present");
                                break;  
                            }

                            
                        }
                    }
                    reportresult(true, "CHECK OBJECT PROPERTY :" + objectName
                            + "." + propertyname, "PASSED", "Input Value = "
                            + expectedvale);
                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception e) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {
              
                        
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                              reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                      + objectName + "." + propertyname, "FAILED",
                                      "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty :Element : ["
                                              + objectName + "] is not accessible");
                              checkTrue(false, stopOnFailure,
                                      "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty :Element : ["
                                              + objectName + "] is not accessible");
                              
                        } else {

                            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                    + objectName + "." + propertyname, "FAILED",
                                    " command checkObjectProperty :Element : ["
                                            + objectName + "] is not accessible");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty :Element : ["
                                            + objectName + "] is not accessible");
                        }
                        
                        
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName
                    + "." + propertyname, "FAILED",
                    " command checkObjectProperty :Element : [" + objectName
                            + "] is not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty :Element : [" + objectName
                            + "] is not present");
        }
    }

    /**
     * Check if a element is displaying or not in the webpage.
     * 
     * @param objectName
     *            the object name
     * @param propertyname
     *            the propertyname
     * @param expectedvale
     *            the expectedvale
     * @param stopOnFailure
     *            the stop on failure
     */

    public final void checkElementNotPresent(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure, final Object [] customError) {

        int counter = getRetryCount();
        // retrieve the actual object ID from object repository
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);

        String isObjectFound = "false";
        /*
         * START DESCRIPTION following for loop was added to make the command
         * more consistent try the command for give amount of time (can be
         * configured through class variable RETRY) command will be tried for
         * "RETRY" amount of times or until command works. any exception thrown
         * within the tries will be handled internally.
         * 
         * can be exited from the loop under 2 conditions 1. if the command
         * succeeded 2. if the RETRY count is exceeded
         */
        while (counter > 0) {
            try {
                counter--;
                try {
                    objectLocator(objectID);
                    isObjectFound = "True";
                } catch (Exception ex) {
                    getLog().error(
                            "Element " + objectName
                                    + " cannot be found. Error : "
                                    + ex.getMessage());
                }
                if (isObjectFound.equalsIgnoreCase(expectedvale)) {
                    reportresult(true, "CHECK OBJECT PROPERTY :" + objectName
                            + ".ELEMENTPRESENT", "PASSED", "");
                    break;
                } else {
                    if (counter < 1) {
                        throw new Exception("Element " + objectName);
                    }
                }

            } catch (Exception e) {
                sleep(retryInterval);

                if (e.getMessage().startsWith("Element")) {
         
                    
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                          reportresult(
                                  stopOnFailure,
                                  "CHECK OBJECT PROPERTY: " + objectName
                                          + ".ELEMENTPRESENT",
                                  "FAILED",
                                  "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty :Element : ["
                                          + objectName
                                          + "] [ELEMENTPRESENT] condition is different from actual. Expected :"
                                          + expectedvale + " Actual : "
                                          + isObjectFound + "");
                          checkTrue(
                                  false,
                                  stopOnFailure,
                                  "Custom Error :"+generateCustomError(customError)+" System generated Error :  command checkObjectProperty :Element : ["
                                          + objectName
                                          + "] [ELEMENTPRESENT] condition is different from actual. Expected :"
                                          + expectedvale + " Actual : "
                                          + isObjectFound + "");
                          break;
                          
                          
                          
                    } else {
                        reportresult(
                                stopOnFailure,
                                "CHECK OBJECT PROPERTY: " + objectName
                                        + ".ELEMENTPRESENT",
                                "FAILED",
                                " command checkObjectProperty :Element : ["
                                        + objectName
                                        + "] [ELEMENTPRESENT] condition is different from actual. Expected :"
                                        + expectedvale + " Actual : "
                                        + isObjectFound + "");
                        checkTrue(
                                false,
                                stopOnFailure,
                                " command checkObjectProperty :Element : ["
                                        + objectName
                                        + "]  [ELEMENTPRESENT] condition is different from actual. Expected :"
                                        + expectedvale + " Actual : "
                                        + isObjectFound + "");
                        break;
                    }
                    
                    
                   
                } else {
                    reportresult(true, "CHECK OBJECT PROPERTY : " + objectName
                            + ".ELEMENTPRESENT", "FAILED",
                            " command checkObjectProperty :Element : ["
                                    + objectName
                                    + "] [ELEMENTPRESENT] is not accessible");
                    checkTrue(false, stopOnFailure,
                            " command checkObjectProperty :Element : ["
                                    + objectName
                                    + "] [ELEMENTPRESENT] is not accessible");
                    break;
                }

            }
        }

    }

    /**
     * Checks all the options in a select element. <br>
     * The option value count must be same in actual and expected.
     * 
     * @param objectName
     *            the object name
     * @param propertyname
     *            the propertyname
     * @param expectedvale
     *            the expectedvale
     * @param stopOnFailure
     *            the stop on failure
     */
    private void checkAllSelectOptions(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure, final Object [] customError) {

        int counter = getRetryCount();
        String verificationErrors = "";
        // retrieve the actual object ID from object repository
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            Select selectElement = new Select(element);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            String[] expectedSelectOptions = expectedvale.split(",");

            while (counter > 0) {
                try {
                    counter--;
                    List<WebElement> elementOptions =
                            selectElement.getOptions();
                    String[] actualSelectOptions =
                            new String[elementOptions.size()];
                    for (int i = 0; i < elementOptions.size(); i++) {
                        actualSelectOptions[i] =
                                elementOptions.get(i).getText();
                    }
                    // Check if the input option count is different from the
                    // actual option count
                    if (actualSelectOptions.length != expectedSelectOptions.length) {
              
                        
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                              
                              reportresult(
                                      stopOnFailure,
                                      "CHECK OBJECT PROPERTY :" + objectName + "."
                                              + propertyname,
                                      "FAILED",
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error : command checkObjectProperty()  "
                                              + ":Expected options count :"
                                              + expectedSelectOptions.length
                                              + ""
                                              + " is diffrernt from the Actual options count : "
                                              + actualSelectOptions.length
                                              + " of ["
                                              + objectName
                                              + "] Expected : "
                                              + Arrays.toString(expectedSelectOptions)
                                              + " Actual : " + ""
                                              + Arrays.toString(actualSelectOptions)
                                              + "");
                              checkTrue(
                                      false,
                                      stopOnFailure,
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error : command checkObjectProperty()  "
                                              + ":Expected options count :"
                                              + expectedSelectOptions.length
                                              + ""
                                              + " is diffrernt from the Actual options count : "
                                              + actualSelectOptions.length
                                              + " of ["
                                              + objectName
                                              + "] Expected : "
                                              + Arrays.toString(expectedSelectOptions)
                                              + " Actual : " + ""
                                              + Arrays.toString(actualSelectOptions)
                                              + "");
                              break;
                              
                              
                              
                        } else {
                            
                            reportresult(
                                    stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName + "."
                                            + propertyname,
                                    "FAILED",
                                    " command checkObjectProperty()  "
                                            + ":Expected options count :"
                                            + expectedSelectOptions.length
                                            + ""
                                            + " is diffrernt from the Actual options count : "
                                            + actualSelectOptions.length
                                            + " of ["
                                            + objectName
                                            + "] Expected : "
                                            + Arrays.toString(expectedSelectOptions)
                                            + " Actual : " + ""
                                            + Arrays.toString(actualSelectOptions)
                                            + "");
                            checkTrue(
                                    false,
                                    stopOnFailure,
                                    " command checkObjectProperty()  "
                                            + ":Expected options count :"
                                            + expectedSelectOptions.length
                                            + ""
                                            + " is diffrernt from the Actual options count : "
                                            + actualSelectOptions.length
                                            + " of ["
                                            + objectName
                                            + "] Expected : "
                                            + Arrays.toString(expectedSelectOptions)
                                            + " Actual : " + ""
                                            + Arrays.toString(actualSelectOptions)
                                            + "");
                            break;
                        }
                    }
                    StringBuilder verificationErrorBuilder =
                            compareActualOptionValuesWithExpected(
                                    actualSelectOptions, expectedSelectOptions);
                    verificationErrors = verificationErrorBuilder.toString();
                    // If there is a mismatch
                    if (!verificationErrors.isEmpty()) {

               
                        
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                              reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                      + objectName + "." + propertyname, "FAILED",
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK OBJECT PROPERTY :Element (" + objectName
                                              + ") Error Str:" + verificationErrors);

                              // VTAF specific validation framework reporting
                              checkTrue(false, stopOnFailure,
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK OBJECT PROPERTY :Element (" + objectName
                                      + ") Error Str:" + verificationErrors);
                              break;
                              
                              
                        } else {
                            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                    + objectName + "." + propertyname, "FAILED",
                                    "CHECK OBJECT PROPERTY :Element (" + objectName
                                            + ") Error Str:" + verificationErrors);

                            // VTAF specific validation framework reporting
                            checkTrue(false, stopOnFailure,
                                    "CHECK OBJECT PROPERTY :Element (" + objectName
                                            + ") Error Str:" + verificationErrors);
                            break;  
                        }
                        

                    } else {
                        reportresult(true, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "PASSED",
                                "Input Value " + expectedvale);
                        break;
                    }

                    // If the length of the input does not match with the
                    // actual option count

                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception e) {
                    sleep(retryInterval);

                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(stopOnFailure,
                                "CHECK ALL SELECT OPTIONS :" + objectName + "."
                                        + propertyname, "FAILED",
                                " command checkObjectProperty()  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                        checkTrue(false, stopOnFailure,
                                " command checkObjectProperty()  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amount, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            e.printStackTrace();
            reportresult(stopOnFailure, "CHECK ALL SELECT OPTIONS :"
                    + objectName + "." + propertyname, "FAILED",
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
        }
    }

    /**
     * Compare actual option values with expected.
     * 
     * @param actualSelectOptions
     *            the actual select options
     * @param expectedSelectOptions
     *            the expected select options
     * @return the string builder
     */
    private StringBuilder compareActualOptionValuesWithExpected(
            final String[] actualSelectOptions,
            final String[] expectedSelectOptions) {

        StringBuilder verificationErrorBuilder = new StringBuilder();
        for (int optionIndex = 0; optionIndex < actualSelectOptions.length; optionIndex++) {

            if (!Arrays.asList(actualSelectOptions).contains(
                    expectedSelectOptions[optionIndex])) {

                verificationErrorBuilder
                        .append("\n Option :"
                                + optionIndex
                                + " : "
                                + expectedSelectOptions[optionIndex]
                                + " Option is not available in the actual element. Actual ["
                                + Arrays.toString(actualSelectOptions) + "]");
            }
        }
        return verificationErrorBuilder;
    }

    /**
     * Check the current selected option value in a select element.
     * 
     * @param objectName
     *            the object name
     * @param propertyname
     *            the propertyname
     * @param expectedvale
     *            the expectedvale
     * @param stopOnFailure
     *            the stop on failure
     */
    private void checkSelectedOption(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure, final Object [] customError) {

        int counter = getRetryCount();
        // retrieve the actual object ID from object repository
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            Select selectElement = new Select(element);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;

                    /*
                     * String selectedOptionLabel = selenium
                     * .getSelectedLabel(objectID);
                     */
                    String selectedOptionLabel =
                            selectElement.getFirstSelectedOption().getText();

                    if (selectedOptionLabel.equals(expectedvale)) {

                        reportresult(true, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "PASSED",
                                "Input Value " + expectedvale);
                        break;
                    } else {
               
                        
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                              reportresult(stopOnFailure, "CHECK OBJECT PROPERTY : "
                                      + objectName + "." + propertyname, "FAILED",
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error :  object property match Expected:"
                                              + expectedvale + " Actual:"
                                              + selectedOptionLabel);
                              checkTrue(false, stopOnFailure,
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error :  object property match Expected:"
                                              + expectedvale + " Actual:"
                                              + selectedOptionLabel);
                              break;
                              
                              
                        } else {

                            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY : "
                                    + objectName + "." + propertyname, "FAILED",
                                    " object property match Expected:"
                                            + expectedvale + " Actual:"
                                            + selectedOptionLabel);
                            checkTrue(false, stopOnFailure,
                                    " object property match Expected:"
                                            + expectedvale + " Actual:"
                                            + selectedOptionLabel);
                            break; 
                        }
                        
                    }

                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception e) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {
                        reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "FAILED",
                                " command checkObjectProperty()  :Element ("
                                        + "[" + objectName
                                        + "] cannot access element");
                        checkTrue(false, stopOnFailure,
                                " command checkObjectProperty()  :Element ("
                                        + "[" + objectName
                                        + "] cannot access element");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName
                    + "." + propertyname, "FAILED",
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
        }
    }

    /**
     * Check user defined attribute value in the specific element.
     * 
     * @param objectName
     *            the object name
     * @param propertyname
     *            the propertyname
     * @param expectedvale
     *            the expectedvale
     * @param stopOnFailure
     *            the stop on failure
     */

    private void checkObjectOtherProperty(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure,final Object [] customError) {
        int counter = getRetryCount();
        // retrieve the actual object ID from object repository
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    String attributeValue =
                            validateObjectProperty(element, propertyname);
                    if (attributeValue.trim().equals(expectedvale.trim())) {
                        reportresult(true, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "PASSED",
                                "Input Value " + expectedvale);
                        break;
                    } else {
               
                        
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                              reportresult(
                                      true,
                                      "CHECK OBJECT PROPERTY :" + objectName + "."
                                              + propertyname,
                                      "FAILED",
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error :  object property value match expected. Expected value : "
                                              + expectedvale
                                              + " is not equal to the Actual value : "
                                              + attributeValue);
                              checkTrue(
                                      false,
                                      stopOnFailure,
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error :  object property value match expected. Expected value : "
                                              + expectedvale
                                              + " is not equal to the Actual value : "
                                              + attributeValue);
                              break;
                              
                              
                        } else {
                            reportresult(
                                    true,
                                    "CHECK OBJECT PROPERTY :" + objectName + "."
                                            + propertyname,
                                    "FAILED",
                                    " object property value match expected. Expected value : "
                                            + expectedvale
                                            + " is not equal to the Actual value : "
                                            + attributeValue);
                            checkTrue(
                                    false,
                                    stopOnFailure,
                                    " object property value match expected. Expected value : "
                                            + expectedvale
                                            + " is not equal to the Actual value : "
                                            + attributeValue);
                            break; 
                        }
                    }

                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);
                    /*
                     * after the retry amout, if still the object is not found,
                     * report the failure error will be based on the exception
                     * message, if e contains attribute report attribute failure
                     * else if e contains element, report object not found
                     */
                    if (!(counter > 0)) {
                        if (e.getMessage().startsWith("Attribute")) {
                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    " command checkObjectProperty() :Atrribute ("
                                            + propertyname + ")of ["
                                            + objectName + "] not present");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty() :Atrribute ("
                                            + propertyname + ")of ["
                                            + objectName + "] not present");
                        } else if (e.getMessage().startsWith("Element")) {
                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    " command checkObjectProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                        }
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName
                    + "." + propertyname, "FAILED",
                    " command checkObjectProperty() :Element (" + objectName
                            + ") [" + objectID + "] not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty() :Element (" + objectName
                            + ") [" + objectID + "] not present");
        }
    }

    /**
     * This is a multipurpose function which performes various validations in a
     * web table. This is the only function provided with the VTAF which does
     * many types of validations in table. Function provides the following
     * validations
     * 
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>Option</th>
     * <th>Description</th>
     * <th>Option specifier</th>
     * <th>Expected data examples</th>
     * <th>expected result</th>
     * <tbody >
     * <tr>
     * <td>
     * Validate Row Count</td>
     * <td>
     * Checks whether the table contains the expected row count.</td>
     * <td>
     * TableValidationType.ROWCOUNT</td>
     * <td>
     * and integer value specifying the row conut <br>
     * E.g. 10.</td>
     * <td>
     * Pass if the table contains the expectd row count</td>
     * </tr>
     * <tr>
     * <td>Validate Table Cell Value</td>
     * <td>Validating whether the expected data is contained in the cell
     * specified by row and column indexes</td>
     * <td>TableValidationType.CELL</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>firstname</th>
     * <th>lastname</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tbody > </tbody>
     * </table>
     * <br>
     * To validate whether data 'Navaratne' exists in cell given by (1,2) <br>
     * where 1 is the row and column is 2, <br>
     * passing data should be given as, <br>
     * 1,2,Navaratne <br>
     * <br>
     * Note that the index numbering for rows and columns starts from 0 <br>
     * inclusive table header</td>
     * 
     * <td>Pass if the expected data exists in the specified cell</td>
     * </tr>
     * <tr>
     * <td>
     * Validate Column Count</td>
     * <td>
     * Checks whether the column count is maching the expected column count</td>
     * <td>
     * TableValidationType.COLCOUNT</td>
     * <td>
     * and integer value specifying the column conut <br>
     * E.g. 2.</td>
     * <td>
     * Pass if the table contains the expectd column count</td>
     * </tr>
     * 
     * 
     * <tr>
     * <td>
     * validating table data</td>
     * <td>
     * Validating whether the expected data is contained in the table</td>
     * <td>
     * TableValidationType.TABLEDATA</td>
     * <td>
     * A comma seperated list of data specifying the values to be tested <br>
     * E.g. Nadee,Navaratne,29</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * <tr>
     * <td>
     * Relative data validation</td>
     * <td>
     * Validating data in a cell, which is n numbers of cells away from the cell
     * <br>
     * which has the base data</td>
     * <td>
     * TableValidationType.RELATIVE</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>first name</th>
     * <th>sur name</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>Damith</td>
     * <td>Chandana</td>
     * <td>26</td>
     * </tr>
     * 
     * <tbody > </tbody>
     * </table>
     * E.g. 1 <br>
     * suppose if user wants to check the age of Nadee,So the value should be <br>
     * #Nadee,2,29<br>
     * The above data says, cell which is 2 cells away from Nadee should contain
     * 29<br>
     * <br>
     * 
     * E.g. 2<br>
     * suppose if we need to validate the ages of all three users <br>
     * the passing data shlould be<br>
     * #Nadee,2,29#Kanchana,2,28#Damith,2,26 <br>
     * <br>
     * 
     * E.g. 3 <br>
     * suppose the data we know is different,<br>
     * we know<br>
     * first name of the first user<br>
     * sur name of the second user<br>
     * id of the third user<br>
     * <br>
     * 
     * the string to validate age should be <br>
     * #Nadee,2,29#Wickramasingha,1,28#3,3,26 W *</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * </tbody>
     * </table>
     * 
     * @param objectName
     *            Logical name of the object
     * @param identifire
     *            identifier is us to increase the reusability of the locator.
     *            The usage can be defined using the following example <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * @param validationType
     *            validation type code please refer the above table for detailed
     *            information
     * @param expectedvale
     *            value\value string which will be provided by the user to
     *            specify the expected result
     * @param stopOnFaliure
     *            specify the continuation of the test script if the validation
     *            fails
     */
    public final void checkTable(final String objectName,
            final String identifire, final String validationType,
            final Object expectedvale, final boolean stopOnFaliure, final Object... customError) {
        this.locatorIdentifire = identifire;
        checkTable(objectName, validationType, expectedvale, stopOnFaliure,customError);
        this.locatorIdentifire = "";

    }

    /**
     * Gets the object count.
     * 
     * @param objectName
     *            the object name
     * @param identifire
     *            the identifire
     * @return the object count
     */
    public final int getObjectCount(final String objectName,
            final String identifire) {
        this.locatorIdentifire = identifire;
        int objCount = getObjectCount(objectName);
        this.locatorIdentifire = "";
        return objCount;
    }

    /**
     * Select an option\options from a drop-down using an option locator. <br>
     * 
     * Option locators provide different ways of specifying options of an HTML
     * Select element (e.g. for selecting a specific option, or for asserting
     * that the selected option satisfies a specification). There are several
     * forms of Select Option Locator.<br>
     * <br>
     * 
     * <b>label=labelPattern:</b> matches options based on their labels, i.e.
     * the visible text. (This is the default.) label=regexp:^[Oo]ther<br>
     * <b>value=valuePattern:</b> matches options based on their values.
     * value=other<br>
     * <b>id=id:</b> matches options based on their ids. id=option1<br>
     * <b>index=index:</b> matches an option based on its index (offset from
     * zero). index=2<br>
     * <br>
     * 
     * If no option locator prefix is provided, the default behaviour is to
     * match on label. <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter <br>
     * <br>
     * @return the object count
     */
    public final int getObjectCount(final String objectName) {
        int counter = getRetryCount();
        int objectCount = 0;
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        WebDriver driver = getDriver();
        try {

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;

                    List<WebElement> elements =
                            driver.findElements(getLocatorType(objectID));
                    objectCount = elements.size();
                    reportresult(true, "GET OBJECT COUNT :" + objectName + "",
                            "PASSED", "getObjectCount command :Element ("
                                    + objectName + ") [" + objectID + "] ");
                    break;
                } catch (Exception e) {
                    sleep(retryInterval);

                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "GET OBJECT COUNT :" + objectName
                                + "", "FAILED",
                                "getObjectCount command cannot access :Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "getObjectCount command cannot access  :Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // waiting for the maximum amount of waiting time before failing the
            // test case
            // Several checks were introduced to narrow down to the failure to
            // the exact cause.
            if (!(counter > 0)) {
                e.printStackTrace();
                reportresult(true, "SELECT :" + objectName + "", "FAILED",
                        "GET OBJECT COUNT command  :Element (" + objectName
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, "GET OBJECT COUNT command  :Element ("
                        + objectName + ") [" + objectID + "] not present");
            } else if ("Element".equalsIgnoreCase(e.getMessage())) {
                e.printStackTrace();
                reportresult(true, "GET OBJECT COUNT :" + objectName + "",
                        "FAILED", "GET OBJECT COUNT command  :Element ("
                                + objectName + ") [" + objectID
                                + "] not present");
                checkTrue(false, true, "GET OBJECT COUNT command  :Element ("
                        + objectName + ") [" + objectID + "] not present");

            }

        }
        return objectCount;
    }

    /**
     * This is a multipurpose function which performes various validations in a
     * web table. This is the only function provided with the VTAF which does
     * many types of validations in table. Function provides the following
     * validations
     * 
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>Option</th>
     * <th>Description</th>
     * <th>Option specifier</th>
     * <th>Expected data examples</th>
     * <th>expected result</th>
     * <tbody >
     * <tr>
     * <td>
     * Validate Row Count</td>
     * <td>
     * Checks whether the table contains the expected row count.</td>
     * <td>
     * TableValidationType.ROWCOUNT</td>
     * <td>
     * and integer value specifying the row conut <br>
     * E.g. 10.</td>
     * <td>
     * Pass if the table contains the expectd row count</td>
     * </tr>
     * <tr>
     * <td>Validate Table Cell Value</td>
     * <td>Validating whether the expected data is contained in the cell
     * specified by row and column indexes</td>
     * <td>TableValidationType.CELL</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>firstname</th>
     * <th>lastname</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tbody > </tbody>
     * </table>
     * <br>
     * To validate whether data 'Navaratne' exists in cell given by (1,2) <br>
     * where 1 is the row and column is 2, <br>
     * passing data should be given as, <br>
     * 1,2,Navaratne <br>
     * <br>
     * Note that the index numbering for rows and columns starts from 0 <br>
     * inclusive table header</td>
     * 
     * <td>Pass if the expected data exists in the specified cell</td>
     * </tr>
     * <tr>
     * <td>
     * Validate Column Count</td>
     * <td>
     * Checks whether the column count is maching the expected column count</td>
     * <td>
     * TableValidationType.COLCOUNT</td>
     * <td>
     * and integer value specifying the column conut <br>
     * E.g. 2.</td>
     * <td>
     * Pass if the table contains the expectd column count</td>
     * </tr>
     * 
     * 
     * <tr>
     * <td>
     * validating table data</td>
     * <td>
     * Validating whether the expected data is contained in the table</td>
     * <td>
     * TableValidationType.TABLEDATA</td>
     * <td>
     * A comma seperated list of data specifying the values to be tested <br>
     * E.g. Nadee,Navaratne,29</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * <tr>
     * <td>
     * Relative data validation</td>
     * <td>
     * Validating data in a cell, which is n numbers of cells away from the cell
     * <br>
     * which has the base data</td>
     * <td>
     * TableValidationType.RELATIVE</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>first name</th>
     * <th>sur name</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>Damith</td>
     * <td>Chandana</td>
     * <td>26</td>
     * </tr>
     * 
     * <tbody > </tbody>
     * </table>
     * E.g. 1 <br>
     * suppose if user wants to check the age of Nadee,So the value should be <br>
     * #Nadee,2,29<br>
     * The above data says, cell which is 2 cells away from Nadee should contain
     * 29<br>
     * <br>
     * 
     * E.g. 2<br>
     * suppose if we need to validate the ages of all three users <br>
     * the passing data shlould be<br>
     * #Nadee,2,29#Kanchana,2,28#Damith,2,26 <br>
     * <br>
     * 
     * E.g. 3 <br>
     * suppose the data we know is different,<br>
     * we know<br>
     * first name of the first user<br>
     * sur name of the second user<br>
     * id of the third user<br>
     * <br>
     * 
     * the string to validate age should be <br>
     * #Nadee,2,29#Wickramasingha,1,28#3,3,26 W *</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * </tbody>
     * </table>
     * 
     * @param objectName
     *            Logical name of the object
     * @param validationTypeS
     *            the validation type s
     * @param objExpectedvale
     *            the obj expectedvale
     * @param stopOnFaliure
     *            specify the continuation of the test script if the validation
     *            fails
     */
    public final void checkTable(final String objectName,
            final String validationTypeS, final Object objExpectedvale,
            final boolean stopOnFaliure, final Object... customError) {

        TableValidationType validationType =
                TableValidationType.valueOf(validationTypeS);
        String expectedvale = checkNullObject(objExpectedvale, "CHECK TABLE");

        String objectID = "";
        // load the actual object id from the OR
        objectID = ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        try {
            // checks the element presence
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);

            // Call the relavant internal method based on the
            // TableValidationType provided by the user
            try {
                if (validationType == TableValidationType.ROWCOUNT) {

                    validateTableRowCount(element, objectID, expectedvale,
                            stopOnFaliure, customError);
                } else if (validationType == TableValidationType.COLCOUNT) {

                    validateTableColCount(element, objectID, expectedvale,
                            stopOnFaliure, customError);
                } else if (validationType == TableValidationType.TABLEDATA) {

                    compareTableData(element, objectID, expectedvale,
                            stopOnFaliure, customError);
                } else if (validationType == TableValidationType.RELATIVE) {

                    validateTableOffset(element, objectID, expectedvale,
                            stopOnFaliure, customError);
                } else if (validationType == TableValidationType.TABLECELL) {

                    validateCellValue(element, objectID, expectedvale,
                            stopOnFaliure, customError);
                }
            } catch (Exception e) {
                // waiting for the maximum amount of waiting time before
                // failing the test case
                // Several checks were introduced to narrow down to the
                // failure to the exact cause.

                reportresult(true, "CHECK TABLE :" + validationType + " :"
                        + objectName + "", "FAILED", objectName
                        + " is not accessible");
                checkTrue(false, stopOnFaliure,
                        " command checkTable()  :Element (" + objectName
                                + ") [" + objectID + "] is not accessible");

            }
        } catch (Exception e) {
            // if object is not present catch the exception and repor the
            // error
            e.printStackTrace();
            reportresult(true, "CHECK TABLE :" + validationType + " :"
                    + objectName + "", "FAILED", objectName + " not present");
            checkTrue(false, stopOnFaliure, " command checkTable()  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    /**
     * Validate table cell value function.
     * 
     * @param element
     *            the element
     * @param objectName
     *            the object name
     * @param expectedvalue
     *            the expectedvalue
     * @param fail
     *            the fail
     * @throws Exception
     *             the exception
     */
    private void validateCellValue(final WebElement element,
            final String objectName, final String expectedvalue,
            final boolean fail, final Object [] customError) throws Exception {

        ArrayList<String> inputStringArray;
        boolean failedOnce = false;
        int row = -1;
        int col = -1;
        String cellText = "";
        String result = "";
        ArrayList<String> htmlTable = new ArrayList<String>();
        final int inputStringItems = 3;

        inputStringArray =
                new ArrayList<String>(Arrays.asList(expectedvalue.split(
                        "(?<!\\\\),", Integer.MAX_VALUE)));

        ArrayList<String> tempInputTable = new ArrayList<String>();
        for (String inputVal : inputStringArray) {
            String formattedValue = inputVal.replaceAll("\\\\,", ",");
            tempInputTable.add(formattedValue);
        }
        inputStringArray = tempInputTable;

        if (inputStringArray.size() < inputStringItems) {
            failedOnce = true;
   
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {

                  result = " verification data not provided correctly";
                  reportresult(true, "CHECK TABLE :TABLE CELL", "FAILED", " Custom Error :"+generateCustomError(customError)+" System generated Error : "+objectName
                          + "'s  CELL validation " + " is not as expected  " + result);
                  checkTrue(false, fail, objectName + " Custom Error :"+generateCustomError(customError)+" System generated Error : "+objectName
                          + "'s  CELL validation " + " is not as expected  " + result);
                  return;
                  
            } else {

                result = " verification data not provided correctly";
                reportresult(true, "CHECK TABLE :TABLE CELL", "FAILED", objectName
                        + "'s  CELL validation " + " is not as expected  " + result);
                checkTrue(false, fail, objectName + "'s  CELL validation "
                        + " is not as expected  " + result);
                return;  
            }
            
            
        }
        row = Integer.parseInt(inputStringArray.get(0));
        col = Integer.parseInt(inputStringArray.get(1));

        cellText = StringUtils.join(
                inputStringArray.subList(2, inputStringArray.size()).toArray(),
                ",");

        try {
            htmlTable = getAppTableRow(element, row);
        } catch (Exception ex) {
            failedOnce = true;
            result =
                    result + "|Expected Row : " + row
                            + " cannot be found in the actual table \n";
        }

        int verifyIndex = col; // get the sequential index of the value to be
                               // verified

        String verifyValue = "";

        try {
            verifyValue = htmlTable.get(verifyIndex).trim();

            if (!cellText.equals(verifyValue)) {
                failedOnce = true;
                result =
                        result + "|Expected : " + cellText + " Actual :"
                                + htmlTable.get(verifyIndex) + "\n";

            }

        } catch (IndexOutOfBoundsException ex) {
            failedOnce = true;
            result =
                    result + "|Expected Column : " + verifyIndex
                            + " cannot be found in the actual table \n";
        }

        if (failedOnce) {
            reportresult(true, "CHECK TABLE :TABLE CELL", "FAILED", objectName
                    + "'s  TABLECELL validation " + " is not as expected  "
                    + result);
            checkTrue(false, fail, objectName + "'s  TABLECELL validation "
                    + " is not as expected  " + result);

        } else {

            reportresult(true, "CHECK TABLE :TABLE CELL", "PASSED", objectName
                    + " . Input Value = " + expectedvalue);
        }

    }

    /**
     * Validate table offset function.
     * 
     * @param element
     *            the element
     * @param objectName
     *            the object name
     * @param expectedvalue
     *            the expectedvalue
     * @param fail
     *            the fail
     * @throws Exception
     *             the exception
     */
    private void validateTableOffset(final WebElement element,
            final String objectName, final String expectedvalue,
            final boolean fail, final Object [] customError) throws Exception {

        ArrayList<String> inputStringArray;
        String parentText = "";
        Integer offset;
        String cellText = "";
        String inputStringCurrStr = "";
        String result = "";
        ArrayList<String> htmlTable;

        htmlTable = getAppTable(element);
        StringBuilder resultBuilder = new StringBuilder();

        ArrayList<String> inputStringCurrArray;
        inputStringArray =
                new ArrayList<String>(Arrays.asList(expectedvalue.split("#")));

        for (int i = 0; i < inputStringArray.size(); i++) {

            // Split the string to parts and entered to an array NAMED
            // inputTable

            // Getting the values out
            inputStringCurrStr = inputStringArray.get(i);

            inputStringCurrArray =
                    new ArrayList<String>(Arrays.asList(inputStringCurrStr
                            .split("(?<!\\\\),", Integer.MAX_VALUE)));

            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputStringCurrArray) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputStringCurrArray = tempInputTable;

            parentText = inputStringCurrArray.get(0);
            offset = Integer.valueOf(inputStringCurrArray.get(1));
            cellText = inputStringCurrArray.get(2);
            resultBuilder
                    .append(checkIfTheTableContainsTheExpectedRelativeValue(
                            htmlTable, parentText, offset, cellText));
        }

        result = resultBuilder.toString();
        if (!result.isEmpty()) {
            
            
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {

                  reportresult(fail, "CHECK TABLE :RELATIVE", "FAILED", " Custom Error :"+generateCustomError(customError)+" System generated Error : "+objectName
                          + "'s  RELATIVE validation " + " is not as expected  "
                          + result);
                  checkTrue(false, fail, objectName + " Custom Error :"+generateCustomError(customError)+" System generated Error : "+objectName
                          + "'s  RELATIVE validation " + " is not as expected  "
                          + result);
                  
            } else {

                reportresult(fail, "CHECK TABLE :RELATIVE", "FAILED", objectName
                        + "'s  RELATIVE validation " + " is not as expected  "
                        + result);
                checkTrue(false, fail, objectName + "'s  RELATIVE validation "
                        + " is not as expected  " + result);  
            }
            
            

        } else {

            reportresult(true, "CHECK TABLE :RELATIVE", "PASSED", objectName
                    + " . Input Value = " + expectedvalue);
        }

    }

    /**
     * Check if the table contains the expected relative value.
     * 
     * @param htmlTable
     *            the html table
     * @param parentText
     *            the parent text
     * @param offset
     *            the offset
     * @param cellText
     *            the cell text
     * @return the string
     */
    private String checkIfTheTableContainsTheExpectedRelativeValue(
            final List<String> htmlTable, final String parentText,
            final int offset, final String cellText) {
        int indexParent;
        StringBuilder resultBuilder = new StringBuilder();
        if (htmlTable.contains(parentText)) {

            ArrayList<Integer> parentTextIndexList = new ArrayList<Integer>();
            for (int k = 0; k < htmlTable.size(); k++) {
                if (htmlTable.get(k).equals(parentText)) {
                    parentTextIndexList.add(k);
                }
            }
            for (int j = 0; j < parentTextIndexList.size(); j++) {

                // indexParent = htmlTable.indexOf(parentText);
                indexParent = parentTextIndexList.get(j);
                String actualText = "";
                try {
                    actualText = htmlTable.get((indexParent + offset));
                    if (!cellText.equals(actualText)) {
                        // failedOnce = true;
                        resultBuilder.append("|Expected : " + cellText
                                + " Actual :" + actualText + " Base value : "
                                + parentText + "\n");
                    } else {
                        break;
                    }

                } catch (IndexOutOfBoundsException ex) {
                    resultBuilder
                            .append("|Expected value : " + cellText
                                    + " cannot be found in the field: "
                                    + (indexParent + offset)
                                    + " in the actual table\n");
                }
            }
        } else {
            resultBuilder.append("|Expected RELATIVE text: " + parentText
                    + " is not present in the actual table \n");
        }
        return resultBuilder.toString();
    }

    /**
     * Validate table row count function.
     * 
     * @param element
     *            the element
     * @param tableName
     *            the Table name
     * @param expectedValue
     *            the expected value
     * @param fail
     *            the fail
     * @return the int
     */
    private int validateTableRowCount(final WebElement element,
            final String tableName, final String expectedValue,
            final boolean fail, final Object [] customError) {
        /* TableName = TableName.replace("\"", "\\\""); */
        int rowCount = 0;
        try {
            /*
             * JS = "this.browserbot.findElement(\"" + TableName + "\")" +
             * ".rows.length.toString()";
             */
            rowCount = element.findElements(By.tagName("tr")).size();
            /* rowCount = selenium.getEval(JS); */

            if (rowCount == Integer.parseInt(expectedValue)) {
                reportresult(true, "CHECK TABLE :ROW COUNT", "PASSED",
                        "CHECK TABLE :ROW COUNT" + tableName
                                + "| Input Value = " + expectedValue);

            } else {
                
               
                
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                      reportresult(true, "CHECK TABLE :ROW COUNT", "FAILED",
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK TABLE :ROW COUNT" + tableName + "| Expected :"
                                      + expectedValue + " |Actual : " + rowCount);
                      checkTrue(false, fail,
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK TABLE :ROW COUNT" + tableName + "| Expected :"
                                      + expectedValue + " |Actual : " + rowCount);
                      
                } else {

                    reportresult(true, "CHECK TABLE :ROW COUNT", "FAILED",
                            "CHECK TABLE :ROW COUNT" + tableName + "| Expected :"
                                    + expectedValue + " |Actual : " + rowCount);
                    checkTrue(false, fail,
                            "CHECK TABLE [ROWCOUNT]  : Row count mismatch Expected:"
                                    + expectedValue + " Actual:" + rowCount);
                }
                
                
            }

        } catch (Exception e) {
            e.printStackTrace();
           
            
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                  reportresult(fail, "CHECK TABLE :ROW COUNT :", "FAILED",
                          " Custom Error :"+generateCustomError(customError)+" System generated Error : "+e.getMessage());
                  checkTrue(false, fail,
                          " Custom Error :"+generateCustomError(customError)+" System generated Error : "+e.getMessage());
                  
            } else {
                reportresult(fail, "CHECK TABLE :ROW COUNT :", "FAILED",
                        e.getMessage());
                checkTrue(false, fail,
                        "CHECK TABLE [ROWCOUNT]  : " + e.getMessage()); 
            }
            
           
        }
        return rowCount;
    }

    /**
     * validate Table Column count function.
     * 
     * @param element
     *            the element
     * @param tableName
     *            the Table name
     * @param expectedValue
     *            the expected value
     * @param fail
     *            the fail
     * @return the int
     */
    private int validateTableColCount(final WebElement element,
            final String tableName, final String expectedValue,
            final boolean fail, final Object [] customError) {

        List<WebElement> rowElements = null;
        int actualdValue = 0;
        /* TableName = TableName.replace("\"", "\\\""); */
        try {
            /*
             * JS = "this.browserbot.findElement(\"" + TableName + "\")" +
             * ".rows[0].cells.length.toString()";
             */
            // String colCount = selenium.getEval(JS);
            rowElements = element.findElements(By.tagName("tr"));
            actualdValue =
                    rowElements.get(1).findElements(By.tagName("td")).size();

            // actualdValue = selenium.getEval(JS);

            if (actualdValue == Integer.parseInt(expectedValue)) {
                reportresult(true, "CHECK TABLE :COLUMN COUNT ", "PASSED",
                        " CHECK TABLE COLUMN COUNT" + tableName
                                + " Input Value = " + expectedValue);

            } else {
   
                
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                      
                      reportresult(fail, "CHECK TABLE :COLUMN COUNT", "FAILED",
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : Expected :" + expectedValue + " |Actual : "
                                      + actualdValue);
                      checkTrue(false, fail,
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : Expected :" + expectedValue + " |Actual : "
                                      + actualdValue);
                      
                } else {
                    
                    reportresult(fail, "CHECK TABLE :COLUMN COUNT", "FAILED",
                            "Expected :" + expectedValue + " |Actual : "
                                    + actualdValue);
                    checkTrue(false, fail,
                            "checkTable [COLUMNCOUNT]  : Column count mismatch Expected:"
                                    + expectedValue + " Actual:" + actualdValue); 
                }
               
            }

        } catch (Exception e) {
            e.printStackTrace();
            reportresult(fail, "CHECK TABLE :COLUMN COUNT", "FAILED",
                    e.getMessage());
            checkTrue(false, fail, e.getMessage());
        }
        return actualdValue;

    }

    /**
     * Reads the online table and load the contents to an arraylist.
     * 
     * @param element
     *            the element
     * @return the app table
     * @throws Exception
     *             the exception
     */
    private ArrayList<String> getAppTable(final WebElement element)
            throws Exception {

        WebElement rowElement;
        List<WebElement> columnElements;
        List<WebElement> rowElements;

        ArrayList<String> htmlTable = new ArrayList<String>();

        /* locator = locator.replace("\"", "\\\""); */

        /*
         * JS = "this.browserbot.findElement(\"" + locator +
         * "\") .rows.length.toString()";
         * 
         * Integer rowNum = Integer.parseInt(selenium.getEval(JS));
         */

        rowElements = element.findElements(By.tagName("tr"));
        int rowNum = rowElements.size();

        if (rowNum > 0) {
            /*
             * JS = "this.browserbot.findElement(\"" + locator + "\")" +
             * ".rows[0].cells.length.toString()";
             * 
             * Integer colNum = Integer.parseInt(selenium.getEval(JS));
             */
            /* locator = locator.replace("\\\"", "\""); */
            String value = "";
            for (int i = 0; i < rowNum; i++) {

                rowElement = rowElements.get(i);

                columnElements = rowElement.findElements(By.tagName("td"));
                if (columnElements.isEmpty()) {
                    columnElements = rowElement.findElements(By.tagName("th"));
                }

                int colNum = columnElements.size();

                for (int j = 0; j < colNum; j++) {

                    value = columnElements.get(j).getText();
                    // value = selenium.getTable(locator + "." + i + "." + j);

                    if (value != null) {
                        htmlTable.add(value);
                    } else {
                        htmlTable.add("");
                    }
                }
            }
        } else {
            throw new Exception();

        }
        return htmlTable;
    }

    /**
     * Reads the online table and load the contents to an arraylist.
     * 
     * @param element
     *            the element
     * @param row
     *            the row
     * @return the app table row
     * @throws Exception
     *             the exception
     */
    private ArrayList<String> getAppTableRow(final WebElement element,
            final int row) throws Exception {

        List<WebElement> rowElements;
        List<WebElement> colElements;
        WebElement rowElement;

        ArrayList<String> htmlTable = new ArrayList<String>();

        /* locator = locator.replace("\"", "\\\""); */

        /*
         * JS = "this.browserbot.findElement(\"" + locator + "\")" + ".rows[" +
         * row + "].cells.length.toString()"; Integer colNum =
         * Integer.parseInt(selenium.getEval(JS));
         */

        rowElements = element.findElements(By.tagName("tr"));
        rowElement = rowElements.get(row);
        colElements = rowElement.findElements(By.tagName("th"));
        colElements.addAll(rowElement.findElements(By.tagName("td")));
        int colNum = colElements.size();

        /* locator = locator.replace("\\\"", "\""); */
        String value = "";
        for (int j = 0; j < colNum; j++) {

            value = colElements.get(j).getText();
            /* value = selenium.getTable(locator + "." + row + "." + j); */

            if (value != null) {
                htmlTable.add(value);
            } else {
                htmlTable.add("");
            }
        }

        return htmlTable;
    }

    /**
     * Checks in a table whether the given table is in.
     * 
     * @param element
     *            the element
     * @param objectName
     *            the object name
     * @param expectedvale
     *            the expectedvale
     * @param fail
     *            the fail
     */
    private void compareTableData(final WebElement element,
            final String objectName, final String expectedvale,
            final boolean fail, final Object [] customError) {

        ArrayList<String> htmlTable;
        ArrayList<String> inputTable;
        try {
            htmlTable = getAppTable(element);

            inputTable =
                    new ArrayList<String>(Arrays.asList(expectedvale
                            .split("(?<!\\\\),")));
            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputTable) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputTable = tempInputTable;

            String inputTableStr = StringUtils.join(inputTable, "|");
            String actualTableStr = StringUtils.join(htmlTable, "|");

            if (actualTableStr.contains(inputTableStr)) {

                reportresult(true, "CHECK TABLE :TABLE DATA ", "PASSED",
                        objectName + " :Input Value = " + expectedvale);

            } else {
                String inputTableString = inputTable.toString();
                String htmlTableString = htmlTable.toString();
        
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                      reportresult(fail, "CHECK TABLE :TABLE DATA ", "FAILED",
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : "+objectName + "'s  TABLEDATA is not as expected  "
                                      + inputTableString + ": Actual :"
                                      + htmlTableString);
                      checkTrue(false, fail, objectName
                              + " Custom Error :"+generateCustomError(customError)+" System generated Error : "+objectName + "'s  TABLEDATA is not as expected  "
                              + inputTableString + ": Actual :"
                              + htmlTableString);
                      
                      
                } else {
                    
                    reportresult(fail, "CHECK TABLE :TABLE DATA ", "FAILED",
                            objectName + "'s  TABLEDATA is not as expected  "
                                    + inputTableString + ": Actual :"
                                    + htmlTableString);
                    checkTrue(false, fail, objectName
                            + "'s  TABLEDATA is not as expected  "
                            + inputTableString + ": Actual :" + htmlTableString);
                }
                
               
            }

        } catch (Exception e) {
            String errorString = e.getMessage();
            reportresult(fail, "CHECK TABLE :TABLE DATA", "FAILED", errorString);
            checkTrue(false, fail, errorString);
        }
    }

    /**
     * The Enum LocatorType.
     */
    public enum LocatorType {

        /** The classname. */
        CLASSNAME,
        /** The css. */
        CSS,
        /** The id. */
        ID,
        /** The link. */
        LINK,
        /** The name. */
        NAME,
        /** The tagname. */
        TAGNAME,
        /** The xpath. */
        XPATH;
    }

    /**
     * Gets the locator type.
     * 
     * @param objectID
     *            the object id
     * @return the locator type
     * @throws Exception
     *             the exception
     */
    public final By getLocatorType(final String objectID) throws Exception {

        String typeString = "";
        String ref = "";
        String objectIDinLowerCase = objectID.toLowerCase(Locale.getDefault());
        boolean isObjectTypeisXpath = objectIDinLowerCase.startsWith("/");
        if (!isObjectTypeisXpath) {
            try {
                typeString =
                        objectIDinLowerCase.substring(0, objectID.indexOf('='));
                ref =
                        objectID.substring(objectID.indexOf('=') + 1,
                                objectID.length());
            } catch (Exception e) {
                throw new Exception("Invalid Locator Passed " + objectID);
            }
        }
        // String objectIDType = typeString.toLowerCase(Locale.getDefault());
        if (isObjectTypeisXpath) {

            return By.xpath(objectID);
        } else if (typeString.contains("xpath")) {

            return By.xpath(ref);
        } else if (typeString.contains("css")) {

            return By.cssSelector(ref);
        } else if (typeString.contains("id")) {

            return By.id(ref);
        } else if (typeString.contains("link")) {

            return By.linkText(ref);
        } else if (typeString.contains("tagname")) {

            return By.tagName(ref);
        } else if (typeString.contains("name")) {

            return By.name(ref);
        } else if (typeString.contains("classname")) {

            return By.className(ref);
        }

        throw new Exception("Invalid Locator Type Passed " + ref);

    }

    /**
     * Object locator.
     * 
     * @param objectID
     *            the object id
     * @return the web element
     * @throws Exception
     *             the exception
     */
    public final WebElement objectLocator(final String objectID)
            throws Exception {

        Logger log = getLog();
        log.info("INFO : Finding Element [ " + objectID + " ]");
        WebDriver driver = getDriver();
        String typeString = "";
        String ref = "";
        String objectIDinLowerCase = objectID.toLowerCase(Locale.getDefault());
        boolean isObjectTypeisXpath = objectIDinLowerCase.startsWith("/");
        if (!isObjectTypeisXpath) {
            typeString =
                    objectIDinLowerCase.substring(0,
                            objectIDinLowerCase.indexOf('='));
            ref =
                    objectID.substring(objectID.indexOf('=') + 1,
                            objectID.length());
        }
        if (isObjectTypeisXpath) {

            return driver.findElement(By.xpath(objectID));
        } else if ("xpath".equals(typeString)) {

            return driver.findElement(By.xpath(ref));
        } else if ("css".equals(typeString)) {

            return driver.findElement(By.cssSelector(ref));
        } else if ("id".equals(typeString)) {

            return driver.findElement(By.id(ref));
        } else if ("link".equals(typeString)) {

            return driver.findElement(By.partialLinkText(ref));
        } else if ("tagname".equals(typeString)) {

            return driver.findElement(By.tagName(ref));
        } else if ("classname".equals(typeString) || "class".equals(typeString)) {

            return driver.findElement(By.className(ref));
        } else if ("name".equals(typeString)) {

            return driver.findElement(By.name(ref));
        }
        log.error("Invalid Locator Type Passed "
                + objectID
                + ". Expected locator types : XPATH, CSS, ID, NAME, LINK, TAGNAME, CLASSNAME");
        throw new Exception("Invalid Locator Type Passed " + ref);

    }

    /**
     * internal method which actually checks whether the given element is
     * exists.
     * 
     * @param searchPath
     *            the search path
     * @return the web element
     * @throws Exception
     *             the exception
     */
    private WebElement checkElementPresence(final String searchPath)
            throws Exception {
        WebDriver driver = getDriver();
        WebElement webElement = null;
        String locator = searchPath;
        final Logger log = getLog();
        int count = getRetryCount();
        setCommandStartTime(getCurrentTime());
        final By searchBy = getLocatorType(locator);
        final Long retryMillis = 1000L;

        try {

            Function<WebDriver, WebElement> findElementFunction =
                    new FindElementFunction<WebDriver, WebElement>(searchBy);

            Wait<WebDriver> wait =
                    new FluentWait<WebDriver>(driver)
                            .withTimeout((count * retryMillis),
                                    TimeUnit.MILLISECONDS)
                            .pollingEvery(retryInterval, TimeUnit.MILLISECONDS)
                            .ignoring(NoSuchElementException.class)
                            .ignoring(WebDriverException.class);

            webElement = wait.until(findElementFunction);

        } catch (Exception e) {
            log.error("Element [ " + searchPath + " ] Not Found", e);
        }

        if (webElement != null) {
            try {
                log.info("Element [ " + searchBy.toString() + " ] Found");
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                jsExecutor.executeScript("arguments[0].scrollIntoView(false);",
                        webElement);
            } catch (Exception ex) {
                log.error("Exception occured while scrolling to the element.",
                        ex);
            }
        } else {
            throw new Exception("Element " + searchPath);
        }

        return webElement;
    }

    /**
     * The FindElementFunction inner class.
     * 
     * @param <T>
     *            the generic type
     * @param <K>
     *            the key type
     */
    public static class FindElementFunction < T, K > implements
            Function<WebDriver, WebElement> {
        /** The search by locator. */
        private By searchByLocator;

        /**
         * The constructor for inner class.
         * 
         * @param by
         *            The by locator
         * */
        public FindElementFunction(final By by) {
            searchByLocator = by;
        }

        /**
         * The apply method which returns a web element.
         * 
         * @param driver
         *            the webdriver
         * @return The web element found
         * */
        @Override
        public final WebElement apply(final WebDriver driver) {
            getLog().info(
                    "Finding Element [ " + searchByLocator.toString() + " ]");
            return driver.findElement(searchByLocator);
        }
    }

    /**
     * Internal method which checks whether the option is available in the
     * select box.
     * 
     * @param valuetoBeSelect
     *            the valueto be select
     * @param actualOptions
     *            the actual options
     * @return true, if check select options
     * @throws Exception
     *             the exception
     */
    private boolean checkSelectOptions(final String valuetoBeSelect,
            final String[] actualOptions) throws Exception {

        String[] optionList = valuetoBeSelect.split("#");
        boolean optionPresent = false;
        boolean multiSelect = false;
        int notFoundCount = 0;
        StringBuilder notFoundItems = new StringBuilder();

        multiSelect = valuetoBeSelect.contains("#");

        for (String option : optionList) {
            optionPresent = false;
            if (option.startsWith("regexp:")) {
                for (String actualOption : actualOptions) {
                    optionPresent =
                            isMatchingPattern(option.substring(
                                    option.indexOf(':') + 1, option.length()),
                                    actualOption);
                    if (optionPresent) {
                        break;
                    }
                }
                if (!optionPresent) {

                    notFoundCount++;
                    notFoundItems.append(option).append(",");
                }

            } else if (option.startsWith("index=")) {
                int indexNo = Integer.parseInt(option.replace("index=", ""));
                if (actualOptions.length <= indexNo) {
                    notFoundCount++;
                    notFoundItems.append(option).append(",");
                }
            } else {
                optionPresent = Arrays.asList(actualOptions).contains(option);
                if (!optionPresent) {
                    notFoundCount++;
                    notFoundItems.append(option).append(",");
                }
            }
        }

        if (!optionPresent && notFoundCount > 0) {
            setErrorMessages(getErrorMessages() + " Options cannot be found |"
                    + notFoundItems.toString());

            throw new Exception("No_Item " + notFoundItems);
        }

        return multiSelect;

    }

    /**
     * Validating the value of the given property of the object, further
     * continuation of the script execution will be decided <br>
     * besed on value of the <b> stopExecution </b> parameter provided by the
     * user <br>
     * <br>
     * in the web page.
     * 
     * @param element
     *            the element
     * @param propertyname
     *            : Name of the object property to be validated
     * @return the string
     * @throws Exception
     *             the exception
     */
    private String validateObjectProperty(final WebElement element,
            final String propertyname) throws Exception {
        String attributeValue = "";
        if ("textContent".equals(propertyname)) {
            try {
                attributeValue = element.getText();

            } catch (Exception ex) {
                throw new Exception("Attribute " + propertyname, ex);
            }
        } else if ("checked".equals(propertyname)) {
            try {
                if (element.isSelected()) {
                    attributeValue = "true";
                } else {
                    attributeValue = "false";
                }
            } catch (Exception ex) {
                throw new Exception("Attribute " + propertyname, ex);
            }
        } else {
            try {
                attributeValue = element.getAttribute(propertyname);

                if (attributeValue == null) {
                    throw new Exception("Attribute " + propertyname);
                }
            } catch (Exception e1) {

                throw new Exception("Attribute " + propertyname, e1);
            }
        }
        return attributeValue;
    }

    /**
     * evaluate the given logical condition and report the result Further
     * execution or termination of test will be decided by the value of isAssert
     * variable.
     * 
     * @param checkingCondition
     *            the checking condition
     * @param isAssert
     *            the is assert
     * @param failedMessage
     *            the failed message
     */
    private void checkTrue(final boolean checkingCondition,
            final boolean isAssert, final String failedMessage) {
        String errorMessage = getErrorMessages();
        String callingClassName = getCallingClassName();
        String currentMethod = getCurrentMethod();
        int lineNumber = getLineNumber();
        if (isAssert) {

            endTestReporting(isAssert);
            assertTrue("Failed " + failedMessage + "\n" + errorMessage
                    + " [At : " + callingClassName + "." + currentMethod
                    + "(Line:" + lineNumber + ")]" + "\n", checkingCondition);
        } else {
            try {
                ITestResult reult;
                setErrorMessages(errorMessage + "\n" + failedMessage
                        + " [At : " + callingClassName + "." + currentMethod
                        + "(Line:" + lineNumber + ")]" + "\n");
                reult = Reporter.getCurrentTestResult();
                reult.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
                reult.setThrowable(new Exception(getErrorMessages()));
                Reporter.setCurrentTestResult(reult);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieve the data table for the parameterized executin
     * */
    public DataTable getTable(final String name) {
        File file;
        if (tables == null) {
            File tempFile = new File("tempFile");
            System.out.println("running location :"
                    + tempFile.getAbsolutePath());
            if (tempFile.getAbsolutePath().contains("grid")) {
                file =
                        new File("grid" + File.separator
                                + "selenium-grid-1.0.6" + File.separator
                                + "data" + File.separator + "DataTables.xml");
                System.out.println("Location of data file is :"
                        + file.getAbsolutePath());
            } else {
                file =
                        new File("src" + File.separator + "main"
                                + File.separator + "resources" + File.separator
                                + "data" + File.separator + "DataTables.xml");
                System.out.println("Location of data file is :"
                        + file.getAbsolutePath());
            }
            tables = DataTablesParser.parseTables(file);
        }
        return tables.get(name);
    }

    /**
     * read the Multiple DataTable store All data in one Virtual Data Table
     * 
     * */
    public DataTable getVirtualDataTable(String[] tableNames) {

        DataTable virtualDataTable = new DataTable();

        for (int i = 0; i < tableNames.length; i++) {
            int columnIndex = 0;
            DataTable table = getTable(tableNames[i]);
            for (String col : table.getColumns()) {
                String colNew = tableNames[i] + "_" + col;
                virtualDataTable.addColumn(colNew);
                virtualDataTable.addDataTypes(table.getDataType(columnIndex));

                for (int row = 0; row < table.getRowCount(); row++) {
                    virtualDataTable.setValue(row, colNew, table.get(row, col));
                }
                columnIndex++;
            }
        }

        return virtualDataTable;
    }

    /**
     * read the DataTable and convert it to a two dimentional array.
     * 
     * @param table
     *            the table
     * @return the table array
     */
    public final Object[][] getTableArray(final DataTable table) {

        Integer rowcount = table.getRowCount();
        Integer colcount = table.getcolCount();

        Object[][] tabArray = new Object[rowcount][colcount];

        for (int row = 0; row < rowcount; row++) {
            for (int col = 0; col < colcount; col++) {
                if ("Int".equals(table.getDataType(col))) {
                    tabArray[row][col] = table.intValue(row, col);
                } else if ("Boolean".equals(table.getDataType(col))) {
                    tabArray[row][col] = table.booleanValue(row, col);
                } else if ("String".equals(table.getDataType(col))) {
                    tabArray[row][col] = table.get(row, col);
                }
            }

        }

        return tabArray;

    }

    /**
     * Gets the data table iterator.
     * 
     * @param tableName
     *            the table name
     * @param associatedRows
     *            the associated rows
     * @return the data table iterator
     */
    public final List<HashMap<String, String>> getDataTableIterator(
            final String tableNames, final String associatedRows) {

        List<HashMap<String, String>> iteratorList =
                new ArrayList<HashMap<String, String>>();
        List<DataTable> tableList = new ArrayList<DataTable>();
        List<int[]> rowList = new ArrayList<int[]>();
        String[] rowsPerTableArray = associatedRows.split("\\|");
        String[] tableNamesArray = tableNames.split("\\|");
        int rowsPerTableCounts = rowsPerTableArray.length;
        int tableNamesCount = tableNamesArray.length;
        if (rowsPerTableCounts == tableNamesCount) {
            try {
                for (int i = 0; i < tableNamesCount; i++) {
                    DataTable table = getTable(tableNamesArray[i]);
                    tableList.add(table);
                    rowList.add(getAssociatedRowNumbers(table,
                            rowsPerTableArray[i]));
                }

                for (int i = 0; i < tableNamesCount; i++) {
                    int currentInsertRow = 0;
                    DataTable dataTable = tableList.get(i);
                    int[] reqRows = rowList.get(i);
                    String tableName = tableNamesArray[i];
                    for (int row : reqRows) {
                        if (iteratorList.size() <= currentInsertRow) {
                            iteratorList.add(new HashMap<String, String>());
                        }
                        HashMap<String, String> currRow =
                                iteratorList.get(currentInsertRow);
                        List<String> columnHeaders = dataTable.getColumns();

                        for (String column : columnHeaders) {
                            currRow.put(tableName.concat("_").concat(column),
                                    dataTable.get(row, column));
                        }

                        currentInsertRow++;
                    }
                }

            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                String errString = e.getMessage();
                reportresult(true, "DATA TABLE ITERATION :", "FAILED",
                        "Cannot iterate over the data table. Cannot find the row numbers : "
                                + associatedRows + " in data table. "
                                + "Actual Error : " + errString);
                checkTrue(false, true,
                        "Cannot iterate over the data table. Actual Error : "
                                + errString);
            } catch (Exception e) {
                e.printStackTrace();
                String errString = e.getMessage();
                reportresult(true, "DATA TABLE ITERATION :", "FAILED",
                        "Cannot iterate over the data table. Actual Error : "
                                + errString);
                checkTrue(false, true,
                        "Cannot iterate over the data table. Actual Error : "
                                + errString);
            }

        } else {
            reportresult(
                    true,
                    "DATA TABLE ITERATION :",
                    "FAILED",
                    "Cannot iterate over the data table. Invalid input data. Actual error : Number of tables passed : "
                            + tableNamesCount
                            + " does not match the associated data rows count : "
                            + rowsPerTableCounts);
            checkTrue(
                    false,
                    true,
                    "Cannot iterate over the data table. Invalid input data. Actual error : Number of tables passed : "
                            + tableNamesCount
                            + " does not match the associated data rows count : "
                            + rowsPerTableCounts);
        }
        return iteratorList;

    }

    /**
     * Gets the associated row numbers.
     * 
     * @param table
     *            the table
     * @param rowList
     *            the row list
     * @return the associated row numbers
     */
    private int[] getAssociatedRowNumbers(final DataTable table,
            final String rowList) {
        int[] rowNumbers = null;
        try {
            if (rowList.equalsIgnoreCase("ALL")) {
                int rowCount = table.getRowCount();
                rowNumbers = new int[rowCount];
                for (int i = 0; i < rowCount; i++) {
                    rowNumbers[i] = i;
                }
            } else if (rowList.contains("-")) {
                String[] range = rowList.split("-");
                int start = Integer.parseInt(range[0]);
                int end = Integer.parseInt(range[1]);
                rowNumbers = new int[((end - start)) + 1];
                int rowNumberIndex = 0;
                for (int i = start; i <= end; i++) {
                    rowNumbers[rowNumberIndex] = i;
                    rowNumberIndex++;
                }
            } else if (rowList.contains(",")) {
                String[] range = rowList.split(",");
                rowNumbers = new int[range.length];
                for (int i = 0; i < range.length; i++) {
                    rowNumbers[i] = Integer.parseInt(range[i]);
                }
            } else {
                rowNumbers = new int[] {Integer.parseInt(rowList)};
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            String errString = e.getMessage();
            reportresult(true, "DATA TABLE ITERATION :", "FAILED",
                    "Cannot iterate over the data table. Cannot parse the row numbers : "
                            + rowList + ". Actual Error : " + errString);
            checkTrue(false, true,
                    "Cannot iterate over the data table. Cannot parse the row numbers : "
                            + rowList + ". Actual Error : " + errString);
        } catch (IndexOutOfBoundsException e) {
            String errString = e.getMessage();
            reportresult(true, "DATA TABLE ITERATION :", "FAILED",
                    "Cannot iterate over the data table. Actual Error : "
                            + errString);
            checkTrue(false, true,
                    "Cannot iterate over the data table. Actual Error : "
                            + errString);
        }

        return rowNumbers;
    }

    /**
     * Retrieving all the browser titles opened.
     * 
     * @return the all windows
     * @throws Exception
     *             the exception
     */
    public final Set<String> getAllWindows() throws Exception {

        WebDriver driver = getDriver();
        try {
            // allData = new String[ (availableWindows.size() )];
            return driver.getWindowHandles();

        } catch (Exception e) {
            throw new Exception("cannot access the windows ", e);
        }
    }

    /**
     * Simulates the back button click event of the browser. <br>
     * 
     * The goBack command waits for the page to load after the navigation
     * 
     * @param waitTime
     *            : Time to wait for goBack command to complete
     * 
     * */
    public final void goBack(final String waitTime) {
        WebDriver driver = getDriver();
        try {
            driver.navigate().back();
            super.sleep(Integer.parseInt(waitTime));
            reportresult(true, "GO BACK :", "PASSED", "");
        } catch (Exception e) {
            String errorString = e.getMessage();
            reportresult(true, "GO BACK  :", "FAILED", errorString);
            checkTrue(false, true, "BROWSER BACK :" + "FAILED" + errorString);

        }
    }

    /**
     * Retrieves a String value previously stored.
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return String value stored for the given <b>key</b>
     */
    public final String retrieveString(final String key) {
        return retrieve(key, "String");
    }

    /**
     * Retrieve the value of a given key previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the given key is not stored previously</li> <li>
     * stored value type mismatches the type expected</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @param type
     *            : type of the previously stored value
     * @return value for the particular <b>key</b>
     */
    public final String retrieve(final String key, final String type) {

        String value = null;
        String projectPropertiesLocation = "project_data.properties";
        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(projectPropertiesLocation);
            try {
                prop.load(fis);
            } catch (IOException e) {
                String errorString = e.getMessage();
                reportresult(true, "RETRIEVE Value : " + type + " " + key
                        + " :", "FAILED", errorString);
                checkTrue(false, true, "RETRIEVE Value : " + type + " " + key
                        + " :" + "FAILED " + errorString);
            }

            value = prop.getProperty(key + "_Val");
            if (value != null) {
                String type2 = prop.getProperty(key + "_Type");
                if (!type2.equalsIgnoreCase(type)) {
                    // compare against stored type
                    reportresult(true, "RETRIEVE Value : " + type + " " + key
                            + " :", "FAILED", "Trying to retrieve " + type
                            + ", found" + type2);
                    checkTrue(false, true, "RETRIEVE Value : " + type + " "
                            + key + " :" + "FAILED " + "Trying to retrieve "
                            + type + ", found" + type2);
                }
            }

            reportresult(true, "RETRIEVE Value : " + type + " " + key + " :",
                    "PASSED", "Value = " + value);

        } catch (FileNotFoundException e) {

            reportresult(true, "RETRIEVE Value : " + type + " " + key + " :",
                    "PASSED", "RETRIEVE Value : " + type + " " + key + " :");
            return null;

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    /**
     * Retrieves an int value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to int</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return int value stored for the given <b>key</b> , default is -1
     */
    public final int retrieveInt(final String key) {
        String value = retrieve(key, "Int");
        try {
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            String errorString = e.getMessage();
            reportresult(true, "RETRIEVE Value : Int" + " " + key + " : ",
                    "FAILED", errorString);
            checkTrue(false, true, "RETRIEVE Value : Int " + " " + key + " : "
                    + "FAILED " + errorString);
        }
        return -1;
    }

    /**
     * Retrieves an double value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to int</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return int value stored for the given <b>key</b> , default is -1
     */
    public final double retrieveDouble(final String key) {
        String value = retrieve(key, "Double");
        try {
            if (value != null) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            String errorString = e.getMessage();
            reportresult(true, "RETRIEVE Value : Double" + " " + key + " : ",
                    "FAILED", errorString);
            checkTrue(false, true, "RETRIEVE Value : Double " + " " + key
                    + " : " + "FAILED " + errorString);
        }
        return -1;
    }

    /**
     * Retrieves a float value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to float</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return float value stored for the given <b>key</b> , default is -1
     */
    public final float retrieveFloat(final String key) {
        String value = retrieve(key, "Float");
        try {
            if (value != null) {
                return Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {
            String errorString = e.getMessage();
            reportresult(true, "RETRIEVE FLOAT: " + " " + key + " : ",
                    "FAILED", errorString);
            checkTrue(false, true, "RETRIEVE FLOAT: " + " " + key + " : "
                    + "FAILED " + errorString);
        }
        return -1;
    }

    /**
     * Retrieves a boolean value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to boolean</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return boolean value stored for the given <b>key</b> , default is false
     */
    public final boolean retrieveBoolean(final String key) {
        String value = retrieve(key, "Boolean");
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        } else {
            reportresult(true, "RETRIEVE BOOLEAN: " + " " + key + " : ",
                    "FAILED", "Cannot parse value to boolean");
            checkTrue(false, true, "RETRIEVE BOOLEAN: " + " " + key + " : "
                    + "FAILED " + "Cannot parse value to boolean");
            return false;
        }
    }

    /**
     * Stores a given key-value pair of given type <br>
     * Overwrites any existing value of same key <br>
     * <br>
     * <b>Fails</b> if, <li>data store file cannot be created</li> <li>data
     * cannot be written to file</li> <li>type of the value to be stored
     * mismatches the type specified</li> <br>
     * <br>
     * .
     * 
     * @param key
     *            : key for the value to be stored
     * @param type
     *            : type of value to be stored
     * @param objValue
     *            the obj value
     */
    public final void store(final String key, final String type,
            final Object objValue) {
        String value = checkNullObject(objValue, "STORE");
        String projectPropertiesLocation = "project_data.properties";
        Properties prop = new Properties();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File file = new File(projectPropertiesLocation);
        try {
            if (!file.exists() && !file.createNewFile()) {

                getLog().error(
                        "Cannot create a new file in the intended location. "
                                + "" + file.getAbsolutePath());
            }
            fis = new FileInputStream(file.getAbsoluteFile());
            prop.load(fis);
            prop.setProperty(key + "_Val", value);
            prop.setProperty(key + "_Type", type);

            checkStoreValueType(type, value);

            fos = new FileOutputStream(projectPropertiesLocation);
            prop.store(fos, "project settings");
            reportresult(true, "STORE Value : " + value + " " + type + " "
                    + key + " :", "PASSED", value);

        } catch (IOException e) {
            String errorString = e.getMessage();
            reportresult(true, "STORE :" + value + " : " + type + " : " + key
                    + " :", "FAILED", errorString);
            checkTrue(false, true, "STORE value " + value + " : " + type
                    + " : " + key + " :" + "FAILED " + errorString);
        } catch (NumberFormatException e) {
            String errorString = e.getMessage();
            reportresult(true, "STORE value : " + value + " Int" + " " + key
                    + " :", "FAILED", errorString);
            checkTrue(false, true, "STORE value " + value + " Int " + " " + key
                    + " :" + "FAILED " + errorString);
        } catch (IllegalArgumentException e) {
            String errorString = e.getMessage();
            reportresult(true, "STORE Value type : " + type + " " + key + " :",
                    "FAILED",
                    "Cannot parse value to the expected format. Error : "
                            + errorString);
            checkTrue(false, true, "STORE value type " + type + " " + key
                    + " :" + "FAILED "
                    + "Cannot parse value to the expected format. Error : "
                    + errorString);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Check store value type.
     * 
     * @param type
     *            the type
     * @param value
     *            the value
     */
    private void checkStoreValueType(final String type, final String value) {
        try {
            if ("Int".equalsIgnoreCase(type)) {
                Integer.parseInt(value);
            } else if ("Boolean".equalsIgnoreCase(type)) {
                if ("true".equalsIgnoreCase(value)
                        || "false".equalsIgnoreCase(value)) {
                    Boolean.parseBoolean(value);
                } else {
                    throw new IllegalArgumentException(
                            "Cannot convert to boolean value " + value);
                }
            } else if ("Float".equalsIgnoreCase(type)) {
                Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    // private boolean isPopupHandled;

    /**
     * Handle popup.
     * 
     * @param actionFlow
     *            the action flow
     * @param waitTime
     *            the wait time
     * @throws Exception
     *             the exception
     */
    public final void handlePopup(final String actionFlow, final String waitTime)
            throws Exception {
        initRobot();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.inputStringp = actionFlow;
        this.waitTimep = waitTime;

        Thread newThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    sleep(Integer.parseInt(waitTimep));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (inputStringp.startsWith("FORCE%")) {

                    forceHandlePopup(getRobot(), inputStringp.split("%")[1]);
                    reportresult(true, "HANDLE POPUP :" + inputStringp + "",
                            "PASSED", "");
                } else {
                    /*
                     * If the popup is not a forcrfully handled it will be
                     * handled in the normal way
                     */
                    String verificationErrors = "";
                    String actualAlertText = "";
                    WebDriver driver = getDriver();
                    String[] commands = inputStringp.split("\\|");
                    try {
                        actualAlertText = driver.switchTo().alert().getText();
                    } catch (NoAlertPresentException e) {

                        reportresult(true,
                                "HANDLE POPUP : failed. No Alert Present",
                                "FAILED", "");
                        checkTrue(false, true,
                                "HANDLE POPUP : failed. No Alert Present");
                    }

                    verificationErrors =
                            executeHandlePopupCommands(driver, commands,
                                    actualAlertText);
                    if (verificationErrors.isEmpty()) {
                        reportresult(true, "HANDLE POPUP :" + actualAlertText
                                + "", "PASSED", "");
                    } else {
                        reportresult(true, "HANDLE POPUP : failed", "FAILED",
                                "Errors : " + verificationErrors + "");
                        checkTrue(false, false,
                                "HANDLE POPUP : failed. Errors : "
                                        + verificationErrors + "");
                    }
                }

            }
        }

        );

        newThread.start();

    }

    /**
     * Execute handle popup commands.
     * 
     * @param driver
     *            the driver
     * @param commands
     *            the commands
     * @param actualAlertText
     *            the actual alert text
     * @return the string
     */
    private String executeHandlePopupCommands(final WebDriver driver,
            final String[] commands, final String actualAlertText) {

        StringBuilder verificationErrorBuilder = new StringBuilder();
        boolean isPopupHandled = false;
        for (String command : commands) {
            String commandString = command.toLowerCase(Locale.getDefault());
            if (commandString.startsWith("type=")) {
                String typeStr =
                        command.substring(command.indexOf('=') + 1,
                                command.length());
                driver.switchTo().alert().sendKeys(typeStr);

            } else if (commandString.startsWith("verify=")) {
                String verifyStr =
                        command.substring(command.indexOf('=') + 1,
                                command.length());
                if (!verifyStr.equals(actualAlertText)) {
                    verificationErrorBuilder
                            .append("VERIFY TEXT failed. Actual : " + ""
                                    + actualAlertText + " Expected : "
                                    + verifyStr + " ");
                }
            } else if (commandString.startsWith("action=")) {
                String actionStr =
                        command.substring(command.indexOf('=') + 1,
                                command.length());
                if ("ok".equalsIgnoreCase(actionStr)) {
                    driver.switchTo().alert().accept();
                    isPopupHandled = true;
                } else if ("cancel".equalsIgnoreCase(actionStr)) {
                    driver.switchTo().alert().dismiss();
                    isPopupHandled = true;
                }
            } else {
                verificationErrorBuilder
                        .append("Handle Popup command failed. Given input command ("
                                + command
                                + ")is not recognized. Supported commands : type, verify, action.");
            }
        }
        if (!isPopupHandled) {
            driver.switchTo().alert().accept();
            isPopupHandled = true;
        }
        return verificationErrorBuilder.toString();
    }

    /**
     * Insert new window handlesinto index.
     * 
     * @param currentWinHandles
     *            the current win handles
     */
    private void insertNewWindowHandlesintoIndex(
            final Set<String> currentWinHandles) {
        List<String> tempWindowHandleIndex = getOpenWindowHandleIndex();
        for (int i = 0; i < tempWindowHandleIndex.size(); i++) {
            String oldWinHandle = tempWindowHandleIndex.get(i);
            if (!currentWinHandles.contains(oldWinHandle)) {
                try {
                    tempWindowHandleIndex.remove(oldWinHandle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        setOpenWindowHandleIndex(tempWindowHandleIndex);
    }

    /**
     * Merge current windows with new window index.
     * 
     * @param currentWinHandles
     *            the current win handles
     */
    private void mergeCurrentWindowsWithNewWindowIndex(
            final Set<String> currentWinHandles) {

        List<String> tempOpenWindowHandles = getOpenWindowHandleIndex();
        for (String newWinHandle : currentWinHandles) {
            if (!tempOpenWindowHandles.contains(newWinHandle)) {
                try {
                    tempOpenWindowHandles.add(newWinHandle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 0; i < tempOpenWindowHandles.size(); i++) {
            String openWinHandle = tempOpenWindowHandles.get(i);
            if (!currentWinHandles.contains(openWinHandle)) {
                try {
                    tempOpenWindowHandles.remove(openWinHandle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        setOpenWindowHandleIndex(tempOpenWindowHandles);
    }

    /**
     * Removes the closed window handles from index.
     * 
     * @param currentWinHandles
     *            the current win handles
     */
    private void removeClosedWindowHandlesFromIndex(
            final Set<String> currentWinHandles) {

        List<String> tempAllWinHandles = getOpenWindowHandleIndex();
        currentWinHandles.removeAll(tempAllWinHandles);

        if (currentWinHandles.size() > 0) {
            for (String newWinHandle : currentWinHandles) {
                try {
                    tempAllWinHandles.add(newWinHandle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setOpenWindowHandleIndex(tempAllWinHandles);
        }
    }

    /**
     * Check for new window popups.
     * 
     * @throws Exception
     *             the exception
     */
    private void checkForNewWindowPopups() throws Exception {

        Set<String> currentWinHandles = getAllWindows();
        int currentWinHandleCount = currentWinHandles.size();
        int openWindowHandleCount = getOpenWindowHandleIndex().size();
        if (currentWinHandleCount < openWindowHandleCount) {

            insertNewWindowHandlesintoIndex(currentWinHandles);

        } else if (currentWinHandleCount == openWindowHandleCount) {

            mergeCurrentWindowsWithNewWindowIndex(currentWinHandles);

        } else if (currentWinHandleCount > openWindowHandleCount) {

            removeClosedWindowHandlesFromIndex(currentWinHandles);
        }
    }

    /**
     * Force handle popup.
     * 
     * @param robot
     *            the robot
     * @param inputString
     *            the input string
     */
    public final void forceHandlePopup(final Robot robot,
            final String inputString) {
        String[] commandSet = inputString.split("\\|");

        for (String fullCommand : commandSet) {
            sleep(retryInterval);
            int commandIndex = 0;
            int inputIndex = 1;
            String command = fullCommand.split("=")[commandIndex];
            String input = fullCommand.split("=")[inputIndex];
            if ("type".equalsIgnoreCase(command)) {

                StringSelection stringSelection = new StringSelection(input);
                clipboard.setContents(stringSelection, null);

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);

            } else if ("Key".equalsIgnoreCase(command)) {

                type(input);

            } else if ("wait".equalsIgnoreCase(command)) {

                super.sleep(Integer.parseInt(input));
            }

        }
    }

    /** The input stringp. */
    private String inputStringp;

    /** The wait timep. */
    private String waitTimep;

    /**
     * Type.
     * 
     * @param character
     *            the character
     */
    public final void type(final String character) {

        KeyCodes keys = new KeyCodes();
        doType(keys.getKeyCodes(character));
    }

    /**
     * Do type.
     * 
     * @param keyCodes
     *            the key codes
     */
    private void doType(final int... keyCodes) {
        doTypeKeys(keyCodes, 0, keyCodes.length);
    }

    /**
     * Do type.
     * 
     * @param keyCodes
     *            the key codes
     * @param offset
     *            the offset
     * @param length
     *            the length
     */
    private void doTypeKeys(final int[] keyCodes, final int offset,
            final int length) {
        if (length == 0) {
            return;
        }
        Robot robot = getRobot();
        try {
            robot.keyPress(keyCodes[offset]);
            doTypeKeys(keyCodes, offset + 1, length - 1);
            robot.keyRelease(keyCodes[offset]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Simulates a keypress in to an input field as though you typed it key by
     * key from the keyboard.<br>
     * The keys should be seperated form a | charater to be typed correctly.<br>
     * <br>
     * 
     * Example: A|B|C|ctrl|\n|\t|1|2|3 <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * @param value
     *            : value to be typed in the object
     */

    public final void keyPress(final String objectName,
            final String identifire, final Object value) {
        this.locatorIdentifire = identifire;
        keyPress(objectName, value);
        this.locatorIdentifire = "";
    }

    /**
     * Simulates a keypress in to an input field as though you typed it key by
     * key from the keyboard.<br>
     * The keys should be seperated form a | charater to be typed correctly.<br>
     * <br>
     * 
     * Example: A|B|C|ctrl|\n|\t|1|2|3 <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param objValue
     *            the obj value
     */

    public final void keyPress(final String objectName, final Object objValue) {

        String value = checkNullObject(objValue, "KEYPRESS");
        WebDriver driver = getDriver();
        int counter = getRetryCount();

        String[] valueStringsArr = value.split("\\|");

        // Getting the actual object identification from the object map
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    // Calling the actual command
                    element.sendKeys("");
                    Actions getFocus = new Actions(driver);
                    getFocus.moveToElement(element).build().perform();

                    for (int strLocation = 0; strLocation < valueStringsArr.length; strLocation++) {
                        if (!valueStringsArr[strLocation].isEmpty()) {
                            super.sleep(Integer.parseInt("1000"));
                            type(valueStringsArr[strLocation]);
                        }
                    }

                    reportresult(true, "KEYPRESS :" + objectName + "",
                            "PASSED", "Input Value = " + value);
                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);

                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "KEYPRESS :" + objectName + "",
                                "FAILED",
                                "KEYPRESS command cannot access :Element ("
                                        + objectName + ") [" + objectID
                                        + "] [Input Value = " + value + "]");
                        checkTrue(false, true,
                                "KEYPRESS command cannot access :Element ("
                                        + objectName + ") [" + objectID
                                        + "] [Input Value = " + value + "]");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            // if any exception was raised, report a test failure
            e.printStackTrace();
            reportresult(true, "KEYPRESS :" + objectName + "", "FAILED",
                    "KEYPRESS command  :Element (" + objectName + ") ["
                            + objectID + "] [Input Value = " + value
                            + "] not present");
            checkTrue(false, true, "KEYPRESS command  :Element (" + objectName
                    + ") [" + objectID + "] [Input Value = " + value
                    + "] not present");
        }
    }

    /**
     * Checks if a text in an element is according to a given pattern <br/>
     * . Can be used to check the value of labels, spans, inputs, etc.(Any
     * element which is containing inner text.)<br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param identifier
     *            the identifier
     * @param pattern
     * <br>
     *            For the pattern following format shouls be used.<br>
     * <br>
     *            For a uppercase string format should be <b>S</b><br>
     *            For a lowercase string format should be <b>s</b><br>
     *            For a digit format should be <b>d</b><br>
     *            For a special character the character should be entered as it
     *            is.<br>
     * <br>
     *            Ex 1:<br>
     *            For verifying a string like : abc-123#ABC <br>
     *            the pattern should be : sss-ddd#SSS <br>
     * <br>
     * 
     *            Ex 2:<br>
     *            For verifying a date : 12/March/2013 <br>
     *            the pattern should be : dd/Ssss/dddd <br>
     * <br>
     */

    public final void checkPattern(final String objectName,
            final String identifier, final String pattern,final boolean stopOnFailure, final Object... customError) {

        String actualLocator = ObjectMap.getObjectSearchPath(objectName,
                identifier);
        ObjectLocator locator = new ObjectLocator(objectName, identifier,
                actualLocator);
        doCheckPattern(locator, pattern, customError);
    }

    /**
     * Check pattern.
     * 
     * @param objectName
     *            the object name
     * @param pattern
     *            the pattern
     */
    public final void checkPattern(final String objectName, final String pattern, final boolean stopOnFailure,final Object... customError) {
        checkPattern(objectName, "", pattern,stopOnFailure,customError);
    }

    /*
     * public void checkPattern(final String objectName, final String
     * identifire, final String pattern) { this.identifire = identifire;
     * checkPattern(objectName, pattern); this.identifire = ""; }
     */

    /**
     * Checks if a text in an element is according to a given pattern <br/>
     * . Can be used to check the value of labels, spans, inputs, etc.(Any
     * element which is containing inner text.)<br>
     * 
     * @param locator
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param pattern
     * <br>
     * <br>
     *            <b>(Simplified Use)</b><br>
     * <br>
     *            For the pattern following format shouls be used.<br>
     * <br>
     *            For a uppercase string format should be <b>S</b><br>
     *            For a lowercase string format should be <b>s</b><br>
     *            For a digit format should be <b>d</b><br>
     *            For a special character the character should be entered as it
     *            is.<br>
     * <br>
     *            Ex 1:<br>
     *            For verifying a string like : abc-123#ABC <br>
     *            the pattern should be : sss-ddd#SSS <br>
     * <br>
     * 
     *            Ex 2:<br>
     *            For verifying a date : 12/March/2013 <br>
     *            the pattern should be : dd/Ssss/dddd <br>
     * <br>
     * 
     *            <b>(Advanced Use)</b><br>
     * <br>
     *            For advanced use the pure java regex pattern can be passed for
     *            the pattern. The regex pattern should have a prefix of
     *            'regex='<br>
     * <br>
     * 
     *            Ex 1:<br>
     *            For verifying a string like : abc-123#ABC <br>
     *            An example pattern will be :
     *            regex=[a-z][a-z][a-z]-\d\d\d#[A-Z][A-Z][A-Z]
     * 
     * <br>
     * <br>
     */

    private void doCheckPattern(final ObjectLocator locator,
            final String pattern, final Object... customError) {

        int counter = getRetryCount();
        String returnValue = "";

        String regex = getRegexPattern(pattern);

        // Getting the actual object identification from the object map
        String objectID = locator.getActualLocator();
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    // Calling the actual command
                    returnValue = element.getText().trim();
                    if (returnValue.matches(regex)) {
                        reportresult(true,
                                "CHECKPATTERN :" + locator.getLogicalName()
                                        + "Input Value = " + pattern, "PASSED",
                                "Input pattern : " + pattern);
                        break;
                    } else {
                        
                
                        if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                            

                            reportresult(
                                    true,
                                    "CHECKPATTERN :" + locator.getLogicalName()
                                            + "",
                                    "FAILED",
                                    " Custom Error :"+generateCustomError(customError)+" System generated Error : Checked regex pattern ["
                                            + pattern
                                            + "] is different from the actual value : ("
                                            + returnValue + ")");
                            checkTrue(false, true, " Custom Error :"+generateCustomError(customError)+" System generated Error : Checked regex pattern ["
                                    + pattern
                                    + "] is different from the actual value : ("
                                    + returnValue + ")");
                            break;
                            
                            
                        } else {

                            reportresult(
                                    true,
                                    "CHECKPATTERN :" + locator.getLogicalName()
                                            + "",
                                    "FAILED",
                                    "Checked regex pattern ["
                                            + pattern
                                            + "] is different from the actual value : ("
                                            + returnValue + ")");
                            checkTrue(false, true, "Checked regex pattern ["
                                    + pattern
                                    + "] is different from the actual value : ("
                                    + returnValue + ")");
                            break;
                        }
  
                    }

                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        String objectLogicalName = locator.getLogicalName();
                        
               
                        
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                            
                            reportresult(true, "CHECKPATTERN :" + objectLogicalName
                                    + "", "FAILED",
                                    " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECKPATTERN command cannot access :Element ("
                                            + objectLogicalName + ") [" + objectID
                                            + "] pattern = [" + pattern + "]");
                            checkTrue(false, true,
                                    " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECKPATTERN command cannot access :Element ("
                                            + objectLogicalName + ") [" + objectID
                                            + "] pattern = [" + pattern + "]");
                            
                            
                            
                        } else {
                            reportresult(true, "CHECKPATTERN :" + objectLogicalName
                                    + "", "FAILED",
                                    "CHECKPATTERN command cannot access :Element ("
                                            + objectLogicalName + ") [" + objectID
                                            + "] pattern = [" + pattern + "]");
                            checkTrue(false, true,
                                    "CHECKPATTERN command cannot access :Element ("
                                            + objectLogicalName + ") [" + objectID
                                            + "] pattern = [" + pattern + "]"); 
                        }
                        
                        
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            // if any exception was raised, report a test failure
            String objectLogicalName = locator.getLogicalName();
            e.printStackTrace();
 
                reportresult(true, "CHECKPATTERN :" + objectLogicalName + "",
                        "FAILED", "CHECKPATTERN command  :Element ("
                                + objectLogicalName + ") [" + objectID
                                + "] pattern = [" + pattern + "] not present");
                checkTrue(false, true, "CHECKPATTERN command  :Element ("
                        + objectLogicalName + ") [" + objectID + "] pattern = ["
                        + pattern + "] not present"); 
             
            
        }
    }

    /**
     * Gets the regex pattern.
     * 
     * @param patternString
     *            the pattern string
     * @return the regex pattern
     */
    private String getRegexPattern(final String patternString) {

        String regex = "";
        String pattern = patternString;
        if (pattern.toLowerCase(Locale.getDefault()).startsWith("regex=")) {
            pattern =
                    pattern.substring(pattern.indexOf('=') + 1,
                            pattern.length());
            regex = pattern;
        }
        if (pattern.toLowerCase(Locale.getDefault()).startsWith("regexp:")) {
            pattern = pattern.replaceAll("regexp:", "");
            regex = pattern;
        } else {
            char[] patternChars = pattern.toCharArray();
            StringBuilder regexBuilder = new StringBuilder();
            for (int strIndex = 0; strIndex < patternChars.length; strIndex++) {

                if (patternChars[strIndex] == 'S') {
                    regexBuilder.append("[A-Z]");
                } else if (patternChars[strIndex] == 's') {
                    regexBuilder.append("[a-z]");
                } else if (patternChars[strIndex] == 'd') {
                    regexBuilder.append("\\d");
                } else {
                    regexBuilder.append(patternChars[strIndex]);
                }
            }
            regex = regexBuilder.toString();
        }
        return regex;
    }

    /**
     * Adds a new comment line to the VTAF test manager report.<br>
     * Functions only with the VTAF test manager report<br>
     * <br>
     * 
     * @param objMessage
     *            the obj message
     */

    public final void writeToReport(final Object objMessage) {
        String message = checkNullObject(objMessage, "WRITE TO REPORT");

        FileOutputStream fos = null;
        try {
            Properties prop = getProp();
            fos = new FileOutputStream(getPropertiesLocation());
            prop.setProperty("tcComment", "\n" + message);
            prop.store(fos, null);
            reportresult(true, "WRITE TO REPORT : ", "PASSED", " [" + message
                    + "]");
        } catch (FileNotFoundException e) {
            reportresult(true, "WRITE TO REPORT : ", "FAILED", " [" + message
                    + "]");
            e.printStackTrace();
        } catch (IOException e) {
            reportresult(true, "WRITE TO REPORT : ", "FAILED", " [" + message
                    + "]");
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Simulates a user hovering a mouse over the specified element. <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public final void mouseOver(final String objectName, final String identifire) {

        this.locatorIdentifire = identifire;
        mouseOver(objectName);
        this.locatorIdentifire = "";
    }

    /**
     * Simulates a user hovering a mouse over the specified element. <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * 
     * */

    public final void mouseOver(final String objectName) {
        String objectID = "";
        int counter = getRetryCount();
        WebDriver driver = getDriver();
        try {
            // Retrieve the correct object locator from the object map
            objectID =
                    ObjectMap
                            .getObjectSearchPath(objectName, locatorIdentifire);
            // first verify whether the element is present in the current web
            // pagge
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following while loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exitted from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    Actions builder = new Actions(driver);
                    // call for selenium web driver command
                    builder.moveToElement(element).build().perform();
                    // if not exception is called consider and report the result
                    // as passed
                    reportresult(true, "MOUSE OVER :" + objectName + "",
                            "PASSED", "");
                    // if the testcase passed move out from the loop
                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "MOUSE OVER :" + objectName + "",
                                "FAILED",
                                "MOUSE OVER command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "MOUSE OVER command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {

            e.printStackTrace();
            /*
             * VTAF result reporter call
             */
            reportresult(true, "MOUSE OVER :" + objectName + "", "FAILED",
                    "MOUSE OVER command  :Element (" + objectName + ") ["
                            + objectID + "] not present");

            /*
             * VTAF specific validation framework reporting
             */
            checkTrue(false, true, "MOUSE OVER command  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    /**
     * Selects a frame within the current window. <br>
     * (You may invoke this command multiple times to select nested frames.) <br>
     * To select the parent frame, use "relative=parent" as a locator; to select
     * the top frame, use "relative=top". You can also select a frame by its
     * 0-based index number; select the first frame with "index=0", or the third
     * frame with "index=2". You may also use a DOM expression to identify the
     * frame you want directly, like this: dom=frames["main"].frames["subframe"]<br>
     * <br>
     * 
     * @param frameName
     *            the frame name
     * @param identifier
     *            the identifier
     */

    public final void selectFrame(final String frameName,
            final String identifier) {

        String actualLocator =
                ObjectMap.getObjectSearchPath(frameName, identifier);
        ObjectLocator locator =
                new ObjectLocator(frameName, identifier, actualLocator);
        doSelectFrame(locator);
    }

    /**
     * Select frame.
     * 
     * @param objectName
     *            the object name
     */
    public final void selectFrame(final String objectName) {
        selectFrame(objectName, "");
    }

    /**
     * Selects a frame within the current window. <br>
     * (You may invoke this command multiple times to select nested frames.) <br>
     * To select the parent frame, use "relative=parent" as a locator; to select
     * the top frame, use "relative=top". You can also select a frame by its
     * 0-based index number; select the first frame with "index=0", or the third
     * frame with "index=2". You may also use a DOM expression to identify the
     * frame you want directly, like this: dom=frames["main"].frames["subframe"]
     * 
     * @param locator
     *            the locator
     */

    private void doSelectFrame(final ObjectLocator locator) {
        int counter = getRetryCount();
        int frameIndex = -1;
        WebElement element = null;

        // Getting the actual object identification from the object map
        String objectID = locator.getActualLocator();
        String objectIDValue = objectID.toLowerCase(Locale.getDefault()).trim();
        try {
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {

                    counter--;
                    // Calling the actual command
                    TargetLocator target;
                    if (objectIDValue.startsWith("index=")) {
                        frameIndex =
                                Integer.parseInt(objectID.substring(
                                        objectID.indexOf('=') + 1,
                                        objectID.length()).trim());
                        target = getDriver().switchTo();
                        target.defaultContent();
                        target.frame(frameIndex);

                    } else if ("parent".equals(objectIDValue)
                            || "null".equals(objectIDValue)) {

                        target = getDriver().switchTo();
                        target.defaultContent();
                    } else {
                        checkForNewWindowPopups();
                        element = checkElementPresence(objectID);
                        target = getDriver().switchTo();
                        target.frame(element);
                    }

                    reportresult(true, "SELECT FRAME :" + locator + "",
                            "PASSED", "");
                    break;
                } catch (Exception e) {

                    sleep(retryInterval);

                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "SELECT FRAME :" + locator + "",
                                "FAILED",
                                "SELECT FRAME command cannot access :Frame ("
                                        + locator + ") [" + objectID + "]");
                        checkTrue(false, true,
                                "SELECT FRAME command cannot access :Frame ("
                                        + locator + ") [" + objectID + "]");
                    }
                }

            }
        } catch (Exception ex) {

            ex.printStackTrace();
            reportresult(true, "SELECT FRAME :" + locator + "", "FAILED",
                    "SELECT FRAME command  :Frame (" + locator + ") ["
                            + objectID + "] not present");
            checkTrue(false, true, "SELECT FRAME command  :Frame (" + locator
                    + ") [" + objectID + "] not present");

        }
        /*
         * END DESCRIPTION
         */
    }

    /**
     * Performs a Java robot click on the specific coordinates. <br>
     * 
     * @param resolution
     *            the resolution
     * @param coordinates
     *            the coordinates
     * @param waitTime
     *            the wait time
     * @throws Exception
     *             the exception
     */

    public final void mouseMoveAndClick(final String resolution,
            final String coordinates, final String waitTime) throws Exception {

        String res = resolution;
        final int f11KeyCode = KeyEvent.VK_F11;
        final int optimumPauseBetweenkeyCombs = 10;
        if (res.startsWith("prop=")) {

            String resolutionFromProp =
                    getExecProps().getProperty((res.split("prop=")[1]));
            if (resolutionFromProp != null) {
                res = resolutionFromProp;
            } else {
                reportresult(true, "MOUSE MOVE AND CLICK:", "FAILED",
                        "MOUSE MOVE AND CLICK command: Invalid property key value passed : "
                                + res);
                checkTrue(false, true,
                        "MOUSE MOVE AND CLICK command: Invalid property key value passed : "
                                + res);
            }
        }

        float screenWidht = 0;
        float screeHigt = 0;
        try {
            String[] resArr = res.split(",");
            screenWidht = Float.parseFloat(resArr[0]);
            screeHigt = Float.parseFloat(resArr[1]);
        } catch (Exception e) {

            getLog().error(e);
            reportresult(true, "MOUSE MOVE AND CLICK:", "FAILED",
                    "MOUSE MOVE AND CLICK command: Invalid input value passed for resolution : "
                            + res);
            checkTrue(false, true,
                    "MOUSE MOVE AND CLICK command: Invalid input value passed for resolution : "
                            + res);
        }

        String[] coordinatesArr = coordinates.split(",");
        float xCordinate = 0;
        float yCordinate = 0;
        try {
            xCordinate = Float.parseFloat(coordinatesArr[0]);
            yCordinate = Float.parseFloat(coordinatesArr[1]);
        } catch (Exception e) {

            getLog().error(e);
            reportresult(true, "MOUSE MOVE AND CLICK:", "FAILED",
                    "MOUSE MOVE AND CLICK command: Invalid input value passed for coordinates : "
                            + coordinates);
            checkTrue(false, true,
                    "MOUSE MOVE AND CLICK command: Invalid input value passed for coordinates : "
                            + coordinates);
        }
        String command = "";

        if (coordinatesArr.length > 2) {

            command = coordinatesArr[2];
        }

        Robot robot = new Robot();

        super.sleep(Integer.parseInt(waitTime));

        int xCordinateAutual = (int) calWidth(screenWidht, xCordinate);
        int yCordinateAutual = (int) calHight(screeHigt, yCordinate);

        robot.keyPress(f11KeyCode);
        robot.delay(optimumPauseBetweenkeyCombs);
        robot.keyRelease(f11KeyCode);
        sleep(retryInterval);

        // Mouse Move
        robot.mouseMove(xCordinateAutual, yCordinateAutual);

        // Click
        if ("".equals(command)) {

            robot.mousePress(InputEvent.BUTTON1_MASK);
            sleep(retryInterval);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            reportresult(true, "MOUSE MOVE AND CLICK : ", "PASSED",
                    "MOUSE MOVE AND CLICK command: Resolution : " + res);

        } else if ("dclick".equals(command.toLowerCase(Locale.getDefault()))) {

            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            final int optimumPauseBetweenDclick = 500;
            robot.delay(optimumPauseBetweenDclick);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);

            reportresult(true, "MOUSE MOVE AND DOUBLE CLICK : ", "PASSED",
                    "MOUSE MOVE AND DOUBLE CLICK command: Resolution: " + res);
            checkTrue(false, true, "MOUSE MOVE AND CLICK command: Resolution: "
                    + res);

        }

        robot.keyPress(f11KeyCode);
        robot.delay(optimumPauseBetweenkeyCombs);
        robot.keyRelease(f11KeyCode);
    }

    /**
     * Support method for mouseMoveAndClick Calculate the width of the test
     * runner PC.
     * 
     * @param oldSystemWidth
     *            the old system width
     * @param oldSystemX
     *            the old system x
     * @return the double
     */

    public static double calWidth(final double oldSystemWidth,
            final double oldSystemX) {
        double newSystemWidth = resizeScreen().width;
        return (oldSystemX / oldSystemWidth) * newSystemWidth;

    }

    /**
     * Support method for mouseMoveAndClick <br>
     * Calculate the height of the test runner PC.
     * 
     * @param oldSystemHigh
     *            the old system high
     * @param oldSystemY
     *            the old system y
     * @return the double
     */
    // High of the test runner PC
    public static double calHight(final double oldSystemHigh,
            final double oldSystemY) {
        double newSystemHigh = resizeScreen().height;
        return (oldSystemY / oldSystemHigh) * newSystemHigh;
    }

    /**
     * Support method for mouseMoveAndClick <br>
     * Resize the screen.
     * 
     * @return the dimension
     */
    private static Dimension resizeScreen() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    /**
     * Fires a native robot event into the webpage. <br>
     * 
     * @param event
     *            : Specicy the event which should be performed<br>
     *            1. If a keyboard event event should be started with KEY%<br>
     * <br>
     *            Ex: KEY%\n|\t<br>
     * <br>
     *            2. If it is a mouse event event should be started with MOUSE%<br>
     * <br>
     *            Ex: MOUSE%CLICK|RCLICK <br>
     * <br>
     * @param waittime
     *            : Wait time before the events.
     * */
    public final void fireEvent(final String event, final String waittime) {

        super.sleep(Integer.parseInt(waittime));
        /*
         * START DESCRIPTION following for loop was added to make the command
         * more consistent try the command for give amount of time (can be
         * configured through class variable RETRY) command will be tried for
         * "RETRY" amount of times or until command works. any exception thrown
         * within the tries will be handled internally.
         * 
         * can be exited from the loop under 2 conditions 1. if the command
         * succeeded 2. if the RETRY count is exceeded
         */
        try {
            setRobot(new Robot());
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (event.startsWith("KEY%")) {

                fireKeyEvent(event.split("%")[1]);
            } else if (event.startsWith("MOUSE%")) {

                fireMouseEvent(event.split("%")[1]);
            } else if (event.startsWith("VERIFY%")) {

                fireEventVerifyValue(event.split("%")[1]);
            } else {
                reportresult(true, "FIRE EVENT :", "FAILED",
                        "Invalid event type passed :" + event);
                checkTrue(false, true, "Invalid event type passed :" + event);
            }

            reportresult(true, "FIRE EVENT Command : ", "PASSED",
                    "Input Events = " + event);
        } catch (Exception e) {

            if (e.getMessage().startsWith("Command")) {
                e.printStackTrace();
                reportresult(true, "FIRE EVENT :", "FAILED",
                        "FIRE EVENT passed command is invalid (" + event + ")");
                checkTrue(false, true, "FIRE EVENT passed command is invalid ("
                        + event + ") ");
            } else {
                e.printStackTrace();
                reportresult(true, "FIRE EVENT Command:", "FAILED",
                        "FIRE EVENT command cannot perform the event (" + event
                                + ")");
                checkTrue(false, true,
                        "FIRE EVENT command cannot cannot perform the event ("
                                + event + ") ");

            }
        }
    }

    /**
     * Get the selected text in webpage to the clipboard and compare the value
     * with the given input.
     * 
     * @param value
     *            the value
     * @throws Exception
     *             the exception
     */

    private void fireEventVerifyValue(final String value) throws Exception {

        String clipBoardText = "";
        Robot robot = getRobot();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        sleep(retryInterval);
        Transferable trans =
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .getContents(null);

        try {
            if (trans != null) {
                clipBoardText =
                        (String) trans.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (clipBoardText.equals(value)) {

            reportresult(true, "FIRE EVENT : VERIFY VALUE " + value + "",
                    "PASSED", "");
        } else {

            reportresult(true, "FIRE EVENT : VERIFY VALUE " + value + "",
                    "FAILED",
                    "FIRE EVENT : VERIFY VALUE : value match expected. Actual : "
                            + clipBoardText + " Expected : " + value + "");
            checkTrue(false, true,
                    "FIRE EVENT : VERIFY VALUE : value match expected. Actual : "
                            + clipBoardText + " Expected : " + value + "");
        }
    }

    /**
     * Fires a set of java robot key events into the webpage.
     * 
     * @param commands
     *            the commands
     * @throws Exception
     *             the exception
     */

    private void fireKeyEvent(final String commands) throws Exception {

        String[] commandSet = commands.split("\\|");
        Robot robot = getRobot();
        for (String fullCommand : commandSet) {
            sleep(retryInterval / 2);
            int commandIndex = 0;
            int inputIndex = 1;
            String command = fullCommand.split("=")[commandIndex];
            String input = fullCommand.split("=")[inputIndex];
            if ("type".equalsIgnoreCase(command)) {

                StringSelection stringSelection = new StringSelection(input);
                clipboard.setContents(stringSelection, null);

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);

            } else if ("Key".equalsIgnoreCase(command)) {

                type(input);
            } else if ("wait".equalsIgnoreCase(command)) {

                super.sleep(Integer.parseInt(input));
            } else {
                throw new Exception("Command " + command);
            }
        }
    }

    /**
     * Fires a set of java robot mouse events into the webpage.
     * 
     * @param commands
     *            the commands
     * @throws Exception
     *             the exception
     */

    private void fireMouseEvent(final String commands) throws Exception {

        String[] commandSet = commands.split("\\|");
        Robot robot = getRobot();
        final int optimumPauseBetweenKeyCombs = 10;
        final int f11KeyCode = KeyEvent.VK_F11;
        for (String fullCommand : commandSet) {
            sleep(retryInterval);
            int commandIndex = 0;
            int inputIndex = 1;
            String command = fullCommand.split("=")[commandIndex];
            String input = fullCommand.split("=")[inputIndex];

            if ("MOVE".equalsIgnoreCase(command)) {

                String[] coords = input.split(",");
                int resolutionWidth = Integer.parseInt(coords[0]);
                int resolutionHeight = Integer.parseInt(coords[inputIndex]);
                int x = Integer.parseInt(coords[inputIndex + 1]);
                int y = Integer.parseInt(coords[inputIndex + 2]);

                int xCordinateAutual = (int) calWidth(resolutionWidth, x);
                int yCordinateAutual = (int) calHight(resolutionHeight, y);

                robot.keyPress(f11KeyCode);
                robot.delay(optimumPauseBetweenKeyCombs);
                robot.keyRelease(f11KeyCode);
                sleep(retryInterval);

                // Mouse Move
                robot.mouseMove(xCordinateAutual, yCordinateAutual);

                robot.keyPress(f11KeyCode);
                sleep(optimumPauseBetweenKeyCombs);
                robot.keyRelease(f11KeyCode);

            } else if ("SCROLL".equalsIgnoreCase(command)) {

                robot.mouseWheel(Integer.parseInt(input));

            } else if ("wait".equalsIgnoreCase(command)) {

                super.sleep(Integer.parseInt(input));
            } else {
                throw new Exception("Command " + command);
            }
        }
    }

    /**
     * Switch between diffent users by user name.
     * 
     * @param instanceName
     *            the instance name
     */
    public final void switchUser(final String instanceName) {

        boolean isNewInstance;
        if (getSeleniumInstances().containsKey(instanceName)) {
            isNewInstance = false;
        } else {
            isNewInstance = true;
        }
        if (!isNewInstance) {
            setDriver(getSeleniumInstances().get(instanceName));
            reportresult(true, "SWITCH USER :" + instanceName + "", "PASSED",
                    "SWITCH USER command :Current user changed to New User("
                            + instanceName + ")");
        } else {
            reportresult(true, "SWITCH USER :" + instanceName + "", "FAILED",
                    "SWITCH USER command : User (" + instanceName
                            + ") is not created. ");
            checkTrue(false, true, "SWITCH USER command : User ("
                    + instanceName + ") is not created. ");
        }

    }

    /**
     * Creates a new user profile and launches a seperate browser session for
     * the user.
     * 
     * @param instanceName
     *            the instance name
     * @param browser
     *            the browser
     * @param serverConfig
     *            the server config
     */

    public final void createUser(final String instanceName,
            final String browser, final String serverConfig) {

        boolean isNewInstance;
        if (getSeleniumInstances().containsKey(instanceName)) {
            isNewInstance = false;
        } else {
            isNewInstance = true;
        }

        if (isNewInstance) {

            try {
                setUp(instanceName, browser, serverConfig);
                startBrowserSession(browser);
                startOfTestCase();
                reportresult(true, "CREATE USER :" + instanceName + "",
                        "PASSED", "CREATE USER command : User (" + instanceName
                                + ") is Created. ");
            } catch (IllegalArgumentException e) {

                String errorString = e.getMessage();

                reportresult(
                        true,
                        "CREATE USER :" + instanceName + "",

                        "FAILED",

                        "CREATE USER command : Error occured while confuguring selenium with given configuration. Error : "

                                + errorString);

                checkTrue(
                        false,
                        true,

                        "CREATE USER command : Error occured while confuguring selenium with given configuration. Error : "

                                + errorString);

            }

            catch (Exception e) {
                String errorString = e.getMessage();
                reportresult(true, "CREATE USER :" + instanceName + "",
                        "FAILED",
                        "CREATE USER command : Error occured while invoking the new user. Error : "
                                + errorString);
                checkTrue(false, true,
                        "CREATE USER command : Error occured while invoking the new user. Error : "
                                + errorString);
            }

        } else {
            reportresult(true, "CREATE USER :" + instanceName + "", "FAILED",
                    "CREATE USER command : User (" + instanceName
                            + ") is already available. ");
            checkTrue(false, true, "CREATE USER command : User ("
                    + instanceName + ") is already available. ");
        }

    }

    /**
     * Checks the properties of an browser property given by the property name
     * is exists if the property does not exists, further continuation of the
     * script execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page.
     * 
     * Property name : WINDOWPRESENT <br>
     * Check if the window is present
     * 
     * @param windowName
     *            : object name alias given by the user.
     * @param identifier
     *            the identifier
     * @param propertyname
     *            : Name of the object property
     * @param objExpectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     */
    public final void checkWindowProperty(final String windowName,
            final String identifier, final String propertyname,
            final Object objExpectedvale, final boolean stopOnFailure, final Object... customError) {

        this.locatorIdentifire = identifier;
        checkWindowProperty(windowName, propertyname, objExpectedvale,
                stopOnFailure, customError);
        this.locatorIdentifire = "";
    }

    /**
     * Checks the properties of an browser property given by the property name
     * is exists if the property does not exists, further continuation of the
     * script execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br/>
     * <br>
     * in the web page.
     * 
     * Property name : WINDOWPRESENT <br>
     * Check if the window is present
     * 
     * @param windowName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param objExpectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     * */

    public final void checkWindowProperty(final String windowName,
            final String propertyname, final Object objExpectedvale,
            final boolean stopOnFailure, final Object... customError) {

        if (propertyname.equals(WindowValidationType.WINDOWPRESENT.toString())) {

            String expectedvale =
                    checkNullObject(objExpectedvale, "CHECK OBJECT PROPERTY");
            checkWindowPresent(windowName, propertyname, expectedvale,
                    stopOnFailure, customError);
        }

    }

    /**
     * Check if the window is present in the context.
     * 
     * @param windowName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param expectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     * */

    private void checkWindowPresent(final String windowName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure, final Object [] customError) {

        int counter = getRetryCount();
        boolean objectFound = false;
        WebDriver driver = getDriver();
        // String windowiden = "";

        // Getting the actual object identification from the object map
        String window =
                ObjectMap.getObjectSearchPath(windowName, locatorIdentifire);
        try {
            checkForNewWindowPopups();

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    String currentWinHandle = driver.getWindowHandle();
                    String targetWindow =
                            getMatchingWindowFromCurrentWindowHandles(driver,
                                    window);
                    driver.switchTo().window(currentWinHandle);
                    if (expectedvale.equalsIgnoreCase(String
                            .valueOf(targetWindow != null))) {

                        reportresult(true, "CHECK WINDOW PROPERTY:"
                                + propertyname + "", "PASSED", "");
                    } else {
                        
                        /*String customErrorMessage = "";
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                            
                              for (int i=0;i<customError.length;i++){
                                  customErrorMessage = customErrorMessage+customError[i].toString();
                              }
                              System.out.println(customErrorMessage);
                        }*/
                        
                       if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                              reportresult(true, "CHECK WINDOW PROPERTY:"
                                      + propertyname + "", "FAILED",
                                      " Custom Error :"+generateCustomError(customError)+" System generated Error : Expected Property : " + propertyname
                                              + " expected value [ " + expectedvale
                                              + " ]does not match the actual ["
                                              + objectFound + "] for the window ["
                                              + windowName + "] [" + window + "]");
                              checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError(customError)+" System generated Error : Expected Property : " + propertyname
                                      + " expected value [ " + expectedvale
                                      + " ]does not match the actual ["
                                      + objectFound + "] for the window ["
                                      + windowName + "] [" + window + "]");
                              
                              
                        } else {

                            reportresult(true, "CHECK WINDOW PROPERTY:"
                                    + propertyname + "", "FAILED",
                                    "Expected Property : " + propertyname
                                            + " expected value [ " + expectedvale
                                            + " ]does not match the actual ["
                                            + objectFound + "] for the window ["
                                            + windowName + "] [" + window + "]");
                            checkTrue(false, stopOnFailure, "Expected Property : "
                                    + propertyname + " expected value [ "
                                    + expectedvale
                                    + " ]does not match the actual [" + objectFound
                                    + "] for the window [" + windowName + "] ["
                                    + window + "]"); 
                        }
                        
                        
                    }
                    break;
                } catch (WebDriverException ex) {
                    sleep(retryInterval);
                    if (!(counter > 0)) {
                        reportresult(true, "CHECK WINDOW PROPERTY:"
                                + propertyname + "", "FAILED",
                                "CHECK WINDOW PROPERTY  :Window (" + windowName
                                        + ") [" + window
                                        + "] is not accessible");
                        checkTrue(false, stopOnFailure,
                                "CHECK WINDOW PROPERTY  :Window (" + windowName
                                        + ") [" + window
                                        + "] is not accessible");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // if any exception is raised, report failure
            reportresult(true, "CHECK WINDOW PROPERTY:" + propertyname + "",
                    "FAILED", "CHECK WINDOW PROPERTY  :Window (" + windowName
                            + ") [" + window + "] is not accessible");
            checkTrue(false, stopOnFailure, "CHECK WINDOW PROPERTY  :Window ("
                    + windowName + ") [" + window + "] is not accessible");

        }

    }

    /**
     * Gets the db table.
     * 
     * @param instanceName
     *            the instance name
     * @param query
     *            the query
     * @return the DB table
     * @throws Exception
     *             the exception
     */
    private ArrayList<Object> getDBTable(final String instanceName,
            final String query) throws Exception { // private

        ArrayList<Object> arrList = new ArrayList<Object>();
        ResultSet result = null;
        Connection con = null;
        PreparedStatement preparedStmt = null;
        con = getDatabaseInstances().get(instanceName);
        if (con == null) {

            throw new Exception("Connection instance unavaliable "
                    + instanceName);
        }
        try {
            preparedStmt = con.prepareStatement(query);
            result = preparedStmt.executeQuery();
            ResultSetMetaData md = result.getMetaData();
            int count = md.getColumnCount();

            while (result.next()) {

                for (int i = 1; i <= count; i++) {
                    arrList.add(result.getObject(i));
                }
            }
            if (arrList.isEmpty()) {
                throw new NullPointerException(
                        "Empty Result set for the query :- " + query);
            }
        } finally {
            if (preparedStmt != null) {
                preparedStmt.close();
            }
        }
        return arrList;

    }

    /**
     * Check db results.
     * 
     * @param instanceName
     *            the instance name
     * @param query
     *            the query
     * @param expectedValue
     *            the expected value
     * @param stopOnFaliure
     *            the stop on faliure
     */
    public final void checkDBResults(final String instanceName,
            final String query, final String expectedValue,
            final boolean stopOnFaliure, final Object... customError) {

        ArrayList<Object> objArrList;
        ArrayList<String> inputTable;
        ArrayList<String> strArrList = new ArrayList<String>();
        try {
            objArrList = getDBTable(instanceName, query);

            inputTable =
                    new ArrayList<String>(Arrays.asList(expectedValue
                            .split("(?<!\\\\),")));
            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputTable) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputTable = tempInputTable;
            for (Object obj : objArrList) {
                strArrList.add(obj.toString());
            }
            String inputTableStr = StringUtils.join(inputTable, "|");
            String actualTableStr = StringUtils.join(strArrList, "|");
            if (actualTableStr.contains(inputTableStr)) {

                reportresult(true, "CHECK DB RESULTS : ", "PASSED",
                        "For Query = " + query + " :EXPECTED Value = "
                                + expectedValue);

            } else {

                String inputTableAllString = inputTable.toString();
                String objetcArrayAllString = objArrList.toString();
                
                /*String customErrorMessage = "";
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    
                      for (int i=0;i<customError.length;i++){
                          customErrorMessage = customErrorMessage+customError[i].toString();
                      }
                      System.out.println(customErrorMessage);
                }*/
                
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                      reportresult(stopOnFaliure, "CHECK DB RESULTS : ", "FAILED",
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : For Query = " + query
                                      + " , TABLEDATA is not as expected  "
                                      + inputTableAllString + ": Actual :"
                                      + objetcArrayAllString);
                      checkTrue(false, stopOnFaliure, " Custom Error :"+generateCustomError(customError)+" System generated Error : For Query = " + query
                              + " , TABLEDATA is not as expected  "
                              + inputTableAllString + ": Actual :"
                              + objetcArrayAllString);
                      
                } else {
                    reportresult(stopOnFaliure, "CHECK DB RESULTS : ", "FAILED",
                            "For Query = " + query
                                    + " , TABLEDATA is not as expected  "
                                    + inputTableAllString + ": Actual :"
                                    + objetcArrayAllString);
                    checkTrue(false, stopOnFaliure, "For Query = " + query
                            + " , TABLEDATA is not as expected  "
                            + inputTableAllString + ": Actual :"
                            + objetcArrayAllString);
                }
                
                
            }

        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            reportresult(stopOnFaliure, "CHECK DB RESULTS :", "FAILED",
                    "SQL Error occured" + errorMsg);
            checkTrue(false, stopOnFaliure, "SQL Error occured" + errorMsg);

        } catch (NullPointerException e) {
            String errorMsg = e.getMessage();
            
            /*String customErrorMessage = "";
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                
                  for (int i=0;i<customError.length;i++){
                      customErrorMessage = customErrorMessage+customError[i].toString();
                  }
                  System.out.println(customErrorMessage);
            }*/
            
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                  reportresult(stopOnFaliure, "CHECK DB RESULTS :", "FAILED","Custom Error :"+generateCustomError(customError)+" System generated Error :"+
                          errorMsg);
                  checkTrue(false, stopOnFaliure, "Custom Error :"+generateCustomError(customError)+" System generated Error :"+
                          errorMsg);
                  
                  
            } else {
                reportresult(stopOnFaliure, "CHECK DB RESULTS :", "FAILED",
                        errorMsg);
                checkTrue(false, stopOnFaliure, errorMsg);
            }

           
        } catch (Exception e) {
            if (e.getMessage().startsWith("Connection instance unavaliable")) {
                /*String customErrorMessage = "";
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    
                      for (int i=0;i<customError.length;i++){
                          customErrorMessage = customErrorMessage+customError[i].toString();
                      }
                      System.out.println(customErrorMessage);
                }*/
                
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                      reportresult(true, "CHECK DB RESULTS :" + instanceName + "",
                              "FAILED", " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK DB RESULTS command : connection ("
                                      + instanceName + ") is not created. ");
                      checkTrue(false, true,
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK DB RESULTS command : connection ("
                                      + instanceName + ") is not created. ");
     
                } else {
                    reportresult(true, "CHECK DB RESULTS :" + instanceName + "",
                            "FAILED", "CHECK DB RESULTS command : connection ("
                                    + instanceName + ") is not created. ");
                    checkTrue(false, true,
                            "CHECK DB RESULTS command : connection ("
                                    + instanceName + ") is not created. ");
                }
                
                
                
                
                
            } else {
                String errorMsg = e.getMessage();
                reportresult(stopOnFaliure, "CHECK DB RESULTS :", "FAILED",
                        errorMsg);
                checkTrue(false, stopOnFaliure, errorMsg);
            }
        }

    }

    /**
     * Gets the string db result.
     * 
     * @param instanceName
     *            the instance name
     * @param query
     *            the query
     * @return the string db result
     */
    public final String getStringDBResult(final String instanceName,
            final String query) {

        ArrayList<Object> arrList;
        String value = null;
        try {
            arrList = getDBTable(instanceName, query);
            value = arrList.get(0).toString();
            if (arrList.size() >= 2) {
                reportresult(
                        true,
                        "SET DB RESULTS : ",
                        "PASSED",
                        "For Query = "
                                + query
                                + " Actual result contains more than one value. Actual Values :- "
                                + arrList + " Return Value :- " + value);
            }
            reportresult(true, "SET DB RESULTS : ", "PASSED", "For Query = "
                    + query);
        } catch (SQLException e) {
            String errorString = e.getMessage();
            reportresult(true, "SET DB RESULTS :", "FAILED",
                    "SQL Error occured" + errorString);
            checkTrue(false, true, "SQL Error occured" + errorString);
        } catch (NullPointerException e) {
            String errorString = e.getMessage();
            reportresult(true, "SET DB RESULTS :", "FAILED", errorString);
            checkTrue(false, false, errorString);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Connection instance unavaliable")) {
                reportresult(true, "SET DB RESULTS :" + instanceName + "",
                        "FAILED", "SET DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
                checkTrue(false, true, "SET DB RESULTS command : connection ("
                        + instanceName + ") is not created. ");
            } else {
                String errorString = e.getMessage();
                reportresult(true, "SET DB RESULTS :", "FAILED", errorString);
                checkTrue(false, false, errorString);
            }
        }
        return value;
    }

    /**
     * Gets the int db result.
     * 
     * @param instanceName
     *            the instance name
     * @param query
     *            the query
     * @return the int db result
     */
    public final Integer getIntDBResult(final String instanceName,
            final String query) {

        ArrayList<Object> arrList;
        Integer value = null;
        try {
            arrList = getDBTable(instanceName, query);
            if (!(arrList.get(0) instanceof Integer)) {
                throw new NumberFormatException("The value trying to retrive ("
                        + arrList.get(0).toString()
                        + ") is not stored as an interger in the database.");
            }
            value = (Integer) arrList.get(0);
            reportresult(true, "SET DB RESULTS : ", "PASSED", "For Query = "
                    + query);
        } catch (SQLException e) {
            String errorString = e.getMessage();
            reportresult(true, "SET DB RESULTS :", "FAILED",
                    "SQL Error occured" + errorString);
            checkTrue(false, true, "SQL Error occured" + errorString);
        } catch (NullPointerException e) {
            String errorString = e.getMessage();
            reportresult(true, "SET DB RESULTS :", "FAILED", errorString);
            checkTrue(false, false, errorString);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Connection instance unavaliable")) {
                reportresult(true, "SET DB RESULTS :" + instanceName + "",
                        "FAILED", "SET DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
                checkTrue(false, true, "SET DB RESULTS command : connection ("
                        + instanceName + ") is not created. ");
            } else {
                String errorString = e.getMessage();
                reportresult(true, "SET DB RESULTS :", "FAILED", errorString);
                checkTrue(false, false, errorString);
            }
        }
        return value;
    }

    /**
     * Gets the boolean db result.
     * 
     * @param instanceName
     *            the instance name
     * @param query
     *            the query
     * @return the boolean db result
     */
    public final Boolean getBooleanDBResult(final String instanceName,
            final String query) {

        ArrayList<Object> arrList;
        Boolean value = null;
        try {
            arrList = getDBTable(instanceName, query);
            if (!(arrList.get(0) instanceof Boolean)) {
                throw new Exception("The value trying to retrive ("
                        + arrList.get(0).toString()
                        + ") is not stored as a boolean in the database.");
            }

            value = (Boolean) arrList.get(0);
            reportresult(true, "SET DB RESULTS : ", "PASSED", "For Query = "
                    + query);
        } catch (SQLException e) {
            String errorString = e.getMessage();
            reportresult(true, "SET DB RESULTS :", "FAILED",
                    "SQL Error occured" + errorString);
            checkTrue(false, true, "SQL Error occured" + errorString);
        } catch (NullPointerException e) {
            String errorString = e.getMessage();
            reportresult(true, "SET DB RESULTS :", "FAILED", errorString);
            checkTrue(false, false, errorString);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Connection instance unavaliable")) {
                reportresult(true, "SET DB RESULTS :" + instanceName + "",
                        "FAILED", "SET DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
                checkTrue(false, true, "SET DB RESULTS command : connection ("
                        + instanceName + ") is not created. ");
            } else {
                String errorString = e.getMessage();
                reportresult(true, "SET DB RESULTS :", "FAILED", errorString);
                checkTrue(false, false, errorString);
            }
        }
        return value;
    }

    /**
     * Creates the db connection.
     * 
     * @param databaseType
     *            the database type
     * @param instanceName
     *            the instance name
     * @param url
     *            the url
     * @param username
     *            the username
     * @param password
     *            the password
     */
    public final void createDBConnection(final String databaseType,
            final String instanceName, final String url, final String username,
            final String password) {
        Connection con = null;
        boolean isNewInstance;

        if (getDatabaseInstances().containsKey(instanceName)) {
            isNewInstance = false;
        } else {
            isNewInstance = true;
        }

        if (isNewInstance) {

            try {
                if ("mysql".equalsIgnoreCase(databaseType)) {
                    String dbClass = "com.mysql.jdbc.Driver";
                    Class.forName(dbClass).newInstance();
                    con = DriverManager.getConnection(url, username, password);
                } else if ("oracle".equalsIgnoreCase(databaseType)) {
                    DriverManager
                            .registerDriver(new oracle.jdbc.driver.OracleDriver());
                    con = DriverManager.getConnection(url, username, password);
                } else if ("mssql".equalsIgnoreCase(databaseType)) {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    con = DriverManager.getConnection(url, username, password);
                } else if (databaseType.isEmpty()) {
                    reportresult(true, "CREATE DB CONNECTION :", "FAILED",
                            "Database type not selected");
                    checkTrue(false, true,
                            "CREATE DB CONNECTION command: Error :- No database type selected.");
                }
                putDatabaseInstances(instanceName, con);
                reportresult(true, "CREATE DB CONNECTION :", "PASSED",
                        "Instance name : " + instanceName + " DB URL : " + url
                                + "");
            } catch (SQLException e) {

                e.printStackTrace();
                reportresult(true, "CREATE DB CONNECTION :", "FAILED",
                        e.getMessage());
                checkTrue(
                        false,
                        true,
                        "CREATE DB CONNECTION command: Error :- "
                                + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                reportresult(true, "CREATE DB CONNECTION :", "FAILED",
                        e.getMessage());
                checkTrue(
                        false,
                        true,
                        "CREATE DB CONNECTION command: Error :- "
                                + e.getMessage());
            }

        }

    }

    /**
     * Checks if is image present.
     * 
     * @param path
     *            the path
     * @param isRotatable
     *            the is rotatable
     * @return the screen region
     * @throws FileNotFoundException
     *             throws if file is not avaliable
     */
    private ScreenRegion isImagePresent(final String path,
            final boolean isRotatable) throws FileNotFoundException {

        PropertyHandler propHandler = new PropertyHandler("runtime.properties");
        double maxRecQuality =
                Double.parseDouble(propHandler
                        .getRuntimeProperty("MAX_REG_QUALITY"));

        int rotationDegree =
                Integer.parseInt(propHandler
                        .getRuntimeProperty("ROTATION_DEGREE"));

        int retry = getRetryCount();
        double regQuality = maxRecQuality;
        Target target;
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        javaxt.io.Image img = new javaxt.io.Image(path);
        ScreenRegion targetRegion = null;
        while (retry > 0) {
            retry--;
            ScreenRegion s = new DesktopScreenRegion();
            target = new ImageTarget(img.getBufferedImage());
            target.setMinScore(regQuality);
            targetRegion = s.find(target);
            if (targetRegion == null) {
                sleep(retryInterval);
                if (isRotatable) {
                    img.rotate(rotationDegree);
                }
            } else {
                break;
            }
        }

        return targetRegion;
    }

    /**
     * Handle image popup.
     * 
     * @param imagePath
     *            the image path
     * @param actionFlow
     *            the action flow
     * @param waitTime
     *            the wait time
     * @throws Exception
     *             the exception
     */
    public final void handleImagePopup(final String imagePath,
            final String actionFlow, final String waitTime) throws Exception {
        initRobot();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.inputStringp = actionFlow;
        this.waitTimep = waitTime;

        Thread newThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    sleep(Integer.parseInt(waitTimep));

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    ScreenRegion targetImage = isImagePresent(imagePath, false);
                    if (targetImage != null) {
                        forceHandlePopup(getRobot(), inputStringp);
                        reportresult(true, "HANDLE IMAGE POPUP :", "PASSED", "");
                    }
                } catch (FileNotFoundException f) {

                    reportresult(true, "HANDLE IMAGE POPUP :", "FAILED",
                            "File not found in the given location.");
                    checkTrue(true, true,
                            "HANDLE IMAGE POPUP command: Error :- File not found in the given location.");
                } catch (Exception e) {
                    e.printStackTrace();
                    reportresult(true, "HANDLE IMAGE POPUP :", "FAILED",
                            e.getMessage());
                    checkTrue(
                            false,
                            true,
                            "HANDLE IMAGE POPUP command: Error :- "
                                    + e.getMessage());
                }

            }
        }

        );

        newThread.start();

    }

    /**
     * Check image present.
     * 
     * @param path
     *            the path
     * @param isRotatable
     *            the is rotatable
     * @return true, if check image present
     */
    public final boolean checkImagePresent(final String path,
            final boolean isRotatable) {

        try {

            return isImagePresent(path, isRotatable) != null;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {

            return false;
        }

    }

    /**
     * Check image present.
     * 
     * @param locator
     *            the path
     * @param isRotatable
     *            the is rotatable
     * @param stopOnFailure
     *            the stop on failure
     */
    public final void doCheckImagePresent(final ObjectLocator locator,
            final boolean isRotatable, final boolean stopOnFailure, final Object [] customError) {

        String path = "";
        // load the actual object id from the OR
        path = locator.getActualLocator();
        boolean isElementPresent = false;
        try {

            isElementPresent = isImagePresent(path, isRotatable) != null;
            if (isElementPresent) {
                reportresult(true, "CHECK IMAGE PRESENT:", "PASSED", path);

            } else {
         
                
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                      reportresult(true, "CHECK IMAGE PRESENT:", "FAILED",
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK IMAGE PRESENT command cannot find the image :- "
                                      + path + " in current screen.");
                      checkTrue(false, stopOnFailure,
                              " Custom Error :"+generateCustomError(customError)+" System generated Error : CHECK IMAGE PRESENT command cannot find the image :- "
                                      + path + " in current screen.");

                } else {

                    reportresult(true, "CHECK IMAGE PRESENT:", "FAILED",
                            "CHECK IMAGE PRESENT command cannot find the image :- "
                                    + path + " in current screen.");
                    checkTrue(false, stopOnFailure,
                            "CHECK IMAGE PRESENT command cannot find the image :- "
                                    + path + " in current screen.");

                }
                
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            
  
            
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                  reportresult(true, "CHECK IMAGE PRESENT :", "FAILED",
                          " Custom Error :"+generateCustomError(customError)+" System generated Error : Given location is invalid :- " + path + "");
                  checkTrue(false, stopOnFailure,
                          " Custom Error :"+generateCustomError(customError)+" System generated Error : Given location is invalid :- " + path + "");
                  
                  
            } else {
                
                
                reportresult(true, "CHECK IMAGE PRESENT :", "FAILED",
                        "Given location is invalid :- " + path + "");
                checkTrue(false, stopOnFailure,
                        "CHECK IMAGE PRESENT command:Given location is invalid :- "
                                + path + "");
            }
            
            
           
        } catch (Exception e) {
            e.printStackTrace();

           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                  reportresult(true, "CHECK IMAGE PRESENT :", "FAILED",
                          " Custom Error :"+generateCustomError(customError)+" System generated Error : Trying to access Invalid Image :- " + path + "");
                  checkTrue(false, stopOnFailure,
                          " Custom Error :"+generateCustomError(customError)+" System generated Error : Trying to access Invalid Image :- " + path + "");
                  
            } else {
                reportresult(true, "CHECK IMAGE PRESENT :", "FAILED",
                        "Trying to access Invalid Image :- " + path + "");
                checkTrue(false, stopOnFailure,
                        "CHECK IMAGE PRESENT command:Trying to access Invalid Image :- "
                                + path + ""); 
            }
            
            

        }

    }

    /**
     * 
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param identifier
     *            the identifier
     * @param isRotatable
     *            : image rotate or not
     * @param stopOnFailure
     *            : true or false
     */

    public final void checkImagePresent(final String objectName, final String identifier,
            final boolean isRotatable, final boolean stopOnFailure, final Object... customError) {

        String actualLocator = ObjectMap.getObjectSearchPath(objectName,
                identifier);
        ObjectLocator locator = new ObjectLocator(objectName, identifier,
                actualLocator);
        doCheckImagePresent(locator, isRotatable, stopOnFailure,customError);
    }

    /**
     * checkImagePresent.
     * 
     * @param objectName
     *            the object name
     * @param isRotatable
     *            : image rotate or not
     * @param stopOnFailure
     *            : true or false
     */
    public final void checkImagePresent(final String objectName, final boolean isRotatable, final boolean stopOnFailure, final Object... customError) {
        checkImagePresent(objectName,"",isRotatable,stopOnFailure,customError );
    }

    /**
     * Screenshot.
     * 
     * @param imageName
     *            the image name
     */
    public final void screenshot(final String imageName) {

        File screenShotFolder = new File("Screenshots");
        WebDriver driver = getDriver();
        try {
            if (!screenShotFolder.exists() && !screenShotFolder.mkdir()) {
                getLog().error(
                        "Cannot create a new file in the intended location. "
                                + "" + screenShotFolder.getAbsolutePath());
            }
            File scrFile =
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String filePath =
                    screenShotFolder.getAbsolutePath() + File.separator
                            + imageName + ".png";
            FileUtils.copyFile(scrFile, new File(filePath));
            reportresult(true, "SCREENSHOT :", "PASSED",
                    "Screenshot command: Screenshot saved at :" + filePath);

        } catch (Exception e) {

            reportresult(
                    true,
                    "SCREENSHOT :",
                    "FAILED",
                    "Screenshot command: Screen shot capturing failed"
                            + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pdf verify.
     * 
     * @param filePath
     *            the file path
     * @param pageNumberRange
     *            the page number range
     * @param verifyType
     *            the verify type
     * @param inputString
     *            the input string
     * @param stopOnFaliure
     *            the stop on faliure
     */
    private void pdfVerify(final String filePath, final String pageNumberRange,
            final String verifyType, final String inputString,
            final boolean stopOnFaliure, final Object [] customError) {

        String pageText;

        if ("RELATIVE".equalsIgnoreCase(verifyType)) {
            String[] inputStringSeperate = inputString.split("\\,");
            String key = inputStringSeperate[0];
            String value = inputStringSeperate[1];
            String[] pageSeperate = pageNumberRange.split("\\-");
            int startPage = Integer.parseInt(pageSeperate[0]);
            int endPage = Integer.parseInt(pageSeperate[1]);
            try {
                File file = new File(filePath);
                PDDocument pdfDoc = PDDocument.load(file);

                PDFTextStripper reader = new PDFTextStripper();
                reader.setStartPage(startPage);
                reader.setEndPage(endPage);
                reader.setSuppressDuplicateOverlappingText(false);
                pageText = reader.getText(pdfDoc);

                BufferedReader br =
                        new BufferedReader(new StringReader(pageText));

                String tmp = "";
                boolean isPresent = false;
                StringBuilder entireString = new StringBuilder();
                while ((tmp = br.readLine()) != null) {

                    if (tmp.equals(key)) {
                        String val = br.readLine();
                        if (val != null && val.equals(value)) {
                            isPresent = true;
                            break;
                        } else {
                            continue;
                        }
                    }
                    entireString.append(tmp);
                }
                if (entireString.toString().contains(key)) {

                    int index = entireString.indexOf(key);
                    String subEntire = entireString.toString().substring(index);
                    String newString = subEntire.replace(key, "");
                    if (newString.trim().startsWith(value)) {
                        isPresent = true;
                    }

                }
                if (isPresent) {
                    reportresult(true, "CHECK DOCUMENT :", "PASSED",
                            "CheckDocument command: Verified text as expected. Expected Value : "
                                    + inputString);
                } else {
                                     
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {

                          reportresult(true, "CHECK DOCUMENT :", "FAILED",
                                  " Custom Error :"+generateCustomError (customError)+" System generated Error : CheckDocument command: Verified text as not expected. Expected Value : "
                                          + inputString);
                          checkTrue(false, stopOnFaliure,
                                  " Custom Error :"+generateCustomError (customError)+" System generated Error : CheckDocument command: Verified text as not expected. Expected Value : "
                                          + inputString);
                          
                    } else {

                        reportresult(true, "CHECK DOCUMENT :", "FAILED",
                                "CheckDocument command: Verified text as not expected. Expected Value : "
                                        + inputString);
                        checkTrue(false, stopOnFaliure,
                                "CheckDocument command: Verified text as not expected : "
                                        + inputString);
                    }
                    
                    
                }
            } catch (Exception e) {
               
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                      
                      reportresult(true, "CHECK DOCUMENT :", "FAILED",
                              " Custom Error :"+generateCustomError (customError)+" System generated Error : CheckDocument command: Text Verification failed: "
                                      + inputString);
                      checkTrue(
                              false,
                              stopOnFaliure,
                              " Custom Error :"+generateCustomError (customError)+" System generated Error : CheckDocument command: Text Verification failed: "
                                      + inputString
                                      + e.getMessage());
                      
                      
                } else {
                    
                    
                    reportresult(true, "CHECK DOCUMENT :", "FAILED",
                            "CheckDocument command: Verified text as not expected. Expected Value : "
                                    + inputString);
                    checkTrue(false, stopOnFaliure,
                            "CheckDocument command: Verified text as not expected : "
                                    + inputString);
                }
            } 
        }

    }

    /**
     * Check document.
     * 
     * @param docType
     *            the doc type
     * @param filePath
     *            the file path
     * @param pageNumberRange
     *            the page number range
     * @param verifyType
     *            the verify type
     * @param inputString
     *            the input string
     * @param stopOnFaliure
     *            the stop on faliure
     */
    public final void checkDocument(final String docType,
            final String filePath, final String pageNumberRange,
            final String verifyType, final String inputString,
            final boolean stopOnFaliure, final Object... customError) {
        if ("pdf".equalsIgnoreCase(docType)) {
            pdfVerify(filePath, pageNumberRange, verifyType, inputString,
                    stopOnFaliure,customError);
        }

    }

    /**
     * Check element present.
     * 
     * @param objectName
     *            the object name
     * @param identifire
     *            the identifire
     * @return true, if successful
     */
    public final boolean checkElementPresent(final String objectName,
            final String identifire) {

        locatorIdentifire = identifire;

        return checkElementPresent(objectName);

    }

    /** The Constant CHAR_LIST. */
    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyz";

    /**
     * This method generates random string.
     * 
     * @param length
     *            set to length
     * @return String
     * 
     */
    private String generateRandomString(final int length) {

        StringBuilder randStr = new StringBuilder();
        for (int i = 0; i < length; i++) {
            // int number = getRandomNumber();
            Random randomGenerator = new Random(System.nanoTime());
            int number = randomGenerator.nextInt(CHAR_LIST.length());
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    /**
     * This method generates random numbers.
     * 
     * @return int
     */
    private long getRandomNumber() {
        long randomInt;
        // int range = (int)System.nanoTime();
        // Random randomGenerator = new Random(System.nanoTime());

        randomInt = System.nanoTime();
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    /**
     * Gets the date.
     * 
     * @param format
     *            the format
     * @param skipWeekend
     *            the skip weekend
     * @param day
     *            the day
     * @return the date
     */
    private String getDate(final String format, final boolean skipWeekend,
            final int day) {

        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;

        if (skipWeekend) {
            final int five = 5;
            final int two = 2;
            final int seven = 7;
            int days = day;
            days = ((days / five) * two) + days;
            date = DateUtils.addDays(Calendar.getInstance().getTime(), days);
            Calendar myCal = new GregorianCalendar();
            myCal.setTime(date);
            if (myCal.get(Calendar.DAY_OF_WEEK) == seven) {
                date = DateUtils.addDays(date, 2);
            } else if (myCal.get(Calendar.DAY_OF_WEEK) == 1) {
                date = DateUtils.addDays(date, 1);
            }
        } else {
            date = DateUtils.addDays(Calendar.getInstance().getTime(), day);
        }
        return dateFormat.format(date);

    }

    /**
     * This method generates random string.
     * 
     * @param dataType
     *            set to dataType
     * @param reqLength
     *            set to length
     * @return String
     * 
     */

    public final String generateData(final String dataType, final int reqLength) {

        String random = "";
        String split = "";
        

        if ("String".equalsIgnoreCase(dataType)) {
            random = this.generateRandomString(reqLength);
        } else if ("int".equalsIgnoreCase(dataType)) {
            if(reqLength<=0){
                throw new IllegalArgumentException("Can not use minus values as length");
            }
            split = String.valueOf(this.getRandomNumber());
            int splitLength = split.length();
            random = split.substring(splitLength - reqLength, splitLength);
        } else if ("Alphanumeric".equalsIgnoreCase(dataType)) {
            if(reqLength<=0){
                throw new IllegalArgumentException("Can not use minus values as length");
            }
            split = String.valueOf(this.getRandomNumber());
            int splitLength = split.length();
            random = this.generateRandomString(reqLength - 1)
                    + split.substring(splitLength - 1, splitLength);
        } else if (dataType.toLowerCase(Locale.getDefault()).startsWith("date")) {
            String format = "yyyy/MM/dd";
            boolean skipWeekend = false;
            String[] params = dataType.split("\\|");
            if (params.length > 2) {
                format = dataType.split("\\|")[1];
                skipWeekend = Boolean.parseBoolean(dataType.split("\\|")[2]);

            } else if (params.length > 1) {
                format = dataType.split("\\|")[1];
            }

            random = getDate(format, skipWeekend, reqLength);

        }
        return random;
    }

    /**
     * Right clicks on an web element.
     * 
     * @param objectName
     *            : Logical name of the object to be doubleclicked.
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public final void rightClick(final String objectName,
            final String identifire) {
        this.locatorIdentifire = identifire;
        rightClick(objectName);
        this.locatorIdentifire = "";
    }

    /**
     * Right clicks on an web element.
     * 
     * @param objectName
     *            : Logical name of the object to be doubleclicked.
     * 
     * */

    public final void rightClick(final String objectName) {
        // Retrieve the actual object name from the object repository
        String objectID =
                ObjectMap.getObjectSearchPath(objectName, locatorIdentifire);
        int counter = getRetryCount();
        try {
            // First chacking whether the element is present
            checkForNewWindowPopups();
            WebElement element = checkElementPresence(objectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;

                    Actions rClick = new Actions(getDriver());
                    rClick.moveToElement(element).contextClick(element);
                    rClick.build().perform();
                    /* selenium.doubleClick(objectID); */
                    reportresult(true, "RIGHT CLICK :" + objectName + "",
                            "PASSED", "");
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "RIGHT CLICK :" + objectName + "",
                                "FAILED",
                                "RIGHT CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "RIGHT CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // if object not found exception is raised fail the test cases
            e.printStackTrace();
            reportresult(true, "RIGHT CLICK :" + objectName + "", "FAILED",
                    "RIGHT CLICK command  :Element (" + objectName + ") ["
                            + objectID + "] not present");
            checkTrue(false, true, "RIGHT CLICK command  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    

 public final void checkFileInfo(String fileName,
            String verificationType,
            boolean stopOnFailure, final Object... customError) {
     //String customErrorMessage = "";
     
     try{
     String root;
     String filename;

     if (fileName.contains("/")){
         fileName = fileName.replace("/","\\");
        // System.out.println(fileName); 
         filename =
                 fileName.substring(fileName.lastIndexOf(File.separator) + 1,
                         fileName.length());
        root = fileName.substring(0, fileName.lastIndexOf(File.separator));
     } else {
     filename =
             fileName.substring(fileName.lastIndexOf(File.separator) + 1,
                     fileName.length());
    root = fileName.substring(0, fileName.lastIndexOf(File.separator));
     }
     

        File fileRoot = new File(root);

        if (fileRoot.isDirectory()) {
            
            if (verificationType.equals("EXIST")) {
                byExist(filename,stopOnFailure,root,customError); // root, fileName with
                                                         // extension,root
            }
        } else {
            System.out.println("File root path does not exist");
            
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {

                reportresult(true, "CHECK FILE INFORMATION :" + root + "",
                        "FAILED",
                        " Custom Error :"+generateCustomError (customError)+" System generated Error : File root path :" + root + " does not exist");
                checkTrue(false, stopOnFailure,
                        " Custom Error :"+generateCustomError (customError)+" System generated Error : File root path :" + root + " does not exist");
                
            } else {

                reportresult(true, "CHECK FILE INFORMATION :" + root + "",
                        "FAILED",
                        "File root path :" + root + " does not exist");
                checkTrue(false, stopOnFailure,
                        "File root path :" + root + " does not exist"); 
            }

        }
     } catch (Exception e) {
         e.printStackTrace();
         
          reportresult(true, "CHECK FILE INFORMATION : EXIST",
         "FAILED",
         "File root path does not exist");
         checkTrue(false, stopOnFailure,
         "File root path does not exist");
       }

    }

    private void byExist(String filename,
            boolean stopOnFailure,String root, final Object [] customError) {
        
        /*String checkFextn =
                filename.substring(filename.lastIndexOf('.') + 1,
                        filename.length());
        String checkFName = filename.substring(0, filename.lastIndexOf('.'));*/
        
        try {
            List<String> fileNameListWithEx = new ArrayList<String>();
            
            File[] files = new File(root).listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    fileNameListWithEx.add(file.getName());
                }
            } // end add file in to list

            StringBuilder errors = new StringBuilder();
            boolean isConditionMatched = false;

            for (int i = 0; i < fileNameListWithEx.size(); i++) {
                String fname = fileNameListWithEx.get(i);

                Pattern pattern = Pattern.compile(filename, Pattern.CASE_INSENSITIVE);
               // Pattern pattern = Pattern.compile(filename);
                Matcher matcher = pattern.matcher(fname);
                
                if(matcher.matches()){
                     //System.out.println("Exist");
                     System.out.println(fname);
                     isConditionMatched = true;
                     
                      reportresult(true, "CHECK FILE INFORMATION : EXIST", "PASSED",
                             "CHECK FILE INFORMATION command : Passed. "+fname+" is exists.");
                      break;
   
                 }  else{
                  
                     isConditionMatched = false;
                    
                 }
  
           } // end for loop for read list
            
            
            if (!isConditionMatched) {

                String errorsMessages = errors.toString();
                System.out.println(errorsMessages+ " does not exist.");
                    
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                
                    reportresult(true, "CHECK FILE INFORMATION : EXIST ",
                   "FAILED",
                   " Custom Error :"+generateCustomError(customError)+" System generated Error : File " + filename + " does not exist.");
                   checkTrue(false, stopOnFailure,
                           " Custom Error :"+generateCustomError(customError)+" System generated Error : File " + filename + " does not exist.");
                } else {
                    
                    reportresult(true, "CHECK FILE INFORMATION : EXIST ",
                   "FAILED",
                   "File " + filename + " does not exist.");
                   checkTrue(false, stopOnFailure,
                   "File Name "+filename+" does not exist.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            reportresult(true, "CHECK FILE INFORMATION : EXIST",
            "FAILED",
            "File "+filename+" in "+root+"  cannot access.\n" + e.getMessage());
            checkTrue(false, stopOnFailure,
                   "File "+filename+" in " + root + " cannot access.\n "+ e.getMessage());
              
        }
    } // end byExist
    
    private String generateCustomError ( final Object [] customError) {
        String customErrorMessage = "";
        if(customError !=null && customError.length > 0 ) {
            
              for (int i=0;i<customError.length;i++){
                  customErrorMessage = customErrorMessage+customError[i].toString()+". ";
              }
              
        }
         return customErrorMessage;
    }

    /**
     * checkSorting command.
     * 
     **/
    
    
    public final void checkSorting(final String objectName,final String type,final String pattern,final String order,boolean stopOnFailure, final Object... customError ) {
    	checkSorting(objectName, "", type, pattern, order,stopOnFailure,customError);
    }
        
    public final int checkSorting(final String objectName, final String identifier, final String type,final String pattern,final String order,boolean stopOnFailure, final Object... customError ) {
        int counter = getRetryCount();
        int objectCount = 0;
        String objectID = ObjectMap.getObjectSearchPath(objectName,
        		identifier);
        WebDriver driver = getDriver();
        
        ArrayList<String> list = new ArrayList<String>();
        try {


            while (counter > 0) {
                try {
                    counter--;

                    List<WebElement> elements = driver
                            .findElements(getLocatorType(objectID));
                    objectCount = elements.size();
                    
                    for(int i=0;i<elements.size();i++){
                        list.add(i, elements.get(i).getText());
                      //  System.out.println(list.get(i));
                       // System.out.println(list.size());
                    }
               
 
                 sort(list,objectName,type, pattern, order,stopOnFailure,customError);
                
    
                    break;
                } catch (Exception e) {
                    sleep(retryInterval);

                    if (!(counter > 0)) {
                        e.printStackTrace();
                     
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", "CHECK SORTING command cannot access  : Element ("
                                + objectName + ") not present");
                        checkTrue(false, stopOnFailure, "CHECK SORTING command cannot access  : Element ("
                                + objectName + ") not present");
  
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // waiting for the maximum amount of waiting time before failing the
            // test case
            // Several checks were introduced to narrow down to the failure to
            // the exact cause.
            if (!(counter > 0)) {
                e.printStackTrace();
 
                reportresult(true, "CHECK SORTING :" + objectName + "",
                        "FAILED", "CHECK SORTING command  : Element ("
                        + objectName + ") not present");
                checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                        + objectName + ") not present");
               
            } else if ("Element".equalsIgnoreCase(e.getMessage())) {
                e.printStackTrace();

                reportresult(true, "CHECK SORTING :" + objectName + "",
                        "FAILED", "CHECK SORTING command  : Element ("
                        + objectName + ") not present");
                checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                        + objectName + ") not present");

            }

        }
        return objectCount;
    }
    
    private void sort(ArrayList<String> l,String objectName,String type, String pattern, String order,boolean stopOnFailure, final Object[] customError) {
        
        for (int i=0;i<l.size();i++) {
        // System.out.println(l.get(i));
         
         if (l.get(i).equals("")){
             l.set(i," ");
         }
        }
  
        System.out.println("Unsorted ArrayList in Java : " + l);
        System.out.println("Order :"+order);
        System.out.println("Pattern :"+pattern);
        System.out.println("Type :"+type);
        List<String> listOrg = new ArrayList<String>();
       
        for (int i = 0; i < l.size(); i++) {
            listOrg.add(l.get(i).toString());     
        }
        
        //////////////////////////////////////////////////////////////////////////
        
        if (type.equals("string")) {
            
           if("ascending".equals(order) && "alphabetically".equals(pattern)){
                

                //System.out.println("list"+l);
                //Collections.sort(l, ALPHA_ORDER); 
                Collections.sort(l, String.CASE_INSENSITIVE_ORDER);  
                //System.out.println("Ordered list"+l);
                
                if (l.equals(listOrg)){
                   // System.out.println("In ascending Order alphabetically" );
                    
                    reportresult(true, "CHECK SORTING :",
                            "PASSED", "Check sorting command : Element "+objectName+" sorted");
                    
                } else {
                  //  System.out.println("Not in ascending Order alphabetically" );
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
           
                    } else {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
            
                    }
                    
                }
             
            } else if("descending".equals(order) && "alphabetically".equals(pattern)){

               // Collections.sort(l, ALPHA_ORDER);
                Collections.sort(l, String.CASE_INSENSITIVE_ORDER);
  
                //System.out.println("Ordered list"+l);
                List<String> listDes = new ArrayList<String>();
                for ( int i = l.size()-1 ; i >= 0 ; i-- )
                {
                   // System.out.println(l.get(i).toString());
                    //String s = (String) l.get( i ) ;
                    listDes.add(l.get( i ).toString());
                }
             
                //System.out.println("Ordered list"+listDes);
                if (listDes.equals(listOrg)){
                    //System.out.println("In descending Order alphabetically" );
                    
                    reportresult(true, "CHECK SORTING :",
                            "PASSED", "Check sorting command : Element "+objectName+" sorted");
      
                } else {
                    //System.out.println("Not in descending Order alphabetically" );
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false,stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                    } else {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false,stopOnFailure, "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                    }
                    
       
                } 
            } else if("ascending".equals(order) && "1Aa".equals(pattern)){      
           
            Collections.sort(l, DiffSort.diffNaturalOrder1Aa); 
            //Collections.sort(l);
            
            //System.out.println("Ordered list"+l);
            if (l.equals(listOrg)){
                //System.out.println("In Ascending Order 1Aa" );
                
                reportresult(true, "CHECK SORTING :",
                        "PASSED", "Check sorting command : Element "+objectName+" sorted");
  
            } else {
                //System.out.println("Not in Ascending Order 1Aa" );
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false,stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");   
                } else {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false,stopOnFailure, "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");  
                }
                
   
            }
      
        } else if("descending".equals(order) && "1Aa".equals(pattern)){

            Collections.sort(l, DiffSort.diffNaturalOrder1Aa); 
            
            List<String> listDes = new ArrayList<String>();
            for ( int i = l.size()-1 ; i >= 0 ; i-- )
            {
               // System.out.println(l.get(i).toString());
                //String s = (String) l.get( i ) ;
                listDes.add(l.get( i ).toString());
            }
         
            //System.out.println("Ordered list"+listDes);
            if (listDes.equals(listOrg)){
                //System.out.println("In Descending Order 1Aa" );
                
                reportresult(true, "CHECK SORTING :",
                        "PASSED", "Check sorting command : Element "+objectName+" sorted");
                
            } else {
                //System.out.println("Not in Descending Order 1Aa" );
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
        
                } else {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
       
                }
                
            }
         
        } else if("ascending".equals(order) && "Aa1".equals(pattern)){
            Collections.sort(l, DiffSort.diffNaturalOrderAa1);           
       
            //System.out.println("Ordered list"+l);
            if (l.equals(listOrg)){
                //System.out.println("In Ascending Order Aa1" );
                
                reportresult(true, "CHECK SORTING :",
                        "PASSED", "Check sorting command : Element "+objectName+" sorted");
                
            } else {
                //System.out.println("Not in Ascending Order Aa1" );
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
        
                } else {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
        
                }
                
            }

        } else if ("descending".equals(order) && "Aa1".equals(pattern)){
            Collections.sort(l, DiffSort.diffNaturalOrderAa1); 
            
            List<String> listDes = new ArrayList<String>();
            for ( int i = l.size()-1 ; i >= 0 ; i-- )
            {
               // System.out.println(l.get(i).toString());
                //String s = (String) l.get( i ) ;
                listDes.add(l.get( i ).toString());
            }
         
            //System.out.println("Ordered list"+listDes);
            if (listDes.equals(listOrg)){
                //System.out.println("In Descending Order Aa1" );
                
                reportresult(true, "CHECK SORTING :",
                        "PASSED", "Check sorting command : Element "+objectName+" sorted");
                
            } else {
              //  System.out.println("Not in Descending Order Aa1" );
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
       
                } else {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
         
                }
               
            }

        } else if("ascending".equals(order) && "aA1".equals(pattern)){
            Collections.sort(l, DiffSort.diffNaturalOrderaA1);           

            //System.out.println("Ordered list"+l);
            if(l.equals(listOrg)){
               // System.out.println("In Ascending Order aA1" );
                
                reportresult(true, "CHECK SORTING :",
                        "PASSED", "Check sorting command : Element "+objectName+" sorted");
                
                
            } else {
               // System.out.println("Not in Ascending Order aA1" );
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
       
                } else {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
       
                }
                
            }

        } else if("descending".equals(order) && "aA1".equals(pattern)){
            Collections.sort(l, DiffSort.diffNaturalOrderaA1);  
            
            List<String> listDes = new ArrayList<String>();
            for ( int i = l.size()-1 ; i >= 0 ; i-- )
            {
               // System.out.println(l.get(i).toString());
                //String s = (String) l.get( i ) ;
                listDes.add(l.get( i ).toString());
            }
         
            //System.out.println("Ordered list"+listDes);
            if (listDes.equals(listOrg)){
               // System.out.println("In Descending Order aA1" );
                
                reportresult(true, "CHECK SORTING :",
                        "PASSED", "Check sorting command : Element "+objectName+" sorted");
                
            } else {
                //System.out.println("Not in Descending Order aA1" );
               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
       
                } else {
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
                    checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                            + objectName + ") not sorted");
        
                }
                
            }

        } else {
           if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                reportresult(true, "CHECK SORTING :" + objectName + "",
                        "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                        + objectName + ") not sorted");
                checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                        + objectName + ") not sorted");
   
            } else {
                reportresult(true, "CHECK SORTING :" + objectName + "",
                        "FAILED", "CHECK SORTING command  : Element ("
                        + objectName + ") not sorted");
                checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                        + objectName + ") not sorted");
    
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////
        } else if (type.equals("numeric")){
            List<Double> DoublelistOrg = new ArrayList<Double>();
            List<Double> DoublelistTemp = new ArrayList<Double>();
            try {
            for (int i=0;i<l.size();i++) {
                // System.out.println(l.get(i));       Rs    �
                String temp =l.get(i).replace(",", "");
                temp =temp.replace("�", "");
                temp =temp.replace("Rs", "");
                temp =temp.replace("$", "");
                temp =temp.replace("", "");
               // System.out.println(temp);
                
                double value = Double.parseDouble(temp);
                DoublelistOrg.add(value);
                DoublelistTemp.add(value);

                }
             } catch (Exception e) {
                // e.printStackTrace();
                reportresult(true, "CHECK SORTING :" + objectName + "",
                        "FAILED", "CHECK SORTING command  : Element ("
                        + type + ") not match");
                checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                        + type + ") not match");
            }
            
           // System.out.println(DoublelistOrg);
            
            if(order.equals("ascending")) {
                Collections.sort(DoublelistTemp);
                
                if (DoublelistTemp.equals(DoublelistOrg)){
                   // System.out.println("ascending");
                    reportresult(true, "CHECK SORTING :",
                            "PASSED", "Check sorting command : Element "+objectName+" sorted");
                } else {
                    //System.out.println("Not ascending"); 
                    
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted"); 
                    } else {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted"); 
                    }
                   
                }
                
            } else if(order.equals("descending")) {
                Collections.sort(DoublelistTemp);
                Collections.reverse(DoublelistTemp);
                
                if (DoublelistTemp.equals(DoublelistOrg)){
                  //  System.out.println("descending");
                    reportresult(true, "CHECK SORTING :",
                            "PASSED", "Check sorting command : Element "+objectName+" sorted");
                } else {
                   // System.out.println("Not descending"); 
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted"); 
                    } else {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted"); 
                    }
                    
                }
                
            } else {
                reportresult(true, "CHECK SORTING :" + objectName + "",
                        "FAILED", "CHECK SORTING command  : Element ("
                        + order + ") not match");
                checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                        + order + ") not match");
            }
            
        } else if (type.equals("date")) {
            List<Date> datelistOrg = new ArrayList<Date>();
            List<Date>  datelistTemp = new ArrayList<Date>();
            try {
            for (int i=0;i<l.size();i++) {
                // System.out.println(l.get(i));       Rs    �
               SimpleDateFormat formatter = new SimpleDateFormat(pattern);
               String tempDate = l.get(i);
              //  System.out.println(l.get(i));
    
                    Date date = formatter.parse(tempDate);
                   // System.out.println(date);
                    //System.out.println(formatter.format(date));
                    datelistOrg.add(date);
                    datelistTemp.add(date);
                }   
                } catch (Exception e) {
                   // e.printStackTrace();
                    reportresult(true, "CHECK SORTING :" + objectName + "",
                            "FAILED", "CHECK SORTING command  : Element ("
                            + type + ") not match");
                    checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                            + type + ") not match");
                }

            //System.out.println( datelistOrg);
            
            if(order.equals("ascending")) {
                Collections.sort(datelistTemp);
                
                if (datelistTemp.equals(datelistOrg)){
                   // System.out.println("ascending");
                    reportresult(true, "CHECK SORTING :",
                            "PASSED", "Check sorting command : Element "+objectName+" sorted");
                } else {
                    //System.out.println("Not ascending"); 
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                    } else {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted"); 
                    }
                   
                }
                
            } else if(order.equals("descending")) {
                Collections.sort(datelistTemp);
                Collections.reverse(datelistTemp);
                
                if (datelistTemp.equals(datelistOrg)){
                  //  System.out.println("descending");
                    reportresult(true, "CHECK SORTING :",
                            "PASSED", "Check sorting command : Element "+objectName+" sorted");
                } else {
                   // System.out.println("Not descending"); 
                   if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, " Custom Error :"+generateCustomError (customError)+" System generated Error : CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                    } else {
                        reportresult(true, "CHECK SORTING :" + objectName + "",
                                "FAILED", "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");
                        checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                                + objectName + ") not sorted");  
                    }
                    
                }
                
            } else {
                reportresult(true, "CHECK SORTING :" + objectName + "",
                        "FAILED", "CHECK SORTING command  : Element ("
                        + order + ") not match");
                checkTrue(false, stopOnFailure, "CHECK SORTING command  : Element ("
                        + order + ") not match");
            }
            
            
        } else {
            reportresult(true, "CHECK SORTING :" + objectName + "",
                    "FAILED", "CHECK SORTING command  : Type ("
                    + type + ") not match");
            checkTrue(false, stopOnFailure, "CHECK SORTING command  : Type ("
                    + type + ") not match");
        }
       }
    
    /**
     * Insight custom command. Checks insight charts content values.
     * 
     **/

    public void checkChartContent(String chartName, String identifire,
            String lData, boolean stopOnFailure, final Object... customError) {
        this.locatorIdentifire = identifire;
        checkChartContent(chartName, lData, stopOnFailure,customError);
        this.locatorIdentifire = "";
        
    }

    public void checkChartContent(String chartName, String lData,
            boolean stopOnFailure, final Object... customError) {

        String objectID = ObjectMap.getObjectSearchPath(chartName, locatorIdentifire);
        System.out.println("2###########");
        String chartdata[] = objectID.split("\\|");

        try {
            if (chartdata[0].equalsIgnoreCase("PIE")) {

                checkChartPieContent(chartdata[1], lData, stopOnFailure,customError);

            } else if (chartdata[0].equalsIgnoreCase("BAR")) {
                System.out.println("3###########");
                checkChartBarContent(chartdata[1], lData, stopOnFailure,customError);
                System.out.println("4###########");

            } else if (chartdata[0].equalsIgnoreCase("CBAR")) {

                checkColoredChartBarContent(chartdata[1], lData, stopOnFailure,customError);

            }

        } catch (RowsExceededException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (WriteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BiffException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean elementChartElementPresent(String xPath) {

        try {
            WebDriver driver = getDriver();
            driver.findElement(By.xpath(xPath));

            return true;
        } catch (NoSuchElementException e) {
            // TODO: handle exception
            return false;
        }
    }

    private void checkColoredChartBarContent(String chartName, String lData,
            boolean stopOnFailure, final Object[] customError) throws InterruptedException,
            RowsExceededException, WriteException, BiffException, IOException {
        // initiating
        boolean isElementFound = false;
        String inputstring = "";

        // Input array
        ArrayList<String> inputStringArray = new ArrayList<String>();

        // variables for each pie chart element
        ArrayList<String> chartnames = new ArrayList<String>();
        ArrayList<String> chartvalues = new ArrayList<String>();
        ArrayList<String> chartpercentages = new ArrayList<String>();
        int startingele = 1;
        StringBuffer errorstr = new StringBuffer();
        String chartLocator = "";
        String elementLocateVariable = "";
        boolean isEqual = false;
        String chartElementXpath = "";

        // Chathura 24 April
        HashMap<Integer, ArrayList> userColumns =
                new HashMap<Integer, ArrayList>();
        ArrayList userColumnValues = new ArrayList();

        // Getting Data
        inputstring = lData;
        int checkingColCount = 1;

        for (int retry = 0; retry < getRetryCount(); retry++) {
            try {

                if (elementChartElementPresent("//img[@title='" + chartName
                        + "']")) {
                    elementLocateVariable = "//img[@title='" + chartName + "']";
                    isElementFound = true;
                    chartLocator =
                            "//img[@title='" + chartName
                                    + "']/../../../..//child::*[name()='svg']";
                    chartElementXpath = chartLocator;
                    break;
                }

                else if (elementChartElementPresent("//*[text()='" + chartName
                        + "']")) {
                    elementLocateVariable =
                            "//*[text()='" + chartName + "']/../../../svg";
                    isElementFound = true;
                    chartLocator = "//*[text()='" + chartName + "']/../..";
                    chartElementXpath = "//*[text()='" + chartName + "']";
                    break;

                } else {
                    Thread.sleep(RETRY_INTERVAL);
                }
            } catch (Exception ex) {
            }
        }

        if (isElementFound) {

            String inputStringCurrStr = "";
            ArrayList<String> inputStringCurrArray = new ArrayList<String>();

            inputStringArray =
                    new ArrayList<String>(Arrays.asList(inputstring.split("#")));

            checkingColCount = inputStringArray.size();

            for (int i = 0; i < inputStringArray.size(); i++) {

                userColumnValues = new ArrayList();
                chartnames = new ArrayList<String>();
                chartvalues = new ArrayList<String>();

                try {
                    // Getting the values out
                    inputStringCurrStr = inputStringArray.get(i);
                    inputStringCurrArray =
                            new ArrayList<String>(
                                    Arrays.asList(inputStringCurrStr
                                            .split("\\|")));

                    // Putting them to arrays to be used in pie chart check

                    for (int arrayPos = 0; arrayPos < inputStringCurrArray
                            .size(); arrayPos++) {
                        chartnames.add(inputStringCurrArray.get(arrayPos));
                        chartvalues.add(inputStringCurrArray.get(++arrayPos));

                    }
                    userColumnValues.add(chartnames);
                    userColumnValues.add(chartvalues);

                    userColumns.put(i + 1, userColumnValues);

                } catch (Exception e) {

                }

            }

            // Check the Bar Chart
            System.out.println(userColumns);
            errorstr =
                    insightColorBarChartContentCheck(chartLocator, chartName,
                            chartnames, chartvalues, chartpercentages,
                            startingele, checkingColCount, userColumns,
                            elementLocateVariable, chartElementXpath);
            if ((errorstr.length() == 0) || errorstr == null) {
                isEqual = true;
            } else {
                isEqual = false;
            }

            // Pass or fail the test case
            if (isEqual) {
                reportresult(true, "MULTICOLOR BAR CHART VALIDATION PASSED :"
                        + chartName + "", "PASSED", "");
            } else {

               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                    /*
                     * VTAF result reporter call
                     */
                    reportresult(true, "BARCHART VALIDATION FAILED :" + chartName
                            + "", "FAILED",
                            " Custom Error :"+generateCustomError(customError)+" System generated Error : BARCHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);

                    /*
                     * VTAF specific validation framework reporting
                     */
                    checkTrue(false, stopOnFailure,
                            " Custom Error :"+generateCustomError(customError)+" System generated Error : BARCHART VALIDATION  command  :Element (" + chartName
                            + ") Error Str:" + errorstr);

                } else {
                    /*
                     * VTAF result reporter call
                     */
                    reportresult(true, "BARCHART VALIDATION FAILED :" + chartName
                            + "", "FAILED",
                            "BARCHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);

                    /*
                     * VTAF specific validation framework reporting
                     */
                    checkTrue(false, stopOnFailure,
                            "BARCHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);
 
                }
                
                
            }

        } else // if element is not present
        {
            /*
             * VTAF result reporter call
             */
            reportresult(true, "BARCHART VALIDATION FAILED :" + chartName + "",
                    "FAILED", "BARCHART VALIDATION  command  :Element ("
                            + chartName + ")  not present");

            /*
             * VTAF specific validation framework reporting
             */
            checkTrue(false, stopOnFailure,
                    "BARCHART VALIDATION  command  :Element (" + chartName
                            + ")  not present");
        }

    }

    private void checkChartBarContent(String chartName, String lData,
            boolean stopOnFailure, final Object[] customError) throws InterruptedException,
            RowsExceededException, WriteException, BiffException, IOException {
        // initiating
        boolean isElementFound = false;
        String inputstring = "";

        // Input array
        ArrayList<String> inputStringArray = new ArrayList<String>();

        // variables for each pie chart element
        ArrayList<String> chartnames = new ArrayList<String>();
        ArrayList<String> chartvalues = new ArrayList<String>();
        ArrayList<String> chartpercentages = new ArrayList<String>();
        int startingele = 1;
        StringBuffer errorstr = new StringBuffer();
        String chartLocator = "";
        String elementLocateVariable = "";
        boolean isEqual = false;

        // Getting Data
        inputstring = lData;
        for (int retry = 0; retry < getRetryCount(); retry++) {
            try {

                String xpath = "//img[@title='" + chartName + "']";
                System.out.println(xpath);

                if (elementChartElementPresent(xpath)) {
                    isElementFound = true;

                    chartLocator =
                            "//img[@title='" + chartName
                                    + "']/../../../..//child::*[name()='svg']";
                    elementLocateVariable = chartLocator;
                    break;
                }

                else if (elementChartElementPresent("//*[text()='" + chartName
                        + "']")) {
                    elementLocateVariable = "//*[text()='" + chartName + "']";

                    isElementFound = true;
                    chartLocator = "//*[text()='" + chartName + "']/../..";
                    break;
                } else {
                    Thread.sleep(RETRY_INTERVAL);
                }
            } catch (Exception ex) {
            }
        }

        if (isElementFound) {

            String inputStringCurrStr = "";
            ArrayList<String> inputStringCurrArray = new ArrayList<String>();

            inputStringArray =
                    new ArrayList<String>(Arrays.asList(inputstring.split("#")));

            for (int i = 0; i < inputStringArray.size(); i++) {

                // Split the string to parts and entered to an array NAMED
                // inputTable
                try {

                    // Getting the values out
                    inputStringCurrStr = inputStringArray.get(i);
                    inputStringCurrArray =
                            new ArrayList<String>(
                                    Arrays.asList(inputStringCurrStr
                                            .split("\\|")));

                    // Putting them to arrays to be used in pie chart check
                    chartnames.add(inputStringCurrArray.get(0));
                    chartvalues.add(inputStringCurrArray.get(1));
                    chartpercentages.add(inputStringCurrArray.get(2));
                } catch (Exception e) {

                }

            }

            // Check the Pie chart
            errorstr =
                    insightBarChartContentCheck(chartLocator, chartnames,
                            chartvalues, chartpercentages, startingele,
                            elementLocateVariable);
            if ((errorstr.length() == 0) || errorstr == null) {
                isEqual = true;
            } else {
                isEqual = false;
            }

            // Pass or fail the test case
            if (isEqual) {
                reportresult(true, "BARCHART VALIDATION PASSED :" + chartName
                        + "", "PASSED", "");
            } else {

               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                 // * VTAF result reporter call

                    reportresult(true, "BARCHART VALIDATION FAILED :" + chartName
                            + "", "FAILED",
                            " Custom Error :"+generateCustomError(customError)+" System generated Error : PIECHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);

                    // * VTAF specific validation framework reporting

                    checkTrue(false, stopOnFailure,
                            " Custom Error :"+generateCustomError(customError)+" System generated Error : PIECHART VALIDATION  command  :Element (" + chartName
                            + ") Error Str:" + errorstr);
                } else {
                 // * VTAF result reporter call

                    reportresult(true, "BARCHART VALIDATION FAILED :" + chartName
                            + "", "FAILED",
                            "BARCHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);

                    // * VTAF specific validation framework reporting

                    checkTrue(false, stopOnFailure,
                            "BARCHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);
                }
                
                

            }

        } else // if element is not present
        {
           
            // * VTAF result reporter call

            reportresult(true, "BARCHART VALIDATION FAILED :" + chartName + "",
                    "FAILED", "PIECHART VALIDATION  command  :Element ("
                            + chartName + ")  not present");

            // * VTAF specific validation framework reporting

            checkTrue(false, stopOnFailure,
                    "BARCHART VALIDATION  command  :Element (" + chartName
                            + ")  not present");
        }

    }

    private void checkChartPieContent(String chartName, String lData,
            boolean stopOnFailure, final Object[] customError) throws InterruptedException,
            RowsExceededException, WriteException, BiffException, IOException {

        boolean isElementFound = false;
        String inputstring = "";
        System.out.println("5###########");
        // Input array
        ArrayList<String> inputStringArray = new ArrayList<String>();

        // variables for each pie chart element
        ArrayList<String> chartnames = new ArrayList<String>();
        ArrayList<String> chartvalues = new ArrayList<String>();
        ArrayList<String> chartpercentages = new ArrayList<String>();
        int startingele = 4;
        StringBuffer errorstr = new StringBuffer();
        String chartLocator = "";
        boolean isEqual = false;
        String chartElementXpath = "";

        // Getting Data
        inputstring = lData;

        //Pie charts
        for (int retry = 0; retry < getRetryCount(); retry++) {

            if (elementChartElementPresent("//img[@title=\"" + chartName
                    + "\"]")) {

                isElementFound = true;
                chartLocator =
                        "//img[@title=\"" + chartName + "\"]/../../../..";
                chartElementXpath = chartLocator + "//*[name()='svg']";
                break;
            } else if (elementChartElementPresent("//*[text()='" + chartName
                    + "']")) {

                isElementFound = true;
                chartLocator = "//*[text()='" + chartName + "']/../../..";
                chartElementXpath = chartLocator + "//*[name()='svg']";
                break;
            } else {
                Thread.sleep(RETRY_INTERVAL);
            }
        }

        if (isElementFound) {

            String inputStringCurrStr = "";
            ArrayList<String> inputStringCurrArray = new ArrayList<String>();

            inputStringArray =
                    new ArrayList<String>(Arrays.asList(inputstring.split("#")));

            for (int i = 0; i < inputStringArray.size(); i++) {

                // Split the string to parts and entered to an array NAMED
                // inputTable
                try {

                    // Getting the values out
                    inputStringCurrStr = inputStringArray.get(i);
                    inputStringCurrArray =
                            new ArrayList<String>(
                                    Arrays.asList(inputStringCurrStr
                                            .split("\\|")));

                    // Putting them to arrays to be used in pie chart check
                    chartnames.add(inputStringCurrArray.get(0));
                    chartvalues.add(inputStringCurrArray.get(1));
                    chartpercentages.add(inputStringCurrArray.get(2));
                } catch (Exception e) {

                }

            }

            // Check the Pie chart
            errorstr =
                    insightPieChartContentCheck(chartLocator, chartnames,
                            chartvalues, chartpercentages, startingele,
                            chartElementXpath);
            if ((errorstr.length() == 0) || errorstr == null) {
                isEqual = true;
            } else {
                isEqual = false;
            }

            // Pass or fail the test case
            if (isEqual) {
                reportresult(true, "PIE CHART VALIDATION PASSED :" + chartName
                        + "", "PASSED", "");
            } else {

                

               if(customError !=null && !(customError[0].equals("null")||customError[0].equals("")) ) {
                 // VTAF result reporter call
                    reportresult(true, "PIECHART VALIDATION FAILED :" + chartName
                            + "", "FAILED",
                            " Custom Error :"+generateCustomError(customError)+" System generated Error : PIECHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);

                    // VTAF specific validation framework reporting

                    checkTrue(false, stopOnFailure,
                            " Custom Error :"+generateCustomError(customError)+" System generated Error : PIECHART VALIDATION  command  :Element (" + chartName
                            + ") Error Str:" + errorstr);
                } else {
                 // VTAF result reporter call
                    reportresult(true, "PIECHART VALIDATION FAILED :" + chartName
                            + "", "FAILED",
                            "PIECHART VALIDATION  command  :Element (" + chartName
                                    + ") Error Str:" + errorstr);

                    // VTAF specific validation framework reporting

                    checkTrue(false, stopOnFailure,
                            "PIECHART VALIDATION  command  :Element (" + chartName
                                    + ")  Error Str:" + errorstr);  
                }
             
            }

        } else // if element is not present
        {

            // VTAF result reporter call

            reportresult(true, "PIECHART VALIDATION FAILED :" + chartName + "",
                    "FAILED", "PIECHART VALIDATION  command  :Element ("
                            + chartName + ") not present");

            // VTAF specific validation framework reporting

            checkTrue(false, stopOnFailure,
                    "PIECHART VALIDATION  command  :Element (" + chartName
                            + ")  not present");
        }

    }

    /**
     * Checks the contents of a multi color bar chart against the expected
     * values.
     * 
     * @param userColumns
     * @param chartElementXpath
     * @param
     */

    @SuppressWarnings("rawtypes")
    private StringBuffer insightColorBarChartContentCheck(String chartName,
            String chartLocator, ArrayList<String> chartnames,
            ArrayList<String> chartvalues, ArrayList<String> chartpercentages,
            int startingele, int checkingColCount,
            HashMap<Integer, ArrayList> userColumns,
            String chartElementCapture, String chartElementXpath) {

        // Portlet content variables
        WebDriver driver = getDriver();
        ArrayList<String> chartelementnames = chartnames;
        ArrayList<String> chartelementvalues = chartvalues;
        // ArrayList<String> chartpercentage = chartpercentages;
        int startingelement = startingele;
        int arraycounter = 0;
        StringBuffer verificationErrors = new StringBuffer();
        int actCategoryCount = 0;
        int actElementCnt = 0;
        int elementOffset = 0;
        ArrayList<String> htmlElementNames = new ArrayList<String>();
        ArrayList<String> htmlElementValues = new ArrayList<String>();

        // chathura
        HashMap<Integer, ArrayList> htmlColumns =
                new HashMap<Integer, ArrayList>();
        ArrayList htmlColumnValues = new ArrayList();

        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        Actions action = new Actions(driver);

        if (chartName.contains("text")) {

            elementOffset = 3;
        } else if (chartName.contains("img")) {

            elementOffset = 2;
        }

        List<WebElement> locateElementsThree =
                driver.findElements(By
                        .xpath(chartName
                                + "//child::*[position()=2 and name()='g']/child::*[position()=2 "
                                + "and name()='g']/child::*[name()='rect']"));

        actCategoryCount = locateElementsThree.size();

        List<WebElement> locateElementsFour =
                driver.findElements(By.xpath(chartName
                        + "//child::*[position()=2 and name()='g']"));

        actElementCnt = locateElementsFour.size();

        int rectFormat = actCategoryCount / actElementCnt;
        // The first loop of the pie chart check

        for (int i = startingelement; i <= rectFormat; i++) {
            // Accessing the different colored elements in a bar
            // ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT
            // ELEMENT ELEMENT ELEMENT

            htmlElementNames = new ArrayList<String>();
            htmlElementValues = new ArrayList<String>();

            htmlColumnValues = new ArrayList<>();

            for (int colorFieldRect = i; colorFieldRect <= actCategoryCount; colorFieldRect +=
                    rectFormat) {

                try {
                    try {

                        WebElement chartHeaderEle =
                                driver.findElement(By.xpath(chartElementXpath));
                        jsExecutor
                                .executeScript(
                                        "var evObj = document.createEvent('MouseEvents');"
                                                + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                                + "arguments[0].dispatchEvent(evObj);",
                                        chartHeaderEle);

                        WebElement element =
                                driver.findElement(By
                                        .xpath(chartName
                                                + "//child::*[position()=2 and name()='g']/child::*[position()=2 and name()='g']"));
                        jsExecutor
                                .executeScript(
                                        "var evObj = document.createEvent('MouseEvents');"
                                                + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                                + "arguments[0].dispatchEvent(evObj);",
                                        element);
                        System.out.println(element);
                        Thread.sleep(500);

                        WebElement element2 =
                                driver.findElement(By
                                        .xpath(chartName
                                                + "//child::*[position()=2 and name()='g']/child::*[position()=2 and name()='g']/child::*[position()="
                                                + colorFieldRect
                                                + " and name()='rect']"));
                        jsExecutor
                                .executeScript(
                                        "var evObj = document.createEvent('MouseEvents');"
                                                + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                                + "arguments[0].dispatchEvent(evObj);",
                                        element2);
                        System.out.println(element2);
                        Thread.sleep(500);

                    } catch (Exception ex) {
                        if (ex.getMessage().contains("Alert")) {
                            if (ex.getMessage().contains("Alert")) {

                                WebElement element3 =
                                        driver.findElement(By
                                                .xpath(chartName
                                                        + "//child::*[position()=2 and name()='g']/child::*[position()=2 and name()='g']/child::*[position()="
                                                        + colorFieldRect
                                                        + " and name()='rect']"));
                                jsExecutor
                                        .executeScript(
                                                "var evObj = document.createEvent('MouseEvents');"
                                                        + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                                        + "arguments[0].dispatchEvent(evObj);",
                                                element3);

                            }

                        }
                    }

                    try {
                        driver.findElements(By.xpath(chartName
                                + "//child::*[name()='g' and position()="
                                + (actElementCnt + elementOffset) + "]"));

                    } catch (Error e) {
                        verificationErrors.append("\n" + e.toString());
                    }
                    try {
                        String chartstr12 =

                                driver.findElement(
                                        By.xpath(chartName
                                                + "/..//child::*[position()=1 and name()='svg']/child::*[name()='g' and position()="
                                                + (actElementCnt + elementOffset)
                                                + "]//*[name()='text' and position()="
                                                + 1 + "]")).getText();
                        System.out.println("chartstr12 : " + chartstr12);
                        htmlElementNames.add(chartstr12);

                    } catch (Error e) {
                        verificationErrors.append("\n" + e.toString());
                    }
                    try {

                        String chartstr22 =

                                driver.findElement(
                                        By.xpath(chartName
                                                + "/..//child::*[position()=1 and name()='svg']/child::*[name()='g' and position()="
                                                + (actElementCnt + elementOffset)
                                                + "]//*[name()='text' and position()="
                                                + 2 + "]")).getText();
                        System.out.println("chartstr22 : " + chartstr22);

                        htmlElementValues.add(chartstr22);

                    } catch (Error e) {
                        verificationErrors.append("\n" + e.toString());
                    }
                    arraycounter = arraycounter + 1;

                } catch (Error e) {
                    verificationErrors.append("\n" + e.toString());
                } catch (Exception e1) {
                    verificationErrors.append("\n" + e1.toString());
                }
            }// end of second for loop

            htmlColumnValues.add(htmlElementNames);
            htmlColumnValues.add(htmlElementValues);

            htmlColumns.put(i, htmlColumnValues);

        }// end of first for loop

        for (int i = 1; i <= userColumns.size(); i++) {

            int checkingHtmlColumn = -1;

            for (int j = 1; j <= htmlColumns.size(); j++) {

                if (((ArrayList) userColumns.get(i).get(0)).get(0).equals(
                        ((ArrayList) htmlColumns.get(j).get(0)).get(0))) {
                    checkingHtmlColumn = j;
                    break;
                }
            }
            // check if the
            if (checkingHtmlColumn < 0) {
                verificationErrors.append("\n Element :" + i
                        + " Input column name : "
                        + ((ArrayList) userColumns.get(i).get(0)).get(0)
                        + " cannot be found in the chart");
                continue;
            } else {
                ArrayList currUserColumnNames =
                        ((ArrayList) userColumns.get(i).get(0));
                ArrayList currUserColumnValues =
                        ((ArrayList) userColumns.get(i).get(1));
                ArrayList currHtmlColumnNames =
                        ((ArrayList) htmlColumns.get(checkingHtmlColumn).get(0));
                ArrayList currHtmlColumnValues =
                        ((ArrayList) htmlColumns.get(checkingHtmlColumn).get(1));

                for (int k = 0; k < currHtmlColumnValues.size(); k++) {
                    if (currHtmlColumnValues.get(k).equals("0")
                            || currHtmlColumnValues.get(k) == null) {

                        currHtmlColumnNames.remove(k);
                        currHtmlColumnValues.remove(k);
                        k--;
                    }
                }
                // Checking the column names
                if (currHtmlColumnNames.equals(currUserColumnNames)) {

                } else {

                    verificationErrors.append("\n Element :" + i
                            + " Expected element names = "
                            + userColumns.get(i).get(0)
                            + " are different from the " + "actual names : "
                            + htmlColumns.get(checkingHtmlColumn).get(0) + " | ");
                }
                // Checking the column values
                if (currHtmlColumnValues.equals(currUserColumnValues)) {

                } else {
                    verificationErrors.append("\n Element :" + i
                            + " Expected element values = "
                            + userColumns.get(i).get(1)
                            + " are different from the " + "actual values : "
                            + htmlColumns.get(checkingHtmlColumn).get(1) +" | ");
                }

            }
        }

        return verificationErrors;
    }

    /**
     * Checks the contents of a bar chart against the expected values.
     */

    private StringBuffer insightBarChartContentCheck(String chartName,
            ArrayList<String> chartnames, ArrayList<String> chartvalues,
            ArrayList<String> chartpercentages, int startingele,
            String chartElementXpath) {

        // Portlet content variables
        WebDriver driver = getDriver();
        ArrayList<String> chartelementnames = chartnames;
        ArrayList<String> chartelementvalues = chartvalues;
        ArrayList<String> chartpercentage = chartpercentages;
        int startingelement = startingele;
        int arraycounter = 0;
        StringBuffer verificationErrors = new StringBuffer();
        int actCategoryCount = 0;
        int actElementCnt = 0;
        int elementOffset = 0;
        int firstChild = 0;

        ArrayList<String> htmlElementNames = new ArrayList<String>();
        ArrayList<String> htmlElementValues = new ArrayList<String>();

        Actions action = new Actions(driver);

        jsExecutor = (JavascriptExecutor) driver;

        // Checking chart structure
        // The first loop of the pie chart check
        if (chartName.contains("text")) {

            elementOffset = 3;
            firstChild = 4;
        } else if (chartName.contains("img")) {

            elementOffset = 2;
            firstChild = 3;
        }

        List<WebElement> locateElementsThree =
                driver.findElements(By.xpath(chartName
                        + "//child::*[position()=" + firstChild
                        + " and name()='g']/child::*[position()=2 "
                        + "and name()='g']/child::*[position()=2 "
                        + "and name()='g']/child::*[name()='rect']"));

        System.out.println("### :" + locateElementsThree);
        actCategoryCount = locateElementsThree.size();

        List<WebElement> locateElementsFour =
                driver.findElements(By.xpath(chartName
                        + "//child::*[position()=2 and name()='g']"));

        actElementCnt = locateElementsFour.size();

        for (int i = startingelement; i < actCategoryCount + startingelement; i++) {

            // Accessing the elements in the pie chart
            // ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT
            // ELEMENT ELEMENT ELEMENT

            try {
                try {

                    action = new Actions(driver);

                    action = new Actions(driver);
                    WebElement chartHeaderEle =
                            driver.findElement(By.xpath(chartElementXpath));

                    jsExecutor
                            .executeScript(
                                    "var evObj = document.createEvent('MouseEvents');"
                                            + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                            + "arguments[0].dispatchEvent(evObj);",
                                    chartHeaderEle);

                    Thread.sleep(1000);

                    WebElement elementTwo =
                            driver.findElement(By
                                    .xpath(chartName
                                            + "//child::*[position()="
                                            + firstChild
                                            + " and name()='g']/child::*[position()=2 and name()='g']/child::*[position()=2 "
                                            + "and name()='g']/child::*[position()="
                                            + i + " and name()='rect']"));
                    Thread.sleep(1000);
                    jsExecutor
                            .executeScript(
                                    "var evObj = document.createEvent('MouseEvents');"
                                            + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                            + "arguments[0].dispatchEvent(evObj);",
                                    elementTwo);

                    Thread.sleep(1000);

                } catch (Exception ex) {
                    if (ex.getMessage().contains("Alert")) {
                        action = new Actions(driver);

                        action.moveToElement(
                                driver.findElement(By
                                        .xpath(chartName
                                                + "//child::*[position()="
                                                + firstChild
                                                + " and name()='g']/child::*[position()=2 and name()='g']/child::*[position()=2 and name()='g']/child::*[position()="
                                                + i + " and name()='rect']")))
                                .build().perform();

                    }

                }

                try {

                    driver.findElements(By.xpath(chartName
                            + "/child::*[name()='g' and position()="
                            + (actElementCnt + elementOffset) + "]"));
                } catch (Error e) {
                    verificationErrors.append("\n" + e.toString());
                }

                try {

                    String chartstr12 =
                            driver.findElement(
                                    By.xpath(chartName
                                            + "/child::*[name()='g' and position()="
                                            + (actElementCnt + elementOffset)
                                            + "]//*[name()='text' and position()="
                                            + 1 + "]")).getText();

                    htmlElementNames.add(chartstr12);

                } catch (Error e) {
                    verificationErrors.append("\n" + e.toString());
                }
                try {

                    String chartstr22 =
                            driver.findElement(
                                    By.xpath(chartName
                                            + "/child::*[name()='g' and position()="
                                            + (actElementCnt + elementOffset)
                                            + "]//*[name()='text' and position()="
                                            + 2 + "]")).getText();

                    htmlElementValues.add(chartstr22);

                } catch (Error e) {
                    verificationErrors.append("\n" + e.toString());
                }
                arraycounter = arraycounter + 1;

            } catch (Error e) {
                verificationErrors.append("\n" + e.toString());
            } catch (Exception e1) {
                verificationErrors.append("\n" + e1.toString());
            }

        }

        for (int i = 0; i < chartelementnames.size(); i++) {
            boolean isElementNameMatched = false;
            if (htmlElementValues.contains(chartelementvalues.get(i))) {
                // Get the indexes of duplicate entries
                ArrayList<Integer> indexes = new ArrayList<Integer>();
                for (int j = 0; j < htmlElementValues.size(); j++) {
                    if (htmlElementValues.get(j).equals(
                            chartelementvalues.get(i))) {
                        indexes.add(j);

                        for (int k = 0; k < indexes.size(); k++) {
                            if (htmlElementNames.get(indexes.get(k)).equals(
                                    chartelementnames.get(i))) {
                                isElementNameMatched = true;
                                break;
                            } else {
                                continue;
                            }
                        }
                    }
                }
            } else {
                verificationErrors.append("\n Element :" + i
                        + " Element value = " + chartelementvalues.get(i)
                        + " is not present. Actual Value :- "
                        + htmlElementValues.get(i) + "\n");
            }

            if (htmlElementNames.contains(chartelementnames.get(i))) {
                isElementNameMatched = true;
                continue;                
            }

            if (!isElementNameMatched) {                
                verificationErrors.append("\n Element :" + i
                        + " Expected element name = "
                        + chartelementnames.get(i) + " is different from the "
                        + "actual names :- " + htmlElementNames);
            }

        }

        return verificationErrors;
    }    
       
        /*for (int i = 0; i < chartelementnames.size(); i++) {

            if (htmlElementValues.contains(chartelementvalues.get(i))) {

                // Get the indexes of duplicate entries
                ArrayList<Integer> indexes = new ArrayList<Integer>();
                for (int j = 0; j < htmlElementValues.size(); j++) {

                    if (htmlElementValues.get(j).equals(
                            chartelementvalues.get(i))) {
                        indexes.add(j);
                    }
                }
                boolean isElementNameMatched = false;
                for (int k = 0; k < indexes.size(); k++) {

                    if (htmlElementNames.get(indexes.get(k)).equals(
                            chartelementnames.get(i))) {
                        isElementNameMatched = true;
                        break;
                    } else {
                        continue;
                    }
                }

                if (!isElementNameMatched) {

                    int valuePosition =
                            htmlElementValues
                                    .indexOf(chartelementvalues.get(i));
                    verificationErrors.append("\n Element :" + i
                            + " Expected element name = "
                            + chartelementnames.get(i)
                            + " is different from the " + "actual value : "
                            + htmlElementNames.get(valuePosition));
                }

            } else {
                verificationErrors.append("\n Element :" + i
                        + " Element value = " + chartelementvalues.get(i)
                        + " is not present. Actual Value :- "+htmlElementValues.get(i)+"\n");                
                
            }

        }

        return verificationErrors;
    }
*/
    /**
     * Checks the contents of a pie chart against the expected values.
     * 
     * @param chartElementXpath
     */

    private StringBuffer insightPieChartContentCheck(String chartName,
            ArrayList<String> chartnames, ArrayList<String> chartvalues,
            ArrayList<String> chartpercentages, int startingele,
            String chartElementXpath) {

        // Portlet content variables
        WebDriver driver = getDriver();
        ArrayList<String> chartelementnames = chartnames;
        ArrayList<String> chartelementvalues = chartvalues;
        ArrayList<String> chartpercentage = chartpercentages;

        ArrayList<String> chartelementnamesAct = new ArrayList<String>();
        ArrayList<String> chartelementvaluesAct = new ArrayList<String>();

        int startingelement = startingele;
        int arraycounter = 0;
        StringBuffer verificationErrors = new StringBuffer();
        int elementCount = 0;

        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        // Checking chart structure
        // The first loop of the pie chart check

        // Accessing the elements in the pie chart
        // ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT ELEMENT
        // ELEMENT ELEMENT ELEMENT

        try {

            int countA = 0;

            List<WebElement> locateElements =
                    driver.findElements(By.xpath("" + chartName
                            + "//*[name()='svg']/child::*[name()='g']"));
            countA = locateElements.size();

            int categories = countA - 2;
            for (int j = startingelement; j < categories + startingelement; j++) {

                System.out.println("" + chartName
                        + "//*[name()='svg']/child::*[position()=" + (j) + "]");
                WebElement chartElement =
                        driver.findElement(By.xpath(chartElementXpath));

                jsExecutor
                        .executeScript(
                                "var evObj = document.createEvent('MouseEvents');"
                                        + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                        + "arguments[0].dispatchEvent(evObj);",
                                chartElement);

                Thread.sleep(1000);

                WebElement element =
                        driver.findElement(By.xpath("" + chartName
                                + "//*[name()='svg']/child::*[position()="
                                + (j) + "]"));

                jsExecutor
                        .executeScript(
                                "var evObj = document.createEvent('MouseEvents');"
                                        + "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                                        + "arguments[0].dispatchEvent(evObj);",
                                element);

                try {
                    driver.findElements(By.xpath("" + chartName
                            + "//*[name()='svg']/child::*[position()="
                            + (chartelementnames.size() + startingelement)
                            + "]"));

                } catch (Error e) {
                    verificationErrors.append("\n" + e.toString());
                }

                Thread.sleep(500);

                int countB = 0;
                List<WebElement> locateElementsTwo =
                        driver.findElements(By.xpath("" + chartName
                                + "//*[name()='svg']/child::*[name()='g']"));
                countB = locateElementsTwo.size();

                elementCount = countB;

                if (elementChartElementPresent("" + chartName
                        + "//*[name()='svg']/child::*[position()="
                        + (categories + 4)
                        + "]/child::*[position()=1]/child::*[position()=4]")) {

                    String xpath =
                            ""
                                    + chartName
                                    + "//*[name()='svg']/child::*[position()="
                                    + (categories + 4)
                                    + "]/child::*[position()=1]/child::*[position()=4]";

                    System.out.println("XPATH :" + xpath);
                    chartelementnamesAct
                            .add(driver
                                    .findElement(
                                            By.xpath(""
                                                    + chartName
                                                    + "//*[name()='svg']/child::*[position()="
                                                    + (categories + 4)
                                                    + "]/child::*[position()=1]/child::*[position()=4]"))
                                    .getText());

                }
                System.out.println("chartelementnamesAct : "
                        + chartelementnamesAct);

                if (elementChartElementPresent("" + chartName
                        + "//*[name()='svg']/child::*[position()="
                        + (categories + 4)
                        + "]/child::*[position()=1]/child::*[position()=5]")) {

                    chartelementvaluesAct
                            .add(driver
                                    .findElement(
                                            By.xpath(""
                                                    + chartName
                                                    + "//*[name()='svg']/child::*[position()="
                                                    + (categories + 4)
                                                    + "]/child::*[position()=1]/child::*[position()=5]"))
                                    .getText());

                    System.out.println("chartelementvaluesAct:"
                            + chartelementvaluesAct);
                }
            }
            for (int i = 0; i < chartelementnames.size(); i++) {

                String expectedname = "" + chartelementnames.get(i) + "";
                String expectedPers =
                        "" + chartelementvalues.get(i) + " ("
                                + chartpercentage.get(i) + "%)";

                if (chartelementnamesAct.contains(expectedname)) {

                    if (chartelementvaluesAct.contains(expectedPers)) {

                    } else {

                        verificationErrors.append(
                                "\n Chart comparison mismatch : percentage "
                                        + expectedPers + " is not present in the actual percentage values "
                                        + expectedname).append(
                                chartelementvaluesAct);
                    }

                } else {

                    verificationErrors.append(
                            "\n Chart comparison mismatch : category name "
                                    + expectedname + " is not present in the actual percentage values ").append(
                            chartelementnamesAct);
                }
            }
      
            
        } catch (Exception e) {
            verificationErrors.append("\n" + e.toString());

        }

        return verificationErrors;

    }


}
