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

import groovy.json.JsonSlurperClassic

/**
 * Generates a list of plugins used the predefined plugins and jenkins update centre
 */
class Generator implements Serializable {

    private final DependencyResolver dependencyResolver

    /**
     * Generates a list of plugins used the predefined plugins and jenkins update centre
     */
    Generator() {
        this.dependencyResolver = new DependencyResolver()
    }

    /**
     * Process the list of plugins and resolve their dependencies using the folloowing rules:
     * 1. pick the highest version if multiples are found
     * 2. pick the pinned version if any pinned are found
     *
     * @param pluginTemplatePath path to template plugin file
     * @param updateCentrePath path to update centre file
     * @return a list of plugins which jenkins needs to download
     */
    List<PluginReference> process(String pluginTemplatePath, String updateCentrePath) {
        return process(new File(new URI(pluginTemplatePath)), new File(new URI(updateCentrePath)))
    }

    /**
     * Process the list of plugins and resolve their dependencies using the folloowing rules:
     * 1. pick the highest version if multiples are found
     * 2. pick the pinned version if any pinned are found
     *
     * @param pluginTemplatePath file for template plugin
     * @param updateCentrePath file for update centre
     * @return a list of plugins which jenkins needs to download
     */
    List<PluginReference> process(File pluginTemplatePath, File updateCentrePath) {
        Map updateCentreMap = parseUpdateCentre(updateCentrePath)
        List<PluginReference> templatePlugins = parsePluginTemplate(updateCentreMap, pluginTemplatePath)

        Map<String, List<PluginReference>> resolvedPlugins = dependencyResolver.resolve(updateCentreMap, templatePlugins)

        List<PluginReference> answer = flattenResolvedPlugins(resolvedPlugins)

        return answer
    }

    private Map parseUpdateCentre(File updateCentrePath) {
        return new JsonSlurperClassic().parse(updateCentrePath)
    }

    private List<PluginReference> parsePluginTemplate(Map updateCentreMap, File pluginTemplatePath) {
        List<PluginReference> answer = []

        String pluginTemplateContent = pluginTemplatePath.getText("UTF-8")
        pluginTemplateContent.split('\n').each { line ->
            Map nameMap = PluginReference.parse(line, true)

            answer.add(new PluginReference(updateCentreMap.plugins.get(nameMap.get("name")), line, (Boolean) nameMap.get("isPinned")))
        }

        return answer
    }

    private List<PluginReference> flattenResolvedPlugins(final Map<String, List<PluginReference>> resolvedPlugins) {
        List<PluginReference> answer = []

        resolvedPlugins.each { entry ->
            PluginReference pinnedReference = entry.value.find { reference -> reference.isPinned }
            if (pinnedReference == null) {
                if (entry.value.size() == 1) {
                    //Its not pinned and we've only got 1 version to pick, so use that
                    answer.add(entry.value.get(0))
                } else if (entry.value.size() > 1) {
                    //If we've got multiple versions, pick the highest
                    PluginReference lastReference = PluginReference.sortByVersion(entry.value).last()
                    answer.add(lastReference)

                    println("Highest -> Plugin '${entry.key}' has '${entry.value.size()}' different versions. Picking highest which is: ${lastReference.toString()}")
                } else {
                    throw new Exception("We should never get here!")
                }
            } else {
                //If its pinned, always use that version
                println("Pinned -> Plugin '${entry.key}' is pinned to '${pinnedReference.toString()}'")

                answer.add(pinnedReference)
            }
        }

        return PluginReference.sortByName(answer)
    }
}
