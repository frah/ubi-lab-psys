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
import org.ubilab.payment.sql.exceptions.IllegalOrphanException;
import org.ubilab.payment.sql.exceptions.NonexistentEntityException;
import org.ubilab.payment.sql.exceptions.PreexistingEntityException;

/**
 *
 * @author atsushi-o
 */
public class PurseJpaController implements Serializable {

    public PurseJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Purse purse) throws IllegalOrphanException, PreexistingEntityException, Exception {
        List<String> illegalOrphanMessages = null;
        Users usersOrphanCheck = purse.getUsers();
        if (usersOrphanCheck != null) {
            Purse oldPurseOfUsers = usersOrphanCheck.getPurse();
            if (oldPurseOfUsers != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Users " + usersOrphanCheck + " already has an item of type Purse whose users column cannot be null. Please make another selection for the users field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Users users = purse.getUsers();
            if (users != null) {
                users = em.getReference(users.getClass(), users.getId());
                purse.setUsers(users);
            }
            em.persist(purse);
            if (users != null) {
                users.setPurse(purse);
                users = em.merge(users);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPurse(purse.getUid()) != null) {
                throw new PreexistingEntityException("Purse " + purse + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Purse purse) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Purse persistentPurse = em.find(Purse.class, purse.getUid());
            Users usersOld = persistentPurse.getUsers();
            Users usersNew = purse.getUsers();
            List<String> illegalOrphanMessages = null;
            if (usersNew != null && !usersNew.equals(usersOld)) {
                Purse oldPurseOfUsers = usersNew.getPurse();
                if (oldPurseOfUsers != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Users " + usersNew + " already has an item of type Purse whose users column cannot be null. Please make another selection for the users field.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (usersNew != null) {
                usersNew = em.getReference(usersNew.getClass(), usersNew.getId());
                purse.setUsers(usersNew);
            }
            purse = em.merge(purse);
            if (usersOld != null && !usersOld.equals(usersNew)) {
                usersOld.setPurse(null);
                usersOld = em.merge(usersOld);
            }
            if (usersNew != null && !usersNew.equals(usersOld)) {
                usersNew.setPurse(purse);
                usersNew = em.merge(usersNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = purse.getUid();
                if (findPurse(id) == null) {
                    throw new NonexistentEntityException("The purse with id " + id + " no longer exists.");
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
            Purse purse;
            try {
                purse = em.getReference(Purse.class, id);
                purse.getUid();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The purse with id " + id + " no longer exists.", enfe);
            }
            Users users = purse.getUsers();
            if (users != null) {
                users.setPurse(null);
                users = em.merge(users);
            }
            em.remove(purse);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Purse> findPurseEntities() {
        return findPurseEntities(true, -1, -1);
    }

    public List<Purse> findPurseEntities(int maxResults, int firstResult) {
        return findPurseEntities(false, maxResults, firstResult);
    }

    private List<Purse> findPurseEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Purse.class));
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

    public Purse findPurse(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Purse.class, id);
        } finally {
            em.close();
        }
    }

    public int getPurseCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Purse> rt = cq.from(Purse.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
