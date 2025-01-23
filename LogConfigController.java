package com.lseg.ipps.solutions.tpl.controller;

import com.lseg.ipps.solutions.tpl.cache.Log4JCache;
import com.lseg.ipps.solutions.tpl.model.Timer;
import com.lseg.ipps.solutions.tpl.model.TimerResponse;
import com.lseg.ipps.solutions.tpl.service.LoggingLevelService;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Controller
public class LogConfigController {

    private static final Logger logger = LogManager.getLogger(LogConfigController.class);
    public static final String LOG4J_CONFIG_CACHE_KEY = "log4j-config";
    public static final String ADMIN_USER = "admin_timer";
    public static final String ROOT_LEVEL_KEY = "rootLevel";
    public static final String WARN = "WARN";
    public static final String PACKAGE_LOG_LEVELS = "packageLogLevels";
    public static final String LOG_CONFIG_HAS_BEEN_UPDATED_SUCCESSFULLY = "Log config has been updated successfully";
    private final FreeMarkerConfigurationFactoryBean freemarkerConfig;

    @Autowired
    private LoggingLevelService loggingLevelService;

    @Autowired
    private ResourceLoader resourceLoader;

    public LogConfigController(FreeMarkerConfigurationFactoryBean freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    @GetMapping("/log-level-controller")
    public String showDashboard() {
        return "log-level-controller";
    }

    @PutMapping("/log4j2.xml")
    public ResponseEntity<TimerResponse> generateLog4j(
            @RequestParam(name = "rootLevel") String rootLevel,
            @RequestParam(name = "packageName", required = false) String packageName,
            @RequestParam(name = "packageLevel", required = false) String packageLevel,
            @RequestParam(name = "timer") String timer) {
            Map<String, Object> data = new HashMap<>();
            data.put(ROOT_LEVEL_KEY, rootLevel);

            Map<String, String> filteredPackageLogLevels = new HashMap<>();
            filteredPackageLogLevels.put(packageName, packageLevel);
            data.put(PACKAGE_LOG_LEVELS, filteredPackageLogLevels);

            long cacheExpirationTime = Long.parseLong(timer);
            Log4JCache log4JCache = Log4JCache.getInstance(TimeUnit.SECONDS, cacheExpirationTime);
            long cacheExpirationTime1 = TimeUnit.SECONDS.toMillis(cacheExpirationTime);
            Timer timer1 = new Timer(System.currentTimeMillis(), System.currentTimeMillis() + cacheExpirationTime1);
            log4JCache.put(ADMIN_USER, timer1);
            log4JCache.put(LOG4J_CONFIG_CACHE_KEY, data);
            logger.info(LOG_CONFIG_HAS_BEEN_UPDATED_SUCCESSFULLY);
            return ResponseEntity.ok(new TimerResponse(loggingLevelService.getRemainingTime(), (Map<String, Object>)log4JCache.get(LOG4J_CONFIG_CACHE_KEY)));
    }

    @GetMapping("/log4j2.json")
    public ResponseEntity<TimerResponse> getLog4jData() {
        Map<String, Object> data = new HashMap<>();
        data.put(ROOT_LEVEL_KEY, WARN);
        if (Log4JCache.checkIfLog4JCacheIsNull()) {
            return ResponseEntity.ok(new TimerResponse(0, data));
        }
        Log4JCache log4JCache = Log4JCache.getInstance(TimeUnit.SECONDS, 0);
        Object obj = log4JCache.get(LOG4J_CONFIG_CACHE_KEY);
        if (!Log4JCache.checkIfLog4JCacheIsNull() && obj instanceof HashMap && ((Map<String, Object>) obj).size() > 0) {
            return ResponseEntity.ok(new TimerResponse(0, (Map<String, Object>) obj));
        }
        return ResponseEntity.ok(new TimerResponse(0, data));
    }

    @GetMapping("/log4j2.xml")
    public ResponseEntity<String> generateLog4jXml() throws IOException {

        Map<String, Object> data;
        if (Log4JCache.checkIfLog4JCacheIsNull()) {
            Map<String, Object> defaultLogLevelMap = new HashMap<>();
            defaultLogLevelMap.put(ROOT_LEVEL_KEY, WARN);
            return ResponseEntity.ok(processTemplateAndGenerateXmlContent(defaultLogLevelMap));
        }
        Log4JCache log4JCache = Log4JCache.getInstance(TimeUnit.SECONDS, 0);
        Object obj = log4JCache.get(LOG4J_CONFIG_CACHE_KEY);
        if (obj instanceof HashMap && ((Map<String, Object>) obj).size() > 0) {
            data = (Map<String, Object>) obj;
            return ResponseEntity.ok(processTemplateAndGenerateXmlContent(data));

        } else {
            Map<String, Object> defaultLogLevelMap = new HashMap<>();
            defaultLogLevelMap.put(ROOT_LEVEL_KEY, WARN);
            return ResponseEntity.ok(processTemplateAndGenerateXmlContent(defaultLogLevelMap));
        }
    }


    private String processTemplateAndGenerateXmlContent(Map<String, Object> defaultLogLevelMap) throws IOException {
        StringWriter writer = new StringWriter();
        Template template;
        try {
            template = freemarkerConfig.createConfiguration().getTemplate("log4j_template.ftl");
            template.process(defaultLogLevelMap, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        } finally {
            writer.close();
        }
    }


    @DeleteMapping("/log4j2.xml")
    public ResponseEntity<Object> clearExistingLogging() {
        boolean log4JCacheIsNull = Log4JCache.checkIfLog4JCacheIsNull();

        if (log4JCacheIsNull) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            Log4JCache log4JCache = Log4JCache.getInstance(TimeUnit.SECONDS, 0);
            log4JCache.clearAll(LOG4J_CONFIG_CACHE_KEY);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @GetMapping("/remaining-time")
    public ResponseEntity<TimerResponse> getRemainingTime() {

        long remainingTime = loggingLevelService.getRemainingTime();
        return populateRemainingTime(remainingTime);
    }


    public ResponseEntity<TimerResponse> populateRemainingTime(long remainingTime) {

        boolean log4JCacheIsNull = Log4JCache.checkIfLog4JCacheIsNull();

        if (log4JCacheIsNull) {
            return ResponseEntity.ok(new TimerResponse(remainingTime, new HashMap<>()));
        } else{
            return ResponseEntity.ok(new TimerResponse(remainingTime, Optional.ofNullable(Log4JCache.getInstance(TimeUnit.SECONDS, 0)
                            .get(LOG4J_CONFIG_CACHE_KEY))
                    .map(cachePresent -> (Map<String, Object>) cachePresent)
                    .orElse(new HashMap<>())));
        }
    }
}
