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

import groovy.json.JsonSlurper
import org.junit.Assert
import org.junit.Test

class DependencyResolverTest {

    private final Map pluginsUpdateCentre = new JsonSlurper().parse(new URL("https://updates.jenkins.io/stable/update-center.actual.json"))

    @Test
    void canResolveRecursively() {
        PluginReference kubernetesPlugin = new PluginReference(pluginsUpdateCentre.plugins.get("kubernetes"), "kubernetes", false)

        DependencyResolver dependencyResolver = new DependencyResolver()
        def answer = dependencyResolver.resolve(pluginsUpdateCentre, [kubernetesPlugin])

        Assert.assertNotNull(answer)
        Assert.assertTrue(answer.size() >=30)
    }

    @Test
    void canHandlePinnedPluginsWithLowerVersion() {
        PluginReference kubernetesPlugin = new PluginReference(pluginsUpdateCentre.plugins.get("kubernetes"), "kubernetes", false)
        PluginReference kubernetesCredsPinnedPlugin = new PluginReference(pluginsUpdateCentre.plugins.get("kubernetes-credentials"), "kubernetes-credentials:0.2.0", true)

        DependencyResolver dependencyResolver = new DependencyResolver()
        def answer = dependencyResolver.resolve(pluginsUpdateCentre, [kubernetesPlugin, kubernetesCredsPinnedPlugin])

        Assert.assertNotNull(answer)
        Assert.assertTrue(answer.size() >=30)
        Assert.assertEquals(answer.get("kubernetes-credentials").toListString(),2, answer.get("kubernetes-credentials").size())

        Assert.assertEquals(answer.get("kubernetes-credentials").toListString(),1, answer.get("kubernetes-credentials").findAll {
            it.comparableVersion.toString() == "0.2.0"
        }.size())

        Assert.assertEquals(answer.get("kubernetes-credentials").toListString(), 1, answer.get("kubernetes-credentials").findAll {
            it.comparableVersion.toString() == "0.9.0"
        }.size())
    }

    @Test
    void canHandlePinnedPluginsWithHigherVersion() {
        PluginReference kubernetesPlugin = new PluginReference(pluginsUpdateCentre.plugins.get("kubernetes"), "kubernetes", false)
        PluginReference kubernetesCredsPinnedPlugin = new PluginReference(pluginsUpdateCentre.plugins.get("kubernetes-credentials"), "kubernetes-credentials:1.0.0", true)

        DependencyResolver dependencyResolver = new DependencyResolver()
        def answer = dependencyResolver.resolve(pluginsUpdateCentre, [kubernetesPlugin, kubernetesCredsPinnedPlugin])

        Assert.assertNotNull(answer)
        Assert.assertTrue(answer.toMapString(), answer.size() >=30)

        Assert.assertEquals(answer.get("kubernetes-credentials").toListString(),1, answer.get("kubernetes-credentials").findAll {
            it.comparableVersion.toString() == "1.0.0"
        }.size())

        Assert.assertEquals(answer.get("kubernetes-credentials").toListString(),1, answer.get("kubernetes-credentials").findAll {
            it.comparableVersion.toString() == "0.9.0"
        }.size())
    }

    @Test
    void canHandlePinnedPluginsWithSameVersion() {
        PluginReference kubernetesPlugin = new PluginReference(pluginsUpdateCentre.plugins.get("kubernetes"), "kubernetes", false)
        PluginReference kubernetesCredsPinnedPlugin = new PluginReference(pluginsUpdateCentre.plugins.get("kubernetes-credentials"), "kubernetes-credentials:0.4.0", true)

        DependencyResolver dependencyResolver = new DependencyResolver()
        def answer = dependencyResolver.resolve(pluginsUpdateCentre, [kubernetesPlugin, kubernetesCredsPinnedPlugin])

        Assert.assertNotNull(answer)
        Assert.assertTrue(answer.size() >=30)

        Assert.assertEquals(answer.get("kubernetes-credentials").toListString(),1, answer.get("kubernetes-credentials").findAll {
            it.comparableVersion.toString() == "0.4.0"
        }.size())

        Assert.assertEquals(answer.get("kubernetes-credentials").toListString(),1, answer.get("kubernetes-credentials").findAll {
            it.comparableVersion.toString() == "0.9.0"
        }.size())
    }
}
