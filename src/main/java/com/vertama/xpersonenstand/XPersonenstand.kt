package com.vertama.xpersonenstand

import de.domap.xpsw.xpsw2511.NachrichtG2G
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.util.JAXBSource
import org.w3c.dom.ls.LSInput
import org.w3c.dom.ls.LSResourceResolver
import org.xml.sax.SAXException
import java.io.InputStream
import java.io.Reader
import java.net.URI
import javax.xml.XMLConstants
import javax.xml.catalog.CatalogFeatures
import javax.xml.catalog.CatalogManager
import javax.xml.catalog.CatalogResolver
import javax.xml.validation.SchemaFactory

object XPersonenstand {

    /**
     * Validates a NachrichtG2G against the XPersonenstand schema.
     *
     * Schema resolution is fully offline: every external xs:import is routed through
     * src/main/resources/catalog.xml to a vendored copy under src/main/resources/schemas/.
     * `ACCESS_EXTERNAL_SCHEMA = ""` forbids the parser from opening any network connection,
     * so a missing catalog entry fails loudly here rather than silently fetching from the
     * upstream URL (which is how the previous design was flaky in environments where the
     * upstream returned 4xx/5xx or was unreachable).
     *
     * @throws SAXException If the validation failed.
     */
    @JvmStatic
    @Throws(SAXException::class)
    fun <T : NachrichtG2G> validateOrThrow(message: T) {
        validate(message)
    }

    private val catalogResolver: CatalogResolver by lazy {
        val catalogUrl = this::class.java.getResource("/catalog.xml")
            ?: error("XPersonenstand: /catalog.xml not on classpath — broken jar packaging?")
        CatalogManager.catalogResolver(CatalogFeatures.defaults(), catalogUrl.toURI())
    }

    private fun <T : Any> validate(obj: T) {
        val context = JAXBContext.newInstance(obj.javaClass)
        val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).apply {
            // Block network protocols only. file: + jar: are required for the legitimate
            // relative xs:includes between our own bundled XSDs (resolved as file:// during
            // development, jar:// at runtime). http(s)/ftp would silently fetch — we want
            // any xs:import the catalog can't map to fail loudly here instead.
            setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "file,jar")
            setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file,jar")
            resourceResolver = CatalogLSResourceResolver(catalogResolver)
        }

        val schema = sf.newSchema(
            this::class.java.getResource("/schemas/xpsw.domap.de/xpsw2511/xinneres.xpersonenstand.xsd")
        )
        val validator = schema.newValidator()
        validator.validate(JAXBSource(context, obj))
    }
}

/**
 * Adapter from [CatalogResolver] (which exposes URIResolver/EntityResolver) to
 * [LSResourceResolver] (which SchemaFactory consults during schema compilation).
 *
 * Returns `null` for any URL the catalog doesn't map — combined with
 * `ACCESS_EXTERNAL_SCHEMA = ""` on the SchemaFactory, this produces a hard error
 * instead of falling back to a network fetch.
 */
private class CatalogLSResourceResolver(
    private val catalogResolver: CatalogResolver,
) : LSResourceResolver {
    override fun resolveResource(
        type: String?,
        namespaceURI: String?,
        publicId: String?,
        systemId: String?,
        baseURI: String?,
    ): LSInput? {
        if (systemId == null) return null
        val source = try {
            catalogResolver.resolve(systemId, baseURI.orEmpty())
        } catch (_: Exception) {
            return null
        } ?: return null
        val resolvedUrl = source.systemId
        if (resolvedUrl.isNullOrEmpty() || resolvedUrl == systemId) return null
        return UrlBackedLSInput(publicId, systemId, resolvedUrl)
    }
}

private class UrlBackedLSInput(
    private val pid: String?,
    private val originalSystemId: String,
    private val resolvedUrl: String,
) : LSInput {
    override fun getByteStream(): InputStream = URI(resolvedUrl).toURL().openStream()
    override fun setByteStream(byteStream: InputStream?) {}
    override fun getCharacterStream(): Reader? = null
    override fun setCharacterStream(characterStream: Reader?) {}
    override fun getStringData(): String? = null
    override fun setStringData(stringData: String?) {}
    override fun getSystemId(): String = originalSystemId
    override fun setSystemId(systemId: String?) {}
    override fun getPublicId(): String? = pid
    override fun setPublicId(publicId: String?) {}
    override fun getBaseURI(): String? = null
    override fun setBaseURI(baseURI: String?) {}
    override fun getEncoding(): String? = null
    override fun setEncoding(encoding: String?) {}
    override fun getCertifiedText(): Boolean = false
    override fun setCertifiedText(certifiedText: Boolean) {}
}
