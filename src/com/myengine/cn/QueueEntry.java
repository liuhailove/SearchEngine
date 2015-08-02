package com.myengine.cn;

/* Request queue descriptor: */
public class QueueEntry
{
	HttpRequest req;	/* Request descriptor           */
	HttpResponse res;	/* Response descriptor          */
	ConnEntry c;		/* Connection currently used    */
	QueueEntry prev;	/* Previous queue entry         */
	QueueEntry next;    /* Next queue entry             */
	boolean retrying;	/* Request being retried?       */
	
}
