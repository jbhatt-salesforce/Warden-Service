package com.salesforce.dva.argus.entity;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;

/**
 * The entity encapsulates information about the suspension levels for a given policy.
 *
 * <p>Fields that determine uniqueness are:</p>
 *
 * <ul>
 *   <li>ID</li>
 * </ul>
 *
 * <p>Fields that cannot be null are:</p>
 *
 * <ul>
 *   <li>ID</li>
 *   <li>POLICY_ID</li>
 *   <li>LEVEL_NUMBER</li>
 *   <li>INFRACTION_COUNT</li>
 *   <li>SUSPENSION_TIME</li>
 * </ul>
 *
 * @author  Ruofan Zhang(rzhang@salesforce.com)
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "SUSPENSION_LEVEL", uniqueConstraints = @UniqueConstraint(columnNames = { "policy_id", "level_number" }))
@NamedQueries(
    {
        @NamedQuery(
            name = "SuspensionLevel.findAllByPolicy", 
            query = "SELECT r FROM SuspensionLevel r WHERE r.policy = :policy"
        ),
        @NamedQuery(
                name = "SuspensionLevel.findByPolicyAndLevel", 
                query = "SELECT r FROM SuspensionLevel r WHERE r.policy = :policy and r.levelNumber = :levelNumber"
        ),
        @NamedQuery(
                name = "SuspensionLevel.findInfractionCountByPolicyAndLevel", 
                query = "SELECT r.infractionCount FROM SuspensionLevel r WHERE r.policy = :policy and r.levelNumber = :levelNumber"
        ),
        @NamedQuery(
                name = "SuspensionLevel.findSuspensionTimeByPolicyAndLevel", 
                query = "SELECT r.suspensionTime FROM SuspensionLevel r WHERE r.policy = :policy and r.levelNumber = :levelNumber"
        ),
        @NamedQuery(
                name = "SuspensionLevel.findSuspensionTimeByPolicyAndInfractionCount", 
                query = "SELECT r.suspensionTime FROM SuspensionLevel r WHERE r.policy = :policy and r.infractionCount = :infractionCount"
        )
    }
)
public class SuspensionLevel extends JPAEntity {
	//~ Instance fields ******************************************************************************************************************************

    @ManyToOne(targetEntity = Policy.class, fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;    
    
    @Basic(optional = false)
    @Column(name = "level_number", nullable = false)
    private int levelNumber;
    
    @Basic(optional = false)
    @Column(name = "infraction_count", nullable = false)
    private int infractionCount;
    
    @Column(name = "suspension_time", nullable = false)
    @Basic(optional = false)
    private long suspensionTime;   
    
    
    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new SubsystemSuspensionLevels object.
     *
     * @param  creator         The creator of this suspension level.
     * @param  policyId        The policy for which to set suspension levels. Cannot be null.
     * @param  levelNumber     The level number for this suspension level. Cannot be null.
     * @param  infractionCount The infraction count for this suspension level. Cannot be null.
     * @param  suspensionTime  The suspension time for this suspension level. Cannot be null.
     */
    public SuspensionLevel(PrincipalUser creator, BigInteger policyId, int levelNumber, int infractionCount, Long suspensionTime) {
    	super(creator);
    	setPolicy(policy);
        setLevelNumber(levelNumber);
        setInfractionCount(infractionCount);
        setSuspensionTime(suspensionTime);
    }
    /**
     * Creates a new SubsystemSuspensionLevel object.     
     */
    protected SuspensionLevel(){
    	super(null);
    }
  //~ Methods **************************************************************************************************************************************
    /**
     * Finds an suspension time given its name and owner.
     *
     * @param   em        			The entity manager to use. Cannot be null.
     * @param   policyId  			The policy id associated with this suspension level. Cannot be null.
     * @param   infractionCount		The infraction count associated with this suspension level. Cannot be null.
     *
     * @return  The corresponding suspension time or null if no suspension level having the specified policy and infraction count exist.
     */
    public static SuspensionLevel findByPolicyAndLevel(EntityManager em, BigInteger policyId, int level) {
        requireArgument(em != null, "Entity manager can not be null.");
        requireArgument(policyId != null , "Policy id cannot be null");
        requireArgument(level > 0, "Level must be greater than zero.");

        TypedQuery<SuspensionLevel> query = em.createNamedQuery("SuspensionLevel.findByPolicyAndLevel", SuspensionLevel.class);

        
        try {
            query.setParameter("policyId", policyId);
            query.setParameter("level", level);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    /**
     * Finds an suspension time given its policy and infraction count.
     *
     * @param   em        			The entity manager to use. Cannot be null.
     * @param   policyId  			The policy id associated with this suspension level. Cannot be null.
     * @param   infractionCount		The infraction count associated with this suspension level. Cannot be null.
     *
     * @return  The corresponding suspension time or null if no suspension level having the specified policy and infraction count exist.
     */
    public static Long findSuspensionTimeByPolicyAndInfractionCount(EntityManager em, Long policyId, int infractionCount) {
        requireArgument(em != null, "Entity manager can not be null.");
        requireArgument(policyId != null , "Policy id cannot be null");
        requireArgument(infractionCount >=0, "infraction count cannot be negative.");

        TypedQuery<SuspensionLevel> query = em.createNamedQuery("SuspensionLevel.findSuspensionTimeByPolicyAndInfractionCount", SuspensionLevel.class);

        
        try {
            query.setParameter("policyId", policyId);
            query.setParameter("infractionCount", infractionCount);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getSingleResult().getSuspensionTime();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    /**
     * Finds all suspension levels for the given policy.
     *
     * @param   em     		The entity manager to user. Cannot be null.
     * @param   policyId  	The policy id to retrieve suspension levels for. Cannot be null.
     *
     * @return  The list of suspension level for the policy. Will never be null but may be empty.
     */
    public static List<SuspensionLevel> findAllByPolicy(EntityManager em, Long policyId) {
        requireArgument(em != null, "Entity manager can not be null.");
        requireArgument(policyId != null, "Policy id cannot be null.");

        TypedQuery<SuspensionLevel> query = em.createNamedQuery("SuspensionLevel.findAllByPolicy", SuspensionLevel.class);
        
        try {
            query.setParameter("policyId", policyId);
            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
            return query.getResultList();
        } catch (NoResultException ex) {
            return new ArrayList<>(0);
        }
    }
    public Policy getPolicy() {
		return policy;
	}
    
	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public int getLevelNumber() {
		return levelNumber;
	}

	public void setLevelNumber(int levelNumber) {
		this.levelNumber = levelNumber;
	}

	public int getInfractionCount() {
		return infractionCount;
	}

	public void setInfractionCount(int infractionCount) {
		this.infractionCount = infractionCount;
	}

	public long getSuspensionTime() {
		return suspensionTime;
	}

	public void setSuspensionTime(long suspensionTime) {
		this.suspensionTime = suspensionTime;
	}
}
