package com.salesforce.dva.argus.entity;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

/**
 * The entity encapsulates information about the infraction. There are no uniqueness constraints in this entity.
 *
 * <p>
 * Fields that cannot be null are:
 * </p>
 *
 * <ul>
 * <li>POLICY_ID</li>
 * <li>USER_ID</li>
 * <li>INFRACTION_TIMESTAMP</li>
 * <li>EXPIRATION_TIMESTAMP</li>
 * </ul>
 *
 * @author Ruofan Zhang (rzhang@salesforce.com)
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "INFRACTION")
@NamedQueries({
    @NamedQuery(
            name = "Infraction.findByPolicyAndUsername",
            query = "SELECT r FROM Infraction r, PrincipalUser u WHERE r.policy.id = :policyId AND r.user = u AND u.userName = :userName"),
    @NamedQuery(
            name = "Infraction.findByPolicyAndInfraction",
            query = "SELECT r FROM Infraction r WHERE r.policy = :policy and r.id = :id"
    ),
    @NamedQuery(
            name = "Infraction.findByPolicy",
            query = "SELECT r FROM Infraction r WHERE r.policy.id = :policyId"
    ),
    @NamedQuery(
            name = "Infraction.findByUsername",
            query = "SELECT r FROM Infraction r WHERE r.user.userName = :userName"
    ),
    @NamedQuery(
            name = "Infraction.findSuspensionsByUsername",
            query = "SELECT r FROM Infraction r WHERE r.user.userName = :userName AND r.expirationTimestamp IS NOT NULL"
    ),
    @NamedQuery(
            name = "Infraction.deleteExpired",
            query = "DELETE FROM Infraction r WHERE r.infractionTimestamp < :time"
    )
})

public class Infraction extends JPAEntity {

    private static final long MONTH_IN_MILLIS = 30*24*3600*1000;

    /**
     * Returns all suspensions for the specified user.
     * 
     * @param em The entity manager.  Cannot be null.
     * @param userName The userName to match.  Cannot be null or empty.
     * @return The list of matching suspensions.
     */
    public static List<Infraction> findSuspensionsByUserName(EntityManager em, String userName) {
        requireArgument(em != null, "Entity manager can not be null.");
        requireArgument(userName != null && !userName.isEmpty(), "Username cannot be null or empty.");
        TypedQuery<Infraction> query = em.createNamedQuery("Infraction.findSuspensionsByUsername", Infraction.class);
        query.setHint("javax.persistence.cache.storeMode", "REFRESH");
        return query.getResultList();
    }

    public static void deleteExpired(EntityManager em) {
        requireArgument(em != null, "Entity manager can not be null.");
        Query query = em.createNamedQuery("Infraction.deleteExpired");
        query.setParameter("time", System.currentTimeMillis()-MONTH_IN_MILLIS);
        query.executeUpdate();
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private PrincipalUser user;

    @Basic(optional = false)
    @Column(name = "infraction_timestamp", nullable = false)
    private Long infractionTimestamp;

    @Column(name = "expiration_timestamp")
    private Long expirationTimestamp = null;

    private Double value = null;

    // ~ Constructors
    // *********************************************************************************************************************************
    /**
     * Creates a new Infraction object.
     *
     * @param creator The creator of this infraction. Cannot be null.
     * @param policy The policy associated with this infraction. Cannot be null.
     * @param user The user name associated with this infraction. Cannot be null.
     * @param infractionTimestamp The infraction timestamp of this infraction. Cannot be null.
     * @param expirationTimestamp The expiration timestamp of this infraction.  If -1 then the suspension is indefinite.  If null, the infraction
     * doesn't correspond to a suspension.
     */
    public Infraction(PrincipalUser creator, Policy policy, PrincipalUser user, Long infractionTimestamp, Long expirationTimestamp, Double value) {
        super(creator);
        setPolicy(policy);
        setUser(user);
        setInfractionTimestamp(infractionTimestamp);
        setExpirationTimestamp(expirationTimestamp);
        setValue(value);
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Creates a new Infraction object.
     */
    protected Infraction() {
        super(null);
    }

    // ~ Methods
    // **************************************************************************************************************************************
    /**
     * Finds the number of user infractions for a policy-user combination that have occurred since the given start time and end time.
     *
     * @param em The entity manager to use. Cannot be null.
     * @param userName The user name. Cannot be null.
     * @param policy The policy. Cannot be null.
     * @param startTime The start time threshold.
     *
     * @return The number of infractions.
     */
    public static int findInfractionCount(EntityManager em, Policy policy, String userName, long startTime, long endTime) {

        List<Infraction> records = findByPolicyAndUserName(em, policy.getId(), userName);

        int count = 0;

        for (Infraction record : records) {
            Long timestamp = record.getInfractionTimestamp();

            if (timestamp > startTime && timestamp < endTime) {
                count++;
            }

        }
        return count;
    }

    /**
     * Finds infractions given its policy.
     *
     * @param em The entity manager to use. Cannot be null.
     * @param policyId The policy ID associated with the infractions. Cannot be null.
     *
     * @return The corresponding infractions or null if no infractions having the specified policy exist.
     */
    public static List<Infraction> findByPolicy(EntityManager em, BigInteger policyId) {
        requireArgument(em != null, "Entity manager can not be null.");
        requireArgument(policyId != null, "Policy ID cannot be null");

        TypedQuery<Infraction> query = em.createNamedQuery("Infraction.findByPolicy", Infraction.class);

        try {
            query.setParameter("policyId", policyId);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getResultList();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Finds infractions given its user.
     *
     * @param em The entity manager to use. Cannot be null.
     * @param userName	The userName associated with the infractions. Cannot be null or empty.
     *
     * @return The corresponding infractions or null if no infractions having the specified policy exist.
     */
    public static List<Infraction> findByUserName(EntityManager em, String userName) {
        requireArgument(em != null, "Entity manager can not be null.");
        requireArgument(userName != null && !userName.isEmpty(), "Username cannot be null or empty.");

        TypedQuery<Infraction> query = em.createNamedQuery("Infraction.findByUsername", Infraction.class);

        try {
            query.setParameter("userName", userName);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getResultList();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Finds an infraction given its policy and infraction id.
     *
     * @param em The entity manager to use. Cannot be null.
     * @param policy The policy associated with this suspension level. Cannot be null.
     * @param infractionId	The infraction id. Cannot be null.
     *
     * @return The corresponding infraction or null if no infraction having the specified policy and infraction id exist.
     */
    public static Infraction findByPolicyAndInfraction(EntityManager em, Policy policy, BigInteger infractionId) {
        requireArgument(em != null, "Entity manager can not be null.");
        requireArgument(policy != null, "Policy cannot be null");
        requireArgument(infractionId.signum() == 1, "Infraction id must be greater than zero.");

        TypedQuery<Infraction> query = em.createNamedQuery("Infraction.findByPolicyAndInfraction", Infraction.class);

        try {
            query.setParameter("policy", policy);
            query.setParameter("id", infractionId);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Find the infractions for a given user-policy combination.
     *
     * @param em The EntityManager to use.
     * @param userName The userName for which to retrieve record.
     * @param policyId The policy for which to retrieve record.
     *
     * @return The infractions for the given user-policy combination. Null if no such record exists.
     */
    public static List<Infraction> findByPolicyAndUserName(EntityManager em, BigInteger policyId, String userName) {
        TypedQuery<Infraction> query = em.createNamedQuery("Infraction.findByPolicyAndUsername", Infraction.class);

        try {
            query.setParameter("userName", userName);
            query.setParameter("policyId", policyId);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getResultList();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Indicates whether a suspension is active.
     *
     * @return True if a suspension is active.
     */
    public boolean isSuspended() {
        return (System.currentTimeMillis() < getExpirationTimestamp()) || isSuspendedIndefinitely();
    }

    /**
     * Indicates if the user is suspended indefinitely.
     *
     * @return True if the user is suspended indefinitely.
     */
    public boolean isSuspendedIndefinitely() {
        return expirationTimestamp == -1L;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public PrincipalUser getUser() {
        return user;
    }

    public void setUser(PrincipalUser user) {
        this.user = user;
    }

    public Long getInfractionTimestamp() {
        return infractionTimestamp;
    }

    public void setInfractionTimestamp(Long infractionTimestamp) {
        this.infractionTimestamp = infractionTimestamp;
    }

    public Long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(Long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 29 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Notification other = (Notification) obj;

        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Infraction{" + "policy=" + policy + ", user=" + user + ", infractionTimestamp=" + infractionTimestamp + ", expirationTimestamp="
                + expirationTimestamp + '}';
    }

}
