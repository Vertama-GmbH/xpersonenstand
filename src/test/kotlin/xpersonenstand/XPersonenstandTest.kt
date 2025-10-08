package xpersonenstand

import com.vertama.xpersonenstand.XPersonenstand
import com.vertama.xpersonenstand.XPersonenstandMarshaller
import de.domap.xpsw.xpsw2511.*
import de.osci.xinneres.geschlecht._1.CodeGeschlecht
import de.osci.xinneres.postanschrift._5.PostalischeInlandsanschrift
import de.osci.xinneres.postanschrift._5.PostalischeInlandsanschriftGebaeudeanschrift
import de.xoev.schemata.basisnachricht.unqualified.g2g._1_1.BehoerdeType
import de.xoev.schemata.basisnachricht.unqualified.g2g._1_1.CodeKommunikationKanalType
import de.xoev.schemata.basisnachricht.unqualified.g2g._1_1.CodeVerzeichnisdienstType
import de.xoev.schemata.basisnachricht.unqualified.g2g._1_1.KommunikationType
import de.xoev.schemata.code._1_0.Code
import org.junit.jupiter.api.assertThrows
import org.xml.sax.SAXException
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import javax.xml.datatype.DatatypeFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class XPersonenstandTest {

    @Test
    fun `validation of invalid Portal2StAGeburt081020 throws error`() {
        val invalidXml = this::class.java.getResource("/xpersonenstand/Portal2StAGeburt081020_invalid.xml")
            ?: error("XML file of basisnachricht not found in resources.")

        val unmarshalFromFile = XPersonenstandMarshaller.unmarshalFromInputStream(invalidXml.openStream(), Portal2StAGeburt081020::class.java)
        assertThrows<SAXException> {
            XPersonenstand.validateOrThrow(unmarshalFromFile)
        }
    }

    @Test
    fun `validation of invalid created Portal2StAGeburt081020 throws error`() {
        val xps = buildPortal2StAGeburt081020()
        xps.produkt = null

        assertThrows<SAXException> {
            XPersonenstand.validateOrThrow(xps)
        }
    }

    @Test
    fun `validation of valid Portal2StAGeburt081020 throws no error`() {
        val validXml = this::class.java.getResource("/xpersonenstand/Portal2StAGeburt081020.xml")
            ?: error("XML file of basisnachricht not found in resources.")

        val unmarshalFromFile = XPersonenstandMarshaller.unmarshalFromInputStream(validXml.openStream(), Portal2StAGeburt081020::class.java)
        XPersonenstand.validateOrThrow(unmarshalFromFile)
    }

    @Test
    fun `create valid Portal2StAGeburt081020 xml`() {
        val expectedXml = this::class.java.getResource("/xpersonenstand/Portal2StAGeburt081020.xml")?.readText()
            ?: error("Expected XML file of basisnachricht not found in resources.")

        val portal2StAGeburt081020 = buildPortal2StAGeburt081020()
        XPersonenstand.validateOrThrow(portal2StAGeburt081020)
        val outputStream = ByteArrayOutputStream()
        XPersonenstandMarshaller.marshalToOutputStream(portal2StAGeburt081020, outputStream)

        // line breaks and indents are irrelevant
        val expected = expectedXml.lines().map { it.trim() }
        val actual = outputStream.toString().lines().map { it.trim() }

        assertEquals(expected, actual)
    }

    @Test
    fun `create valid Portal2StASterbefall084020 xml`() {
        val expectedXml = this::class.java.getResource("/xpersonenstand/Portal2StASterbefall084020.xml")?.readText()
            ?: error("Expected XML file of basisnachricht not found in resources.")

        val portal2StASterbefall084020 = buildPortal2StASterbefall084020()
        XPersonenstand.validateOrThrow(portal2StASterbefall084020)
        val outputStream = ByteArrayOutputStream()
        XPersonenstandMarshaller.marshalToOutputStream(portal2StASterbefall084020, outputStream)

        // line breaks and indents are irrelevant
        val expected = expectedXml.lines().map { it.trim() }
        val actual = outputStream.toString().lines().map { it.trim() }

        assertEquals(expected, actual)
    }

    private fun buildPortal2StAGeburt081020(): Portal2StAGeburt081020 {
        return Portal2StAGeburt081020().apply {
            produkt = "DIGG"
            produkthersteller = "Vertama GmbH"
            produktversion = "0.1"
            standard = "XPersonenstand"
            test = "test"
            version = "25.11"

            nachrichtenkopfG2G = buildNachrichtenKopf(
                Code().apply {
                    code = "081020"
                    name = "portal2StA.Geburt.081020"
                    listURI = "urn:xoev-de:xpersonenstand:codelist:nachrichtentyp"
                    listVersionID = "25.11"
                }
            )

            anschriftLeser = PostalischeInlandsanschrift().apply {
                gebaeude = PostalischeInlandsanschriftGebaeudeanschrift().apply {
                    hausnummer = "1"
                    postleitzahl = "10117"
                    strasse = "LeserStrasse"
                    wohnort = "LeserWohnort"
                }
            }
            anschriftAutor = PostalischeInlandsanschrift().apply {
                gebaeude = PostalischeInlandsanschriftGebaeudeanschrift().apply {
                    hausnummer = "1"
                    postleitzahl = "10117"
                    strasse = "AutorStrasse"
                    wohnort = "AutorWohnort"
                }
            }

            nameEinrichtung = "NameEinrichtung"
            geburtsangaben = Anz2StATemplateGeburtsanzeige.Geburtsangaben().apply {
                ort = Ereignisort().apply {
                    strasse = "Straße der Geburtsangaben"
                    hausnummer = "1"
                    ort = "Geburtsort"
                }
                tag = LocalDate.of(2024,11,1)
                uhrzeit = "10:00"
            }
            kind = Anz2StAGeburtKind().apply {
                geschlecht = CodeGeschlecht().apply {
                    listURI = "urn:xoev-de:xinneres:codeliste:geschlecht"
                    listVersionID = "1"
                    code = "w"
                }
            }
            mutter = Anz2StATemplateGeburtsanzeige.Mutter().apply {
                standard = Anz2StATemplateGeburtsanzeige.Mutter.Standard().apply {
                    namen = PersonName().apply {
                        familienname = AllgemeinerNamePersonenstandswesen().apply {
                            name = "Lee"
                        }
                        vornamen = AllgemeinerNamePersonenstandswesen().apply {
                            name = "Sophia"
                        }
                    }
                    geburtsdatum = LocalDate.of(2024,11,1)
                    anschrift = AnschriftInland().apply {
                        hausnummer = "2"
                        postleitzahl = "10117"
                        strasse = "Straße der Mutter"
                        wohnort = "Wohnortdermutter"
                    }
                }
            }
//            anhang.addAll(buildAnhaenge())
        }
    }

    private fun buildPortal2StASterbefall084020(): Portal2StASterbefall084020 {
        return Portal2StASterbefall084020().apply {
            produkt = "DIGT"
            produkthersteller = "Vertama GmbH"
            produktversion = "0.1"
            standard = "XPersonenstand"
            test = "test"
            version = "25.11"

            nachrichtenkopfG2G = buildNachrichtenKopf(
                Code().apply {
                    code = "084020"
                    name = "portal2StA.Sterbefall.084020"
                    listURI = "urn:xoev-de:xpersonenstand:codelist:nachrichtentyp"
                    listVersionID = "25.11"
                }
            )

            anschriftLeser = PostalischeInlandsanschrift().apply {
                gebaeude = PostalischeInlandsanschriftGebaeudeanschrift().apply {
                    hausnummer = "1"
                    postleitzahl = "10117"
                    strasse = "LeserStrasse"
                    wohnort = "LeserWohnort"
                }
            }
            anschriftAutor = PostalischeInlandsanschrift().apply {
                gebaeude = PostalischeInlandsanschriftGebaeudeanschrift().apply {
                    hausnummer = "1"
                    postleitzahl = "10117"
                    strasse = "AutorStrasse"
                    wohnort = "AutorWohnort"
                }
            }

            ansprechpartner = "Ansprechpartner"
            sterbefall = Portal2StASterbefall().apply {
                todestag = Portal2StASterbefall.Todestag().apply {
                    todestag = Portal2StASterbefall.Todestag.InnerTodestag().apply {
                        todestag = LocalDate.of(2024,11,1)
                        todeszeit = UhrzeitPersonenstandswesenMitExakt().apply {
                            value = "05:21"
                            isExakt = true
                        }
                    }
                }
                sterbeort = Portal2StASterbefall.Sterbeort().apply {
                    strasse = "Sterbeort Strasse"
                    hausnummer = "2"
                    ort = "Sterbeort Ort"
                }
            }
            verstorbener = Portal2StASterbefallVerstorbenerEinrichtung().apply {
                vorname = "Vorname"
                familienname = "Familienname"
                geschlecht = CodeGeschlecht().apply {
                    listURI = "urn:xoev-de:xinneres:codeliste:geschlecht"
                    listVersionID = "1"
                    code = "w"
                }
            }
            anzeigender = Portal2StASterbefall084020.Anzeigender().apply {
                namen = Portal2StASterbefall084020.Anzeigender.Namen().apply {
                    nameOrganisation = "Organisationsname"
                }
                kontaktdaten.add(KommunikationType().apply{
                    kanal = CodeKommunikationKanalType().apply{
                        listURI = "urn:de:xoev:codeliste:erreichbarkeit"
                        listVersionID = "1"
                        code = "03"
                    }
                    kennung = "555-0100"
                })
            }
        }
    }

    private fun buildNachrichtenKopf(nachrichtenTyp: Code): NachrichtenkopfG2G {
        return NachrichtenkopfG2G().apply {
            identifikationNachricht = IdentifikationNachricht().apply {
                nachrichtenUUID = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFF0"
                nachrichtentyp = nachrichtenTyp
                erstellungszeitpunkt =
                    DatatypeFactory.newDefaultInstance()
                        .newXMLGregorianCalendar(2024, 11, 1, 10, 0, 0, 0, 2)
            }

            leser = BehoerdeType().apply {
                verzeichnisdienst = CodeVerzeichnisdienstType().apply {
                    listURI = "urn:xoev-de:kosit:codeliste:verzeichnisdienst"
                    listVersionID = "3"
                    code = "DVDV"
                }
                kennung = "psw:11007007"
                name = "Test Standesamt"
            }
            autor = BehoerdeType().apply {
                verzeichnisdienst = CodeVerzeichnisdienstType().apply {
                    listURI = "urn:xoev-de:kosit:codeliste:verzeichnisdienst"
                    listVersionID = "3"
                    code = "DVDV"
                }
                kennung = "sap:000000013"
                name = "Test Standesamtportal"
                erreichbarkeit.add(KommunikationType().apply {
                    kanal = CodeKommunikationKanalType().apply {
                        listURI = "urn:de:xoev:codeliste:erreichbarkeit"
                        listVersionID = "1"
                        code = "03"
                    }
                    kennung = "555-0100"
                })
            }
        }
    }
}