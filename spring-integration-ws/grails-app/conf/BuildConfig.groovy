grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
  inherits("global") {
    excludes 'xml-apis'
  }
  log "warn"
  repositories {
    grailsPlugins()
    grailsHome()
    grailsCentral()
    mavenCentral()
  }
  dependencies {
    runtime('org.springframework.integration:spring-integration-ws:2.0.5.RELEASE') {
      excludes([group: 'org.springframework'])
      excludes([name: 'spring-oxm'])
    }
    runtime('org.springframework.integration:spring-integration-xml:2.0.5.RELEASE') {
      excludes([group: 'org.springframework'])
    }
    runtime 'org.apache.ws.commons.schema:XmlSchema:1.4.7'
  }
}
