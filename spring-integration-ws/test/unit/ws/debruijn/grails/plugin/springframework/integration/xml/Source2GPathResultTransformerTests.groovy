package ws.debruijn.grails.plugin.springframework.integration.xml

import grails.test.GrailsUnitTestCase
import groovy.util.slurpersupport.GPathResult
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.springframework.xml.transform.StringSource

@RunWith(JUnit4)
class Source2GPathResultTransformerTests extends GrailsUnitTestCase {

  @Test(expected = TransformerException)
  void nullMessage_throwsTransformerException() {
    Source source = null
    new Source2GPathResultTransformer().transform(source)
  }

  @Test(expected = TransformerException)
  void emptyMessage_throwsTransformerException() {
    Source source = new StringSource('')
    new Source2GPathResultTransformer().transform(source)
  }

  @Test(expected = TransformerException)
  void invalidMessage_throwsTransformerException() {
    Source source = new StringSource('<this>isnovalidxml')
    new Source2GPathResultTransformer().transform(source)
  }

  @Test
  void validSingleLevelMessage_returnsGPathResultWithContent() {
    Source source = new StringSource('<valid>xml</valid>')
    GPathResult result = new Source2GPathResultTransformer().transform(source)
    assertNotNull 'result is not null', result
    assertEquals 'message content', 'xml', result.toString()
  }

  @Test
  void validMultiLevelMessage_returnsGPathResultWithLeveledContent() {
    Source source = new StringSource('<valid><nested>xml</nested></valid>')
    GPathResult result = new Source2GPathResultTransformer().transform(source)
    assertNotNull 'result is not null', result
    assertEquals 'message content', 'xml', result.nested.toString()
  }
}
