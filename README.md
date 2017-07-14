# slickqa-testng
A Slick TestNg Connector

To utilize this in your testNG tests you have 2 options:
1. (recommended) Have your Test classes extend the SlickBaseTest class
2. Use the following 2 Listeners for your tests: SlickSuite.class, SlickResult.class
    * More info on configuring Listeners can be viewed here: http://testng.org/doc/documentation-main.html#testng-listeners
    * Note: if you use this approach instead of extending the SlickBaseTest then the slickLog and slickFileAttach methods will
            not be provided to you.  You would have to implement initializing the SlickResultLogger and SlickFileAttacher 
            objects yourself.
