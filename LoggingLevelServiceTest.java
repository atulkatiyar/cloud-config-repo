package com.lseg.ipps.solutions.tpl.service;

import com.lseg.ipps.solutions.tpl.cache.Log4JCache;
import com.lseg.ipps.solutions.tpl.model.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingLevelServiceTest {

    private LoggingLevelService loggingLevelService;
    private Log4JCache log4JCache;

    @BeforeEach
    void setUp() {
        loggingLevelService = new LoggingLevelService();
        log4JCache = mock(Log4JCache.class);
    }

    @Test
    void getRemainingTime_WhenCacheIsNull_ReturnsZero() {
        try (MockedStatic<Log4JCache> mockedStatic = mockStatic(Log4JCache.class)) {
            mockedStatic.when(Log4JCache::checkIfLog4JCacheIsNull).thenReturn(true);
            
            long result = loggingLevelService.getRemainingTime();
            
            assertEquals(0, result);
            mockedStatic.verify(Log4JCache::checkIfLog4JCacheIsNull);
        }
    }

    @Test
    void getRemainingTime_WhenTimerIsNull_ReturnsZero() {
        try (MockedStatic<Log4JCache> mockedStatic = mockStatic(Log4JCache.class)) {
            mockedStatic.when(Log4JCache::checkIfLog4JCacheIsNull).thenReturn(false);
            mockedStatic.when(() -> Log4JCache.getInstance(any(TimeUnit.class), anyLong()))
                    .thenReturn(log4JCache);
            when(log4JCache.get(LoggingLevelService.ADMIN_TIMER)).thenReturn(null);
            
            long result = loggingLevelService.getRemainingTime();
            
            assertEquals(0, result);
        }
    }

    @Test
    void getRemainingTime_WhenTimerExists_ReturnsCorrectTime() {
        try (MockedStatic<Log4JCache> mockedStatic = mockStatic(Log4JCache.class)) {
            long currentTime = System.currentTimeMillis();
            long endTime = currentTime + 1000; // 1 second in future
            Timer timer = new Timer(currentTime, endTime);
            
            mockedStatic.when(Log4JCache::checkIfLog4JCacheIsNull).thenReturn(false);
            mockedStatic.when(() -> Log4JCache.getInstance(any(TimeUnit.class), anyLong()))
                    .thenReturn(log4JCache);
            when(log4JCache.get(LoggingLevelService.ADMIN_TIMER)).thenReturn(timer);
            
            long result = loggingLevelService.getRemainingTime();
            
            // Assert that result is greater than 0 but less than or equal to 1000
            assertTrue(result > 0 && result <= 1000);
        }
    }

    @Test
    void getRemainingTime_WhenTimerExpired_ReturnsZero() {
        try (MockedStatic<Log4JCache> mockedStatic = mockStatic(Log4JCache.class)) {
            long currentTime = System.currentTimeMillis();
            long endTime = currentTime - 1000; // 1 second in past
            Timer timer = new Timer(currentTime, endTime);
            
            mockedStatic.when(Log4JCache::checkIfLog4JCacheIsNull).thenReturn(false);
            mockedStatic.when(() -> Log4JCache.getInstance(any(TimeUnit.class), anyLong()))
                    .thenReturn(log4JCache);
            when(log4JCache.get(LoggingLevelService.ADMIN_TIMER)).thenReturn(timer);
            
            long result = loggingLevelService.getRemainingTime();
            
            assertEquals(0, result);
        }
    }
}