/**
 * 
 */
package recoder.abstraction;

import java.util.List;

import recoder.ModelException;
import recoder.service.ImplicitElementInfo;
import recoder.service.ProgramModelInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public class ParameterizedField implements Field {
	private final Field genericField;
	private final ParameterizedType parentType;
	private final ImplicitElementInfo service;
	
	/**
	 * 
	 */
	public ParameterizedField(Field genericField, ParameterizedType parentType,
			ImplicitElementInfo service) {
		this.genericField = genericField;
		this.parentType = parentType;
		this.service = service;
	}
	
	public Field getGenericField() {
		return genericField;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Field#getTypeArguments()
	 */
	public List<? extends TypeArgument> getTypeArguments() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Variable#getType()
	 */
	public Type getType() {
		return service.getType(this);
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Variable#isFinal()
	 */
	public boolean isFinal() {
		return genericField.isFinal();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getFullName()
	 */
	public String getFullName() {
		return genericField.getFullName();
	}

	public String getBinaryName() {
		return genericField.getBinaryName();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#getProgramModelInfo()
	 */
	public ImplicitElementInfo getProgramModelInfo() {
		return service;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.ProgramModelElement#setProgramModelInfo(recoder.service.ProgramModelInfo)
	 */
	public void setProgramModelInfo(ProgramModelInfo pmi) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see recoder.NamedModelElement#getName()
	 */
	public String getName() {
		return genericField.getName();
	}

	/* (non-Javadoc)
	 * @see recoder.ModelElement#validate()
	 */
	public void validate() throws ModelException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getAnnotations()
	 */
	public List<? extends AnnotationUse> getAnnotations() {
		return genericField.getAnnotations(); // ??
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#getContainingClassType()
	 */
	public ParameterizedType getContainingClassType() {
		return parentType;
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPrivate()
	 */
	public boolean isPrivate() {
		return genericField.isPrivate();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isProtected()
	 */
	public boolean isProtected() {
		return genericField.isProtected();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isPublic()
	 */
	public boolean isPublic() {
		return genericField.isPublic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStatic()
	 */
	public boolean isStatic() {
		return genericField.isStatic();
	}

	/* (non-Javadoc)
	 * @see recoder.abstraction.Member#isStrictFp()
	 */
	public boolean isStrictFp() {
		return genericField.isStrictFp();
	}

	@Override
	public Field getGenericMember() {
		return genericField;
	}

}
