package com.slickqa.testng;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.slickqa.client.SlickClient;
import com.slickqa.client.errors.SlickError;
import com.slickqa.client.impl.JsonUtil;
import com.slickqa.client.impl.SlickClientImpl;
import com.slickqa.client.model.*;
import com.slickqa.testng.annotations.SlickMetaData;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common class used by both the Tests and the Suite.  This class will initialize the slick client, create
 * the testrun in slick, and will hold a mapping of tests tests to slick results.  The test writer will not likely
 * have to interact with this class unless they want to customize the process by extending.
 */
public class SlickTestNGController {
    public static boolean usingSlick;
    protected SlickConfigurationSource configurationSource;
    protected Project project;

    protected Map<String, Result> results;

    protected Map<String, String> testRunIds;

    public static String baseURL;
    private String providedResultId;

    private String DEFAULTED_COMMAND_LINE_TEST = "Command line test";
    public String javaParamTestPlan;

    public SlickTestNGController() {
        usingSlick = false;
        configurationSource = initializeConfigurationSource();
        initializeBaseURL();
        testRunIds = new HashMap<String, String>();
        results = new HashMap<>();
        providedResultId = null;
    }

    private void setJavaParamTestPlan(String javaParamTestPlan) {
        if (javaParamTestPlan == null) {
            this.javaParamTestPlan = "DEFAULT_TESTPLAN";
        }
        else {
            this.javaParamTestPlan = javaParamTestPlan;
        }
    }

    /**
     * This method exists in case you need to override where slick get's it's configuration data from.  By default
     * it will get it from system properties.
     *
     * @return an instance of
     */
    protected SlickConfigurationSource initializeConfigurationSource() {
        return new SystemPropertyConfigurationSource();
    }

    protected void initializeBaseURL() {
        baseURL = configurationSource.getConfigurationEntry(ConfigurationNames.BASE_URL, null);
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void resetController() {
        testRunIds = new HashMap<String, String>();
        results = new HashMap<>();
    }

    public void initializeController(List<String> testPlanNames) throws SlickError {
        String projectName = configurationSource.getConfigurationEntry(ConfigurationNames.PROJECT_NAME, null);
        String resultUrl = configurationSource.getConfigurationEntry(ConfigurationNames.RESULT_URL, null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonUtil.mapper = mapper;

        if(baseURL != null && projectName != null) {
            try {
                if(!baseURL.endsWith("api") && !baseURL.endsWith("api/")) {
                    String add = "api/";
                    if(baseURL.endsWith("/")) {
                        baseURL = baseURL + add;
                    } else {
                        baseURL = baseURL + "/" + add;
                    }
                }
                SlickClient slickClient = getSlickClient();
                ProjectReference projectReference = new ProjectReference();
                ReleaseReference releaseReference = null;
                BuildReference buildReference = null;
                String testplanId = null;
                String testrunName = null;
                try {
                    project = slickClient.project(projectName).get();
                } catch (SlickError e) {
                    project = new Project();
                    project.setName(projectName);
                    project = slickClient.projects().create(project);
                }
                projectReference.setName(project.getName());
                projectReference.setId(project.getId());

                String releaseName = configurationSource.getConfigurationEntry(ConfigurationNames.RELEASE_NAME, null);
                if(releaseName != null) {
                    releaseReference = new ReleaseReference();
                    releaseReference.setName(releaseName);
                }
                String buildName = configurationSource.getConfigurationEntry(ConfigurationNames.BUILD_NAME, null);
                if(buildName != null) {
                    buildReference = new BuildReference();
                    buildReference.setName(buildName);
                }

                String paramTestPlanName = configurationSource.getConfigurationEntry(ConfigurationNames.TESTPLAN_NAME, null);
                setJavaParamTestPlan(paramTestPlanName);

                if (testPlanNames.size() == 0) {
                    testPlanNames.add(javaParamTestPlan);
                }
                for (String testplanName : testPlanNames) {
                    if (testplanName.equals(DEFAULTED_COMMAND_LINE_TEST)) {
                        // use the command line option if provided
                        testplanName = javaParamTestPlan;
                    }
                    HashMap<String, String> query = new HashMap<>();
                    query.put("project.id", project.getId());
                    query.put("name", testplanName);
                    TestPlan tplan = null;
                    try {
                        List<TestPlan> tplans = slickClient.testplans(query).getList();
                        if(tplans != null && tplans.size() > 0) {
                            tplan = tplans.get(0);
                        }
                    } catch (SlickError e) {
                        // don't care
                    }
                    if(tplan == null) {
                        tplan = new TestPlan();
                        tplan.setName(testplanName);
                        tplan.setProject(projectReference);
                        tplan = slickClient.testplans().create(tplan);
                    }
                    testplanId = tplan.getId();

                    Testrun testrun = new Testrun();
                    testrun.setName(testplanName);
                    testrun.setTestplanId(testplanId);
                    testrun.setProject(projectReference);
                    testrun.setRelease(releaseReference);
                    testrun.setBuild(buildReference);
                    if (System.getProperty("scheduleTests", "false").equalsIgnoreCase("true")) {
                        testrun.getAttributes().put("scheduled", "true");
                    }
                    testrun = slickClient.testruns().create(testrun);
                    if (!testRunIds.containsKey(testplanName)) {
                        testRunIds.put(testplanName, testrun.getId());
                    }
                }

                usingSlick = true;
            } catch (SlickError e) {
                System.out.println("!!!!!! Error occurred when initializing slick, no slick report will happen !!!!!!");
                e.printStackTrace();
            }
        } else if(resultUrl != null) {
            Pattern urlRegex = Pattern.compile("^(.*?)/results/([0-9a-f]+)$");
            Matcher match = urlRegex.matcher(resultUrl);
            if(match.matches()) {
                baseURL = match.group(1);
                providedResultId = match.group(2);
                usingSlick = true;
            } else {
                System.out.println("!!!!!! Error URL doesn't match a slick URL " + resultUrl);
            }
        }
    }

    public static boolean isUsingSlick() {
        return usingSlick;
    }

    public SlickClient getSlickClient() {
        SlickClient slickClient;
        try {
            slickClient = SlickResult.getThreadSlickClient();
        } catch (NullPointerException e) {
            slickClient = new SlickClientImpl(baseURL);
        }

        if(slickClient == null)
            slickClient = new SlickClientImpl(baseURL);

        return slickClient;
    }

    public String getAutomationId(Method testMethod) {
        String automationId = null;
        try {
            SlickMetaData metaData = testMethod.getAnnotation(SlickMetaData.class);
            if(metaData != null && metaData.automationId() != null && !"".equals(metaData.automationId())) {
                automationId = metaData.automationId();
            }
        } catch (RuntimeException e) {
            // ignore
        }
        if(automationId == null) {
            automationId = testMethod.getDeclaringClass().getName() + ":" + testMethod.getName();
        }

        return automationId;
    }

    public void addResultFor(Method testMethod, String testPlanName) throws SlickError {
        if (isUsingSlick() && providedResultId == null) {
            SlickMetaData metaData = testMethod.getAnnotation(SlickMetaData.class);
            if (metaData != null) {
                String automationId = getAutomationId(testMethod);
                Testcase testcase = null;

                HashMap<String, String> query = new HashMap<>();
                query.put("project.id", project.getId());
                query.put("automationId", automationId);
                ProjectReference projectReference = new ProjectReference();
                projectReference.setName(project.getName());
                projectReference.setId(project.getId());

                try {
                    List<Testcase> testcases = getSlickClient().testcases(query).getList();
                    if (testcases != null && testcases.size() > 0) {
                        testcase = testcases.get(0);
                        // update author if needed
                        if(!metaData.author().equals("") && !metaData.author().equals(testcase.getAuthor())) {
                            testcase.setAuthor(metaData.author());
                            getSlickClient().testcase(testcase.getId()).update(testcase);
                        }
                    }
                } catch (SlickError e) {
                    // ignore
                }

                if (testcase == null) {
                    testcase = new Testcase();
                    testcase.setName(metaData.title());
                    testcase.setProject(projectReference);
                    testcase.setAuthor(metaData.author());
                    testcase = getSlickClient().testcases().create(testcase);
                }

                testcase.setName(metaData.title());
                testcase.setAutomated(true);
                testcase.setAutomationId(automationId);
                testcase.setAutomationKey(getValueOrNullIfEmpty(metaData.automationKey()));
                testcase.setAutomationTool("testng");
                testcase.setPurpose(metaData.purpose());
                ComponentReference componentReference = null;
                Component component = null;
                if (metaData.component() != null && !"".equals(metaData.component())) {
                    componentReference = new ComponentReference();
                    componentReference.setName(metaData.component());
                    if (project.getComponents() != null) {
                        for (Component possible : project.getComponents()) {
                            if (metaData.component().equals(possible.getName())) {
                                componentReference.setId(possible.getId());
                                componentReference.setCode(possible.getCode());
                                component = possible;
                                break;
                            }
                        }
                    }
                    if (componentReference.getId() == null) {
                        component = new Component();
                        component.setName(metaData.component());
                        try {
                            component = getSlickClient().project(project.getId()).components().create(component);
                            componentReference.setId(component.getId());
                            project = getSlickClient().project(project.getId()).get();
                        } catch (SlickError e) {
                            component = null;
                            componentReference = null;
                        }
                    }
                }
                testcase.setComponent(componentReference);
                FeatureReference featureReference = null;
                if (metaData.feature() != null && !"".equals(metaData.feature()) && component != null) {
                    featureReference = new FeatureReference();
                    featureReference.setName(metaData.feature());
                    Feature feature = null;
                    if (component.getFeatures() != null) {
                        for (Feature possible : component.getFeatures()) {
                            if (metaData.feature().equals(possible.getName())) {
                                featureReference.setId(possible.getId());
                                feature = possible;
                                break;
                            }
                        }
                    }
                    if (feature == null) {
                        feature = new Feature();
                        feature.setName(metaData.feature());
                        if (component.getFeatures() == null) {
                            component.setFeatures(new ArrayList<Feature>(1));
                        }
                        component.getFeatures().add(feature);
                        try {
                            component = getSlickClient().project(project.getId()).component(component.getId()).update(component);
                            project = getSlickClient().project(project.getId()).get();
                            if (component.getFeatures() != null) {
                                for (Feature possible : component.getFeatures()) {
                                    if (metaData.feature().equals(possible.getName())) {
                                        featureReference.setId(feature.getId());
                                    }
                                }

                            } else {
                                // this shouldn't be possible which probably means it'll happen
                                feature = null;
                                featureReference = null;
                            }
                        } catch (SlickError e) {
                            feature = null;
                            featureReference = null;
                        }
                    }
                }
                testcase.setFeature(featureReference);
                if (metaData.steps() != null && metaData.steps().length > 0) {
                    testcase.setSteps(new ArrayList<Step>(metaData.steps().length));
                    for (com.slickqa.testng.annotations.Step metaStep : metaData.steps()) {
                        Step slickStep = new Step();
                        slickStep.setName(metaStep.step());
                        slickStep.setExpectedResult(metaStep.expectation());
                        testcase.getSteps().add(slickStep);
                    }
                }
                testcase = getSlickClient().testcase(testcase.getId()).update(testcase);
                TestcaseReference testReference = new TestcaseReference();
                testReference.setName(testcase.getName());
                testReference.setAutomationId(testcase.getAutomationId());
                testReference.setAutomationKey(testcase.getAutomationKey());
                testReference.setTestcaseId(testcase.getId());
                testReference.setAutomationTool(testcase.getAutomationTool());

                TestrunReference testrunReference = new TestrunReference();
                if (testPlanName.equals(DEFAULTED_COMMAND_LINE_TEST)) {
                    testPlanName = javaParamTestPlan;
                }

                testrunReference.setName(testPlanName);
                testrunReference.setTestrunId(testRunIds.get(testPlanName));


                Result result = new Result();
                result.setProject(projectReference);
                result.setTestrun(testrunReference);
                result.setTestcase(testReference);
                result.setStatus("NO_RESULT");
                result.setReason("not run yet...");
                result.setRecorded(new Date());
                if (System.getProperty("scheduleTests", "false").equalsIgnoreCase("true")) {
                    result.setRunstatus("SCHEDULED");
                    result.setReason("scheduled");

                    HashMap<String, String> attributes = new HashMap<String, String>();
                    for(String property : System.getProperties().stringPropertyNames()) {
                        if(property.startsWith("attr.")) {
                            attributes.put(property.substring(5), System.getProperty(property));
                        }
                    }
                    attributes.put("scheduled", "true");
                    result.setAttributes(attributes);
                }
                result = getSlickClient().results().create(result);
                results.put(automationId, result);
            }
        }
    }

    public static String getValueOrNullIfEmpty(String value) {
        if(value == null || "".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    public Result updateResultFor(String resultId, Result update) throws SlickError {
        return SlickResult.getThreadSlickClient().result(resultId).update(update);
    }

    public Result getResultFor(Method testMethod) {
        String automationId = getAutomationId(testMethod);
        if(results.containsKey(automationId)) {
            return results.get(automationId);
        } else {
            return null;
        }
    }

    public Result getOrCreateResultFor(Method testMethod, String testPlanName) {
        if(isUsingSlick() && providedResultId == null) {
            Result result = getResultFor(testMethod);
            if (result == null) {
                try {
                    addResultFor(testMethod, testPlanName);
                    return getResultFor(testMethod);
                } catch (SlickError e) {
                    e.printStackTrace();
                    System.err.println("!!!! ERROR creating slick result for " + testMethod.getName() + " !!!!");
                    return null;
                }
            } else {
                return result;
            }
        } else if(providedResultId != null) {
            Result result = getResultFor(testMethod);
            if (result == null) {
                System.err.println("!!!! ERROR result id " + providedResultId + "'s automationId did not match test that ran.");
                System.err.println("!!!! ERROR Test that ran was " + testMethod.getDeclaringClass().getName() + ":" +  testMethod.getName());
            }
            return result;
        } else {
            return null;
        }
    }

    public void createSuiteResults(List<ITestNGMethod> testNGMethods) {
        if(isUsingSlick() && providedResultId == null) {
            for(ITestNGMethod testNGMethod : testNGMethods) {
                testNGMethod.getConstructorOrMethod().getMethod();
                XmlTest xmlTest = testNGMethod.getXmlTest();;
                try {
                    addResultFor(testNGMethod.getConstructorOrMethod().getMethod(), xmlTest.getName() );
                } catch (Exception e) {
                    System.err.println("exception: " + e.getMessage());
                }
            }
            if (System.getProperty("scheduleTests", "false").equalsIgnoreCase("true")) {
                if (System.getProperty("exitAfterSchedule", "true").equalsIgnoreCase("true")) {
                    System.out.println("Tests scheduled, exiting...");
                    System.exit(0);
                }
            }

        } else if(providedResultId != null && testNGMethods.size() == 1) {
            try {
                Result result = getSlickClient().result(providedResultId).get();
                results.put(result.getTestcase().getAutomationId(), result);
            } catch (SlickError slickError) {
                System.err.println("!!!! ERROR getting slick result with id " + providedResultId +
                        " from " + configurationSource.getConfigurationEntry(ConfigurationNames.RESULT_URL) + ": " +
                        slickError.getMessage());
                slickError.printStackTrace();
            }
        }
    }
}
