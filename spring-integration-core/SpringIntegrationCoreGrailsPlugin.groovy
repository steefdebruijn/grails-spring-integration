import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

class SpringIntegrationCoreGrailsPlugin {
  static final String PACKAGE_NAME = 'ws.debruijn.grails.plugin.springframework.integration.core'
  static final Log log = LogFactory.getLog("$PACKAGE_NAME.${this.simpleName}")

  def version = "2.0.5.1.RELEASE"
  def grailsVersion = "1.3.7 > *"
  def dependsOn = [:]
  def pluginExcludes = [
          "grails-app/views/error.gsp"
  ]

  def author = "Steef de Bruijn"
  def authorEmail = "steef@debruijn.ws"
  def title = "Spring Integration support for Grails - Core"
  def description = '''\\
This plugin provides basic dependency support for the Spring Integration Core jar.

Please post your issues on GitHub
'''

  def documentation = "http://steefdebruijn.github.com/grails-spring-integration"
}
