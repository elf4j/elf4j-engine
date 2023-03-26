package elf4j.engine;

import elf4j.Logger;
import elf4j.engine.service.LogServiceManager;
import elf4j.engine.util.MoreAwaitility;

import java.time.Duration;
import java.util.function.Supplier;

public class Main {
    static Logger logger = Logger.instance();

    public static void main(String[] args) {
        Logger info = logger.atInfo();
        info.log("Message can have any number of arguments of {} type", Object.class.getTypeName());
        info.log(
                "Lazy arguments, of {} type, whose values may be {} can be mixed with eager arguments of non-Supplier types",
                Supplier.class.getTypeName(),
                (Supplier) () -> "expensive to compute");
        info.atWarn()
                .log("The Supplier downcast is mandatory per lambda syntax because arguments are declared as generic Object rather than functional interface");

        Exception exception = new Exception("Exception message");
        logger.atError().log(exception);
        logger.atError().log(exception, "Optional log message");
        logger.atInfo()
                .log(exception,
                        "Exception is always the first argument to a logging method. The {} log message and following arguments work the same way {}.",
                        "optional",
                        (Supplier) () -> "as usual");

        logger.atInfo().log("Hello, world!");
        Exception issue = new Exception("Test ex message");
        logger.atWarn().log(issue, "Testing issue '{}' in {}", issue, Main.class);

        logger.log(
                "Logger instance is thread-safe so it can be declared and used as a local, instance, or static variable");
        logger.log("Default severity level is decided by the logging provider implementation");
        Logger trace = logger.atTrace();
        trace.log("Explicit severity level is specified by user i.e. TRACE");
        Logger.instance().atTrace().log("Same explicit level TRACE");
        logger.atDebug().log("Severity level is DEBUG");
        logger.atInfo().log("Severity level is INFO");
        trace.atWarn().log("Severity level is WARN, not TRACE");
        logger.atError().log("Severity level is ERROR");
        Logger.instance()
                .atDebug()
                .atError()
                .atTrace()
                .atWarn()
                .atInfo()
                .log("Not a practical example but the severity level is INFO");

        MoreAwaitility.block(Duration.ofMillis(200), "Making sure console streams show up");
        LogServiceManager.shutdown();
    }
}
