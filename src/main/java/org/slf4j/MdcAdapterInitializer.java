package org.slf4j;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

public class MdcAdapterInitializer {
    private MdcAdapterInitializer() {}

    public static void initialize() {
        MDCAdapter byOtherSlf4jProvider = MDC.mdcAdapter;
        if (byOtherSlf4jProvider == null || byOtherSlf4jProvider instanceof NOPMDCAdapter) {
            MDC.mdcAdapter = new BasicMDCAdapter();
        }
    }
}
