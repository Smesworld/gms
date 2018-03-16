package com.gms.repository.security.authorization.dao;

import com.gms.domain.security.role.BRole;
import com.gms.service.db.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQLBAuthorizationDAOImpl (default implementation of {@link BAuthorizationDAO}.
 *
 * @author Asiel Leal Celdeiro | lealceldeiro@gmail.com
 * @version 0.1
 * Mar 16, 2018
 */
@RequiredArgsConstructor
@Service("PostgreSQLBAuthorizationDAOImpl")
@Transactional
public class BAuthorizationDAOImpl extends BAuthorizationDAOImplBase implements BAuthorizationDAO {

    private final QueryService queryService;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<BRole>> getRolesForUserOverAllEntities(long userId) {
        Map<String, List<BRole>> r = new HashMap<>();
        final Query queryOE = queryService.createNativeQuery("SELECT e.username FROM eowned_entity e INNER JOIN" +
                " bauthorization auth ON e.id = auth.entity_id WHERE auth.user_id = :userId");
        queryOE.setParameter(USER_ID_PARAM, userId);
        List<String> oEntitiesUsernames = queryOE.getResultList();

        Query queryR;
        List<Object[]> result;
        List<BRole> rl;
        for (String OEUsername : oEntitiesUsernames) {
            queryR = queryService.createNativeQuery("SELECT r.id, r.version, r.label, r.description, r.enabled" +
                    " FROM brole r INNER JOIN bauthorization auth ON r.id = auth.role_id INNER JOIN eowned_entity e ON" +
                    " auth.entity_id = e.id WHERE e.username = :eUsername AND auth.user_id = :userId");
            queryR.setParameter("eUsername", OEUsername).setParameter(USER_ID_PARAM, userId);
            result = queryR.getResultList();
            rl = new LinkedList<>();
            processRolesVal(result, rl);
            r.put(OEUsername, rl);
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BRole> getRolesForUserOverEntity(long userId, long entityId) {
        Query queryR = queryService.createNativeQuery("SELECT r.id, r.version, r.label, r.description, r.enabled" +
                " FROM brole r INNER JOIN bauthorization auth ON r.id = auth.role_id INNER JOIN eowned_entity e ON" +
                " auth.entity_id = e.id WHERE e.id = :entityId AND auth.user_id = :userId");
        queryR.setParameter("entityId", entityId).setParameter(USER_ID_PARAM, userId);
        List<Object[]> rawVal = queryR.getResultList();
        List<BRole> r = new LinkedList<>();

        processRolesVal(rawVal, r);

        return r;
    }
}
