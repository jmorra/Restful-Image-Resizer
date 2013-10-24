package com.tilofy;

// Taken from http://sqa.fyicenter.com/FAQ/JUnit/How_To_Group_Multiple_Test_Classes_into_a_Suite_.html
// as a way to split out my tests.

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// specify a runner class: Suite.class
@RunWith(Suite.class)

// specify an array of test classes
@Suite.SuiteClasses({
        ImageJobManagerTest.class,
        PhotoQueueControllerTest.class,
        URLResizerTest.class}
)

public class TestSuite {
}
