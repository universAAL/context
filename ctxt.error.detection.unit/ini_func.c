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
 *  implelmentation of initialization process for the EDUCore.
 * 
 * @author <a href="mailto:hamzah.dakheel@uni-siegen.de">Hamzah Dakheel</a>
 * @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
 *    
 * 
 */





#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "ini_func.h"

//#include <conio.h>

//static void print_element_names(xmlNode * a_node);
//int set_message(xmlDoc * , xmlNode * );
//#ifdef LIBXML_TREE_ENABLED

int ini_fc (struct main_hashtable **users)
{
	const char *Filename= "~/error.detection.unit/test_xml.xml";	
	xmlDoc *doc= NULL;
	xmlNode *root_element=NULL;
	xmlNode *msg_node=NULL;
	doc = xmlParseFile(Filename);
	
	if(doc == NULL)
	{
		fprintf(stderr,"Error!. xml Configuration file (%s) is not parsed successfully\n ",Filename);
		return -1;
	}
	
	// Retrieve the document's root element.
	root_element = xmlDocGetRootElement(doc);
	
	// Check to make sure the document actually contains something
	if (root_element == NULL)
	{
		fprintf(stderr,"Document is Empty\n");
		xmlFreeDoc(doc);
		return -1;
	}
	
	/*
	 * We need to make sure the document is the right type. 
	*/
	if (xmlStrcmp(root_element->name, (const xmlChar *) "messages"))
	{
		fprintf(stderr,"Document is of the wrong type, root node != messages");
		xmlFreeDoc(doc);
		return -1;
	}
	
	/* 
	 * Get the first message of root_element.
	 */  
	msg_node = root_element->xmlChildrenNode; 
	
	// This loop iterates through the elements that are children of "root"
	while (msg_node != NULL)
	{
		if ((!xmlStrcmp(msg_node->name, (const xmlChar *)"message")))
		set_message(doc,msg_node, users);
		msg_node=msg_node->next;
	}
	
	/*free the document */
	xmlFreeDoc(doc);
	
	/*
	 * Free the global variables that may
	 * have been allocated by the parser.
	 */
	xmlCleanupParser(); 
	
	return 0;
} 

/*#else
int main(void) 
{
	fprintf(stderr, "Tree support not compiled in\n");
	exit(1);
}
#endif*/

void set_message(xmlDoc * doc,xmlNode * msg_node, struct main_hashtable **users)
{
	xmlChar *key;
	
	xmlAttr *message_attr;
	xmlAttr *process_attr;
	xmlNode *process_node = NULL;
	xmlNode *process_child = NULL;
	struct main_hashtable *mHash= malloc (sizeof (*mHash));
	int ID;
	//struct per_msg *pm = malloc (sizeof (*pm));
	
	// search for "ID" attribute in the "message" node
	message_attr = xmlHasProp(msg_node, (const xmlChar*)"ID");
	if (message_attr == NULL)
	{
		fprintf (stderr, "Attribute ID within message node is not set!");
		return;
	}
	else
	{
		key = xmlGetProp(msg_node, (const xmlChar*)"ID");
		//fprintf(stderr,"hash: %s\n", key);
		ID= atoi((char *)key);
		printf("%d\n",ID);
		
		mHash->Id = ID;//msg_id;
		mHash->head = NULL;
		HASH_ADD_INT (*users, Id, mHash);
	}
	// Get the the check process type
	process_node = msg_node->xmlChildrenNode;
	while (process_node != NULL)
	{
		if ((!xmlStrcmp(process_node->name, (const xmlChar *)"message_process")))
		{
			process_attr = xmlHasProp(process_node, (const xmlChar*)"process");
			if (process_attr == NULL)
			{
				fprintf (stderr, "Attribute process within process node is not set!");
				return;
			}
			else
			{
				char pro_name[10]; 				
				key = xmlGetProp(process_node, (const xmlChar*)"process");
				if (!xmlStrcmp(key,(const xmlChar*)"periodic"))
				{
					double period = -1, phase = -1, window = -1;
					xmlFree(key);
					process_child = process_node->xmlChildrenNode;
					while (process_child !=NULL)
					{
						if ((!xmlStrcmp(process_child->name, (const xmlChar *)"period")))
						{
							key = xmlNodeListGetString(doc, process_child->xmlChildrenNode, 1);
							
							period = atoi((char *)key);
							xmlFree(key);
							printf("period = %f\n", period);
							
						}
						if ((!xmlStrcmp(process_child->name, (const xmlChar *)"phase")))
						{
						        key = xmlNodeListGetString(doc, process_child->xmlChildrenNode, 1);
							phase = atoi((char *)key);
							xmlFree(key);
							printf("phase = %f\n", phase);
						}
						if ((!xmlStrcmp(process_child->name, (const xmlChar *)"window")))
						{
							key = xmlNodeListGetString(doc, process_child->xmlChildrenNode, 1);
							window = atoi((char *)key);
							xmlFree(key);
							printf("window = %f\n", window);							
						}
						
						
						//xmlFree(key);
						process_child = process_child->next;
					}
					if ((period == -1) || (phase == -1) || (window == -1) )
					printf("more information are needed to configure periodic messages\n");
					
					// if not upload the acquired information into data structure of EDU
					
					struct per_msg *pm = malloc (sizeof (*pm));
					pm->per = period;
					pm->phs = phase;
					pm->win = window;
					void *per_struct = pm;
					strcpy(pro_name,"per");
					set_list (pro_name, per_struct, mHash);
					
				}
				
				if (!xmlStrcmp(key,(const xmlChar *)"sporadic"))
				{
					double minInterarriavalTime = -1, maxInterarriavalTime = -1, window = -1;
					xmlFree(key);
					process_child = process_node->xmlChildrenNode;
					while (process_child !=NULL)
					{
						if ((!xmlStrcmp(process_child->name, (const xmlChar *)"min_interarriaval_time")))
						{
							key = xmlNodeListGetString(doc, process_child->xmlChildrenNode, 1);
							minInterarriavalTime = atoi((char *)key);
							xmlFree(key);
							printf("min_interarriaval_time = %f\n", minInterarriavalTime);
							
						}
						if ((!xmlStrcmp(process_child->name, (const xmlChar *)"max_interarriaval_time")))
						{
						        key = xmlNodeListGetString(doc, process_child->xmlChildrenNode, 1);
							maxInterarriavalTime = atoi((char*)key);
							xmlFree(key);
							printf("max_interarriaval_time = %f\n", maxInterarriavalTime);
						}
						if ((!xmlStrcmp(process_child->name, (const xmlChar *)"window")))
						{
							key = xmlNodeListGetString(doc, process_child->xmlChildrenNode, 1);
							window = atoi((char *)key);
							xmlFree(key);
							printf("window = %f\n", window);							
						}
						
						
						//xmlFree(key);
						process_child = process_child->next;
					}
					if ((minInterarriavalTime == -1) || (maxInterarriavalTime == -1) || (window == -1) )
					printf("more information are needed to configure sporadic messages\n");
					// if not upload the acquired information into data structure of EDU
					
					struct spr_msg *sprm = malloc (sizeof (*sprm));
					sprm-> min_interarriaval_t = minInterarriavalTime; // in ms
					sprm-> max_interarriaval_t = maxInterarriavalTime; // in ms
					sprm-> win = window;
					void *spr_struct = sprm;
					strcpy(pro_name,"spr");
					set_list (pro_name, spr_struct, mHash);
					
				}
			}
			
		}
		process_node = process_node->next;
	}
	
}

/*_____set_list_function______*/
void set_list (char *proc_name, void *pro_struct, struct main_hashtable *hash_ptr){
	
	struct pro_list *list = (struct pro_list*)malloc (sizeof(struct pro_list));
	strcpy(list->pro,proc_name);
	list->msg = pro_struct;
	LL_APPEND(hash_ptr->head,list);
}




