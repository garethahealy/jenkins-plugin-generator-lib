/*-
 * #%L
 * GarethHealy :: Jenkins Plugin Generator Lib
 * %%
 * Copyright (C) 2013 - 2018 Gareth Healy
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.garethahealy.jenkins.plugin.generator.lib

import org.junit.Assert
import org.junit.Test

class GeneratorTest {

    @Test
    void canProcess() {
        String localPath = new File("").getAbsolutePath()

        Generator generator = new Generator()
        List<PluginReference> plugins = generator.process(new File(new URI("file://${localPath}/test/resources/latest-plugins.txt.template")), new File(new URI("file://${localPath}/test/resources/jenkins-2_50_1-update-centre.json")))

        Assert.assertNotNull(plugins)
        Assert.assertEquals(174, plugins.size())
        Assert.assertEquals(174, plugins.unique { a, b -> a.reference.get("name") <=> b.reference.get("name") }.size())
    }

    @Test
    void canHandlePinnedPluginsToLow() {
        String localPath = new File("").getAbsolutePath()

        Generator generator = new Generator()
        List<PluginReference> plugins = generator.process(new File(new URI("file://${localPath}/test/resources/low-pinned-plugins.txt.template")), new File(new URI("file://${localPath}/test/resources/jenkins-2_50_1-update-centre.json")))

        Assert.assertNotNull(plugins)
        Assert.assertEquals(174, plugins.size())
        Assert.assertEquals(174, plugins.unique { a, b -> a.reference.get("name") <=> b.reference.get("name") }.size())

        Assert.assertEquals(1, plugins.findAll {
            it.reference.name == "kubernetes" && it.comparableVersion.toString() == "0.1" && it.isPinned
        }.size())
    }

    @Test
    void canHandleMultipleVersions() {
        String localPath = new File("").getAbsolutePath()

        Generator generator = new Generator()
        List<PluginReference> plugins = generator.process(new File(new URI("file://${localPath}/test/resources/clashing-plugins.txt.template")), new File(new URI("file://${localPath}/test/resources/jenkins-2_50_1-update-centre.json")))

        Assert.assertNotNull(plugins)
        Assert.assertEquals(31, plugins.size())
        Assert.assertEquals(31, plugins.unique { a, b -> a.reference.get("name") <=> b.reference.get("name") }.size())

        Assert.assertEquals(1, plugins.findAll {
            it.reference.name == "kubernetes" && it.comparableVersion.toString() == "1.14.1"
        }.size())
    }
}
