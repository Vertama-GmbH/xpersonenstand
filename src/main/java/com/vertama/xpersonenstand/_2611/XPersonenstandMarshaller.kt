package com.vertama.xpersonenstand._2611

import de.domap.xpsw.xpsw2611.NachrichtG2G
import jakarta.xml.bind.JAXB
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.MarshalException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object XPersonenstandMarshaller {

    /**
     * Marshal the content tree into an output stream.
     *
     * @param message The root of the content tree to be marshalled.
     * @param file The XML will be added to this file.
     * @exception JAXBException If any unexpected problem occurs during the marshalling.
     * @exception MarshalException If the Marshaller is unable to marshal the message (or any object reachable from it)
     */
    @JvmStatic
    @Throws(JAXBException::class, MarshalException::class)
    fun <T : NachrichtG2G> marshalToFile(message: T, file: File) {
        JAXB.marshal(message, file)
    }

    /**
     * Marshal the content tree into an output stream.
     *
     * @param message The root of the content tree to be marshalled.
     * @param outputStream The XML will be added to this output stream.
     * @exception JAXBException If any unexpected problem occurs during the marshalling.
     * @exception MarshalException If the Marshaller is unable to marshal the message (or any object reachable from it)
     */
    @JvmStatic
    @Throws(JAXBException::class, MarshalException::class)
    fun <T : NachrichtG2G> marshalToOutputStream(message: T, outputStream: OutputStream) {
        JAXB.marshal(message, outputStream)
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param file The XML file.
     * @return The object build from the XML file
     */
    @JvmStatic
    fun <T : Any> unmarshalFromFile(file: File, clazz: Class<T>): T = JAXB.unmarshal(file, clazz)

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param inputStream The input stream of the xml.
     * @return The object build from the input stream
     */
    @JvmStatic
    @Throws(JAXBException::class, MarshalException::class)
    fun <T : Any> unmarshalFromInputStream(inputStream: InputStream, clazz: Class<T>): T =
        JAXB.unmarshal(inputStream, clazz)
}
