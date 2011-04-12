package ws.debruijn.grails.plugin.springframework.integration.core.test

import junit.framework.Test

class AllUnitTestSuite extends AllTestSuite {
  private static final String BASEDIR = "./test/unit"
  private static final String PATTERN = "**/*Tests.groovy"

  public static Test suite() {
    return suite(BASEDIR, PATTERN)
  }
}
