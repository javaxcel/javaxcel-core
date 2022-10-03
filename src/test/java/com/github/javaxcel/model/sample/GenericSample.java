package com.github.javaxcel.model.sample;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Deque;

@ToString
@EqualsAndHashCode
public class GenericSample<
        ID extends Number,
        TITLE extends String,
        NUMBER extends BigDecimal,
        SUBTITLES extends Deque<TITLE>> {

    private ID id;

    private TITLE title;

    private NUMBER number;

    private TITLE[] titleArray;

    private SUBTITLES subtitles;

    private SUBTITLES[][] subtitles2DArray;

}
