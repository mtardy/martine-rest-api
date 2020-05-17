FROM jboss/wildfly
COPY config/standalone-full.xml /opt/jboss/wildfly/standalone/configuration/
COPY target/api.war /opt/jboss/wildfly/standalone/deployments/
EXPOSE 8080