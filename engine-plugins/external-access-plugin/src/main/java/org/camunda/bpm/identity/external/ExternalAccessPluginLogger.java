package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.commons.logging.BaseLogger;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalAccessPluginLogger extends BaseLogger {

  public static final String PROJECT_CODE = "EXTERNAL-ACCESS-PLUGIN";

  public static final ExternalAccessPluginLogger LOGGER = BaseLogger.createLogger(ExternalAccessPluginLogger.class, PROJECT_CODE, "org.camunda.external-access.plugin", "01");

  public void logNoDataFormatsInitiailized(String dataFormatDescription, String reason) {
    logInfo(
        "001", "Cannot initialize %s: %s", dataFormatDescription, reason);
  }

  public ProcessEngineException fallbackSerializerCannotDeserializeObjects() {
    return new ProcessEngineException(exceptionMessage(
        "002", "Fallback serializer cannot handle deserialized objects"));
  }

  public void writeLog(String message) {
    logInfo("001", message);
  }
}
