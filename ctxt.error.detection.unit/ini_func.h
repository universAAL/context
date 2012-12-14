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
 * Header of Initialization function.
 * 
 * @author <a href="mailto:hamzah.dakheel@uni-siegen.de">Hamzah Dakheel</a>
 * @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
 *    
 * 
 */

#include "utlist.h"
#include "uthash.h"
#include <libxml/parser.h>
#include <libxml/tree.h>

/*______Hashtable_structure______*/
struct main_hashtable {	int Id;                    /* key */
	struct pro_list* head;
	UT_hash_handle hh;         /* makes this structure hashable */
};



/*______Processes_List_structure______*/
struct pro_list {
    char pro[10];
	void *msg;
    struct pro_list *next, *prev;
};


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
};

void set_message (xmlDoc *doc, xmlNode *node, struct main_hashtable **);
int ini_fc(struct main_hashtable **);
void set_list (char *proc_name, void *pro_struct, struct main_hashtable *hash_ptr);
