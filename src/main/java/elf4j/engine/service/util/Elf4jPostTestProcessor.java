/*
 * MIT License
 *
 * Copyright (c) 2023 Qingtian Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package elf4j.engine.service.util;

import elf4j.engine.service.NativeLogServiceManager;
import elf4j.util.IeLogger;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * The Elf4jPostTestProcessor class implements the TestExecutionListener interface and is
 * responsible for shutting down the elf4j service after a test plan execution is finished. This is
 * particularly useful in a testing environment where resources need to be cleaned up after tests
 * are run to prevent memory leaks or other issues.
 */
public class Elf4jPostTestProcessor implements TestExecutionListener {
  /**
   * This method is called when the execution of a test plan is finished. It logs the completion of
   * the test plan and then shuts down the elf4j service. This ensures that the service does not
   * continue running and consuming resources after the tests have completed.
   *
   * @param testPlan the TestPlan object representing the finished test plan
   */
  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    IeLogger.INFO.log("Shutting down elf4j service after finishing {}", testPlan);
    NativeLogServiceManager.INSTANCE.shutdown();
  }
}
