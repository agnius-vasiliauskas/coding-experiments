/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author agnius
 */
public class SimulatorTest {
    
    public SimulatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSpartacusBot() {
        // TODO review the generated test code and remove the default call to fail.
        Simulator sim = new Simulator();
        
        String failMessage = sim.getSimulatedGameFailMessage();
        if (failMessage.length() > 0)
            fail(failMessage);
    }
    
}
