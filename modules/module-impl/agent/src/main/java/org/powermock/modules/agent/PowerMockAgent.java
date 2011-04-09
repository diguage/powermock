/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.powermock.modules.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * This is the "agent class" that initializes the PowerMock "Java agent". It is not intended for use in client code.
 * It must be public, however, so the JVM can call the {@code premain} method, which as the name implies is called
 * <em>before</em> the {@code main} method.
 *
 * @see #premain(String, Instrumentation)
 */
public final class PowerMockAgent
{
    static final String javaSpecVersion = System.getProperty("java.specification.version");
    static final boolean jdk6OrLater = "1.6".equals(javaSpecVersion) || "1.7".equals(javaSpecVersion);

    private static Instrumentation instrumentation;

    private PowerMockAgent() {}

    public static boolean isJava6OrLater() { return jdk6OrLater; }

    /**
     * This method must only be called by the JVM, to provide the instrumentation object.
     * In order for this to occur, the JVM must be started with "-javaagent:powermock-module-javaagent-nnn.jar" as a command line parameter
     * (assuming the jar file is in the current directory).
     *
     * @param agentArgs zero or more <em>instrumentation tool specifications</em> (separated by semicolons if more than
     *                  one); each tool specification must be expressed as "&lt;tool class name>[=tool arguments]", with
     *                  fully qualified class names for classes available in the classpath; tool arguments are optional
     * @param inst      the instrumentation service provided by the JVM
     */
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        initialize(agentArgs, inst);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        initialize(agentArgs, inst);
    }

    private static void initialize(String agentArgs, Instrumentation inst) throws IOException {
        instrumentation = inst;
    }

    public static Instrumentation instrumentation()  {
        verifyInitialization();
        return instrumentation;
    }

    public static void verifyInitialization()
    {
        if (instrumentation == null) {
            new AgentInitialization().initializeAccordingToJDKVersion();
        }
    }

    public static boolean initializeIfNeeded()
    {
        if (instrumentation == null) {
            try {
                new AgentInitialization().initializeAccordingToJDKVersion();
                return true;
            }
            catch (RuntimeException e) {
                e.printStackTrace(); // makes sure the exception gets printed at least once
                throw e;
            }
        }

        return false;
    }

    public static void initializeIfPossible() {
        if (jdk6OrLater) {
            initializeIfNeeded();
        }
    }
}
