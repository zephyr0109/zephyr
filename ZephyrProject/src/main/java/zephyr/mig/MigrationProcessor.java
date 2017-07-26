package zephyr.mig;

import java.io.IOException;

public interface MigrationProcessor {
    public static final String POP3_MODE = "pop3";
    public static final String FILE_MODE = "file";

    void migrationProcess(String[] args) throws IOException;
}
