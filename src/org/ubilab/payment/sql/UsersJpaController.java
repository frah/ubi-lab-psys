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
            Purse purse = users.getPurse();
            if (purse != null) {
                purse = em.getReference(purse.getClass(), purse.getUid());
                users.setPurse(purse);
            }
            Collection<History> attachedHistoryCollection = new ArrayList<History>();
            for (History historyCollectionHistoryToAttach : users.getHistoryCollection()) {
                historyCollectionHistoryToAttach = em.getReference(historyCollectionHistoryToAttach.getClass(), historyCollectionHistoryToAttach.getId());
                attachedHistoryCollection.add(historyCollectionHistoryToAttach);
            }
            users.setHistoryCollection(attachedHistoryCollection);
            em.persist(users);
            if (purse != null) {
                Users oldUsersOfPurse = purse.getUsers();
                if (oldUsersOfPurse != null) {
                    oldUsersOfPurse.setPurse(null);
                    oldUsersOfPurse = em.merge(oldUsersOfPurse);
                }
                purse.setUsers(users);
                purse = em.merge(purse);
            }
            for (History historyCollectionHistory : users.getHistoryCollection()) {
                Users oldUidOfHistoryCollectionHistory = historyCollectionHistory.getUid();
                historyCollectionHistory.setUid(users);
                historyCollectionHistory = em.merge(historyCollectionHistory);
                if (oldUidOfHistoryCollectionHistory != null) {
                    oldUidOfHistoryCollectionHistory.getHistoryCollection().remove(historyCollectionHistory);
                    oldUidOfHistoryCollectionHistory = em.merge(oldUidOfHistoryCollectionHistory);
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
            Purse purseOld = persistentUsers.getPurse();
            Purse purseNew = users.getPurse();
            Collection<History> historyCollectionOld = persistentUsers.getHistoryCollection();
            Collection<History> historyCollectionNew = users.getHistoryCollection();
            List<String> illegalOrphanMessages = null;
            if (purseOld != null && !purseOld.equals(purseNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Purse " + purseOld + " since its users field is not nullable.");
            }
            for (History historyCollectionOldHistory : historyCollectionOld) {
                if (!historyCollectionNew.contains(historyCollectionOldHistory)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain History " + historyCollectionOldHistory + " since its uid field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (purseNew != null) {
                purseNew = em.getReference(purseNew.getClass(), purseNew.getUid());
                users.setPurse(purseNew);
            }
            Collection<History> attachedHistoryCollectionNew = new ArrayList<History>();
            for (History historyCollectionNewHistoryToAttach : historyCollectionNew) {
                historyCollectionNewHistoryToAttach = em.getReference(historyCollectionNewHistoryToAttach.getClass(), historyCollectionNewHistoryToAttach.getId());
                attachedHistoryCollectionNew.add(historyCollectionNewHistoryToAttach);
            }
            historyCollectionNew = attachedHistoryCollectionNew;
            users.setHistoryCollection(historyCollectionNew);
            users = em.merge(users);
            if (purseNew != null && !purseNew.equals(purseOld)) {
                Users oldUsersOfPurse = purseNew.getUsers();
                if (oldUsersOfPurse != null) {
                    oldUsersOfPurse.setPurse(null);
                    oldUsersOfPurse = em.merge(oldUsersOfPurse);
                }
                purseNew.setUsers(users);
                purseNew = em.merge(purseNew);
            }
            for (History historyCollectionNewHistory : historyCollectionNew) {
                if (!historyCollectionOld.contains(historyCollectionNewHistory)) {
                    Users oldUidOfHistoryCollectionNewHistory = historyCollectionNewHistory.getUid();
                    historyCollectionNewHistory.setUid(users);
                    historyCollectionNewHistory = em.merge(historyCollectionNewHistory);
                    if (oldUidOfHistoryCollectionNewHistory != null && !oldUidOfHistoryCollectionNewHistory.equals(users)) {
                        oldUidOfHistoryCollectionNewHistory.getHistoryCollection().remove(historyCollectionNewHistory);
                        oldUidOfHistoryCollectionNewHistory = em.merge(oldUidOfHistoryCollectionNewHistory);
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
            Purse purseOrphanCheck = users.getPurse();
            if (purseOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Users (" + users + ") cannot be destroyed since the Purse " + purseOrphanCheck + " in its purse field has a non-nullable users field.");
            }
            Collection<History> historyCollectionOrphanCheck = users.getHistoryCollection();
            for (History historyCollectionOrphanCheckHistory : historyCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Users (" + users + ") cannot be destroyed since the History " + historyCollectionOrphanCheckHistory + " in its historyCollection field has a non-nullable uid field.");
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
