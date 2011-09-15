package ws.debruijn.grails.plugin.springframework.integration.ws.test

class ServiceClassService {
  static final String requestNamespace = "http://known.ws/namespace/uri"
  static final String requestXsdFilename = "ws/debruijn/grails/plugin/springframework/integration/ws/test/service.xsd"

  private received = []

  String invoke(message) {
    received << message.toString()
    null
  }

  Boolean hasReceived(String key) {
    received.contains(key)
  }
}
