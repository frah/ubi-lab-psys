/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubilab.payment.sql;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author atsushi-o
 */
@Entity
@Table(name = "users", catalog = "ubilab_pos", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"IDm", "mail"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
    @NamedQuery(name = "Users.findById", query = "SELECT u FROM Users u WHERE u.id = :id"),
    @NamedQuery(name = "Users.findByIDm", query = "SELECT u FROM Users u WHERE u.iDm = :iDm"),
    @NamedQuery(name = "Users.findByName", query = "SELECT u FROM Users u WHERE u.name = :name"),
    @NamedQuery(name = "Users.findByMail", query = "SELECT u FROM Users u WHERE u.mail = :mail"),
    @NamedQuery(name = "Users.findByTwitter", query = "SELECT u FROM Users u WHERE u.twitter = :twitter"),
    @NamedQuery(name = "Users.findBySkinFqcn", query = "SELECT u FROM Users u WHERE u.skinFqcn = :skinFqcn"),
    @NamedQuery(name = "Users.findByFlags", query = "SELECT u FROM Users u WHERE u.flags = :flags"),
    @NamedQuery(name = "Users.findByRemainder", query = "SELECT u FROM Users u WHERE u.remainder = :remainder")})
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "IDm", nullable = false, length = 16)
    private String iDm;
    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Basic(optional = false)
    @Column(name = "mail", nullable = false, length = 255)
    private String mail;
    @Basic(optional = false)
    @Lob
    @Column(name = "password", nullable = false)
    private byte[] password;
    @Column(name = "twitter", length = 255)
    private String twitter;
    @Basic(optional = false)
    @Column(name = "skin_fqcn", nullable = false, length = 255)
    private String skinFqcn;
    @Basic(optional = false)
    @Column(name = "flags", nullable = false)
    private boolean flags;
    @Basic(optional = false)
    @Column(name = "remainder", nullable = false)
    private int remainder;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "uid")
    private Collection<History> historyCollection;

    public Users() {
    }

    public Users(Integer id) {
        this.id = id;
    }

    public Users(Integer id, String iDm, String name, String mail, byte[] password, String skinFqcn, boolean flags, int remainder) {
        this.id = id;
        this.iDm = iDm;
        this.name = name;
        this.mail = mail;
        this.password = password;
        this.skinFqcn = skinFqcn;
        this.flags = flags;
        this.remainder = remainder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIDm() {
        return iDm;
    }

    public void setIDm(String iDm) {
        this.iDm = iDm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getSkinFqcn() {
        return skinFqcn;
    }

    public void setSkinFqcn(String skinFqcn) {
        this.skinFqcn = skinFqcn;
    }

    public boolean getFlags() {
        return flags;
    }

    public void setFlags(boolean flags) {
        this.flags = flags;
    }

    public int getRemainder() {
        return remainder;
    }

    public void setRemainder(int remainder) {
        this.remainder = remainder;
    }

    @XmlTransient
    public Collection<History> getHistoryCollection() {
        return historyCollection;
    }

    public void setHistoryCollection(Collection<History> historyCollection) {
        this.historyCollection = historyCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ubilab.payment.sql.Users[ id=" + id + " ]";
    }

}
