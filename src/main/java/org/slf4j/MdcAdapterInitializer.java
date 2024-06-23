package org.slf4j;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

/** Initializes the SLF4J's MDC implementation */
public class MdcAdapterInitializer {
  private MdcAdapterInitializer() {}

  /**
   * Initializes the MDC implementation for SLF4J. If no MDC adapter is set or if the current
   * adapter is a NOPMDCAdapter, it sets the MDC adapter to a BasicMDCAdapter instance.
   */
  public static void initialize() {
    MDCAdapter byOtherSlf4jProvider = MDC.mdcAdapter;
    if (byOtherSlf4jProvider == null || byOtherSlf4jProvider instanceof NOPMDCAdapter) {
      MDC.mdcAdapter = new BasicMDCAdapter();
    }
  }
}
