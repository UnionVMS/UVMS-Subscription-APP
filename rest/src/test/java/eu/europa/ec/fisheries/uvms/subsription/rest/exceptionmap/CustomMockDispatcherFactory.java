/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * Recreates the functionality of the {@code org.jboss.resteasy.mock.MockDispatcherFactory},
 * activating CDI and the application-specific providers.
 */
public class CustomMockDispatcherFactory {
	public static Dispatcher createDispatcher(Class<?>... providers) {
		ResteasyProviderFactory providerFactory = new ResteasyProviderFactoryImpl();
		providerFactory.setInjectorFactory(new CdiInjectorFactory());
		for (Class<?> provider : providers) {
			providerFactory.registerProvider(provider);
		}
		Dispatcher dispatcher = new SynchronousDispatcher(providerFactory);
		ResteasyProviderFactory.setInstance(providerFactory);
		RegisterBuiltin.register(providerFactory);
		return dispatcher;
	}
}
