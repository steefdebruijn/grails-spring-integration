package ws.debruijn.grails.plugin.springframework.integration.ws.test

import grails.test.GrailsUnitTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4)
class DependencyTests extends GrailsUnitTestCase {
  private static final String SI_WS_CLASS = "org.springframework.integration.ws.DefaultSoapHeaderMapper"
  private static final String SI_XML_CLASS = "org.springframework.integration.xml.DefaultXmlPayloadConverter"

  @Test
  void loadingSpringIntegrationWsClass_shouldNotFail() {
    loadSpringIntegrationWsClass()
  }

  private void loadSpringIntegrationWsClass() {
    Class.forName(SI_WS_CLASS)
  }

  @Test
  void loadingSpringIntegrationXmlClass_shouldNotFail() {
    loadSpringIntegrationXmlClass()
  }

  private void loadSpringIntegrationXmlClass() {
    Class.forName(SI_XML_CLASS)
  }
}
