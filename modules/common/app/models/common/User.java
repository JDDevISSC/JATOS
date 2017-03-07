package models.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.common.workers.JatosWorker;

/**
 * Domain model / entity of a user. Used for JSON marshalling and JPA
 * persistance.
 * 
 * @author Kristian Lange
 */
@Entity
@Table(name = "User")
public class User {

	public static final String NAME = "name";
	public static final String EMAIL = "email";
	public static final String PASSWORD = "password";
	public static final String PASSWORD_REPEAT = "passwordRepeat";
	public static final String OLD_PASSWORD = "oldPassword";
	public static final String ADMIN = "admin";

	public enum Role {
		USER, // Normal JATOS user
		ADMIN; // Allows to create/change/delete other users
	}

	/**
	 * Email address is used as ID.
	 */
	@Id
	private String email;

	private String name;

	@ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	private Set<Role> roleList = new HashSet<>();

	/**
	 * Corresponding JatosWorker. This relationship is bidirectional.
	 */
	@JsonIgnore
	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
	private JatosWorker worker;

	// Password is stored as a hash
	@JsonIgnore
	private String passwordHash;

	/**
	 * List of studies this user has access rights to. This relationship is
	 * bidirectional.
	 */
	@ManyToMany(mappedBy = "userList", fetch = FetchType.LAZY)
	private Set<Study> studyList = new HashSet<>();

	public User(String email, String name, String passwordHash) {
		this.email = email;
		this.name = name;
		this.passwordHash = passwordHash;
	}

	public User(String email, String name) {
		this.email = email;
		this.name = name;
	}

	public User() {
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public Set<Role> getRoleList() {
		return roleList;
	}

	public void setRoleList(Set<Role> roleList) {
		this.roleList = roleList;
	}

	public void addRole(Role role) {
		this.roleList.add(role);
	}

	public boolean hasRole(Role role) {
		return roleList.contains(role);
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getPasswordHash() {
		return this.passwordHash;
	}

	public void setStudyList(Set<Study> studyList) {
		this.studyList = studyList;
	}

	public Set<Study> getStudyList() {
		return this.studyList;
	}

	public void addStudy(Study study) {
		this.studyList.add(study);
	}

	public void setWorker(JatosWorker worker) {
		this.worker = worker;
	}

	public JatosWorker getWorker() {
		return this.worker;
	}

	@Override
	public String toString() {
		if (name != null && !name.trim().isEmpty()) {
			return name + " (" + email + ")";
		} else {
			return email;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;
		if (email == null) {
			if (other.getEmail() != null) {
				return false;
			}
		} else if (!email.equals(other.getEmail())) {
			return false;
		}
		return true;
	}

}
