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
import org.ubilab.payment.sql.exceptions.NonexistentEntityException;

/**
 *
 * @author atsushi-o
 */
public class HistoryJpaController implements Serializable {

    public HistoryJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(History history) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Items itemId = history.getItemId();
            if (itemId != null) {
                itemId = em.getReference(itemId.getClass(), itemId.getId());
                history.setItemId(itemId);
            }
            Users uid = history.getUid();
            if (uid != null) {
                uid = em.getReference(uid.getClass(), uid.getId());
                history.setUid(uid);
            }
            em.persist(history);
            if (itemId != null) {
                itemId.getHistoryCollection().add(history);
                itemId = em.merge(itemId);
            }
            if (uid != null) {
                uid.getHistoryCollection().add(history);
                uid = em.merge(uid);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(History history) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            History persistentHistory = em.find(History.class, history.getId());
            Items itemIdOld = persistentHistory.getItemId();
            Items itemIdNew = history.getItemId();
            Users uidOld = persistentHistory.getUid();
            Users uidNew = history.getUid();
            if (itemIdNew != null) {
                itemIdNew = em.getReference(itemIdNew.getClass(), itemIdNew.getId());
                history.setItemId(itemIdNew);
            }
            if (uidNew != null) {
                uidNew = em.getReference(uidNew.getClass(), uidNew.getId());
                history.setUid(uidNew);
            }
            history = em.merge(history);
            if (itemIdOld != null && !itemIdOld.equals(itemIdNew)) {
                itemIdOld.getHistoryCollection().remove(history);
                itemIdOld = em.merge(itemIdOld);
            }
            if (itemIdNew != null && !itemIdNew.equals(itemIdOld)) {
                itemIdNew.getHistoryCollection().add(history);
                itemIdNew = em.merge(itemIdNew);
            }
            if (uidOld != null && !uidOld.equals(uidNew)) {
                uidOld.getHistoryCollection().remove(history);
                uidOld = em.merge(uidOld);
            }
            if (uidNew != null && !uidNew.equals(uidOld)) {
                uidNew.getHistoryCollection().add(history);
                uidNew = em.merge(uidNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = history.getId();
                if (findHistory(id) == null) {
                    throw new NonexistentEntityException("The history with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            History history;
            try {
                history = em.getReference(History.class, id);
                history.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The history with id " + id + " no longer exists.", enfe);
            }
            Items itemId = history.getItemId();
            if (itemId != null) {
                itemId.getHistoryCollection().remove(history);
                itemId = em.merge(itemId);
            }
            Users uid = history.getUid();
            if (uid != null) {
                uid.getHistoryCollection().remove(history);
                uid = em.merge(uid);
            }
            em.remove(history);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<History> findHistoryEntities() {
        return findHistoryEntities(true, -1, -1);
    }

    public List<History> findHistoryEntities(int maxResults, int firstResult) {
        return findHistoryEntities(false, maxResults, firstResult);
    }

    private List<History> findHistoryEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(History.class));
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

    public History findHistory(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(History.class, id);
        } finally {
            em.close();
        }
    }

    public int getHistoryCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<History> rt = cq.from(History.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
