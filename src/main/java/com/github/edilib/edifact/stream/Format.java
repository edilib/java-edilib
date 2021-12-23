package com.github.edilib.edifact.stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Format {
    public static final Format UNEDIFACT_DEFAULT = new Format(true,true, ':', '+', '.', '?', '*', '\'');
    public static final Format X12_DEFAULT = new Format(true,false, '>', '*', '.', '?', ' ', '~');

    public final boolean skipNewlineAfterSegment;
    public final boolean unaAllowed;
    public final char componentDataElementSeperator;
    public final char dataElementSeperator;
    public final char decimalMark;
    public final char releaseCharacter;
    public final char repetitionSeperator; // optional, if unset ' '
    public final char segmentTerminator;

    public boolean hasRepetitionSeperator() {
        return releaseCharacter != ' ';
    }
}
