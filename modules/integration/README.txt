*****************This folder contains all integration and selenium tests**************

tests-common            - Common utility and base classes
tests-integration       - Integration tests
tests-selenium
    tests-scenario1     - Virtusa selenium test scenario 1
    tests-scenario2     - Virtusa selenium test scenario 2


How to run the tests
====================
Step 1: Setup the prerequisites
Step 2: Instrument jars only if you want to generate the code coverage
Step 3: Execute the tests (integration and/or selenium)
Step 4: Generate code coverage report, only if you executed step 2:


Step 1: Setup the prerequisites
--------------------------------

Do a app factory setup and update the host entries in your local machine

*** For integration tests:
- Create a tenant and update the test artifacts accordingly.
a. <defaultTenant> details in tests-integration/tests-scenarios/src/test/resources/automation.xml

*** For selenium tests:
- Create a tenant and update the test artifacts accordingly.
a. initializeTenantLogin table in integration/tests-selenium/test-scenario1/src/main/resources/data/DataTables.xml
b. initializeTenantLogin table in integration/tests-selenium/test-scenario2/src/main/resources/data/DataTables.xml

- Create a API with following information (required only for selenium tests)
Name: testAPI001,
Version: 1.0.0
Production url : http://weather.yahooapis.com/forecastrss?w=2442047&u=c
Sandbox url: http://weather.yahooapis.com/forecastrss?w=2502265

- Update DataTable.xml files - enable test cases and update file paths


Step 2: instrument jars if you want to generate the code coverage
-----------------------------------------------------------------

1. Update emma_config.properties file in the emma folder - add the IP of the machine you gonna execute the tests

2. execute instrument_jars_p1.sh in the emma folder


Step 3: Execute the tests
-------------------------

Run integration tests
    mvn clean install -DskipPlatformTests=false

Run selenium tests
    go to integration/tests-selenium/tests-scenario1 or integration/tests-selenium/tests-scenario1
    mvn clean install


Step 4: Generate code coverage report, only if you executed step 2:
-------------------------------------------------------------------

Execute generate_report_p1.sh in the emma folder



Generate Code Coverage (Manually)
=================================

1. Stop the App Factory
2. Copy the emma/emma*.jar file to the <APPFACTORY_HOME>/ and <APPFACTORY_HOME>/repository/component/lib/
3. Copy emma/jarlist.txt file to the <APPFACTORY_HOME>/
4. Go to <APPFACTORY_HOME>/ and run - java -cp emma*.jar emma instr -m overwrite -cp @jarlist.txt
5. Start the App Factory
6. Run the tests (see 'How to run tests' section)
7. Stop the App Factory (now you can see the coverage.ec file in the emma folder)
8. Generate the report - java -cp emma*.jar emma report -r html -in coverage.em -in coverage.ec
9. Now you can find the coverage at <APPFACTORY_HOME>/coverage/index.html






