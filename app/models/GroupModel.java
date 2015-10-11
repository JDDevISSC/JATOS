package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Model and DB entity of a group result.
 * 
 * @author Kristian Lange
 */
@Entity
public class GroupModel {

	@Id
	@GeneratedValue
	private Long id;

	public enum GroupState {
		STARTED, // Group study run was started
		FINISHED; // Group study run is finished
		public static String allStatesAsString() {
			String str = Arrays.toString(values());
			return str.substring(1, str.length() - 1);
		}
	}

	/**
	 * Current group state
	 */
	private GroupState groupState;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id")
	private StudyModel study;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "group_id")
	private List<StudyResult> studyResultList = new ArrayList<>();

	/**
	 * Time and date when the study was started on the server.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss")
	private Timestamp startDate;

	/**
	 * Time and date when the study was finished on the server.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss")
	private Timestamp endDate;

	public GroupModel() {
	}

	public GroupModel(StudyModel study) {
		this.startDate = new Timestamp(new Date().getTime());
		this.study = study;
		this.groupState = GroupState.STARTED;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GroupState getGroupState() {
		return groupState;
	}

	public void setGroupState(GroupState groupState) {
		this.groupState = groupState;
	}

	public StudyModel getStudy() {
		return study;
	}

	public void setStudy(StudyModel study) {
		this.study = study;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getStartDate() {
		return this.startDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public Timestamp getEndDate() {
		return this.endDate;
	}

	public void setStudyResultList(List<StudyResult> studyResultList) {
		this.studyResultList = studyResultList;
	}

	public List<StudyResult> getStudyResultList() {
		return this.studyResultList;
	}

	public void removeStudyResult(StudyResult studyResult) {
		studyResultList.remove(studyResult);
	}

	public void addStudyResult(StudyResult studyResult) {
		studyResultList.add(studyResult);
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (!(obj instanceof GroupModel)) {
			return false;
		}
		GroupModel other = (GroupModel) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.getId())) {
			return false;
		}
		return true;
	}

}
