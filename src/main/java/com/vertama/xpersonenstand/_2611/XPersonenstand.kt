package com.vertama.xpersonenstand._2611

import de.domap.xpsw.xpsw2611.NachrichtG2G
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
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

/**
 * Local-only URI schemes that the schema parser is allowed to access.
 * Shared between the [SchemaFactory] `accessExternalSchema` property and the
 * [CatalogLSResourceResolver] allowlist so the two cannot drift apart.
 *
 * - `file`, `jar` — standard JDK schemes
 * - `nested`       — Spring Boot 3.2+ fat-JAR classloader
 * - `vfs`          — JBoss / WildFly virtual filesystem
 * - `resource`     — OSGi and some embedded containers
 */
private val LOCAL_SCHEMES = setOf("file", "jar", "nested", "vfs", "resource")

object XPersonenstand {

    /**
     * Validates a NachrichtG2G against the XPersonenstand schema.
     *
     * Schema resolution is fully offline: every external xs:import is routed through
     * src/main/resources/catalog.xml to a vendored copy under src/main/resources/schemas/.
     * `ACCESS_EXTERNAL_SCHEMA` (set to [LOCAL_SCHEMES]) forbids the parser from opening any network connection,
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

    /** Cached JAXBContext — expensive to create, thread-safe and reusable once built. */
    private val jaxbContext: JAXBContext by lazy {
        JAXBContext.newInstance(NachrichtG2G::class.java)
    }

    /** Cached compiled Schema — schema compilation is heavy and the result is thread-safe. */
    private val schema: Schema by lazy {
        val allowedProtocols = LOCAL_SCHEMES.joinToString(",")
        val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).apply {
            // Allow local protocols only. The JAXP security check on these properties runs
            // BEFORE the LSResourceResolver is consulted, so relative xs:include references
            // (like xpersonenstand-baukasten.xsd) whose base URI uses a container-specific
            // scheme (nested:, vfs:, …) will be rejected unless that scheme is listed here.
            setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, allowedProtocols)
            setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, allowedProtocols)
            resourceResolver = CatalogLSResourceResolver(catalogResolver)
        }
        sf.newSchema(
            this::class.java.getResource("/schemas/xpsw.domap.de/xpsw2611/xinneres.xpersonenstand.xsd")
        )
    }

    private fun <T : Any> validate(obj: T) {
        val validator = schema.newValidator()
        validator.validate(JAXBSource(jaxbContext, obj))
    }
}

/**
 * Adapter from [CatalogResolver] (which exposes URIResolver/EntityResolver) to
 * [LSResourceResolver] (which SchemaFactory consults during schema compilation).
 *
 * Resolves relative system IDs relative to the baseURI to bypass JAXP protocol checks
 * on custom classpath loaders (e.g. Spring Boot's nested: or JBoss's vfs:).
 * Returns `null` for any external URL the catalog doesn't map — combined with
 * `ACCESS_EXTERNAL_SCHEMA` (set to [LOCAL_SCHEMES]) on the SchemaFactory, this produces a hard error
 * instead of falling back to a network fetch.
 */
private class CatalogLSResourceResolver(
    private val catalogResolver: CatalogResolver,
) : LSResourceResolver {

    private val localSchemes = LOCAL_SCHEMES

    override fun resolveResource(
        type: String?,
        namespaceURI: String?,
        publicId: String?,
        systemId: String?,
        baseURI: String?,
    ): LSInput? {
        if (systemId == null) return null

        // 1. Try resolving via catalog
        val source = try {
            catalogResolver.resolve(systemId, baseURI.orEmpty())
        } catch (_: Exception) {
            null
        }
        val resolvedUrl = source?.systemId
        if (!resolvedUrl.isNullOrEmpty() && resolvedUrl != systemId) {
            if (!isLocalUrl(resolvedUrl)) return null
            return UrlBackedLSInput(publicId, systemId, resolvedUrl)
        }

        // 2. Resolve relative includes/imports relative to baseURI
        if (!baseURI.isNullOrEmpty() && !isAbsolute(systemId)) {
            val resolvedRelative = try {
                URI(baseURI).resolve(systemId).toString()
            } catch (_: Exception) {
                null
            }
            if (resolvedRelative != null) {
                if (!isLocalUrl(resolvedRelative)) return null
                return UrlBackedLSInput(publicId, systemId, resolvedRelative)
            }
        }

        return null
    }

    /** Returns true only if [url] uses a known-safe local scheme. */
    private fun isLocalUrl(url: String): Boolean {
        val scheme = try { URI(url).scheme?.lowercase() } catch (_: Exception) { null }
        return scheme != null && scheme in localSchemes
    }

    private fun isAbsolute(uri: String): Boolean {
        return try {
            URI(uri).isAbsolute
        } catch (_: Exception) {
            uri.contains(":")
        }
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
