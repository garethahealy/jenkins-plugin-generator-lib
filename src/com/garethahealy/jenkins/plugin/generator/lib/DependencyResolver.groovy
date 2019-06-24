/*-
 * #%L
 * GarethHealy :: Jenkins Plugin Generator Lib
 * %%
 * Copyright (C) 2013 - 2022 Gareth Healy
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

/**
 * Resolves plugin dependencies
 */
class DependencyResolver implements Serializable {

    /**
     * Builds a map of plugins
     *
     * @param updateCentreMap jenkins update centre for a specific version
     * @param templatePlugins list of plugins to search for
     * @return map which has a key of the plugin name and a list of plugins found by searching through templatePlugins for its value
     */
    Map<String, List<PluginReference>> resolve(final Map updateCentreMap, final List<PluginReference> templatePlugins) {
        Map<String, List<PluginReference>> answer = [:]
        templatePlugins.each { current ->
            println("Collecting dependencies for: '${current.toString()}'")

            resolveDependenciesRecursively(updateCentreMap, current, answer)
        }

        return answer
    }

    private void resolveDependenciesRecursively(final Map updateCentreMap, final PluginReference pluginReference, Map<String, List<PluginReference>> resolvedPlugins) {
        if (resolvedPlugins.containsKey(pluginReference.name)) {
            //We've already found this plugin name
            List<PluginReference> foundPluginReferences = resolvedPlugins.get(pluginReference.name)
            if (pluginReference.isPinned) {
                //If its pinned, always store it
                foundPluginReferences.add(pluginReference)
            } else {
                //Only add the plugin if we've not seen the same version before
                if (foundPluginReferences.find { it.compareTo(pluginReference) == 0 } == null) {
                    foundPluginReferences.add(pluginReference)
                }
            }
        } else {
            //We've not seen this plugin name before
            resolvedPlugins.put(pluginReference.name, [pluginReference])
        }

        //Collect all the dependencies of this plugin recursively
        pluginReference.reference.get("dependencies").each { dependency ->
            PluginReference dependencyReference = getPluginReference(updateCentreMap, dependency.get("name"), dependency.get("version", null))

            resolveDependenciesRecursively(updateCentreMap, dependencyReference, resolvedPlugins)
        }
    }

    private PluginReference getPluginReference(final Map pluginsUpdateCentre, final String name, String version) {
        String nameVersion = version == null ? name : name + ":" + version
        return new PluginReference(pluginsUpdateCentre.plugins.get(name), nameVersion, false)
    }
}
