package com.vertama.xpersonenstand

import de.domap.xpsw.xpsw2511.NachrichtG2G
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.util.JAXBSource
import org.xml.sax.SAXException
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory

object XPersonenstand {

    /**
     * Validates a NachrichtG2G against the XPersonenstand schema
     *
     * @throws SAXException If the validation failed
     */
    @JvmStatic
    @Throws(SAXException::class)
    fun <T : NachrichtG2G> validateOrThrow(message: T) {
        validate(message)
    }

    private fun <T: Any> validate(obj: T) {
        val context = JAXBContext.newInstance(obj.javaClass)
        val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

        val schema = sf.newSchema(
            this::class.java.getResource("/xpersonenstand-25.11/xinneres.xpersonenstand.xsd")
        )
        val validator = schema.newValidator()
        validator.validate(JAXBSource(context, obj))
    }
}
