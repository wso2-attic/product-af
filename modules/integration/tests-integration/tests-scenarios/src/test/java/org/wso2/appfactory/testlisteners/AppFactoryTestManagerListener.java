package org.wso2.appfactory.testlisteners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.wso2.appfactory.integration.test.utils.AFDefaultDataPopulator;
import org.wso2.carbon.automation.engine.testlisteners.TestManagerListener;

/**
 * Does the initial appfactory related data population
 */
public class AppFactoryTestManagerListener extends TestManagerListener{
    private static final Log log = LogFactory.getLog(AppFactoryTestManagerListener.class);

    @Override
    public void onTestStart(ITestResult iTestResult) {
        super.onTestStart(iTestResult);
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        super.onTestSuccess(iTestResult);
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        super.onTestFailure(iTestResult);
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        super.onTestSkipped(iTestResult);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        super.onTestFailedButWithinSuccessPercentage(iTestResult);
    }

    @Override
    public void onStart(ITestContext iTestContext) {
        super.onStart(iTestContext);
        try {
            AFDefaultDataPopulator AFDefaultDataPopulator= new AFDefaultDataPopulator();
            AFDefaultDataPopulator.initTenantApplicationAndVersionCreation();
            //AFDefaultDataPopulator.addDefaultAPI();
        } catch (Exception e) {
            final String msg = "Error occurred while populating initial data ";
            log.error(msg, e);
            class DefaultDataPopulationException extends RuntimeException{
                public DefaultDataPopulationException(String msg, Exception e) {
                    super(msg, e);
                }
            }
            throw new DefaultDataPopulationException(msg, e);
        }
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        super.onFinish(iTestContext);
    }
}
