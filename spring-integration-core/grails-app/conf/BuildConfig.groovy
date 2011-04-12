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
    runtime('org.springframework.integration:spring-integration-core:2.0.4.RELEASE') {
      excludes([group: 'org.springframework'])
    }
  }
}
