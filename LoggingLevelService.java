package com.lseg.ipps.solutions.tpl.service;

import com.lseg.ipps.solutions.tpl.cache.Log4JCache;
import com.lseg.ipps.solutions.tpl.model.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoggingLevelService {

    public static final String ADMIN_TIMER = "admin_timer";

    /**
     * Method to get remaining time
     *
     * @return @{@link Long}
     */
    public long getRemainingTime() {
        if (Log4JCache.checkIfLog4JCacheIsNull()) {
            return 0;
        }
        Log4JCache log4JCache = Log4JCache.getInstance(TimeUnit.SECONDS, 0);
        Timer timer = (Timer) log4JCache.get(ADMIN_TIMER);
        if (timer == null) {
            return 0;
        }
        long remaining = timer.endTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

}
