package com.salesforce.dva.argus.entity;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;

/**
 * The entity which encapsulates information about a warden client subscription.
 *
 * <p>
 * Fields that determine uniqueness are:
 * </p>
 *
 * <ul>
 * <li>HOSTNAME</li>
 * <li>PORT</li>
 * </ul>
 *
 * <p>
 * Fields that must be unique are:
 * </p>
 *
 * <ul>
 * <li>HOSTNAME</li>
 * <li>PORT</li>
 * </ul>
 *
 * <p>
 * Fields that cannot be null are:
 * </p>
 *
 * <ul>
 * <li>HOSTNAME</li>
 * <li>PORT</li>
 * </ul>
 *
 * @author Ruofan Zhang (rzhang@salesforce.com)
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "SUBSCRIPTION", uniqueConstraints = @UniqueConstraint(columnNames = { "hostname", "port" }) )
@NamedQueries({
		@NamedQuery(name = "Subscription.findByHostnameAndPort", query = "SELECT r FROM Subscription r WHERE r.hostname = :hostname AND r.port = :port")
})
public class Subscription extends JPAEntity implements Serializable {
	// ~ Instance fields
	// ******************************************************************************************************************************

	@Basic(optional = false)
	@Column(nullable = false)
	private String hostname;

	@Basic(optional = false)
	@Column(nullable = false)
	private Integer port;

	// ~ Constructors
	// *********************************************************************************************************************************

	/**
	 * Creates a new Subscription object.
	 *
	 * @param creator
	 *            The creator of this subscription.
	 * @param hostname
	 *            The hostname for this client subscription. Cannot be null.
	 * @param port
	 *            The port number for this client subscription. Cannot be null.
	 */
	public Subscription(PrincipalUser creator, String hostname, Integer port) {
		super(creator);
		setHostname(hostname);
		setPort(port);
	}

	/** Creates a new Subscription object. */
	protected Subscription() {
		super(null);
	}

	// ~ Methods
	// **************************************************************************************************************************************
	/**
	 * Finds a subscription given its hostname and port.
	 *
	 * @param em
	 *            The entity manager to use. Cannot be null.
	 * @param hostname
	 *            The hostname of the subscription. Cannot be null or empty.
	 * @param port
	 *            The port of the subscription. Cannot be null or empty.
	 *
	 * @return The corresponding subscription or null if no subscription having
	 *         the specified hostname and port combination.
	 */
	public static Subscription findByHostnameAndPort(EntityManager em, String hostname, Integer port) {
		requireArgument(em != null, "Entity manager can not be null.");
		requireArgument(hostname != null && !hostname.isEmpty(), "Hostname cannot be null or empty.");
		requireArgument(port != null, "Port number cannot be null or empty.");

		TypedQuery<Subscription> query = em.createNamedQuery("Subscription.findByHostnameAndPort", Subscription.class);

		try {
			query.setParameter("hostname", hostname);
			query.setParameter("port", port);
			query.setHint("javax.persistence.cache.storeMode", "REFRESH");
			return query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.hostname);
		hash = 97 * hash + Objects.hashCode(this.port);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Subscription other = (Subscription) obj;
		if (!Objects.equals(this.hostname, other.hostname)) {
			return false;
		}
		if (!Objects.equals(this.port, other.port)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		Object[] params = { getHostname(), getPort() };
		String format = "Subscription{ hostname = {0}, port = {1}}";

		return MessageFormat.format(format, params);
	}

}
