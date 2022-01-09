package io.github.edilib.edifact.stream.scanner;

// https://unece.org/fileadmin/DAM/trade/edifact/untdid/d422_s.htm#interchange
public enum ScannerTokenType {
    UNA_TAG,
    REPETITION_SEPERATOR, // default *
    COMPONENT_DATA_ELEMENT_SEPERATOR, // default :
    DATA_ELEMENT_SEPERATOR, // default +
    SEGMENT_TERMINATOR, // default '
    EOF,
    VALUE,
    ERROR;
}
