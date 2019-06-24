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

import com.cloudbees.groovy.cps.NonCPS

/**
 * Stores reference information about a plugin
 */
class PluginReference implements Comparable<PluginReference>, Serializable {

    public String name
    public Map reference
    public ComparableVersionSerializable comparableVersion
    public Boolean isPinned

    /**
     * Stores reference information about a plugin
     *
     * @param reference reference from update centre
     * @param nameWithVersion name of plugin with optional version
     * @param isPinned is the version pinned
     */
    PluginReference(Map reference, String nameWithVersion, Boolean isPinned) {
        Map parsedName = parse(nameWithVersion, false)

        this.name = parsedName.get("name")
        this.comparableVersion = new ComparableVersionSerializable(parsedName.get("version") ?: reference.get("version"))
        this.reference = reference
        this.isPinned = isPinned
    }

    /**
     * Parse a plugin name which might contain a version, i.e.: kubernetes:1.12.0
     *
     * @param nameWithVersion name of plugin with optional version
     * @param isLoadedFromTemplate if its loaded from template, and contains ':' then mark it as a pinned version
     * @return name, version and whether the plugin isPinned
     */
    @NonCPS
    static Map parse(String nameWithVersion, Boolean isLoadedFromTemplate) {
        if (nameWithVersion.contains(':')) {
            def splitName = nameWithVersion.split(':')
            return [name: splitName[0], version: splitName[1], isPinned: isLoadedFromTemplate]
        } else {
            return [name: nameWithVersion]
        }
    }

    /**
     * Sorts a list by name
     * NOTE: Requires NonCPS due to Jenkins: https://issues.jenkins-ci.org/browse/JENKINS-44924
     *
     * @param self list to sort
     * @return sorted list
     */
    @NonCPS
    static List<PluginReference> sortByName(List<PluginReference> self) {
        return self.sort { a, b -> a.reference.name <=> b.reference.name }
    }

    /**
     * Sorts a list by version
     * NOTE: Requires NonCPS due to Jenkins: https://issues.jenkins-ci.org/browse/JENKINS-44924
     *
     * @param self list to sort
     * @return sorted list
     */
    @NonCPS
    static List<PluginReference> sortByVersion(List<PluginReference> self) {
        return self.sort { a, b -> a.comparableVersion <=> b.comparableVersion }
    }

    @Override
    int compareTo(PluginReference o) {
        return comparableVersion <=> o.comparableVersion
    }

    @Override
    String toString() {
        return "${name}:${comparableVersion.toString()}"
    }
}
