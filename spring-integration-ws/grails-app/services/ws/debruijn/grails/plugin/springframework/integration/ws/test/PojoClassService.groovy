package ws.debruijn.grails.plugin.springframework.integration.ws.test

class PojoClassService {
  static final String requestNamespace = "http://pojo.ws/namespace/uri"
  static final String requestXsdFilename = "ws/debruijn/grails/plugin/springframework/integration/ws/test/service.xsd"
  static final RequestType = PojoObject

  private received = []

  String invoke(PojoObject message) {
    received << message.messageContent
    null
  }

  Boolean hasReceived(String key) {
    received.contains(key)
  }
}
