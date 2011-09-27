package ws.debruijn.grails.plugin.springframework.integration.ws.test

import grails.test.GrailsUnitTestCase
import groovy.xml.DOMBuilder
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.springframework.integration.Message
import org.springframework.integration.MessagingException
import org.springframework.integration.support.MessageBuilder

@RunWith(JUnit4)
class MessageRoutingTests extends GrailsUnitTestCase {

  def siWsRequestsChannel
  def serviceClassService
  def pojoClassService

  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
  DocumentBuilder builder = factory.newDocumentBuilder()

  @Test
  void inputChannel_shouldNotBeNull() {
    assertNotNull 'input channel is null', siWsRequestsChannel
  }

  @Test(expected = MessagingException)
  void messageWithoutNamespace_shouldThrowMessagingException() {
    String s = '<message>content</message>'
    Message<DOMSource> message = createMessage(s)
    siWsRequestsChannel.send(message)
  }

  @Test(expected = MessagingException)
  void messageWithUnknownNamespace_shouldThrowMessagingException() {
    String s = '<message xmlns="http://unknown.ws/namespace/uri">content</message>'
    Message<DOMSource> message = createMessage(s)
    siWsRequestsChannel.send(message)
  }

  @Test
  void messageWithKnownNamespaceWithoutPrefix_shouldBeDeliveredToServiceClass() {
    String s = '<message xmlns="http://known.ws/namespace/uri">contentWithoutPrefix</message>'
    Message<DOMSource> message = createMessage(s)
    assertFalse 'serviceClass already contains my key', serviceClassService.hasReceived('contentWithoutPrefix')
    siWsRequestsChannel.send(message)
    assertTrue 'serviceClass did not receive my key', serviceClassService.hasReceived('contentWithoutPrefix')
  }

  @Test
  void messageWithKnownNamespaceWithPrefix_shouldBeDeliveredToServiceClass() {
    String s = '<prefix:message xmlns:prefix="http://known.ws/namespace/uri">contentWithPrefix</prefix:message>'
    Message<DOMSource> message = createMessage(s)
    assertFalse 'serviceClass already contains my key', serviceClassService.hasReceived('contentWithPrefix')
    siWsRequestsChannel.send(message)
    assertTrue 'serviceClass did not receive my key', serviceClassService.hasReceived('contentWithPrefix')
  }

  @Test
  void message_shouldBeDeliveredAsPOJOToServiceClass() {
    String s = '<prefix:message xmlns:prefix="http://pojo.ws/namespace/uri"><prefix:content>pojoContent</prefix:content></prefix:message>'
    Message<DOMSource> message = createMessage(s)
    assertFalse 'serviceClass already contains my key', pojoClassService.hasReceived('pojoContent')
    siWsRequestsChannel.send(message)
    assertTrue 'serviceClass did not receive my key', pojoClassService.hasReceived('pojoContent')
  }

  private Message<DOMSource> createMessage(String messageContent) {
    MessageBuilder.withPayload(
            new DOMSource(
                    DOMBuilder.newInstance().parseText(messageContent).
                            documentElement)).build()
  }

}
