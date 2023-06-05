package org.jboss.aerogear.keycloak.metrics;

import java.util.Map;
import java.util.HashMap;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class MetricsEventListener implements EventListenerProvider {

    public final static String ID = "metrics-listener";

    private final static Logger logger = Logger.getLogger(MetricsEventListener.class);
    private RealmProvider realmProvider = null;

    public MetricsEventListener(RealmProvider realmProvider) {
        this.realmProvider = realmProvider;
    }

    private KeycloakSession session;

    public MetricsEventListener (KeycloakSession session) {
        this.session = session;
        this.realmProvider = session.realms();
    }

    @Override
    public void onEvent(Event event) {
        logEventDetails(event);

        switch (event.getType()) {
            case LOGIN:
                PrometheusExporter.instance().recordLogin(event, realmProvider);
                break;
            case CLIENT_LOGIN:
                PrometheusExporter.instance().recordClientLogin(event, realmProvider);
                break;
            case REGISTER:
                PrometheusExporter.instance().recordRegistration(event, realmProvider);
                break;
            case REFRESH_TOKEN:
                PrometheusExporter.instance().recordRefreshToken(event, realmProvider);
                break;
            case CODE_TO_TOKEN:
                PrometheusExporter.instance().recordCodeToToken(event, realmProvider);
                break;
            case REGISTER_ERROR:
                PrometheusExporter.instance().recordRegistrationError(event, realmProvider);
                break;
            case LOGIN_ERROR:
                PrometheusExporter.instance().recordLoginError(event, realmProvider);
                break;
            case CLIENT_LOGIN_ERROR:
                PrometheusExporter.instance().recordClientLoginError(event, realmProvider);
                break;
            case REFRESH_TOKEN_ERROR:
                PrometheusExporter.instance().recordRefreshTokenError(event, realmProvider);
                break;
            case CODE_TO_TOKEN_ERROR:
                PrometheusExporter.instance().recordCodeToTokenError(event, realmProvider);
                break;
            default:
                PrometheusExporter.instance().recordGenericEvent(event, realmProvider);
        }

        setSessions(session.realms().getRealm(event.getRealmId()));

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        logAdminEventDetails(event);

        PrometheusExporter.instance().recordGenericAdminEvent(event, realmProvider);
        setSessions(session.realms().getRealm(event.getRealmId()));
    }

//    private void setSessions(RealmModel realm) {

//         Map<String,Long> onlineSessions = new HashMap<String,Long>();
//         session.sessions().getActiveClientSessionStats(realm,false).forEach((id, count) -> 
//             onlineSessions.put(realm.getClientById(id).getClientId(), count)
//         );

//         Map<String,Long> offlineSessions = new HashMap<String,Long>(); 
//         session.sessions().getActiveClientSessionStats(realm,true).forEach((id, count) -> 
//             offlineSessions.put(realm.getClientById(id).getClientId(), count)
//         );

//         PrometheusExporter.instance().recordSessions(getRealmName(realm.getId()), onlineSessions, offlineSessions);
//     }

private void setSessions(RealmModel realm) {

    Map<String,Long> onlineSessions = new HashMap<String,Long>();
    Map<String,Long> onlineUserSessions = new HashMap<String,Long>();

    // session.sessions().getActiveClientSessionStats(realm,false).forEach((id, count) -> 
      
    //     onlineSessions.put(realm.getClientById(id).getClientId(), count)
    // );

    

    session.sessions().getActiveClientSessionStats(realm,false).forEach((id, count) -> {
        System.output.print("id" + id);
        System.output.print("count" + count);
        Long activeUsercount= session.sessions().getActiveUserSessions(realm,realm.getClientById(id));
        System.output.print("activeUsercount" + activeUsercount);

        onlineSessions.put(realm.getClientById(id).getClientId(), count);

    });
    
    Map<String,Long> offlineSessions = new HashMap<String,Long>(); 
    session.sessions().getActiveClientSessionStats(realm,true).forEach((id, count) -> 
        offlineSessions.put(realm.getClientById(id).getClientId(), count)
    );

    PrometheusExporter.instance().recordSessions(getRealmName(realm.getId()), onlineSessions, offlineSessions);
}


            /**
     * Retrieve the real realm name in the event by id from the RealmProvider.
     *
     * @param realmId Id of Realm
     * @param realmProvider RealmProvider instance
     * @return Realm name
     */
    private String getRealmName(String realmId) {
        RealmModel realm = null;
        if (realmId != null) {
             realm = realmProvider.getRealm(realmId);
        }
        if (realm != null) {
            return realm.getName();
        }
        return null;
    }

    private void logEventDetails(Event event) {
        logger.debugf("Received user event of type %s in realm %s",
                event.getType().name(),
                event.getRealmId());
    }

    private void logAdminEventDetails(AdminEvent event) {
        logger.debugf("Received admin event of type %s (%s) in realm %s",
                event.getOperationType().name(),
                event.getResourceType().name(),
                event.getRealmId());
    }

    @Override
    public void close() {
        // unused
    }
}
