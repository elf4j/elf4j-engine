package org.slf4j;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.NOPMDCAdapter;

/** Initializes the SLF4J's MDC implementation */
public class MdcAdapterInitializer {
  private MdcAdapterInitializer() {}

  /**
   * Initializes the MDC implementation for SLF4J. If no MDC adapter is set or if the current
   * adapter is a NOPMDCAdapter, it sets the MDC adapter to a BasicMDCAdapter instance.
   */
  public static void initialize() {
    var mdcAdapter = MDC.getMDCAdapter();
    if (mdcAdapter == null || mdcAdapter instanceof NOPMDCAdapter) {
      MDC.setMDCAdapter(new BasicMDCAdapter());
    }
  }
}
