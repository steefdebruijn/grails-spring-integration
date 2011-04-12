package ws.debruijn.grails.plugin.springframework.integration.core.test

import grails.test.*

class DependencyTests extends GrailsUnitTestCase {
  private static final String SI_CORE_CLASS = "org.springframework.integration.core.MessagingTemplate"

  void testForLoadableSpringIntegrationCoreClasses() {
    try {
      loadSpringIntegrationCoreClass()
    } catch (ClassNotFoundException e) {
      fail "Class $SI_CORE_CLASS not found. Dependencies not setup correctly."
    }
  }

  private void loadSpringIntegrationCoreClass() {
    Class.forName(SI_CORE_CLASS)
  }
}
