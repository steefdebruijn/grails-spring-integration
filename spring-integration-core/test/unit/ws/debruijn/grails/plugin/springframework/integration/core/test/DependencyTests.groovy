package ws.debruijn.grails.plugin.springframework.integration.core.test

import grails.test.GrailsUnitTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4)
class DependencyTests extends GrailsUnitTestCase {
  private static final String SI_CORE_CLASS = "org.springframework.integration.core.MessagingTemplate"

  @Test
  void loadingSpringIntegrationCoreClass_shouldNotFail() {
    loadSpringIntegrationCoreClass()
  }

  private void loadSpringIntegrationCoreClass() {
    Class.forName(SI_CORE_CLASS)
  }
}
