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
 * Implementation of Error Detection Unit Core.
 * 
 * @author <a href="mailto:hamzah.dakheel@uni-siegen.de">Hamzah Dakheel</a>
 * @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
 *    
 * 
 */


/*______Hashtable_structure______
struct main_hashtable {	int Id;                    // key 
	struct pro_list* head;
	UT_hash_handle hh;         // makes this structure hashable 
};*/

/*______events_Calendar_List______*/		  
struct events_calendar {	int Id;
	struct timespec timeStamp;
	struct events_calendar *next, *prev;
};


/*______periodic time stamp list______*/
struct per_ts_list {	int Id;
	struct timespec timeStamp;
	struct per_ts_list *next, *prev;};		  
		  


/*______Processes_List_structure______
struct pro_list {
    char pro[10];
	void *msg;
    struct pro_list *next, *prev;
};*/

struct timeStamp{	char *seconds, *nanoseconds;};

/*______faulty messages list structure______*/
struct faulty_msg {	int Id, judgment;
	char process[10];
	struct timeStamp ts;	
	struct faulty_msg *next, *prev;};


/*
struct per_msg
{	double per;
	double phs;
	double win;
	//double win_max;
};

struct spr_msg{
   double min_interarriaval_t;
   double max_interarriaval_t;
   double win;
};*/

struct spr_time_stamp{	int Id;
	struct timespec timeStamp;
	struct spr_time_stamp *next, *prev;
};


struct rng_chk{
  double min_val;
  double max_val;
};

struct trend_chk{
   double max_drv;
   double min_drv;
};

/* * a pointer from timer_struct is passed as the sole arguement to the timer_thread funcion */

struct timer_struct{	char pro[10];
	//struct timespec sleep_time;
	void* pro_struct;	};
void per_msg_func(struct per_msg*, int msgId, bool itsFirstMsg, struct timespec curTS);
void spr_msg_func(struct spr_msg*, int msgId, bool itsFirstMsg, struct timespec curTS);
struct timespec add_timespec (struct timespec*, struct timespec*); 
void *timer_thread(void*);
void *publish(void *);
void sighand(int);
void set_spr_time_stamp(int, struct timespec);
//void timer_thread();

/* the user should set the message Id and the check processes related to it*/

