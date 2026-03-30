package org.webswing.server.common.service.stats;

import org.apache.commons.lang3.tuple.Pair;
import org.webswing.server.common.service.stats.logger.Aggregation;
import org.webswing.server.common.service.stats.logger.InstanceStats;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface StatisticsReader {

  Map<String, List<Aggregation>> summaryRulesMap = new HashMap<>();

  Map<String, Map<String, Pair<BigDecimal, Integer>>> getSummaryStats();

  Map<String, List<String>> getSummaryWarnings();

  /**
   * @return Map &lt; name_of_metric, Map &lt; timestamp, value &gt; &gt;
   */
  Map<String, Map<Long, Number>> getInstanceStats(String instance);

  /**
   * @return Map &lt; name_of_metric, value &gt;
   */
  Map<String, Number> getInstanceMetrics(String instanceId);

  List<String> getInstanceWarnings(String instance);

  List<String> getInstanceWarningHistory(String instance);

  Collection<InstanceStats> getAllInstanceStats();

}
