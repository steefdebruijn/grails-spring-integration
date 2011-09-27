package ws.debruijn.grails.plugin.springframework.integration.ws.test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "message", namespace = "http://pojo.ws/namespace/uri")
public class PojoObject {
  @XmlElement(name = "content", namespace = "http://pojo.ws/namespace/uri")
  String messageContent;
}
