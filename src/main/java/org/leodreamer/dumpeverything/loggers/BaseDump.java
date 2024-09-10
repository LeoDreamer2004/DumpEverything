package org.leodreamer.dumpeverything.loggers;

import java.util.List;
import java.util.Map;

public interface BaseDump {
    String getTypeName();

    Map<String, List<String>> getIdentifierMap();

    Map<String, List<String>> getTagMap();
}
