package com.salesforce.dva.argus.entity;

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
/**
 * The entity encapsulates information about the infraction history.
 *
 * <p>Fields that cannot be null are:</p>
 *
 * <ul>
 *   <li>POLICY_ID</li>
 *   <li>USER_ID</li>
 *   <li>INFRACTION_TIMESTAMP</li>
 *   <li>EXPIRATION_TIMESTAMP</li>
 * </ul>
 *
 * @author  Ruofan Zhang (rzhang@salesforce.com)
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "INFRACTION_HISTORY")
@NamedQueries(
	    {
	        @NamedQuery(
	            name = "InfractionHistory.findByPolicyIdAndUserName", 
	            query = "SELECT r FROM InfractionHistory r WHERE r.policy = :policy AND r.user in (SELECT p.id from PrincipalUser p where p.userName = :userName)"
	        ),
	        
	        @NamedQuery(//need to use order by since result list will include both indefinite and temp suspension
		            name = "InfractionHistory.findByPolicyNameAndUserName", 
		            query = "SELECT r FROM InfractionHistory r WHERE r.policy in (SELECT p.id from Policy p where p.name = :name)"
		            		+ " AND r.user in (SELECT u.id from PrincipalUser u where u.userName = :userName) "
		            		+ "ORDER BY r.infractionTimestamp DESC"
		        ),

	        
	    }
	)
 
public class InfractionHistory extends JPAEntity {
	 	@ManyToOne(targetEntity = Policy.class, fetch = FetchType.LAZY,optional = false)
	    @JoinColumn(name = "policy_id", nullable = false)
	    private Policy policy;    
	 	
	 	@ManyToOne(targetEntity = PrincipalUser.class, fetch = FetchType.LAZY,optional = false)
	    @JoinColumn(name = "user_id", nullable = false)
	    private PrincipalUser user;	 	
	 	
	 	@Basic(optional = false)
		@Column(name = "infraction_timestamp", nullable=false)
	    private Long infractionTimestamp;

	 	@Basic(optional = false)
		@Column(name = "expiration_timestamp", nullable=false, columnDefinition="LONG default '0L'")
	    private Long expirationTimestamp;

	 	//~ Constructors *********************************************************************************************************************************
		
		 /**
	     * Creates a new InfractionHistory object.
	     *
	     * @param  creator      		The creator of this infraction history. Cannot be null.
	     * @param  policyId      		The policy associated with this infraction history. Cannot be null.
	     * @param  user     			The user name associated with this infraction history. Cannot be null.
	     * @param  infractionTimestamp 	The infraction timestamp of this infraction history. Cannot be null.
	     * @param  expirationTimestamp 	The expiration timestamp of this infraction history. Cannot be null.	    
	     */
	    public InfractionHistory(PrincipalUser creator, Policy policy, PrincipalUser user, long infractionTimestamp , long expirationTimestamp) {
			super(creator);
			setPolicy(policy);
			setUser(user);
			setInfractionTimestamp(infractionTimestamp);
			setExpirationTimestamp(expirationTimestamp);
		}
	    
	    /** Creates a new InfractionHistory object. */
	    protected InfractionHistory() {
	        super(null);
	    }
	 	
	  //~ Methods **************************************************************************************************************************************

	    /**
	     * Finds the number of user infractions for a policy-user combination that have occurred since the given start time and end time.
	     *
	     * @param   em         The entity manager to use. Cannot be null.
	     * @param   userName   The user name. Cannot be null.
	     * @param   policyId   The policy id. Cannot be null.
	     * @param   startTime  The start time threshold.
	     *
	     * @return  The number of infractions.
	     */
	    public static int findInfractionCount(EntityManager em, String userName, long policyId, long startTime, long endTime) {    

	        List<InfractionHistory> records = findByPolicyIdAndUserName(em, userName, policyId);	        

	        int count = 0;

	        for ( InfractionHistory record : records) {
	            Long timestamp = record.getInfractionTimestamp();
	           
	                if (timestamp > startTime && timestamp < endTime) {
	                    count++;
	                }
	            
	        }
	        return count;
	    }
	    /**
	     * Find the infraction history for a given user-policy combination.
	     *
	     * @param   em         The EntityManager to use.
	     * @param   userName   The userName for which to retrieve record.
	     * @param   policyName   The policy name for which to retrieve record.
	     *
	     * @return  The infraction history for the given user-policy combination. Null if no such record exists.
	     */
	    public static List<InfractionHistory> findByPolicyNameAndUserName(EntityManager em, String userName, String policyName) {
	        TypedQuery<InfractionHistory> query = em.createNamedQuery("InfractionHistory.findByPolicyNameAndUserName", InfractionHistory.class);

	        try {	        	 
	            query.setParameter("userName", userName);
	            query.setParameter("policyName", policyName);
	            query.setHint("javax.persistence.cache.storeMode", "REFRESH");
	            return query.getResultList();
	        } catch (NoResultException ex) {
	            return null;
	        }
	    }
	    
	    /**
	     * Find the infraction history for a given user-policy combination.
	     *
	     * @param   em         The EntityManager to use.
	     * @param   userName   The userName for which to retrieve record.
	     * @param   policyId   The policy id for which to retrieve record.
	     *
	     * @return  The infraction history for the given user-policy combination. Null if no such record exists.
	     */
	    public static List<InfractionHistory> findByPolicyIdAndUserName(EntityManager em, String userName, long policyId) {
	        TypedQuery<InfractionHistory> query = em.createNamedQuery("InfractionHistory.findByPolicyIdAndUserName", InfractionHistory.class);

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
	     * @return  True if a suspension is active.
	     */
	    public boolean isSuspended() {
	        return System.currentTimeMillis() < getExpirationTimestamp() || isSuspendedIndefinitely();
	    }
	    /**
	     * Indicates if the user is suspended indefinitely.
	     *
	     * @return  True if the user is suspended indefinitely.
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
	 	
	 	
}
