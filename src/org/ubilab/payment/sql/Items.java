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
@Table(name = "items", catalog = "ubilab_pos", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "JAN"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Items.findAll", query = "SELECT i FROM Items i"),
    @NamedQuery(name = "Items.findById", query = "SELECT i FROM Items i WHERE i.id = :id"),
    @NamedQuery(name = "Items.findByName", query = "SELECT i FROM Items i WHERE i.name = :name"),
    @NamedQuery(name = "Items.findByJan", query = "SELECT i FROM Items i WHERE i.jan = :jan"),
    @NamedQuery(name = "Items.findByPrice", query = "SELECT i FROM Items i WHERE i.price = :price"),
    @NamedQuery(name = "Items.findByNum", query = "SELECT i FROM Items i WHERE i.num = :num")})
public class Items implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Basic(optional = false)
    @Column(name = "JAN", nullable = false, length = 13)
    private String jan;
    @Basic(optional = false)
    @Column(name = "price", nullable = false)
    private int price;
    @Basic(optional = false)
    @Column(name = "num", nullable = false)
    private int num;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "itemId")
    private Collection<History> historyCollection;

    public Items() {
    }

    public Items(Integer id) {
        this.id = id;
    }

    public Items(Integer id, String name, String jan, int price, int num) {
        this.id = id;
        this.name = name;
        this.jan = jan;
        this.price = price;
        this.num = num;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJan() {
        return jan;
    }

    public void setJan(String jan) {
        this.jan = jan;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
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
        if (!(object instanceof Items)) {
            return false;
        }
        Items other = (Items) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.ubilab.payment.sql.Items[ id=" + id + " ]";
    }

}
