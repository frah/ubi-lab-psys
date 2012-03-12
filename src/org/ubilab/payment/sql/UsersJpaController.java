/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubilab.payment.sql;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import org.ubilab.payment.sql.exceptions.IllegalOrphanException;
import org.ubilab.payment.sql.exceptions.NonexistentEntityException;

/**
 *
 * @author atsushi-o
 */
public class UsersJpaController implements Serializable {

    public UsersJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Users users) {
        if (users.getHistoryCollection() == null) {
            users.setHistoryCollection(new ArrayList<History>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<History> attachedHistoryCollection = new ArrayList<History>();
            for (History historyCollectionHistoryToAttach : users.getHistoryCollection()) {
                historyCollectionHistoryToAttach = em.getReference(historyCollectionHistoryToAttach.getClass(), historyCollectionHistoryToAttach.getId());
                attachedHistoryCollection.add(historyCollectionHistoryToAttach);
            }
            users.setHistoryCollection(attachedHistoryCollection);
            em.persist(users);
            for (History historyCollectionHistory : users.getHistoryCollection()) {
                Users oldUserIdOfHistoryCollectionHistory = historyCollectionHistory.getUserId();
                historyCollectionHistory.setUserId(users);
                historyCollectionHistory = em.merge(historyCollectionHistory);
                if (oldUserIdOfHistoryCollectionHistory != null) {
                    oldUserIdOfHistoryCollectionHistory.getHistoryCollection().remove(historyCollectionHistory);
                    oldUserIdOfHistoryCollectionHistory = em.merge(oldUserIdOfHistoryCollectionHistory);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Users users) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Users persistentUsers = em.find(Users.class, users.getId());
            Collection<History> historyCollectionOld = persistentUsers.getHistoryCollection();
            Collection<History> historyCollectionNew = users.getHistoryCollection();
            List<String> illegalOrphanMessages = null;
            for (History historyCollectionOldHistory : historyCollectionOld) {
                if (!historyCollectionNew.contains(historyCollectionOldHistory)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain History " + historyCollectionOldHistory + " since its userId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<History> attachedHistoryCollectionNew = new ArrayList<History>();
            for (History historyCollectionNewHistoryToAttach : historyCollectionNew) {
                historyCollectionNewHistoryToAttach = em.getReference(historyCollectionNewHistoryToAttach.getClass(), historyCollectionNewHistoryToAttach.getId());
                attachedHistoryCollectionNew.add(historyCollectionNewHistoryToAttach);
            }
            historyCollectionNew = attachedHistoryCollectionNew;
            users.setHistoryCollection(historyCollectionNew);
            users = em.merge(users);
            for (History historyCollectionNewHistory : historyCollectionNew) {
                if (!historyCollectionOld.contains(historyCollectionNewHistory)) {
                    Users oldUserIdOfHistoryCollectionNewHistory = historyCollectionNewHistory.getUserId();
                    historyCollectionNewHistory.setUserId(users);
                    historyCollectionNewHistory = em.merge(historyCollectionNewHistory);
                    if (oldUserIdOfHistoryCollectionNewHistory != null && !oldUserIdOfHistoryCollectionNewHistory.equals(users)) {
                        oldUserIdOfHistoryCollectionNewHistory.getHistoryCollection().remove(historyCollectionNewHistory);
                        oldUserIdOfHistoryCollectionNewHistory = em.merge(oldUserIdOfHistoryCollectionNewHistory);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = users.getId();
                if (findUsers(id) == null) {
                    throw new NonexistentEntityException("The users with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Users users;
            try {
                users = em.getReference(Users.class, id);
                users.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The users with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<History> historyCollectionOrphanCheck = users.getHistoryCollection();
            for (History historyCollectionOrphanCheckHistory : historyCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Users (" + users + ") cannot be destroyed since the History " + historyCollectionOrphanCheckHistory + " in its historyCollection field has a non-nullable userId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(users);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Users> findUsersEntities() {
        return findUsersEntities(true, -1, -1);
    }

    public List<Users> findUsersEntities(int maxResults, int firstResult) {
        return findUsersEntities(false, maxResults, firstResult);
    }

    private List<Users> findUsersEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Users.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Users findUsers(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Users.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsersCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Users> rt = cq.from(Users.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
