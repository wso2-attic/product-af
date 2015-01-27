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

package com.thoughtworks.selenium;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.opera.core.systems.OperaDriver;
import com.virtusa.VTAF.reporter.reader.ReportBase;
import com.virtusa.isq.vtaf.report.reporter.Reporter;
import com.virtusa.isq.vtaf.utils.PropertyHandler;

/**
 * The Class SeleneseTestNgHelperVir.
 */
public class SeleneseTestNgHelperVir extends SeleneseTestBaseVir {

    /** The reporter. */
    private static ReportBase reporter;

    /** The robot. */
    private static Robot robot;

    /** The prop. */
    private static Properties prop;

    /** The properties location. */
    private static String propertiesLocation;

    /** The error messages. */
    private String errorMessages = "Verification failures : \n";

    /** The test package name. */
    private String testPackageName = "";

    /** The current method. */
    private String currentMethod = "";

    /** The calling class name. */
    private String callingClassName = "";

    /** The line number. */
    private int lineNumber;

    /** The retry count. */
    private static int retryCount;

    /** The time out period. */
    private static String timeOutPeriod;

    /** The exec props. */
    private static Properties execProps;

    /** The open window handle index. */
    private List<String> openWindowHandleIndex;

    /** The result reporter. */
    private static Reporter resultReporter;

    /** The command start time. */
    private Date commandStartTime;

    /** The testcase start time. */
    private Date testcaseStartTime;

    /** The total execution time taken. */
    private long totalExecutionTimeTaken;

    /** Stores the time taken to each command. */
    private static String logTimeCSVFileBuilder = "";

    /**
     * Sets the before test configuration for the test.
     * 
     * @param url
     *            the url
     * @param browserString
     *            the browser string
     * @param context
     *            the context
     * @throws Exception
     *             the exception
     */
    @BeforeTest
    @Parameters({ "selenium.url", "selenium.browser" })
    public final void setUp(
            @Optional("http://www.google.com") final String url,
            @Optional final String browserString, final ITestContext context)
            throws Exception {
        Logger log = getLog();
        try {
            String browserStr = super.getBrowserString();
            if (browserStr == null || browserStr.isEmpty()) {
                super.setBrowserString(runtimeBrowserString());
            }
            log.info("Execution Browser : " + browserStr);
            super.setUp(url, browserStr);
        } catch (Exception e) {
            log.error("Exception occured while setting up the test ", e);
        }
        super.setCaptureScreenShotOnFailure(true);
        cleanDriverServerSessions();
    };

    /**
     * Read run prop.
     * 
     * @throws Exception
     *             Signals that an I/O exception has occurred.
     */
    @BeforeSuite
    public final void readRunProp() throws Exception {

        // Initializing the logger
        initLogger();
        initReporter();
        resultReporter.addNewTestExecution();
        getLogger(SeleneseTestNgHelperVir.class);

        setRobot(new Robot());

        setProp(new Properties());
        String rootFile = new File("").getAbsolutePath();
        String file = "";
        if (rootFile.indexOf("grid") > -1) {
            file =
                    rootFile + File.separator + "grid" + File.separator
                            + "selenium-grid-1.0.6" + File.separator
                            + "project.properties";

        } else {
            file = rootFile + File.separator + "project.properties";
        }
        setPropertiesLocation(file);
        getLog().info("Propery file location : " + rootFile);
        this.readUserProp();
    }

    /**
     * Read user prop.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void readUserProp() throws IOException {

        final int defaultRetryCount = 30;
        final int defaultTimeout = 30000;

        PropertyHandler propHandler = new PropertyHandler("runtime.properties");

        String timeOut = propHandler.getRuntimeProperty("TIMEOUT");
        String retry = propHandler.getRuntimeProperty("RETRY");
        String browser = propHandler.getRuntimeProperty("BROWSER");
        setExecProps(propHandler.getPropertyObject());

        if (!browser.isEmpty()) {
            super.setBrowserString(browser);
            System.setProperty("selenium.defaultBrowser", getBrowserString());
        }
        if (!retry.isEmpty()) {
            retryCount = Integer.parseInt(retry);
        } else {
            retryCount = defaultRetryCount;
        }
        if (!timeOut.isEmpty()) {
            timeOutPeriod = timeOut;
        } else {
            timeOutPeriod = String.valueOf(defaultTimeout);
        }

        getLog().info("Retry Count : " + retryCount);
        getLog().info("Timeout Period : " + timeOutPeriod);

    }

    /**
     * Gets the selenium.
     * 
     */
    @BeforeClass
    public final void getSelenium() {

        resultReporter.addNewTestSuite(this.getClass().getSimpleName());

    }

    /**
     * Sets the test context.
     * 
     * @param method
     *            the new test context
     */
    @BeforeMethod
    public final void setTestContext(final Method method) {
        setTestCaseFailedStatus(false);
        PropertyHandler propHandler = new PropertyHandler(propertiesLocation);
        propHandler.setRuntimeProperty("tcComment", "");

        totalExecutionTimeTaken = 0;
        testcaseStartTime = getCurrentTime();
        errorMessages = "Verification failures : \n";
        setOpenWindowHandleIndex(new ArrayList<String>());
        setBrowserString(System.getProperty("selenium.defaultBrowser"));
        this.cleanBrowserSessions();
        getLog().info("Starting a new selenium webdriver session.");

        this.startBrowserSession(getBrowserString());
        Logger log = getLog();
        log.info("Started the selenium webdriver session.");

        reporter.startReporter(method.getDeclaringClass().getCanonicalName(),
                method.getName());
        log.info("Executing the test case : "
                + method.getDeclaringClass().getSimpleName() + "."
                + method.getName());

        getLogger(this.getClass());

        testPackageName =
                this.getClass().getPackage().toString().split("package ")[1];
        String testCaseName = method.getName();
        resultReporter.addNewTestCase(testCaseName);

        appendToCSVFileBuilder("\n", testCaseName, "\n");
        // logTimeCSVFileBuilder.append("\n").append(testCaseName).append("\n");
        startOfTestCase();

        log.info("Starting the test case..");
    }

    /**
     * Start browser session.
     * 
     * @param browserString
     *            the browser string
     */
    public final void startBrowserSession(final String browserString) {

        DesiredCapabilities capabilities = getWebDriverCapabilities();
        WebDriver driver;
        if (browserString.contains("chrome")
                || browserString.contains("Chrome")) {
            driver = new ChromeDriver();

        } else if (browserString.contains("safari")) {

            DesiredCapabilities.safari();
            driver = new SafariDriver(capabilities);

        } else if (browserString.contains("iexplore")) {

            DesiredCapabilities.internetExplorer();
            capabilities.setJavascriptEnabled(true);
            driver = new InternetExplorerDriver(capabilities);

        } else if (browserString.contains("firefox")) {
            FirefoxProfile profile = getDefaultProfile();
            profile.setEnableNativeEvents(true);
            driver = new FirefoxDriver(profile);

        } else if (browserString.contains("opera")) {

            DesiredCapabilities.opera();
            driver = new OperaDriver(capabilities);
        } else {
            getLog().info("Unsupported browser type passed " + browserString);
            throw new AssertionError("Unsupported Browser");
        }

        setDriver(driver);
        if (getSeleniumInstances().isEmpty()) {
            putSeleniumInstances("default", driver);
        } else {
            putSeleniumInstances(getSeleniumInstanceName(), driver);
        }
    }

    /**
     * Clean driver server sessions.
     */
    private void cleanDriverServerSessions() {
        String os = getOperatingSystem();
        if (os.contains("win")) {
            
            this.killBrowserProcess("chromedriver_win");
            if (isx64bit()) {
                this.killBrowserProcess("IEDriverServer(x64)");
            } else {
                this.killBrowserProcess("IEDriverServer(x86)");
            }
        } else if (os.contains("mac")) {
            
            this.killBrowserProcess("chromedriver_mac");
        } else if (os.contains("nux")) {
            if (isx64bit()) {
                this.killBrowserProcess("chromedriver_linux(x64)");
            } else {
                this.killBrowserProcess("chromedriver_linux(x86)");
            }
        }

    }

    /**
     * Clean browser sessions.
     */
    private void cleanBrowserSessions() {
        String browser = getBrowserString();
        if (browser.contains("iexplore")) {
            this.killBrowserProcess("iexplore");

        } else if (browser.contains("chrome") || browser.contains("Chrome")) {
            this.killBrowserProcess("chrome");

        } else if (browser.contains("firefox")) {
            this.killBrowserProcess("firefox");

        } else if (browser.contains("opera")) {
            this.killBrowserProcess("opera");

        } else if (browser.contains("safari")) {
            this.killBrowserProcess("safari");
        }
    }

    /*
     * @BeforeSuite
     * 
     * @Parameters({ "selenium.host", "selenium.port" }) public void
     * attachScreenshotListener(@Optional("localhost") String host,
     * 
     * @Optional("4444") String port, ITestContext context) {
     * 
     * }
     */

    /**
     * Check for verification error.
     */
    @AfterMethod(alwaysRun = true)
    public final void checkForVerificationError() {

        Logger log = getLog();
        getLogger(SeleneseTestNgHelperVir.class);
        getTestCaseFailedStatus();
        log.info("End of the test case.");

        log.info("Total Time taken to execute the commands : "
                + totalExecutionTimeTaken + " ms");
        logTime("Total Time taken to execute the test case : ",
                testcaseStartTime, getCurrentTime(), log);

        Map<String, WebDriver> seleniumInstances = getSeleniumInstances();

        for (Map.Entry<String, WebDriver> entry : seleniumInstances.entrySet()) {
            try {
                entry.getValue().quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, Connection> databaseInstances = getDatabaseInstances();
        for (Map.Entry<String, Connection> entry : databaseInstances.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cleanup sessions.
     */
    @AfterMethod(alwaysRun = true)
    public final void cleanupSessions() {
        resultReporter.endTestReporting();
        setSeleniumInstances(new HashMap<String, WebDriver>());
        setDatabaseInstances(new HashMap<String, Connection>());
        endTestReporting(false);
        super.checkForVerificationErrors();
        this.cleanBrowserSessions();
    }

    /**
     * Tear down.
     * 
     * @throws Exception
     *             the exception
     * @see com.thoughtworks.selenium.SeleneseTestBaseVir#tearDown()
     * @AfterSuite.
     */
    @AfterSuite
    public final void tearDownSuite() throws Exception {
        resultReporter.endTestReporting();
        super.tearDown();
        cleanDriverServerSessions();
        generateTimeLogCSV();
    }

    /**
     * Generates the log time CSV file in the target/logs folder.
     * 
     */
    /**
     * Generates the log time CSV file in the target/logs folder.
     * 
     */
    private void generateTimeLogCSV() {
        FileWriter writer = null;
        Logger log = getLog();
        try {
            File logsDir = new File("target" + File.separator + "logs");
            if(!logsDir.exists()) {
                logsDir.mkdir();
            }
            writer = new FileWriter("ExecutionTime.csv");
            writer.append(logTimeCSVFileBuilder);
            writer.flush();
        } catch (IOException e) {
            log.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    /**
     * Kill the browser process.<br>
     * Specify the browser process to be killed.
     * 
     * @param process
     *            the process
     * @Parameters<br> process name which should be killed. <br>
     *                 Ex:<br>
     *                 If the process is firefox.exe parameter should be firefox
     */
    public final void killBrowserProcess(final String process) {
        String os = getOperatingSystem().toLowerCase();
        Logger log = getLog();
        final int pauseTimeAfterBrowserKill = 3000;
        try {
            if (os.contains("win")) {
                
                String processName = process.concat(".exe");
                if (isProcessRunning(processName)) {
                    this.killProcess(processName);

                    log.info(process
                            .concat(" browser session cleaned successfully"));
                    sleep(pauseTimeAfterBrowserKill);
                }
            } else if (os.contains("mac") || os.contains("nux")) {
                
                this.killProcess(process);
                log.info(process
                        .concat(" browser session cleaned successfully"));
                sleep(pauseTimeAfterBrowserKill);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Inits the reporter.
     */
    public static synchronized void initReporter() {
        reporter = new ReportBase();
        resultReporter = new Reporter();
    }

    /**
     * Inits the robot.
     */
    public static synchronized void initRobot() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start of test case.
     */
    public final void startOfTestCase() {
        WebDriver driver = getDriver();
        final int pageloadTimeOut = 300;
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // driver.manage().timeouts().implicitlyWait(300, TimeUnit.SECONDS);
        try {
            driver.manage().timeouts()
                    .pageLoadTimeout(pageloadTimeOut, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Reportresult.
     * 
     * @param isAssert
     *            the is assert
     * @param step
     *            the step
     * @param result
     *            the result
     * @param messageString
     *            the message string
     */
    public final void reportresult(final boolean isAssert, final String step,
            final String result, final String messageString) {
        String message = messageString;

        StackTraceElement[] stackTraceElements = Thread.currentThread()
                .getStackTrace();
        callingClassName = stackTraceElements[0].getClassName().toString()
                .toString();
        String callingMethod = "";
        // System.out.println("-----superman-----");
        for (int i = 0; i < stackTraceElements.length; i++) {
            callingClassName = stackTraceElements[i].getClassName().toString();
            if (callingClassName.startsWith(testPackageName)) {
                callingMethod = stackTraceElements[i].getMethodName();
                lineNumber = stackTraceElements[i].getLineNumber();
                break;
            }
        }
        System.out.println(callingClassName);
        // System.out.println("-----superman-----");
        Class callingClass = null;
        try {
            callingClass = Class.forName(callingClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        getLogger(callingClass);
        if (!currentMethod.equals(callingMethod)) {
            getLog().info("Executing : " + callingClass.getName() + " : "
                    + callingMethod);
            currentMethod = callingMethod;
        }

        getLog().info("Step : " + step + "\t|\tResult : " + result
                + "\t|\tMessage : " + message);

        logTime(step, getCommandStartTime(), getCurrentTime(), getLog());

        reporter.reportResult(step, result, message);

        // Adding data to the new reporter
        try {

            String testStep = step.substring(0, step.indexOf(":"));
            // replace xml special characters in the message.
            message =replaceXMLSpecialCharacters(message);
            if (result.equals("PASSED")) {
                String testMessage = message;
                if (message.equals("") || message == null) {
                    testMessage = "Passed";
                }
                if (callingClassName.contains("LIBRARY_RECOVERY")) {
                    resultReporter.reportStepResults(true, "RECOVERY : "+testStep, testMessage,
                            "Success", "");
                }else{
                    resultReporter.reportStepResults(true, testStep, testMessage,
                            "Success", "");
                }
            } else {
                if (callingClassName.contains("LIBRARY_RECOVERY")) {
                    resultReporter.reportStepResults(false,  "RECOVERY : "+testStep, message,
                            "Error",
                            getSourceLines(new Throwable(message).getStackTrace()));
                }else{
                    resultReporter.reportStepResults(false, testStep, message,
                            "Error",
                            getSourceLines(new Throwable(message).getStackTrace()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        
    }

    /**
     * End test reporting.
     * 
     * @param testFailed
     *            the test failed
     */
    public final void endTestReporting(final boolean testFailed) {

        reporter.endResultReporting(testFailed);

    }

    /**
     * Gets the current time.
     * 
     * @return the current time
     */
    public final Date getCurrentTime() {
        return Calendar.getInstance().getTime();
    }

    /**
     * Sets the command start time.
     * 
     * @param startTime
     *            the new command start time
     */
    protected final void setCommandStartTime(final Date startTime) {
        this.commandStartTime = startTime;
    }

    /**
     * Gets the command start time.
     * 
     * @return the command start time
     */
    protected final Date getCommandStartTime() {
        return this.commandStartTime;
    }

    /**
     * Log time.
     * 
     * @param desc
     *            the description
     * @param start
     *            the start date time
     * @param end
     *            the end time
     * @param log
     *            the log
     */
    private void logTime(final String desc, final Date start, final Date end,
            final Logger log) {
        try {
            if (!desc.startsWith("PAUSE")) {
                Long timeDiff = Math.abs(end.getTime() - start.getTime());
                totalExecutionTimeTaken += timeDiff;
                log.info("Time taken to execute " + desc + " " + timeDiff
                        + " ms");
                appendToCSVFileBuilder(desc, ",", timeDiff, "\n");
                // logTimeCSVFileBuilder.append(desc).append(',').append(timeDiff)
                // .append("\n");
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * @return the reporter
     */
    public static final ReportBase getReporter() {
        return reporter;
    }

    /**
     * @param reporterObj
     *            the reporter to set
     */
    public static final void setReporter(final ReportBase reporterObj) {
        SeleneseTestNgHelperVir.reporter = reporterObj;
    }

    /**
     * @return the robot
     */
    public static final Robot getRobot() {
        return robot;
    }

    /**
     * @param robotObj
     *            the robot to set
     */
    public static final void setRobot(final Robot robotObj) {
        SeleneseTestNgHelperVir.robot = robotObj;
    }

    /**
     * @return the prop
     */
    public static final Properties getProp() {
        return prop;
    }

    /**
     * @param propObj
     *            the prop to set
     */
    public static final void setProp(final Properties propObj) {
        SeleneseTestNgHelperVir.prop = propObj;
    }

    /**
     * @return the propertiesLocation
     */
    public static final String getPropertiesLocation() {
        return propertiesLocation;
    }

    /**
     * @param propertiesLocationString
     *            the propertiesLocation to set
     */
    public static final void setPropertiesLocation(
            final String propertiesLocationString) {
        SeleneseTestNgHelperVir.propertiesLocation = propertiesLocationString;
    }

    /**
     * @return the errorMessages
     */
    public final String getErrorMessages() {
        return errorMessages;
    }

    /**
     * @param errorMessagesString
     *            the errorMessages to set
     */
    public final void setErrorMessages(final String errorMessagesString) {
        this.errorMessages = errorMessagesString;
    }

    /**
     * @return the testPackageName
     */
    public final String getTestPackageName() {
        return testPackageName;
    }

    /**
     * @param testPackageNameString
     *            the testPackageName to set
     */
    public final void setTestPackageName(final String testPackageNameString) {
        this.testPackageName = testPackageNameString;
    }

    /**
     * @return the currentMethod
     */
    public final String getCurrentMethod() {
        return currentMethod;
    }

    /**
     * @param currentMethodString
     *            the currentMethod to set
     */
    public final void setCurrentMethod(final String currentMethodString) {
        this.currentMethod = currentMethodString;
    }

    /**
     * @return the callingClassName
     */
    public final String getCallingClassName() {
        return callingClassName;
    }

    /**
     * @param callingClassNameString
     *            the callingClassName to set
     */
    public final void setCallingClassName(final String callingClassNameString) {
        this.callingClassName = callingClassNameString;
    }

    /**
     * @return the lineNumber
     */
    public final int getLineNumber() {
        return lineNumber;
    }

    /**
     * @param lineNumberInt
     *            the lineNumber to set
     */
    public final void setLineNumber(final int lineNumberInt) {
        this.lineNumber = lineNumberInt;
    }

    /**
     * @return the execProps
     */
    public static final Properties getExecProps() {
        return execProps;
    }

    /**
     * @param execPropsObj
     *            the execProps to set
     */
    public static final void setExecProps(final Properties execPropsObj) {
        SeleneseTestNgHelperVir.execProps = execPropsObj;
    }

    /**
     * @return the openWindowHandleIndex
     */
    public final List<String> getOpenWindowHandleIndex() {
        return openWindowHandleIndex;
    }

    /**
     * @param openWindowHandleIndexList
     *            the openWindowHandleIndex to set
     */
    public final void setOpenWindowHandleIndex(
            final List<String> openWindowHandleIndexList) {
        this.openWindowHandleIndex = openWindowHandleIndexList;
    }

    /**
     * @return the resultReporter
     */
    public static final Reporter getResultReporter() {
        return resultReporter;
    }

    /**
     * @param resultReporterObj
     *            the resultReporter to set
     */
    public static final void setResultReporter(final Reporter resultReporterObj) {
        SeleneseTestNgHelperVir.resultReporter = resultReporterObj;
    }

    /**
     * @return the totalExecutionTimeTaken
     */
    public final long getTotalExecutionTimeTaken() {
        return totalExecutionTimeTaken;
    }

    /**
     * @param totalTimeTaken
     *            the totalExecutionTimeTaken to set
     */
    public final void setTotalExecutionTimeTaken(final long totalTimeTaken) {
        this.totalExecutionTimeTaken = totalTimeTaken;
    }

    /**
     * @return the retryCount
     */
    public static final int getRetryCount() {
        return retryCount;
    }

    /**
     * @param retryCountInt
     *            the retryCount to set
     */
    public static final void setRetryCount(final int retryCountInt) {
        SeleneseTestNgHelperVir.retryCount = retryCountInt;
    }

    /**
     * Append to the csv log.
     * 
     * @param appendValues
     *            objects to append
     */
    private static void appendToCSVFileBuilder(final Object... appendValues) {
        StringBuilder builder = new StringBuilder();
        for (Object currentAppendValue : appendValues) {
            builder.append(currentAppendValue);

        }
        logTimeCSVFileBuilder += builder.toString();
    }
}
