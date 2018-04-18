
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ducbm
 */
public class Test {
    
    private static final Logger LOGGER = LogManager.getLogger(Test.class.getCanonicalName());
    
    public static void main(String[] args) {
        System.out.println(Test.class.getCanonicalName());
        
        try {
            int x = 1 / 0;
        } catch (Exception e) {
            System.out.println("error");
            LOGGER.error(e);
        }
        
        LOGGER.info("info");
        LOGGER.debug("debug");
        LOGGER.warn("warn");
        LOGGER.error("error");
        
    }
    
}
