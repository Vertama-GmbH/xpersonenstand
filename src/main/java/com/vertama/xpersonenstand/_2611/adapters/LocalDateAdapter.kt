package com.vertama.xpersonenstand._2611.adapters

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * XmlAdapter to convert between XML date strings (ISO_LOCAL_DATE format)
 * and java.time.LocalDate objects.
 *
 * This adapter is used by JAXB to map xsd:date types to LocalDate.
 */
class LocalDateAdapter : XmlAdapter<String, LocalDate>() {

    // Companion object to hold the DateTimeFormatter, similar to a static field in Java.
    companion object {
        // Standard ISO_LOCAL_DATE formatter, e.g., "2011-12-03"
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }

    /**
     * Converts an XML date string (e.g., "2023-05-15") into a LocalDate object.
     *
     * @param xmlDate The date string from the XML.
     * @return The corresponding LocalDate object, or null if the input string is null.
     * @throws Exception If parsing fails.
     */
    @Throws(Exception::class)
    override fun unmarshal(xmlDate: String?): LocalDate? {
        // If the input string is null, return null.
        // Otherwise, parse the string using the predefined formatter.
        return xmlDate?.let { LocalDate.parse(it, DATE_FORMATTER) }
    }

    /**
     * Converts a LocalDate object into an XML date string (e.g., "2023-05-15").
     *
     * @param javaDate The LocalDate object.
     * @return The date string formatted according to ISO_LOCAL_DATE, or null if the input LocalDate is null.
     * @throws Exception If formatting fails.
     */
    @Throws(Exception::class)
    override fun marshal(javaDate: LocalDate?): String? {
        // If the input LocalDate is null, return null.
        // Otherwise, format the LocalDate using the predefined formatter.
        return javaDate?.format(DATE_FORMATTER)
    }
}
