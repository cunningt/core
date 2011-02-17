/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.switchyard.deployment;

import org.apache.log4j.Logger;

import org.jboss.deployers.spi.DeploymentException;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

public class SwitchYardDeployer extends AbstractSimpleVFSRealDeployer<SwitchYardMetaData> {	
	
    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(SwitchYardDeployer.class);
	
    /**
     * No args constructor.
     */
    public SwitchYardDeployer()
    {
        super(SwitchYardMetaData.class);	
        setOutput(BeanMetaData.class);
        setStage(DeploymentStages.POST_CLASSLOADER);
    }

    @Override
    public void deploy(VFSDeploymentUnit unit, SwitchYardMetaData metaData)
            throws DeploymentException {
    	try {
    	    String fileName = unit.getSimpleName();
	    if (metaData != null) { 
		// TODO : Do the same things as the CDI deployer to deploy
		// domains and services
	    }
        } catch (Exception e) {
           throw DeploymentException.rethrowAsDeploymentException("Error creating switchyard deployment " + unit.getName(), e);
        }
    }
    
    public void undeploy(DeploymentUnit unit, SwitchYardMetaData metadata) {
	// TODO : clean up
    }
}
