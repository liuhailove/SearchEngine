package com.myengine.cn;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/* Open keep-alive connection descriptor: */
public class ConnEntry
{
	Socket socket;		 /* The actual file descriptor   */
	PROTO proto; /* Protocol (PROTO_*)           */
	String addr;	 /* Destination IP               */
	int port;	/* Destination port             */
	boolean reused;  /* Used for earier requests?    */
	long req_start;/*  request start     */
	long last_rw;  /*  last read / write */
	LinkedList<String> read_buf=new LinkedList<String>();                 /* Current read buffer          */
	int  read_len;
	String write_buf;                /* Pending write buffer         */
	int write_off;                /* Current write offset         */
	int write_len;
	QueueEntry q;	/* Current queue entry          */
	ConnEntry prev; /* Previous connection entry    */
	ConnEntry next;  /* Next connection entry        */
}
