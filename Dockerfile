FROM jboss/wildfly
COPY target/api.war /opt/jboss/wildfly/standalone/deployments/
EXPOSE 8080