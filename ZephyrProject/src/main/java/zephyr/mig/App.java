package zephyr.mig;

import java.util.Properties;

public class App {

    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.load(App.class.getClassLoader().getResourceAsStream("eml.properties"));
            String processMode = props.getProperty("process.mode");
            if (MigrationProcessor.FILE_MODE.equals(processMode)) {
                new FileConsoleProcessor(props).migrationProcess(args);
            } else {
                new POP3ConsoleProcessor(props).migrationProcess(args);
            }
            //new MssqlConsoleProcessor(props).migrationProcess(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
