package recoder.kit;

public class TransformationNotApplicable extends Problem {
	/**
	 * serialization id
	 */
	private static final long serialVersionUID = -6773985381605479555L;
	private Transformation alternative;
	
	public TransformationNotApplicable() {
		super();
	}
	
	public TransformationNotApplicable(Transformation t) {
		this.alternative = t;
	}
	
	public Transformation getAlternativeTransformation() {
		return alternative;
	}
}
