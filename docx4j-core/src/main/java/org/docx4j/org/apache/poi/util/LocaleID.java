/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */

/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.util;

import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Locale Collection
 * <p>
 * This enum can be used to map between Windows LCID and Java {@link java.util.Locale Locales}
 *
 * @see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-lcid/70feba9f-294e-491e-b6eb-56532684c37f">[MS-LCID]: Windows Language Code Identifier (LCID) Reference</a>
 */
@SuppressWarnings("unused")
public enum LocaleID {
    AR(0x0001, "ar", "ar", "Arabic", 1256, SUNDAY),
    BG(0x0002, "bg", "bg", "Bulgarian", 1251, -1),
    CA(0x0003, "ca", "ca", "Catalan", 1252, -1),
    ZH_HANS(0x0004, "zh_hans", "zh-Hans", "Chinese (Simplified)", 936, -1),
    CS(0x0005, "cs", "cs", "Czech", 1250, -1),
    DA(0x0006, "da", "da", "Danish", 1252, -1),
    DE(0x0007, "de", "de", "German", 1252, -1),
    EL(0x0008, "el", "el", "Greek", 1253, -1),
    EN(0x0009, "en", "en", "English", 1252, -1),
    ES(0x000A, "es", "es", "Spanish", 1252, -1),
    FI(0x000B, "fi", "fi", "Finnish", 1252, -1),
    FR(0x000C, "fr", "fr", "French", 1252, -1),
    HE(0x000D, "he", "he", "Hebrew", 1255, SUNDAY),
    HU(0x000E, "hu", "hu", "Hungarian", 1250, -1),
    IS(0x000F, "is", "is", "Icelandic", 1252, -1),
    IT(0x0010, "it", "it", "Italian", 1252, -1),
    JA(0x0011, "ja", "ja", "Japanese", 932, SUNDAY),
    KO(0x0012, "ko", "ko", "Korean", 949, SUNDAY),
    NL(0x0013, "nl", "nl", "Dutch", 1252, -1),
    NO(0x0014, "no", "no", "Norwegian", 1252, -1),
    PL(0x0015, "pl", "pl", "Polish", 1250, -1),
    PT(0x0016, "pt", "pt", "Portuguese", 1252, SUNDAY),
    RM(0x0017, "rm", "rm", "Romansh", 1252, -1),
    RO(0x0018, "ro", "ro", "Romanian", 1250, -1),
    RU(0x0019, "ru", "ru", "Russian", 1251, -1),
    HR(0x001A, "hr", "hr", "Croatian", 1250, -1),
    SK(0x001B, "sk", "sk", "Slovak", 1250, -1),
    SQ(0x001C, "sq", "sq", "Albanian", 1250, -1),
    SV(0x001D, "sv", "sv", "Swedish", 1252, -1),
    TH(0x001E, "th", "th", "Thai", 874, -1),
    TR(0x001F, "tr", "tr", "Turkish", 1254, -1),
    UR(0x0020, "ur", "ur", "Urdu", 1256, -1),
    ID(0x0021, "id", "id", "Indonesian", 1252, SUNDAY),
    UK(0x0022, "uk", "uk", "Ukrainian", 1251, -1),
    BE(0x0023, "be", "be", "Belarusian", 1251, -1),
    SL(0x0024, "sl", "sl", "Slovenian", 1250, -1),
    ET(0x0025, "et", "et", "Estonian", 1257, -1),
    LV(0x0026, "lv", "lv", "Latvian", 1257, -1),
    LT(0x0027, "lt", "lt", "Lithuanian", 1257, -1),
    TG(0x0028, "tg", "tg", "Tajik", 1251, -1),
    FA(0x0029, "fa", "fa", "Persian", 1256, SATURDAY),
    VI(0x002A, "vi", "vi", "Vietnamese", 1258, -1),
    HY(0x002B, "hy", "hy", "Armenian", 0, -1),
    AZ(0x002C, "az", "az", "Azerbaijani", 1254, -1),
    EU(0x002D, "eu", "eu", "Basque", 1252, -1),
    HSB(0x002E, "hsb", "hsb", "Upper Sorbian", 1252, -1),
    MK(0x002F, "mk", "mk", "Macedonian (FYROM)", 1251, -1),
    ST(0x0030, "st", "st", "Southern Sotho", 0, -1),
    TS(0x0031, "ts", "ts", "Tsonga", 0, SUNDAY),
    TN(0x0032, "tn", "tn", "Setswana", 1252, SUNDAY),
    VE(0x0033, "ve", "ve", "Venda", 32759, SUNDAY),
    XH(0x0034, "xh", "xh", "isiXhosa", 1252, SUNDAY),
    ZU(0x0035, "zu", "zu", "isiZulu", 1252, SUNDAY),
    AF(0x0036, "af", "af", "Afrikaans", 1252, SUNDAY),
    KA(0x0037, "ka", "ka", "Georgian", 0, -1),
    FO(0x0038, "fo", "fo", "Faroese", 1252, -1),
    HI(0x0039, "hi", "hi", "Hindi", 0, -1),
    MT(0x003A, "mt", "mt", "Maltese", 0, SUNDAY),
    SE(0x003B, "se", "se", "Sami (Northern)", 1252, -1),
    GA(0x003C, "ga", "ga", "Irish", 1252, SUNDAY),
    YI(0x003D, "yi", "yi", "Yiddish", 32759, -1),
    MS(0x003E, "ms", "ms", "Malay", 1252, -1),
    KK(0x003F, "kk", "kk", "Kazakh", 0, -1),
    KY(0x0040, "ky", "ky", "Kyrgyz", 1251, -1),
    SW(0x0041, "sw", "sw", "Kiswahili", 1252, SUNDAY),
    TK(0x0042, "tk", "tk", "Turkmen", 1250, -1),
    UZ(0x0043, "uz", "uz", "Uzbek", 1254, -1),
    TT(0x0044, "tt", "tt", "Tatar", 1251, -1),
    BN(0x0045, "bn", "bn", "Bangla", 0, SUNDAY),
    PA(0x0046, "pa", "pa", "Punjabi", 0, -1),
    GU(0x0047, "gu", "gu", "Gujarati", 0, -1),
    OR(0x0048, "or", "or", "Odia", 0, -1),
    TA(0x0049, "ta", "ta", "Tamil", 0, -1),
    TE(0x004A, "te", "te", "Telugu", 0, -1),
    KN(0x004B, "kn", "kn", "Kannada", 0, -1),
    ML(0x004C, "ml", "ml", "Malayalam", 0, SUNDAY),
    AS(0x004D, "as", "as", "Assamese", 0, -1),
    MR(0x004E, "mr", "mr", "Marathi", 0, -1),
    SA(0x004F, "sa", "sa", "Sanskrit", 0, SUNDAY),
    MN(0x0050, "mn", "mn", "Mongolian", 1251, -1),
    BO(0x0051, "bo", "bo", "Tibetan", 0, -1),
    CY(0x0052, "cy", "cy", "Welsh", 1252, -1),
    KM(0x0053, "km", "km", "Khmer", 0, SUNDAY),
    LO(0x0054, "lo", "lo", "Lao", 0, SUNDAY),
    MY(0x0055, "my", "my", "Burmese", 0, SUNDAY),
    GL(0x0056, "gl", "gl", "Galician", 1252, -1),
    KOK(0x0057, "kok", "kok", "Konkani", 0, -1),
    MNI(0x0058, "mni", "mni", "Manipuri", 32759, -1),
    SD(0x0059, "sd", "sd", "Sindhi", 1256, -1),
    SYR(0x005A, "syr", "syr", "Syriac", 0, SUNDAY),
    SI(0x005B, "si", "si", "Sinhala", 0, -1),
    CHR(0x005C, "chr", "chr", "Cherokee", 0, SUNDAY),
    IU(0x005D, "iu", "iu", "Inuktitut", 1252, SUNDAY),
    AM(0x005E, "am", "am", "Amharic", 0, SUNDAY),
    TZM(0x005F, "tzm", "tzm", "Tamazight", 1252, -1),
    KS(0x0060, "ks", "ks", "Kashmiri", 32759, -1),
    NE(0x0061, "ne", "ne", "Nepali", 0, SUNDAY),
    FY(0x0062, "fy", "fy", "Frisian", 1252, -1),
    PS(0x0063, "ps", "ps", "Pashto", 0, SATURDAY),
    FIL(0x0064, "fil", "fil", "Filipino", 1252, SUNDAY),
    DV(0x0065, "dv", "dv", "Divehi", 0, SUNDAY),
    BIN(0x0066, "bin", "bin", "Edo", 32759, SUNDAY),
    FF(0x0067, "ff", "ff", "Fulah", 1252, -1),
    HA(0x0068, "ha", "ha", "Hausa", 1252, -1),
    IBB(0x0069, "ibb", "ibb", "Ibibio", 32759, SUNDAY),
    YO(0x006A, "yo", "yo", "Yoruba", 1252, -1),
    QUZ(0x006B, "quz", "quz", "Quechua", 1252, SUNDAY),
    NSO(0x006C, "nso", "nso", "Sesotho sa Leboa", 1252, SUNDAY),
    BA(0x006D, "ba", "ba", "Bashkir", 1251, -1),
    LB(0x006E, "lb", "lb", "Luxembourgish", 1252, -1),
    KL(0x006F, "kl", "kl", "Greenlandic", 1252, -1),
    IG(0x0070, "ig", "ig", "Igbo", 1252, -1),
    KR(0x0071, "kr", "kr", "Kanuri", 32759, SUNDAY),
    OM(0x0072, "om", "om", "Oromo", 0, SUNDAY),
    TI(0x0073, "ti", "ti", "Tigrinya", 0, -1),
    GN(0x0074, "gn", "gn", "Guarani", 1252, SUNDAY),
    HAW(0x0075, "haw", "haw", "Hawaiian", 1252, SUNDAY),
    LA(0x0076, "la", "la", "Latin", 32759, SUNDAY),
    SO(0x0077, "so", "so", "Somali", 0, -1),
    II(0x0078, "ii", "ii", "Yi", 0, -1),
    PAP(0x0079, "pap", "pap", "Papiamento", 32759, -1),
    ARN(0x007A, "arn", "arn", "Mapudungun", 1252, SUNDAY),
    INVALID_A(0x007B, "invalid_a", "", "", 32759, -1),
    MOH(0x007C, "moh", "moh", "Mohawk", 1252, SUNDAY),
    INVALID_B(0x007D, "invalid_b", "", "", 32759, -1),
    BR(0x007E, "br", "br", "Breton", 1252, -1),
    INVALID_C(0x007F, "invalid_c", "", "", 1252, -1),
    UG(0x0080, "ug", "ug", "Uyghur", 1256, -1),
    MI(0x0081, "mi", "mi", "Maori", 0, -1),
    OC(0x0082, "oc", "oc", "Occitan", 1252, -1),
    CO(0x0083, "co", "co", "Corsican", 1252, -1),
    GSW(0x0084, "gsw", "gsw", "Alsatian", 1252, -1),
    SAH(0x0085, "sah", "sah", "Sakha", 1251, -1),
    QUT(0x0086, "qut", "qut", "Guatemala", 1252, -1),
    RW(0x0087, "rw", "rw", "Kinyarwanda", 1252, -1),
    WO(0x0088, "wo", "wo", "Wolof", 1252, -1),
    INVALID_D(0x0089, "invalid_d", "", "", 32759, -1),
    INVALID_E(0x008A, "invalid_e", "", "", 32759, -1),
    INVALID_F(0x008B, "invalid_f", "", "", 32759, -1),
    PRS(0x008C, "prs", "prs", "Dari", 1256, SATURDAY),
    INVALID_G(0x008D, "invalid_g", "", "", 32759, -1),
    INVALID_H(0x008E, "invalid_h", "", "", 32759, -1),
    INVALID_I(0x008F, "invalid_i", "", "", 32759, -1),
    INVALID_J(0x0090, "invalid_j", "", "", 32759, -1),
    GD(0x0091, "gd", "gd", "Scottish Gaelic", 1252, -1),
    KU(0x0092, "ku", "ku", "Central Kurdish", 1256, SUNDAY),
    QUC(0x0093, "quc", "quc", "K'iche'", 32759, -1),
    AR_SA(0x0401, "ar_sa", "ar-SA", "Arabic (Saudi Arabia)", 1256, SUNDAY),
    BG_BG(0x0402, "bg_bg", "bg-BG", "Bulgarian (Bulgaria)", 1251, -1),
    CA_ES(0x0403, "ca_es", "ca-ES", "Catalan (Catalan)", 1252, -1),
    ZH_TW(0x0404, "zh_tw", "zh-TW", "Chinese (Traditional, Taiwan)", 950, SUNDAY),
    CS_CZ(0x0405, "cs_cz", "cs-CZ", "Czech (Czech Republic)", 1250, -1),
    DA_DK(0x0406, "da_dk", "da-DK", "Danish (Denmark)", 1252, -1),
    DE_DE(0x0407, "de_de", "de-DE", "German (Germany)", 1252, -1),
    EL_GR(0x0408, "el_gr", "el-GR", "Greek (Greece)", 1253, -1),
    EN_US(0x0409, "en_us", "en-US", "English (United States)", 1252, SUNDAY),
    ES_ES_TRADNL(0x040A, "es_es_tradnl", "es-ES-tradnl", "Spanish (Spain,tradnl)", 1252, -1),
    FI_FI(0x040B, "fi_fi", "fi-FI", "Finnish (Finland)", 1252, -1),
    FR_FR(0x040C, "fr_fr", "fr-FR", "French (France)", 1252, -1),
    HE_IL(0x040D, "he_il", "he-IL", "Hebrew (Israel)", 1255, SUNDAY),
    HU_HU(0x040E, "hu_hu", "hu-HU", "Hungarian (Hungary)", 1250, -1),
    IS_IS(0x040F, "is_is", "is-IS", "Icelandic (Iceland)", 1252, -1),
    IT_IT(0x0410, "it_it", "it-IT", "Italian (Italy)", 1252, -1),
    JA_JP(0x0411, "ja_jp", "ja-JP", "Japanese (Japan)", 932, SUNDAY),
    KO_KR(0x0412, "ko_kr", "ko-KR", "Korean (Korea)", 949, SUNDAY),
    NL_NL(0x0413, "nl_nl", "nl-NL", "Dutch (Netherlands)", 1252, -1),
    NB_NO(0x0414, "nb_no", "nb-NO", "Norwegian, Bokm\u00E5l (Norway)", 1252, -1),
    PL_PL(0x0415, "pl_pl", "pl-PL", "Polish (Poland)", 1250, -1),
    PT_BR(0x0416, "pt_br", "pt-BR", "Portuguese (Brazil)", 1252, SUNDAY),
    RM_CH(0x0417, "rm_ch", "rm-CH", "Romansh (Switzerland)", 1252, -1),
    RO_RO(0x0418, "ro_ro", "ro-RO", "Romanian (Romania)", 1250, -1),
    RU_RU(0x0419, "ru_ru", "ru-RU", "Russian (Russia)", 1251, -1),
    HR_HR(0x041A, "hr_hr", "hr-HR", "Croatian (Croatia)", 1250, -1),
    SK_SK(0x041B, "sk_sk", "sk-SK", "Slovak (Slovakia)", 1250, -1),
    SQ_AL(0x041C, "sq_al", "sq-AL", "Albanian (Albania)", 1250, -1),
    SV_SE(0x041D, "sv_se", "sv-SE", "Swedish (Sweden)", 1252, -1),
    TH_TH(0x041E, "th_th", "th-TH", "Thai (Thailand)", 874, -1),
    TR_TR(0x041F, "tr_tr", "tr-TR", "Turkish (Turkey)", 1254, -1),
    UR_PK(0x0420, "ur_pk", "ur-PK", "Urdu (Islamic Republic of Pakistan)", 1256, -1),
    ID_ID(0x0421, "id_id", "id-ID", "Indonesian (Indonesia)", 1252, SUNDAY),
    UK_UA(0x0422, "uk_ua", "uk-UA", "Ukrainian (Ukraine)", 1251, -1),
    BE_BY(0x0423, "be_by", "be-BY", "Belarusian (Belarus)", 1251, -1),
    SL_SI(0x0424, "sl_si", "sl-SI", "Slovenian (Slovenia)", 1250, -1),
    ET_EE(0x0425, "et_ee", "et-EE", "Estonian (Estonia)", 1257, -1),
    LV_LV(0x0426, "lv_lv", "lv-LV", "Latvian (Latvia)", 1257, -1),
    LT_LT(0x0427, "lt_lt", "lt-LT", "Lithuanian (Lithuania)", 1257, -1),
    TG_CYRL_TJ(0x0428, "tg_cyrl_tj", "tg-Cyrl-TJ", "Tajik (Cyrillic, Tajikistan)", 1251, -1),
    FA_IR(0x0429, "fa_ir", "fa-IR", "Persian (Iran)", 1256, SATURDAY),
    VI_VN(0x042A, "vi_vn", "vi-VN", "Vietnamese (Vietnam)", 1258, -1),
    HY_AM(0x042B, "hy_am", "hy-AM", "Armenian (Armenia)", 0, -1),
    AZ_LATN_AZ(0x042C, "az_latn_az", "az-Latn-AZ", "Azerbaijani (Latin, Azerbaijan)", 1254, -1),
    EU_ES(0x042D, "eu_es", "eu-ES", "Basque (Basque)", 1252, -1),
    HSB_DE(0x042E, "hsb_de", "hsb-DE", "Upper Sorbian (Germany)", 1252, -1),
    MK_MK(0x042F, "mk_mk", "mk-MK", "Macedonian (Former Yugoslav Republic of Macedonia)", 1251, -1),
    ST_ZA(0x0430, "st_za", "st-ZA", "Southern Sotho (South Africa)", 0, -1),
    TS_ZA(0x0431, "ts_za", "ts-ZA", "Tsonga (South Africa)", 0, -1),
    TN_ZA(0x0432, "tn_za", "tn-ZA", "Setswana (South Africa)", 1252, SUNDAY),
    VE_ZA(0x0433, "ve_za", "ve-ZA", "Venda (South Africa)", 32759, SUNDAY),
    XH_ZA(0x0434, "xh_za", "xh-ZA", "isiXhosa (South Africa)", 1252, SUNDAY),
    ZU_ZA(0x0435, "zu_za", "zu-ZA", "isiZulu (South Africa)", 1252, SUNDAY),
    AF_ZA(0x0436, "af_za", "af-ZA", "Afrikaans (South Africa)", 1252, SUNDAY),
    KA_GE(0x0437, "ka_ge", "ka-GE", "Georgian (Georgia)", 0, -1),
    FO_FO(0x0438, "fo_fo", "fo-FO", "Faroese (Faroe Islands)", 1252, -1),
    HI_IN(0x0439, "hi_in", "hi-IN", "Hindi (India)", 0, -1),
    MT_MT(0x043A, "mt_mt", "mt-MT", "Maltese (Malta)", 0, SUNDAY),
    SE_NO(0x043B, "se_no", "se-NO", "Sami, Northern (Norway)", 1252, -1),
    YI_HEBR(0x043D, "yi_hebr", "yi-Hebr", "Yiddish (Hebrew)", 32759, -1),
    MS_MY(0x043E, "ms_my", "ms-MY", "Malay (Malaysia)", 1252, -1),
    KK_KZ(0x043F, "kk_kz", "kk-KZ", "Kazakh (Kazakhstan)", 0, -1),
    KY_KG(0x0440, "ky_kg", "ky-KG", "Kyrgyz (Kyrgyzstan)", 1251, -1),
    SW_KE(0x0441, "sw_ke", "sw-KE", "Kiswahili (Kenya)", 1252, SUNDAY),
    TK_TM(0x0442, "tk_tm", "tk-TM", "Turkmen (Turkmenistan)", 1250, -1),
    UZ_LATN_UZ(0x0443, "uz_latn_uz", "uz-Latn-UZ", "Uzbek (Latin, Uzbekistan)", 1254, -1),
    TT_RU(0x0444, "tt_ru", "tt-RU", "Tatar (Russia)", 1251, -1),
    BN_IN(0x0445, "bn_in", "bn-IN", "Bangla (India)", 0, -1),
    PA_IN(0x0446, "pa_in", "pa-IN", "Punjabi (India)", 0, -1),
    GU_IN(0x0447, "gu_in", "gu-IN", "Gujarati (India)", 0, -1),
    OR_IN(0x0448, "or_in", "or-IN", "Odia (India)", 0, -1),
    TA_IN(0x0449, "ta_in", "ta-IN", "Tamil (India)", 0, -1),
    TE_IN(0x044A, "te_in", "te-IN", "Telugu (India)", 0, -1),
    KN_IN(0x044B, "kn_in", "kn-IN", "Kannada (India)", 0, -1),
    ML_IN(0x044C, "ml_in", "ml-IN", "Malayalam (India)", 0, SUNDAY),
    AS_IN(0x044D, "as_in", "as-IN", "Assamese (India)", 0, -1),
    MR_IN(0x044E, "mr_in", "mr-IN", "Marathi (India)", 0, -1),
    SA_IN(0x044F, "sa_in", "sa-IN", "Sanskrit (India)", 0, SUNDAY),
    MN_MN(0x0450, "mn_mn", "mn-MN", "Mongolian (Cyrillic, Mongolia)", 1251, -1),
    BO_CN(0x0451, "bo_cn", "bo-CN", "Tibetan (PRC)", 0, -1),
    CY_GB(0x0452, "cy_gb", "cy-GB", "Welsh (United Kingdom)", 1252, -1),
    KM_KH(0x0453, "km_kh", "km-KH", "Khmer (Cambodia)", 0, SUNDAY),
    LO_LA(0x0454, "lo_la", "lo-LA", "Lao (Lao P.D.R.)", 0, SUNDAY),
    MY_MM(0x0455, "my_mm", "my-MM", "Burmese (Myanmar)", 0, SUNDAY),
    GL_ES(0x0456, "gl_es", "gl-ES", "Galician (Galician)", 1252, -1),
    KOK_IN(0x0457, "kok_in", "kok-IN", "Konkani (India)", 0, -1),
    MNI_IN(0x0458, "mni_in", "mni-IN", "Manipuri (India)", 32759, -1),
    SD_DEVA_IN(0x0459, "sd_deva_in", "sd-Deva-IN", "Sindhi (Devanagari, India)", 32759, SUNDAY),
    SYR_SY(0x045A, "syr_sy", "syr-SY", "Syriac (Syria)", 0, SUNDAY),
    SI_LK(0x045B, "si_lk", "si-LK", "Sinhala (Sri Lanka)", 0, -1),
    CHR_CHER_US(0x045C, "chr_cher_us", "chr-Cher-US", "Cherokee (Cherokee)", 0, SUNDAY),
    IU_CANS_CA(0x045D, "iu_cans_ca", "iu-Cans-CA", "Inuktitut (Syllabics, Canada)", 0, SUNDAY),
    AM_ET(0x045E, "am_et", "am-ET", "Amharic (Ethiopia)", 0, SUNDAY),
    TZM_ARAB_MA(0x045F, "tzm_arab_ma", "tzm-Arab-MA", "Central Atlas Tamazight (Arabic, Morocco)", 32759, SATURDAY),
    KS_ARAB(0x0460, "ks_arab", "ks-Arab", "Kashmiri (Perso-Arabic)", 32759, SUNDAY),
    NE_NP(0x0461, "ne_np", "ne-NP", "Nepali (Nepal)", 0, SUNDAY),
    FY_NL(0x0462, "fy_nl", "fy-NL", "Frisian (Netherlands)", 1252, -1),
    PS_AF(0x0463, "ps_af", "ps-AF", "Pashto (Afghanistan)", 0, SATURDAY),
    FIL_PH(0x0464, "fil_ph", "fil-PH", "Filipino (Philippines)", 1252, SUNDAY),
    DV_MV(0x0465, "dv_mv", "dv-MV", "Divehi (Maldives)", 0, SUNDAY),
    BIN_NG(0x0466, "bin_ng", "bin-NG", "Edo (Nigeria)", 32759, SUNDAY),
    FUV_NG(0x0467, "fuv_ng", "fuv-NG", "fuv (Nigeria)", 32759, -1),
    HA_LATN_NG(0x0468, "ha_latn_ng", "ha-Latn-NG", "Hausa (Latin, Nigeria)", 1252, -1),
    IBB_NG(0x0469, "ibb_ng", "ibb-NG", "Ibibio (Nigeria)", 32759, SUNDAY),
    YO_NG(0x046A, "yo_ng", "yo-NG", "Yoruba (Nigeria)", 1252, -1),
    QUZ_BO(0x046B, "quz_bo", "quz-BO", "Quechua (Bolivia)", 1252, SUNDAY),
    NSO_ZA(0x046C, "nso_za", "nso-ZA", "Sesotho sa Leboa (South Africa)", 1252, SUNDAY),
    BA_RU(0x046D, "ba_ru", "ba-RU", "Bashkir (Russia)", 1251, -1),
    LB_LU(0x046E, "lb_lu", "lb-LU", "Luxembourgish (Luxembourg)", 1252, -1),
    KL_GL(0x046F, "kl_gl", "kl-GL", "Greenlandic (Greenland)", 1252, -1),
    IG_NG(0x0470, "ig_ng", "ig-NG", "Igbo (Nigeria)", 1252, -1),
    KR_NG(0x0471, "kr_ng", "kr-NG", "Kanuri (Nigeria)", 32759, SUNDAY),
    OM_ET(0x0472, "om_et", "om-ET", "Oromo (Ethiopia)", 0, SUNDAY),
    TI_ET(0x0473, "ti_et", "ti-ET", "Tigrinya (Ethiopia)", 0, SUNDAY),
    GN_PY(0x0474, "gn_py", "gn-PY", "Guarani (Paraguay)", 1252, SUNDAY),
    HAW_US(0x0475, "haw_us", "haw-US", "Hawaiian (United States)", 1252, SUNDAY),
    LA_LATN(0x0476, "la_latn", "la-Latn", "Latin (Latin)", 32759, -1),
    SO_SO(0x0477, "so_so", "so-SO", "Somali (Somalia)", 0, -1),
    II_CN(0x0478, "ii_cn", "ii-CN", "Yi (PRC)", 0, -1),
    PAP_029(0x0479, "pap_029", "pap-029", "Papiamento (Caribbean)", 32759, -1),
    ARN_CL(0x047A, "arn_cl", "arn-CL", "Mapudungun (Chile)", 1252, SUNDAY),
    MOH_CA(0x047C, "moh_ca", "moh-CA", "Mohawk (Mohawk)", 1252, SUNDAY),
    BR_FR(0x047E, "br_fr", "br-FR", "Breton (France)", 1252, -1),
    UG_CN(0x0480, "ug_cn", "ug-CN", "Uyghur (PRC)", 1256, -1),
    MI_NZ(0x0481, "mi_nz", "mi-NZ", "Maori (New Zealand)", 0, -1),
    OC_FR(0x0482, "oc_fr", "oc-FR", "Occitan (France)", 1252, -1),
    CO_FR(0x0483, "co_fr", "co-FR", "Corsican (France)", 1252, -1),
    GSW_FR(0x0484, "gsw_fr", "gsw-FR", "Alsatian (France)", 1252, -1),
    SAH_RU(0x0485, "sah_ru", "sah-RU", "Sakha (Russia)", 1251, -1),
    QUT_GT(0x0486, "qut_gt", "qut-GT", "qut (Guatemala)", 1252, -1),
    RW_RW(0x0487, "rw_rw", "rw-RW", "Kinyarwanda (Rwanda)", 1252, -1),
    WO_SN(0x0488, "wo_sn", "wo-SN", "Wolof (Senegal)", 1252, -1),
    PRS_AF(0x048C, "prs_af", "prs-AF", "Dari (Afghanistan)", 1256, SATURDAY),
    PLT_MG(0x048D, "plt_mg", "plt-MG", "plt (Madagascar)", 32759, -1),
    ZH_YUE_HK(0x048E, "zh_yue_hk", "yue-HK", "yue (Hong Kong)", 32759, -1),
    TDD_TALE_CN(0x048F, "tdd_tale_cn", "tdd-Tale-CN", "tdd (Tai Le,China)", 32759, -1),
    KHB_TALU_CN(0x0490, "khb_talu_cn", "khb-Talu-CN", "khb (New Tai Lue,China)", 32759, -1),
    GD_GB(0x0491, "gd_gb", "gd-GB", "Scottish Gaelic (United Kingdom)", 1252, -1),
    KU_ARAB_IQ(0x0492, "ku_arab_iq", "ku-Arab-IQ", "Central Kurdish (Iraq)", 1256, SUNDAY),
    QUC_CO(0x0493, "quc_co", "quc-CO", "quc (Colombia)", 32759, -1),
    QPS_PLOC(0x0501, "qps_ploc", "qps-Ploc", "qps (Ploc)", 1250, -1),
    QPS_PLOCA(0x05FE, "qps_ploca", "qps-ploca", "qps (ploca)", 932, -1),
    AR_IQ(0x0801, "ar_iq", "ar-IQ", "Arabic (Iraq)", 1256, SATURDAY),
    CA_ES_VALENCIA(0x0803, "ca_es_valencia", "ca-ES-valencia", "Valencian (Spain)", 1252, -1),
    ZH_CN(0x0804, "zh_cn", "zh-CN", "Chinese (Simplified, PRC)", 936, -1),
    DE_CH(0x0807, "de_ch", "de-CH", "German (Switzerland)", 1252, -1),
    EN_GB(0x0809, "en_gb", "en-GB", "English (United Kingdom)", 1252, -1),
    ES_MX(0x080A, "es_mx", "es-MX", "Spanish (Mexico)", 1252, SUNDAY),
    FR_BE(0x080C, "fr_be", "fr-BE", "French (Belgium)", 1252, -1),
    IT_CH(0x0810, "it_ch", "it-CH", "Italian (Switzerland)", 1252, -1),
    JA_PLOC_JP(0x0811, "ja_ploc_jp", "ja-Ploc-JP", "Japanese (Ploc,Japan)", 32759, -1),
    NL_BE(0x0813, "nl_be", "nl-BE", "Dutch (Belgium)", 1252, -1),
    NN_NO(0x0814, "nn_no", "nn-NO", "Norwegian, Nynorsk (Norway)", 1252, -1),
    PT_PT(0x0816, "pt_pt", "pt-PT", "Portuguese (Portugal)", 1252, SUNDAY),
    RO_MD(0x0818, "ro_md", "ro-MD", "Romanian (Moldova)", 0, -1),
    RU_MD(0x0819, "ru_md", "ru-MD", "Russian (Moldova)", 32759, -1),
    SR_LATN_CS(0x081A, "sr_latn_cs", "sr-Latn-CS", "Serbian (Latin,Serbia and Montenegro)", 1250, -1),
    SV_FI(0x081D, "sv_fi", "sv-FI", "Swedish (Finland)", 1252, -1),
    UR_IN(0x0820, "ur_in", "ur-IN", "Urdu (India)", 0, -1),
    INVALID_K(0x0827, "invalid_k", "", "", 32759, -1),
    AZ_CYRL_AZ(0x082C, "az_cyrl_az", "az-Cyrl-AZ", "Azerbaijani (Cyrillic, Azerbaijan)", 1251, -1),
    DSB_DE(0x082E, "dsb_de", "dsb-DE", "Lower Sorbian (Germany)", 1252, -1),
    TN_BW(0x0832, "tn_bw", "tn-BW", "Setswana (Botswana)", 1252, SUNDAY),
    SE_SE(0x083B, "se_se", "se-SE", "Sami, Northern (Sweden)", 1252, -1),
    GA_IE(0x083C, "ga_ie", "ga-IE", "Irish (Ireland)", 1252, SUNDAY),
    MS_BN(0x083E, "ms_bn", "ms-BN", "Malay (Brunei Darussalam)", 1252, -1),
    UZ_CYRL_UZ(0x0843, "uz_cyrl_uz", "uz-Cyrl-UZ", "Uzbek (Cyrillic, Uzbekistan)", 1251, -1),
    BN_BD(0x0845, "bn_bd", "bn-BD", "Bangla (Bangladesh)", 0, SUNDAY),
    PA_ARAB_PK(0x0846, "pa_arab_pk", "pa-Arab-PK", "Punjabi (Islamic Republic of Pakistan)", 1256, -1),
    TA_LK(0x0849, "ta_lk", "ta-LK", "Tamil (Sri Lanka)", 0, -1),
    MN_MONG_CN(0x0850, "mn_mong_cn", "mn-Mong-CN", "Mongolian (Traditional Mongolian, PRC)", 0, -1),
    BO_BT(0x0851, "bo_bt", "bo-BT", "Tibetan (Bhutan)", 32759, -1),
    SD_ARAB_PK(0x0859, "sd_arab_pk", "sd-Arab-PK", "Sindhi (Islamic Republic of Pakistan)", 1256, -1),
    IU_LATN_CA(0x085D, "iu_latn_ca", "iu-Latn-CA", "Inuktitut (Latin, Canada)", 1252, SUNDAY),
    TZM_LATN_DZ(0x085F, "tzm_latn_dz", "tzm-Latn-DZ", "Tamazight (Latin, Algeria)", 1252, -1),
    KS_DEVA(0x0860, "ks_deva", "ks-Deva", "Kashmiri (Devanagari)", 32759, -1),
    NE_IN(0x0861, "ne_in", "ne-IN", "Nepali (India)", 0, SUNDAY),
    FF_LATN_SN(0x0867, "ff_latn_sn", "ff-Latn-SN", "Fulah (Latin, Senegal)", 1252, -1),
    QUZ_EC(0x086B, "quz_ec", "quz-EC", "Quechua (Ecuador)", 1252, SUNDAY),
    TI_ER(0x0873, "ti_er", "ti-ER", "Tigrinya (Eritrea)", 0, -1),
    QPS_PLOCM(0x09FF, "qps_plocm", "qps-plocm", "qps (plocm)", 1256, -1),
    AR_EG(0x0C01, "ar_eg", "ar-EG", "Arabic (Egypt)", 1256, SATURDAY),
    ZH_HK(0x0C04, "zh_hk", "zh-HK", "Chinese (Traditional, Hong Kong S.A.R.)", 950, SUNDAY),
    DE_AT(0x0C07, "de_at", "de-AT", "German (Austria)", 1252, -1),
    EN_AU(0x0C09, "en_au", "en-AU", "English (Australia)", 1252, -1),
    ES_ES(0x0C0A, "es_es", "es-ES", "Spanish (Spain)", 1252, -1),
    FR_CA(0x0C0C, "fr_ca", "fr-CA", "French (Canada)", 1252, SUNDAY),
    SR_CYRL_CS(0x0C1A, "sr_cyrl_cs", "sr-Cyrl-CS", "Serbian (Cyrillic,Serbia and Montenegro)", 1251, -1),
    SE_FI(0x0C3B, "se_fi", "se-FI", "Sami, Northern (Finland)", 1252, -1),
    MN_MONG_MN(0x0C50, "mn_mong_mn", "mn-Mong-MN", "Mongolian (Traditional Mongolian, Mongolia)", 0, -1),
    DZ_BT(0x0C51, "dz_bt", "dz-BT", "Dzongkha (Bhutan)", 0, SUNDAY),
    TMZ_MA(0x0C5F, "tmz_ma", "tmz-MA", "tmz (Morocco)", 32759, -1),
    QUZ_PE(0x0C6b, "quz_pe", "quz-PE", "Quechua (Peru)", 1252, -1),
    AR_LY(0x1001, "ar_ly", "ar-LY", "Arabic (Libya)", 1256, SATURDAY),
    ZH_SG(0x1004, "zh_sg", "zh-SG", "Chinese (Simplified, Singapore)", 936, SUNDAY),
    DE_LU(0x1007, "de_lu", "de-LU", "German (Luxembourg)", 1252, -1),
    EN_CA(0x1009, "en_ca", "en-CA", "English (Canada)", 1252, SUNDAY),
    ES_GT(0x100A, "es_gt", "es-GT", "Spanish (Guatemala)", 1252, SUNDAY),
    FR_CH(0x100C, "fr_ch", "fr-CH", "French (Switzerland)", 1252, -1),
    HR_BA(0x101A, "hr_ba", "hr-BA", "Croatian (Latin, Bosnia and Herzegovina)", 1250, -1),
    SMJ_NO(0x103B, "smj_no", "smj-NO", "Sami, Lule (Norway)", 1252, -1),
    TZM_TFNG_MA(0x105F, "tzm_tfng_ma", "tzm-Tfng-MA", "Central Atlas Tamazight (Tifinagh, Morocco)", 0, SATURDAY),
    AR_DZ(0x1401, "ar_dz", "ar-DZ", "Arabic (Algeria)", 1256, SATURDAY),
    ZH_MO(0x1404, "zh_mo", "zh-MO", "Chinese (Traditional, Macao S.A.R.)", 950, SUNDAY),
    DE_LI(0x1407, "de_li", "de-LI", "German (Liechtenstein)", 1252, -1),
    EN_NZ(0x1409, "en_nz", "en-NZ", "English (New Zealand)", 1252, SUNDAY),
    ES_CR(0x140A, "es_cr", "es-CR", "Spanish (Costa Rica)", 1252, -1),
    FR_LU(0x140C, "fr_lu", "fr-LU", "French (Luxembourg)", 1252, -1),
    BS_LATN_BA(0x141A, "bs_latn_ba", "bs-Latn-BA", "Bosnian (Latin, Bosnia and Herzegovina)", 1250, -1),
    SMJ_SE(0x143B, "smj_se", "smj-SE", "Sami, Lule (Sweden)", 1252, -1),
    AR_MA(0x1801, "ar_ma", "ar-MA", "Arabic (Morocco)", 1256, -1),
    EN_IE(0x1809, "en_ie", "en-IE", "English (Ireland)", 1252, SUNDAY),
    ES_PA(0x180A, "es_pa", "es-PA", "Spanish (Panama)", 1252, SUNDAY),
    FR_MC(0x180C, "fr_mc", "fr-MC", "French (Monaco)", 1252, -1),
    SR_LATN_BA(0x181A, "sr_latn_ba", "sr-Latn-BA", "Serbian (Latin, Bosnia and Herzegovina)", 1250, -1),
    SMA_NO(0x183B, "sma_no", "sma-NO", "Sami, Southern (Norway)", 1252, -1),
    AR_TN(0x1C01, "ar_tn", "ar-TN", "Arabic (Tunisia)", 1256, -1),
    EN_ZA(0x1C09, "en_za", "en-ZA", "English (South Africa)", 1252, SUNDAY),
    ES_DO(0x1C0A, "es_do", "es-DO", "Spanish (Dominican Republic)", 1252, SUNDAY),
    INVALID_L(0x1C0C, "invalid_l", "", "", 32759, -1),
    SR_CYRL_BA(0x1C1A, "sr_cyrl_ba", "sr-Cyrl-BA", "Serbian (Cyrillic, Bosnia and Herzegovina)", 1251, -1),
    SMA_SE(0x1C3B, "sma_se", "sma-SE", "Sami, Southern (Sweden)", 1252, -1),
    AR_OM(0x2001, "ar_om", "ar-OM", "Arabic (Oman)", 1256, SUNDAY),
    INVALID_M(0x2008, "invalid_m", "", "", 32759, -1),
    EN_JM(0x2009, "en_jm", "en-JM", "English (Jamaica)", 1252, SUNDAY),
    ES_VE(0x200A, "es_ve", "es-VE", "Spanish (Venezuela)", 1252, -1),
    FR_RE(0x200C, "fr_re", "fr-RE", "French (Reunion)", 0, -1),
    BS_CYRL_BA(0x201A, "bs_cyrl_ba", "bs-Cyrl-BA", "Bosnian (Cyrillic, Bosnia and Herzegovina)", 1251, -1),
    SMS_FI(0x203B, "sms_fi", "sms-FI", "Sami, Skolt (Finland)", 1252, -1),
    AR_YE(0x2401, "ar_ye", "ar-YE", "Arabic (Yemen)", 1256, SATURDAY),
    EN_029(0x2409, "en_029", "en-029", "English (Caribbean)", 1252, -1),
    ES_CO(0x240A, "es_co", "es-CO", "Spanish (Colombia)", 1252, SUNDAY),
    FR_CD(0x240C, "fr_cd", "fr-CD", "French (Congo DRC)", 0, -1),
    SR_LATN_RS(0x241A, "sr_latn_rs", "sr-Latn-RS", "Serbian (Latin, Serbia)", 1250, -1),
    SMN_FI(0x243B, "smn_fi", "smn-FI", "Sami, Inari (Finland)", 1252, -1),
    AR_SY(0x2801, "ar_sy", "ar-SY", "Arabic (Syria)", 1256, SATURDAY),
    EN_BZ(0x2809, "en_bz", "en-BZ", "English (Belize)", 1252, SUNDAY),
    ES_PE(0x280A, "es_pe", "es-PE", "Spanish (Peru)", 1252, SUNDAY),
    FR_SN(0x280C, "fr_sn", "fr-SN", "French (Senegal)", 0, -1),
    SR_CYRL_RS(0x281A, "sr_cyrl_rs", "sr-Cyrl-RS", "Serbian (Cyrillic, Serbia)", 1251, -1),
    AR_JO(0x2C01, "ar_jo", "ar-JO", "Arabic (Jordan)", 1256, SATURDAY),
    EN_TT(0x2C09, "en_tt", "en-TT", "English (Trinidad and Tobago)", 1252, SUNDAY),
    ES_AR(0x2C0A, "es_ar", "es-AR", "Spanish (Argentina)", 1252, SUNDAY),
    FR_CM(0x2C0C, "fr_cm", "fr-CM", "French (Cameroon)", 0, -1),
    SR_LATN_ME(0x2C1A, "sr_latn_me", "sr-Latn-ME", "Serbian (Latin, Montenegro)", 1250, -1),
    AR_LB(0x3001, "ar_lb", "ar-LB", "Arabic (Lebanon)", 1256, -1),
    EN_ZW(0x3009, "en_zw", "en-ZW", "English (Zimbabwe)", 1252, SUNDAY),
    ES_EC(0x300A, "es_ec", "es-EC", "Spanish (Ecuador)", 1252, -1),
    FR_CI(0x300C, "fr_ci", "fr-CI", "French (C\u00F4te d\u2019Ivoire)", 0, -1),
    SR_CYRL_ME(0x301A, "sr_cyrl_me", "sr-Cyrl-ME", "Serbian (Cyrillic, Montenegro)", 1251, -1),
    AR_KW(0x3401, "ar_kw", "ar-KW", "Arabic (Kuwait)", 1256, SATURDAY),
    EN_PH(0x3409, "en_ph", "en-PH", "English (Philippines)", 1252, SUNDAY),
    ES_CL(0x340A, "es_cl", "es-CL", "Spanish (Chile)", 1252, -1),
    FR_ML(0x340C, "fr_ml", "fr-ML", "French (Mali)", 0, -1),
    AR_AE(0x3801, "ar_ae", "ar-AE", "Arabic (U.A.E.)", 1256, SATURDAY),
    EN_ID(0x3809, "en_id", "en-ID", "English (Indonesia)", 32759, SUNDAY),
    ES_UY(0x380A, "es_uy", "es-UY", "Spanish (Uruguay)", 1252, -1),
    FR_MA(0x380C, "fr_ma", "fr-MA", "French (Morocco)", 0, SATURDAY),
    AR_BH(0x3c01, "ar_bh", "ar-BH", "Arabic (Bahrain)", 1256, SATURDAY),
    EN_HK(0x3c09, "en_hk", "en-HK", "English (Hong Kong SAR)", 0, SUNDAY),
    ES_PY(0x3c0A, "es_py", "es-PY", "Spanish (Paraguay)", 1252, SUNDAY),
    FR_HT(0x3c0C, "fr_ht", "fr-HT", "French (Haiti)", 0, -1),
    AR_QA(0x4001, "ar_qa", "ar-QA", "Arabic (Qatar)", 1256, SATURDAY),
    EN_IN(0x4009, "en_in", "en-IN", "English (India)", 1252, -1),
    ES_BO(0x400A, "es_bo", "es-BO", "Spanish (Bolivia)", 1252, -1),
    AR_PLOC_SA(0x4401, "ar_ploc_sa", "ar-Ploc-SA", "Arabic (Ploc,Saudi Arabia)", 32759, -1),
    EN_MY(0x4409, "en_my", "en-MY", "English (Malaysia)", 1252, SUNDAY),
    ES_SV(0x440A, "es_sv", "es-SV", "Spanish (El Salvador)", 1252, SUNDAY),
    AR_145(0x4801, "ar_145", "ar-145", "Arabic (Western Asia)", 32759, -1),
    EN_SG(0x4809, "en_sg", "en-SG", "English (Singapore)", 1252, SUNDAY),
    ES_HN(0x480A, "es_hn", "es-HN", "Spanish (Honduras)", 1252, SUNDAY),
    EN_AE(0x4C09, "en_ae", "en-AE", "English (United Arab Emirates)", 32759, -1),
    ES_NI(0x4C0A, "es_ni", "es-NI", "Spanish (Nicaragua)", 1252, SUNDAY),
    EN_BH(0x5009, "en_bh", "en-BH", "English (Bahrain)", 32759, -1),
    ES_PR(0x500A, "es_pr", "es-PR", "Spanish (Puerto Rico)", 1252, SUNDAY),
    EN_EG(0x5409, "en_eg", "en-EG", "English (Egypt)", 32759, -1),
    ES_US(0x540A, "es_us", "es-US", "Spanish (United States)", 1252, SUNDAY),
    EN_JO(0x5809, "en_jo", "en-JO", "English (Jordan)", 32759, -1),
    ES_419(0x580A, "es_419", "es-419", "Spanish (Latin America)", 0, -1),
    EN_KW(0x5C09, "en_kw", "en-KW", "English (Kuwait)", 32759, -1),
    ES_CU(0x5C0A, "es_cu", "es-CU", "Spanish (Cuba)", 0, -1),
    EN_TR(0x6009, "en_tr", "en-TR", "English (Turkey)", 32759, -1),
    EN_YE(0x6409, "en_ye", "en-YE", "English (Yemen)", 32759, -1),
    BS_CYRL(0x641A, "bs_cyrl", "bs-Cyrl", "Bosnian (Cyrillic)", 1251, -1),
    BS_LATN(0x681A, "bs_latn", "bs-Latn", "Bosnian (Latin)", 1250, -1),
    SR_CYRL(0x6C1A, "sr_cyrl", "sr-Cyrl", "Serbian (Cyrillic)", 1251, -1),
    SR_LATN(0x701A, "sr_latn", "sr-Latn", "Serbian (Latin)", 1250, -1),
    SMN(0x703B, "smn", "smn", "Sami (Inari)", 1252, -1),
    AZ_CYRL(0x742C, "az_cyrl", "az-Cyrl", "Azerbaijani (Cyrillic)", 1251, -1),
    SMS(0x743B, "sms", "sms", "Sami (Skolt)", 1252, -1),
    ZH(0x7804, "zh", "zh", "Chinese", 936, -1),
    NN(0x7814, "nn", "nn", "Norwegian (Nynorsk)", 1252, -1),
    BS(0x781A, "bs", "bs", "Bosnian", 1250, -1),
    AZ_LATN(0x782C, "az_latn", "az-Latn", "Azerbaijani (Latin)", 1254, -1),
    SMA(0x783B, "sma", "sma", "Sami (Southern)", 1252, -1),
    UZ_CYRL(0x7843, "uz_cyrl", "uz-Cyrl", "Uzbek (Cyrillic)", 1251, -1),
    MN_CYRL(0x7850, "mn_cyrl", "mn-Cyrl", "Mongolian (Cyrillic)", 1251, -1),
    IU_CANS(0x785D, "iu_cans", "iu-Cans", "Inuktitut (Syllabics)", 0, SUNDAY),
    TZM_TFNG(0x785F, "tzm_tfng", "tzm-Tfng", "Tamazight (Tifinagh)", 0, -1),
    ZH_HANT(0x7C04, "zh_hant", "zh-Hant", "Chinese (Traditional)", 950, SUNDAY),
    NB(0x7C14, "nb", "nb", "Norwegian (Bokm\u00E5l)", 1252, -1),
    SR(0x7C1A, "sr", "sr", "Serbian", 1250, -1),
    TG_CYRL(0x7C28, "tg_cyrl", "tg-Cyrl", "Tajik (Cyrillic)", 1251, -1),
    DSB(0x7C2E, "dsb", "dsb", "Lower Sorbian", 1252, -1),
    SMJ(0x7C3B, "smj", "smj", "Sami (Lule)", 1252, -1),
    UZ_LATN(0x7C43, "uz_latn", "uz-Latn", "Uzbek (Latin)", 1254, -1),
    PA_ARAB(0x7C46, "pa_arab", "pa-Arab", "Punjabi (Arabic)", 1256, -1),
    MN_MONG(0x7C50, "mn_mong", "mn-Mong", "Mongolian (Traditional Mongolian)", 0, -1),
    SD_ARAB(0x7C59, "sd_arab", "sd-Arab", "Sindhi (Arabic)", 1256, -1),
    CHR_CHER(0x7C5C, "chr_cher", "chr-Cher", "Cherokee (Cherokee)", 0, SUNDAY),
    IU_LATN(0x7C5D, "iu_latn", "iu-Latn", "Inuktitut (Latin)", 1252, SUNDAY),
    TZM_LATN(0x7C5F, "tzm_latn", "tzm-Latn", "Tamazight (Latin)", 1252, -1),
    FF_LATN(0x7C67, "ff_latn", "ff-Latn", "Fulah (Latin)", 1252, -1),
    HA_LATN(0x7C68, "ha_latn", "ha-Latn", "Hausa (Latin)", 1252, -1),
    KU_ARAB(0x7C92, "ku_arab", "ku-Arab", "Central Kurdish (Arabic)", 1256, -1),
    INVALID_N(0xF2EE, "invalid_n", "", "", 0, -1),
    INVALID_O(0xEEEE, "invalid_o", "", "", 0, -1),
    ;

    private final int lcid;
    private final String windowsId;
    private final String languageTag;
    private final String description;
    private final int defaultCodepage;
    private final int firstWeekday;

    private static final Map<String, LocaleID> languageTagLookup = Collections.unmodifiableMap(
        Stream.of(values()).filter(LocaleID::isValid)
            .collect(Collectors.toMap(LocaleID::getLanguageTag, Function.identity())));

    private static final Map<Integer, LocaleID> lcidLookup = Collections.unmodifiableMap(
        Stream.of(values()).collect(Collectors.toMap(LocaleID::getLcid, Function.identity())));

    LocaleID(int lcid, String windowsId, String languageTag, String description, int defaultCodepage, int firstWeekday) {
        this.lcid = lcid;
        this.windowsId = windowsId;
        this.languageTag = languageTag;
        this.description = description;
        this.defaultCodepage = defaultCodepage;
        this.firstWeekday = (firstWeekday == -1) ? Calendar.MONDAY : firstWeekday;
    }

    public int getLcid() {
        return lcid;
    }

    public String getWindowsId() {
        return windowsId;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    public String getDescription() {
        return description;
    }

    public int getDefaultCodepage() {
        return defaultCodepage;
    }

    public int getFirstWeekday() {
        return firstWeekday;
    }

    private boolean isValid() {
        return !languageTag.isEmpty();
    }

    /**
     * Lookup via the Java language tag / locale display name
     *
     * @param languageTag the locale display name
     * @return if found the LocaleId, otherwise {@code null}
     */
    public static LocaleID lookupByLanguageTag(String languageTag) {
        return languageTagLookup.get(languageTag);
    }

    /**
     * Lookup via the Windows LCID
     *
     * @param lcid the language code id (LCID)
     * @return if found the LocaleId, otherwise {@code null}
     */
    public static LocaleID lookupByLcid(int lcid) {
        return lcidLookup.get(lcid);
    }

}
