/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
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
package org.universAAL.context.conversion.jena;

import org.universAAL.middleware.rdf.Resource;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author mtazari
 * 
 */
public interface JenaConverter {
    public com.hp.hpl.jena.rdf.model.Resource getJenaRootResource(Model m);

    public com.hp.hpl.jena.rdf.model.Resource toJenaResource(Resource r);

    public Resource toPersonaResource(com.hp.hpl.jena.rdf.model.Resource r);

    public boolean updateDBResource(com.hp.hpl.jena.rdf.model.Resource dbRes,
	    com.hp.hpl.jena.rdf.model.Resource updater);
}
