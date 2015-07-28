package com.myengine.cn;

public class IssueDesc
{
	  int   type;                                   /* PROB_*                    */
	  String   extra;                               /* Problem-specific string   */
	  HttpRequest req;                     /* HTTP request sent         */
	  HttpResponse res;                    /* HTTP response seen        */
}
