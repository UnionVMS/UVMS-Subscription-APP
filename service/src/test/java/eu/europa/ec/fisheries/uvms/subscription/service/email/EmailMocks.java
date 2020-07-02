/*
 *
 *  Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.
 *
 *  This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package eu.europa.ec.fisheries.uvms.subscription.service.email;

public class EmailMocks {

    static EmailAttachment createXmlAttachment(String name) {
        return new EmailAttachment(name, "xml", "<profile>\n" +
                "\t\t\t<id>dgmare-old</id>\n" +
                "\t\t\t<properties>\n" +
                "\t\t\t\t<docker.host>http://localhost:2375</docker.host>\n" +
                "\t\t\t\t<!-- <docker.certPath>C:/Users/inomikos/.docker/machine/machines/default</docker.certPath>\n" +
                "\t\t\t\t-->\n" +
                "\t\t\t\t<arquillian.wildfly.host>localhost</arquillian.wildfly.host>\n" +
                "\t\t\t\t<arquillian.wildfly.port>8080</arquillian.wildfly.port>\n" +
                "\t\t\t\t<arquillian.wildfly.admin.port>9990</arquillian.wildfly.admin.port>\n" +
                "\t\t\t\t<arquillian.wildfly.admin.user>admin</arquillian.wildfly.admin.user>\n" +
                "\t\t\t\t<arquillian.wildfly.admin.pwd>admin</arquillian.wildfly.admin.pwd>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t<jboss.all.user.dependency>user-module.ear</jboss.all.user.dependency>\n" +
                "\t\t\t\t<jboss.all.config.dependency>config-module.ear</jboss.all.config.dependency> \n" +
                "\t\t\t\t\n" +
                "\t\t\t\t<activemq.host>localhost</activemq.host>\n" +
                "\t\t\t</properties>\n" +
                "\t\t</profile>");
    }

    static EmailAttachment createLargeXmlAttachment(String name) {
        return new EmailAttachment(name, "xml",
                "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" \n" +
                        "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\n" +
                        "                      https://maven.apache.org/xsd/settings-1.0.0.xsd\">\n" +
                        "  <!-- <localRepository>/usr/share/maven/ref/repository</localRepository> -->\n" +
                        "  <localRepository>${user.home}/.m2/repository</localRepository>\n" +
                        "    <!-- <interactiveMode>true</interactiveMode>\n" +
                        "  <usePluginRegistry>true</usePluginRegistry>\n" +
                        "  <offline>false</offline> -->\n" +
                        "  <pluginGroups>\n" +
                        "\t\t<pluginGroup>com.github.ferstl</pluginGroup>\n" +
                        "\t\t<pluginGroup>org.wildfly.plugins</pluginGroup>\n" +
                        "\t</pluginGroups> \n" +
                        "  <mirrors>\n" +
                        "\t\t\n" +
                        "\t\t<mirror>\n" +
                        "\t\t\t<id>USMaven</id>\n" +
                        "\t\t\t<mirrorOf>central</mirrorOf>\n" +
                        "\t\t\t<name>US Maven</name>\n" +
                        "\t\t\t<url>http://repo.maven.apache.org/maven2</url>\n" +
                        "\t\t</mirror>\n" +
                        "\n" +
                        "\t\t<mirror>\n" +
                        "\t\t\t<id>DGMARE</id>\n" +
                        "\t\t\t<mirrorOf>DGMARE</mirrorOf>\n" +
                        "\t\t\t<name>DGMARE</name>\n" +
                        "\t\t\t\t<url>http://nexus.focus.fish/nexus/repository/public/</url>\n" +
                        "\t\t</mirror>\n" +
                        "\t</mirrors>\n" +
                        "  <profiles>\n" +
                        "\t<profile>\n" +
                        "      <repositories>\n" +
                        "        <repository>\n" +
                        "          <releases>\n" +
                        "            <enabled>true</enabled>\n" +
                        "            <updatePolicy>never</updatePolicy>\n" +
                        "          </releases>\n" +
                        "          <snapshots>\n" +
                        "            <enabled>false</enabled>\n" +
                        "          </snapshots>\n" +
                        "          <id>focus-repo</id>\n" +
                        "          <name>focus-releases</name>\n" +
                        "          <url>http://nexus.focus.fish/nexus/repository/public/</url>\n" +
                        "        </repository>\n" +
                        "        <repository>\n" +
                        "          <releases>\n" +
                        "            <enabled>false</enabled>\n" +
                        "          </releases>\n" +
                        "          <snapshots />\n" +
                        "          <id>focus-repo-snapshot</id>\n" +
                        "          <name>focus-snapshots</name>\n" +
                        "          <url>http://nexus.focus.fish/nexus/repository/public/</url>\n" +
                        "        </repository>\n" +
                        "      </repositories>\n" +
                        "      <pluginRepositories>\n" +
                        "        <pluginRepository>\n" +
                        "          <releases>\n" +
                        "            <enabled>true</enabled>\n" +
                        "            <updatePolicy>never</updatePolicy>\n" +
                        "          </releases>\n" +
                        "          <snapshots>\n" +
                        "            <enabled>false</enabled>\n" +
                        "          </snapshots>\n" +
                        "          <id>focus-repo</id>\n" +
                        "          <name>focus-plugin-releases</name>\n" +
                        "          <url>http://nexus.focus.fish/nexus/repository/public/</url>\n" +
                        "        </pluginRepository>\n" +
                        "        <pluginRepository>\n" +
                        "          <releases>\n" +
                        "            <enabled>false</enabled>\n" +
                        "          </releases>\n" +
                        "          <snapshots />\n" +
                        "          <id>focus-repo-snapshot</id>\n" +
                        "          <name>focus-plugin-snapshots</name>\n" +
                        "          <url>http://nexus.focus.fish/nexus/repository/public/</url>\n" +
                        "        </pluginRepository>\n" +
                        "      </pluginRepositories>\n" +
                        "      <id>focusrepo</id>\n" +
                        "    </profile>\n" +
                        "\t\t\n" +
                        "\t\t<profile>\n" +
                        "\t\t\t<id>dgmare</id>\n" +
                        "\t\t\t<properties>\n" +
                        "\t\t\t\t<docker.host>http://localhost:2375</docker.host>\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\t<arquillian.wildfly.host>localhost</arquillian.wildfly.host>\n" +
                        "\t\t\t\t<arquillian.wildfly.port>38080</arquillian.wildfly.port>\n" +
                        "\t\t\t\t<arquillian.wildfly.admin.port>39990</arquillian.wildfly.admin.port>\n" +
                        "\t\t\t\t<arquillian.wildfly.admin.user>admin</arquillian.wildfly.admin.user>\n" +
                        "\t\t\t\t<arquillian.wildfly.admin.pwd>Wildfly4ever!</arquillian.wildfly.admin.pwd>\n" +
                        "\t\t\t\t<activemq.host>localhost</activemq.host>\n" +
                        "\t\t\t\t<activemq.port>31616</activemq.port>\n" +
                        "\t\t\t</properties>\n" +
                        "\t\t</profile>\n" +
                        "\t\t<profile>\n" +
                        "\t\t\t<id>dgmare-old</id>\n" +
                        "\t\t\t<properties>\n" +
                        "\t\t\t\t<docker.host>http://localhost:2375</docker.host>\n" +
                        "\t\t\t\t<!-- <docker.certPath>C:/Users/inomikos/.docker/machine/machines/default</docker.certPath>\n" +
                        "\t\t\t\t-->\n" +
                        "\t\t\t\t<arquillian.wildfly.host>localhost</arquillian.wildfly.host>\n" +
                        "\t\t\t\t<arquillian.wildfly.port>8080</arquillian.wildfly.port>\n" +
                        "\t\t\t\t<arquillian.wildfly.admin.port>9990</arquillian.wildfly.admin.port>\n" +
                        "\t\t\t\t<arquillian.wildfly.admin.user>admin</arquillian.wildfly.admin.user>\n" +
                        "\t\t\t\t<arquillian.wildfly.admin.pwd>admin</arquillian.wildfly.admin.pwd>\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\t<jboss.all.user.dependency>user-module.ear</jboss.all.user.dependency>\n" +
                        "\t\t\t\t<jboss.all.config.dependency>config-module.ear</jboss.all.config.dependency> \n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\t<activemq.host>localhost</activemq.host>\n" +
                        "\t\t\t</properties>\n" +
                        "\t\t</profile>\n" +
                        "  </profiles>\n" +
                        "<activeProfiles>\n" +
                        "    <activeProfile>focusrepo</activeProfile>\n" +
                        "\t<!-- <activeProfile>wildfly-local</activeProfile> -->\n" +
                        "\t<activeProfile>dgmare</activeProfile>\n" +
                        "  </activeProfiles>\n" +
                        "</settings>");
    }
}
