# slickqa-testng
A Slick TestNg Connector

This connector enables you to post testNG results to Slick automatically.  

To utilize this in your testNG tests and successfully post your results to Slick you need to do the following:

1. Include Slick Listeners.  There are 2 options for including the Slick Listeners:
   A. (recommended) Have your Test classes extend the SlickBaseTest class
   B. Use the following 2 Listeners for your tests: SlickSuite.class, SlickResult.class
      * More info on configuring Listeners can be viewed here: http://testng.org/doc/documentation-main.html#testng-listeners
      * Note: if you use this approach instead of extending the SlickBaseTest then the slickLog and slickFileAttach methods will not be provided to you.  You would have to implement initializing the SlickResultLogger and SlickFileAttacher objects yourself.

2. Include the SlickMetaData annotation above each test method 

3. Pass the slick config parameters to testNG during execution.  These are the slick parameters you must pass:
   * slick.baseurl = The url to your hosted slick instance
   * slick.project = The Slick project name to post results to
   * slick.testplan = The Slick test plan name to post results to
   * slick.release = The Slick release to post results to
   * slick.build = The Slick build to post results to
   
Example of passing these Java params: -Dslick.baseurl=http://192.168.99.100/slick/ -Dslick.project=TestNG -Dslick.testplan=FirstTestPlan -Dslick.release=1 -Dslick.build=1
            

Example
            
