import com.garethahealy.jenkins.plugin.generator.lib.FileLineConverter
import com.garethahealy.jenkins.plugin.generator.lib.Generator

def call(Map params = [: ]) {
    validate(params)

    this.echo "Generating plugins text for: '${params.pluginTemplatePath}' using update centre: '${params.updateCentrePath}'"

    def plugins = new Generator().process(params.pluginTemplatePath, params.updateCentrePath)
    def content = new FileLineConverter().convert(plugins)

    this.writeFile file: "${WORKSPACE}/plugins.txt", text: content, encoding: "UTF8"

    this.echo "Finished generating ${WORKSPACE}/plugins.txt"
}

private def validate(Map params) {
    if (params.pluginTemplatePath == null || params.pluginTemplatePath.trim().length() <= 0) {
        this.error "'pluginTemplatePath' is null or empty"
    }

    if (params.updateCentrePath == null || params.updateCentrePath.trim().length() <= 0) {
        this.error "'updateCentrePath' is null or empty"
    }
}