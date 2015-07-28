package com.myengine.cn;

import java.util.List;

public class PivotDesc
{
	byte type;                              /* PIVOT_*                   */
	byte state;                             /* PSTATE_*                  */
	byte linked;                            /* Linked to? (0/1/2)        */
	byte missing;                            /* Determined to be missing? */
	byte csens;                              /* Case sensitive names?     */
	byte c_checked;                          /* csens check done?         */
	String name;                            /* Directory / script name   */
	HttpRequest req;                         /* Prototype HTTP request    */
	String fuzz_par;                         /* Fuzz target parameter     */
	List<String>try_list;                    /* Values to try             */
	long try_cnt;                             /* Number of values to try   */
	long try_cur;                              /* Last tested try list offs */
	PivotDesc parent;                          /* Parent pivot, if any      */	
	List<PivotDesc>child;                     /* List of children          */
	long child_cnt;                           /* Number of children        */
	long desc_cnt;                             /* Number of descendants     */
	List<IssueDesc> issue;                      /* List of issues found      */
	long issue_cnt;                           /* Number of issues          */
	long desc_issue_cnt;                     /* Number of child issues    */
	HttpResponse res;                          /* HTTP response seen        */
	byte res_varies;                                /* Response varies?          */
	byte bad_parent;                                /* Parent is well-behaved?   */

	/* Fuzzer and probe state data: */

	byte no_fuzz;                                   /* Do not attepmt fuzzing.   */
	byte sure_dir;                                  /* Very sure it's a dir?     */
	byte  uses_ips;                                 /* Uses IPS filtering?       */

	long cur_key;                                  /* Current keyword           */
	long pdic_cur_key;                             /* ...for param dict         */

	byte guess;                                     /* Guess list keywords?      */
	byte pdic_guess;                                /* ...for param dict         */

	long pending;                                  /* Number of pending reqs    */
	long pdic_pending;                             /* ...for param dict         */
	long num_pending;                              /* ...for numerical enum     */
	long try_pending;                              /* ...for try list           */
	long r404_pending;                             /* ...for 404 probes         */
	long ck_pending;                               /* ...for behavior checks    */

	long check_idx;                                /* Current injection test    */
	long check_state;                              /* Current injection test    */

	List<HttpSig> r404;                /* 404 response signatures   */
	long r404_cnt;                                 /* Number of sigs collected  */
	HttpSig unk_sig;                      /* Original "unknown" sig.   */

	/* Injection attack logic scratchpad: */
	List<HttpRequest>  misc_req; /* Saved requests            */
	List<HttpResponse> misc_res; /* Saved responses           */
	byte misc_cnt;                                  /* Request / response count  */
    byte i_skip;                        /* Injection step skip flags */
    byte i_skip_add;
    byte r404_skip;
    byte bogus_par;                                 /* fuzz_par does nothing?    */
    byte ognl_check;                                /* OGNL check flags          */
  /* Reporting information: */
    long total_child_cnt;                          /* All children              */
    long total_issues;                          /* Issues by severity        */
    byte  dupe;                                     /* Looks like a duplicate?   */
    long pv_sig;                                /* Simple pivot signature    */	
}
