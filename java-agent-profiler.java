import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.FileOutputStream;

public class LogLevelStartupAgentProfiler {
    private static final String LOG4J_XML_URL = "http://localhost:8086/generate-log4j-xml";
    private static final String LOG4J_XML_PATH = "/Users/komalsharma/Downloads/template-logLevel-sboot/target/classes/log4j2.xml";
    private static final String PROFILER_LOG_PATH = "/Users/komalsharma/agent-profiler-log.txt";

    public static void premain(String agentArgs, Instrumentation inst) {
        long startTime = System.nanoTime();
        
        // Capture initial memory usage
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long initialHeapMemory = heapMemoryUsage.getUsed();

        try {
            // Call REST service and log performance
            String response = callRestService();
            
            // Calculate performance metrics
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds
            
            // Capture final memory usage
            heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long finalHeapMemory = heapMemoryUsage.getUsed();
            long memoryUsed = finalHeapMemory - initialHeapMemory;

            // Log performance metrics
            logPerformanceMetrics(duration, memoryUsed, response);

        } catch (Exception e) {
            logError("Error in agent execution", e);
        }
    }

    private static String callRestService() {
        long restStartTime = System.nanoTime();
        try {
            URL url = new URL(LOG4J_XML_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                File outputFile = new File(LOG4J_XML_PATH);
                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }
                
                long restEndTime = System.nanoTime();
                long restDuration = (restEndTime - restStartTime) / 1_000_000;
                logRestServicePerformance(restDuration);
                
                return "success";
            } else {
                return "HTTP Error: " + responseCode;
            }
        } catch (Exception e) {
            logError("Error calling REST API", e);
            return "error";
        }
    }

    private static void logPerformanceMetrics(long totalDuration, long memoryUsed, String apiResponse) {
        try (FileWriter writer = new FileWriter(PROFILER_LOG_PATH, true)) {
            writer.write("--- Agent Execution Performance Metrics ---\n");
            writer.write("Timestamp: " + java.time.LocalDateTime.now() + "\n");
            writer.write("Total Execution Time: " + totalDuration + " ms\n");
            writer.write("Memory Used: " + memoryUsed + " bytes\n");
            writer.write("REST API Response: " + apiResponse + "\n");
            writer.write("-------------------------------------------\n\n");
        } catch (IOException e) {
            System.err.println("Could not write to profiler log: " + e.getMessage());
        }
    }

    private static void logRestServicePerformance(long restDuration) {
        try (FileWriter writer = new FileWriter(PROFILER_LOG_PATH, true)) {
            writer.write("REST Service Performance:\n");
            writer.write("REST Call Duration: " + restDuration + " ms\n");
        } catch (IOException e) {
            System.err.println("Could not write REST performance to log: " + e.getMessage());
        }
    }

    private static void logError(String message, Exception e) {
        try (FileWriter writer = new FileWriter(PROFILER_LOG_PATH, true)) {
            writer.write("ERROR: " + message + "\n");
            writer.write("Exception: " + e.getMessage() + "\n");
            writer.write("Stack Trace:\n");
            
            // Write stack trace to log file
            for (StackTraceElement element : e.getStackTrace()) {
                writer.write("\t" + element.toString() + "\n");
            }
        } catch (IOException ioException) {
            System.err.println("Could not write error to log: " + ioException.getMessage());
        }
    }
}
