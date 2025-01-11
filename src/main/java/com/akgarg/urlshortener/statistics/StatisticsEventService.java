package com.akgarg.urlshortener.statistics;

public interface StatisticsEventService {

    void publishEvent(StatisticsEvent statisticsEvent);

}
