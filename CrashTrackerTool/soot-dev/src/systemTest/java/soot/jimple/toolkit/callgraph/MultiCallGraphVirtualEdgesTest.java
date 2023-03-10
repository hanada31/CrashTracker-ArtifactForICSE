package soot.jimple.toolkit.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Qidan He
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import soot.Scene;
import soot.jimple.toolkits.callgraph.Edge;
import soot.testing.framework.AbstractTestingFramework;


@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*" })
public class MultiCallGraphVirtualEdgesTest extends AbstractTestingFramework {

    private static final String TARGET_CLASS = "soot.jimple.toolkit.callgraph.ContainerMultiTypeSample";
    private static final String TARGET_METHOD = "void target()";

    @Test
    public void TestAsyncTaskBasicCG() {
        prepareTarget(methodSigFromComponents(TARGET_CLASS, TARGET_METHOD), TARGET_CLASS);
        boolean found = false;
        for (Edge edge : Scene.v().getCallGraph()) {
            //String sig = edge.getTgt().method().toString();
            System.out.println(edge);
            String sig = edge.getTgt().method().toString();

            if (edge.toString().contains("AHelper") && edge.toString().contains("handle"))
                found = true;
        }

        //Assert.assertTrue(found);
    }
}
