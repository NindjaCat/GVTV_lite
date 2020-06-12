//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.02.06 um 08:50:02 PM CET 
//


package channels;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Servers">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Server" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="ip" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="ports" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="command" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "servers"
})
@XmlRootElement(name = "Settings")
public class Settings {

    @XmlElement(name = "Servers", required = true)
    protected Settings.Servers servers;

    /**
     * Ruft den Wert der servers-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Settings.Servers }
     *     
     */
    public Settings.Servers getServers() {
        return servers;
    }

    /**
     * Legt den Wert der servers-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Settings.Servers }
     *     
     */
    public void setServers(Settings.Servers value) {
        this.servers = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="Server" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="ip" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="ports" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="command" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "server"
    })
    public static class Servers {

        @XmlElement(name = "Server", required = true)
        protected List<Settings.Servers.Server> server;

        /**
         * Gets the value of the server property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the server property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getServer().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Settings.Servers.Server }
         * 
         * 
         */
        public List<Settings.Servers.Server> getServer() {
            if (server == null) {
                server = new ArrayList<Settings.Servers.Server>();
            }
            return this.server;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="ip" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="ports" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="protocol" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="command" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "id",
            "ip",
            "model",
            "ports",
            "protocol",
            "command"
        })
        public static class Server {

            protected byte id;
            @XmlElement(required = true)
            protected String ip;
            @XmlElement(required = true)
            protected String model;
            @XmlElement(required = true)
            protected String ports;
            @XmlElement(required = true)
            protected String protocol;
            @XmlElement(required = true)
            protected String command;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             */
            public byte getId() {
                return id;
            }

            /**
             * Legt den Wert der id-Eigenschaft fest.
             * 
             */
            public void setId(byte value) {
                this.id = value;
            }

            /**
             * Ruft den Wert der ip-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getIp() {
                return ip;
            }

            /**
             * Legt den Wert der ip-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setIp(String value) {
                this.ip = value;
            }

            /**
             * Ruft den Wert der model-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getModel() {
                return model;
            }

            /**
             * Legt den Wert der model-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setModel(String value) {
                this.model = value;
            }

            /**
             * Ruft den Wert der ports-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPorts() {
                return ports;
            }

            /**
             * Legt den Wert der ports-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPorts(String value) {
                this.ports = value;
            }

            /**
             * Ruft den Wert der protocol-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getProtocol() {
                return protocol;
            }

            /**
             * Legt den Wert der protocol-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setProtocol(String value) {
                this.protocol = value;
            }

            /**
             * Ruft den Wert der command-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCommand() {
                return command;
            }

            /**
             * Legt den Wert der command-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCommand(String value) {
                this.command = value;
            }

        }

    }

}
