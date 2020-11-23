/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of 
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
class PropertiesBean implements Properties {

    private java.util.Properties props;

    @PostConstruct
    void startup() {
        log.debug("In PropertiesBean(Singleton)::startup()");

        try {
            InputStream propsStream = PropertiesBean.class.getResourceAsStream("/config.properties");
            props = new java.util.Properties();

            props.load(propsStream);
            propsStream.close();

            InputStream propsStream2 = PropertiesBean.class.getResourceAsStream("/app-version.properties");
            props.load(propsStream2);
            propsStream2.close();
        } catch (IOException e) {
            throw new UncheckedIOException("PropertiesBean initialization error", e);
        }
    }

    public String getProperty(final String name) {
        return props.getProperty(name);
    }
}
