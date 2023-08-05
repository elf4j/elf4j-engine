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

package elf4j.engine;

import elf4j.Logger;
import elf4j.engine.service.LogServiceManager;

import java.util.function.Supplier;

public class Main {
    static Logger logger = Logger.instance();

    public static void main(String[] args) {
        logger.log("Hello, world!");
        logger.atTrace().log("It's a beautiful day");
        Logger info = logger.atInfo();
        info.log("... no matter on what level you say it");
        Logger warn = info.atWarn();
        warn.log("Houston, we do not have {} but let's do {}", "a problem", (Supplier) () -> "a drill");
        Throwable exception = new Exception("This is a drill");
        warn.atError().log(exception);
        logger.atInfo()
                .log(exception,
                        "i.e. Throwable always comes {}, then the following {} message and arguments work {}",
                        "first",
                        "optional",
                        (Supplier) () -> "as usual");
        Logger.instance()
                .atInfo()
                .atError()
                .atWarn()
                .atTrace()
                .atDebug()
                .log("Not a practical example but now the severity level is DEBUG");
        LogServiceManager.INSTANCE.shutdown();
    }
}
