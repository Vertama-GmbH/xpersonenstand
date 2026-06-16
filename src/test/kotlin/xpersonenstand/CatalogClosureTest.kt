package xpersonenstand

import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.fail

/**
 * Build-time guard: asserts that every external xs:import schemaLocation reachable from any
 * bundled XSD is mapped by catalog.xml and points to a vendored file under schemas/.
 *
 * This is the test the design hinges on. Without it, someone updating an XSD (e.g. bumping
 * xinneres-basisnachricht from /7/ to /8/) could land a change that introduces a new external
 * URL with no corresponding vendored copy — and the lib would silently regress to the broken
 * "fetch from xoev.de at validate time" behaviour we eliminated.
 *
 * What this checks:
 *   1. Every http(s):// URL appearing in any bundled XSD's xs:import schemaLocation is mapped
 *      by an entry in catalog.xml.
 *   2. Every catalog entry's target file exists under src/main/resources/.
 *   3. Catalog has no entries pointing to URLs that no bundled XSD actually imports
 *      (catches stale mappings).
 *
 * What this does NOT check (out of scope):
 *   - Whether vendored files are byte-identical to upstream. That's a "snapshot freshness"
 *     concern, handled separately (e.g. by a refresh script run on demand).
 */
class CatalogClosureTest {

    private val resourcesRoot = File("src/main/resources")
    private val schemasRoot = File(resourcesRoot, "schemas")
    private val catalogFile = File(resourcesRoot, "catalog.xml")

    @Test
    fun `every external xs-import is mapped by catalog and the target exists`() {
        val catalogEntries = readCatalog()
        val externalImports = collectExternalImports()

        val unmapped = externalImports.filter { url -> url !in catalogEntries.keys }
        unmapped.isEmpty() || fail(
            "Found xs:import schemaLocation URLs not mapped in catalog.xml:\n" +
                unmapped.joinToString("\n") { "  - $it" } +
                "\n\nFix: vendor each XSD under schemas/<host>/<full-url-path>/<filename>.xsd " +
                "and add a matching <uri name=\"<url>\" uri=\"schemas/...\"/> entry to catalog.xml."
        )

        val missingTargets = catalogEntries.filter { (_, path) -> !File(resourcesRoot, path).exists() }
        missingTargets.isEmpty() || fail(
            "Catalog entries point to non-existent files:\n" +
                missingTargets.entries.joinToString("\n") { (url, path) -> "  - $url -> $path" }
        )

        val stale = catalogEntries.keys.filter { url -> url !in externalImports }
        stale.isEmpty() || fail(
            "Catalog entries with no corresponding xs:import (stale mappings):\n" +
                stale.joinToString("\n") { "  - $it" } +
                "\n\nIf the schema that referenced this URL was removed, drop the catalog entry " +
                "and the vendored file."
        )
    }

    private fun readCatalog(): Map<String, String> {
        if (!catalogFile.exists()) fail("Catalog file missing: ${catalogFile.absolutePath}")
        val doc = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(catalogFile)
        val uriNs = "urn:oasis:names:tc:entity:xmlns:xml:catalog"
        val nodes = doc.getElementsByTagNameNS(uriNs, "uri")
        return (0 until nodes.length)
            .map { nodes.item(it) as Element }
            .associate { it.getAttribute("name") to it.getAttribute("uri") }
    }

    private fun collectExternalImports(): Set<String> {
        if (!schemasRoot.isDirectory) fail("Schemas root missing: ${schemasRoot.absolutePath}")
        val urls = mutableSetOf<String>()
        val builder = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
        val xsNs = "http://www.w3.org/2001/XMLSchema"
        schemasRoot.walkTopDown()
            .filter { it.isFile && it.extension == "xsd" }
            .forEach { xsd ->
                val doc = builder.parse(xsd)
                listOf("import", "include", "redefine").forEach { tag ->
                    val nodes = doc.getElementsByTagNameNS(xsNs, tag)
                    (0 until nodes.length).forEach { i ->
                        val el = nodes.item(i) as Element
                        val loc = el.getAttribute("schemaLocation")
                        if (loc.startsWith("http://") || loc.startsWith("https://")) {
                            urls += loc
                        }
                    }
                }
            }
        return urls
    }
}
