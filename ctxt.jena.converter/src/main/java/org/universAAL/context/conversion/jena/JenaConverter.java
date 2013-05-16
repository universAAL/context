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
 * Interface for those that wish to implement the conversion between Resources
 * of Jena and Resources of universAAL.
 * 
 * @author mtazari
 * 
 */
public interface JenaConverter {
    /**
     * Get the root Resource of a Jena Model, so that it can be passed for
     * conversion to another method.
     * 
     * @param m
     *            The {@link com.hp.hpl.jena.rdf.model.Model} to get the root
     *            from
     * @return The {@link com.hp.hpl.jena.rdf.model.Resource} that is at the
     *         root of the Model
     */
    public com.hp.hpl.jena.rdf.model.Resource getJenaRootResource(Model m);

    /**
     * Convert a universAAL Resource into a Jena Resource
     * 
     * @param r
     *            The uAAL {@link org.universAAL.middleware.rdf.Resource} to
     *            convert
     * @return The resulting Jena {@link com.hp.hpl.jena.rdf.model.Resource}
     */
    public com.hp.hpl.jena.rdf.model.Resource toJenaResource(Resource r);

    /**
     * Convert a Jena Resource into a universAAL Resource. This method still
     * shows the original name "PERSONA", because it is used in many other
     * artifacts.
     * 
     * @param r
     *            The Jena {@link com.hp.hpl.jena.rdf.model.Resource} to convert
     * @return The resulting uAAL {@link org.universAAL.middleware.rdf.Resource}
     */
    public Resource toPersonaResource(com.hp.hpl.jena.rdf.model.Resource r);

    /**
     * Update a Jena Resource that is linked to a database with a new value.
     * This is a helper method for artifacts that have direct access to the Jena
     * database. Its usage is discouraged.
     * 
     * @param dbRes
     *            The Jena Resource that is backed up by a Database
     * @param updater
     *            The new value of the Resource that will be saved
     * @return <code>true</code> if succeeded
     */
    public boolean updateDBResource(com.hp.hpl.jena.rdf.model.Resource dbRes,
	    com.hp.hpl.jena.rdf.model.Resource updater);
}
