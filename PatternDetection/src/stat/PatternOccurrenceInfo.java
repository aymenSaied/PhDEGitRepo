package stat;

import java.util.List;

import soot.Type;
import soot.Unit;

public class PatternOccurrenceInfo {

	public PatternOccurrenceInfo(Unit unit, String type, String comment) {

		this.setUnitOnwhichOccurrenceIsDetected(unit);
		this.setOccurrenceType(type);
		this.setCommentFragment(comment);
		//this.setRestrictedType(rt);

	}

	private Unit unitOnwhichOccurrenceIsDetected;
	private String occurrenceType;
	private String commentFragment;
	//private List<Type> RestrictedType;

	// todo changer le type en enumeration

	public Unit getUnitOnwhichOccurrenceIsDetected() {
		return unitOnwhichOccurrenceIsDetected;
	}

	public void setUnitOnwhichOccurrenceIsDetected(
			Unit unitOnwhichOccurrenceIsDetected) {
		this.unitOnwhichOccurrenceIsDetected = unitOnwhichOccurrenceIsDetected;
	}

	public String getCommentFragment() {
		return commentFragment;
	}

	public void setCommentFragment(String commentFragment) {
		this.commentFragment = commentFragment;
	}

	public String getOccurrenceType() {
		return occurrenceType;
	}

	public void setOccurrenceType(String occurrenceType) {
		this.occurrenceType = occurrenceType;
	}

	
	/*
	public List<Type> getRestrictedType() {
		return RestrictedType;
	}

	public void setRestrictedType(List<Type> restrictedType) {
		RestrictedType = restrictedType;
	}
	
	
	*/

}