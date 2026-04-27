package org.webswing.jslink.test;

import org.junit.Before;
import org.junit.Test;
import org.webswing.Constants;
import org.webswing.classloader.ClassModificationRegister;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ClassModificationRegisterTest {

  @Before
  public void setUp() throws Exception {
    System.setProperty(Constants.TEMP_DIR_PATH, new File(".").getAbsoluteFile().toURI().toString());
    System.setProperty(Constants.SWING_START_SYS_PROP_APP_ID, "test");
    ClassModificationRegister r1 = new ClassModificationRegister();
    r1.setModificationState("class1", true);
    r1.setModificationState("class2", false);
    r1.setModificationState("class3", false);
  }


  @Test
  public void testReadWritten() throws Exception {
    ClassModificationRegister r = new ClassModificationRegister();
    assertTrue(r.canSkipModification("class1") == false);
    assertTrue(r.canSkipModification("class2") == true);
    assertTrue(r.canSkipModification("class3") == true);
  }

  @Test
  public void testNotifyLoaded() throws Exception {
    ClassModificationRegister r = new ClassModificationRegister();
    assertTrue(r.canSkipModification("class1") == false);
    assertTrue(r.canSkipModification("class2") == true);
    assertTrue(r.canSkipModification("class3") == true);
    r.notifyClassLoaded("class2"); // removes the reference from set
    assertTrue(r.canSkipModification("class1") == false);
    assertTrue(r.canSkipModification("class2") == false);
    assertTrue(r.canSkipModification("class3") == true);
  }


}
