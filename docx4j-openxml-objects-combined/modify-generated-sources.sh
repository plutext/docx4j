#!/usr/bin/env bash
# Copyright © 2026, Oracle and/or its affiliates.

# TODO: This bash script means docx4j will not build on Windows. Do we need to provide an equivalent batch file or
# PowerShell script for Windows? Or assume everyone can use WSL?

# This script should be idempotent so that running "mvn install" twice does not fail.

set -o errexit  # abort on nonzero exit status
set -o nounset  # abort on unbound variable
set -o pipefail # don't hide errors within pipes

# Replace literal string in file. Exits with status 1 if the file or search string could not be found.
# $1: Search string (literal, not regex)
# $2: Replacement string
# $3: File
replace_in_file() {
  printf 'Performing replacement in %s\n' "$3"
  [[ -f "$3" ]] || { printf 'File not found: %s' "$3"; exit 1; }
  export SEARCH_LITERAL="$1"
  export REPLACEMENT_LITERAL="$2"
  perl -i -0777 -pe 'BEGIN { $search = $ENV{SEARCH_LITERAL}; $replacement = $ENV{REPLACEMENT_LITERAL}; } s{\Q$search\E}{$replacement} || die "Search string not found\n";' "$3"
}

file_contains_literal() {
  # $1: literal string
  # $2: file
  export NEEDLE="$1"
  perl -0777 -ne 'exit(index($_, $ENV{NEEDLE}) >= 0 ? 0 : 1)' "$2"
}

patch_once() {
  # $1: marker id (used in marker string)
  # $2: Search string (literal, not regex)
  # $3: Replacement string
  # $4: File
  local marker="docx4j:patched:$1"

  if file_contains_literal "$marker" "$4"; then
    printf '%s already patched; skipping\n' "$4"
    return 0
  fi

  replace_in_file "$2" "$3" "$4"
}

GENERATED_SOURCES_DIR='target/generated-sources/xjc/'

printf 'Changing directory to %s\n' "$GENERATED_SOURCES_DIR"
cd "$GENERATED_SOURCES_DIR" || exit

###################################################################
# Update constructors for custom collection type, ArrayListDocx4j #
###################################################################

printf 'Updating ArrayListDocx4j constructor to pass "this" as argument\n'
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS - BSD sed
  find . -type f -name '*.java' -exec sed -i '' 's/new ArrayListDocx4j<>();/new ArrayListDocx4j<>(this);/g' {} +
else
  # else assume GNU sed
  find . -type f -name '*.java' -exec sed -i 's/new ArrayListDocx4j<>();/new ArrayListDocx4j<>(this);/g' {} +
fi

############
# Document #
############

printf 'Updating Document to set parent when body is set \n'

SEARCH=$(cat <<'EOF'
    public void setBody(Body value) {
        this.body = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setBody(Body value) {
        this.body = value;
        value.setParent(this);
    }
    // docx4j:patched:Document-setBody-parent
EOF
)

patch_once "Document-setBody-parent" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/Document.java"

#######
# PML #
#######

printf 'Updating CTSection to lazily create sldIdLst\n'

SEARCH=$(cat <<'EOF'
    public CTSectionSlideIdList getSldIdLst() {
        return sldIdLst;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public CTSectionSlideIdList getSldIdLst() {
        if (sldIdLst == null) {
            sldIdLst = new CTSectionSlideIdList();
        }
        return sldIdLst;
    }
    // docx4j:patched:CTSection-getSldIdLst-lazy-create
EOF
)
patch_once "CTSection-getSldIdLst-lazy-create" "$SEARCH" "$REPLACEMENT" "org/pptx4j/com/microsoft/schemas/office/powerpoint/x2010/main/CTSection.java"

########################
# SdtElement Interface #
########################

printf 'Updating methods for classes that need to implement SdtElement\n'

#### SdtRun ####

SEARCH=$(cat <<'EOF'
    public void setSdtContent(CTSdtContentRun value) {
        this.sdtContent = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtContent(SdtContent value) {
        this.sdtContent = (CTSdtContentRun)value;
        ((CTSdtContentRun)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:SdtRun-setSdtContent
EOF
)
patch_once "SdtRun-setSdtContent" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/SdtRun.java"

SEARCH=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
        value.setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:SdtRun-setSdtPr
EOF
)
patch_once "SdtRun-setSdtPr" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/SdtRun.java"

#### SdtBlock ####

SEARCH=$(cat <<'EOF'
    public void setSdtContent(SdtContentBlock value) {
        this.sdtContent = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtContent(SdtContent value) {
        this.sdtContent = (SdtContentBlock)value;
        ((SdtContentBlock)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:SdtBlock-setSdtContent
EOF
)
patch_once "SdtBlock-setSdtContent" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/SdtBlock.java"

SEARCH=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
        value.setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:SdtBlock-setSdtPr
EOF
)
patch_once "SdtBlock-setSdtPr" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/SdtBlock.java"

#### CTSdtCell ####

SEARCH=$(cat <<'EOF'
    public void setSdtContent(CTSdtContentCell value) {
        this.sdtContent = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtContent(SdtContent value) {
        this.sdtContent = (CTSdtContentCell)value;
        ((CTSdtContentCell)value).setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:CTSdtCell-setSdtContent
EOF
)
patch_once "CTSdtCell-setSdtContent" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/CTSdtCell.java"

SEARCH=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
        value.setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:CTSdtCell-setSdtPr
EOF
)
patch_once "CTSdtCell-setSdtPr" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/CTSdtCell.java"

#### CTSdtRow ####

SEARCH=$(cat <<'EOF'
    public void setSdtContent(CTSdtContentRow value) {
        this.sdtContent = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtContent(SdtContent value) {
        this.sdtContent = (CTSdtContentRow) value;
        ((CTSdtContentRow) value).setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:CTSdtRow-setSdtContent
EOF
)
patch_once "CTSdtRow-setSdtContent" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/CTSdtRow.java"

SEARCH=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setSdtPr(SdtPr value) {
        this.sdtPr = value;
        value.setParent(this); // unmarshalling does this automatically; this helps user in other cases
    }
    // docx4j:patched:CTSdtRow-setSdtPr
EOF
)
patch_once "CTSdtRow-setSdtPr" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/CTSdtRow.java"

#####################
# WML ObjectFactory #
#####################

printf 'Updating WML ObjectFactory to add get() method returning factory instance\n'

SEARCH=$(cat <<'EOF'
public class ObjectFactory {
EOF
)
REPLACEMENT=$(cat <<'EOF'
public class ObjectFactory {

    private static ObjectFactory thisObjectFactory;

    public static ObjectFactory get() {
        if (thisObjectFactory==null) {
            thisObjectFactory=new ObjectFactory();
        }
        return thisObjectFactory;
    }
    // docx4j:patched:WmlObjectFactory-get
EOF
)
patch_once "WmlObjectFactory-get" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/ObjectFactory.java"

#####################
# DML ObjectFactory #
#####################

printf 'Updating DML ObjectFactory to make _UserShapes_QNAME public\n'

SEARCH=$(cat <<'EOF'
private static final QName _UserShapes_QNAME =
EOF
)
REPLACEMENT=$(cat <<'EOF'
// docx4j:patched:DmlChartObjectFactory-UserShapesQname
public static final QName _UserShapes_QNAME =
EOF
)
patch_once "DmlChartObjectFactory-UserShapesQname" "$SEARCH" "$REPLACEMENT" "org/docx4j/dml/chart/ObjectFactory.java"

############
# VML Line #
############

printf 'Updating CTLine field order\n'

# Note the trailing spaces in the Javadoc search string. Make sure your editor/formatter is not configured to remove trailing spaces.

SEARCH=$(cat <<'EOF'
    /**
     * Line Start
     * 
     */
    @XmlAttribute(name = "from")
    protected String from;
EOF
)
REPLACEMENT=$(cat <<'EOF'
    // docx4j:patched:CTLine-remove-generated-from
EOF
)
patch_once "CTLine-remove-generated-from" "$SEARCH" "$REPLACEMENT" "org/docx4j/vml/CTLine.java"

SEARCH=$(cat <<'EOF'
    /**
     * Line End Point
     * 
     */
    @XmlAttribute(name = "to")
    protected String to;
EOF
)
REPLACEMENT=$(cat <<'EOF'
    // docx4j:patched:CTLine-remove-generated-to
EOF
)
patch_once "CTLine-remove-generated-to" "$SEARCH" "$REPLACEMENT" "org/docx4j/vml/CTLine.java"

SEARCH=$(cat <<'EOF'
    /**
     * Unique Identifier
     * 
     */
    @XmlAttribute(name = "id")
    protected String vmlId;
EOF
)
REPLACEMENT=$(cat <<'EOF'
    // docx4j:patched:CTLine-remove-generated-id
EOF
)
patch_once "CTLine-remove-generated-id" "$SEARCH" "$REPLACEMENT" "org/docx4j/vml/CTLine.java"

SEARCH=$(cat <<'EOF'
    /**
     * Shape Styling Properties
     * 
     */
    @XmlAttribute(name = "style")
    protected String style;
EOF
)
REPLACEMENT=$(cat <<'EOF'
    /*
     * docx4j:patched:CTLine-attribute-order
     * Word is sensitive to the order of the id, style, from, and to attributes; see https://github.com/plutext/docx4j/issues/469
     */
    /**
     * Unique Identifier
     * 
     */
    @XmlAttribute(name = "id")
    protected String vmlId;
    /**
     * Shape Styling Properties
     * 
     */
    @XmlAttribute(name = "style")
    protected String style;
    /**
     * Line Start
     * 
     */
    @XmlAttribute(name = "from")
    protected String from;
    /**
     * Line End Point
     * 
     */
    @XmlAttribute(name = "to")
    protected String to;
EOF
)
patch_once "CTLine-attribute-order" "$SEARCH" "$REPLACEMENT" "org/docx4j/vml/CTLine.java"

#######
# Id #
#######

printf 'Adding Id.java equals/hashCode methods\n'

SEARCH=$(cat <<'EOF'
    @Override
    public Object copy() {
        Id copy = new Id();
        return copyTo(copy);
    }

}
EOF
)
REPLACEMENT=$(cat <<'EOF'
    @Override
    public Object copy() {
        Id copy = new Id();
        return copyTo(copy);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Id) {
            return val.equals(((Id) obj).getVal());
        } else {
            return false;
        }
    }

    public int hashCode() {
        if (val == null) {
            java.math.BigInteger newIdVal = java.math.BigInteger.valueOf(Math.abs(new java.util.Random().nextInt()));
            this.setVal(newIdVal);
            org.slf4j.LoggerFactory.getLogger(Id.class).warn("Generated Id val " + newIdVal);
        }

        // Natural and good enough...
        return val.intValue();
    }

    // docx4j:patched:Id-equals-hashCode

}
EOF
)
patch_once "Id-equals-hashCode" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/Id.java"

#########
# Style #
#########

printf 'Updating Style.java customStyle default\n'

SEARCH=$(cat <<'EOF'
    public boolean isCustomStyle() {
        if (customStyle == null) {
            return true;
        } else {
            return customStyle;
        }
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public boolean isCustomStyle() {
        if (customStyle == null) {
            // the style shall be assumed to be a built-in style: https://github.com/plutext/docx4j/issues/641
            return false;
        } else {
            return customStyle;
        }
    }
    // docx4j:patched:Style-customStyle-default
EOF
)
patch_once "Style-customStyle-default" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/Style.java"

#############
# Highlight #
#############

printf 'Updating Highlight.java with custom methods\n'

SEARCH=$(cat <<'EOF'
    public void setVal(String value) {
        this.val = value;
    }
EOF
)
REPLACEMENT=$(cat <<'EOF'
    public void setVal(String value) {

    	if (value==null) {
    		this.val = value;
    		return;
    	}

    	boolean inEnumeration = false;
    	for (int i = 0; i<colors.length; i++) {
    		if (value.equals(colors[i][0])) {
    			inEnumeration = true;
    			break;
    		}
    	}

    	if (inEnumeration) {
    		this.val = value;
    		return;
    	} else if (value.trim().startsWith("#")) {
    		value=value.trim().substring(1).toUpperCase();

        	for (int i = 0; i<colors.length; i++) {
        		if (value.equals(colors[i][1])) {
        			val = colors[i][0];
        			return;
        		}
        	}

    		log.error("use enumerated color, or implement algorithm to map to closest color: '" + value + "'");

    	} else if (value.trim().contains("rgb")) {

    		log.warn("TODO: implement rgb to color for '" + value + "'");
    	}
		log.error("Can't set w:highlight from '" + value + "'");
    	this.val = null;
    }

    public String getHexVal() {

    	if (val==null) return null;

    	for (int i = 0; i<colors.length; i++) {
    		if (val.equals(colors[i][0])) {
    			return "#" + colors[i][1];
    		}
    	}
		log.error("Unexpected w:highlight value '" + val + "'");
		return null;
    }

	protected static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Highlight.class);

	// See http://www.w3.org/TR/css3-color/#svg-color, except for darkYellow (for which I've used gold)
	private final static String[][] colors = { { "black", "000000" }, { "blue", "0000FF" },
			{ "cyan", "00FFFF" }, { "green", "008000" },
			{ "magenta", "FF00FF" }, { "red", "FF0000" },
			{ "yellow", "FFFF00" }, { "white", "FFFFFF" },
			{ "darkBlue", "00008B" }, { "darkCyan", "008B8B" },
			{ "darkGreen", "006400" }, { "darkMagenta", "8B008B" },
			{ "darkRed", "8B0000" }, { "darkYellow", "FFD700" },
			{ "darkGray", "A9A9A9" }, { "lightGray", "D3D3D3" } };

    // docx4j:patched:Highlight-custom-methods
EOF
)
patch_once "Highlight-custom-methods" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/Highlight.java"

#############
# Styles #
#############

printf 'Updating Styles.java with custom methods\n'

SEARCH=$(cat <<'EOF'
            public Boolean isQFormat() {
                return qFormat;
            }
EOF
)
REPLACEMENT=$(cat <<'EOF'
            public Boolean isQFormat() {
            	
            	if (qFormat==null) {
            		return ((Styles.LatentStyles)this.parent).isDefQFormat();
            	} else {            	
            		return qFormat;
            	}
            }
            // docx4j:patched:Styles-custom-methods
EOF
)
patch_once "Styles-custom-methods" "$SEARCH" "$REPLACEMENT" "org/docx4j/wml/Styles.java"

printf 'Done!\n'
