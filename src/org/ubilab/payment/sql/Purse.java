/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubilab.payment.sql;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author atsushi-o
 */
@Entity
@Table(name = "purse", catalog = "ubilab_pos", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Purse.findAll", query = "SELECT p FROM Purse p"),
    @NamedQuery(name = "Purse.findByUid", query = "SELECT p FROM Purse p WHERE p.uid = :uid"),
    @NamedQuery(name = "Purse.findByRemainder", query = "SELECT p FROM Purse p WHERE p.remainder = :remainder")})
public class Purse implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", nullable = false)
    private Integer uid;
    @Basic(optional = false)
    @Column(name = "remainder", nullable = false)
    private int remainder;
    @JoinColumn(name = "uid", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Users users;

    public Purse() {
    }

    public Purse(Integer uid) {
        this.uid = uid;
    }

    public Purse(Integer uid, int remainder) {
        this.uid = uid;
        this.remainder = remainder;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public int getRemainder() {
        return remainder;
    }

    public void setRemainder(int remainder) {
        this.remainder = remainder;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uid != null ? uid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Purse)) {
            return false;
        }
        Purse other = (Purse) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ubilab.payment.sql.Purse[ uid=" + uid + " ]";
    }

}
