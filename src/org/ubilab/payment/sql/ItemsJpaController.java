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
public class ItemsJpaController implements Serializable {

    public ItemsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Items items) {
        if (items.getHistoryCollection() == null) {
            items.setHistoryCollection(new ArrayList<History>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<History> attachedHistoryCollection = new ArrayList<History>();
            for (History historyCollectionHistoryToAttach : items.getHistoryCollection()) {
                historyCollectionHistoryToAttach = em.getReference(historyCollectionHistoryToAttach.getClass(), historyCollectionHistoryToAttach.getId());
                attachedHistoryCollection.add(historyCollectionHistoryToAttach);
            }
            items.setHistoryCollection(attachedHistoryCollection);
            em.persist(items);
            for (History historyCollectionHistory : items.getHistoryCollection()) {
                Items oldItemIdOfHistoryCollectionHistory = historyCollectionHistory.getItemId();
                historyCollectionHistory.setItemId(items);
                historyCollectionHistory = em.merge(historyCollectionHistory);
                if (oldItemIdOfHistoryCollectionHistory != null) {
                    oldItemIdOfHistoryCollectionHistory.getHistoryCollection().remove(historyCollectionHistory);
                    oldItemIdOfHistoryCollectionHistory = em.merge(oldItemIdOfHistoryCollectionHistory);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Items items) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Items persistentItems = em.find(Items.class, items.getId());
            Collection<History> historyCollectionOld = persistentItems.getHistoryCollection();
            Collection<History> historyCollectionNew = items.getHistoryCollection();
            List<String> illegalOrphanMessages = null;
            for (History historyCollectionOldHistory : historyCollectionOld) {
                if (!historyCollectionNew.contains(historyCollectionOldHistory)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain History " + historyCollectionOldHistory + " since its itemId field is not nullable.");
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
            items.setHistoryCollection(historyCollectionNew);
            items = em.merge(items);
            for (History historyCollectionNewHistory : historyCollectionNew) {
                if (!historyCollectionOld.contains(historyCollectionNewHistory)) {
                    Items oldItemIdOfHistoryCollectionNewHistory = historyCollectionNewHistory.getItemId();
                    historyCollectionNewHistory.setItemId(items);
                    historyCollectionNewHistory = em.merge(historyCollectionNewHistory);
                    if (oldItemIdOfHistoryCollectionNewHistory != null && !oldItemIdOfHistoryCollectionNewHistory.equals(items)) {
                        oldItemIdOfHistoryCollectionNewHistory.getHistoryCollection().remove(historyCollectionNewHistory);
                        oldItemIdOfHistoryCollectionNewHistory = em.merge(oldItemIdOfHistoryCollectionNewHistory);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = items.getId();
                if (findItems(id) == null) {
                    throw new NonexistentEntityException("The items with id " + id + " no longer exists.");
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
            Items items;
            try {
                items = em.getReference(Items.class, id);
                items.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The items with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<History> historyCollectionOrphanCheck = items.getHistoryCollection();
            for (History historyCollectionOrphanCheckHistory : historyCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Items (" + items + ") cannot be destroyed since the History " + historyCollectionOrphanCheckHistory + " in its historyCollection field has a non-nullable itemId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(items);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Items> findItemsEntities() {
        return findItemsEntities(true, -1, -1);
    }

    public List<Items> findItemsEntities(int maxResults, int firstResult) {
        return findItemsEntities(false, maxResults, firstResult);
    }

    private List<Items> findItemsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Items.class));
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

    public Items findItems(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Items.class, id);
        } finally {
            em.close();
        }
    }

    public int getItemsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Items> rt = cq.from(Items.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
