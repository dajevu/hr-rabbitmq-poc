package com.hireright.rabbit;

public class Constants {

    public static int MSG_COUNT = 5;
    public final static String EXCHANGE = "dhs.exchange";
    public final static String REQUEST_QUEUE = "dhs.request.queue";
    public final static String REPLY_QUEUE = "dhs.response.queue";
    public final static String EXCHANGE_TYPE = "topic";
    public final static String DEAD_LETTER_EXCHANGE =  "dhs.dead.letter";
    public final static String DEAD_LETTER_QUEUE = "dhs.dead.letter.queue";
    public final static String HIRERIGHT_SUBREQUEST_HEADER = "HireRightSubRequestId";
}
