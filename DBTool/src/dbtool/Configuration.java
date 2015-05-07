package dbtool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * All configuration values, used in 
 * @author Wojciech Kaczorowski
 *
 */
public class Configuration {

    // default vaules in case property won't be in config.properties file
    public static long seed = 1337;
    public static int performanceTestsAvgRepeat = 10;
    public static int numberOfRandomQueriesToExecute = 100;
    public static int performanceTestsSampleSize = 20;
    public static int numSQLResults = 20;
    public static int generatorBatchSize = 20;
    public static int minColumnRange = 3;
    public static int maxColumnRange = 10;
    public static int numberOfTablesToGenerate = 30;
    public static int numberOfViewsToGenerate = 30;
    public static int extraViewsToGenerate = 30;
    public static int minDataRange = 20; 
    public static int maxDataRange = 100;

    
    public static void loadConfiguration(String propertiesFileName) {
        
        
        Properties properties = new Properties();
        InputStream input = null;
        try {
            input = Configuration.class.getClassLoader().getResourceAsStream(propertiesFileName);
            if (input == null) {
                throw new FileNotFoundException("Config file not found: " + propertiesFileName + " Using default values.");
            }
            properties.load(input);
            
            seed = Long.valueOf(properties.getProperty("seed"));
            performanceTestsAvgRepeat = Integer.valueOf(properties.getProperty("performanceTestsAvgRepeat"));
            numberOfRandomQueriesToExecute = Integer.valueOf(properties.getProperty("numberOfRandomQueriesToExecute"));
            performanceTestsSampleSize = Integer.valueOf(properties.getProperty("performanceTestsSampleSize"));
            numSQLResults = Integer.valueOf(properties.getProperty("numSQLResults"));
            generatorBatchSize = Integer.valueOf(properties.getProperty("generatorBatchSize"));
            minColumnRange = Integer.valueOf(properties.getProperty("minColumnRange"));
            maxColumnRange = Integer.valueOf(properties.getProperty("maxColumnRange"));
            numberOfTablesToGenerate = Integer.valueOf(properties.getProperty("numberOfTablesToGenerate"));
            numberOfViewsToGenerate = Integer.valueOf(properties.getProperty("numberOfViewsToGenerate"));
            extraViewsToGenerate = Integer.valueOf(properties.getProperty("extraViewsToGenerate"));
            minDataRange = Integer.valueOf(properties.getProperty("minDataRange"));
            maxDataRange = Integer.valueOf(properties.getProperty("maxDataRange"));
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
}
