
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for country_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="country_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="inherit"/>
 *     &lt;enumeration value="AF"/>
 *     &lt;enumeration value="AL"/>
 *     &lt;enumeration value="DZ"/>
 *     &lt;enumeration value="AS"/>
 *     &lt;enumeration value="AD"/>
 *     &lt;enumeration value="AO"/>
 *     &lt;enumeration value="AI"/>
 *     &lt;enumeration value="AQ"/>
 *     &lt;enumeration value="AG"/>
 *     &lt;enumeration value="AR"/>
 *     &lt;enumeration value="AM"/>
 *     &lt;enumeration value="AW"/>
 *     &lt;enumeration value="AU"/>
 *     &lt;enumeration value="AT"/>
 *     &lt;enumeration value="AZ"/>
 *     &lt;enumeration value="BS"/>
 *     &lt;enumeration value="BH"/>
 *     &lt;enumeration value="BD"/>
 *     &lt;enumeration value="BB"/>
 *     &lt;enumeration value="BY"/>
 *     &lt;enumeration value="BE"/>
 *     &lt;enumeration value="BZ"/>
 *     &lt;enumeration value="BJ"/>
 *     &lt;enumeration value="BM"/>
 *     &lt;enumeration value="BT"/>
 *     &lt;enumeration value="BO"/>
 *     &lt;enumeration value="BA"/>
 *     &lt;enumeration value="BW"/>
 *     &lt;enumeration value="BV"/>
 *     &lt;enumeration value="BR"/>
 *     &lt;enumeration value="IO"/>
 *     &lt;enumeration value="BN"/>
 *     &lt;enumeration value="BG"/>
 *     &lt;enumeration value="BF"/>
 *     &lt;enumeration value="BI"/>
 *     &lt;enumeration value="KH"/>
 *     &lt;enumeration value="CM"/>
 *     &lt;enumeration value="CA"/>
 *     &lt;enumeration value="CV"/>
 *     &lt;enumeration value="KY"/>
 *     &lt;enumeration value="CF"/>
 *     &lt;enumeration value="TD"/>
 *     &lt;enumeration value="CL"/>
 *     &lt;enumeration value="CN"/>
 *     &lt;enumeration value="CX"/>
 *     &lt;enumeration value="CC"/>
 *     &lt;enumeration value="CO"/>
 *     &lt;enumeration value="KM"/>
 *     &lt;enumeration value="CG"/>
 *     &lt;enumeration value="CD"/>
 *     &lt;enumeration value="CK"/>
 *     &lt;enumeration value="CR"/>
 *     &lt;enumeration value="CI"/>
 *     &lt;enumeration value="HR"/>
 *     &lt;enumeration value="CU"/>
 *     &lt;enumeration value="CY"/>
 *     &lt;enumeration value="CZ"/>
 *     &lt;enumeration value="DK"/>
 *     &lt;enumeration value="DJ"/>
 *     &lt;enumeration value="DM"/>
 *     &lt;enumeration value="DO"/>
 *     &lt;enumeration value="TP"/>
 *     &lt;enumeration value="EC"/>
 *     &lt;enumeration value="EG"/>
 *     &lt;enumeration value="SV"/>
 *     &lt;enumeration value="GQ"/>
 *     &lt;enumeration value="ER"/>
 *     &lt;enumeration value="EE"/>
 *     &lt;enumeration value="ET"/>
 *     &lt;enumeration value="FK"/>
 *     &lt;enumeration value="FO"/>
 *     &lt;enumeration value="FJ"/>
 *     &lt;enumeration value="FI"/>
 *     &lt;enumeration value="FR"/>
 *     &lt;enumeration value="GF"/>
 *     &lt;enumeration value="PF"/>
 *     &lt;enumeration value="TF"/>
 *     &lt;enumeration value="GA"/>
 *     &lt;enumeration value="GM"/>
 *     &lt;enumeration value="GE"/>
 *     &lt;enumeration value="DE"/>
 *     &lt;enumeration value="GH"/>
 *     &lt;enumeration value="GI"/>
 *     &lt;enumeration value="GR"/>
 *     &lt;enumeration value="GL"/>
 *     &lt;enumeration value="GD"/>
 *     &lt;enumeration value="GP"/>
 *     &lt;enumeration value="GU"/>
 *     &lt;enumeration value="GT"/>
 *     &lt;enumeration value="GN"/>
 *     &lt;enumeration value="GW"/>
 *     &lt;enumeration value="GY"/>
 *     &lt;enumeration value="HT"/>
 *     &lt;enumeration value="HM"/>
 *     &lt;enumeration value="VA"/>
 *     &lt;enumeration value="HN"/>
 *     &lt;enumeration value="HK"/>
 *     &lt;enumeration value="HU"/>
 *     &lt;enumeration value="IS"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="ID"/>
 *     &lt;enumeration value="IR"/>
 *     &lt;enumeration value="IQ"/>
 *     &lt;enumeration value="IE"/>
 *     &lt;enumeration value="IL"/>
 *     &lt;enumeration value="IT"/>
 *     &lt;enumeration value="JM"/>
 *     &lt;enumeration value="JP"/>
 *     &lt;enumeration value="JO"/>
 *     &lt;enumeration value="KZ"/>
 *     &lt;enumeration value="KE"/>
 *     &lt;enumeration value="KI"/>
 *     &lt;enumeration value="KP"/>
 *     &lt;enumeration value="KR"/>
 *     &lt;enumeration value="KW"/>
 *     &lt;enumeration value="KG"/>
 *     &lt;enumeration value="LA"/>
 *     &lt;enumeration value="LV"/>
 *     &lt;enumeration value="LB"/>
 *     &lt;enumeration value="LS"/>
 *     &lt;enumeration value="LR"/>
 *     &lt;enumeration value="LY"/>
 *     &lt;enumeration value="LI"/>
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="LU"/>
 *     &lt;enumeration value="MO"/>
 *     &lt;enumeration value="MK"/>
 *     &lt;enumeration value="MG"/>
 *     &lt;enumeration value="MW"/>
 *     &lt;enumeration value="MY"/>
 *     &lt;enumeration value="MV"/>
 *     &lt;enumeration value="ML"/>
 *     &lt;enumeration value="MT"/>
 *     &lt;enumeration value="MH"/>
 *     &lt;enumeration value="MQ"/>
 *     &lt;enumeration value="MR"/>
 *     &lt;enumeration value="MU"/>
 *     &lt;enumeration value="YT"/>
 *     &lt;enumeration value="MX"/>
 *     &lt;enumeration value="FM"/>
 *     &lt;enumeration value="MD"/>
 *     &lt;enumeration value="MC"/>
 *     &lt;enumeration value="MN"/>
 *     &lt;enumeration value="MS"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="MZ"/>
 *     &lt;enumeration value="MM"/>
 *     &lt;enumeration value="NA"/>
 *     &lt;enumeration value="NR"/>
 *     &lt;enumeration value="NP"/>
 *     &lt;enumeration value="NL"/>
 *     &lt;enumeration value="AN"/>
 *     &lt;enumeration value="NC"/>
 *     &lt;enumeration value="NZ"/>
 *     &lt;enumeration value="NI"/>
 *     &lt;enumeration value="NE"/>
 *     &lt;enumeration value="NG"/>
 *     &lt;enumeration value="NU"/>
 *     &lt;enumeration value="NF"/>
 *     &lt;enumeration value="MP"/>
 *     &lt;enumeration value="NO"/>
 *     &lt;enumeration value="OM"/>
 *     &lt;enumeration value="PK"/>
 *     &lt;enumeration value="PW"/>
 *     &lt;enumeration value="PS"/>
 *     &lt;enumeration value="PA"/>
 *     &lt;enumeration value="PG"/>
 *     &lt;enumeration value="PY"/>
 *     &lt;enumeration value="PE"/>
 *     &lt;enumeration value="PH"/>
 *     &lt;enumeration value="PN"/>
 *     &lt;enumeration value="PL"/>
 *     &lt;enumeration value="PT"/>
 *     &lt;enumeration value="PR"/>
 *     &lt;enumeration value="QA"/>
 *     &lt;enumeration value="RE"/>
 *     &lt;enumeration value="RO"/>
 *     &lt;enumeration value="RU"/>
 *     &lt;enumeration value="RW"/>
 *     &lt;enumeration value="SH"/>
 *     &lt;enumeration value="KN"/>
 *     &lt;enumeration value="LC"/>
 *     &lt;enumeration value="PM"/>
 *     &lt;enumeration value="VC"/>
 *     &lt;enumeration value="WS"/>
 *     &lt;enumeration value="SM"/>
 *     &lt;enumeration value="ST"/>
 *     &lt;enumeration value="SA"/>
 *     &lt;enumeration value="SN"/>
 *     &lt;enumeration value="SC"/>
 *     &lt;enumeration value="SL"/>
 *     &lt;enumeration value="SG"/>
 *     &lt;enumeration value="SK"/>
 *     &lt;enumeration value="SI"/>
 *     &lt;enumeration value="SB"/>
 *     &lt;enumeration value="SO"/>
 *     &lt;enumeration value="ZA"/>
 *     &lt;enumeration value="GS"/>
 *     &lt;enumeration value="ES"/>
 *     &lt;enumeration value="LK"/>
 *     &lt;enumeration value="SD"/>
 *     &lt;enumeration value="SR"/>
 *     &lt;enumeration value="SJ"/>
 *     &lt;enumeration value="SZ"/>
 *     &lt;enumeration value="SE"/>
 *     &lt;enumeration value="CH"/>
 *     &lt;enumeration value="SY"/>
 *     &lt;enumeration value="TW"/>
 *     &lt;enumeration value="TJ"/>
 *     &lt;enumeration value="TZ"/>
 *     &lt;enumeration value="TH"/>
 *     &lt;enumeration value="TG"/>
 *     &lt;enumeration value="TK"/>
 *     &lt;enumeration value="TO"/>
 *     &lt;enumeration value="TT"/>
 *     &lt;enumeration value="TN"/>
 *     &lt;enumeration value="TR"/>
 *     &lt;enumeration value="TM"/>
 *     &lt;enumeration value="TC"/>
 *     &lt;enumeration value="TV"/>
 *     &lt;enumeration value="UG"/>
 *     &lt;enumeration value="UA"/>
 *     &lt;enumeration value="AE"/>
 *     &lt;enumeration value="GB"/>
 *     &lt;enumeration value="US"/>
 *     &lt;enumeration value="UM"/>
 *     &lt;enumeration value="UY"/>
 *     &lt;enumeration value="UZ"/>
 *     &lt;enumeration value="VU"/>
 *     &lt;enumeration value="VE"/>
 *     &lt;enumeration value="VN"/>
 *     &lt;enumeration value="VG"/>
 *     &lt;enumeration value="VI"/>
 *     &lt;enumeration value="WF"/>
 *     &lt;enumeration value="EH"/>
 *     &lt;enumeration value="YE"/>
 *     &lt;enumeration value="YU"/>
 *     &lt;enumeration value="ZM"/>
 *     &lt;enumeration value="ZW"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "country_Type")
@XmlEnum
public enum CountryType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("inherit")
    INHERIT("inherit"),

    /**
     *  country-name = "AFGHANISTAN"/ 
     * 
     */
    AF("AF"),

    /**
     *  country-name = "ALBANIA"/ 
     * 
     */
    AL("AL"),

    /**
     *  country-name = "ALGERIA"/ 
     * 
     */
    DZ("DZ"),

    /**
     *  country-name = "AMERICANSAMOA"/ 
     * 
     */
    AS("AS"),

    /**
     *  country-name = "ANDORRA"/ 
     * 
     */
    AD("AD"),

    /**
     *  country-name = "ANGOLA"/ 
     * 
     */
    AO("AO"),

    /**
     *  country-name = "ANGUILLA"/ 
     * 
     */
    AI("AI"),

    /**
     *  country-name = "ANTARCTICA"/ 
     * 
     */
    AQ("AQ"),

    /**
     *  country-name = "ANTIGUAANDBARBUDA"/ 
     * 
     */
    AG("AG"),

    /**
     *  country-name = "ARGENTINA"/ 
     * 
     */
    AR("AR"),

    /**
     *  country-name = "ARMENIA"/ 
     * 
     */
    AM("AM"),

    /**
     *  country-name = "ARUBA"/ 
     * 
     */
    AW("AW"),

    /**
     *  country-name = "AUSTRALIA"/ 
     * 
     */
    AU("AU"),

    /**
     *  country-name = "AUSTRIA"/ 
     * 
     */
    AT("AT"),

    /**
     *  country-name = "AZERBAIJAN"/ 
     * 
     */
    AZ("AZ"),

    /**
     *  country-name = "BAHAMAS"/ 
     * 
     */
    BS("BS"),

    /**
     *  country-name = "BAHRAIN"/ 
     * 
     */
    BH("BH"),

    /**
     *  country-name = "BANGLADESH"/ 
     * 
     */
    BD("BD"),

    /**
     *  country-name = "BARBADOS"/ 
     * 
     */
    BB("BB"),

    /**
     *  country-name = "BELARUS"/ 
     * 
     */
    BY("BY"),

    /**
     *  country-name = "BELGIUM"/ 
     * 
     */
    BE("BE"),

    /**
     *  country-name = "BELIZE"/ 
     * 
     */
    BZ("BZ"),

    /**
     *  country-name = "BENIN"/ 
     * 
     */
    BJ("BJ"),

    /**
     *  country-name = "BERMUDA"/ 
     * 
     */
    BM("BM"),

    /**
     *  country-name = "BHUTAN"/ 
     * 
     */
    BT("BT"),

    /**
     *  country-name = "BOLIVIA"/ 
     * 
     */
    BO("BO"),

    /**
     *  country-name = "BOSNIAANDHERZEGOVINA"/ 
     * 
     */
    BA("BA"),

    /**
     *  country-name = "BOTSWANA"/ 
     * 
     */
    BW("BW"),

    /**
     *  country-name = "BOUVETISLAND"/ 
     * 
     */
    BV("BV"),

    /**
     *  country-name = "BRAZIL"/ 
     * 
     */
    BR("BR"),

    /**
     *  country-name = "BRITISHINDIANOCEANTERRITORY"/ 
     * 
     */
    IO("IO"),

    /**
     *  country-name = "BRUNEIDARUSSALAM"/ 
     * 
     */
    BN("BN"),

    /**
     *  country-name = "BULGARIA"/ 
     * 
     */
    BG("BG"),

    /**
     *  country-name = "BURKINAFASO"/ 
     * 
     */
    BF("BF"),

    /**
     *  country-name = "BURUNDI"/ 
     * 
     */
    BI("BI"),

    /**
     *  country-name = "CAMBODIA"/ 
     * 
     */
    KH("KH"),

    /**
     *  country-name = "CAMEROON"/ 
     * 
     */
    CM("CM"),

    /**
     *  country-name = "CANADA"/ 
     * 
     */
    CA("CA"),

    /**
     *  country-name = "CAPEVERDE"/ 
     * 
     */
    CV("CV"),

    /**
     *  country-name = "CAYMANISLANDS"/ 
     * 
     */
    KY("KY"),

    /**
     *  country-name = "CENTRALAFRICANREPUBLIC"/ 
     * 
     */
    CF("CF"),

    /**
     *  country-name = "CHAD"/ 
     * 
     */
    TD("TD"),

    /**
     *  country-name = "CHILE"/ 
     * 
     */
    CL("CL"),

    /**
     *  country-name = "CHINA"/ 
     * 
     */
    CN("CN"),

    /**
     *  country-name = "CHRISTMASISLAND"/ 
     * 
     */
    CX("CX"),

    /**
     *  country-name = "COCOS(KEELING)ISLANDS"/ 
     * 
     */
    CC("CC"),

    /**
     *  country-name = "COLOMBIA"/ 
     * 
     */
    CO("CO"),

    /**
     *  country-name = "COMOROS"/ 
     * 
     */
    KM("KM"),

    /**
     *  country-name = "CONGO"/ 
     * 
     */
    CG("CG"),

    /**
     *  country-name = "CONGO,THEDEMOCRATICREPUBLICOFTHE"/ 
     * 
     */
    CD("CD"),

    /**
     *  country-name = "COOKISLANDS"/ 
     * 
     */
    CK("CK"),

    /**
     *  country-name = "COSTARICA"/ 
     * 
     */
    CR("CR"),

    /**
     *  country-name = "COTED'IVOIRE"/ 
     * 
     */
    CI("CI"),

    /**
     *  country-name = "CROATIA"/ 
     * 
     */
    HR("HR"),

    /**
     *  country-name = "CUBA"/ 
     * 
     */
    CU("CU"),

    /**
     *  country-name = "CYPRUS"/ 
     * 
     */
    CY("CY"),

    /**
     *  country-name = "CZECHREPUBLIC"/ 
     * 
     */
    CZ("CZ"),

    /**
     *  country-name = "DENMARK"/ 
     * 
     */
    DK("DK"),

    /**
     *  country-name = "DJIBOUTI"/ 
     * 
     */
    DJ("DJ"),

    /**
     *  country-name = "DOMINICA"/ 
     * 
     */
    DM("DM"),

    /**
     *  country-name = "DOMINICANREPUBLIC"/ 
     * 
     */
    DO("DO"),

    /**
     *  country-name = "EASTTIMOR"/ 
     * 
     */
    TP("TP"),

    /**
     *  country-name = "ECUADOR"/ 
     * 
     */
    EC("EC"),

    /**
     *  country-name = "EGYPT"/ 
     * 
     */
    EG("EG"),

    /**
     *  country-name = "ELSALVADOR"/ 
     * 
     */
    SV("SV"),

    /**
     *  country-name = "EQUATORIALGUINEA"/ 
     * 
     */
    GQ("GQ"),

    /**
     *  country-name = "ERITREA"/ 
     * 
     */
    ER("ER"),

    /**
     *  country-name = "ESTONIA"/ 
     * 
     */
    EE("EE"),

    /**
     *  country-name = "ETHIOPIA"/ 
     * 
     */
    ET("ET"),

    /**
     *  country-name = "FALKLANDISLANDS(MALVINAS)"/ 
     * 
     */
    FK("FK"),

    /**
     *  country-name = "FAROEISLANDS"/ 
     * 
     */
    FO("FO"),

    /**
     *  country-name = "FIJI"/ 
     * 
     */
    FJ("FJ"),

    /**
     *  country-name = "FINLAND"/ 
     * 
     */
    FI("FI"),

    /**
     *  country-name = "FRANCE"/ 
     * 
     */
    FR("FR"),

    /**
     *  country-name = "FRENCHGUIANA"/ 
     * 
     */
    GF("GF"),

    /**
     *  country-name = "FRENCHPOLYNESIA"/ 
     * 
     */
    PF("PF"),

    /**
     *  country-name = "FRENCHSOUTHERNTERRITORIES"/ 
     * 
     */
    TF("TF"),

    /**
     *  country-name = "GABON"/ 
     * 
     */
    GA("GA"),

    /**
     *  country-name = "GAMBIA"/ 
     * 
     */
    GM("GM"),

    /**
     *  country-name = "GEORGIA"/ 
     * 
     */
    GE("GE"),

    /**
     *  country-name = "GERMANY"/ 
     * 
     */
    DE("DE"),

    /**
     *  country-name = "GHANA"/ 
     * 
     */
    GH("GH"),

    /**
     *  country-name = "GIBRALTAR"/ 
     * 
     */
    GI("GI"),

    /**
     *  country-name = "GREECE"/ 
     * 
     */
    GR("GR"),

    /**
     *  country-name = "GREENLAND"/ 
     * 
     */
    GL("GL"),

    /**
     *  country-name = "GRENADA"/ 
     * 
     */
    GD("GD"),

    /**
     *  country-name = "GUADELOUPE"/ 
     * 
     */
    GP("GP"),

    /**
     *  country-name = "GUAM"/ 
     * 
     */
    GU("GU"),

    /**
     *  country-name = "GUATEMALA"/ 
     * 
     */
    GT("GT"),

    /**
     *  country-name = "GUINEA"/ 
     * 
     */
    GN("GN"),

    /**
     *  country-name = "GUINEA-BISSAU"/ 
     * 
     */
    GW("GW"),

    /**
     *  country-name = "GUYANA"/ 
     * 
     */
    GY("GY"),

    /**
     *  country-name = "HAITI"/ 
     * 
     */
    HT("HT"),

    /**
     *  country-name = "HEARDISLANDANDMCDONALDISLANDS"/ 
     * 
     */
    HM("HM"),

    /**
     *  country-name = "HOLYSEE(VATICANCITYSTATE)"/ 
     * 
     */
    VA("VA"),

    /**
     *  country-name = "HONDURAS"/ 
     * 
     */
    HN("HN"),

    /**
     *  country-name = "HONGKONG"/ 
     * 
     */
    HK("HK"),

    /**
     *  country-name = "HUNGARY"/ 
     * 
     */
    HU("HU"),

    /**
     *  country-name = "ICELAND"/ 
     * 
     */
    IS("IS"),

    /**
     *  country-name = "INDIA"/ 
     * 
     */
    IN("IN"),

    /**
     *  country-name = "INDONESIA"/ 
     * 
     */
    ID("ID"),

    /**
     *  country-name = "IRAN,ISLAMICREPUBLICOF"/ 
     * 
     */
    IR("IR"),

    /**
     *  country-name = "IRAQ"/ 
     * 
     */
    IQ("IQ"),

    /**
     *  country-name = "IRELAND"/ 
     * 
     */
    IE("IE"),

    /**
     *  country-name = "ISRAEL"/ 
     * 
     */
    IL("IL"),

    /**
     *  country-name = "ITALY"/ 
     * 
     */
    IT("IT"),

    /**
     *  country-name = "JAMAICA"/ 
     * 
     */
    JM("JM"),

    /**
     *  country-name = "JAPAN"/ 
     * 
     */
    JP("JP"),

    /**
     *  country-name = "JORDAN"/ 
     * 
     */
    JO("JO"),

    /**
     *  country-name = "KAZAKSTAN"/ 
     * 
     */
    KZ("KZ"),

    /**
     *  country-name = "KENYA"/ 
     * 
     */
    KE("KE"),

    /**
     *  country-name = "KIRIBATI"/ 
     * 
     */
    KI("KI"),

    /**
     *  country-name = "KOREA,DEMOCRATICPEOPLE'SREPUBLICOF"/ 
     * 
     */
    KP("KP"),

    /**
     *  country-name = "KOREA,REPUBLICOF"/ 
     * 
     */
    KR("KR"),

    /**
     *  country-name = "KUWAIT"/ 
     * 
     */
    KW("KW"),

    /**
     *  country-name = "KYRGYZSTAN"/ 
     * 
     */
    KG("KG"),

    /**
     *  country-name = "LAOPEOPLE'SDEMOCRATICREPUBLIC"/ 
     * 
     */
    LA("LA"),

    /**
     *  country-name = "LATVIA"/ 
     * 
     */
    LV("LV"),

    /**
     *  country-name = "LEBANON"/ 
     * 
     */
    LB("LB"),

    /**
     *  country-name = "LESOTHO"/ 
     * 
     */
    LS("LS"),

    /**
     *  country-name = "LIBERIA"/ 
     * 
     */
    LR("LR"),

    /**
     *  country-name = "LIBYANARABJAMAHIRIYA"/ 
     * 
     */
    LY("LY"),

    /**
     *  country-name = "LIECHTENSTEIN"/ 
     * 
     */
    LI("LI"),

    /**
     *  country-name = "LITHUANIA"/ 
     * 
     */
    LT("LT"),

    /**
     *  country-name = "LUXEMBOURG"/ 
     * 
     */
    LU("LU"),

    /**
     *  country-name = "MACAU"/ 
     * 
     */
    MO("MO"),

    /**
     *  country-name = "MACEDONIA,THEFORMERYUGOSLAVREPUBLICOF"/ 
     * 
     */
    MK("MK"),

    /**
     *  country-name = "MADAGASCAR"/ 
     * 
     */
    MG("MG"),

    /**
     *  country-name = "MALAWI"/ 
     * 
     */
    MW("MW"),

    /**
     *  country-name = "MALAYSIA"/ 
     * 
     */
    MY("MY"),

    /**
     *  country-name = "MALDIVES"/ 
     * 
     */
    MV("MV"),

    /**
     *  country-name = "MALI"/ 
     * 
     */
    ML("ML"),

    /**
     *  country-name = "MALTA"/ 
     * 
     */
    MT("MT"),

    /**
     *  country-name = "MARSHALLISLANDS"/ 
     * 
     */
    MH("MH"),

    /**
     *  country-name = "MARTINIQUE"/ 
     * 
     */
    MQ("MQ"),

    /**
     *  country-name = "MAURITANIA"/ 
     * 
     */
    MR("MR"),

    /**
     *  country-name = "MAURITIUS"/ 
     * 
     */
    MU("MU"),

    /**
     *  country-name = "MAYOTTE"/ 
     * 
     */
    YT("YT"),

    /**
     *  country-name = "MEXICO"/ 
     * 
     */
    MX("MX"),

    /**
     *  country-name = "MICRONESIA,FEDERATEDSTATESOF"/ 
     * 
     */
    FM("FM"),

    /**
     *  country-name = "MOLDOVA,REPUBLICOF"/ 
     * 
     */
    MD("MD"),

    /**
     *  country-name = "MONACO"/ 
     * 
     */
    MC("MC"),

    /**
     *  country-name = "MONGOLIA"/ 
     * 
     */
    MN("MN"),

    /**
     *  country-name = "MONTSERRAT"/ 
     * 
     */
    MS("MS"),

    /**
     *  country-name = "MOROCCO"/ 
     * 
     */
    MA("MA"),

    /**
     *  country-name = "MOZAMBIQUE"/ 
     * 
     */
    MZ("MZ"),

    /**
     *  country-name = "MYANMAR"/ 
     * 
     */
    MM("MM"),

    /**
     *  country-name = "NAMIBIA"/ 
     * 
     */
    NA("NA"),

    /**
     *  country-name = "NAURU"/ 
     * 
     */
    NR("NR"),

    /**
     *  country-name = "NEPAL"/ 
     * 
     */
    NP("NP"),

    /**
     *  country-name = "NETHERLANDS"/ 
     * 
     */
    NL("NL"),

    /**
     *  country-name = "NETHERLANDSANTILLES"/ 
     * 
     */
    AN("AN"),

    /**
     *  country-name = "NEWCALEDONIA"/ 
     * 
     */
    NC("NC"),

    /**
     *  country-name = "NEWZEALAND"/ 
     * 
     */
    NZ("NZ"),

    /**
     *  country-name = "NICARAGUA"/ 
     * 
     */
    NI("NI"),

    /**
     *  country-name = "NIGER"/ 
     * 
     */
    NE("NE"),

    /**
     *  country-name = "NIGERIA"/ 
     * 
     */
    NG("NG"),

    /**
     *  country-name = "NIUE"/ 
     * 
     */
    NU("NU"),

    /**
     *  country-name = "NORFOLKISLAND"/ 
     * 
     */
    NF("NF"),

    /**
     *  country-name = "NORTHERNMARIANAISLANDS"/ 
     * 
     */
    MP("MP"),

    /**
     *  country-name = "NORWAY"/ 
     * 
     */
    NO("NO"),

    /**
     *  country-name = "OMAN"/ 
     * 
     */
    OM("OM"),

    /**
     *  country-name = "PAKISTAN"/ 
     * 
     */
    PK("PK"),

    /**
     *  country-name = "PALAU"/ 
     * 
     */
    PW("PW"),

    /**
     *  country-name = "PALESTINIANTERRITORY,OCCUPIED"/ 
     * 
     */
    PS("PS"),

    /**
     *  country-name = "PANAMA"/ 
     * 
     */
    PA("PA"),

    /**
     *  country-name = "PAPUANEWGUINEA"/ 
     * 
     */
    PG("PG"),

    /**
     *  country-name = "PARAGUAY"/ 
     * 
     */
    PY("PY"),

    /**
     *  country-name = "PERU"/ 
     * 
     */
    PE("PE"),

    /**
     *  country-name = "PHILIPPINES"/ 
     * 
     */
    PH("PH"),

    /**
     *  country-name = "PITCAIRN"/ 
     * 
     */
    PN("PN"),

    /**
     *  country-name = "POLAND"/ 
     * 
     */
    PL("PL"),

    /**
     *  country-name = "PORTUGAL"/ 
     * 
     */
    PT("PT"),

    /**
     *  country-name = "PUERTORICO"/ 
     * 
     */
    PR("PR"),

    /**
     *  country-name = "QATAR"/ 
     * 
     */
    QA("QA"),

    /**
     *  country-name = "R+UNION"/ 
     * 
     */
    RE("RE"),

    /**
     *  country-name = "ROMANIA"/ 
     * 
     */
    RO("RO"),

    /**
     *  country-name = "RUSSIANFEDERATION"/ 
     * 
     */
    RU("RU"),

    /**
     *  country-name = "RWANDA"/ 
     * 
     */
    RW("RW"),

    /**
     *  country-name = "SAINTHELENA"/ 
     * 
     */
    SH("SH"),

    /**
     *  country-name = "SAINTKITTSANDNEVIS"/ 
     * 
     */
    KN("KN"),

    /**
     *  country-name = "SAINTLUCIA"/ 
     * 
     */
    LC("LC"),

    /**
     *  country-name = "SAINTPIERREANDMIQUELON"/ 
     * 
     */
    PM("PM"),

    /**
     *  country-name = "SAINTVINCENTANDTHEGRENADINES"/ 
     * 
     */
    VC("VC"),

    /**
     *  country-name = "SAMOA"/ 
     * 
     */
    WS("WS"),

    /**
     *  country-name = "SANMARINO"/ 
     * 
     */
    SM("SM"),

    /**
     *  country-name = "SAOTOMEANDPRINCIPE"/ 
     * 
     */
    ST("ST"),

    /**
     *  country-name = "SAUDIARABIA"/ 
     * 
     */
    SA("SA"),

    /**
     *  country-name = "SENEGAL"/ 
     * 
     */
    SN("SN"),

    /**
     *  country-name = "SEYCHELLES"/ 
     * 
     */
    SC("SC"),

    /**
     *  country-name = "SIERRALEONE"/ 
     * 
     */
    SL("SL"),

    /**
     *  country-name = "SINGAPORE"/ 
     * 
     */
    SG("SG"),

    /**
     *  country-name = "SLOVAKIA"/ 
     * 
     */
    SK("SK"),

    /**
     *  country-name = "SLOVENIA"/ 
     * 
     */
    SI("SI"),

    /**
     *  country-name = "SOLOMONISLANDS"/ 
     * 
     */
    SB("SB"),

    /**
     *  country-name = "SOMALIA"/ 
     * 
     */
    SO("SO"),

    /**
     *  country-name = "SOUTHAFRICA"/ 
     * 
     */
    ZA("ZA"),

    /**
     *  country-name = "SOUTHGEORGIAANDTHESOUTHSANDWICHISLANDS"/ 
     * 
     */
    GS("GS"),

    /**
     *  country-name = "SPAIN"/ 
     * 
     */
    ES("ES"),

    /**
     *  country-name = "SRILANKA"/ 
     * 
     */
    LK("LK"),

    /**
     *  country-name = "SUDAN"/ 
     * 
     */
    SD("SD"),

    /**
     *  country-name = "SURINAME"/ 
     * 
     */
    SR("SR"),

    /**
     *  country-name = "SVALBARDANDJANMAYEN"/ 
     * 
     */
    SJ("SJ"),

    /**
     *  country-name = "SWAZILAND"/ 
     * 
     */
    SZ("SZ"),

    /**
     *  country-name = "SWEDEN"/ 
     * 
     */
    SE("SE"),

    /**
     *  country-name = "SWITZERLAND"/ 
     * 
     */
    CH("CH"),

    /**
     *  country-name = "SYRIANARABREPUBLIC"/ 
     * 
     */
    SY("SY"),

    /**
     *  country-name = "TAIWAN,PROVINCEOFCHINA"/ 
     * 
     */
    TW("TW"),

    /**
     *  country-name = "TAJIKISTAN"/ 
     * 
     */
    TJ("TJ"),

    /**
     *  country-name = "TANZANIA,UNITEDREPUBLICOF"/ 
     * 
     */
    TZ("TZ"),

    /**
     *  country-name = "THAILAND"/ 
     * 
     */
    TH("TH"),

    /**
     *  country-name = "TOGO"/ 
     * 
     */
    TG("TG"),

    /**
     *  country-name = "TOKELAU"/ 
     * 
     */
    TK("TK"),

    /**
     *  country-name = "TONGA"/ 
     * 
     */
    TO("TO"),

    /**
     *  country-name = "TRINIDADANDTOBAGO"/ 
     * 
     */
    TT("TT"),

    /**
     *  country-name = "TUNISIA"/ 
     * 
     */
    TN("TN"),

    /**
     *  country-name = "TURKEY"/ 
     * 
     */
    TR("TR"),

    /**
     *  country-name = "TURKMENISTAN"/ 
     * 
     */
    TM("TM"),

    /**
     *  country-name = "TURKSANDCAICOSISLANDS"/ 
     * 
     */
    TC("TC"),

    /**
     *  country-name = "TUVALU"/ 
     * 
     */
    TV("TV"),

    /**
     *  country-name = "UGANDA"/ 
     * 
     */
    UG("UG"),

    /**
     *  country-name = "UKRAINE"/ 
     * 
     */
    UA("UA"),

    /**
     *  country-name = "UNITEDARABEMIRATES"/ 
     * 
     */
    AE("AE"),

    /**
     *  country-name = "UNITEDKINGDOM"/ 
     * 
     */
    GB("GB"),

    /**
     *  country-name = "UNITEDSTATES"/ 
     * 
     */
    US("US"),

    /**
     *  country-name = "UNITEDSTATESMINOROUTLYINGISLANDS"/ 
     * 
     */
    UM("UM"),

    /**
     *  country-name = "URUGUAY"/ 
     * 
     */
    UY("UY"),

    /**
     *  country-name = "UZBEKISTAN"/ 
     * 
     */
    UZ("UZ"),

    /**
     *  country-name = "VANUATU"/ 
     * 
     */
    VU("VU"),

    /**
     *  country-name = "VENEZUELA"/ 
     * 
     */
    VE("VE"),

    /**
     *  country-name = "VIETNAM"/ 
     * 
     */
    VN("VN"),

    /**
     *  country-name = "VIRGINISLANDS,BRITISH"/ 
     * 
     */
    VG("VG"),

    /**
     *  country-name = "VIRGINISLANDS,U.S."/ 
     * 
     */
    VI("VI"),

    /**
     *  country-name = "WALLISANDFUTUNA"/ 
     * 
     */
    WF("WF"),

    /**
     *  country-name = "WESTERNSAHARA"/ 
     * 
     */
    EH("EH"),

    /**
     *  country-name = "YEMEN"/ 
     * 
     */
    YE("YE"),

    /**
     *  country-name = "YUGOSLAVIA"/ 
     * 
     */
    YU("YU"),

    /**
     *  country-name = "ZAMBIA"/ 
     * 
     */
    ZM("ZM"),

    /**
     *  country-name = "ZIMBABWE"/ 
     * 
     */
    ZW("ZW");
    private final String value;

    CountryType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CountryType fromValue(String v) {
        for (CountryType c: CountryType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
