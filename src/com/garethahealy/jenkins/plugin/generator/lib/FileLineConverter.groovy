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
 * Converts a list to a string which can be written to a file
 */
class FileLineConverter implements Serializable {

    /**
     * Converts a list to a string separated by new lines
     *
     * @param plugins plugins to output
     * @return string separated by new lines
     */
    String convert(final List<PluginReference> plugins) {
        return plugins.collect { it.toString() }.join('\n')
    }
}
