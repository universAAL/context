/*Copyright [2011-2012] [University of Siegen, Embedded System Instiute]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
      
   */

/**
 * 
 * Header of Error Detection Unit Core.
 * 
 * @author <a href="mailto:hamzah.dakheel@uni-siegen.de">Hamzah Dakheel</a>
 * @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
 *    
 * 
 */



#include <stdio.h>
#include <jni.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>
#include <math.h>
#include <stdbool.h>
#include <pthread.h>
#include <signal.h>
#include <errno.h>
#include <unistd.h>//sleep()

#include "log.h"
#include "JNIMessageDelivery.h"
#include "ini_func.h"
#include "error_det.h"


double interarriaval_t_mx[2003];

int j = 0;
//___accomulators for debuging the results____//
int cor = 0, incor = 0, notcom= 0;
int early = 0, late = 0, timly= 0, nc = 0;
//__________________________________________//


struct main_hashtable *users = NULL;  //initialize a hash table
struct events_calendar *evt_list = NULL; // initialization for event calendar list
struct per_ts_list *per_list = NULL;// initialization for per timestamps list
struct spr_time_stamp *spr_list= NULL; //initialization for spr timestam list
struct faulty_msg *fault_list= NULL;// three threads could access to this list


pthread_t timerThread;// this thread is declared as global.its started by timmer_trigger native methode and interrupted whenever a correct message is received.
pthread_t publishThread;//
pthread_mutex_t starter_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t calendar_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t spr_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t fault_msg_mutex = PTHREAD_MUTEX_INITIALIZER;

bool turn = false;
jclass Gcls;




/* * list of the scheduled events. it contains specially the events that have determinstic timing behaviour  * like periodic messages and sporadic messages with limited interarriaval time. */
 
/*______sorting_func______*/

int tsCmp (struct events_calendar *a, struct events_calendar *b){	if (a->timeStamp.tv_sec == b->timeStamp.tv_sec){		if (a->timeStamp.tv_nsec == b->timeStamp.tv_nsec)return 0;
		else if (a->timeStamp.tv_nsec < b->timeStamp.tv_nsec)return -1;
		else return 1;
	}
	else if (a->timeStamp.tv_sec < b->timeStamp.tv_sec) return -1;
	else return 1; 
} 

void set_events_calendar (int msgId, struct timespec ts){	bool newMember = true;	struct events_calendar *temp;
	struct events_calendar *list = (struct events_calendar*)malloc (sizeof(struct events_calendar));	if (evt_list != NULL){
			LL_FOREACH(evt_list,temp)
		{			if (temp->Id == msgId)
			{				temp->timeStamp=ts;
				newMember = false;
							}		}
			}
	if(evt_list == NULL || newMember)
	{	list->Id = msgId;
	list->timeStamp = ts;
	LL_APPEND(evt_list,list);
	}
	LL_SORT(evt_list, tsCmp);	
}

struct events_calendar get_events_calendar(){	struct events_calendar temp;
	temp.Id = evt_list->Id;
	temp.timeStamp = evt_list->timeStamp;
	return temp;
	}

/* * set the refernce point of time of each periodic message */
void set_per_msg_rt (int msgId, struct timespec ts ) {	struct per_ts_list *list = (struct per_ts_list*)malloc (sizeof(struct per_ts_list));
	list->Id = msgId;
	list->timeStamp = ts;
	LL_APPEND(per_list,list);	}

struct timespec* get_per_msg_rt(int msg_Id){	struct timespec *rt = NULL;
	struct per_ts_list *ptl;
	LL_FOREACH(per_list, ptl)
	{		if (ptl->Id == msg_Id)
		{			rt = &(ptl->timeStamp);
					}
		}
	return rt;}

void set_spr_time_stamp (int msg_Id, struct timespec ts)
	{		bool newMember = true;
		struct spr_time_stamp *temp;		struct spr_time_stamp *st = (struct spr_time_stamp*)malloc (sizeof(struct spr_time_stamp));
		if (spr_list != NULL)
		{			LL_FOREACH(spr_list, temp)
			{				if (temp->Id == msg_Id)
				{					temp->timeStamp=ts;					newMember = false;				}			}
		}
		
		if (spr_list == NULL || newMember)
		{			st->Id = msg_Id;
			st->timeStamp=ts;
			LL_APPEND(spr_list,st);		}					

	}
	
struct timespec get_prev_spr_ts (int msg_Id){	struct timespec ts ;
	struct spr_time_stamp *sts;
	LL_FOREACH(spr_list, sts)
	{		if (sts->Id == msg_Id)
		{			ts = sts->timeStamp;		}	}
	
	return ts;}

void set_fault_queue(int msg_Id, int judgment, char pro[10], struct timespec ts)
{	struct faulty_msg *fm = (struct faulty_msg *)malloc(sizeof (struct faulty_msg));
	char seconds[20];
	char nanoseconds[20];
	sprintf(seconds,"%ld",ts.tv_sec);
	sprintf(nanoseconds,"%ld",ts.tv_nsec);
	fm->Id = msg_Id;
	fm->judgment = judgment;
	strcpy(fm->process,pro);
	fm->ts.seconds = seconds;
	fm->ts.nanoseconds = nanoseconds;
	LL_APPEND(fault_list,fm);}

struct faulty_msg get_fault_queue()
{	struct faulty_msg *f;
	struct faulty_msg msg;
	f=fault_list;
	msg.Id=f->Id;
	msg.judgment= f->judgment;
	msg.ts= f->ts;
		
	LL_DELETE(fault_list,f);
	
	return msg;	}








double timerdiff(struct timespec *e, struct timespec *f){
double result = (e->tv_sec * 1000 + e->tv_nsec/1000000) -(f->tv_sec * 1000 + f->tv_nsec/1000000);
return result;}




JNIEXPORT void JNICALL Java_org_universAAL_messageClassifcationLayer_MCL_MessageDelivery_timer_1trigger
  (JNIEnv *env, jobject obj){  	//initialzation is possible here
  	int rc;
  	struct sigaction        actions;
  	memset(&actions, 0, sizeof(actions));
  	sigemptyset(&actions.sa_mask);
  	actions.sa_flags = 0;
  	actions.sa_handler = sighand;
  	jclass cls;
  	
  	cls = (*env)->FindClass(env, "org/universAAL/messageClassifcationLayer/MCL/CPublisher");
  	if(cls ==0)
	printf("couldn't find CPublisher class\n");
	else
	
	Gcls =(*env)->NewGlobalRef(env,cls);
	

  	rc = ini_fc(&users);
  	if (rc != 0){  		return;  	}
  	rc = sigaction(SIGALRM,&actions,NULL);  	rc = pthread_mutex_lock(&starter_mutex);
  	if (rc != 0){  		printf ("error nr. from timertriggere class is %d\n", rc);  	}
  	
  	
  	rc = pthread_create(&publishThread,NULL,&publish,env);
	if(rc){		printf("error in creating pthread");
		return;	}
  	rc=pthread_create(&timerThread,NULL,&timer_thread,NULL);
	if(rc){		printf("error in creating pthread");
		return;	}
	//block until the thread return 
	rc = pthread_join(timerThread, NULL);
  	
  	  }
  
  
JNIEXPORT void JNICALL Java_org_universAAL_messageClassifcationLayer_MCL_MessageDelivery_eventHandler
  (JNIEnv *env, jobject obj, jint ID, jboolean count)
  {  	struct timespec curTS;
  	struct pro_list *pl;
	struct main_hashtable *ht = (struct main_hashtable*) malloc (sizeof (struct main_hashtable));  	bool itsFirstMsg = count;  	int msg_Id = ID;
 

  	clock_gettime(CLOCK_MONOTONIC, &curTS);
  	HASH_FIND_INT( users, &msg_Id, ht );
	if (ht == NULL)
	return;
	LL_FOREACH(ht->head,pl){
		if(!strcmp(pl->pro,"per"))
		{			struct per_msg* pm = (struct per_msg*) pl->msg;

			per_msg_func(pm, msg_Id, itsFirstMsg, curTS);
		}
		else if (!strcmp(pl->pro, "spr"))
		{			struct spr_msg* sm = (struct spr_msg*) pl->msg;
			spr_msg_func(sm, msg_Id, itsFirstMsg, curTS);
		}
		/*else if (!strcmp(elt->pro,"abs_val"))
		{			struct rng_chk* rc = (struct rng_chk*) elt->msg;
			rng_chk_func (rc,msg, msg_Id);
		}
		else if (!strcmp(elt->pro,"trend"))
		{			struct trend_chk* tc = (struct trend_chk*) elt->msg;
			trend_chk_func (msg_Id, tc, msg, time_stamp);
		}
		else if (!strcmp(elt->pro,"pro1"))
		{			pro1_chk_func (msg_Id, rcvd_ts, msg);
		}*/
		else
		printf ("The %s process is not available\n", pl->pro);
	}
  

  	
  	  }
  
 
 void per_msg_func(struct per_msg *pm, int msg_Id, bool itsFirstMsg, struct timespec ts)
 {
	int rc, cur_per;
	double real_phs;
	struct timespec *rt; //reference time
	
	
	if (itsFirstMsg)
	{		set_per_msg_rt(msg_Id, ts);
				
		struct timespec period = {0,(pm->per + pm->win)* 1000 * 1000};
		struct timespec next_period = add_timespec (&ts, &period);  //to sum two struct of type timespec
		//____add comment
		rc = pthread_mutex_lock(&calendar_mutex);
		if (rc != 0){			printf ("error nr. from per_msg_fun class is %d\n", rc);
			}
		set_events_calendar(msg_Id, next_period );
		rc = pthread_mutex_unlock(&calendar_mutex);
		if (rc != 0){			printf ("error nr. from per_msg_fun class is %d\n", rc);
		}
		if(!turn)
		{			turn = true;
			rc = pthread_mutex_unlock(&starter_mutex);
			if (rc != 0){			printf ("error nr. from per_msg_fun class is %d\n", rc);
			}					}
			
		

			}
	else
	{				rt = get_per_msg_rt(msg_Id);//not done
		if (rt == NULL)
		{			printf("couldn't find the desired message in the reference time list\n");		}
		double diff = timerdiff(&ts,rt);
				
		cur_per = floor(diff/pm->per);
		real_phs = fabs ( (cur_per-( diff / pm->per )) * pm->per );
		
		
		if ( ((pm->per - real_phs) < (pm->win)) || ((pm->per - real_phs) == pm->win) || real_phs < pm->win || real_phs == pm->win)
		{			//printf("the message came timly\n");
			++cor;			rc = pthread_kill(timerThread, SIGALRM);
			if(rc)
			{ 				printf("kill process didn't not succeed\n");			}
		}
		else
		{			//printf("message with ID %d is untimly\n", msg_Id);
			rc = pthread_mutex_lock(&fault_msg_mutex);
			set_fault_queue(msg_Id,1,"periodic",ts);
			rc = pthread_mutex_unlock(&fault_msg_mutex);
			++incor;
			pthread_kill(publishThread, SIGALRM);		}
		
		
	}
		
}

void spr_msg_func(struct spr_msg *sm, int msg_Id, bool isFirstMsg, struct timespec ts){	
	int rc;
	struct timespec prev_ts,period, nxt_per;
	struct events_calendar temp1;
	if (isFirstMsg)
	{		pthread_mutex_lock(&spr_mutex);		set_spr_time_stamp(msg_Id, ts);
		pthread_mutex_unlock(&spr_mutex);
		struct timespec period1 = {0,(sm->max_interarriaval_t + sm->win + 3)* 1000 * 1000};
		nxt_per = add_timespec (&ts, &period1);  //to sum two struct of type timespec
		
		pthread_mutex_lock(&calendar_mutex);
		set_events_calendar(msg_Id, nxt_per );
		pthread_mutex_unlock(&calendar_mutex);

		if (!turn)
		turn = true;
	}
	else{		/*		 * getting the time stamping of the previous message that carry the same Id.		 * it sould be protected from other spr messages to avoid interference on a global 		 * variable.		 */
		pthread_mutex_lock(&spr_mutex);		prev_ts = get_prev_spr_ts(msg_Id);
		pthread_mutex_unlock(&spr_mutex);
		double  interarriaval_t = timerdiff(&ts, &prev_ts);
		interarriaval_t_mx[j]=interarriaval_t;
		j=j+1;
		pthread_mutex_lock(&spr_mutex);
		set_spr_time_stamp(msg_Id, ts);
		pthread_mutex_unlock(&spr_mutex);
					
		//printf("%f\n",interarriaval_t);
		if ( interarriaval_t > (sm->max_interarriaval_t+ sm->win + 3) )
		{			//printf ("spr msg came late\n");
			rc = pthread_mutex_lock(&fault_msg_mutex);
			set_fault_queue(msg_Id,1,"sparodic",ts);
			rc = pthread_mutex_unlock(&fault_msg_mutex);
			++late;
		}
		else if ( interarriaval_t < (sm->min_interarriaval_t- sm->win + 3) )
		{			//printf ("spr msg came early\n");
			rc = pthread_mutex_lock(&fault_msg_mutex);
			set_fault_queue(msg_Id,2,"sparodic",ts);
			rc = pthread_mutex_unlock(&fault_msg_mutex);
			++early;
			
		}
		else
		{			//printf ("spr msg is timly:  \n");
			++timly;
			/*			 * set the current ts of the timely messages as a prev. ts 			 * of the next message.			 */		

			pthread_mutex_lock(&calendar_mutex);
			temp1 = get_events_calendar();
			pthread_mutex_unlock(&calendar_mutex);
			if (temp1.Id != msg_Id)
			{				period.tv_sec = 0; period.tv_nsec = (sm->max_interarriaval_t + sm->win + 3)* 1000 * 1000;
				nxt_per = add_timespec (&ts, &period);				pthread_mutex_lock(&calendar_mutex);
				set_events_calendar(msg_Id, nxt_per);
				pthread_mutex_unlock(&calendar_mutex);
							}
			else			
			rc = pthread_kill(timerThread, SIGALRM);
			

			/*			 * update the previous timestamp table of the spr messages			 */
			/*pthread_mutex_lock(&spr_mutex);
			set_spr_time_stamp(msg_Id, ts);
			pthread_mutex_unlock(&spr_mutex);*/		
			
		}	
		
			}
	
	
	
	}


	
	
void *timer_thread(void* p)

{	int rc,i,bc;
	i= 1000;
	
		struct pro_list *pl;
	struct main_hashtable *ht = (struct main_hashtable*) malloc (sizeof (struct main_hashtable));	struct events_calendar temp;
	struct timespec period, next_period, cur;
	
	//take the required sleep time until the first message come and set the calendar
	rc = pthread_mutex_lock(&starter_mutex);
	if (rc != 0){	printf ("error nr. from timer_thread 1 class is %d\n", rc);
	}	
	while (i)
	{		/*		 * delete *temp and and get temp struct instead from a get(). this insure that the value will 		 * not change during the read operation		 */
		
		rc = pthread_mutex_lock(&calendar_mutex);
		
		if (rc != 0){			printf ("error nr. from timer_thread 1 class is %d\n", rc);
		}
		
		temp = get_events_calendar();
		
		rc = pthread_mutex_unlock(&calendar_mutex);
		if (rc != 0){			printf ("error nr. from timer_thread 2 class is %d\n", rc);
		}
		
		//clock_gettime(CLOCK_MONOTONIC, &old);
		bc = clock_nanosleep(CLOCK_MONOTONIC, TIMER_ABSTIME, &temp.timeStamp, NULL);
		clock_gettime(CLOCK_MONOTONIC, &cur);
		
		HASH_FIND_INT( users, &temp.Id, ht );
		LL_FOREACH(ht->head,pl)
		{			if(!strcmp(pl->pro,"per"))
			{				struct per_msg* pm = (struct per_msg*) pl->msg;
				period.tv_sec = 0; period.tv_nsec= (pm->per)* 1000 * 1000;
					
				if (bc == 0)
				{					++notcom;
					rc = pthread_mutex_lock(&fault_msg_mutex);
					set_fault_queue(temp.Id,0,"periodic",cur);
					rc = pthread_mutex_unlock(&fault_msg_mutex);
					pthread_kill(publishThread, SIGALRM);
					//printf("the message with phase %f dindn't come \n", pm->phs);										}
				
				next_period = add_timespec (&temp.timeStamp, &period);
					
			}
			else if(!strcmp(pl->pro,"spr"))
			{				struct spr_msg* sm = (struct spr_msg*) pl->msg;
				period.tv_sec = 0; period.tv_nsec = (sm->max_interarriaval_t + sm->win + 3)* 1000 * 1000;
				
				if (bc == 0)
				{					printf("the spr message which lies btw %f and  %f dindn't come \n", sm->max_interarriaval_t, sm->min_interarriaval_t);
					/*pthread_mutex_lock(&spr_mutex);
					 set_spr_time_stamp(temp.Id, temp.timeStamp);
					 pthread_mutex_unlock(&spr_mutex);*/
							
					next_period = add_timespec (&temp.timeStamp, &period);
					
					++nc;	
					rc = pthread_mutex_lock(&fault_msg_mutex);
					set_fault_queue(temp.Id,0,"sparodic",cur);
					rc = pthread_mutex_unlock(&fault_msg_mutex);
					pthread_kill(publishThread, SIGALRM);														}
				else
				{					clock_gettime(CLOCK_MONOTONIC, &cur);
					next_period = add_timespec (&cur, &period);
										}

					
					
			}
			
		}//LL_FOREACH()
		
		rc = pthread_mutex_lock(&calendar_mutex);
		if (rc != 0){			printf ("error nr. from timer_thread2 class is %d\n", rc);
		}
		set_events_calendar(temp.Id, next_period);
		rc = pthread_mutex_unlock(&calendar_mutex);
		if (rc != 0){			printf ("error nr. timer_thread2 class is %d\n", rc);
		}
			
		

			i = i-1;
		
	}//while loop end
	
	printf("the timly messages is %d and the untimly once is %d and %d\n",cor,incor, notcom);			
	/*printf ("the timly msgs is: %d, the early is : %d, the late one is: %d, nc is : %d\n",timly,early,late,nc);
	int h = 0;
	for (h;h<1000;++h){		printf("this is time diff : %f\n", interarriaval_t_mx[h] );	}*/
		
		/*		 * in case the message didn't come at its expected time, then try to inform the		 * diagnosis unit about that. Depending on the message type, the report to the 		 * diagnosis unit will be different.		 */
		 
	pthread_exit(NULL);	 	 		


	
	}// ending of timer thread


void* publish(void *ev)
{	
	int rc;
	JNIEnv *env= (JNIEnv*)ev;
	JavaVM *jvm;
	jmethodID mid;
        jsize   vmcount;
        jsize size = sizeof(jvm);
        jint ok= 1;
        ok = JNI_GetCreatedJavaVMs(&jvm,size,&vmcount);
        //printf("%d\n", ok);
        ok = (*jvm)->AttachCurrentThread(jvm,(void**) &env, NULL);
        // ok = (*jvm)->GetEnv(jvm,(void**)&env,JNI_VERSION_1_6);
        //printf("%d\n", ok);
        
        mid = (*env)->GetStaticMethodID(env, Gcls, "handleFault", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
        if(mid ==0)
        printf("coudln't find the intended method\n");
        else
        {		int msg_Id, judgment;
		jstring process, seconds, nanoseconds;        	        	while(1)
        	{        		//sleep a suitable time until interrupted or sleep again
        		Log("before sleep\n");
        		rc = sleep(50);
        		if (rc != 0 && errno == EINTR)
        		{        			Log("After Sleep\n");
        			rc = pthread_mutex_lock(&fault_msg_mutex);
        			struct faulty_msg msg = get_fault_queue();
        			rc = pthread_mutex_unlock(&fault_msg_mutex);
        			msg_Id = msg.Id;
        			judgment = msg.judgment;
        			process = (*env)->NewStringUTF(env, msg.process);
        			seconds = (*env)->NewStringUTF(env, msg.ts.seconds);
        			nanoseconds = (*env)->NewStringUTF(env, msg.ts.nanoseconds);
        			//getting parm from the faulty message buffer or struct
        			//call java method
        			(*env)->CallStaticIntMethod(env, Gcls, mid,msg_Id, judgment, process, seconds, nanoseconds);
        			
        		}
        	}        	        }
        
				
 
	
	pthread_exit(NULL);			}
	

struct timespec add_timespec (struct timespec *ts, struct timespec *period)
{	long max_nano = 999999999;
	struct timespec result = {0,0};
	double diff_nano = max_nano - (ts->tv_nsec + period->tv_nsec);
	if (diff_nano < 0)
	{		result.tv_sec = ts->tv_sec + 1;
		result.tv_nsec = fabs(diff_nano);
		
	}
	else
	{		result.tv_sec = ts->tv_sec + period->tv_sec;
		result.tv_nsec = ts->tv_nsec + period->tv_nsec;
		
	}
	return result;}

void sighand(int signo)
{
  //pthread_t             self = pthread_self();
  //pthread_id_np_t       tid;
 
  //pthread_getunique_np(&self, &tid);
  printf("Thread in signal handler  :%d\n", getpid());
  return;
}



